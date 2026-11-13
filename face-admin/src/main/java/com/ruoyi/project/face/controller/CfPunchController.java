package com.ruoyi.project.face.controller;

import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.project.face.service.IPunchWindowService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 打卡时段校验
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/face/punch")
public class CfPunchController extends BaseController {

    private final IPunchWindowService punchWindowService;

    /**
     * 校验当前是否允许打卡（同一时间段内仅允许一次）
     *
     * @param studentId 学生ID
     * @param mode      checkin / checkout
     */
    @PreAuthorize("@ss.hasPermi('face:checkin:add') or @ss.hasPermi('face:checkout:add')")
    @GetMapping("/validate")
    public AjaxResult validate(@RequestParam Long studentId, @RequestParam(defaultValue = "checkin") String mode) {
        if ("checkout".equalsIgnoreCase(mode)) {
            punchWindowService.assertCheckoutAllowed(studentId);
        } else {
            punchWindowService.assertCheckinAllowed(studentId);
        }
        return success();
    }
}
