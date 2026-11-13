package com.ruoyi.project.face.service;

import com.ruoyi.project.face.domain.CfScheduleSession;
import com.ruoyi.project.face.domain.CfStudentFee;
import com.ruoyi.project.face.domain.FeeTeacherVo;
import com.ruoyi.project.face.domain.ScheduleBatchResultVo;
import com.ruoyi.project.face.domain.ScheduleBatchSaveRequest;
import com.ruoyi.project.face.domain.ScheduleSaveRequest;
import com.ruoyi.project.face.domain.ScheduleStudentOption;

import java.util.Date;
import java.util.List;

public interface ICfScheduleService {

    /** 排课公开：全部教师角色用户，不受缴费 canSelectTeacher 限制 */
    List<FeeTeacherVo> selectTeachers();

    /** 未归档学生；name 为空返回全部，否则按姓名模糊 */
    List<ScheduleStudentOption> selectScheduleStudents(String name);

    /** teacherUserId 为空时返回全部老师排课 */
    List<CfScheduleSession> selectTeacherCalendar(Long teacherUserId, Date beginTime, Date endTime);

    List<CfScheduleSession> selectStudentCalendar(Long studentId, Date beginTime, Date endTime);

    CfScheduleSession selectSessionDetail(Long id);

    Long createSession(ScheduleSaveRequest request);

    ScheduleBatchResultVo createSessionsBatch(ScheduleBatchSaveRequest request);

    void updateSession(ScheduleSaveRequest request);

    void deleteSession(Long id);

    /** 缴费保存后：按每周模板展开未来约 8 周场次 */
    void syncFromFee(CfStudentFee fee);
}
