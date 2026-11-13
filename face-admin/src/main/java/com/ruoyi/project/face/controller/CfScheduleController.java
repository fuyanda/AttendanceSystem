package com.ruoyi.project.face.controller;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.framework.aspectj.lang.annotation.Log;
import com.ruoyi.framework.aspectj.lang.enums.BusinessType;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.project.face.domain.ScheduleBatchSaveRequest;
import com.ruoyi.project.face.domain.ScheduleSaveRequest;
import com.ruoyi.project.face.service.ICfScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Calendar;
import java.util.Date;

@RestController
@RequiredArgsConstructor
@RequestMapping("/face/schedule")
public class CfScheduleController extends BaseController {

    private final ICfScheduleService cfScheduleService;

    @PreAuthorize("@ss.hasPermi('face:schedule:list')")
    @GetMapping("/teachers")
    public AjaxResult teachers() {
        return success(cfScheduleService.selectTeachers());
    }

    @PreAuthorize("@ss.hasPermi('face:schedule:list')")
    @GetMapping("/students")
    public AjaxResult scheduleStudents(@RequestParam(required = false) String name) {
        return success(cfScheduleService.selectScheduleStudents(name));
    }

    @PreAuthorize("@ss.hasPermi('face:schedule:list')")
    @GetMapping("/teacher/calendar")
    public AjaxResult teacherCalendar(@RequestParam(required = false) Long teacherUserId,
                                      @RequestParam String beginDate,
                                      @RequestParam String endDate) {
        Date begin = parseDayStart(beginDate);
        Date end = parseDayEnd(endDate);
        return success(cfScheduleService.selectTeacherCalendar(teacherUserId, begin, end));
    }

    @PreAuthorize("@ss.hasPermi('face:schedule:list')")
    @GetMapping("/student/calendar")
    public AjaxResult studentCalendar(@RequestParam Long studentId,
                                      @RequestParam String beginDate,
                                      @RequestParam String endDate) {
        Date begin = parseDayStart(beginDate);
        Date end = parseDayEnd(endDate);
        return success(cfScheduleService.selectStudentCalendar(studentId, begin, end));
    }

    @PreAuthorize("@ss.hasAnyPermi('face:schedule:query,face:schedule:list,face:schedule:edit')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(cfScheduleService.selectSessionDetail(id));
    }

    @PreAuthorize("@ss.hasPermi('face:schedule:add')")
    @Log(title = "排课", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ScheduleSaveRequest request) {
        return success(cfScheduleService.createSession(request));
    }

    @PreAuthorize("@ss.hasPermi('face:schedule:add')")
    @Log(title = "批量排课", businessType = BusinessType.INSERT)
    @PostMapping("/batch")
    public AjaxResult addBatch(@RequestBody ScheduleBatchSaveRequest request) {
        return success(cfScheduleService.createSessionsBatch(request));
    }

    @PreAuthorize("@ss.hasPermi('face:schedule:edit')")
    @Log(title = "排课", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ScheduleSaveRequest request) {
        cfScheduleService.updateSession(request);
        return success();
    }

    @PreAuthorize("@ss.hasPermi('face:schedule:remove')")
    @Log(title = "排课", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id) {
        cfScheduleService.deleteSession(id);
        return success();
    }

    private Date parseDayStart(String day) {
        Date d = DateUtils.parseDate(day);
        if (d == null) {
            throw new ServiceException("日期格式错误");
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Date parseDayEnd(String day) {
        Date d = DateUtils.parseDate(day);
        if (d == null) {
            throw new ServiceException("日期格式错误");
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }
}
