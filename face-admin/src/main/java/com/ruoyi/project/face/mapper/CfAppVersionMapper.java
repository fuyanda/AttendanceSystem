package com.ruoyi.project.face.mapper;

import com.ruoyi.project.face.domain.CfAppVersion;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * App版本Mapper接口
 */
public interface CfAppVersionMapper {
    CfAppVersion selectCfAppVersionById(Long id);

    List<CfAppVersion> selectCfAppVersionList(CfAppVersion cfAppVersion);

    CfAppVersion selectLatestEnabledByPlatform(@Param("platform") String platform);

    int insertCfAppVersion(CfAppVersion cfAppVersion);

    int updateCfAppVersion(CfAppVersion cfAppVersion);

    int deleteCfAppVersionById(Long id);

    int deleteCfAppVersionByIds(Long[] ids);
}
