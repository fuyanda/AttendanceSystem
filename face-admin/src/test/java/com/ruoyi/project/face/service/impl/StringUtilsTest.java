package com.ruoyi.project.face.service.impl;

import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.utils.StringUtils;
import org.junit.jupiter.api.Test;

public class StringUtilsTest {
    @Test
    public void testSubstringAfter() {
        String filename = "/profile/face/1_测试_20250716175741A002.png";
        String s = StringUtils.substringAfter(filename, Constants.RESOURCE_PREFIX);
        System.out.println(s);
    }
}
