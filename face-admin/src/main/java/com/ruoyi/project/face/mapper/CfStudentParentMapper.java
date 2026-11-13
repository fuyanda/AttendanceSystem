package com.ruoyi.project.face.mapper;

import com.ruoyi.project.face.domain.CfStudentParent;
import com.ruoyi.project.face.domain.WxMsgCheckVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CfStudentParentMapper {

    List<CfStudentParent> selectByStudentId(Long studentId);

    List<CfStudentParent> selectByUserId(Long userId);

    CfStudentParent selectByStudentIdAndUserId(@Param("studentId") Long studentId, @Param("userId") Long userId);

    int insertCfStudentParent(CfStudentParent relation);

    int updateCfStudentParent(CfStudentParent relation);

    int deleteByStudentId(Long studentId);

    int deleteByStudentIdAndUserId(@Param("studentId") Long studentId, @Param("userId") Long userId);

    List<WxMsgCheckVo> selectNotifyTargetsByStudentId(Long studentId);
}
