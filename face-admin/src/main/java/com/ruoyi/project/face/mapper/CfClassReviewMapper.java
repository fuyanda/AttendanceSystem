package com.ruoyi.project.face.mapper;

import com.ruoyi.project.face.domain.CfClassReviewItem;
import com.ruoyi.project.face.domain.CfClassReviewSession;
import com.ruoyi.project.face.domain.ClassReviewInfoVo;
import com.ruoyi.project.face.domain.ClassReviewTodayVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CfClassReviewMapper {

    CfClassReviewSession selectSessionById(@Param("id") Long id);

    ClassReviewTodayVo selectTodayByStudentId(@Param("studentId") Long studentId,
                                              @Param("teacherUserId") Long teacherUserId);

    List<CfClassReviewSession> selectSessionList(CfClassReviewSession query);

    int insertSession(CfClassReviewSession session);

    int updateSession(CfClassReviewSession session);

    List<CfClassReviewItem> selectItemsBySessionId(@Param("sessionId") Long sessionId);

    CfClassReviewItem selectItemById(@Param("id") Long id);

    int insertItem(CfClassReviewItem item);

    int updateItem(CfClassReviewItem item);

    int updateItemPushFlag(@Param("id") Long id, @Param("pushFlag") String pushFlag);

    ClassReviewInfoVo selectReviewInfoByItemId(@Param("itemId") Long itemId);
}
