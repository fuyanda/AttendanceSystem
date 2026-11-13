package com.ruoyi.project.face.controller;

import com.ruoyi.framework.aspectj.lang.annotation.Log;
import com.ruoyi.framework.aspectj.lang.enums.BusinessType;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.framework.web.page.TableDataInfo;
import com.ruoyi.project.face.domain.CfStudentFee;
import com.ruoyi.project.face.domain.FeeBatchConsumeRequest;
import com.ruoyi.project.face.domain.FeeConsumeRequest;
import com.ruoyi.project.face.domain.FeeLedgerQuery;
import com.ruoyi.project.face.domain.FeeRefundRequest;
import com.ruoyi.project.face.service.ICfStudentFeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/face/fee")
public class CfStudentFeeController extends BaseController {

    private final ICfStudentFeeService cfStudentFeeService;

    @PreAuthorize("@ss.hasPermi('face:fee:list')")
    @GetMapping("/list")
    public TableDataInfo list(CfStudentFee query) {
        startPage();
        List<CfStudentFee> list = cfStudentFeeService.selectCfStudentFeeList(query);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('face:fee:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(cfStudentFeeService.selectCfStudentFeeById(id));
    }

    @PreAuthorize("@ss.hasPermi('face:fee:query')")
    @GetMapping("/student/{studentId}")
    public AjaxResult listByStudent(@PathVariable Long studentId) {
        return success(cfStudentFeeService.selectByStudentId(studentId));
    }

    @PreAuthorize("@ss.hasPermi('face:checkin:add') or @ss.hasPermi('face:fee:query')")
    @GetMapping("/options")
    public AjaxResult options(@RequestParam Long studentId) {
        return success(cfStudentFeeService.selectOptions(studentId));
    }

    @PreAuthorize("@ss.hasPermi('face:fee:query') or @ss.hasPermi('face:fee:consume')")
    @GetMapping("/teachers")
    public AjaxResult teachers(@RequestParam(required = false) Long deptId) {
        return success(cfStudentFeeService.selectTeachers(deptId));
    }

    @PreAuthorize("@ss.hasPermi('face:fee:query') or @ss.hasPermi('face:fee:consume')")
    @GetMapping("/teachers/select")
    public AjaxResult teacherSelect(@RequestParam(required = false) Long deptId) {
        return success(cfStudentFeeService.getTeacherSelectContext(deptId));
    }

    @PreAuthorize("@ss.hasPermi('face:fee:add')")
    @Log(title = "学生缴费", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CfStudentFee fee) {
        return toAjax(cfStudentFeeService.insertCfStudentFee(fee));
    }

    @PreAuthorize("@ss.hasPermi('face:fee:edit')")
    @Log(title = "学生缴费", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CfStudentFee fee) {
        return toAjax(cfStudentFeeService.updateCfStudentFee(fee));
    }

    @PreAuthorize("@ss.hasPermi('face:fee:remove')")
    @Log(title = "学生缴费", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(cfStudentFeeService.deleteCfStudentFeeByIds(ids));
    }

    @PreAuthorize("@ss.hasPermi('face:fee:consume')")
    @Log(title = "手动消课", businessType = BusinessType.UPDATE)
    @PostMapping("/consume")
    public AjaxResult consume(@RequestBody FeeConsumeRequest request) {
        cfStudentFeeService.consumeManual(request);
        return success();
    }

    @PreAuthorize("@ss.hasPermi('face:fee:consume')")
    @GetMapping("/consume/context")
    public AjaxResult consumeContext() {
        return success(cfStudentFeeService.getConsumeContext());
    }

    @PreAuthorize("@ss.hasPermi('face:fee:consume')")
    @GetMapping("/consume/subjects")
    public AjaxResult consumeSubjects(@RequestParam Long teacherUserId) {
        return success(cfStudentFeeService.selectSubjectsByTeacher(teacherUserId));
    }

    @PreAuthorize("@ss.hasPermi('face:fee:consume')")
    @GetMapping("/consume/students")
    public AjaxResult consumeStudents(@RequestParam Long teacherUserId, @RequestParam String subject) {
        return success(cfStudentFeeService.selectConsumableStudents(teacherUserId, subject));
    }

    @PreAuthorize("@ss.hasPermi('face:fee:consume')")
    @Log(title = "批量消课", businessType = BusinessType.UPDATE)
    @PostMapping("/consume/batch")
    public AjaxResult batchConsume(@RequestBody FeeBatchConsumeRequest request) {
        return success(cfStudentFeeService.batchConsume(request));
    }

    @PreAuthorize("@ss.hasPermi('face:fee:refund')")
    @Log(title = "中道退费", businessType = BusinessType.UPDATE)
    @PostMapping("/refund")
    public AjaxResult refund(@RequestBody FeeRefundRequest request) {
        cfStudentFeeService.refund(request);
        return success();
    }

    @PreAuthorize("@ss.hasPermi('face:fee:ledger')")
    @GetMapping("/ledger/list")
    public TableDataInfo ledgerList(FeeLedgerQuery query) {
        startPage();
        return getDataTable(cfStudentFeeService.selectLedgerList(query));
    }

    @PreAuthorize("@ss.hasPermi('face:fee:ledger')")
    @GetMapping("/ledger/summary")
    public AjaxResult ledgerSummary(FeeLedgerQuery query) {
        return success(cfStudentFeeService.selectLedgerSummary(query));
    }
}
