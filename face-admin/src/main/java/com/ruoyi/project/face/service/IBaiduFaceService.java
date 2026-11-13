package com.ruoyi.project.face.service;

import com.ruoyi.project.face.domain.FaceRecognizeResultVo;
import com.ruoyi.project.face.domain.FaceSyncResultVo;

public interface IBaiduFaceService {

    FaceRecognizeResultVo searchStudent(String imageBase64);

    void registerStudentFace(Long studentId, String imageBase64);

    void registerStudentFaceFromAvatar(Long studentId);

    FaceSyncResultVo syncAllStudentFaces();
}
