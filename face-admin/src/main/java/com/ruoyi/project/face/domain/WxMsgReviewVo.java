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
public class WxMsgReviewVo {
    private String openId;
    private String studentName;
    private String subjectLabel;
    private String classTime;
    private String teacherName;
    /** 课时详情，对应模板 thing10 */
    private String hourDetail;
    private Long itemId;

    public WxMpTemplateData getStudentNameData() {
        return new WxMpTemplateData("thing17", truncate(studentName, 20));
    }

    public WxMpTemplateData getSubjectData() {
        return new WxMpTemplateData("thing2", truncate(subjectLabel, 20));
    }

    public WxMpTemplateData getClassTimeData() {
        return new WxMpTemplateData("time8", classTime);
    }

    public WxMpTemplateData getTeacherData() {
        // phrase 类型最多 5 个汉字
        return new WxMpTemplateData("phrase6", truncate(teacherName, 5));
    }

    public WxMpTemplateData getHourDetailData() {
        return new WxMpTemplateData("thing10", truncate(hourDetail, 20));
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max);
    }
}
