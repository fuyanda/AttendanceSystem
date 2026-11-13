package com.ruoyi.project.face.service.impl;

import com.ruoyi.common.utils.uuid.UUID;
import com.ruoyi.project.face.domain.CfPunchSettings;
import com.ruoyi.project.face.mapper.CfPunchSettingsMapper;
import com.ruoyi.project.face.service.ICfPunchSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 打卡设置Service业务层处理
 *
 * @author 付延达
 * @date 2024-08-24
 */
@Service
public class CfPunchSettingsServiceImpl implements ICfPunchSettingsService {
    @Autowired
    private CfPunchSettingsMapper cfPunchSettingsMapper;

    /**
     * 查询打卡设置
     *
     * @param id 打卡设置主键
     * @return 打卡设置
     */
    @Override
    public CfPunchSettings selectCfPunchSettingsById(String id) {
        return cfPunchSettingsMapper.selectCfPunchSettingsById(id);
    }

    /**
     * 查询打卡设置列表
     *
     * @param cfPunchSettings 打卡设置
     * @return 打卡设置
     */
    @Override
    public List<CfPunchSettings> selectCfPunchSettingsList(CfPunchSettings cfPunchSettings) {
        return cfPunchSettingsMapper.selectCfPunchSettingsList(cfPunchSettings);
    }

    /**
     * 新增打卡设置
     *
     * @param cfPunchSettings 打卡设置
     * @return 结果
     */
    @Override
    public int insertCfPunchSettings(CfPunchSettings cfPunchSettings) {
        cfPunchSettings.setId(UUID.fastUUID().toString());
        return cfPunchSettingsMapper.insertCfPunchSettings(cfPunchSettings);
    }

    /**
     * 修改打卡设置
     *
     * @param cfPunchSettings 打卡设置
     * @return 结果
     */
    @Override
    public int updateCfPunchSettings(CfPunchSettings cfPunchSettings) {
        return cfPunchSettingsMapper.updateCfPunchSettings(cfPunchSettings);
    }

    /**
     * 批量删除打卡设置
     *
     * @param ids 需要删除的打卡设置主键
     * @return 结果
     */
    @Override
    public int deleteCfPunchSettingsByIds(String[] ids) {
        return cfPunchSettingsMapper.deleteCfPunchSettingsByIds(ids);
    }

    /**
     * 删除打卡设置信息
     *
     * @param id 打卡设置主键
     * @return 结果
     */
    @Override
    public int deleteCfPunchSettingsById(String id) {
        return cfPunchSettingsMapper.deleteCfPunchSettingsById(id);
    }
}
