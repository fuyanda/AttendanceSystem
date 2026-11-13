package com.ruoyi.project.face.service;

import com.ruoyi.project.face.domain.CfStudent;
import com.ruoyi.project.face.domain.FaceCleanupResultVo;
import com.ruoyi.project.face.domain.StudentAccountVo;
import com.ruoyi.project.face.domain.WxMsgCheckVo;

import java.util.List;

/**
 * 学生管理Service接口
 *
 * @author ruoyi
 * @date 2024-10-17
 */
public interface ICfStudentService {
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
    public CfStudent insertCfStudent(CfStudent cfStudent);

    /**
     * 修改学生管理
     *
     * @param cfStudent 学生管理
     * @return 结果
     */
    public CfStudent updateCfStudent(CfStudent cfStudent);

    /**
     * 批量删除学生管理
     *
     * @param ids 需要删除的学生管理主键集合
     * @return 结果
     */
    public int deleteCfStudentByIds(Long[] ids);

    /**
     * 删除学生管理信息
     *
     * @param id 学生管理主键
     * @return 结果
     */
    public int deleteCfStudentById(Long id);

    /**
     * 查询学生头像列表
     *
     * @return
     */
    List<String> selectAvatars();

    public WxMsgCheckVo getInfoByID(Long studentId);

    List<WxMsgCheckVo> getNotifyTargetsByStudentId(Long studentId);

    CfStudent regenerateBindCode(Long studentId);

    /**
     * 清理本地人脸目录：按库中在用头像保留每人一张，删除多余与无引用文件。
     */
    FaceCleanupResultVo cleanupLocalFaces();
}
