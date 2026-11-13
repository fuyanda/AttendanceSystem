package com.ruoyi.project.face.controller;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.sign.RecordViewSignUtils;
import com.ruoyi.framework.aspectj.lang.annotation.Anonymous;
import com.ruoyi.framework.config.ServerConfig;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.project.face.domain.CheckInfoVo;
import com.ruoyi.project.face.service.ICfCheckinService;
import com.ruoyi.project.face.service.ICfCheckoutService;
import com.ruoyi.project.face.service.ICfClassReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * \u5bb6\u957f\u7aef H5 \u6253\u5361\u8be6\u60c5
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/face/parent")
public class ParentRecordController extends BaseController {

    private static final String MSG_RECORD_NOT_FOUND =
            "\u8bb0\u5f55\u4e0d\u5b58\u5728\u6216\u5df2\u8d85\u8fc730\u5929\u4e0d\u53ef\u67e5\u770b";

    private final ICfCheckinService cfCheckinService;
    private final ICfCheckoutService cfCheckoutService;
    private final ICfClassReviewService cfClassReviewService;

    @Value("${wx.apiBaseUrl:}")
    private String apiBaseUrl;

    @Value("${wx.recordSignSecret:}")
    private String recordSignSecret;

    @Anonymous
    @GetMapping("/record")
    public AjaxResult getRecord(@RequestParam Long id,
                                @RequestParam(defaultValue = "checkin") String type,
                                @RequestParam Long exp,
                                @RequestParam String sign,
                                HttpServletRequest request) {
        if (!RecordViewSignUtils.verify(recordSignSecret, id, type, exp, sign)) {
            return error(MSG_RECORD_NOT_FOUND);
        }

        CheckInfoVo info;
        if ("checkout".equalsIgnoreCase(type)) {
            info = cfCheckoutService.getCheckInfo(id);
        } else {
            info = cfCheckinService.getCheckInfo(id);
        }
        if (info == null) {
            return error(MSG_RECORD_NOT_FOUND);
        }
        info.setAvatar(resolveAvatarUrl(info.getAvatar(), request));
        return success(info);
    }

    @Anonymous
    @GetMapping("/review")
    public AjaxResult getReview(@RequestParam Long id,
                                @RequestParam Long exp,
                                @RequestParam String sign) {
        if (!RecordViewSignUtils.verify(recordSignSecret, id, "review", exp, sign)) {
            return error(MSG_RECORD_NOT_FOUND);
        }
        com.ruoyi.project.face.domain.ClassReviewInfoVo info = cfClassReviewService.getParentReviewInfo(id);
        if (info == null) {
            return error(MSG_RECORD_NOT_FOUND);
        }
        return success(info);
    }

    private String resolveAvatarUrl(String avatar, HttpServletRequest request) {
        if (StringUtils.isEmpty(avatar) || avatar.startsWith("http")) {
            return avatar;
        }
        String base = StringUtils.isNotBlank(apiBaseUrl)
                ? StringUtils.removeEnd(apiBaseUrl, "/")
                : ServerConfig.getDomain(request);
        return base + avatar;
    }
}
