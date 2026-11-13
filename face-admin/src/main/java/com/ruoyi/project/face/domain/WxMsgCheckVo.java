package com.ruoyi.project.face.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WxMsgCheckVo {
    // 打卡类型
    private String checkType;
    // 学生姓名
    private String studentName;
    // 打开时间
    private String createTime;
    // 打卡校区
    private String school;
    // 消息ID
    private String msgId;
    //
    private String openId;
    /** 打卡记录 ID，用于推送跳转详情页 */
    private Long recordId;
    /** 记录类型：checkin / checkout */
    private String recordType;

    public WxMpTemplateData getCheckTypeData() {
        return new WxMpTemplateData("thing7", checkType);
    }

    public WxMpTemplateData getStudentNameData() {
        return new WxMpTemplateData("thing5", studentName);
    }

    public WxMpTemplateData getCreateTimeData() {
        return new WxMpTemplateData("time3", createTime);
    }

    public WxMpTemplateData getSchoolData() {
        return new WxMpTemplateData("thing8", school);
    }

    public WxMpTemplateData getMsgIdData() {
        return new WxMpTemplateData("msgId", msgId);
    }
}
