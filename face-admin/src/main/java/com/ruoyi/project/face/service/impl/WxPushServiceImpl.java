package com.ruoyi.project.face.service.impl;

import com.ruoyi.common.utils.sign.RecordViewSignUtils;
import com.ruoyi.project.face.domain.WxMsgCheckVo;
import com.ruoyi.project.face.domain.WxMsgReviewVo;
import com.ruoyi.project.face.service.IWxPushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WxPushServiceImpl implements IWxPushService {

    private final WxMpService wxMpService;
    @Value("${wx.templateId:}")
    private String templateId;
    /** 兼容 reviewTemplateId / review-template-id 两种写法 */
    @Value("${wx.review-template-id:${wx.reviewTemplateId:}}")
    private String reviewTemplateId;
    @Value("${wx.h5BaseUrl:https://student.example.com}")
    private String h5BaseUrl;
    @Value("${wx.recordSignSecret:}")
    private String recordSignSecret;

    /**
     * 推送打卡消息
     *
     * @param param
     * @return
     */
    @Override
    public String sendWxMsgCheck(WxMsgCheckVo param) {
        WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                .toUser(param.getOpenId())
                .templateId(templateId)
                .url(buildRecordUrl(param))
                .build();

        // 添加模板数据
        templateMessage.addData(param.getCheckTypeData());
        templateMessage.addData(param.getStudentNameData());
        templateMessage.addData(param.getCreateTimeData());
        templateMessage.addData(param.getSchoolData());

        try {
            String msgId = wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);
            log.info("推送微信模板信息：{}", msgId != null ? "成功" : "失败");

            return msgId;
        } catch (WxErrorException e) {
            log.error("wx check push failed, openId={}, recordId={}, recordType={}, student={}, err={}",
                    param.getOpenId(), param.getRecordId(), param.getRecordType(),
                    param.getStudentName(), e.getError(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public String sendWxMsgReview(WxMsgReviewVo param) {
        if (StringUtils.isBlank(reviewTemplateId)) {
            throw new IllegalStateException("未配置 wx.reviewTemplateId，请在 application.yml 填写课堂点评模板ID后重启服务");
        }
        log.info("send review templateId={}, openId={}, itemId={}",
                reviewTemplateId, param.getOpenId(), param.getItemId());
        WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                .toUser(param.getOpenId())
                .templateId(reviewTemplateId.trim())
                .url(buildReviewUrl(param))
                .build();
        templateMessage.addData(param.getStudentNameData());
        templateMessage.addData(param.getSubjectData());
        templateMessage.addData(param.getClassTimeData());
        templateMessage.addData(param.getTeacherData());
        templateMessage.addData(param.getHourDetailData());
        try {
            String msgId = wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);
            log.info("推送课堂点评模板：{}", msgId != null ? "成功" : "失败");
            return msgId;
        } catch (WxErrorException e) {
            log.error("wx review push failed, openId={}, itemId={}, student={}, subject={}, err={}",
                    param.getOpenId(), param.getItemId(), param.getStudentName(),
                    param.getSubjectLabel(), e.getError(), e);
            throw new RuntimeException(e);
        }
    }

    private String buildReviewUrl(WxMsgReviewVo param) {
        String base = StringUtils.removeEnd(h5BaseUrl, "/");
        if (param.getItemId() == null || StringUtils.isBlank(recordSignSecret)) {
            return base + "/class-review";
        }
        long exp = RecordViewSignUtils.defaultExpireEpochSeconds();
        String sign = RecordViewSignUtils.sign(recordSignSecret, param.getItemId(), "review", exp);
        return base + "/class-review?id=" + param.getItemId() + "&exp=" + exp + "&sign=" + sign;
    }

    private String buildRecordUrl(WxMsgCheckVo param) {
        String base = StringUtils.removeEnd(h5BaseUrl, "/");
        if (param.getRecordId() == null || StringUtils.isBlank(recordSignSecret)) {
            return base + "/signin-record";
        }
        String type = StringUtils.defaultIfBlank(param.getRecordType(), "checkin");
        long exp = RecordViewSignUtils.defaultExpireEpochSeconds();
        String sign = RecordViewSignUtils.sign(recordSignSecret, param.getRecordId(), type, exp);
        return base + "/signin-record?id=" + param.getRecordId()
                + "&type=" + type
                + "&exp=" + exp
                + "&sign=" + sign;
    }

    /**
     * 通用消息推送
     *
     * @param openId
     * @param templateId
     * @param url
     * @param data
     */
    @Override
    public String sendTemplateMessage(String openId, String templateId, String url, Map<String, String> data) {
        WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                .toUser(openId)
                .templateId(templateId)
                .url(url)
                .build();

        // 添加模板数据
        data.forEach((key, value) -> {
            templateMessage.addData(new WxMpTemplateData(key, value));
        });

        try {
            String sent = wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);

            return sent;
        } catch (WxErrorException e) {
            log.error("wx template push failed, openId={}, templateId={}, url={}, data={}, err={}",
                    openId, templateId, url, data, e.getError(), e);
            throw new RuntimeException(e);
        }
    }
}
