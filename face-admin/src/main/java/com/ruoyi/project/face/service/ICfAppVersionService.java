package com.ruoyi.project.face.service;

import com.ruoyi.project.face.domain.AppVersionCheckVo;
import com.ruoyi.project.face.domain.CfAppVersion;

import java.util.List;

/**
 * App版本Service接口
 */
public interface ICfAppVersionService {
    CfAppVersion selectCfAppVersionById(Long id);

    List<CfAppVersion> selectCfAppVersionList(CfAppVersion cfAppVersion);

    int insertCfAppVersion(CfAppVersion cfAppVersion);

    int updateCfAppVersion(CfAppVersion cfAppVersion);

    int deleteCfAppVersionByIds(Long[] ids);

    int deleteCfAppVersionById(Long id);

    AppVersionCheckVo checkUpdate(String platform, Integer versionCode);
}
