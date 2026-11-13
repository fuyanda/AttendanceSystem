package com.ruoyi.project.face.mapper;

import com.ruoyi.project.face.domain.CfScheduleSession;
import com.ruoyi.project.face.domain.CfScheduleStudent;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface CfScheduleMapper {

    CfScheduleSession selectSessionById(@Param("id") Long id);

    List<CfScheduleSession> selectTeacherCalendar(@Param("teacherUserId") Long teacherUserId,
                                                  @Param("beginTime") Date beginTime,
                                                  @Param("endTime") Date endTime);

    List<CfScheduleSession> selectStudentCalendar(@Param("studentId") Long studentId,
                                                  @Param("beginTime") Date beginTime,
                                                  @Param("endTime") Date endTime);

    List<CfScheduleStudent> selectStudentsBySessionId(@Param("sessionId") Long sessionId);

    int countTeacherConflict(@Param("teacherUserId") Long teacherUserId,
                             @Param("startTime") Date startTime,
                             @Param("endTime") Date endTime,
                             @Param("excludeId") Long excludeId);

    int countStudentConflict(@Param("studentId") Long studentId,
                             @Param("startTime") Date startTime,
                             @Param("endTime") Date endTime,
                             @Param("excludeId") Long excludeId);

    CfScheduleSession selectByFeeScheduleAndStart(@Param("feeScheduleId") Long feeScheduleId,
                                                  @Param("startTime") Date startTime);

    /** 缴费模板重插后 fee_schedule_id 会变，用 fee_id+开始时刻识别已编辑场次 */
    CfScheduleSession selectByFeeIdAndStart(@Param("feeId") Long feeId,
                                            @Param("startTime") Date startTime);

    List<CfScheduleSession> selectUneditedByFeeId(@Param("feeId") Long feeId);

    int insertSession(CfScheduleSession session);

    int updateSession(CfScheduleSession session);

    int softDeleteSession(@Param("id") Long id, @Param("updateBy") String updateBy, @Param("updateTime") Date updateTime);

    int softDeleteUneditedByFeeId(@Param("feeId") Long feeId, @Param("updateBy") String updateBy, @Param("updateTime") Date updateTime);

    int deleteStudentsBySessionId(@Param("sessionId") Long sessionId);

    int batchInsertStudents(@Param("list") List<CfScheduleStudent> list);
}
