package com.ruoyi.project.face.controller;

import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.framework.aspectj.lang.annotation.Anonymous;
import com.ruoyi.framework.aspectj.lang.annotation.Log;
import com.ruoyi.framework.aspectj.lang.enums.BusinessType;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.framework.web.page.TableDataInfo;
import com.ruoyi.project.face.domain.CfCheckin;
import com.ruoyi.project.face.domain.ManualCheckinRequest;
import com.ruoyi.project.face.domain.DeptCountVo;
import com.ruoyi.project.face.service.ICfCheckinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 签到Controller
 *
 * @author fuyd
 * @date 2025-08-16
 */
@RestController
@RequestMapping("/face/checkin")
public class CfCheckinController extends BaseController {
    @Autowired
    private ICfCheckinService cfCheckinService;

    /**
     * 查询签到列表
     */
    @PreAuthorize("@ss.hasPermi('face:checkin:list')")
    @GetMapping("/list")
    public TableDataInfo list(CfCheckin cfCheckin) {
        startPage();
        List<CfCheckin> list = cfCheckinService.selectCfCheckinList(cfCheckin);
        return getDataTable(list);
    }

    /**
     * 导出签到列表
     */
    @PreAuthorize("@ss.hasPermi('face:checkin:export')")
    @Log(title = "签到", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CfCheckin cfCheckin) {
        List<CfCheckin> list = cfCheckinService.selectCfCheckinList(cfCheckin);
        ExcelUtil<CfCheckin> util = new ExcelUtil<CfCheckin>(CfCheckin.class);
        util.exportExcel(response, list, "签到数据");
    }

    /**
     * 获取签到详细信息
     */
    @PreAuthorize("@ss.hasPermi('face:checkin:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(cfCheckinService.selectCfCheckinById(id));
    }

    /**
     * 新增签到
     */
    @PreAuthorize("@ss.hasPermi('face:checkin:add')")
    @Log(title = "签到", businessType = BusinessType.INSERT, excludeParamNames = {"imageBase64"})
    @PostMapping
    public AjaxResult add(@RequestBody CfCheckin cfCheckin) {
        return success(cfCheckinService.insertCfCheckin(cfCheckin));
    }


    /**
     * 重新签到
     */
    @PreAuthorize("@ss.hasPermi('face:checkin:add')")
    @Log(title = "重新签到", businessType = BusinessType.UPDATE)
    @GetMapping("rePush/{id}")
    public AjaxResult rePush(@PathVariable("id") Long id) {
        return toAjax(cfCheckinService.rePush(id));
    }

    /**
     * 查询签到
     * @param id
     * @return
     */
    @Anonymous
    @GetMapping("/getCheckInfo/{id}")
    public AjaxResult getCheckInfo(@PathVariable("id") Long id)
    {
        return success(cfCheckinService.getCheckInfo(id));
    }


    /**
     * 手动签到候选列表（今日尚未签到）
     */
    @PreAuthorize("@ss.hasPermi('face:checkin:add')")
    @GetMapping("/manual/candidates")
    public AjaxResult manualCandidates(@RequestParam(required = false) String studentName,
                                       @RequestParam(required = false) String deptId) {
        return success(cfCheckinService.listManualCheckinCandidates(studentName, deptId));
    }

    /**
     * 手动签到
     */
    @PreAuthorize("@ss.hasPermi('face:checkin:add')")
    @Log(title = "手动签到", businessType = BusinessType.INSERT)
    @PostMapping("/manual")
    public AjaxResult manualCheckin(@Validated @RequestBody ManualCheckinRequest request) {
        return success(cfCheckinService.manualCheckin(request.getStudentId()));
    }

    @PreAuthorize("@ss.hasPermi('face:checkin:list')")
    @GetMapping("/stats/today/dept")

    public AjaxResult statsTodayByDept() {
        return success(cfCheckinService.countTodayByDept());
    }
}
