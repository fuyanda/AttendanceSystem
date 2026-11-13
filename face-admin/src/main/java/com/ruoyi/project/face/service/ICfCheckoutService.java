package com.ruoyi.project.face.service;

import com.ruoyi.project.face.domain.CfCheckout;
import com.ruoyi.project.face.domain.CheckInfoVo;
import com.ruoyi.project.face.domain.DeptCountVo;
import com.ruoyi.project.face.domain.ManualCheckoutCandidateVo;

import java.util.List;

/**
 * 签退Service接口
 *
 * @author fuyd
 * @date 2025-08-14
 */
public interface ICfCheckoutService {
    /**
     * 查询签退
     *
     * @param id 签退主键
     * @return 签退
     */
    public CfCheckout selectCfCheckoutById(Long id);

    /**
     * 查询签退列表
     *
     * @param cfCheckout 签退
     * @return 签退集合
     */
    public List<CfCheckout> selectCfCheckoutList(CfCheckout cfCheckout);

    /**
     * 新增签退
     *
     * @param cfCheckout 签退
     * @return 结果
     */
    public CfCheckout insertCfCheckout(CfCheckout cfCheckout);

    /**
     * 修改签退
     *
     * @param cfCheckout 签退
     * @return 结果
     */
    public int updateCfCheckout(CfCheckout cfCheckout);

    /**
     * 批量删除签退
     *
     * @param ids 需要删除的签退主键集合
     * @return 结果
     */
    public int deleteCfCheckoutByIds(Long[] ids);

    /**
     * 删除签退信息
     *
     * @param id 签退主键
     * @return 结果
     */
    public int deleteCfCheckoutById(Long id);

    int rePush(Long id);

    List<ManualCheckoutCandidateVo> listManualCheckoutCandidates(String studentName, String deptId);

    CfCheckout manualCheckout(Long studentId);

    CheckInfoVo getCheckInfo(Long id);

    List<DeptCountVo> countTodayByDept();
}
