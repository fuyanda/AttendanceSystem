package com.ruoyi.project.face.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.DictUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.security.LoginUser;
import com.ruoyi.project.face.domain.CfStudentFee;
import com.ruoyi.project.face.domain.CfStudentFeeConsume;
import com.ruoyi.project.face.domain.CfStudentFeeRefund;
import com.ruoyi.project.face.domain.CfStudentFeeSchedule;
import com.ruoyi.project.face.domain.FeeBatchConsumeRequest;
import com.ruoyi.project.face.domain.FeeBatchConsumeResultVo;
import com.ruoyi.project.face.domain.FeeConsumeContextVo;
import com.ruoyi.project.face.domain.FeeConsumeRequest;
import com.ruoyi.project.face.domain.FeeLedgerItemVo;
import com.ruoyi.project.face.domain.FeeLedgerQuery;
import com.ruoyi.project.face.domain.FeeLedgerSummaryVo;
import com.ruoyi.project.face.domain.FeeRefundRequest;
import com.ruoyi.project.face.domain.FeeConsumeStudentVo;
import com.ruoyi.project.face.domain.FeeOptionVo;
import com.ruoyi.project.face.domain.FeeTeacherSelectVo;
import com.ruoyi.project.face.domain.FeeTeacherVo;
import com.ruoyi.project.face.mapper.CfStudentFeeMapper;
import com.ruoyi.project.face.service.ICfClassReviewService;
import com.ruoyi.project.face.service.ICfScheduleService;
import com.ruoyi.project.face.service.ICfStudentFeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class CfStudentFeeServiceImpl implements ICfStudentFeeService {

    private static final String DICT_SUBJECT = "cf_fee_subject";
    private static final String[] WEEK_LABELS = {"", "\u5468\u4E00", "\u5468\u4E08", "\u5468\u4E09", "\u5468\u56DB", "\u5468\u4E94", "\u5468\u516D", "\u5468\u65E5"};
    private static final String CAMPUS_LEADER_POST_CODE = "campus_leader";

    private final CfStudentFeeMapper cfStudentFeeMapper;
    private final ICfClassReviewService cfClassReviewService;
    private final ICfScheduleService cfScheduleService;

    @Override
    public CfStudentFee selectCfStudentFeeById(Long id) {
        CfStudentFee fee = cfStudentFeeMapper.selectCfStudentFeeById(id);
        if (fee != null) {
            enrichFee(fee, true);
        }
        return fee;
    }

    @Override
    public List<CfStudentFee> selectCfStudentFeeList(CfStudentFee query) {
        List<CfStudentFee> list = cfStudentFeeMapper.selectCfStudentFeeList(query);
        list.forEach(f -> enrichFee(f, true));
        return list;
    }

    @Override
    public List<CfStudentFee> selectByStudentId(Long studentId) {
        List<CfStudentFee> list = cfStudentFeeMapper.selectActiveByStudentId(studentId);
        list.forEach(f -> enrichFee(f, true));
        return list;
    }

    @Override
    public List<FeeOptionVo> selectOptions(Long studentId) {
        List<CfStudentFee> list = cfStudentFeeMapper.selectActiveByStudentId(studentId);
        int todayWeek = LocalDate.now().getDayOfWeek().getValue();
        List<FeeOptionVo> options = new ArrayList<>();
        for (CfStudentFee fee : list) {
            enrichFee(fee, true);
            int remaining = remainingHours(fee);
            if (remaining <= 0) {
                continue;
            }
            FeeOptionVo vo = new FeeOptionVo();
            vo.setFeeId(fee.getId());
            vo.setTeacherUserId(fee.getTeacherUserId());
            vo.setSubject(fee.getSubject());
            vo.setSubjectLabel(DictUtils.getDictLabel(DICT_SUBJECT, fee.getSubject()));
            vo.setTotalHours(fee.getTotalHours());
            vo.setUsedHours(fee.getUsedHours());
            vo.setRemainingHours(remaining);
            vo.setTeacherName(fee.getTeacherName());
            vo.setScheduleSummary(fee.getScheduleSummary());
            boolean scheduledToday = fee.getSchedules() != null && fee.getSchedules().stream()
                    .anyMatch(s -> s.getWeekDay() != null && s.getWeekDay() == todayWeek);
            vo.setScheduledToday(scheduledToday);
            options.add(vo);
        }
        options.sort(Comparator
                .comparing(FeeOptionVo::getScheduledToday, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(FeeOptionVo::getRemainingHours, Comparator.reverseOrder()));
        return options;
    }

    @Override
    public List<FeeTeacherVo> selectTeachers(Long deptId) {
        return resolveTeacherOptions(deptId);
    }

    @Override
    public FeeTeacherSelectVo getTeacherSelectContext(Long deptId) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        FeeTeacherSelectVo ctx = new FeeTeacherSelectVo();
        ctx.setUserId(SecurityUtils.getUserId());
        ctx.setDeptId(deptId != null ? deptId : loginUser.getDeptId());
        if (loginUser.getUser() != null) {
            ctx.setNickName(loginUser.getUser().getNickName());
        }
        ctx.setCanSelectTeacher(canSelectTeacher());
        ctx.setDefaultTeacherUserId(SecurityUtils.getUserId());
        ctx.setTeachers(resolveTeacherOptions(deptId));
        return ctx;
    }

    private List<FeeTeacherVo> resolveTeacherOptions(Long deptId) {
        if (canSelectTeacher()) {
            return cfStudentFeeMapper.selectTeachers(deptId);
        }
        FeeTeacherVo self = cfStudentFeeMapper.selectTeacherByUserId(SecurityUtils.getUserId());
        if (self != null) {
            return Collections.singletonList(self);
        }
        LoginUser loginUser = SecurityUtils.getLoginUser();
        FeeTeacherVo fallback = new FeeTeacherVo();
        fallback.setUserId(SecurityUtils.getUserId());
        if (loginUser != null && loginUser.getUser() != null) {
            fallback.setUserName(loginUser.getUser().getUserName());
            fallback.setNickName(loginUser.getUser().getNickName());
            fallback.setDeptId(loginUser.getDeptId());
        }
        return Collections.singletonList(fallback);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertCfStudentFee(CfStudentFee fee) {
        validateFee(fee, true);
        fee.setUsedHours(fee.getUsedHours() == null ? 0 : fee.getUsedHours());
        fee.setStatus(StringUtils.isEmpty(fee.getStatus()) ? "0" : fee.getStatus());
        fee.setDelFlag("0");
        fee.setCreateTime(DateUtils.getNowDate());
        fee.setCreateBy(SecurityUtils.getUsername());
        int rows = cfStudentFeeMapper.insertCfStudentFee(fee);
        saveSchedules(fee.getId(), fee.getSchedules());
        syncScheduleFromFee(fee);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateCfStudentFee(CfStudentFee fee) {
        CfStudentFee existing = cfStudentFeeMapper.selectCfStudentFeeById(fee.getId());
        if (existing == null) {
            throw new ServiceException("缴费记录不存在");
        }
        validateFee(fee, false);
        if (fee.getTotalHours() != null && fee.getTotalHours() < existing.getUsedHours()) {
            throw new ServiceException("总课时不能小于已消耗课时");
        }
        fee.setUpdateTime(DateUtils.getNowDate());
        fee.setUpdateBy(SecurityUtils.getUsername());
        int rows = cfStudentFeeMapper.updateCfStudentFee(fee);
        if (fee.getSchedules() != null) {
            cfStudentFeeMapper.deleteSchedulesByFeeId(fee.getId());
            saveSchedules(fee.getId(), fee.getSchedules());
            syncScheduleFromFee(fee);
        }
        return rows;
    }

    @Override
    public int deleteCfStudentFeeByIds(Long[] ids) {
        return cfStudentFeeMapper.deleteCfStudentFeeByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void consumeManual(FeeConsumeRequest request) {
        if (request.getFeeId() == null) {
            throw new ServiceException("请选择缴费记录");
        }
        int hours = request.getHours() == null || request.getHours() < 1 ? 1 : request.getHours();
        doConsume(request.getFeeId(), hours, "manual", null, request.getRemark(), null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void consumeForCheckin(Long feeId, Long checkinId, int hours) {
        doConsume(feeId, hours, "checkin", checkinId, null, DateUtils.getNowDate());
    }

    @Override
    public FeeConsumeContextVo getConsumeContext() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        FeeConsumeContextVo ctx = new FeeConsumeContextVo();
        ctx.setUserId(SecurityUtils.getUserId());
        ctx.setDeptId(loginUser.getDeptId());
        if (loginUser.getUser() != null) {
            ctx.setNickName(loginUser.getUser().getNickName());
        }
        ctx.setCanSelectTeacher(canSelectTeacher());
        ctx.setDefaultTeacherUserId(SecurityUtils.getUserId());
        ctx.setTeachers(resolveTeacherOptions(null));
        return ctx;
    }

    @Override
    public List<String> selectSubjectsByTeacher(Long teacherUserId) {
        assertTeacherAccess(teacherUserId);
        return cfStudentFeeMapper.selectSubjectsByTeacher(teacherUserId);
    }

    @Override
    public List<FeeConsumeStudentVo> selectConsumableStudents(Long teacherUserId, String subject) {
        assertTeacherAccess(teacherUserId);
        if (StringUtils.isEmpty(subject)) {
            throw new ServiceException("请选择学科");
        }
        List<CfStudentFee> list = cfStudentFeeMapper.selectConsumableByTeacherAndSubject(teacherUserId, subject);
        List<FeeConsumeStudentVo> result = new ArrayList<>();
        for (CfStudentFee fee : list) {
            enrichFee(fee, true);
            FeeConsumeStudentVo vo = new FeeConsumeStudentVo();
            vo.setFeeId(fee.getId());
            vo.setStudentId(fee.getStudentId());
            vo.setStudentName(fee.getStudentName());
            vo.setSubject(fee.getSubject());
            vo.setSubjectLabel(DictUtils.getDictLabel(DICT_SUBJECT, fee.getSubject()));
            vo.setTotalHours(fee.getTotalHours());
            vo.setUsedHours(fee.getUsedHours());
            vo.setRemainingHours(fee.getRemainingHours());
            vo.setScheduleSummary(fee.getScheduleSummary());
            result.add(vo);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FeeBatchConsumeResultVo batchConsume(FeeBatchConsumeRequest request) {
        if (request.getTeacherUserId() == null) {
            throw new ServiceException("请选择任课老师");
        }
        if (StringUtils.isEmpty(request.getSubject())) {
            throw new ServiceException("请选择学科");
        }
        if (request.getConsumeTime() == null) {
            throw new ServiceException("请选择消课时间");
        }
        if (CollectionUtils.isEmpty(request.getFeeIds())) {
            throw new ServiceException("请选择至少一名学生");
        }
        assertTeacherAccess(request.getTeacherUserId());
        int hours = request.getHours() == null || request.getHours() < 1 ? 1 : request.getHours();
        Map<Long, Long> feeIdToConsumeId = new HashMap<>();
        for (Long feeId : request.getFeeIds()) {
            CfStudentFee fee = cfStudentFeeMapper.selectCfStudentFeeById(feeId);
            if (fee == null || !request.getTeacherUserId().equals(fee.getTeacherUserId())
                    || !request.getSubject().equals(fee.getSubject())) {
                throw new ServiceException("所选学生与当前老师或学科不匹配");
            }
            Long consumeId = doConsume(feeId, hours, "manual", null, request.getRemark(), request.getConsumeTime());
            feeIdToConsumeId.put(feeId, consumeId);
        }
        Long sessionId = cfClassReviewService.createSessionAfterConsume(request, feeIdToConsumeId);
        FeeBatchConsumeResultVo result = new FeeBatchConsumeResultVo();
        result.setSessionId(sessionId);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refund(FeeRefundRequest request) {
        if (request.getFeeId() == null) {
            throw new ServiceException("请选择缴费记录");
        }
        CfStudentFee fee = cfStudentFeeMapper.selectCfStudentFeeById(request.getFeeId());
        if (fee == null || !"0".equals(fee.getStatus())) {
            throw new ServiceException("缴费记录不存在或已关闭");
        }
        int remaining = remainingHours(fee);
        if (remaining <= 0) {
            throw new ServiceException("无可退课时");
        }
        int hours = request.getHours() == null || request.getHours() < 1 ? remaining : request.getHours();
        if (hours > remaining) {
            throw new ServiceException("退费课时不能超过剩余课时");
        }
        BigDecimal amount = request.getAmount();
        BigDecimal maxRefundAmount = calcRefundAmount(fee.getTotalAmount(), fee.getTotalHours(), remaining);
        if (amount == null) {
            amount = calcRefundAmount(fee.getTotalAmount(), fee.getTotalHours(), hours);
        } else if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException("退费金额须大于0");
        }
        if (amount.compareTo(maxRefundAmount) > 0) {
            throw new ServiceException("退费金额不能超过剩余课时费");
        }
        boolean closeIfEmpty = hours == remaining;
        Date now = DateUtils.getNowDate();
        int updated = cfStudentFeeMapper.applyRefund(fee.getId(), hours, amount, closeIfEmpty,
                SecurityUtils.getUsername(), now);
        if (updated == 0) {
            throw new ServiceException("退费失败，请刷新后重试");
        }
        CfStudentFeeRefund refund = new CfStudentFeeRefund();
        refund.setFeeId(fee.getId());
        refund.setHours(hours);
        refund.setAmount(amount);
        refund.setRefundTime(now);
        refund.setRemark(request.getRemark());
        refund.setCreateBy(SecurityUtils.getUsername());
        refund.setCreateTime(now);
        cfStudentFeeMapper.insertRefund(refund);
    }

    @Override
    public List<FeeLedgerItemVo> selectLedgerList(FeeLedgerQuery query) {
        List<FeeLedgerItemVo> list = cfStudentFeeMapper.selectLedgerList(query);
        list.forEach(this::enrichLedgerItem);
        return list;
    }

    @Override
    public FeeLedgerSummaryVo selectLedgerSummary(FeeLedgerQuery query) {
        FeeLedgerSummaryVo summary = cfStudentFeeMapper.selectLedgerSummary(query);
        if (summary == null) {
            summary = new FeeLedgerSummaryVo();
        }
        if (summary.getPayAmount() == null) {
            summary.setPayAmount(BigDecimal.ZERO);
        }
        if (summary.getConsumeAmount() == null) {
            summary.setConsumeAmount(BigDecimal.ZERO);
        }
        if (summary.getRefundAmount() == null) {
            summary.setRefundAmount(BigDecimal.ZERO);
        }
        summary.setNetAmount(summary.getPayAmount().subtract(summary.getRefundAmount()));
        return summary;
    }

    private void enrichLedgerItem(FeeLedgerItemVo item) {
        if (item == null) {
            return;
        }
        item.setSubjectLabel(DictUtils.getDictLabel(DICT_SUBJECT, item.getSubject()));
        if ("pay".equals(item.getBizType())) {
            item.setBizTypeLabel("缴费");
        } else if ("consume".equals(item.getBizType())) {
            item.setBizTypeLabel("消课");
        } else if ("refund".equals(item.getBizType())) {
            item.setBizTypeLabel("退费");
        } else {
            item.setBizTypeLabel(item.getBizType());
        }
    }

    private BigDecimal calcRefundAmount(BigDecimal totalAmount, Integer totalHours, int refundHours) {
        if (totalAmount == null || totalHours == null || totalHours < 1) {
            return BigDecimal.ZERO;
        }
        return totalAmount.multiply(BigDecimal.valueOf(refundHours))
                .divide(BigDecimal.valueOf(totalHours), 2, RoundingMode.HALF_UP);
    }

    private Long doConsume(Long feeId, int hours, String source, Long checkinId, String remark, Date consumeTime) {
        CfStudentFee fee = cfStudentFeeMapper.selectCfStudentFeeById(feeId);
        if (fee == null || !"0".equals(fee.getStatus())) {
            throw new ServiceException("缴费记录不存在或已失效");
        }
        if (remainingHours(fee) < hours) {
            throw new ServiceException("剩余课时不足");
        }
        int updated = cfStudentFeeMapper.incrementUsedHours(feeId, hours);
        if (updated == 0) {
            throw new ServiceException("消课失败，请重试");
        }
        CfStudentFeeConsume consume = new CfStudentFeeConsume();
        consume.setFeeId(feeId);
        consume.setHours(hours);
        consume.setSource(source);
        consume.setCheckinId(checkinId);
        consume.setConsumeTime(consumeTime != null ? consumeTime : DateUtils.getNowDate());
        consume.setRemark(remark);
        consume.setCreateBy(SecurityUtils.getUsername());
        consume.setCreateTime(DateUtils.getNowDate());
        cfStudentFeeMapper.insertConsume(consume);
        return consume.getId();
    }

    private void validateFee(CfStudentFee fee, boolean isNew) {
        if (fee.getStudentId() == null) {
            throw new ServiceException("学生不能为空");
        }
        if (StringUtils.isEmpty(fee.getSubject())) {
            throw new ServiceException("学科不能为空");
        }
        if (fee.getTotalHours() == null || fee.getTotalHours() < 1) {
            throw new ServiceException("总课时至少为1");
        }
        if (fee.getTotalAmount() == null) {
            throw new ServiceException("总金额不能为空");
        }
        if (fee.getTeacherUserId() == null) {
            throw new ServiceException("\u8BF7\u9009\u62E9\u4EFB\u8BFE\u8001\u5E08");
        }
        assertTeacherAccess(fee.getTeacherUserId());
        if (!isNew && fee.getUsedHours() != null && fee.getTotalHours() != null
                && fee.getUsedHours() > fee.getTotalHours()) {
            throw new ServiceException("已消耗课时不能大于总课时");
        }
    }

    private void saveSchedules(Long feeId, List<CfStudentFeeSchedule> schedules) {
        if (CollectionUtils.isEmpty(schedules)) {
            return;
        }
        for (CfStudentFeeSchedule s : schedules) {
            s.setFeeId(feeId);
            if (StringUtils.isEmpty(s.getStartTime()) || StringUtils.isEmpty(s.getEndTime())) {
                throw new ServiceException("排课时间段请填写开始和结束时间");
            }
            if (s.getEndTime().compareTo(s.getStartTime()) <= 0) {
                throw new ServiceException("排课结束时间须晚于开始时间");
            }
        }
        cfStudentFeeMapper.batchInsertSchedules(schedules);
    }

    /** 按最新模板展开具体日期场次；已编辑场次不被覆盖 */
    private void syncScheduleFromFee(CfStudentFee fee) {
        List<CfStudentFeeSchedule> schedules = cfStudentFeeMapper.selectSchedulesByFeeId(fee.getId());
        fee.setSchedules(schedules);
        cfScheduleService.syncFromFee(fee);
    }

    private void enrichFee(CfStudentFee fee, boolean loadSchedules) {
        fee.setRemainingHours(remainingHours(fee));
        if (loadSchedules) {
            List<CfStudentFeeSchedule> schedules = cfStudentFeeMapper.selectSchedulesByFeeId(fee.getId());
            fee.setSchedules(schedules);
            fee.setScheduleSummary(buildScheduleSummary(schedules));
        }
    }

    private int remainingHours(CfStudentFee fee) {
        int total = fee.getTotalHours() == null ? 0 : fee.getTotalHours();
        int used = fee.getUsedHours() == null ? 0 : fee.getUsedHours();
        return Math.max(0, total - used);
    }

    private String buildScheduleSummary(List<CfStudentFeeSchedule> schedules) {
        if (CollectionUtils.isEmpty(schedules)) {
            return "";
        }
        CfStudentFeeSchedule s = schedules.stream()
                .sorted(Comparator.comparing(CfStudentFeeSchedule::getWeekDay)
                        .thenComparing(CfStudentFeeSchedule::getStartTime))
                .findFirst()
                .orElse(null);
        if (s == null) {
            return "";
        }
        int wd = s.getWeekDay() == null ? 0 : s.getWeekDay();
        String label = wd >= 1 && wd <= 7 ? WEEK_LABELS[wd] : "";
        String timeRange = StringUtils.isNotEmpty(s.getStartTime()) ? s.getStartTime() : "";
        if (StringUtils.isNotEmpty(s.getEndTime())) {
            timeRange += "-" + s.getEndTime();
        }
        return label + timeRange;
    }

    private boolean canSelectTeacher() {
        Long userId = SecurityUtils.getUserId();
        if (SecurityUtils.isAdmin(userId)) {
            return true;
        }
        if (SecurityUtils.hasRole("admin")) {
            return true;
        }
        return cfStudentFeeMapper.countUserPostByCode(userId, CAMPUS_LEADER_POST_CODE) > 0;
    }

    private void assertTeacherAccess(Long teacherUserId) {
        if (canSelectTeacher()) {
            return;
        }
        if (!SecurityUtils.getUserId().equals(teacherUserId)) {
            throw new ServiceException("\u65E0\u6743\u4E3A\u5176\u4ED6\u8001\u5E08\u6D88\u8BFE");
        }
    }
}
