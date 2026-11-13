package com.ruoyi.project.face.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.utils.PasswordGenerator;
import com.ruoyi.common.utils.http.HttpUtils;
import com.ruoyi.framework.aspectj.lang.annotation.Anonymous;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.framework.aspectj.lang.annotation.Log;
import com.ruoyi.framework.aspectj.lang.enums.BusinessType;
import com.ruoyi.project.face.domain.CfUser;
import com.ruoyi.project.face.domain.ParentBindByCodeVo;
import com.ruoyi.project.face.domain.ParentBindPreviewVo;
import com.ruoyi.project.face.domain.ParentOpenIdLookupVo;
import com.ruoyi.project.face.domain.ParentQrBindVo;
import com.ruoyi.project.face.domain.ParentWechatBindVo;
import com.ruoyi.project.face.domain.ParentBindResultVo;
import com.ruoyi.project.face.service.ICfStudentParentService;
import com.ruoyi.project.face.service.ICfUserService;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.framework.web.page.TableDataInfo;

/**
 * 用户信息Controller
 *
 * @author fuyd
 * @date 2025-10-21
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/face/user")
public class CfUserController extends BaseController
{
    private final ICfUserService cfUserService;
    private final ICfStudentParentService cfStudentParentService;

    @Value("${wx.appid}")
    private String appid;
    @Value("${wx.secret}")
    private String secret;

    /**
     * 查询用户信息列表
     */
    @PreAuthorize("@ss.hasPermi('face:user:list')")
    @GetMapping("/list")
    public TableDataInfo list(CfUser cfUser)
    {
        startPage();
        List<CfUser> list = cfUserService.selectCfUserList(cfUser);
        list.forEach(this::decodePasswordForAdmin);
        return getDataTable(list);
    }

    /**
     * 导出用户信息列表
     */
    @PreAuthorize("@ss.hasPermi('face:user:export')")
    @Log(title = "用户信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CfUser cfUser)
    {
        List<CfUser> list = cfUserService.selectCfUserList(cfUser);
        // 密码解密
        list.forEach(t -> t.setPassword(PasswordGenerator.decode(t.getPassword())));

        ExcelUtil<CfUser> util = new ExcelUtil<CfUser>(CfUser.class);
        util.exportExcel(response, list, "用户信息数据");
    }

    /**
     * 微信 OAuth code 换取 openId（进入绑定页时立即调用，避免提交时 code 已过期）
     */
    @Anonymous
    @GetMapping("/wx-openid")
    public AjaxResult wxOpenId(@RequestParam String code)
    {
        String openId = resolveWxOpenId(code);
        if (StringUtils.isBlank(openId)) {
            return AjaxResult.error("\u754c\u9762\u64cd\u4f5c\u8d85\u65f6\uff0c\u8bf7\u5728\u670d\u52a1\u53f7\u91cd\u65b0\u8fdb\u5165\uff01");
        }
        return success(openId);
    }

    /**
     * 家长 H5 微信网页授权所需 appId
     */
    @Anonymous
    @GetMapping("/wx-config")
    public AjaxResult wxConfig()
    {
        return success(java.util.Collections.singletonMap("appId", appid));
    }

    /**
     * 获取用户信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('face:user:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        CfUser user = cfUserService.selectCfUserById(id);
        decodePasswordForAdmin(user);
        return success(user);
    }

    /**
     * 新增用户信息
     */
    @PreAuthorize("@ss.hasPermi('face:user:add')")
    @Log(title = "用户信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CfUser cfUser)
    {
        return toAjax(cfUserService.insertCfUser(cfUser));
    }

    /**
     * 修改用户信息
     */
    @PreAuthorize("@ss.hasPermi('face:user:edit')")
    @Log(title = "用户信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CfUser cfUser)
    {
        return toAjax(cfUserService.updateCfUser(cfUser));
    }

    /**
     * 删除用户信息
     */
    @PreAuthorize("@ss.hasPermi('face:user:remove')")
    @Log(title = "用户信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(cfUserService.deleteCfUserByIds(ids));
    }

    /**
     * 扫码绑定预览：按绑定码返回学生姓名
     */
    @Anonymous
    @GetMapping("/bind-preview")
    public AjaxResult bindPreview(@RequestParam String bindCode)
    {
        return success(cfStudentParentService.previewByBindCode(bindCode));
    }

    /**
     * 按 openId（或 OAuth code）查询是否已有家长账号
     */
    @Anonymous
    @GetMapping("/by-openid")
    public AjaxResult byOpenId(@RequestParam String openId)
    {
        String resolved = resolveWxOpenId(openId);
        if (StringUtils.isBlank(resolved)) {
            return AjaxResult.error("界面操作超时，请重新扫码进入！");
        }
        return success(cfStudentParentService.lookupByOpenId(resolved));
    }

    /**
     * 扫码确认绑定（回访家长免密；新家长需手机号）
     */
    @Anonymous
    @PutMapping("/bind-qr")
    public AjaxResult bindQr(@Validated @RequestBody ParentQrBindVo request)
    {
        String openid = resolveWxOpenId(request.getOpenId().trim());
        if (StringUtils.isBlank(openid)) {
            return AjaxResult.error("界面操作超时，请重新扫码进入！");
        }
        request.setOpenId(openid);
        return success(cfStudentParentService.bindByQr(request));
    }

    /**
     * 家长注册并绑定学生（绑定码一致即可关联）
     */
    @Anonymous
    @PostMapping("/register")
    public AjaxResult register(@Validated @RequestBody ParentBindByCodeVo request)
    {
        return success(cfStudentParentService.bindParentsByCode(request));
    }

    /**
     * 已注册家长追加绑定学生
     */
    @Anonymous
    @PostMapping("/bind-student")
    public AjaxResult bindStudent(@Validated @RequestBody ParentBindByCodeVo request)
    {
        return success(cfStudentParentService.bindParentsByCode(request));
    }

    /**
     * 微信服务号绑定：通过家长绑定码关联孩子并写入 openId
     */
    @Anonymous
    @PutMapping("/bind")
    public AjaxResult bind(@Validated @RequestBody ParentWechatBindVo request)
    {
        String openid = resolveWxOpenId(request.getOpenId().trim());
        if (StringUtils.isBlank(openid)) {
            return AjaxResult.error("\u754c\u9762\u64cd\u4f5c\u8d85\u65f6\uff0c\u8bf7\u5728\u670d\u52a1\u53f7\u91cd\u65b0\u8fdb\u5165\uff01");
        }

        CfUser user = cfUserService.selectCfUserByPhone(request.getPhone());
        ParentBindResultVo bindResult;
        ParentBindByCodeVo bindRequest = new ParentBindByCodeVo();
        bindRequest.setPhone(request.getPhone());
        bindRequest.setName(request.getName());
        bindRequest.setBindCode(request.getBindCode());

        if (user == null) {
            bindRequest.setPassword(StringUtils.isNotBlank(request.getPassword()) ? request.getPassword() : "init");
            bindResult = cfStudentParentService.bindParentsByCode(bindRequest);
            user = cfUserService.selectCfUserByPhone(request.getPhone());
        } else {
            if (StringUtils.isBlank(request.getPassword())) {
                return AjaxResult.error("该手机号已注册，请输入密码！");
            }
            bindRequest.setPassword(request.getPassword());
            String encoded = PasswordGenerator.encode(request.getPassword());
            if (!encoded.equals(user.getPassword())) {
                return AjaxResult.error("手机号或密码错误！");
            }
            bindResult = cfStudentParentService.bindParentsByCode(bindRequest);
        }

        if (user == null) {
            return AjaxResult.error("绑定失败，请重试！");
        }
        user.setOpenId(openid);
        cfUserService.updateCfUser(user);
        return success(bindResult);
    }

    private void decodePasswordForAdmin(CfUser user) {
        if (user == null || StringUtils.isBlank(user.getPassword())) {
            return;
        }
        try {
            user.setPassword(PasswordGenerator.decode(user.getPassword()));
        } catch (Exception ignored) {
            // keep stored value if decode fails
        }
    }

    private String resolveWxOpenId(String wxCodeOrOpenId) {
        if (StringUtils.isBlank(wxCodeOrOpenId)) {
            return null;
        }
        if (isWxOpenId(wxCodeOrOpenId)) {
            return wxCodeOrOpenId;
        }
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + appid
                + "&secret=" + secret + "&code=" + wxCodeOrOpenId + "&grant_type=authorization_code";
        String response = HttpUtils.sendGet(url);
        JSONObject object = JSONObject.parseObject(response);
        if (object == null || !object.containsKey("openid")) {
            return null;
        }
        return object.getString("openid");
    }

    private boolean isWxOpenId(String value) {
        return value.length() >= 20 && value.startsWith("o");
    }

}
