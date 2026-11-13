package com.ruoyi.project.face.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.project.face.domain.CfStudent;
import com.ruoyi.project.face.domain.ScheduleStudentOption;
import com.ruoyi.project.face.domain.WxMsgCheckVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 学生管理Mapper接口
 *
 * @author ruoyi
 * @date 2024-10-17
 */
public interface CfStudentMapper extends BaseMapper<CfStudent> {
    /**
     * 查询学生管理
     *
     * @param id 学生管理主键
     * @return 学生管理
     */
    public CfStudent selectCfStudentById(Long id);

    /**
     * 查询学生管理列表
     *
     * @param cfStudent 学生管理
     * @return 学生管理集合
     */
    public List<CfStudent> selectCfStudentList(CfStudent cfStudent);

    /**
     * 新增学生管理
     *
     * @param cfStudent 学生管理
     * @return 结果
     */
    public int insertCfStudent(CfStudent cfStudent);

    /**
     * 修改学生管理
     *
     * @param cfStudent 学生管理
     * @return 结果
     */
    public int updateCfStudent(CfStudent cfStudent);

    /**
     * 删除学生管理
     *
     * @param id 学生管理主键
     * @return 结果
     */
    public int deleteCfStudentById(Long id);

    /**
     * 批量删除学生管理
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCfStudentByIds(Long[] ids);

    public WxMsgCheckVo getInfoByID(Long id);

    List<CfStudent> selectListByBindCode(String bindCode);

    int countByBindCode(String bindCode);

    int countByBindCodeExcludeId(@Param("bindCode") String bindCode, @Param("id") Long id);

    /** 排课选学生：未归档 */
    List<ScheduleStudentOption> selectActiveStudentOptions(@Param("name") String name);
}
