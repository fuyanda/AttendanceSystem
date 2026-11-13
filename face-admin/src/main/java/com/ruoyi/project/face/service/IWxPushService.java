package com.ruoyi.project.face.service;

import com.ruoyi.project.face.domain.WxMsgCheckVo;
import com.ruoyi.project.face.domain.WxMsgReviewVo;
import java.util.Map;

public interface IWxPushService {
    /**
     * 推送打卡消息
     * @param param
     * @return
     */
    String sendWxMsgCheck(WxMsgCheckVo param);

    /**
     * 推送课堂点评消息（独立模板，不与签到混用）
     */
    String sendWxMsgReview(WxMsgReviewVo param);

    /**
     * 通用消息推送
     * @param openId
     * @param templateId
     * @param url
     * @param data
     */
    String sendTemplateMessage(String openId, String templateId, String url,
                             Map<String, String> data);
}
