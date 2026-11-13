package com.ruoyi.project.face.service.impl;

import com.ruoyi.project.face.domain.WxMsgCheckVo;
import com.ruoyi.project.face.service.IWxPushService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class WxPushServiceImplTest {
    @Autowired
    IWxPushService wxPushService;

    @Test
    void selectAvatars() {
        /*WxMsgCheckVo wxMsgCheckVo = WxMsgCheckVo.builder()
                .openId("oNDkC2BYBxGVtxqGCHz372AoZfF8")
                .checkType("签到")
                .createTime("2025-08-18 13:00:00")
                .studentName("付洺朗")
                .school("育红幼儿园")
                .build();*/
        WxMsgCheckVo wxMsgCheckVo = WxMsgCheckVo.builder()
                .openId("oYMEG6M21dLLBmuUvqu8xOHSgv-g")
                .checkType("签到")
                .createTime("2025-10-22 21:16:17")
                .studentName("付洺朗")
                .school("育红幼儿园")
                .build();
        wxPushService.sendWxMsgCheck(wxMsgCheckVo);
    }
}
