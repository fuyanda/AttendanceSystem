package com.ruoyi.project.face.service.impl;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.DictUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.project.face.domain.CfClassReviewItem;
import com.ruoyi.project.face.domain.CfClassReviewSession;
import com.ruoyi.project.face.domain.CfStudent;
import com.ruoyi.project.face.domain.CfStudentFee;
import com.ruoyi.project.face.domain.ClassReviewCreateRequest;
import com.ruoyi.project.face.domain.ClassReviewInfoVo;
import com.ruoyi.project.face.domain.ClassReviewSaveRequest;
import com.ruoyi.project.face.domain.ClassReviewSubjectVo;
import com.ruoyi.project.face.domain.ClassReviewTodayVo;
import com.ruoyi.project.face.domain.FeeBatchConsumeRequest;
import com.ruoyi.project.face.domain.WxMsgCheckVo;
import com.ruoyi.project.face.domain.WxMsgReviewVo;
import com.ruoyi.project.face.mapper.CfClassReviewMapper;
import com.ruoyi.project.face.mapper.CfStudentFeeMapper;
import com.ruoyi.project.face.mapper.CfStudentMapper;
import com.ruoyi.project.face.service.ICfClassReviewService;
import com.ruoyi.project.face.service.ICfStudentParentService;
import com.ruoyi.project.face.service.IWxPushService;
import com.ruoyi.project.system.domain.SysDictData;
import com.ruoyi.project.system.service.ISysDictTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CfClassReviewServiceImpl implements ICfClassReviewService {

    private static final String DICT_SUBJECT = "cf_fee_subject";
    private static final String CAMPUS_LEADER_POST_CODE = "campus_leader";
    private static final String DEFAULT_SUBJECT = "math";

    private final CfClassReviewMapper cfClassReviewMapper;
    private final CfStudentFeeMapper cfStudentFeeMapper;
    private final CfStudentMapper cfStudentMapper;
    private final ICfStudentParentService cfStudentParentService;
    private final IWxPushService wxPushService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSessionAfterConsume(FeeBatchConsumeRequest request, Map<Long, Long> feeIdToConsumeId) {
        if (CollectionUtils.isEmpty(feeIdToConsumeId)) {
            throw new ServiceException("\u65e0\u6d88\u8bfe\u8bb0\u5f55\uff0c\u65e0\u6cd5\u521b\u5efa\u70b9\u8bc4");
        }
        Date now = DateUtils.getNowDate();
        CfClassReviewSession session = new CfClassReviewSession();
        session.setTeacherUserId(request.getTeacherUserId());
        session.setSubject(request.getSubject());
        session.setClassTime(request.getConsumeTime());
        session.setStatus("0");
        session.setDelFlag("0");
        session.setCreateBy(SecurityUtils.getUsername());
        session.setCreateTime(now);
        cfClassReviewMapper.insertSession(session);

        for (Map.Entry<Long, Long> entry : feeIdToConsumeId.entrySet()) {
            CfStudentFee fee = cfStudentFeeMapper.selectCfStudentFeeById(entry.getKey());
            if (fee == null) {
                continue;
            }
            CfClassReviewItem item = new CfClassReviewItem();
            item.setSessionId(session.getId());
            item.setStudentId(fee.getStudentId());
            item.setFeeId(entry.getKey());
            item.setConsumeId(entry.getValue());
            item.setDisciplineScore(5);
            item.setParticipationScore(5);
            item.setPushFlag("0");
            item.setCreateBy(SecurityUtils.getUsername());
            item.setCreateTime(now);
            cfClassReviewMapper.insertItem(item);
        }
        return session.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClassReviewTodayVo createStandalone(ClassReviewCreateRequest request) {
        if (request == null || request.getStudentId() == null) {
            throw new ServiceException("学员ID不能为空");
        }
        CfStudent student = cfStudentMapper.selectCfStudentById(request.getStudentId());
        if (student == null || (StringUtils.isNotEmpty(student.getDelFlag()) && !"0".equals(student.getDelFlag()))) {
            throw new ServiceException("学员不存在");
        }

        ClassReviewTodayVo existing = selectTodayByStudentId(request.getStudentId());
        if (existing != null) {
            return existing;
        }

        String subject = StringUtils.isNotEmpty(request.getSubject()) ? request.getSubject() : DEFAULT_SUBJECT;
        Date classTime = request.getClassTime() != null ? request.getClassTime() : DateUtils.getNowDate();
        Date now = DateUtils.getNowDate();

        CfClassReviewSession session = new CfClassReviewSession();
        session.setTeacherUserId(SecurityUtils.getUserId());
        session.setSubject(subject);
        session.setClassTime(classTime);
        session.setStatus("0");
        session.setDelFlag("0");
        session.setCreateBy(SecurityUtils.getUsername());
        session.setCreateTime(now);
        cfClassReviewMapper.insertSession(session);

        CfClassReviewItem item = new CfClassReviewItem();
        item.setSessionId(session.getId());
        item.setStudentId(request.getStudentId());
        item.setFeeId(null);
        item.setConsumeId(null);
        item.setDisciplineScore(5);
        item.setParticipationScore(5);
        item.setPushFlag("0");
        item.setCreateBy(SecurityUtils.getUsername());
        item.setCreateTime(now);
        cfClassReviewMapper.insertItem(item);

        ClassReviewTodayVo vo = new ClassReviewTodayVo();
        vo.setSessionId(session.getId());
        vo.setItemId(item.getId());
        vo.setStatus("0");
        return vo;
    }

    @Override
    public List<ClassReviewSubjectVo> listSubjects() {
        List<ClassReviewSubjectVo> result = new ArrayList<>();
        List<SysDictData> datas = DictUtils.getDictCache(DICT_SUBJECT);
        if (CollectionUtils.isEmpty(datas)) {
            // 缓存未命中时走字典服务查库
            try {
                datas = SpringUtils.getBean(ISysDictTypeService.class).selectDictDataByType(DICT_SUBJECT);
            } catch (Exception e) {
                datas = null;
            }
        }
        if (!CollectionUtils.isEmpty(datas)) {
            for (SysDictData d : datas) {
                if (d == null || StringUtils.isEmpty(d.getDictValue())) {
                    continue;
                }
                result.add(new ClassReviewSubjectVo(d.getDictValue(), d.getDictLabel()));
            }
        }
        if (result.isEmpty()) {
            result.add(new ClassReviewSubjectVo("chinese", "语文"));
            result.add(new ClassReviewSubjectVo("math", "数学"));
            result.add(new ClassReviewSubjectVo("english", "英语"));
            result.add(new ClassReviewSubjectVo("geography", "地理"));
            result.add(new ClassReviewSubjectVo("biology", "生物"));
            result.add(new ClassReviewSubjectVo("chemistry", "化学"));
        }
        return result;
    }

    @Override
    public CfClassReviewSession selectSessionDetail(Long sessionId) {
        assertSessionAccess(sessionId);
        CfClassReviewSession session = cfClassReviewMapper.selectSessionById(sessionId);
        if (session == null) {
            throw new ServiceException("\u70b9\u8bc4\u6279\u6b21\u4e0d\u5b58\u5728");
        }
        session.setSubjectLabel(DictUtils.getDictLabel(DICT_SUBJECT, session.getSubject()));
        session.setItems(cfClassReviewMapper.selectItemsBySessionId(sessionId));
        return session;
    }

    @Override
    public List<CfClassReviewSession> selectSessionList(CfClassReviewSession query) {
        if (!canSelectTeacher()) {
            query.setTeacherUserId(SecurityUtils.getUserId());
        }
        List<CfClassReviewSession> list = cfClassReviewMapper.selectSessionList(query);
        list.forEach(s -> s.setSubjectLabel(DictUtils.getDictLabel(DICT_SUBJECT, s.getSubject())));
        return list;
    }

    @Override
    public ClassReviewTodayVo selectTodayByStudentId(Long studentId) {
        if (studentId == null) {
            throw new ServiceException("学员ID不能为空");
        }
        Long teacherUserId = canSelectTeacher() ? null : SecurityUtils.getUserId();
        ClassReviewTodayVo vo = cfClassReviewMapper.selectTodayByStudentId(studentId, teacherUserId);
        if (vo == null || vo.getSessionId() == null) {
            return null;
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveDraft(ClassReviewSaveRequest request) {
        if (request.getSessionId() == null) {
            throw new ServiceException("\u6279\u6b21ID\u4e0d\u80fd\u4e3a\u7a7a");
        }
        assertSessionAccess(request.getSessionId());
        CfClassReviewSession session = cfClassReviewMapper.selectSessionById(request.getSessionId());
        if (session == null) {
            throw new ServiceException("\u70b9\u8bc4\u6279\u6b21\u4e0d\u5b58\u5728");
        }
        if ("1".equals(session.getStatus())) {
            throw new ServiceException("\u5df2\u53d1\u9001\u7684\u70b9\u8bc4\u4e0d\u53ef\u4fee\u6539");
        }
        Date now = DateUtils.getNowDate();
        CfClassReviewSession update = new CfClassReviewSession();
        update.setId(request.getSessionId());
        if (request.getClassTime() != null) {
            update.setClassTime(request.getClassTime());
        }
        update.setClassContent(request.getClassContent());
        update.setClassImages(request.getClassImages());
        update.setUpdateBy(SecurityUtils.getUsername());
        update.setUpdateTime(now);
        cfClassReviewMapper.updateSession(update);

        if (!CollectionUtils.isEmpty(request.getItems())) {
            for (CfClassReviewItem item : request.getItems()) {
                if (item.getId() == null) {
                    continue;
                }
                CfClassReviewItem row = new CfClassReviewItem();
                row.setId(item.getId());
                row.setDisciplineScore(clampScore(item.getDisciplineScore()));
                row.setParticipationScore(clampScore(item.getParticipationScore()));
                row.setContent(item.getContent());
                row.setImages(item.getImages());
                row.setUpdateBy(SecurityUtils.getUsername());
                row.setUpdateTime(now);
                cfClassReviewMapper.updateItem(row);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendSession(Long sessionId) {
        assertSessionAccess(sessionId);
        CfClassReviewSession session = selectSessionDetail(sessionId);
        if ("1".equals(session.getStatus())) {
            throw new ServiceException("\u8be5\u6279\u6b21\u5df2\u53d1\u9001");
        }
        if (CollectionUtils.isEmpty(session.getItems())) {
            throw new ServiceException("\u65e0\u5b66\u5458\u53ef\u53d1\u9001");
        }
        String subjectLabel = DictUtils.getDictLabel(DICT_SUBJECT, session.getSubject());
        String classTimeStr = session.getClassTime() != null
                ? DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, session.getClassTime()) : "";
        int sent = 0;
        String lastError = null;
        for (CfClassReviewItem item : session.getItems()) {
            if ("1".equals(item.getPushFlag())) {
                continue;
            }
            List<WxMsgCheckVo> targets = cfStudentParentService.selectNotifyTargetsByStudentId(item.getStudentId());
            if (CollectionUtils.isEmpty(targets)) {
                log.warn("student has no notify target, itemId={}, studentId={}, studentName={}",
                        item.getId(), item.getStudentId(), item.getStudentName());
                lastError = "请确认家长已绑定微信并开启通知";
                continue;
            }
            String hourDetail = buildHourDetail(item);
            boolean itemSent = false;
            for (WxMsgCheckVo target : targets) {
                WxMsgReviewVo msg = WxMsgReviewVo.builder()
                        .openId(target.getOpenId())
                        .studentName(item.getStudentName())
                        .subjectLabel(subjectLabel)
                        .classTime(classTimeStr)
                        .teacherName(session.getTeacherName())
                        .hourDetail(hourDetail)
                        .itemId(item.getId())
                        .build();
                try {
                    wxPushService.sendWxMsgReview(msg);
                    sent++;
                    itemSent = true;
                } catch (IllegalStateException e) {
                    // 配置类错误直接抛出，避免误报成家长未绑定
                    throw new ServiceException(e.getMessage());
                } catch (Exception e) {
                    lastError = e.getMessage();
                    log.error("review push failed, sessionId={}, itemId={}, studentId={}, studentName={}, openId={}, msg={}",
                            sessionId, item.getId(), item.getStudentId(), item.getStudentName(),
                            target.getOpenId(), e.getMessage(), e);
                }
            }
            if (itemSent) {
                cfClassReviewMapper.updateItemPushFlag(item.getId(), "1");
            }
        }
        if (sent == 0) {
            throw new ServiceException(StringUtils.isNotEmpty(lastError)
                    ? ("推送失败：" + lastError)
                    : "推送失败：请确认家长已绑定微信并开启通知");
        }
        CfClassReviewSession update = new CfClassReviewSession();
        update.setId(sessionId);
        update.setStatus("1");
        update.setUpdateBy(SecurityUtils.getUsername());
        update.setUpdateTime(DateUtils.getNowDate());
        cfClassReviewMapper.updateSession(update);
    }

    @Override
    public ClassReviewInfoVo getParentReviewInfo(Long itemId) {
        CfClassReviewItem item = cfClassReviewMapper.selectItemById(itemId);
        if (item == null) {
            return null;
        }
        CfClassReviewSession session = cfClassReviewMapper.selectSessionById(item.getSessionId());
        if (session == null) {
            return null;
        }
        ClassReviewInfoVo vo = new ClassReviewInfoVo();
        vo.setStudentName(item.getStudentName());
        vo.setSubjectLabel(DictUtils.getDictLabel(DICT_SUBJECT, session.getSubject()));
        vo.setTeacherName(session.getTeacherName());
        vo.setClassTime(session.getClassTime());
        vo.setClassContent(session.getClassContent());
        vo.setClassImages(parseImages(session.getClassImages()));
        vo.setDisciplineScore(item.getDisciplineScore());
        vo.setParticipationScore(item.getParticipationScore());
        vo.setContent(item.getContent());
        vo.setImages(parseImages(item.getImages()));
        return vo;
    }

    private String buildHourDetail(CfClassReviewItem item) {
        if (item.getFeeId() == null && item.getConsumeId() == null) {
            return "课堂表现点评";
        }
        int consume = item.getConsumeHours() != null ? item.getConsumeHours() : 0;
        int remain = item.getRemainingHours() != null ? item.getRemainingHours() : 0;
        return "本次扣" + consume + "节 剩" + remain + "节";
    }

    private List<String> parseImages(String json) {
        if (StringUtils.isEmpty(json)) {
            return Collections.emptyList();
        }
        try {
            return JSON.parseArray(json, String.class);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private int clampScore(Integer score) {
        if (score == null || score < 1) {
            return 1;
        }
        return Math.min(score, 5);
    }

    private void assertSessionAccess(Long sessionId) {
        CfClassReviewSession session = cfClassReviewMapper.selectSessionById(sessionId);
        if (session == null) {
            throw new ServiceException("\u70b9\u8bc4\u6279\u6b21\u4e0d\u5b58\u5728");
        }
        if (canSelectTeacher()) {
            return;
        }
        if (!SecurityUtils.getUserId().equals(session.getTeacherUserId())) {
            throw new ServiceException("\u65e0\u6743\u64cd\u4f5c\u5176\u4ed6\u8001\u5e08\u7684\u70b9\u8bc4");
        }
    }

    private boolean canSelectTeacher() {
        Long userId = SecurityUtils.getUserId();
        if (SecurityUtils.isAdmin(userId) || SecurityUtils.hasRole("admin")) {
            return true;
        }
        return cfStudentFeeMapper.countUserPostByCode(userId, CAMPUS_LEADER_POST_CODE) > 0;
    }
}
