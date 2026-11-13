package com.ruoyi.project.face.controller;

import com.ruoyi.framework.aspectj.lang.annotation.Log;
import com.ruoyi.framework.aspectj.lang.enums.BusinessType;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.framework.web.page.TableDataInfo;
import com.ruoyi.project.face.domain.CfAppVersion;
import com.ruoyi.project.face.service.ICfAppVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * App版本Controller
 */
@RestController
@RequestMapping("/face/appVersion")
@RequiredArgsConstructor
public class CfAppVersionController extends BaseController {
    private final ICfAppVersionService cfAppVersionService;

    @PreAuthorize("@ss.hasPermi('face:appVersion:list')")
    @GetMapping("/list")
    public TableDataInfo list(CfAppVersion cfAppVersion) {
        startPage();
        List<CfAppVersion> list = cfAppVersionService.selectCfAppVersionList(cfAppVersion);
        return getDataTable(list);
    }

    /**
     * 教师端检测更新（登录即可）
     */
    @GetMapping("/check")
    public AjaxResult check(@RequestParam(required = false, defaultValue = "android") String platform,
                            @RequestParam(required = false) Integer versionCode) {
        return success(cfAppVersionService.checkUpdate(platform, versionCode));
    }

    @PreAuthorize("@ss.hasPermi('face:appVersion:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(cfAppVersionService.selectCfAppVersionById(id));
    }

    @PreAuthorize("@ss.hasPermi('face:appVersion:add')")
    @Log(title = "App版本", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CfAppVersion cfAppVersion) {
        return toAjax(cfAppVersionService.insertCfAppVersion(cfAppVersion));
    }

    @PreAuthorize("@ss.hasPermi('face:appVersion:edit')")
    @Log(title = "App版本", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CfAppVersion cfAppVersion) {
        return toAjax(cfAppVersionService.updateCfAppVersion(cfAppVersion));
    }

    @PreAuthorize("@ss.hasPermi('face:appVersion:remove')")
    @Log(title = "App版本", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(cfAppVersionService.deleteCfAppVersionByIds(ids));
    }
}
