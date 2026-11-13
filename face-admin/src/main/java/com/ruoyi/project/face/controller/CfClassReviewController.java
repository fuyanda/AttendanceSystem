package com.ruoyi.project.face.controller;

import com.ruoyi.framework.aspectj.lang.annotation.Log;
import com.ruoyi.framework.aspectj.lang.enums.BusinessType;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.framework.web.page.TableDataInfo;
import com.ruoyi.project.face.domain.CfClassReviewSession;
import com.ruoyi.project.face.domain.ClassReviewCreateRequest;
import com.ruoyi.project.face.domain.ClassReviewSaveRequest;
import com.ruoyi.project.face.service.ICfClassReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/face/review")
public class CfClassReviewController extends BaseController {

    private final ICfClassReviewService cfClassReviewService;

    @PreAuthorize("@ss.hasPermi('face:review:list')")
    @GetMapping("/list")
    public TableDataInfo list(CfClassReviewSession query) {
        startPage();
        return getDataTable(cfClassReviewService.selectSessionList(query));
    }

    @PreAuthorize("@ss.hasPermi('face:review:list') or @ss.hasPermi('face:review:edit') or @ss.hasPermi('face:review:query')")
    @GetMapping("/subjects")
    public AjaxResult subjects() {
        return success(cfClassReviewService.listSubjects());
    }

    @PreAuthorize("@ss.hasPermi('face:review:list') or @ss.hasPermi('face:review:edit') or @ss.hasPermi('face:review:query')")
    @GetMapping("/student/{studentId}/today")
    public AjaxResult todayByStudent(@PathVariable Long studentId) {
        return success(cfClassReviewService.selectTodayByStudentId(studentId));
    }

    @PreAuthorize("@ss.hasPermi('face:review:edit')")
    @Log(title = "ClassReviewCreate", businessType = BusinessType.INSERT)
    @PostMapping("/create")
    public AjaxResult createStandalone(@RequestBody ClassReviewCreateRequest request) {
        return success(cfClassReviewService.createStandalone(request));
    }

    @PreAuthorize("@ss.hasPermi('face:review:list') or @ss.hasPermi('face:review:edit') or @ss.hasPermi('face:review:query')")
    @GetMapping("/{sessionId}")
    public AjaxResult getDetail(@PathVariable Long sessionId) {
        return success(cfClassReviewService.selectSessionDetail(sessionId));
    }

    @PreAuthorize("@ss.hasPermi('face:review:edit')")
    @Log(title = "ClassReview", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult saveDraft(@RequestBody ClassReviewSaveRequest request) {
        cfClassReviewService.saveDraft(request);
        return success();
    }

    @PreAuthorize("@ss.hasPermi('face:review:send')")
    @Log(title = "ClassReviewSend", businessType = BusinessType.OTHER)
    @PostMapping("/send/{sessionId}")
    public AjaxResult send(@PathVariable Long sessionId) {
        cfClassReviewService.sendSession(sessionId);
        return success();
    }
}
