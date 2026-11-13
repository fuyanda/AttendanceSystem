package com.ruoyi.project.face.service.impl;

import com.ruoyi.project.face.domain.WxMsgCheckVo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CfStudentServiceImplTest {
    @Autowired
    CfStudentServiceImpl cfStudentServiceImpl;

    @Test
    void testInfo() {
        WxMsgCheckVo info = cfStudentServiceImpl.getInfoByID(4l);
        System.out.println(info);
        System.out.println(info.getStudentName());
    }
    @Test
    void selectAvatars() {
        cfStudentServiceImpl.selectAvatars().forEach(System.out::println);
    }
}
