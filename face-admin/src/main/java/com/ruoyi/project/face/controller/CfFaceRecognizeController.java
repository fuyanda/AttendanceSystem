package com.ruoyi.project.face.controller;

import com.ruoyi.framework.aspectj.lang.annotation.Log;
import com.ruoyi.framework.aspectj.lang.enums.BusinessType;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.project.face.domain.FaceRecognizeVo;
import com.ruoyi.project.face.service.IFaceEngineService;
import com.ruoyi.project.face.util.FaceLogSupport;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/face/recognize")
public class CfFaceRecognizeController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(CfFaceRecognizeController.class);

    private final IFaceEngineService faceEngineService;

    @PreAuthorize("@ss.hasPermi('face:checkin:add')")
    @Log(title = "人脸搜索", businessType = BusinessType.OTHER, excludeParamNames = {"imageBase64"})
    @PostMapping
    public AjaxResult recognize(@Validated @RequestBody FaceRecognizeVo request) {
        long t0 = System.currentTimeMillis();
        String imageBase64 = request.getImageBase64();
        int base64Len = imageBase64 == null ? 0 : imageBase64.length();
        long approxBytes = base64Len * 3L / 4L;
        log.info("[PERF][recognize] start base64Len={} payload≈{}KB", base64Len, approxBytes / 1024);
        try {
            AjaxResult result = success(faceEngineService.searchStudent(imageBase64));
            log.info("[PERF][recognize] done total={}ms", System.currentTimeMillis() - t0);
            return result;
        } catch (RuntimeException ex) {
            log.warn("[PERF][recognize] fail total={}ms image={} msg={}",
                    System.currentTimeMillis() - t0, FaceLogSupport.imageMeta(imageBase64), ex.getMessage());
            throw ex;
        }
    }
}
