package com.ruoyi.project.face.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.BindCodeUtils;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.config.RuoYiConfig;
import com.ruoyi.project.face.domain.CfStudent;
import com.ruoyi.project.face.domain.CfStudentParent;
import com.ruoyi.project.face.domain.FaceCleanupResultVo;
import com.ruoyi.project.face.domain.StudentParentBindVo;
import com.ruoyi.project.face.domain.WxMsgCheckVo;
import com.ruoyi.project.face.mapper.CfStudentMapper;
import com.ruoyi.project.face.service.ICfStudentParentService;
import com.ruoyi.project.face.service.ICfStudentService;
import com.ruoyi.project.face.service.IFaceEngineService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CfStudentServiceImpl implements ICfStudentService {

    private static final Logger log = LoggerFactory.getLogger(CfStudentServiceImpl.class);

    private final CfStudentMapper cfStudentMapper;
    private final ICfStudentParentService cfStudentParentService;
    private final IFaceEngineService faceEngineService;

    @Override
    public CfStudent selectCfStudentById(Long id) {
        CfStudent student = cfStudentMapper.selectCfStudentById(id);
        if (student != null) {
            student.setParents(toBindVoList(cfStudentParentService.selectByStudentId(id)));
        }
        return student;
    }

    @Override
    public List<CfStudent> selectCfStudentList(CfStudent cfStudent) {
        return cfStudentMapper.selectCfStudentList(cfStudent);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CfStudent insertCfStudent(CfStudent cfStudent) {
        prepareBindCode(cfStudent, null);
        cfStudentMapper.insertCfStudent(cfStudent);
        cfStudent.setCreateTime(DateUtils.getNowDate());
        boolean hasAvatar = StringUtils.isNotBlank(cfStudent.getAvatar());
        if (hasAvatar) {
            ensureSingleLocalFace(cfStudent);
        }
        cfStudentMapper.updateCfStudent(cfStudent);
        bindLegacyParentsIfPresent(cfStudent);
        if (hasAvatar) {
            syncFaceEngines(cfStudent.getId());
        }
        return cfStudent;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CfStudent updateCfStudent(CfStudent cfStudent) {
        boolean avatarChanged = StringUtils.isNotBlank(cfStudent.getAvatar()) && isNewAvatarUpload(cfStudent);
        if (avatarChanged) {
            ensureSingleLocalFace(cfStudent);
        }
        if (StringUtils.isNotEmpty(cfStudent.getBindCode())) {
            prepareBindCode(cfStudent, cfStudent.getId());
        }
        cfStudent.setUpdateTime(DateUtils.getNowDate());
        cfStudentMapper.updateCfStudent(cfStudent);
        bindLegacyParentsIfPresent(cfStudent);
        if (avatarChanged) {
            syncFaceEngines(cfStudent.getId());
        }
        return selectCfStudentById(cfStudent.getId());
    }

    /** 按 face.engine 配置同步百度/本地人脸库。 */
    private void syncFaceEngines(Long studentId) {
        if (studentId == null) {
            return;
        }
        faceEngineService.registerStudentFaceFromAvatar(studentId);
    }

    /**
     * 仅当 avatar 相对库中记录发生变化（通常是新上传到 /upload/）时才整理人脸文件。
     * 普通编辑未换图时不碰 face 目录，避免误删。
     */
    private boolean isNewAvatarUpload(CfStudent cfStudent) {
        if (cfStudent.getId() == null) {
            return true;
        }
        CfStudent existing = cfStudentMapper.selectCfStudentById(cfStudent.getId());
        if (existing == null || StringUtils.isEmpty(existing.getAvatar())) {
            return true;
        }
        String incoming = toProfileRelativePath(cfStudent.getAvatar());
        String stored = toProfileRelativePath(existing.getAvatar());
        return !StringUtils.equals(incoming, stored);
    }

    @Override
    public int deleteCfStudentByIds(Long[] ids) {
        return cfStudentMapper.deleteCfStudentByIds(ids);
    }

    @Override
    public int deleteCfStudentById(Long id) {
        return cfStudentMapper.deleteCfStudentById(id);
    }

    @Override
    public List<String> selectAvatars() {
        LambdaQueryWrapper<CfStudent> lqw = new LambdaQueryWrapper<>();
        lqw.select(CfStudent::getAvatar).eq(CfStudent::getDelFlag, 0);
        return cfStudentMapper.selectList(lqw).stream().map(CfStudent::getAvatar).collect(Collectors.toList());
    }

    @Override
    public WxMsgCheckVo getInfoByID(Long studentId) {
        List<WxMsgCheckVo> targets = getNotifyTargetsByStudentId(studentId);
        return CollectionUtils.isEmpty(targets) ? null : targets.get(0);
    }

    @Override
    public List<WxMsgCheckVo> getNotifyTargetsByStudentId(Long studentId) {
        return cfStudentParentService.selectNotifyTargetsByStudentId(studentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CfStudent regenerateBindCode(Long studentId) {
        CfStudent student = cfStudentMapper.selectCfStudentById(studentId);
        if (student == null) {
            throw new ServiceException("学生不存在");
        }
        String bindCode = BindCodeUtils.generateUnique(code -> cfStudentMapper.countByBindCodeExcludeId(code, studentId) > 0);
        student.setBindCode(bindCode);
        student.setUpdateTime(DateUtils.getNowDate());
        cfStudentMapper.updateCfStudent(student);
        return selectCfStudentById(studentId);
    }

    private void bindLegacyParentsIfPresent(CfStudent cfStudent) {
        if (!CollectionUtils.isEmpty(cfStudent.getParents())) {
            cfStudentParentService.replaceStudentParents(cfStudent.getId(), cfStudent.getParents());
            return;
        }
        if (StringUtils.isNotEmpty(cfStudent.getParentTel())) {
            cfStudentParentService.replaceStudentParents(cfStudent.getId(), resolveParents(cfStudent));
        }
    }

    private void prepareBindCode(CfStudent student, Long excludeId) {
        if (StringUtils.isEmpty(student.getBindCode())) {
            student.setBindCode(BindCodeUtils.generateUnique(code -> {
                if (excludeId != null) {
                    return cfStudentMapper.countByBindCodeExcludeId(code, excludeId) > 0;
                }
                return cfStudentMapper.countByBindCode(code) > 0;
            }));
            return;
        }
        student.setBindCode(BindCodeUtils.normalize(student.getBindCode()));
        int count = excludeId == null
                ? cfStudentMapper.countByBindCode(student.getBindCode())
                : cfStudentMapper.countByBindCodeExcludeId(student.getBindCode(), excludeId);
        if (count > 0) {
            throw new ServiceException("绑定码已存在，请更换");
        }
    }

    private List<StudentParentBindVo> resolveParents(CfStudent cfStudent) {
        StudentParentBindVo bindVo = new StudentParentBindVo();
        bindVo.setPhone(cfStudent.getParentTel());
        bindVo.setName(cfStudent.getFamily());
        bindVo.setRelationType("parent");
        bindVo.setIsPrimary(1);
        bindVo.setNotifyEnabled(1);
        List<StudentParentBindVo> parents = new ArrayList<>();
        parents.add(bindVo);
        return parents;
    }

    private List<StudentParentBindVo> toBindVoList(List<CfStudentParent> relations) {
        if (CollectionUtils.isEmpty(relations)) {
            return new ArrayList<>();
        }
        return relations.stream().map(relation -> {
            StudentParentBindVo vo = new StudentParentBindVo();
            vo.setUserId(relation.getUserId());
            vo.setPhone(relation.getPhone());
            vo.setName(relation.getUserName());
            vo.setRelationType(relation.getRelationType());
            vo.setIsPrimary(relation.getIsPrimary());
            vo.setNotifyEnabled(relation.getNotifyEnabled());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public FaceCleanupResultVo cleanupLocalFaces() {
        FaceCleanupResultVo result = new FaceCleanupResultVo();
        File faceDir = new File(RuoYiConfig.getProfile(), "face");
        if (!faceDir.isDirectory()) {
            return result;
        }
        File[] allFiles = faceDir.listFiles(File::isFile);
        if (allFiles == null) {
            allFiles = new File[0];
        }
        result.setScannedFiles(allFiles.length);

        Map<Long, List<File>> filesByStudent = new HashMap<>();
        for (File file : allFiles) {
            Long studentId = parseStudentIdFromFaceFile(file.getName());
            if (studentId == null) {
                continue;
            }
            filesByStudent.computeIfAbsent(studentId, k -> new ArrayList<>()).add(file);
        }

        LambdaQueryWrapper<CfStudent> query = new LambdaQueryWrapper<>();
        query.select(CfStudent::getId, CfStudent::getName, CfStudent::getAvatar)
                .and(w -> w.isNull(CfStudent::getDelFlag).or().eq(CfStudent::getDelFlag, "0"));
        List<CfStudent> students = cfStudentMapper.selectList(query);

        Set<String> keepPaths = new HashSet<>();
        String profile = RuoYiConfig.getProfile();

        for (CfStudent student : students) {
            if (student.getId() == null) {
                continue;
            }
            List<File> candidates = filesByStudent.getOrDefault(student.getId(), new ArrayList<>());
            File keep = resolveInUseFaceFile(student, candidates, profile);

            if (keep == null) {
                if (StringUtils.isNotEmpty(student.getAvatar())) {
                    result.setMissing(result.getMissing() + 1);
                }
                continue;
            }

            File avatarFile = null;
            if (StringUtils.isNotEmpty(student.getAvatar())) {
                avatarFile = new File(profile + toProfileRelativePath(student.getAvatar()));
            }
            boolean repaired = avatarFile == null || !avatarFile.isFile();

            File canonical = toCanonicalFaceFile(student, keep);
            if (!canonical.getParentFile().exists() && !canonical.getParentFile().mkdirs()) {
                log.warn("cleanup cannot create face dir: {}", canonical.getParentFile().getAbsolutePath());
                keepPaths.add(keep.getAbsolutePath());
                result.setKept(result.getKept() + 1);
                continue;
            }
            if (!canonical.getAbsolutePath().equals(keep.getAbsolutePath())) {
                boolean canRename = true;
                if (canonical.exists() && !canonical.delete()) {
                    log.warn("cleanup cannot overwrite canonical face: {}", canonical.getAbsolutePath());
                    canRename = false;
                }
                if (canRename && !keep.renameTo(canonical)) {
                    log.warn("cleanup rename failed, keep original: {} -> {}",
                            keep.getAbsolutePath(), canonical.getAbsolutePath());
                    canonical = keep;
                } else if (!canRename) {
                    canonical = keep;
                }
            }

            String newUrl = Constants.RESOURCE_PREFIX + StringUtils.substringAfter(canonical.getAbsolutePath(), profile);
            newUrl = newUrl.replace('\\', '/');
            if (!StringUtils.equals(student.getAvatar(), newUrl)) {
                CfStudent patch = new CfStudent();
                patch.setId(student.getId());
                patch.setAvatar(newUrl);
                patch.setUpdateTime(DateUtils.getNowDate());
                cfStudentMapper.updateCfStudent(patch);
                if (repaired) {
                    result.setRepaired(result.getRepaired() + 1);
                }
            }

            keepPaths.add(canonical.getAbsolutePath());
            result.setKept(result.getKept() + 1);
        }

        // 重新列举，包含 rename 后的文件
        File[] latestFiles = faceDir.listFiles(File::isFile);
        if (latestFiles == null) {
            latestFiles = new File[0];
        }
        for (File file : latestFiles) {
            if (keepPaths.contains(file.getAbsolutePath())) {
                continue;
            }
            if (file.delete()) {
                result.setDeleted(result.getDeleted() + 1);
            } else {
                log.warn("cleanup delete failed: {}", file.getAbsolutePath());
            }
        }
        return result;
    }

    private File resolveInUseFaceFile(CfStudent student, List<File> candidates, String profile) {
        if (StringUtils.isNotEmpty(student.getAvatar())) {
            File avatarFile = new File(profile + toProfileRelativePath(student.getAvatar()));
            if (avatarFile.isFile()) {
                return avatarFile;
            }
        }
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        return candidates.stream()
                .filter(File::isFile)
                .max(Comparator.comparingLong(File::lastModified))
                .orElse(null);
    }

    private File toCanonicalFaceFile(CfStudent student, File source) {
        String ext = FilenameUtils.getExtension(source.getName());
        if (StringUtils.isEmpty(ext)) {
            ext = "jpg";
        }
        return new File(StringUtils.format("{}/{}/{}_{}.{}",
                RuoYiConfig.getProfile(), "face",
                student.getId(), sanitizeFileNamePart(student.getName()), ext));
    }

    private static Long parseStudentIdFromFaceFile(String fileName) {
        if (StringUtils.isEmpty(fileName)) {
            return null;
        }
        int idx = fileName.indexOf('_');
        if (idx <= 0) {
            return null;
        }
        try {
            return Long.parseLong(fileName.substring(0, idx));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /** 每个学生在 profile/face/ 下只保留一张本地人脸图，重新上传会覆盖旧文件。 */
    private void ensureSingleLocalFace(CfStudent cfStudent) {
        String relative = toProfileRelativePath(cfStudent.getAvatar());
        File current = new File(RuoYiConfig.getProfile() + relative);
        String faceDirPrefix = "/face/" + cfStudent.getId() + "_";

        // 已是该学生人脸路径：文件在则整理为固定名并清多余；文件已被清理时不阻断普通编辑
        if (relative.startsWith(faceDirPrefix)) {
            if (!current.isFile()) {
                log.warn("face file missing, skip relocate, studentId={}, path={}", cfStudent.getId(), relative);
                return;
            }
            renameToCanonicalFaceFile(cfStudent, current);
            return;
        }

        // 新上传（如 /upload/...）必须存在对应文件
        renameToCanonicalFaceFile(cfStudent, current);
    }

    private static String toProfileRelativePath(String avatar) {
        String relative = StringUtils.substringAfter(avatar, Constants.RESOURCE_PREFIX);
        if (StringUtils.isEmpty(relative)) {
            relative = avatar;
        }
        if (!relative.startsWith("/")) {
            relative = "/" + relative;
        }
        return relative.replace('\\', '/');
    }

    private String renameToCanonicalFaceFile(CfStudent cfStudent, File source) {
        if (source == null || !source.isFile()) {
            throw new ServiceException("人脸照片不存在");
        }

        String localPath = RuoYiConfig.getProfile();
        String ext = FilenameUtils.getExtension(source.getName());
        if (StringUtils.isEmpty(ext)) {
            ext = "jpg";
        }
        String safeName = sanitizeFileNamePart(cfStudent.getName());
        // 固定文件名（去掉序号），保证同学生只有一张
        File target = new File(StringUtils.format("{}/{}/{}_{}.{}",
                localPath, "face", cfStudent.getId(), safeName, ext));
        if (!target.getParentFile().exists() && !target.getParentFile().mkdirs()) {
            throw new ServiceException("创建人脸目录失败");
        }

        // 写入前清掉同学生其它人脸，必须保留 source，否则会先把当前照片删掉
        deleteStudentFaceFiles(cfStudent.getId(), source);

        if (!source.getAbsolutePath().equals(target.getAbsolutePath())) {
            if (target.exists() && !target.delete()) {
                log.warn("failed to delete existing face file: {}", target.getAbsolutePath());
            }
            if (!source.renameTo(target)) {
                throw new ServiceException("人脸配置失败");
            }
        }

        String newUrl = Constants.RESOURCE_PREFIX + StringUtils.substringAfter(target.getAbsolutePath(), localPath);
        newUrl = newUrl.replace('\\', '/');
        cfStudent.setAvatar(newUrl);
        deleteStudentFaceFiles(cfStudent.getId(), target);
        return newUrl;
    }

    private void deleteStudentFaceFiles(Long studentId, File keep) {
        if (studentId == null) {
            return;
        }
        File dir = new File(RuoYiConfig.getProfile(), "face");
        if (!dir.isDirectory()) {
            return;
        }
        final String prefix = studentId + "_";
        String keepPath = keep == null ? null : keep.getAbsolutePath();
        File[] files = dir.listFiles((d, name) -> name.startsWith(prefix));
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (keepPath != null && keepPath.equals(file.getAbsolutePath())) {
                continue;
            }
            if (!file.delete()) {
                log.warn("failed to delete obsolete face file: {}", file.getAbsolutePath());
            }
        }
    }

    private static String sanitizeFileNamePart(String name) {
        if (StringUtils.isEmpty(name)) {
            return "student";
        }
        return name.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
    }
}

