package com.ruoyi.project.face.service;

import com.ruoyi.project.face.domain.CfStudentFee;
import com.ruoyi.project.face.domain.FeeBatchConsumeRequest;
import com.ruoyi.project.face.domain.FeeBatchConsumeResultVo;
import com.ruoyi.project.face.domain.FeeConsumeContextVo;
import com.ruoyi.project.face.domain.FeeConsumeRequest;
import com.ruoyi.project.face.domain.FeeRefundRequest;
import com.ruoyi.project.face.domain.FeeConsumeStudentVo;
import com.ruoyi.project.face.domain.FeeLedgerItemVo;
import com.ruoyi.project.face.domain.FeeLedgerQuery;
import com.ruoyi.project.face.domain.FeeLedgerSummaryVo;
import com.ruoyi.project.face.domain.FeeOptionVo;
import com.ruoyi.project.face.domain.FeeTeacherSelectVo;
import com.ruoyi.project.face.domain.FeeTeacherVo;

import java.util.List;

public interface ICfStudentFeeService {
    CfStudentFee selectCfStudentFeeById(Long id);

    List<CfStudentFee> selectCfStudentFeeList(CfStudentFee query);

    List<CfStudentFee> selectByStudentId(Long studentId);

    List<FeeOptionVo> selectOptions(Long studentId);

    List<FeeTeacherVo> selectTeachers(Long deptId);

    FeeTeacherSelectVo getTeacherSelectContext(Long deptId);

    int insertCfStudentFee(CfStudentFee fee);

    int updateCfStudentFee(CfStudentFee fee);

    int deleteCfStudentFeeByIds(Long[] ids);

    void consumeManual(FeeConsumeRequest request);

    void consumeForCheckin(Long feeId, Long checkinId, int hours);

    FeeConsumeContextVo getConsumeContext();

    List<String> selectSubjectsByTeacher(Long teacherUserId);

    List<FeeConsumeStudentVo> selectConsumableStudents(Long teacherUserId, String subject);

    FeeBatchConsumeResultVo batchConsume(FeeBatchConsumeRequest request);

    void refund(FeeRefundRequest request);

    List<FeeLedgerItemVo> selectLedgerList(FeeLedgerQuery query);

    FeeLedgerSummaryVo selectLedgerSummary(FeeLedgerQuery query);
}
