import { chooseCameraImage } from '@/shared/utils/camera-permission.js'
import { supportsLiveCameraPreview } from '@/shared/utils/platform.js'
import { getPreferredCameraPosition } from '@/shared/utils/camera-preference.js'

const CAPTURE_PAGE = '/pages/camera/capture'
export const CAMERA_CAPTURE_CANCELLED = 'CAMERA_CAPTURE_CANCELLED'

/**
 * 打开应用内拍照页（live-pusher 实时预览 + 取景框），返回拍摄照片的临时路径。
 * 若用户曾保留摄像头设置，则按保存的前置/后置打开。
 */
export function openCameraCapture() {
  return new Promise((resolve, reject) => {
    if (!supportsLiveCameraPreview()) {
      chooseCameraImage().then(resolve).catch(reject)
      return
    }
    const preferred = getPreferredCameraPosition()
    const url = preferred === 'back' || preferred === 'front'
      ? `${CAPTURE_PAGE}?device=${preferred}`
      : CAPTURE_PAGE
    uni.navigateTo({
      url,
      events: {
        onPhotoCaptured(path) {
          if (path) resolve(path)
          else reject(new Error(CAMERA_CAPTURE_CANCELLED))
        }
      },
      fail(err) {
        reject(new Error(err.errMsg || '无法打开拍照页'))
      }
    })
  })
}
