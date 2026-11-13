package com.ruoyi.project.face.service.impl;

import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.config.RuoYiConfig;
import com.ruoyi.project.face.domain.AppVersionCheckVo;
import com.ruoyi.project.face.domain.CfAppVersion;
import com.ruoyi.project.face.mapper.CfAppVersionMapper;
import com.ruoyi.project.face.service.ICfAppVersionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

/**
 * App版本Service业务层处理
 */
@Service
@RequiredArgsConstructor
public class CfAppVersionServiceImpl implements ICfAppVersionService {
    private static final Logger log = LoggerFactory.getLogger(CfAppVersionServiceImpl.class);

    private final CfAppVersionMapper cfAppVersionMapper;

    @Override
    public CfAppVersion selectCfAppVersionById(Long id) {
        return cfAppVersionMapper.selectCfAppVersionById(id);
    }

    @Override
    public List<CfAppVersion> selectCfAppVersionList(CfAppVersion cfAppVersion) {
        return cfAppVersionMapper.selectCfAppVersionList(cfAppVersion);
    }

    @Override
    public int insertCfAppVersion(CfAppVersion cfAppVersion) {
        validate(cfAppVersion);
        if (StringUtils.isEmpty(cfAppVersion.getPlatform())) {
            cfAppVersion.setPlatform("android");
        }
        if (StringUtils.isEmpty(cfAppVersion.getForceUpdate())) {
            cfAppVersion.setForceUpdate("0");
        }
        if (StringUtils.isEmpty(cfAppVersion.getStatus())) {
            cfAppVersion.setStatus("1");
        }
        relocateApkIfNeeded(cfAppVersion);
        cfAppVersion.setCreateBy(SecurityUtils.getUsername());
        cfAppVersion.setCreateTime(DateUtils.getNowDate());
        return cfAppVersionMapper.insertCfAppVersion(cfAppVersion);
    }

    @Override
    public int updateCfAppVersion(CfAppVersion cfAppVersion) {
        validate(cfAppVersion);
        relocateApkIfNeeded(cfAppVersion);
        cfAppVersion.setUpdateBy(SecurityUtils.getUsername());
        cfAppVersion.setUpdateTime(DateUtils.getNowDate());
        return cfAppVersionMapper.updateCfAppVersion(cfAppVersion);
    }

    @Override
    public int deleteCfAppVersionByIds(Long[] ids) {
        return cfAppVersionMapper.deleteCfAppVersionByIds(ids);
    }

    @Override
    public int deleteCfAppVersionById(Long id) {
        return cfAppVersionMapper.deleteCfAppVersionById(id);
    }

    @Override
    public AppVersionCheckVo checkUpdate(String platform, Integer versionCode) {
        String plat = StringUtils.isEmpty(platform) ? "android" : platform;
        int localCode = versionCode == null ? 0 : versionCode;
        CfAppVersion latest = cfAppVersionMapper.selectLatestEnabledByPlatform(plat);
        if (latest == null || latest.getVersionCode() == null || latest.getVersionCode() <= localCode) {
            return AppVersionCheckVo.noUpdate();
        }
        AppVersionCheckVo vo = new AppVersionCheckVo();
        vo.setNeedUpdate(true);
        vo.setForceUpdate("1".equals(latest.getForceUpdate()));
        vo.setVersionName(latest.getVersionName());
        vo.setVersionCode(latest.getVersionCode());
        vo.setApkUrl(latest.getApkUrl());
        vo.setChangelog(latest.getChangelog());
        return vo;
    }

    private void validate(CfAppVersion version) {
        if (version == null) {
            throw new ServiceException("版本信息不能为空");
        }
        if (StringUtils.isEmpty(version.getVersionName())) {
            throw new ServiceException("版本名不能为空");
        }
        if (version.getVersionCode() == null || version.getVersionCode() <= 0) {
            throw new ServiceException("版本号必须为正整数");
        }
        if (StringUtils.isEmpty(version.getApkUrl())) {
            throw new ServiceException("APK地址不能为空");
        }
    }

    /**
     * 历史版本若仍落在 upload 目录，保存时迁到 apk 目录，避免清理签到文件时误删。
     */
    private void relocateApkIfNeeded(CfAppVersion version) {
        String apkUrl = version.getApkUrl();
        if (StringUtils.isEmpty(apkUrl)) {
            return;
        }
        String resourcePath = extractResourcePath(apkUrl);
        if (StringUtils.isEmpty(resourcePath)) {
            return;
        }
        if (resourcePath.startsWith("/apk/") || resourcePath.contains("/apk/")) {
            version.setApkUrl(Constants.RESOURCE_PREFIX + resourcePath);
            return;
        }
        if (!(resourcePath.startsWith("/upload/") || resourcePath.contains("/upload/"))) {
            return;
        }

        File source = new File(RuoYiConfig.getProfile() + resourcePath);
        if (!source.isFile()) {
            log.warn("apk source missing, keep url as-is: {}", apkUrl);
            return;
        }

        String relative = DateUtils.datePath() + "/" + source.getName();
        File target = new File(RuoYiConfig.getApkPath(), relative);
        if (!target.getParentFile().exists() && !target.getParentFile().mkdirs()) {
            throw new ServiceException("创建 APK 目录失败");
        }
        try {
            if (target.exists()) {
                Files.delete(target.toPath());
            }
            Files.move(source.toPath(), target.toPath());
        } catch (Exception ex) {
            throw new ServiceException("迁移 APK 文件失败: " + ex.getMessage());
        }
        String newUrl = Constants.RESOURCE_PREFIX + "/apk/" + relative;
        version.setApkUrl(newUrl);
        log.info("relocated apk {} -> {}", apkUrl, newUrl);
    }

    private static String extractResourcePath(String apkUrl) {
        String value = apkUrl.trim();
        int idx = value.indexOf(Constants.RESOURCE_PREFIX);
        if (idx >= 0) {
            return value.substring(idx + Constants.RESOURCE_PREFIX.length()).replace('\\', '/');
        }
        if (value.startsWith("/upload/") || value.startsWith("/apk/")) {
            return value;
        }
        return null;
    }
}
