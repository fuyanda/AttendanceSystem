package com.ruoyi.project.face.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.config.properties.BaiduFaceProperties;
import com.ruoyi.framework.config.properties.FaceEngineProperties;
import com.ruoyi.project.face.domain.CfStudent;
import com.ruoyi.project.face.domain.FaceRecognizeResultVo;
import com.ruoyi.project.face.domain.FaceSyncResultVo;
import com.ruoyi.project.face.mapper.CfStudentMapper;
import com.ruoyi.project.face.service.IBaiduFaceService;
import com.ruoyi.project.face.service.IFaceEngineService;
import com.ruoyi.project.face.util.FaceLogSupport;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Face engine router: search is single-provider; register can dual-write.
 */
@Service
@RequiredArgsConstructor
public class FaceEngineServiceImpl implements IFaceEngineService {

    private static final Logger log = LoggerFactory.getLogger(FaceEngineServiceImpl.class);

    private final FaceEngineProperties engineProperties;
    private final BaiduFaceProperties baiduFaceProperties;
    private final IBaiduFaceService baiduFaceService;
    private final LocalFaceServiceImpl localFaceService;
    private final CfStudentMapper cfStudentMapper;

    @Override
    public FaceRecognizeResultVo searchStudent(String imageBase64) {
        String provider = normalizeProvider(engineProperties.getSearchProvider());
        if ("local".equals(provider)) {
            return localFaceService.searchStudent(imageBase64);
        }
        if ("baidu".equals(provider)) {
            return baiduFaceService.searchStudent(imageBase64);
        }
        log.error("unsupported face search provider, provider={}, image={}",
                engineProperties.getSearchProvider(), FaceLogSupport.imageMeta(imageBase64));
        throw new ServiceException("不支持的人脸搜索提供方: " + engineProperties.getSearchProvider());
    }

    @Override
    public void registerStudentFace(Long studentId, String imageBase64) {
        runRegister(studentId, () -> baiduFaceService.registerStudentFace(studentId, imageBase64),
                () -> localFaceService.registerStudentFace(studentId, imageBase64));
    }

    @Override
    public void registerStudentFaceFromAvatar(Long studentId) {
        runRegister(studentId, () -> baiduFaceService.registerStudentFaceFromAvatar(studentId),
                () -> localFaceService.registerStudentFaceFromAvatar(studentId));
    }

    @Override
    public FaceSyncResultVo syncAllStudentFaces() {
        if (!hasAnyRegisterTarget()) {
            throw new ServiceException("未启用任何人脸注册目标（register-baidu / register-local）");
        }
        CfStudent query = new CfStudent();
        List<CfStudent> students = cfStudentMapper.selectCfStudentList(query);
        int total = 0;
        int success = 0;
        int failed = 0;
        for (CfStudent student : students) {
            if (student == null || student.getId() == null || StringUtils.isEmpty(student.getAvatar())) {
                continue;
            }
            if (student.getDelFlag() != null && !"0".equals(student.getDelFlag())) {
                continue;
            }
            total++;
            try {
                registerStudentFaceFromAvatar(student.getId());
                success++;
            } catch (Exception ex) {
                failed++;
                log.warn("sync face failed, studentId={}, name={}, avatar={}, msg={}",
                        student.getId(), student.getName(), student.getAvatar(), ex.getMessage());
            }
        }
        return FaceSyncResultVo.builder().total(total).success(success).failed(failed).build();
    }

    private void runRegister(Long studentId, Runnable baiduAction, Runnable localAction) {
        boolean wantBaidu = engineProperties.isRegisterBaidu();
        boolean wantLocal = engineProperties.isRegisterLocal();
        if (!wantBaidu && !wantLocal) {
            log.debug("skip face register, no provider enabled, studentId={}", studentId);
            return;
        }

        List<String> errors = new ArrayList<>();
        boolean anyAttempt = false;

        if (wantBaidu) {
            if (baiduFaceProperties != null && baiduFaceProperties.isEnabled()) {
                anyAttempt = true;
                try {
                    baiduAction.run();
                    log.info("baidu face register ok, studentId={}", studentId);
                } catch (Exception ex) {
                    log.warn("baidu face register failed, studentId={}, msg={}", studentId, ex.getMessage());
                    errors.add("百度: " + ex.getMessage());
                }
            } else {
                log.warn("register-baidu=true but baidu.face.enabled=false, skip baidu, studentId={}", studentId);
            }
        }

        if (wantLocal) {
            anyAttempt = true;
            try {
                localAction.run();
                log.info("local face register ok, studentId={}", studentId);
            } catch (Exception ex) {
                log.warn("local face register failed, studentId={}, msg={}", studentId, ex.getMessage());
                errors.add("本地: " + ex.getMessage());
            }
        }

        if (!anyAttempt) {
            throw new ServiceException("无人脸注册目标可用，请检查 face.engine / baidu.face 配置");
        }
        if (!errors.isEmpty()) {
            throw new ServiceException("人脸注册失败: " + String.join("; ", errors));
        }
    }

    private boolean hasAnyRegisterTarget() {
        boolean baiduOk = engineProperties.isRegisterBaidu()
                && baiduFaceProperties != null
                && baiduFaceProperties.isEnabled();
        return baiduOk || engineProperties.isRegisterLocal();
    }

    private static String normalizeProvider(String provider) {
        if (StringUtils.isEmpty(provider)) {
            return "baidu";
        }
        return provider.trim().toLowerCase(Locale.ROOT);
    }
}
