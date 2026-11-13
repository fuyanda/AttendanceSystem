package com.ruoyi.project.face.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.project.face.mapper.CfUserMapper;
import com.ruoyi.project.face.domain.CfUser;
import com.ruoyi.project.face.service.ICfUserService;

/**
 * 用户信息Service业务层处理
 *
 * @author fuyd
 * @date 2025-10-21
 */
@Service
public class CfUserServiceImpl implements ICfUserService
{
    @Autowired
    private CfUserMapper cfUserMapper;

    /**
     * 查询用户信息
     *
     * @param id 用户信息主键
     * @return 用户信息
     */
    @Override
    public CfUser selectCfUserById(Long id)
    {
        return cfUserMapper.selectCfUserById(id);
    }

    /**
     * 根据电话号码查询用户信息
     *
     * @param phone 电话号码
     * @return 用户信息
     */
    @Override
    public CfUser selectCfUserByPhone(String phone) {
        return cfUserMapper.selectCfUserByPhone(phone);
    }

    @Override
    public CfUser selectCfUserByOpenId(String openId) {
        return cfUserMapper.selectCfUserByOpenId(openId);
    }

    /**
     * 查询用户信息列表
     *
     * @param cfUser 用户信息
     * @return 用户信息
     */
    @Override
    public List<CfUser> selectCfUserList(CfUser cfUser)
    {
        return cfUserMapper.selectCfUserList(cfUser);
    }

    /**
     * 新增用户信息
     *
     * @param cfUser 用户信息
     * @return 结果
     */
    @Override
    public int insertCfUser(CfUser cfUser)
    {
        cfUser.setCreateTime(DateUtils.getNowDate());
        return cfUserMapper.insertCfUser(cfUser);
    }

    /**
     * 修改用户信息
     *
     * @param cfUser 用户信息
     * @return 结果
     */
    @Override
    public int updateCfUser(CfUser cfUser)
    {
        cfUser.setUpdateTime(DateUtils.getNowDate());
        return cfUserMapper.updateCfUser(cfUser);
    }

    /**
     * 批量删除用户信息
     *
     * @param ids 需要删除的用户信息主键
     * @return 结果
     */
    @Override
    public int deleteCfUserByIds(Long[] ids)
    {
        return cfUserMapper.deleteCfUserByIds(ids);
    }

    /**
     * 删除用户信息信息
     *
     * @param id 用户信息主键
     * @return 结果
     */
    @Override
    public int deleteCfUserById(Long id)
    {
        return cfUserMapper.deleteCfUserById(id);
    }
}
