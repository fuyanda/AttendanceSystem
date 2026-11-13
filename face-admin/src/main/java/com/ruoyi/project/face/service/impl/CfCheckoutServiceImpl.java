package com.ruoyi.project.face.service.impl;

import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.ImageBase64Utils;
import com.ruoyi.framework.config.RuoYiConfig;
import com.ruoyi.project.face.domain.CfCheckin;
import com.ruoyi.project.face.domain.CfCheckout;
import com.ruoyi.project.face.domain.CheckInfoVo;
import com.ruoyi.project.face.domain.DeptCountVo;
import com.ruoyi.project.face.domain.ManualCheckoutCandidateVo;
import com.ruoyi.project.face.domain.WxMsgCheckVo;
import com.ruoyi.project.face.mapper.CfCheckinMapper;
import com.ruoyi.project.face.mapper.CfCheckoutMapper;
import com.ruoyi.project.face.service.ICfCheckoutService;
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
 * 签退Service业务层处理
 *
 * @author fuyd
 * @date 2025-08-14
 */
@Service
public class CfCheckoutServiceImpl implements ICfCheckoutService {
    private static final Logger log = LoggerFactory.getLogger(CfCheckoutServiceImpl.class);

    @Autowired
    private CfCheckoutMapper cfCheckoutMapper;
    @Autowired
    private CfCheckinMapper cfCheckinMapper;
    @Autowired
    private IWxPushService wxPushService;
    @Autowired
    private ICfStudentService studentService;
    @Autowired
    private IPunchWindowService punchWindowService;

    /**
     * 查询签退
     *
     * @param id 签退主键
     * @return 签退
     */
    @Override
    public CfCheckout selectCfCheckoutById(Long id) {
        return cfCheckoutMapper.selectCfCheckoutById(id);
    }

    /**
     * 查询签退列表
     *
     * @param cfCheckout 签退
     * @return 签退
     */
    @Override
    public List<CfCheckout> selectCfCheckoutList(CfCheckout cfCheckout) {
        return cfCheckoutMapper.selectCfCheckoutList(cfCheckout);
    }

    /**
     * 新增签退
     *
     * @param cfCheckout 签退
     * @return 结果
     */
    @Override
    public CfCheckout insertCfCheckout(CfCheckout cfCheckout) {
        punchWindowService.assertCheckoutAllowed(cfCheckout.getStudentId());

        String imageBase64 = cfCheckout.getImageBase64();
        if (StringUtils.isNotEmpty(imageBase64)) {
            String avatar = StringUtils.format("{}/{}.{}",DateUtils.datePath(), System.currentTimeMillis(), "jpg");
            // 保存文件地址
            String outputPath = StringUtils.format("{}/{}", RuoYiConfig.getUploadPath(), avatar);
            // 保存图片到本地
            ImageBase64Utils.saveBase64Image(cfCheckout.getImageBase64(), outputPath);

            cfCheckout.setAvatar(Constants.RESOURCE_PREFIX + StringUtils.substringAfter(outputPath, RuoYiConfig.getProfile()));
        }

        cfCheckout.setCreateTime(DateUtils.getNowDate());

        cfCheckoutMapper.insertCfCheckout(cfCheckout);
        extracted(cfCheckout);
        cfCheckoutMapper.updateCfCheckout(cfCheckout);

        cfCheckout.setImageBase64(null);

        return cfCheckout;
    }

    private void extracted(CfCheckout cfCheckout){
        extracted(cfCheckout, DateUtils.getTime());
    }

    private void extracted(CfCheckout cfCheckout, String time) {
        try {
            List<WxMsgCheckVo> targets = studentService.getNotifyTargetsByStudentId(cfCheckout.getStudentId());
            if (targets == null || targets.isEmpty()) {
                cfCheckout.setPushFlag("0");
                return;
            }
            boolean anySuccess = false;
            for (WxMsgCheckVo msg : targets) {
                msg.setCheckType("签退");
                msg.setCreateTime(time);
                msg.setRecordId(cfCheckout.getId());
                msg.setRecordType("checkout");
                String okStr = wxPushService.sendWxMsgCheck(msg);
                if (okStr != null) {
                    anySuccess = true;
                }
            }
            cfCheckout.setPushFlag(anySuccess ? "1" : "0");
        } catch (Exception e) {
            log.error("checkout wx push failed, checkoutId={}, studentId={}, msg={}",
                    cfCheckout.getId(), cfCheckout.getStudentId(), e.getMessage(), e);
            cfCheckout.setPushFlag("0");
        }
    }

    /**
     * 修改签退
     *
     * @param cfCheckout 签退
     * @return 结果
     */
    @Override
    public int updateCfCheckout(CfCheckout cfCheckout) {
        cfCheckout.setUpdateTime(DateUtils.getNowDate());
        return cfCheckoutMapper.updateCfCheckout(cfCheckout);
    }

    /**
     * 批量删除签退
     *
     * @param ids 需要删除的签退主键
     * @return 结果
     */
    @Override
    public int deleteCfCheckoutByIds(Long[] ids) {
        return cfCheckoutMapper.deleteCfCheckoutByIds(ids);
    }

    /**
     * 删除签退信息
     *
     * @param id 签退主键
     * @return 结果
     */
    @Override
    public int deleteCfCheckoutById(Long id) {
        return cfCheckoutMapper.deleteCfCheckoutById(id);
    }

    @Override
    public int rePush(Long id) {
        CfCheckout cfCheckout = cfCheckoutMapper.selectCfCheckoutById(id);
        extracted(cfCheckout, DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, cfCheckout.getCreateTime()));
        return cfCheckoutMapper.updateCfCheckout(cfCheckout);
    }

    @Override
    public List<ManualCheckoutCandidateVo> listManualCheckoutCandidates(String studentName, String deptId) {
        if (StringUtils.isEmpty(deptId)) {
            return Collections.emptyList();
        }
        return cfCheckinMapper.selectManualCheckoutCandidates(studentName, deptId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CfCheckout manualCheckout(Long studentId) {
        CfCheckin todayCheckin = cfCheckinMapper.selectTodayLatestByStudentId(studentId);
        if (todayCheckin == null) {
            throw new ServiceException("该学生今日未签到，无法签退");
        }
        if (cfCheckoutMapper.countTodayCheckoutByStudentId(studentId) > 0) {
            throw new ServiceException("该学生今日已签退");
        }
        CfCheckout checkout = new CfCheckout();
        checkout.setStudentId(studentId);
        checkout.setAvatar(todayCheckin.getAvatar());
        return insertCfCheckout(checkout);
    }

    @Override
    public CheckInfoVo getCheckInfo(Long id) {
        CfCheckout checkout = cfCheckoutMapper.selectCfCheckoutById(id);
        if (checkout == null || !isWithin30Days(checkout.getCreateTime())) {
            return null;
        }
        return cfCheckoutMapper.getCheckInfo(id);
    }

    @Override
    public List<DeptCountVo> countTodayByDept() {
        return cfCheckoutMapper.countTodayByDept();
    }

    private boolean isWithin30Days(java.util.Date createTime) {
        if (createTime == null) {
            return false;
        }
        long diffMs = System.currentTimeMillis() - createTime.getTime();
        return diffMs <= 30L * 24 * 60 * 60 * 1000;
    }
}
