package com.ruoyi.project.face.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.ImageCompressUtils;
import com.ruoyi.framework.config.RuoYiConfig;
import com.ruoyi.framework.config.properties.LocalFaceProperties;
import com.ruoyi.project.face.domain.CfStudent;
import com.ruoyi.project.face.domain.FaceRecognizeResultVo;
import com.ruoyi.project.face.mapper.CfStudentMapper;
import com.ruoyi.project.face.util.FaceLogSupport;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;

/**
 * Local face-service HTTP client.
 */
@Service
@RequiredArgsConstructor
public class LocalFaceServiceImpl {

    private static final Logger log = LoggerFactory.getLogger(LocalFaceServiceImpl.class);

    private final LocalFaceProperties properties;
    private final CfStudentMapper cfStudentMapper;

    public FaceRecognizeResultVo searchStudent(String imageBase64) {
        byte[] imageBytes = decodeImageBytes(imageBase64);
        imageBytes = compressIfNeeded(imageBytes);
        String url = properties.apiBase() + "/search";
        String raw = postMultipart(url, "file", "query.jpg", imageBytes, "image/jpeg");
        JSONObject resp = parseJson(raw);
        if (!resp.getBooleanValue("query_face_detected")) {
            log.warn("local search no face detected, image={}, resp={}",
                    FaceLogSupport.imageMeta(imageBase64), FaceLogSupport.truncate(raw));
            throw new ServiceException("未检测到人脸");
        }
        JSONArray matches = resp.getJSONArray("matches");
        if (matches == null || matches.isEmpty()) {
            log.warn("local search no match, image={}, threshold={}, resp={}",
                    FaceLogSupport.imageMeta(imageBase64), properties.getSimilarityThreshold(),
                    FaceLogSupport.truncate(raw));
            throw new ServiceException("未匹配到学生");
        }
        JSONObject top = matches.getJSONObject(0);
        double similarity = top.getDoubleValue("similarity");
        if (similarity < properties.getSimilarityThreshold()) {
            log.warn("local search similarity low, image={}, similarity={}, threshold={}, top={}",
                    FaceLogSupport.imageMeta(imageBase64), similarity, properties.getSimilarityThreshold(),
                    FaceLogSupport.truncate(top.toJSONString()));
            throw new ServiceException("人脸相似度不足");
        }
        String externalId = top.getString("external_id");
        if (StringUtils.isEmpty(externalId)) {
            log.warn("local search missing external_id, image={}, top={}",
                    FaceLogSupport.imageMeta(imageBase64), FaceLogSupport.truncate(top.toJSONString()));
            throw new ServiceException("本地人脸库缺少学生编号");
        }
        Long studentId;
        try {
            studentId = Long.valueOf(externalId.trim());
        } catch (NumberFormatException ex) {
            log.warn("local search invalid external_id, image={}, externalId={}, top={}",
                    FaceLogSupport.imageMeta(imageBase64), externalId, FaceLogSupport.truncate(top.toJSONString()));
            throw new ServiceException("本地人脸库学生编号无效: " + externalId);
        }
        CfStudent student = cfStudentMapper.selectCfStudentById(studentId);
        if (student == null) {
            log.warn("local search student missing, image={}, studentId={}, top={}",
                    FaceLogSupport.imageMeta(imageBase64), studentId, FaceLogSupport.truncate(top.toJSONString()));
            throw new ServiceException("匹配到的学生不存在");
        }
        return FaceRecognizeResultVo.builder()
                .studentId(studentId)
                .studentName(student.getName())
                .score(similarity * 100d)
                .build();
    }

    public void registerStudentFace(Long studentId, String imageBase64) {
        CfStudent student = cfStudentMapper.selectCfStudentById(studentId);
        if (student == null) {
            throw new ServiceException("学生不存在");
        }
        byte[] imageBytes = decodeImageBytes(imageBase64);
        imageBytes = compressIfNeeded(imageBytes);
        registerBytes(student, imageBytes);
    }

    public void registerStudentFaceFromAvatar(Long studentId) {
        CfStudent student = cfStudentMapper.selectCfStudentById(studentId);
        if (student == null) {
            throw new ServiceException("学生不存在");
        }
        if (StringUtils.isEmpty(student.getAvatar())) {
            throw new ServiceException("请先采集学生人脸照片");
        }
        try {
            byte[] imageBytes = Files.readAllBytes(resolveAvatarFile(student.getAvatar()).toPath());
            imageBytes = compressIfNeeded(imageBytes);
            registerBytes(student, imageBytes);
        } catch (IOException e) {
            log.error("read student avatar failed, studentId={}, avatar={}, msg={}",
                    studentId, student.getAvatar(), e.getMessage(), e);
            throw new ServiceException("读取学生照片失败");
        }
    }

    private void registerBytes(CfStudent student, byte[] imageBytes) {
        String externalId = String.valueOf(student.getId());
        String name = StringUtils.defaultIfEmpty(student.getName(), externalId);
        // Exists -> reuse person and replace faces; missing -> create.
        JSONObject person = ensurePerson(name, externalId);
        int personId = person.getIntValue("id");
        clearPersonFaces(personId);
        String filename = "face_" + externalId + ".jpg";
        String raw = postMultipart(
                properties.apiBase() + "/persons/" + personId + "/faces",
                "file",
                filename,
                imageBytes,
                "image/jpeg");
        JSONObject face = parseJson(raw);
        if (face.get("id") == null) {
            log.warn("local face register failed, studentId={}, personId={}, imageBytes={}, resp={}",
                    student.getId(), personId, imageBytes == null ? 0 : imageBytes.length,
                    FaceLogSupport.truncate(raw));
            throw new ServiceException("本地人脸注册失败");
        }
        log.info("local face registered(replace), studentId={}, personId={}, faceId={}",
                student.getId(), personId, face.get("id"));
    }

    /** Get or create person by external_id; 409 means already exists -> fetch and reuse. */
    private JSONObject ensurePerson(String name, String externalId) {
        JSONObject person = findPersonByExternalId(externalId);
        if (person != null) {
            return person;
        }
        try {
            return createPerson(name, externalId);
        } catch (ServiceException ex) {
            String msg = ex.getMessage() == null ? "" : ex.getMessage();
            if (msg.contains("409") || msg.contains("external_id") || msg.contains("已存在")) {
                person = findPersonByExternalId(externalId);
                if (person != null) {
                    return person;
                }
                person = findPersonByExternalIdFromList(externalId);
                if (person != null) {
                    return person;
                }
            }
            throw ex;
        }
    }

    private JSONObject findPersonByExternalId(String externalId) {
        try {
            String raw = httpGet(properties.apiBase() + "/persons/by-external-id/" + urlPathEncode(externalId));
            return parseJson(raw);
        } catch (ServiceException ex) {
            String msg = ex.getMessage() == null ? "" : ex.getMessage();
            if (msg.contains("404") || msg.contains("人员不存在") || msg.contains("Not Found")) {
                return findPersonByExternalIdFromList(externalId);
            }
            throw ex;
        }
    }

    private JSONObject findPersonByExternalIdFromList(String externalId) {
        try {
            String raw = httpGet(properties.apiBase() + "/persons");
            JSONArray persons = JSON.parseArray(raw);
            if (persons == null || persons.isEmpty()) {
                return null;
            }
            for (int i = 0; i < persons.size(); i++) {
                JSONObject item = persons.getJSONObject(i);
                if (externalId.equals(item.getString("external_id"))) {
                    return item;
                }
            }
            return null;
        } catch (ServiceException ex) {
            log.warn("list local persons failed, externalIdLookup skipped, msg={}", ex.getMessage());
            return null;
        }
    }

    private JSONObject createPerson(String name, String externalId) {
        JSONObject body = new JSONObject();
        body.put("name", name);
        body.put("external_id", externalId);
        String raw = httpJson("POST", properties.apiBase() + "/persons", body.toJSONString());
        return parseJson(raw);
    }

    private void clearPersonFaces(int personId) {
        String raw = httpGet(properties.apiBase() + "/faces?person_id=" + personId);
        JSONArray faces = JSON.parseArray(raw);
        if (faces == null || faces.isEmpty()) {
            return;
        }
        for (int i = 0; i < faces.size(); i++) {
            int faceId = faces.getJSONObject(i).getIntValue("id");
            httpDelete(properties.apiBase() + "/faces/" + faceId);
        }
    }

    private File resolveAvatarFile(String avatar) {
        String relative = StringUtils.substringAfter(avatar, Constants.RESOURCE_PREFIX);
        if (StringUtils.isEmpty(relative)) {
            relative = avatar;
        }
        File file = new File(RuoYiConfig.getProfile() + relative);
        if (!file.isFile()) {
            throw new ServiceException("学生照片不存在: " + avatar);
        }
        return file;
    }

    private byte[] decodeImageBytes(String imageBase64) {
        String pure = stripBase64Prefix(imageBase64);
        if (StringUtils.isEmpty(pure)) {
            throw new ServiceException("图片数据为空");
        }
        try {
            return Base64.getDecoder().decode(pure);
        } catch (IllegalArgumentException ex) {
            throw new ServiceException("图片数据无效");
        }
    }

    private byte[] compressIfNeeded(byte[] raw) {
        if (raw == null || raw.length <= 200 * 1024) {
            return raw;
        }
        try {
            return ImageCompressUtils.compress(raw, 720, 0.82f);
        } catch (Exception ex) {
            log.warn("local face compress failed, rawBytes={}, msg={}", raw == null ? 0 : raw.length, ex.getMessage());
            return raw;
        }
    }

    private static String stripBase64Prefix(String imageBase64) {
        String value = imageBase64.replaceAll("\\s+", "");
        int comma = value.indexOf(',');
        if (comma > 0 && value.substring(0, comma).toLowerCase(Locale.ROOT).contains("base64")) {
            return value.substring(comma + 1);
        }
        return value;
    }

    private String postMultipart(String url, String fieldName, String filename, byte[] content, String contentType) {
        String boundary = "----FaceBoundary" + UUID.randomUUID().toString().replace("-", "");
        HttpURLConnection conn = null;
        try {
            conn = open(url, "POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            try (OutputStream os = conn.getOutputStream()) {
                writeMultipart(os, boundary, fieldName, filename, content, contentType);
            }
            return readResponse(conn);
        } catch (ServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("local face multipart failed, url={}, filename={}, bytes={}, msg={}",
                    url, filename, content == null ? 0 : content.length, ex.getMessage(), ex);
            throw new ServiceException("本地人脸服务请求失败: " + ex.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private void writeMultipart(OutputStream os, String boundary, String fieldName, String filename,
                                byte[] content, String contentType) throws IOException {
        String header = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + filename + "\"\r\n"
                + "Content-Type: " + contentType + "\r\n\r\n";
        os.write(header.getBytes(StandardCharsets.UTF_8));
        os.write(content);
        os.write("\r\n".getBytes(StandardCharsets.UTF_8));
        os.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        os.flush();
    }

    private String httpJson(String method, String url, String body) {
        HttpURLConnection conn = null;
        try {
            conn = open(url, method);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(bytes);
            }
            return readResponse(conn);
        } catch (ServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("local face json request failed, method={}, url={}, body={}, msg={}",
                    method, url, FaceLogSupport.sanitizePayload(body), ex.getMessage(), ex);
            throw new ServiceException("本地人脸服务请求失败: " + ex.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String httpGet(String url) {
        HttpURLConnection conn = null;
        try {
            conn = open(url, "GET");
            return readResponse(conn);
        } catch (ServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("local face get failed, url={}, msg={}", url, ex.getMessage(), ex);
            throw new ServiceException("本地人脸服务请求失败: " + ex.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private void httpDelete(String url) {
        HttpURLConnection conn = null;
        try {
            conn = open(url, "DELETE");
            readResponse(conn);
        } catch (ServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("local face delete failed, url={}, msg={}", url, ex.getMessage(), ex);
            throw new ServiceException("本地人脸服务请求失败: " + ex.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private HttpURLConnection open(String url, String method) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(properties.getConnectTimeoutMs());
        conn.setReadTimeout(properties.getReadTimeoutMs());
        conn.setRequestProperty("Accept", "application/json");
        return conn;
    }

    private String readResponse(HttpURLConnection conn) throws IOException {
        int code = conn.getResponseCode();
        InputStream stream = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
        String body = "";
        if (stream != null) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                }
                body = sb.toString();
            }
        }
        if (code == 204) {
            return "";
        }
        if (code >= 400) {
            String detail = body;
            try {
                JSONObject err = JSON.parseObject(body);
                if (err != null && err.get("detail") != null) {
                    detail = String.valueOf(err.get("detail"));
                }
            } catch (Exception ignored) {
                // keep raw
            }
            log.warn("local face service http error, code={}, detail={}, body={}",
                    code, detail, FaceLogSupport.truncate(body));
            throw new ServiceException("本地人脸服务错误(" + code + "): " + detail);
        }
        return body;
    }

    private static JSONObject parseJson(String raw) {
        if (StringUtils.isEmpty(raw)) {
            throw new ServiceException("本地人脸服务无响应");
        }
        JSONObject obj = JSON.parseObject(raw);
        if (obj == null) {
            throw new ServiceException("本地人脸服务响应无效");
        }
        return obj;
    }

    private static String urlPathEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8.name()).replace("+", "%20");
        } catch (Exception ex) {
            return value;
        }
    }
}
