package com.ruoyi.project.face.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.DictUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.project.face.domain.CfScheduleSession;
import com.ruoyi.project.face.domain.CfScheduleStudent;
import com.ruoyi.project.face.domain.CfStudentFee;
import com.ruoyi.project.face.domain.CfStudentFeeSchedule;
import com.ruoyi.project.face.domain.FeeTeacherVo;
import com.ruoyi.project.face.domain.ScheduleBatchResultVo;
import com.ruoyi.project.face.domain.ScheduleBatchSaveRequest;
import com.ruoyi.project.face.domain.ScheduleSaveRequest;
import com.ruoyi.project.face.domain.ScheduleStudentOption;
import com.ruoyi.project.face.domain.ScheduleTimeSlotRequest;
import com.ruoyi.project.face.mapper.CfScheduleMapper;
import com.ruoyi.project.face.mapper.CfStudentFeeMapper;
import com.ruoyi.project.face.mapper.CfStudentMapper;
import com.ruoyi.project.face.service.ICfScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CfScheduleServiceImpl implements ICfScheduleService {

    private static final String DICT_SUBJECT = "cf_fee_subject";
    private static final int SYNC_WEEKS = 8;
    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("H:mm");
    private static final DateTimeFormatter HM2 = DateTimeFormatter.ofPattern("HH:mm");

    private final CfScheduleMapper cfScheduleMapper;
    private final CfStudentFeeMapper cfStudentFeeMapper;
    private final CfStudentMapper cfStudentMapper;

    @Override
    public List<FeeTeacherVo> selectTeachers() {
        return cfStudentFeeMapper.selectTeachers(null);
    }

    @Override
    public List<ScheduleStudentOption> selectScheduleStudents(String name) {
        String keyword = StringUtils.isEmpty(name) ? null : name.trim();
        return cfStudentMapper.selectActiveStudentOptions(keyword);
    }

    @Override
    public List<CfScheduleSession> selectTeacherCalendar(Long teacherUserId, Date beginTime, Date endTime) {
        assertRange(beginTime, endTime);
        List<CfScheduleSession> list = cfScheduleMapper.selectTeacherCalendar(teacherUserId, beginTime, endTime);
        list.forEach(this::enrichDisplay);
        return list;
    }

    @Override
    public List<CfScheduleSession> selectStudentCalendar(Long studentId, Date beginTime, Date endTime) {
        if (studentId == null) {
            throw new ServiceException("请选择学生");
        }
        assertRange(beginTime, endTime);
        List<CfScheduleSession> list = cfScheduleMapper.selectStudentCalendar(studentId, beginTime, endTime);
        list.forEach(this::enrichDisplay);
        return list;
    }

    @Override
    public CfScheduleSession selectSessionDetail(Long id) {
        CfScheduleSession session = cfScheduleMapper.selectSessionById(id);
        if (session == null) {
            throw new ServiceException("排课不存在");
        }
        enrichDisplay(session);
        List<CfScheduleStudent> students = cfScheduleMapper.selectStudentsBySessionId(id);
        session.setStudents(students);
        session.setStudentIds(students.stream().map(CfScheduleStudent::getStudentId).collect(Collectors.toList()));
        return session;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSession(ScheduleSaveRequest request) {
        return doCreateSession(request);
    }

    @Override
    public ScheduleBatchResultVo createSessionsBatch(ScheduleBatchSaveRequest request) {
        if (request == null) {
            throw new ServiceException("请求不能为空");
        }
        if (request.getTeacherUserId() == null) {
            throw new ServiceException("请选择老师");
        }
        if (StringUtils.isEmpty(request.getSubject())) {
            throw new ServiceException("请选择科目");
        }
        if (CollectionUtils.isEmpty(request.getStudentIds())) {
            throw new ServiceException("请选择学生");
        }
        if (CollectionUtils.isEmpty(request.getSlots())) {
            throw new ServiceException("请添加上课时段");
        }
        ScheduleBatchResultVo result = new ScheduleBatchResultVo();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        for (ScheduleTimeSlotRequest slot : request.getSlots()) {
            if (slot == null || slot.getStartTime() == null || slot.getEndTime() == null) {
                result.setFailCount(result.getFailCount() + 1);
                result.getFailMessages().add("存在未填写完整的时段");
                continue;
            }
            if (!slot.getEndTime().after(slot.getStartTime())) {
                result.setFailCount(result.getFailCount() + 1);
                result.getFailMessages().add(fmt.format(slot.getStartTime()) + " 结束时间须晚于开始时间");
                continue;
            }
            ScheduleSaveRequest one = new ScheduleSaveRequest();
            one.setTeacherUserId(request.getTeacherUserId());
            one.setSubject(request.getSubject());
            one.setRemark(request.getRemark());
            one.setStudentIds(request.getStudentIds());
            one.setStartTime(slot.getStartTime());
            one.setEndTime(slot.getEndTime());
            try {
                doCreateSession(one);
                result.setSuccessCount(result.getSuccessCount() + 1);
            } catch (ServiceException ex) {
                result.setFailCount(result.getFailCount() + 1);
                result.getFailMessages().add(fmt.format(slot.getStartTime()) + " " + ex.getMessage());
            }
        }
        if (result.getSuccessCount() == 0) {
            throw new ServiceException("全部时段创建失败：" + String.join("；", result.getFailMessages()));
        }
        return result;
    }

    private Long doCreateSession(ScheduleSaveRequest request) {
        validateSaveRequest(request, true);
        assertNoConflict(request.getTeacherUserId(), request.getStudentIds(),
                request.getStartTime(), request.getEndTime(), null);

        Date now = DateUtils.getNowDate();
        String username = SecurityUtils.getUsername();
        CfScheduleSession session = new CfScheduleSession();
        session.setTeacherUserId(request.getTeacherUserId());
        session.setSubject(request.getSubject());
        session.setStartTime(request.getStartTime());
        session.setEndTime(request.getEndTime());
        session.setRemark(request.getRemark());
        session.setSource("manual");
        session.setEditedFlag("0");
        session.setDelFlag("0");
        session.setCreateBy(username);
        session.setCreateTime(now);
        cfScheduleMapper.insertSession(session);
        replaceStudents(session.getId(), request.getStudentIds(), username, now);
        return session.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSession(ScheduleSaveRequest request) {
        if (request.getId() == null) {
            throw new ServiceException("缺少排课ID");
        }
        CfScheduleSession existing = cfScheduleMapper.selectSessionById(request.getId());
        if (existing == null) {
            throw new ServiceException("排课不存在");
        }
        validateSaveRequest(request, false);
        assertNoConflict(request.getTeacherUserId(), request.getStudentIds(),
                request.getStartTime(), request.getEndTime(), request.getId());

        Date now = DateUtils.getNowDate();
        String username = SecurityUtils.getUsername();
        CfScheduleSession update = new CfScheduleSession();
        update.setId(request.getId());
        update.setTeacherUserId(request.getTeacherUserId());
        update.setSubject(request.getSubject());
        update.setStartTime(request.getStartTime());
        update.setEndTime(request.getEndTime());
        update.setRemark(request.getRemark() == null ? "" : request.getRemark());
        update.setEditedFlag("1");
        update.setUpdateBy(username);
        update.setUpdateTime(now);
        cfScheduleMapper.updateSession(update);
        replaceStudents(request.getId(), request.getStudentIds(), username, now);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSession(Long id) {
        CfScheduleSession existing = cfScheduleMapper.selectSessionById(id);
        if (existing == null) {
            throw new ServiceException("排课不存在");
        }
        cfScheduleMapper.softDeleteSession(id, SecurityUtils.getUsername(), DateUtils.getNowDate());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncFromFee(CfStudentFee fee) {
        if (fee == null || fee.getId() == null) {
            return;
        }
        if (fee.getTeacherUserId() == null || fee.getStudentId() == null || StringUtils.isEmpty(fee.getSubject())) {
            return;
        }
        Date now = DateUtils.getNowDate();
        String username = StringUtils.isNotEmpty(fee.getUpdateBy()) ? fee.getUpdateBy()
                : (StringUtils.isNotEmpty(fee.getCreateBy()) ? fee.getCreateBy() : "system");

        // 先软删本缴费下未编辑的同步场次，再按当前模板重建
        cfScheduleMapper.softDeleteUneditedByFeeId(fee.getId(), username, now);

        List<CfStudentFeeSchedule> templates = fee.getSchedules();
        if (CollectionUtils.isEmpty(templates)) {
            return;
        }

        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusWeeks(SYNC_WEEKS);
        for (CfStudentFeeSchedule tpl : templates) {
            if (tpl.getId() == null || tpl.getWeekDay() == null
                    || StringUtils.isEmpty(tpl.getStartTime()) || StringUtils.isEmpty(tpl.getEndTime())) {
                continue;
            }
            LocalTime startHm = parseHm(tpl.getStartTime());
            LocalTime endHm = parseHm(tpl.getEndTime());
            if (startHm == null || endHm == null || !endHm.isAfter(startHm)) {
                continue;
            }
            for (LocalDate day = today; !day.isAfter(endDate); day = day.plusDays(1)) {
                if (day.getDayOfWeek().getValue() != tpl.getWeekDay()) {
                    continue;
                }
                Date start = toDate(LocalDateTime.of(day, startHm));
                Date end = toDate(LocalDateTime.of(day, endHm));

                // 模板重插后 fee_schedule_id 会变，按缴费+开始时刻识别已编辑场次
                CfScheduleSession existed = cfScheduleMapper.selectByFeeIdAndStart(fee.getId(), start);
                if (existed != null && "1".equals(existed.getEditedFlag())) {
                    continue;
                }

                CfScheduleSession session = new CfScheduleSession();
                session.setTeacherUserId(fee.getTeacherUserId());
                session.setSubject(fee.getSubject());
                session.setStartTime(start);
                session.setEndTime(end);
                session.setSource("fee");
                session.setFeeId(fee.getId());
                session.setFeeScheduleId(tpl.getId());
                session.setEditedFlag("0");
                session.setDelFlag("0");
                session.setRemark("");
                session.setCreateBy(username);
                session.setCreateTime(now);
                try {
                    assertNoConflict(fee.getTeacherUserId(),
                            java.util.Collections.singletonList(fee.getStudentId()), start, end, null);
                } catch (ServiceException ex) {
                    // 同步遇冲突跳过该日，不阻断缴费保存
                    continue;
                }
                cfScheduleMapper.insertSession(session);
                replaceStudents(session.getId(),
                        java.util.Collections.singletonList(fee.getStudentId()), username, now);
            }
        }
    }

    private void validateSaveRequest(ScheduleSaveRequest request, boolean creating) {
        if (request.getTeacherUserId() == null) {
            throw new ServiceException("请选择老师");
        }
        if (StringUtils.isEmpty(request.getSubject())) {
            throw new ServiceException("请选择科目");
        }
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new ServiceException("请选择上课时间");
        }
        if (!request.getEndTime().after(request.getStartTime())) {
            throw new ServiceException("结束时间须晚于开始时间");
        }
        if (CollectionUtils.isEmpty(request.getStudentIds())) {
            throw new ServiceException("请选择学生");
        }
    }

    private void assertNoConflict(Long teacherUserId, List<Long> studentIds,
                                  Date startTime, Date endTime, Long excludeId) {
        if (cfScheduleMapper.countTeacherConflict(teacherUserId, startTime, endTime, excludeId) > 0) {
            throw new ServiceException("该老师在此时段已有排课");
        }
        if (CollectionUtils.isEmpty(studentIds)) {
            return;
        }
        for (Long studentId : studentIds) {
            if (studentId == null) {
                continue;
            }
            if (cfScheduleMapper.countStudentConflict(studentId, startTime, endTime, excludeId) > 0) {
                throw new ServiceException("存在学生在此时段已有其他课程");
            }
        }
    }

    private void replaceStudents(Long sessionId, List<Long> studentIds, String username, Date now) {
        cfScheduleMapper.deleteStudentsBySessionId(sessionId);
        List<CfScheduleStudent> rows = new ArrayList<>();
        for (Long studentId : studentIds) {
            if (studentId == null) {
                continue;
            }
            CfScheduleStudent row = new CfScheduleStudent();
            row.setSessionId(sessionId);
            row.setStudentId(studentId);
            row.setCreateBy(username);
            row.setCreateTime(now);
            rows.add(row);
        }
        if (!rows.isEmpty()) {
            cfScheduleMapper.batchInsertStudents(rows);
        }
    }

    private void enrichDisplay(CfScheduleSession session) {
        session.setSubjectLabel(DictUtils.getDictLabel(DICT_SUBJECT, session.getSubject()));
        session.setPeriodLabel(resolvePeriodLabel(session.getStartTime()));
    }

    private String resolvePeriodLabel(Date startTime) {
        if (startTime == null) {
            return "";
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(startTime);
        return cal.get(Calendar.HOUR_OF_DAY) < 12 ? "上午" : "下午";
    }

    private void assertRange(Date beginTime, Date endTime) {
        if (beginTime == null || endTime == null) {
            throw new ServiceException("请选择日期范围");
        }
        if (!endTime.after(beginTime)) {
            throw new ServiceException("结束日期须晚于开始日期");
        }
    }

    private LocalTime parseHm(String text) {
        try {
            String t = text.trim();
            if (t.length() == 4) {
                t = "0" + t;
            }
            if (t.length() >= 5) {
                t = t.substring(0, 5);
            }
            try {
                return LocalTime.parse(t, HM2);
            } catch (Exception e) {
                return LocalTime.parse(t, HM);
            }
        } catch (Exception e) {
            return null;
        }
    }

    private Date toDate(LocalDateTime ldt) {
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }
}
