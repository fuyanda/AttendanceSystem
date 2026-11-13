package com.ruoyi.project.face.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.project.face.domain.CfPunchSettings;

import java.util.List;

/**
 * 打卡设置Mapper接口
 *
 * @author 付延达
 * @date 2024-08-24
 */
public interface CfPunchSettingsMapper extends BaseMapper<CfPunchSettings> {
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
     * 删除打卡设置
     *
     * @param id 打卡设置主键
     * @return 结果
     */
    public int deleteCfPunchSettingsById(String id);

    /**
     * 批量删除打卡设置
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCfPunchSettingsByIds(String[] ids);
}
