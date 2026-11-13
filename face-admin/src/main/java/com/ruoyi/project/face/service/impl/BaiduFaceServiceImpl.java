package com.ruoyi.project.face.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.ImageCompressUtils;
import com.ruoyi.common.utils.http.HttpUtils;
import com.ruoyi.framework.config.RuoYiConfig;
import com.ruoyi.framework.config.properties.BaiduFaceProperties;
import com.ruoyi.project.face.domain.CfStudent;
import com.ruoyi.project.face.domain.FaceRecognizeResultVo;
import com.ruoyi.project.face.domain.FaceSyncResultVo;
import com.ruoyi.project.face.mapper.CfStudentMapper;
import com.ruoyi.project.face.service.IBaiduFaceService;
import com.ruoyi.project.face.util.FaceLogSupport;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BaiduFaceServiceImpl implements IBaiduFaceService {

    private static final Logger log = LoggerFactory.getLogger(BaiduFaceServiceImpl.class);

    private static final String TOKEN_URL = "https://aip.baidubce.com/oauth/2.0/token";
    private static final String SEARCH_URL = "https://aip.baidubce.com/rest/2.0/face/v3/search";
    private static final String USER_ADD_URL = "https://aip.baidubce.com/rest/2.0/face/v3/faceset/user/add";
    private static final String GROUP_ADD_URL = "https://aip.baidubce.com/rest/2.0/face/v3/faceset/group/add";
    private static final String FACE_GET_LIST_URL = "https://aip.baidubce.com/rest/2.0/face/v3/faceset/face/getlist";
    private static final String FACE_DELETE_URL = "https://aip.baidubce.com/rest/2.0/face/v3/faceset/face/delete";

    private final BaiduFaceProperties properties;
    private final CfStudentMapper cfStudentMapper;

    private volatile String cachedToken;
    private volatile long tokenExpireAt;

    @Override
    public FaceRecognizeResultVo searchStudent(String imageBase64) {
        long t0 = System.currentTimeMillis();
        ensureEnabled();
        long t1 = System.currentTimeMillis();

        String pureBase64 = stripBase64Prefix(imageBase64);
        long approxBytesBefore = pureBase64 == null ? 0 : pureBase64.length() * 3L / 4L;
        pureBase64 = compressBase64IfNeeded(pureBase64);
        long approxBytes = pureBase64 == null ? 0 : pureBase64.length() * 3L / 4L;
        long t2 = System.currentTimeMillis();

        boolean tokenCached = cachedToken != null && System.currentTimeMillis() < tokenExpireAt;
        String token = getAccessToken();
        long t3 = System.currentTimeMillis();

        // urlEncode on large base64 can be costly; compress above keeps it small
        String body = "group_id_list=" + urlEncode(properties.getGroupId())
                + "&image=" + urlEncode(pureBase64)
                + "&image_type=BASE64"
                + "&max_user_num=1";
        long t4 = System.currentTimeMillis();

        String raw = HttpUtils.sendPost(SEARCH_URL + "?access_token=" + token, body);
        long t5 = System.currentTimeMillis();

        JSONObject response = parseResponse(raw);
        JSONObject result = response.getJSONObject("result");
        if (result == null) {
            log.warn("baidu search no result, image={}, groupId={}, resp={}",
                    FaceLogSupport.imageMeta(imageBase64), properties.getGroupId(), FaceLogSupport.truncate(raw));
            throw new ServiceException("未识别到人脸，请重试");
        }
        JSONArray userList = result.getJSONArray("user_list");
        if (userList == null || userList.isEmpty()) {
            log.warn("baidu search empty user_list, image={}, groupId={}, resp={}",
                    FaceLogSupport.imageMeta(imageBase64), properties.getGroupId(), FaceLogSupport.truncate(raw));
            throw new ServiceException("未匹配到学生，请确认已同步人脸库");
        }
        JSONObject best = userList.getJSONObject(0);
        double score = best.getDoubleValue("score");
        if (score < properties.getScoreThreshold()) {
            log.warn("baidu search score low, image={}, score={}, threshold={}, best={}",
                    FaceLogSupport.imageMeta(imageBase64), score, properties.getScoreThreshold(),
                    FaceLogSupport.truncate(best.toJSONString()));
            throw new ServiceException("匹配置信度不足(" + String.format("%.1f", score) + ")，请重试");
        }
        Long studentId = Long.valueOf(best.getString("user_id"));
        CfStudent student = cfStudentMapper.selectCfStudentById(studentId);
        long t6 = System.currentTimeMillis();
        if (student == null) {
            log.warn("baidu search student missing, image={}, studentId={}, best={}",
                    FaceLogSupport.imageMeta(imageBase64), studentId, FaceLogSupport.truncate(best.toJSONString()));
            throw new ServiceException("匹配到的学生不存在");
        }

        log.info("[PERF][baidu-search] payload≈{}KB (beforeCompress≈{}KB) tokenCached={} ensure={}ms strip+compress={}ms token={}ms encode={}ms http={}ms db+parse={}ms total={}ms score={} studentId={}",
                approxBytes / 1024,
                approxBytesBefore / 1024,
                tokenCached,
                t1 - t0,
                t2 - t1,
                t3 - t2,
                t4 - t3,
                t5 - t4,
                t6 - t5,
                t6 - t0,
                String.format("%.1f", score),
                studentId);

        return FaceRecognizeResultVo.builder()
                .studentId(studentId)
                .studentName(student.getName())
                .score(score)
                .build();
    }

    @Override
    public void registerStudentFace(Long studentId, String imageBase64) {
        ensureEnabled();
        ensureGroupExists();
        String pureBase64 = stripBase64Prefix(imageBase64);
        registerStudentFaceInternal(studentId, pureBase64);
    }

    @Override
    public void registerStudentFaceFromAvatar(Long studentId) {
        ensureEnabled();
        CfStudent student = cfStudentMapper.selectCfStudentById(studentId);
        if (student == null) {
            throw new ServiceException("学生不存在");
        }
        if (StringUtils.isEmpty(student.getAvatar())) {
            throw new ServiceException("请先采集学生人脸照片");
        }
        ensureGroupExists();
        try {
            registerStudentFaceInternal(studentId, readAvatarBase64(student.getAvatar()));
        } catch (IOException e) {
            log.error("baidu read avatar failed, studentId={}, avatar={}, msg={}",
                    studentId, student.getAvatar(), e.getMessage(), e);
            throw new ServiceException("读取学生照片失败");
        }
    }

    private void registerStudentFaceInternal(Long studentId, String pureBase64) {
        String token = getAccessToken();
        String image = compressBase64IfNeeded(pureBase64);
        String userId = String.valueOf(studentId);
        // Official param is action_type (not action). REPLACE keeps only the latest face under this user.
        JSONObject body = new JSONObject();
        body.put("user_id", userId);
        body.put("group_id", properties.getGroupId());
        body.put("image", image);
        body.put("image_type", "BASE64");
        body.put("action_type", "REPLACE");
        JSONObject response = parseResponse(postBaiduJson(USER_ADD_URL + "?access_token=" + token, body));
        String keepToken = null;
        JSONObject result = response.getJSONObject("result");
        if (result != null) {
            keepToken = result.getString("face_token");
        }
        // Cleanup historical APPEND leftovers if any still remain.
        ensureSingleFace(userId, keepToken);
    }

    /**
     * Keep at most one face for the user. Prefer keepFaceToken from latest register; otherwise keep the first.
     */
    private void ensureSingleFace(String userId, String keepFaceToken) {
        try {
            String token = getAccessToken();
            JSONObject listBody = new JSONObject();
            listBody.put("user_id", userId);
            listBody.put("group_id", properties.getGroupId());
            JSONObject listResp = JSONObject.parseObject(postBaiduJson(FACE_GET_LIST_URL + "?access_token=" + token, listBody));
            if (listResp == null) {
                return;
            }
            // user not found / empty is fine
            int errorCode = listResp.getIntValue("error_code");
            if (errorCode != 0) {
                return;
            }
            JSONObject result = listResp.getJSONObject("result");
            if (result == null) {
                return;
            }
            JSONArray faceList = result.getJSONArray("face_list");
            if (faceList == null || faceList.size() <= 1) {
                return;
            }
            String keep = keepFaceToken;
            if (StringUtils.isEmpty(keep)) {
                keep = faceList.getJSONObject(0).getString("face_token");
            }
            int deleted = 0;
            for (int i = 0; i < faceList.size(); i++) {
                String faceToken = faceList.getJSONObject(i).getString("face_token");
                if (StringUtils.isEmpty(faceToken) || faceToken.equals(keep)) {
                    continue;
                }
                JSONObject delBody = new JSONObject();
                delBody.put("user_id", userId);
                delBody.put("group_id", properties.getGroupId());
                delBody.put("face_token", faceToken);
                JSONObject delResp = JSONObject.parseObject(postBaiduJson(FACE_DELETE_URL + "?access_token=" + token, delBody));
                if (delResp != null && delResp.getIntValue("error_code") == 0) {
                    deleted++;
                } else {
                    log.warn("delete extra face failed, userId={}, faceToken={}, resp={}",
                            userId, faceToken, delResp);
                }
            }
            if (deleted > 0) {
                log.info("cleaned extra faces for userId={}, kept={}, deleted={}", userId, keep, deleted);
            }
        } catch (Exception ex) {
            log.warn("ensureSingleFace failed, userId={}, groupId={}, keepFaceToken={}, msg={}",
                    userId, properties.getGroupId(), keepFaceToken, ex.getMessage());
        }
    }

    /** Baidu face v3 management APIs expect application/json body. */
    private String postBaiduJson(String url, JSONObject body) {
        HttpURLConnection conn = null;
        try {
            URL realUrl = new URL(url);
            conn = (HttpURLConnection) realUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(60000);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            byte[] bytes = body.toJSONString().getBytes(StandardCharsets.UTF_8);
            conn.setRequestProperty("Content-Length", String.valueOf(bytes.length));
            try (OutputStream os = conn.getOutputStream()) {
                os.write(bytes);
                os.flush();
            }
            int code = conn.getResponseCode();
            java.io.InputStream stream = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
            if (stream == null) {
                return "";
            }
            StringBuilder sb = new StringBuilder();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                }
            }
            String raw = sb.toString();
            log.info("baiduJsonPost code={} url={} recv={}", code, url, raw.length() > 500 ? raw.substring(0, 500) + "..." : raw);
            return raw;
        } catch (Exception e) {
            log.error("baiduJsonPost failed, url={}, body={}, msg={}",
                    url, FaceLogSupport.baiduBodyMeta(body), e.getMessage(), e);
            return "";
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    @Override
    public FaceSyncResultVo syncAllStudentFaces() {
        ensureEnabled();
        ensureGroupExists();
        CfStudent query = new CfStudent();
        query.setDelFlag("0");
        List<CfStudent> students = cfStudentMapper.selectCfStudentList(query);
        int success = 0;
        int failed = 0;
        for (CfStudent student : students) {
            if (StringUtils.isEmpty(student.getAvatar())) {
                failed++;
                continue;
            }
            try {
                String base64 = readAvatarBase64(student.getAvatar());
                registerStudentFace(student.getId(), base64);
                success++;
            } catch (Exception ex) {
                log.warn("sync student face failed, id={}, name={}, avatar={}, msg={}",
                        student.getId(), student.getName(), student.getAvatar(), ex.getMessage());
                failed++;
            }
        }
        return FaceSyncResultVo.builder()
                .total(students.size())
                .success(success)
                .failed(failed)
                .build();
    }

    private void ensureEnabled() {
        if (!properties.isEnabled()) {
            throw new ServiceException("百度云人脸识别未启用，请在 application.yml 配置 baidu.face");
        }
        if (StringUtils.isEmpty(properties.getApiKey()) || StringUtils.isEmpty(properties.getSecretKey())) {
            throw new ServiceException("百度云 API Key 未配置");
        }
    }

    private synchronized String getAccessToken() {
        long now = System.currentTimeMillis();
        if (cachedToken != null && now < tokenExpireAt) {
            return cachedToken;
        }
        long t0 = System.currentTimeMillis();
        String param = "grant_type=client_credentials"
                + "&client_id=" + urlEncode(properties.getApiKey())
                + "&client_secret=" + urlEncode(properties.getSecretKey());
        JSONObject response = JSONObject.parseObject(HttpUtils.sendPost(TOKEN_URL, param));
        if (response == null || !response.containsKey("access_token")) {
            log.error("baidu token failed, resp={}",
                    FaceLogSupport.sanitizePayload(response == null ? null : response.toJSONString()));
            throw new ServiceException("获取百度云 access_token 失败");
        }
        cachedToken = response.getString("access_token");
        long expiresIn = response.getLongValue("expires_in");
        tokenExpireAt = now + Math.max(60, expiresIn - 300) * 1000L;
        log.info("[PERF][baidu-token] refresh cost={}ms expiresIn={}s", System.currentTimeMillis() - t0, expiresIn);
        return cachedToken;
    }

    private void ensureGroupExists() {
        String token = getAccessToken();
        String body = "group_id=" + urlEncode(properties.getGroupId());
        JSONObject response = JSONObject.parseObject(HttpUtils.sendPost(GROUP_ADD_URL + "?access_token=" + token, body));
        if (response == null) {
            return;
        }
        int errorCode = response.getIntValue("error_code");
        String errorMsg = response.getString("error_msg");
        // 223103/223104：人脸库已存在；幂等创建，视为成功
        if (errorCode == 0 || errorCode == 223103 || errorCode == 223104
                || (errorMsg != null && errorMsg.toLowerCase().contains("already exist"))) {
            return;
        }
        log.error("baidu create group failed, groupId={}, errorCode={}, errorMsg={}, resp={}",
                properties.getGroupId(), errorCode, errorMsg,
                FaceLogSupport.truncate(response == null ? null : response.toJSONString()));
        throw new ServiceException("创建百度云人脸库失败: " + errorMsg);
    }

    private JSONObject parseResponse(String raw) {
        if (StringUtils.isEmpty(raw)) {
            log.warn("baidu empty response");
            throw new ServiceException("百度云接口无响应");
        }
        JSONObject response = JSONObject.parseObject(raw);
        int errorCode = response.getIntValue("error_code");
        if (errorCode != 0) {
            log.warn("baidu api error, errorCode={}, errorMsg={}, resp={}",
                    errorCode, response.getString("error_msg"), FaceLogSupport.truncate(raw));
            throw new ServiceException("百度云识别失败: " + response.getString("error_msg"));
        }
        return response;
    }

    private String readAvatarBase64(String avatar) throws IOException {
        String localPath = RuoYiConfig.getProfile()
                + StringUtils.substringAfter(avatar, Constants.RESOURCE_PREFIX);
        File file = new File(localPath);
        if (!file.exists()) {
            throw new ServiceException("学生照片不存在: " + avatar);
        }
        return Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));
    }


    /** Compress oversized face images before calling Baidu (safety net if client skipped). */
    private String compressBase64IfNeeded(String pureBase64) {
        if (StringUtils.isEmpty(pureBase64)) {
            return pureBase64;
        }
        long approxBytes = pureBase64.length() * 3L / 4L;
        if (approxBytes <= 200 * 1024L) {
            return pureBase64;
        }
        long t0 = System.currentTimeMillis();
        try {
            byte[] raw = Base64.getDecoder().decode(pureBase64);
            byte[] compressed = ImageCompressUtils.compress(raw, 720, 0.82f);
            String out = Base64.getEncoder().encodeToString(compressed);
            log.info("[PERF][baidu-compress] {}KB -> {}KB cost={}ms",
                    approxBytes / 1024, compressed.length / 1024, System.currentTimeMillis() - t0);
            return out;
        } catch (Exception ex) {
            log.warn("[PERF][baidu-compress] fail cost={}ms msg={}", System.currentTimeMillis() - t0, ex.getMessage());
            return pureBase64;
        }
    }

    private static String stripBase64Prefix(String imageBase64) {
        String value = imageBase64.replaceAll("\\s+", "");
        int comma = value.indexOf(',');
        if (comma > 0 && value.substring(0, comma).contains("base64")) {
            return value.substring(comma + 1);
        }
        return value;
    }

    private static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (Exception ex) {
            throw new ServiceException("参数编码失败");
        }
    }
}
