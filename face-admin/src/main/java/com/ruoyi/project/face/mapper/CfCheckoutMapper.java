package com.ruoyi.project.face.mapper;

import com.ruoyi.project.face.domain.CfCheckout;
import com.ruoyi.project.face.domain.DeptCountVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 签退Mapper接口
 *
 * @author fuyd
 * @date 2025-08-14
 */
public interface CfCheckoutMapper {
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
    public int insertCfCheckout(CfCheckout cfCheckout);

    /**
     * 修改签退
     *
     * @param cfCheckout 签退
     * @return 结果
     */
    public int updateCfCheckout(CfCheckout cfCheckout);

    /**
     * 删除签退
     *
     * @param id 签退主键
     * @return 结果
     */
    public int deleteCfCheckoutById(Long id);

    /**
     * 批量删除签退
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCfCheckoutByIds(Long[] ids);

    int countByStudentAndTimeRange(@Param("studentId") Long studentId,
                                   @Param("startTime") String startTime,
                                   @Param("endTime") String endTime);

    com.ruoyi.project.face.domain.CheckInfoVo getCheckInfo(Long id);

    List<DeptCountVo> countTodayByDept();

    int countTodayCheckoutByStudentId(@Param("studentId") Long studentId);
}
