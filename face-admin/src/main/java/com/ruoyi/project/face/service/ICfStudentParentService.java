package com.ruoyi.project.face.service;

import com.ruoyi.project.face.domain.CfStudentParent;
import com.ruoyi.project.face.domain.ParentBindByCodeVo;
import com.ruoyi.project.face.domain.ParentBindPreviewVo;
import com.ruoyi.project.face.domain.ParentBindResultVo;
import com.ruoyi.project.face.domain.ParentOpenIdLookupVo;
import com.ruoyi.project.face.domain.ParentQrBindVo;
import com.ruoyi.project.face.domain.StudentAccountVo;
import com.ruoyi.project.face.domain.StudentParentBindVo;
import com.ruoyi.project.face.domain.WxMsgCheckVo;

import java.util.List;

public interface ICfStudentParentService {

    List<CfStudentParent> selectByStudentId(Long studentId);

    List<CfStudentParent> selectByUserId(Long userId);

    List<WxMsgCheckVo> selectNotifyTargetsByStudentId(Long studentId);

    List<StudentAccountVo> replaceStudentParents(Long studentId, List<StudentParentBindVo> parents);

    int bindParent(Long studentId, StudentParentBindVo bindVo);

    int unbindParent(Long studentId, Long userId);

    int setPrimaryParent(Long studentId, Long userId);

    int updateRelation(CfStudentParent relation);

    ParentBindResultVo bindParentsByCode(ParentBindByCodeVo request);

    ParentBindPreviewVo previewByBindCode(String bindCode);

    ParentOpenIdLookupVo lookupByOpenId(String openId);

    ParentBindResultVo bindByQr(ParentQrBindVo request);
}
