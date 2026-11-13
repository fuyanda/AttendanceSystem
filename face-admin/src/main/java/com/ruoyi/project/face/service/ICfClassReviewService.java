package com.ruoyi.project.face.service;

import com.ruoyi.project.face.domain.CfClassReviewSession;
import com.ruoyi.project.face.domain.ClassReviewCreateRequest;
import com.ruoyi.project.face.domain.ClassReviewInfoVo;
import com.ruoyi.project.face.domain.ClassReviewSaveRequest;
import com.ruoyi.project.face.domain.ClassReviewSubjectVo;
import com.ruoyi.project.face.domain.ClassReviewTodayVo;
import com.ruoyi.project.face.domain.FeeBatchConsumeRequest;

import java.util.List;
import java.util.Map;

public interface ICfClassReviewService {

    Long createSessionAfterConsume(FeeBatchConsumeRequest request, Map<Long, Long> feeIdToConsumeId);

    /**
     * 不依赖报课/消课，为学员创建当天单人点评草稿（若已存在则直接返回）。
     */
    ClassReviewTodayVo createStandalone(ClassReviewCreateRequest request);

    List<ClassReviewSubjectVo> listSubjects();

    CfClassReviewSession selectSessionDetail(Long sessionId);

    List<CfClassReviewSession> selectSessionList(CfClassReviewSession query);

    ClassReviewTodayVo selectTodayByStudentId(Long studentId);

    void saveDraft(ClassReviewSaveRequest request);

    void sendSession(Long sessionId);

    ClassReviewInfoVo getParentReviewInfo(Long itemId);
}
