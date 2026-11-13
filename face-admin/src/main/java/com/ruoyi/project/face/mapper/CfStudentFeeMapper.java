package com.ruoyi.project.face.mapper;

import com.ruoyi.project.face.domain.CfStudentFee;
import com.ruoyi.project.face.domain.CfStudentFeeConsume;
import com.ruoyi.project.face.domain.CfStudentFeeRefund;
import com.ruoyi.project.face.domain.CfStudentFeeSchedule;
import com.ruoyi.project.face.domain.FeeLedgerItemVo;
import com.ruoyi.project.face.domain.FeeLedgerQuery;
import com.ruoyi.project.face.domain.FeeLedgerSummaryVo;
import com.ruoyi.project.face.domain.FeeTeacherVo;
import com.ruoyi.project.face.domain.ScheduleStudentOption;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CfStudentFeeMapper {
    CfStudentFee selectCfStudentFeeById(Long id);

    List<CfStudentFee> selectCfStudentFeeList(CfStudentFee query);

    List<CfStudentFee> selectActiveByStudentId(@Param("studentId") Long studentId);

    int insertCfStudentFee(CfStudentFee fee);

    int updateCfStudentFee(CfStudentFee fee);

    int deleteCfStudentFeeByIds(Long[] ids);

    int incrementUsedHours(@Param("id") Long id, @Param("hours") int hours);

    List<CfStudentFeeSchedule> selectSchedulesByFeeId(@Param("feeId") Long feeId);

    int deleteSchedulesByFeeId(@Param("feeId") Long feeId);

    int batchInsertSchedules(@Param("list") List<CfStudentFeeSchedule> list);

    int insertConsume(CfStudentFeeConsume consume);

    int insertRefund(CfStudentFeeRefund refund);

    int applyRefund(@Param("id") Long id, @Param("hours") int hours, @Param("amount") java.math.BigDecimal amount,
                    @Param("closeIfEmpty") boolean closeIfEmpty, @Param("updateBy") String updateBy,
                    @Param("updateTime") java.util.Date updateTime);

    List<FeeTeacherVo> selectTeachers(@Param("deptId") Long deptId);

    FeeTeacherVo selectTeacherByUserId(@Param("userId") Long userId);

    /** 有效报课该老师的学生（可选姓名模糊） */
    List<ScheduleStudentOption> selectEnrolledStudentsByTeacher(@Param("teacherUserId") Long teacherUserId,
                                                                @Param("name") String name);

    int countUserPostByCode(@Param("userId") Long userId, @Param("postCode") String postCode);

    List<String> selectSubjectsByTeacher(@Param("teacherUserId") Long teacherUserId);

    List<CfStudentFee> selectConsumableByTeacherAndSubject(@Param("teacherUserId") Long teacherUserId,
                                                           @Param("subject") String subject);

    List<FeeLedgerItemVo> selectLedgerList(FeeLedgerQuery query);

    FeeLedgerSummaryVo selectLedgerSummary(FeeLedgerQuery query);
}
