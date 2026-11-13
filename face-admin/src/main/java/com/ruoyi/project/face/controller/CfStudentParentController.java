package com.ruoyi.project.face.controller;

import com.ruoyi.framework.aspectj.lang.annotation.Log;
import com.ruoyi.framework.aspectj.lang.enums.BusinessType;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.project.face.domain.CfStudentParent;
import com.ruoyi.project.face.domain.StudentParentBindVo;
import com.ruoyi.project.face.service.ICfStudentParentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/face/student-parent")
public class CfStudentParentController extends BaseController {

    private final ICfStudentParentService cfStudentParentService;

    @PreAuthorize("@ss.hasPermi('face:student:query')")
    @GetMapping("/student/{studentId}")
    public AjaxResult listByStudent(@PathVariable Long studentId) {
        return success(cfStudentParentService.selectByStudentId(studentId));
    }

    @PreAuthorize("@ss.hasPermi('face:user:query')")
    @GetMapping("/user/{userId}")
    public AjaxResult listByUser(@PathVariable Long userId) {
        return success(cfStudentParentService.selectByUserId(userId));
    }

    @PreAuthorize("@ss.hasPermi('face:student:edit')")
    @Log(title = "绑定家长", businessType = BusinessType.INSERT)
    @PostMapping("/student/{studentId}")
    public AjaxResult bind(@PathVariable Long studentId, @Validated @RequestBody StudentParentBindVo bindVo) {
        return toAjax(cfStudentParentService.bindParent(studentId, bindVo));
    }

    @PreAuthorize("@ss.hasPermi('face:student:edit')")
    @Log(title = "解绑家长", businessType = BusinessType.DELETE)
    @DeleteMapping("/student/{studentId}/user/{userId}")
    public AjaxResult unbind(@PathVariable Long studentId, @PathVariable Long userId) {
        return toAjax(cfStudentParentService.unbindParent(studentId, userId));
    }

    @PreAuthorize("@ss.hasPermi('face:student:edit')")
    @Log(title = "设置主联系人", businessType = BusinessType.UPDATE)
    @PutMapping("/student/{studentId}/user/{userId}/primary")
    public AjaxResult setPrimary(@PathVariable Long studentId, @PathVariable Long userId) {
        return toAjax(cfStudentParentService.setPrimaryParent(studentId, userId));
    }

    @PreAuthorize("@ss.hasPermi('face:student:edit')")
    @Log(title = "更新家长关系", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult update(@RequestBody CfStudentParent relation) {
        return toAjax(cfStudentParentService.updateRelation(relation));
    }

    @PreAuthorize("@ss.hasPermi('face:student:edit')")
    @Log(title = "批量绑定家长", businessType = BusinessType.UPDATE)
    @PutMapping("/student/{studentId}")
    public AjaxResult replace(@PathVariable Long studentId, @RequestBody List<StudentParentBindVo> parents) {
        return success(cfStudentParentService.replaceStudentParents(studentId, parents));
    }
}
