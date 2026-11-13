package com.ruoyi.project.face.service;

/**
 * 打卡时段校验：同一时间段内仅允许打卡一次
 */
public interface IPunchWindowService {

    void assertCheckinAllowed(Long studentId);

    void assertCheckoutAllowed(Long studentId);

    boolean isCheckoutAllowed(Long studentId);
}
