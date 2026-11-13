package com.ruoyi.project.face.domain;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class FaceRecognizeVo {

    @NotBlank(message = "imageBase64 is required")
    private String imageBase64;
}
