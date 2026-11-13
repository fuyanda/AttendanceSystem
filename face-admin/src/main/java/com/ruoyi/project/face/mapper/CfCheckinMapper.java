package com.ruoyi.project.face.mapper;

import com.ruoyi.project.face.domain.CfCheckin;
import com.ruoyi.project.face.domain.CheckInfoVo;
import com.ruoyi.project.face.domain.DeptCountVo;
import com.ruoyi.project.face.domain.ManualCheckinCandidateVo;
import com.ruoyi.project.face.domain.ManualCheckoutCandidateVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 签到Mapper接口
 *
 * @author fuyd
 * @date 2025-08-14
 */
public interface CfCheckinMapper {
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
    public int insertCfCheckin(CfCheckin cfCheckin);

    /**
     * 修改签到
     *
     * @param cfCheckin 签到
     * @return 结果
     */
    public int updateCfCheckin(CfCheckin cfCheckin);

    /**
     * 删除签到
     *
     * @param id 签到主键
     * @return 结果
     */
    public int deleteCfCheckinById(Long id);

    /**
     * 批量删除签到
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCfCheckinByIds(Long[] ids);

    CheckInfoVo getCheckInfo(Long id);

    int countByStudentAndTimeRange(@Param("studentId") Long studentId,
                                   @Param("startTime") String startTime,
                                   @Param("endTime") String endTime);

    CfCheckin selectTodayLatestByStudentId(@Param("studentId") Long studentId);

    List<CfCheckin> selectTodayLatestCheckins(@Param("studentName") String studentName);

    List<ManualCheckoutCandidateVo> selectManualCheckoutCandidates(@Param("studentName") String studentName,
                                                                     @Param("deptId") String deptId);

    List<ManualCheckinCandidateVo> selectManualCheckinCandidates(@Param("studentName") String studentName,
                                                                   @Param("deptId") String deptId);

    List<DeptCountVo> countTodayByDept();
}
