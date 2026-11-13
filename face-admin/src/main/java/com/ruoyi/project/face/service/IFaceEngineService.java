package com.ruoyi.project.face.service;

import com.ruoyi.project.face.domain.FaceRecognizeResultVo;
import com.ruoyi.project.face.domain.FaceSyncResultVo;

/**
 * Face engine facade: route search by config; register can write multiple backends.
 */
public interface IFaceEngineService {

    FaceRecognizeResultVo searchStudent(String imageBase64);

    void registerStudentFace(Long studentId, String imageBase64);

    void registerStudentFaceFromAvatar(Long studentId);

    FaceSyncResultVo syncAllStudentFaces();
}
