package com.ruoyi.project.face.service;

import com.ruoyi.project.face.domain.CfCheckin;
import com.ruoyi.project.face.domain.CheckInfoVo;
import com.ruoyi.project.face.domain.DeptCountVo;
import com.ruoyi.project.face.domain.ManualCheckinCandidateVo;

import java.util.List;

/**
 * 签到Service接口
 *
 * @author fuyd
 * @date 2025-08-14
 */
public interface ICfCheckinService {
    /**
     * 查询签到
     *
     * @param id 签到主键
     * @return 签到
     */
    public CfCheckin selectCfCheckinById(Long id);

    /**
     * 查询签到列表
     *
     * @param cfCheckin 签到
     * @return 签到集合
     */
    public List<CfCheckin> selectCfCheckinList(CfCheckin cfCheckin);

    /**
     * 新增签到
     *
     * @param cfCheckin 签到
     * @return 结果
     */
    public CfCheckin insertCfCheckin(CfCheckin cfCheckin);

    /**
     * 修改签到
     *
     * @param cfCheckin 签到
     * @return 结果
     */
    public int updateCfCheckin(CfCheckin cfCheckin);

    /**
     * 批量删除签到
     *
     * @param ids 需要删除的签到主键集合
     * @return 结果
     */
    public int deleteCfCheckinByIds(Long[] ids);

    /**
     * 删除签到信息
     *
     * @param id 签到主键
     * @return 结果
     */
    public int deleteCfCheckinById(Long id);

    int rePush(Long id);

    CheckInfoVo getCheckInfo(Long id);

    List<DeptCountVo> countTodayByDept();

    List<ManualCheckinCandidateVo> listManualCheckinCandidates(String studentName, String deptId);

    CfCheckin manualCheckin(Long studentId);
}
