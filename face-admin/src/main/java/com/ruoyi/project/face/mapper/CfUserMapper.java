package com.ruoyi.project.face.mapper;

import java.util.List;
import com.ruoyi.project.face.domain.CfUser;

/**
 * 用户信息Mapper接口
 *
 * @author fuyd
 * @date 2025-10-21
 */
public interface CfUserMapper
{
    /**
     * 查询用户信息
     *
     * @param id 用户信息主键
     * @return 用户信息
     */
    public CfUser selectCfUserById(Long id);

    /**
     * 查询用户信息列表
     *
     * @param cfUser 用户信息
     * @return 用户信息集合
     */
    public List<CfUser> selectCfUserList(CfUser cfUser);

    /**
     * 新增用户信息
     *
     * @param cfUser 用户信息
     * @return 结果
     */
    public int insertCfUser(CfUser cfUser);

    /**
     * 修改用户信息
     *
     * @param cfUser 用户信息
     * @return 结果
     */
    public int updateCfUser(CfUser cfUser);

    /**
     * 删除用户信息
     *
     * @param id 用户信息主键
     * @return 结果
     */
    public int deleteCfUserById(Long id);

    /**
     * 批量删除用户信息
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCfUserByIds(Long[] ids);

    public CfUser selectCfUserByPhone(String phone);

    public CfUser selectCfUserByOpenId(String openId);

    public CfUser selectCfUserByStudentID(Long id);
}
