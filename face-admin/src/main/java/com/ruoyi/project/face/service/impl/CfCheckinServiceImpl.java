package com.ruoyi.project.face.service.impl;

import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.ImageBase64Utils;
import com.ruoyi.framework.config.RuoYiConfig;
import com.ruoyi.project.face.domain.CfCheckin;
import com.ruoyi.project.face.domain.CfStudent;
import com.ruoyi.project.face.domain.CheckInfoVo;
import com.ruoyi.project.face.domain.DeptCountVo;
import com.ruoyi.project.face.domain.ManualCheckinCandidateVo;
import com.ruoyi.project.face.domain.WxMsgCheckVo;
import com.ruoyi.project.face.mapper.CfCheckinMapper;
import com.ruoyi.project.face.service.ICfCheckinService;
import com.ruoyi.project.face.service.ICfStudentFeeService;
import com.ruoyi.project.face.service.ICfStudentService;
import com.ruoyi.project.face.service.IPunchWindowService;
import com.ruoyi.project.face.service.IWxPushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * 签到Service业务层处理
 *
 * @author fuyd
 * @date 2025-08-14
 */
@Service
public class CfCheckinServiceImpl implements ICfCheckinService {
    private static final Logger log = LoggerFactory.getLogger(CfCheckinServiceImpl.class);

    @Autowired
    private CfCheckinMapper cfCheckinMapper;
    @Autowired
    private ICfStudentService studentService;
    @Autowired
    private IWxPushService wxPushService;
    @Autowired
    private IPunchWindowService punchWindowService;
    @Autowired
    private ICfStudentFeeService cfStudentFeeService;

    /**
     * 查询签到
     *
     * @param id 签到主键
     * @return 签到
     */
    @Override
    public CfCheckin selectCfCheckinById(Long id) {
        return cfCheckinMapper.selectCfCheckinById(id);
    }

    /**
     * 查询签到列表
     *
     * @param cfCheckin 签到
     * @return 签到
     */
    @Override
    public List<CfCheckin> selectCfCheckinList(CfCheckin cfCheckin) {
        return cfCheckinMapper.selectCfCheckinList(cfCheckin);
    }

    /**
     * 新增签到
     *
     * @param cfCheckin 签到
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CfCheckin insertCfCheckin(CfCheckin cfCheckin) {
        punchWindowService.assertCheckinAllowed(cfCheckin.getStudentId());

        String imageBase64 = cfCheckin.getImageBase64();
        if (StringUtils.isNotEmpty(imageBase64)) {
            String avatar = StringUtils.format("{}/{}.{}",DateUtils.datePath(), System.currentTimeMillis(), "jpg");
            // 保存文件地址
            String outputPath = StringUtils.format("{}/{}", RuoYiConfig.getUploadPath(), avatar);
            // 保存图片到本地
            ImageBase64Utils.saveBase64Image(cfCheckin.getImageBase64(), outputPath);

            cfCheckin.setAvatar(Constants.RESOURCE_PREFIX + StringUtils.substringAfter(outputPath, RuoYiConfig.getProfile()));
        }

        cfCheckin.setCreateTime(DateUtils.getNowDate());

        cfCheckinMapper.insertCfCheckin(cfCheckin);
        if (cfCheckin.getFeeId() != null) {
            cfStudentFeeService.consumeForCheckin(cfCheckin.getFeeId(), cfCheckin.getId(), 1);
        }
        extracted(cfCheckin);
        cfCheckinMapper.updateCfCheckin(cfCheckin);

        cfCheckin.setImageBase64(null);

        return cfCheckin;
    }

    /**
     * 修改签到
     *
     * @param cfCheckin 签到
     * @return 结果
     */
    @Override
    public int updateCfCheckin(CfCheckin cfCheckin) {
        cfCheckin.setUpdateTime(DateUtils.getNowDate());
        return cfCheckinMapper.updateCfCheckin(cfCheckin);
    }

    /**
     * 批量删除签到
     *
     * @param ids 需要删除的签到主键
     * @return 结果
     */
    @Override
    public int deleteCfCheckinByIds(Long[] ids) {
        return cfCheckinMapper.deleteCfCheckinByIds(ids);
    }

    /**
     * 删除签到信息
     *
     * @param id 签到主键
     * @return 结果
     */
    @Override
    public int deleteCfCheckinById(Long id) {
        return cfCheckinMapper.deleteCfCheckinById(id);
    }

    @Override
    public int rePush(Long id) {
        CfCheckin checkin = cfCheckinMapper.selectCfCheckinById(id);
        extracted(checkin, DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, checkin.getCreateTime()));
        return cfCheckinMapper.updateCfCheckin(checkin);
    }

    private void extracted(CfCheckin checkin) {
        extracted(checkin, DateUtils.getTime());
    }

    private void extracted(CfCheckin checkin, String time) {
        try {
            List<WxMsgCheckVo> targets = studentService.getNotifyTargetsByStudentId(checkin.getStudentId());
            if (targets == null || targets.isEmpty()) {
                checkin.setPushFlag("0");
                return;
            }
            boolean anySuccess = false;
            for (WxMsgCheckVo msg : targets) {
                msg.setCheckType("签到");
                msg.setCreateTime(time);
                msg.setRecordId(checkin.getId());
                msg.setRecordType("checkin");
                String okStr = wxPushService.sendWxMsgCheck(msg);
                if (okStr != null) {
                    anySuccess = true;
                }
            }
            checkin.setPushFlag(anySuccess ? "1" : "0");
        } catch (Exception e) {
            log.error("checkin wx push failed, checkinId={}, studentId={}, msg={}",
                    checkin.getId(), checkin.getStudentId(), e.getMessage(), e);
            checkin.setPushFlag("0");
        }
    }

    @Override
    public List<ManualCheckinCandidateVo> listManualCheckinCandidates(String studentName, String deptId) {
        if (StringUtils.isEmpty(deptId)) {
            return Collections.emptyList();
        }
        return cfCheckinMapper.selectManualCheckinCandidates(studentName, deptId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CfCheckin manualCheckin(Long studentId) {
        if (studentId == null) {
            throw new ServiceException("学生ID不能为空");
        }
        if (cfCheckinMapper.selectTodayLatestByStudentId(studentId) != null) {
            throw new ServiceException("该学生今日已签到");
        }
        CfStudent student = studentService.selectCfStudentById(studentId);
        if (student == null) {
            throw new ServiceException("学生不存在");
        }
        CfCheckin checkin = new CfCheckin();
        checkin.setStudentId(studentId);
        checkin.setAvatar(student.getAvatar());
        return insertCfCheckin(checkin);
    }

    @Override
    public List<DeptCountVo> countTodayByDept() {
        return cfCheckinMapper.countTodayByDept();
    }

    @Override
    public CheckInfoVo getCheckInfo(Long id) {
        CfCheckin checkin = cfCheckinMapper.selectCfCheckinById(id);
        if (checkin == null || !isWithin30Days(checkin.getCreateTime())) {
            return null;
        }
        return cfCheckinMapper.getCheckInfo(id);
    }

    private boolean isWithin30Days(java.util.Date createTime) {
        if (createTime == null) {
            return false;
        }
        long diffMs = System.currentTimeMillis() - createTime.getTime();
        return diffMs <= 30L * 24 * 60 * 60 * 1000;
    }
}
