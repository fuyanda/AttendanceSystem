import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getHost } from '@/shared/config/host.js'
import { uploadFile } from '@/shared/http/upload.js'
import { fetchStudent, syncBaiduFace, updateStudent } from '@/modules/students/api/index.js'
import { requestCameraPermission } from '@/shared/utils/camera-permission.js'
import { CAMERA_CAPTURE_CANCELLED, openCameraCapture } from '@/shared/utils/camera-capture.js'
import { compressImageForFace } from '@/shared/utils/image.js'

export function useFaceCapture() {
  const studentId = ref(null)
  const studentName = ref('')
  const existingPhoto = ref('')
  const previewPath = ref('')
  const capturedPath = ref('')
  const processing = ref(false)
  const submitting = ref(false)
  const tips = ref('请对照黄色框线确认照片合格后保存')

  const hasExistingFace = computed(() => !!existingPhoto.value)

  async function loadStudentInfo() {
    const student = await fetchStudent(studentId.value)
    studentName.value = student.name || studentName.value
    existingPhoto.value = student.photo || ''
  }

  onLoad(async (query) => {
    studentId.value = query.id ? Number(query.id) : null
    studentName.value = query.name ? decodeURIComponent(query.name) : ''
    const path = query.path ? decodeURIComponent(query.path) : ''

    if (!studentId.value || !path) {
      uni.showToast({ title: '缺少采集信息', icon: 'none' })
      setTimeout(() => uni.navigateBack(), 600)
      return
    }

    previewPath.value = path
    capturedPath.value = path

    try {
      await loadStudentInfo()
    } catch (e) {
      uni.showToast({ title: e.message || '加载学生失败', icon: 'none' })
      setTimeout(() => uni.navigateBack(), 600)
    }
  })

  function goBack() {
    uni.navigateBack()
  }

  async function capturePhoto() {
    if (processing.value || submitting.value || !studentId.value) return
    const granted = await requestCameraPermission()
    if (!granted) {
      uni.showToast({ title: '未获得相机权限', icon: 'none' })
      return
    }
    processing.value = true
    try {
      const path = await openCameraCapture()
      capturedPath.value = path
      previewPath.value = path
      tips.value = '请对照黄色框线确认照片合格后保存'
    } catch (e) {
      if (e.message === CAMERA_CAPTURE_CANCELLED) return
      uni.showToast({ title: e.message || '拍照失败', icon: 'none' })
    } finally {
      processing.value = false
    }
  }

  async function confirmSave() {
    if (!capturedPath.value || submitting.value || !studentId.value) return
    submitting.value = true
    tips.value = '正在压缩照片...'
    try {
      // 采集略高于打卡分辨率，兼顾百度注册质量与上传速度
      const uploadPath = await compressImageForFace(capturedPath.value, { maxSide: 1080, quality: 85 })
      tips.value = '正在上传照片...'
      const avatarPath = await uploadFile(uploadPath)
      tips.value = '正在保存学生信息...'
      const student = await fetchStudent(studentId.value)
      await updateStudent({
        id: student.id,
        name: student.name,
        phone: student.phone,
        deptId: student.deptId,
        deadline: student.deadline || null,
        avatar: avatarPath,
        remark: student.remark || '',
        bindCode: student.bindCode || null
      })
      tips.value = '正在同步百度云人脸库...'
      await syncBaiduFace(studentId.value)
      existingPhoto.value = `${getHost()}${avatarPath}`
      uni.showToast({ title: '人脸采集成功', icon: 'success' })
      setTimeout(() => uni.navigateBack(), 800)
    } catch (e) {
      uni.showToast({ title: e.message || '保存失败', icon: 'none' })
      tips.value = '请对照黄色框线确认照片合格后保存'
    } finally {
      submitting.value = false
    }
  }

  return {
    studentName,
    previewPath,
    submitting,
    tips,
    hasExistingFace,
    goBack,
    capturePhoto,
    confirmSave
  }
}
