package com.ruoyi.project.face.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.project.face.domain.CfPunchSettings;
import com.ruoyi.project.face.domain.CfStudent;
import com.ruoyi.project.face.mapper.CfCheckinMapper;
import com.ruoyi.project.face.mapper.CfCheckoutMapper;
import com.ruoyi.project.face.mapper.CfPunchSettingsMapper;
import com.ruoyi.project.face.service.ICfStudentService;
import com.ruoyi.project.face.service.IPunchWindowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PunchWindowServiceImpl implements IPunchWindowService {

    private static final DateTimeFormatter DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern(DateUtils.YYYY_MM_DD_HH_MM_SS);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final CfPunchSettingsMapper cfPunchSettingsMapper;
    private final ICfStudentService studentService;
    private final CfCheckinMapper cfCheckinMapper;
    private final CfCheckoutMapper cfCheckoutMapper;

    @Override
    public void assertCheckinAllowed(Long studentId) {
        assertAllowed(studentId, PunchAction.CHECKIN);
    }

    @Override
    public void assertCheckoutAllowed(Long studentId) {
        assertAllowed(studentId, PunchAction.CHECKOUT);
    }

    @Override
    public boolean isCheckoutAllowed(Long studentId) {
        try {
            assertCheckoutAllowed(studentId);
            return true;
        } catch (ServiceException e) {
            return false;
        }
    }

    private void assertAllowed(Long studentId, PunchAction action) {
        CfStudent student = requireStudent(studentId, action);
        CfPunchSettings currentSlot = findCurrentSlot(student.getDeptId(), action);
        if (hasPunchedInSlot(studentId, currentSlot, action)) {
            throw new ServiceException(buildDuplicateMessage(currentSlot, action));
        }
    }

    private CfStudent requireStudent(Long studentId, PunchAction action) {
        if (studentId == null) {
            throw new ServiceException("学生ID不能为空");
        }
        CfStudent student = studentService.selectCfStudentById(studentId);
        if (student == null || StringUtils.isEmpty(student.getDeptId())) {
            throw new ServiceException(action.noDeptMessage);
        }
        return student;
    }

    private CfPunchSettings findCurrentSlot(String deptId, PunchAction action) {
        CfPunchSettings query = new CfPunchSettings();
        query.setDeptId(deptId);
        query.setStatus("1");
        List<CfPunchSettings> settings = cfPunchSettingsMapper.selectCfPunchSettingsList(query);
        if (settings == null || settings.isEmpty()) {
            throw new ServiceException(action.noSettingsMessage);
        }

        LocalTime now = LocalTime.now();
        return settings.stream()
                .filter(item -> item.getStartTime() != null && item.getEndTime() != null)
                .sorted(Comparator.comparing(CfPunchSettings::getStartTime))
                .filter(item -> isWithinWindow(now, item.getStartTime(), item.getEndTime()))
                .findFirst()
                .orElseThrow(() -> new ServiceException(buildOutsideWindowMessage(settings, action)));
    }

    private boolean isWithinWindow(LocalTime now, LocalTime start, LocalTime end) {
        return !now.isBefore(start) && !now.isAfter(end);
    }

    private boolean hasPunchedInSlot(Long studentId, CfPunchSettings slot, PunchAction action) {
        LocalDate today = LocalDate.now();
        LocalDateTime rangeStart = LocalDateTime.of(today, slot.getStartTime());
        LocalDateTime rangeEnd = LocalDateTime.of(today, slot.getEndTime());
        String start = DATETIME_FORMATTER.format(rangeStart);
        String end = DATETIME_FORMATTER.format(rangeEnd);
        int count = action == PunchAction.CHECKIN
                ? cfCheckinMapper.countByStudentAndTimeRange(studentId, start, end)
                : cfCheckoutMapper.countByStudentAndTimeRange(studentId, start, end);
        return count > 0;
    }

    private String buildDuplicateMessage(CfPunchSettings slot, PunchAction action) {
        String slotLabel = StringUtils.isNotEmpty(slot.getName()) ? slot.getName() : action.defaultSlotName;
        return String.format("「%s」%s-%s 内已%s，请勿重复打卡",
                slotLabel,
                formatTime(slot.getStartTime()),
                formatTime(slot.getEndTime()),
                action.actionLabel);
    }

    private String buildOutsideWindowMessage(List<CfPunchSettings> settings, PunchAction action) {
        String slots = settings.stream()
                .filter(item -> item.getStartTime() != null && item.getEndTime() != null)
                .sorted(Comparator.comparing(CfPunchSettings::getStartTime))
                .map(item -> {
                    String name = StringUtils.isNotEmpty(item.getName()) ? item.getName() : action.defaultSlotName;
                    return String.format("%s(%s-%s)", name, formatTime(item.getStartTime()), formatTime(item.getEndTime()));
                })
                .collect(Collectors.joining("、"));
        if (StringUtils.isEmpty(slots)) {
            return action.outsideWindowMessage;
        }
        return action.outsideWindowMessage + "，可打卡时段：" + slots;
    }

    private String formatTime(LocalTime time) {
        return time == null ? "--:--" : TIME_FORMATTER.format(time);
    }

    private enum PunchAction {
        CHECKIN("签到", "当前不在签到时间范围内", "未找到学生对应的校区，无法签到", "未配置签到时间段，请联系管理员", "签到时段"),
        CHECKOUT("签退", "当前不在签退时间范围内", "未找到学生对应的校区，无法签退", "未配置签退时间段，请联系管理员", "签退时段");

        private final String actionLabel;
        private final String outsideWindowMessage;
        private final String noDeptMessage;
        private final String noSettingsMessage;
        private final String defaultSlotName;

        PunchAction(String actionLabel, String outsideWindowMessage, String noDeptMessage,
                    String noSettingsMessage, String defaultSlotName) {
            this.actionLabel = actionLabel;
            this.outsideWindowMessage = outsideWindowMessage;
            this.noDeptMessage = noDeptMessage;
            this.noSettingsMessage = noSettingsMessage;
            this.defaultSlotName = defaultSlotName;
        }
    }
}
