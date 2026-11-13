import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { recognizeFace, submitCheckin, submitCheckout, validatePunch } from '@/modules/punch/api/index.js'
import { fetchFeeOptions } from '@/modules/fee/api/index.js'
import { compressImageForFace, filePathToBase64 } from '@/shared/utils/image.js'
import { requestCameraPermission } from '@/shared/utils/camera-permission.js'
import { CAMERA_CAPTURE_CANCELLED, openCameraCapture } from '@/shared/utils/camera-capture.js'
import { createPerf, estimateBase64Bytes, formatKb, getFileSize } from '@/shared/utils/perf.js'

export function usePunch() {
  const mode = ref('checkin')
  const tips = ref('正在识别...')
  const previewPath = ref('')
  const imageBase64 = ref('')
  const processing = ref(false)
  const submitting = ref(false)
  const matchResult = ref(null)
  const feeOptions = ref([])
  const selectedFeeId = ref(null)
  const feeLoading = ref(false)
  let openingCamera = false

  const pageTitle = computed(() => (mode.value === 'checkin' ? '签到识别' : '签退识别'))
  const submitText = computed(() => (mode.value === 'checkin' ? '确认签到' : '确认签退'))
  const successText = computed(() => (mode.value === 'checkin' ? '签到成功' : '签退成功'))
  const hasPreview = computed(() => !!previewPath.value)
  const isCheckin = computed(() => mode.value === 'checkin')

  function goBack() {
    uni.navigateBack()
  }

  async function loadFeeOptions(studentId) {
    feeLoading.value = true
    feeOptions.value = []
    selectedFeeId.value = null
    try {
      const res = await fetchFeeOptions(studentId)
      feeOptions.value = res.data || []
      if (feeOptions.value.length === 1) {
        selectedFeeId.value = feeOptions.value[0].feeId
      } else if (feeOptions.value.length === 0) {
        uni.showToast({ title: '无可用课时包，将仅签到', icon: 'none', duration: 2500 })
      }
    } catch (e) {
      feeOptions.value = []
    } finally {
      feeLoading.value = false
    }
  }

  async function recognizeFromPath(path) {
    previewPath.value = path
    tips.value = '正在识别...'
    processing.value = true
    const perf = createPerf('recognize')
    try {
      const fileBytes = await getFileSize(path)
      perf.mark('fileInfo', { path, size: formatKb(fileBytes), bytes: fileBytes })

      const compressedPath = await compressImageForFace(path, { maxSide: 720, quality: 80 })
      const compressedBytes = await getFileSize(compressedPath)
      perf.mark('compress', {
        size: formatKb(compressedBytes),
        bytes: compressedBytes,
        path: compressedPath
      })

      const base64 = await filePathToBase64(compressedPath)
      const payloadBytes = estimateBase64Bytes(base64)
      imageBase64.value = base64
      perf.mark('toBase64', {
        base64Len: base64.length,
        payload: formatKb(payloadBytes)
      })

      const res = await recognizeFace(base64)
      perf.mark('recognizeApi', {
        studentId: res?.data?.studentId,
        score: res?.data?.score
      })

      await validatePunch(res.data.studentId, mode.value)
      perf.mark('validatePunch')

      matchResult.value = res.data
      if (mode.value === 'checkin') {
        await loadFeeOptions(res.data.studentId)
        perf.mark('loadFeeOptions', { count: feeOptions.value.length })
      }
      tips.value = '识别成功，请确认'
      perf.done({ ok: true })
    } catch (e) {
      perf.done({ ok: false, error: e?.message || String(e) })
      if (e.message && !e.message.includes('cancel')) {
        uni.showToast({ title: e.message || '识别失败', icon: 'none', duration: 3000 })
      }
      tips.value = '识别失败，请重拍'
      matchResult.value = null
    } finally {
      processing.value = false
    }
  }

  onLoad(async (query) => {
    mode.value = query.mode === 'checkout' ? 'checkout' : 'checkin'
    const path = query.path ? decodeURIComponent(query.path) : ''
    if (!path) {
      uni.showToast({ title: '缺少照片', icon: 'none' })
      setTimeout(() => uni.navigateBack(), 600)
      return
    }
    await recognizeFromPath(path)
  })

  async function resetCapture() {
    if (processing.value || submitting.value || openingCamera) return
    matchResult.value = null
    feeOptions.value = []
    selectedFeeId.value = null

    const granted = await requestCameraPermission()
    if (!granted) {
      uni.showToast({ title: '未获得相机权限', icon: 'none' })
      return
    }

    openingCamera = true
    try {
      const path = await openCameraCapture()
      await recognizeFromPath(path)
    } catch (e) {
      if (e.message === CAMERA_CAPTURE_CANCELLED) return
      uni.showToast({ title: e.message || '拍照失败', icon: 'none' })
    } finally {
      openingCamera = false
    }
  }

  async function confirmSubmit() {
    if (!matchResult.value || submitting.value) return
    if (mode.value === 'checkin' && feeOptions.value.length > 1 && !selectedFeeId.value) {
      uni.showToast({ title: '请选择签到科目', icon: 'none' })
      return
    }
    submitting.value = true
    try {
      const payload = {
        studentId: matchResult.value.studentId,
        imageBase64: imageBase64.value
      }
      if (mode.value === 'checkin' && selectedFeeId.value) {
        payload.feeId = selectedFeeId.value
      }
      if (mode.value === 'checkin') await submitCheckin(payload)
      else await submitCheckout(payload)
      uni.showToast({ title: successText.value, icon: 'success' })
      setTimeout(() => uni.navigateBack(), 800)
    } catch (e) {
      uni.showToast({ title: e.message || '提交失败', icon: 'none' })
    } finally {
      submitting.value = false
    }
  }

  return {
    tips,
    previewPath,
    processing,
    submitting,
    matchResult,
    pageTitle,
    submitText,
    hasPreview,
    isCheckin,
    feeOptions,
    selectedFeeId,
    feeLoading,
    goBack,
    resetCapture,
    confirmSubmit
  }
}
