package com.ruoyi.project.face.controller;

import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.framework.aspectj.lang.annotation.Log;
import com.ruoyi.framework.aspectj.lang.enums.BusinessType;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.framework.web.page.TableDataInfo;
import com.ruoyi.project.face.domain.CfPunchSettings;
import com.ruoyi.project.face.service.ICfPunchSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 打卡设置Controller
 *
 * @author 付延达
 * @date 2024-08-24
 */
@RestController
@RequestMapping("/face/punch")
public class CfPunchSettingsController extends BaseController {
    @Autowired
    private ICfPunchSettingsService cfPunchSettingsService;

    /**
     * 查询打卡设置列表
     */
    @PreAuthorize("@ss.hasPermi('face:punch:list')")
    @GetMapping("/list")
    public TableDataInfo list(CfPunchSettings cfPunchSettings) {
        startPage();
        List<CfPunchSettings> list = cfPunchSettingsService.selectCfPunchSettingsList(cfPunchSettings);
        return getDataTable(list);
    }

    /**
     * 导出打卡设置列表
     */
    @PreAuthorize("@ss.hasPermi('face:punch:export')")
    @Log(title = "打卡设置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CfPunchSettings cfPunchSettings) {
        List<CfPunchSettings> list = cfPunchSettingsService.selectCfPunchSettingsList(cfPunchSettings);
        ExcelUtil<CfPunchSettings> util = new ExcelUtil<CfPunchSettings>(CfPunchSettings.class);
        util.exportExcel(response, list, "打卡设置数据");
    }

    /**
     * 获取打卡设置详细信息
     */
    @PreAuthorize("@ss.hasPermi('face:punch:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        return success(cfPunchSettingsService.selectCfPunchSettingsById(id));
    }

    /**
     * 新增打卡设置
     */
    @PreAuthorize("@ss.hasPermi('face:punch:add')")
    @Log(title = "打卡设置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CfPunchSettings cfPunchSettings) {
        return toAjax(cfPunchSettingsService.insertCfPunchSettings(cfPunchSettings));
    }

    /**
     * 修改打卡设置
     */
    @PreAuthorize("@ss.hasPermi('face:punch:edit')")
    @Log(title = "打卡设置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CfPunchSettings cfPunchSettings) {
        return toAjax(cfPunchSettingsService.updateCfPunchSettings(cfPunchSettings));
    }

    /**
     * 删除打卡设置
     */
    @PreAuthorize("@ss.hasPermi('face:punch:remove')")
    @Log(title = "打卡设置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) {
        return toAjax(cfPunchSettingsService.deleteCfPunchSettingsByIds(ids));
    }
}
