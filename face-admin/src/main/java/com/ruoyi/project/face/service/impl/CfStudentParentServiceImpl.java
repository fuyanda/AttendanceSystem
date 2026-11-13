package com.ruoyi.project.face.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.BindCodeUtils;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.PasswordGenerator;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.project.face.domain.CfStudent;
import com.ruoyi.project.face.domain.CfStudentParent;
import com.ruoyi.project.face.domain.CfUser;
import com.ruoyi.project.face.domain.ParentBindByCodeVo;
import com.ruoyi.project.face.domain.ParentBindPreviewVo;
import com.ruoyi.project.face.domain.ParentBindResultVo;
import com.ruoyi.project.face.domain.ParentOpenIdLookupVo;
import com.ruoyi.project.face.domain.ParentQrBindVo;
import com.ruoyi.project.face.domain.StudentAccountVo;
import com.ruoyi.project.face.domain.StudentParentBindVo;
import com.ruoyi.project.face.domain.WxMsgCheckVo;
import com.ruoyi.project.face.mapper.CfStudentMapper;
import com.ruoyi.project.face.mapper.CfStudentParentMapper;
import com.ruoyi.project.face.mapper.CfUserMapper;
import com.ruoyi.project.face.service.ICfStudentParentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CfStudentParentServiceImpl implements ICfStudentParentService {

    private final CfStudentParentMapper cfStudentParentMapper;
    private final CfUserMapper cfUserMapper;
    private final CfStudentMapper cfStudentMapper;

    @Override
    public List<CfStudentParent> selectByStudentId(Long studentId) {
        return cfStudentParentMapper.selectByStudentId(studentId);
    }

    @Override
    public List<CfStudentParent> selectByUserId(Long userId) {
        return cfStudentParentMapper.selectByUserId(userId);
    }

    @Override
    public List<WxMsgCheckVo> selectNotifyTargetsByStudentId(Long studentId) {
        List<WxMsgCheckVo> targets = cfStudentParentMapper.selectNotifyTargetsByStudentId(studentId);
        if (!CollectionUtils.isEmpty(targets)) {
            return targets;
        }
        WxMsgCheckVo legacy = cfStudentMapper.getInfoByID(studentId);
        if (legacy != null && StringUtils.isNotEmpty(legacy.getOpenId())) {
            List<WxMsgCheckVo> fallback = new ArrayList<>();
            fallback.add(legacy);
            return fallback;
        }
        return targets;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<StudentAccountVo> replaceStudentParents(Long studentId, List<StudentParentBindVo> parents) {
        if (studentId == null) {
            throw new ServiceException("学生ID不能为空");
        }
        if (CollectionUtils.isEmpty(parents)) {
            throw new ServiceException("至少绑定一位家长");
        }
        ensureSinglePrimary(parents);
        cfStudentParentMapper.deleteByStudentId(studentId);

        List<StudentAccountVo> accounts = new ArrayList<>();
        for (StudentParentBindVo bindVo : parents) {
            accounts.add(bindOne(studentId, bindVo));
        }
        syncStudentPrimaryFields(studentId, parents);
        return accounts;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int bindParent(Long studentId, StudentParentBindVo bindVo) {
        if (Integer.valueOf(1).equals(bindVo.getIsPrimary())) {
            clearPrimaryFlag(studentId);
        }
        bindOne(studentId, bindVo);
        if (Integer.valueOf(1).equals(bindVo.getIsPrimary())) {
            syncStudentPrimaryFields(studentId, Collections.singletonList(bindVo));
        }
        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int unbindParent(Long studentId, Long userId) {
        CfStudentParent relation = cfStudentParentMapper.selectByStudentIdAndUserId(studentId, userId);
        if (relation == null) {
            return 0;
        }
        boolean wasPrimary = Integer.valueOf(1).equals(relation.getIsPrimary());
        int rows = cfStudentParentMapper.deleteByStudentIdAndUserId(studentId, userId);
        if (rows <= 0) {
            return 0;
        }
        List<CfStudentParent> remaining = cfStudentParentMapper.selectByStudentId(studentId);
        if (CollectionUtils.isEmpty(remaining)) {
            clearStudentPrimaryFields(studentId);
            return rows;
        }
        if (wasPrimary) {
            CfStudentParent next = remaining.get(0);
            next.setIsPrimary(1);
            next.setUpdateTime(DateUtils.getNowDate());
            cfStudentParentMapper.updateCfStudentParent(next);
            syncStudentPrimaryFields(studentId, Collections.singletonList(toBindVo(next)));
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int setPrimaryParent(Long studentId, Long userId) {
        CfStudentParent relation = cfStudentParentMapper.selectByStudentIdAndUserId(studentId, userId);
        if (relation == null) {
            throw new ServiceException("未找到该家长绑定关系");
        }
        if (Integer.valueOf(1).equals(relation.getIsPrimary())) {
            return 1;
        }
        clearPrimaryFlag(studentId);
        relation.setIsPrimary(1);
        relation.setUpdateTime(DateUtils.getNowDate());
        int rows = cfStudentParentMapper.updateCfStudentParent(relation);
        syncStudentPrimaryFields(studentId, Collections.singletonList(toBindVo(relation)));
        return rows;
    }

    @Override
    public int updateRelation(CfStudentParent relation) {
        relation.setUpdateTime(DateUtils.getNowDate());
        if (Integer.valueOf(1).equals(relation.getIsPrimary())) {
            clearPrimaryFlag(relation.getStudentId());
            syncStudentPrimaryFields(relation.getStudentId(),
                    cfStudentParentMapper.selectByStudentId(relation.getStudentId()).stream()
                            .map(item -> {
                                if (item.getId().equals(relation.getId())) {
                                    item.setIsPrimary(1);
                                }
                                return toBindVo(item);
                            }).collect(Collectors.toList()));
        }
        return cfStudentParentMapper.updateCfStudentParent(relation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ParentBindResultVo bindParentsByCode(ParentBindByCodeVo request) {
        String bindCode = BindCodeUtils.normalize(request.getBindCode());
        if (StringUtils.isEmpty(bindCode)) {
            throw new ServiceException("绑定码不能为空");
        }
        List<CfStudent> students = cfStudentMapper.selectListByBindCode(bindCode);
        if (CollectionUtils.isEmpty(students)) {
            throw new ServiceException("绑定码无效，请确认后重试");
        }

        CfUser user = cfUserMapper.selectCfUserByPhone(request.getPhone());
        boolean newlyCreated = false;
        String password;
        if (user == null) {
            newlyCreated = true;
            password = PasswordGenerator.generatePassword();
            user = CfUser.builder()
                    .code(bindCode)
                    .phone(request.getPhone())
                    .name(StringUtils.isEmpty(request.getName()) ? request.getPhone() : request.getName())
                    .password(PasswordGenerator.encode(password))
                    .isInviteEnabled(1)
                    .delFlag("0")
                    .build();
            user.setCreateTime(DateUtils.getNowDate());
            cfUserMapper.insertCfUser(user);
        } else {
            password = PasswordGenerator.decode(user.getPassword());
            if (!PasswordGenerator.encode(request.getPassword()).equals(user.getPassword())) {
                throw new ServiceException("手机号或密码错误");
            }
            if (StringUtils.isNotEmpty(request.getName()) && !request.getName().equals(user.getName())) {
                user.setName(request.getName());
            }
            if (StringUtils.isEmpty(user.getCode())) {
                user.setCode(bindCode);
            }
            user.setUpdateTime(DateUtils.getNowDate());
            cfUserMapper.updateCfUser(user);
        }

        List<String> studentNames = new ArrayList<>();
        int boundCount = 0;
        String relationType = StringUtils.isEmpty(request.getRelationType()) ? "parent" : request.getRelationType();
        for (CfStudent student : students) {
            CfStudentParent existing = cfStudentParentMapper.selectByStudentIdAndUserId(student.getId(), user.getId());
            if (existing != null) {
                studentNames.add(student.getName());
                continue;
            }
            StudentParentBindVo bindVo = new StudentParentBindVo();
            bindVo.setUserId(user.getId());
            bindVo.setPhone(user.getPhone());
            bindVo.setName(user.getName());
            bindVo.setRelationType(relationType);
            bindVo.setNotifyEnabled(1);
            List<CfStudentParent> currentRelations = cfStudentParentMapper.selectByStudentId(student.getId());
            bindVo.setIsPrimary(currentRelations.isEmpty() ? 1 : 0);
            bindOne(student.getId(), bindVo);
            if (Integer.valueOf(1).equals(bindVo.getIsPrimary())) {
                syncStudentPrimaryFields(student.getId(), Collections.singletonList(bindVo));
            }
            studentNames.add(student.getName());
            boundCount++;
        }

        return ParentBindResultVo.builder()
                .phone(user.getPhone())
                .password(password)
                .newlyCreated(newlyCreated)
                .bindCode(bindCode)
                .studentNames(studentNames)
                .boundCount(boundCount)
                .build();
    }

    @Override
    public ParentBindPreviewVo previewByBindCode(String bindCode) {
        String code = BindCodeUtils.normalize(bindCode);
        if (StringUtils.isEmpty(code)) {
            throw new ServiceException("绑定码不能为空");
        }
        List<CfStudent> students = cfStudentMapper.selectListByBindCode(code);
        if (CollectionUtils.isEmpty(students)) {
            throw new ServiceException("绑定码无效或已更新");
        }
        return ParentBindPreviewVo.builder()
                .bindCode(code)
                .studentName(students.get(0).getName())
                .build();
    }

    @Override
    public ParentOpenIdLookupVo lookupByOpenId(String openId) {
        if (StringUtils.isEmpty(openId)) {
            return ParentOpenIdLookupVo.builder().exists(false).build();
        }
        CfUser user = cfUserMapper.selectCfUserByOpenId(openId);
        if (user == null) {
            return ParentOpenIdLookupVo.builder().exists(false).build();
        }
        return ParentOpenIdLookupVo.builder()
                .exists(true)
                .name(user.getName())
                .phoneMasked(maskPhone(user.getPhone()))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ParentBindResultVo bindByQr(ParentQrBindVo request) {
        if (request == null || StringUtils.isEmpty(request.getOpenId())) {
            throw new ServiceException("微信身份无效，请重新扫码进入");
        }
        String openId = request.getOpenId().trim();
        String bindCode = BindCodeUtils.normalize(request.getBindCode());
        if (StringUtils.isEmpty(bindCode)) {
            throw new ServiceException("绑定码不能为空");
        }
        List<CfStudent> students = cfStudentMapper.selectListByBindCode(bindCode);
        if (CollectionUtils.isEmpty(students)) {
            throw new ServiceException("绑定码无效或已更新");
        }

        CfUser user = cfUserMapper.selectCfUserByOpenId(openId);
        boolean newlyCreated = false;
        String password = null;

        if (user == null) {
            String phone = request.getPhone() == null ? null : request.getPhone().trim();
            if (StringUtils.isEmpty(phone) || !phone.matches("^1[3-9]\\d{9}$")) {
                throw new ServiceException("请填写正确的手机号");
            }
            CfUser byPhone = cfUserMapper.selectCfUserByPhone(phone);
            if (byPhone == null) {
                newlyCreated = true;
                password = PasswordGenerator.generatePassword();
                user = CfUser.builder()
                        .code(bindCode)
                        .phone(phone)
                        .name(StringUtils.isEmpty(request.getName()) ? phone : request.getName())
                        .password(PasswordGenerator.encode(password))
                        .openId(openId)
                        .isInviteEnabled(1)
                        .delFlag("0")
                        .build();
                user.setCreateTime(DateUtils.getNowDate());
                cfUserMapper.insertCfUser(user);
            } else {
                if (StringUtils.isNotEmpty(byPhone.getOpenId()) && !openId.equals(byPhone.getOpenId())) {
                    throw new ServiceException("该手机号已绑定其他微信，请更换手机号或联系老师");
                }
                user = byPhone;
                password = PasswordGenerator.decode(user.getPassword());
                if (StringUtils.isNotEmpty(request.getName()) && !request.getName().equals(user.getName())) {
                    user.setName(request.getName());
                }
                if (StringUtils.isEmpty(user.getCode())) {
                    user.setCode(bindCode);
                }
                user.setOpenId(openId);
                user.setUpdateTime(DateUtils.getNowDate());
                cfUserMapper.updateCfUser(user);
            }
        } else {
            password = PasswordGenerator.decode(user.getPassword());
            if (StringUtils.isEmpty(user.getCode())) {
                user.setCode(bindCode);
                user.setUpdateTime(DateUtils.getNowDate());
                cfUserMapper.updateCfUser(user);
            }
        }

        ParentBindResultVo result = attachStudentsToUser(user, students, bindCode, newlyCreated, password);
        return result;
    }

    private ParentBindResultVo attachStudentsToUser(CfUser user, List<CfStudent> students, String bindCode,
                                                    boolean newlyCreated, String password) {
        List<String> studentNames = new ArrayList<>();
        int boundCount = 0;
        for (CfStudent student : students) {
            CfStudentParent existing = cfStudentParentMapper.selectByStudentIdAndUserId(student.getId(), user.getId());
            if (existing != null) {
                studentNames.add(student.getName());
                continue;
            }
            StudentParentBindVo bindVo = new StudentParentBindVo();
            bindVo.setUserId(user.getId());
            bindVo.setPhone(user.getPhone());
            bindVo.setName(user.getName());
            bindVo.setRelationType("parent");
            bindVo.setNotifyEnabled(1);
            List<CfStudentParent> currentRelations = cfStudentParentMapper.selectByStudentId(student.getId());
            bindVo.setIsPrimary(currentRelations.isEmpty() ? 1 : 0);
            bindOne(student.getId(), bindVo);
            if (Integer.valueOf(1).equals(bindVo.getIsPrimary())) {
                syncStudentPrimaryFields(student.getId(), Collections.singletonList(bindVo));
            }
            studentNames.add(student.getName());
            boundCount++;
        }
        return ParentBindResultVo.builder()
                .phone(user.getPhone())
                .password(password)
                .newlyCreated(newlyCreated)
                .bindCode(bindCode)
                .studentNames(studentNames)
                .boundCount(boundCount)
                .build();
    }

    private String maskPhone(String phone) {
        if (StringUtils.isEmpty(phone) || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private StudentAccountVo bindOne(Long studentId, StudentParentBindVo bindVo) {
        if (bindVo == null || StringUtils.isEmpty(bindVo.getPhone())) {
            throw new ServiceException("家长手机号不能为空");
        }
        UserEnsureResult ensureResult = ensureUser(bindVo);
        CfStudentParent existing = cfStudentParentMapper.selectByStudentIdAndUserId(studentId, ensureResult.user.getId());
        if (existing != null) {
            existing.setRelationType(StringUtils.isEmpty(bindVo.getRelationType()) ? "parent" : bindVo.getRelationType());
            existing.setIsPrimary(bindVo.getIsPrimary() == null ? existing.getIsPrimary() : bindVo.getIsPrimary());
            existing.setNotifyEnabled(bindVo.getNotifyEnabled() == null ? 1 : bindVo.getNotifyEnabled());
            existing.setUpdateTime(DateUtils.getNowDate());
            cfStudentParentMapper.updateCfStudentParent(existing);
            return ensureResult.account;
        }
        CfStudentParent relation = new CfStudentParent();
        relation.setStudentId(studentId);
        relation.setUserId(ensureResult.user.getId());
        relation.setRelationType(StringUtils.isEmpty(bindVo.getRelationType()) ? "parent" : bindVo.getRelationType());
        relation.setIsPrimary(bindVo.getIsPrimary() == null ? 0 : bindVo.getIsPrimary());
        relation.setNotifyEnabled(bindVo.getNotifyEnabled() == null ? 1 : bindVo.getNotifyEnabled());
        relation.setCreateTime(DateUtils.getNowDate());
        cfStudentParentMapper.insertCfStudentParent(relation);
        return ensureResult.account;
    }

    private UserEnsureResult ensureUser(StudentParentBindVo bindVo) {
        CfUser cfUser = bindVo.getUserId() != null
                ? cfUserMapper.selectCfUserById(bindVo.getUserId())
                : cfUserMapper.selectCfUserByPhone(bindVo.getPhone());
        String password = PasswordGenerator.generatePassword();
        boolean newlyCreated = false;
        if (cfUser == null) {
            newlyCreated = true;
            cfUser = CfUser.builder()
                    .code(IdUtils.fastUUID())
                    .phone(bindVo.getPhone())
                    .name(StringUtils.isEmpty(bindVo.getName()) ? bindVo.getPhone() : bindVo.getName())
                    .password(PasswordGenerator.encode(password))
                    .isInviteEnabled(1)
                    .delFlag("0")
                    .build();
            cfUser.setCreateTime(DateUtils.getNowDate());
            cfUserMapper.insertCfUser(cfUser);
        } else {
            password = PasswordGenerator.decode(cfUser.getPassword());
            if (StringUtils.isNotEmpty(bindVo.getName()) && !bindVo.getName().equals(cfUser.getName())) {
                cfUser.setName(bindVo.getName());
                cfUser.setUpdateTime(DateUtils.getNowDate());
                cfUserMapper.updateCfUser(cfUser);
            }
        }
        StudentAccountVo account = StudentAccountVo.builder()
                .phone(cfUser.getPhone())
                .password(password)
                .newlyCreated(newlyCreated)
                .build();
        return new UserEnsureResult(cfUser, account);
    }

    private void clearStudentPrimaryFields(Long studentId) {
        CfStudent student = cfStudentMapper.selectCfStudentById(studentId);
        if (student == null) {
            return;
        }
        student.setParentTel("");
        student.setFamily("");
        student.setUpdateTime(DateUtils.getNowDate());
        cfStudentMapper.updateCfStudent(student);
    }

    private void syncStudentPrimaryFields(Long studentId, List<StudentParentBindVo> parents) {
        StudentParentBindVo primary = parents.stream()
                .filter(item -> Integer.valueOf(1).equals(item.getIsPrimary()))
                .findFirst()
                .orElse(parents.get(0));

        CfStudent student = cfStudentMapper.selectCfStudentById(studentId);
        if (student == null) {
            return;
        }
        student.setParentTel(primary.getPhone());
        student.setFamily(StringUtils.isEmpty(primary.getName()) ? student.getFamily() : primary.getName());
        student.setUpdateTime(DateUtils.getNowDate());
        cfStudentMapper.updateCfStudent(student);
    }

    private void ensureSinglePrimary(List<StudentParentBindVo> parents) {
        long primaryCount = parents.stream().filter(item -> Integer.valueOf(1).equals(item.getIsPrimary())).count();
        if (primaryCount == 0) {
            parents.get(0).setIsPrimary(1);
        } else if (primaryCount > 1) {
            throw new ServiceException("只能设置一位主联系人");
        }
    }

    private void clearPrimaryFlag(Long studentId) {
        List<CfStudentParent> relations = cfStudentParentMapper.selectByStudentId(studentId);
        for (CfStudentParent relation : relations) {
            if (Integer.valueOf(1).equals(relation.getIsPrimary())) {
                relation.setIsPrimary(0);
                relation.setUpdateTime(DateUtils.getNowDate());
                cfStudentParentMapper.updateCfStudentParent(relation);
            }
        }
    }

    private StudentParentBindVo toBindVo(CfStudentParent relation) {
        StudentParentBindVo vo = new StudentParentBindVo();
        vo.setUserId(relation.getUserId());
        vo.setPhone(relation.getPhone());
        vo.setName(relation.getUserName());
        vo.setRelationType(relation.getRelationType());
        vo.setIsPrimary(relation.getIsPrimary());
        vo.setNotifyEnabled(relation.getNotifyEnabled());
        return vo;
    }

    private static class UserEnsureResult {
        private final CfUser user;
        private final StudentAccountVo account;

        private UserEnsureResult(CfUser user, StudentAccountVo account) {
            this.user = user;
            this.account = account;
        }
    }
}
