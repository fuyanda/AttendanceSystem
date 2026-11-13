import { requestCameraPermission } from '@/shared/utils/camera-permission.js'
import { CAMERA_CAPTURE_CANCELLED, openCameraCapture } from '@/shared/utils/camera-capture.js'

/**
 * �ڵ�ǰҳֱ�Ӵ������ȡ����ʧ�ܷ��� null���ɹ�������ʱ��Ƭ·����
 */
export async function capturePhotoAtEntry() {
  const granted = await requestCameraPermission()
  if (!granted) {
    uni.showToast({ title: 'δ������Ȩ��', icon: 'none' })
    return null
  }
  try {
    return await openCameraCapture()
  } catch (e) {
    if (e.message !== CAMERA_CAPTURE_CANCELLED) {
      uni.showToast({ title: e.message || '����ʧ��', icon: 'none' })
    }
    return null
  }
}

export function buildPathQuery(path) {
  return `path=${encodeURIComponent(path)}`
}
