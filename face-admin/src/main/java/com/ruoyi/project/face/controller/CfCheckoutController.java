package com.ruoyi.project.face.controller;

import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.framework.aspectj.lang.annotation.Log;
import com.ruoyi.framework.aspectj.lang.enums.BusinessType;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.framework.web.page.TableDataInfo;
import com.ruoyi.project.face.domain.CfCheckout;
import com.ruoyi.project.face.domain.DeptCountVo;
import com.ruoyi.project.face.domain.ManualCheckoutRequest;
import com.ruoyi.project.face.service.ICfCheckoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 签退Controller
 *
 * @author fuyd
 * @date 2025-08-16
 */
@RestController
@RequestMapping("/face/checkout")
public class CfCheckoutController extends BaseController {
    @Autowired
    private ICfCheckoutService cfCheckoutService;

    /**
     * 查询签退列表
     */
    @PreAuthorize("@ss.hasPermi('face:checkout:list')")
    @GetMapping("/list")
    public TableDataInfo list(CfCheckout cfCheckout) {
        startPage();
        List<CfCheckout> list = cfCheckoutService.selectCfCheckoutList(cfCheckout);
        return getDataTable(list);
    }

    /**
     * 导出签退列表
     */
    @PreAuthorize("@ss.hasPermi('face:checkout:export')")
    @Log(title = "签退", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CfCheckout cfCheckout) {
        List<CfCheckout> list = cfCheckoutService.selectCfCheckoutList(cfCheckout);
        ExcelUtil<CfCheckout> util = new ExcelUtil<CfCheckout>(CfCheckout.class);
        util.exportExcel(response, list, "签退数据");
    }

    /**
     * 获取签退详细信息
     */
    @PreAuthorize("@ss.hasPermi('face:checkout:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(cfCheckoutService.selectCfCheckoutById(id));
    }

    /**
     * 新增签退
     */
    @PreAuthorize("@ss.hasPermi('face:checkout:add')")
    @Log(title = "签退", businessType = BusinessType.INSERT, excludeParamNames = {"imageBase64"})
    @PostMapping
    public AjaxResult add(@RequestBody CfCheckout cfCheckout) {
        return success(cfCheckoutService.insertCfCheckout(cfCheckout));
    }


    /**
     * 重新签退
     */
    @PreAuthorize("@ss.hasPermi('face:checkout:add')")
    @Log(title = "签退", businessType = BusinessType.UPDATE)
    @GetMapping("rePush/{id}")
    public AjaxResult rePush(@PathVariable("id") Long id) {
        return toAjax(cfCheckoutService.rePush(id));
    }

    /**
     * 手动签退候选列表（今日已签到且当前时段可签退）
     */
    @PreAuthorize("@ss.hasPermi('face:checkout:add')")
    @GetMapping("/manual/candidates")
    public AjaxResult manualCandidates(@RequestParam(required = false) String studentName,
                                       @RequestParam(required = false) String deptId) {
        return success(cfCheckoutService.listManualCheckoutCandidates(studentName, deptId));
    }

    /**
     * 手动签退
     */
    @PreAuthorize("@ss.hasPermi('face:checkout:add')")
    @Log(title = "手动签退", businessType = BusinessType.INSERT)
    @PostMapping("/manual")
    public AjaxResult manualCheckout(@Validated @RequestBody ManualCheckoutRequest request) {
        return success(cfCheckoutService.manualCheckout(request.getStudentId()));
    }

    @PreAuthorize("@ss.hasPermi('face:checkout:list')")
    @GetMapping("/stats/today/dept")
    public AjaxResult statsTodayByDept() {
        return success(cfCheckoutService.countTodayByDept());
    }
}
