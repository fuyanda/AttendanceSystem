package com.ruoyi.project.face.service;

import com.ruoyi.project.face.domain.CfPunchSettings;

import java.util.List;

/**
 * 打卡设置Service接口
 *
 * @author 付延达
 * @date 2024-08-24
 */
public interface ICfPunchSettingsService {
    /**
     * 查询打卡设置
     *
     * @param id 打卡设置主键
     * @return 打卡设置
     */
    public CfPunchSettings selectCfPunchSettingsById(String id);

    /**
     * 查询打卡设置列表
     *
     * @param cfPunchSettings 打卡设置
     * @return 打卡设置集合
     */
    public List<CfPunchSettings> selectCfPunchSettingsList(CfPunchSettings cfPunchSettings);

    /**
     * 新增打卡设置
     *
     * @param cfPunchSettings 打卡设置
     * @return 结果
     */
    public int insertCfPunchSettings(CfPunchSettings cfPunchSettings);

    /**
     * 修改打卡设置
     *
     * @param cfPunchSettings 打卡设置
     * @return 结果
     */
    public int updateCfPunchSettings(CfPunchSettings cfPunchSettings);

    /**
     * 批量删除打卡设置
     *
     * @param ids 需要删除的打卡设置主键集合
     * @return 结果
     */
    public int deleteCfPunchSettingsByIds(String[] ids);

    /**
     * 删除打卡设置信息
     *
     * @param id 打卡设置主键
     * @return 结果
     */
    public int deleteCfPunchSettingsById(String id);
}
