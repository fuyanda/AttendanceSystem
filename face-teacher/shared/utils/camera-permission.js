/**
 * 请求相机权限（Android 等待 plusready 后调用 requestPermissions）
 * @returns {Promise<boolean>}
 */
export function requestCameraPermission() {
  return waitPlusReady().then(() => {
    // #ifdef APP-PLUS
    if (typeof plus === 'undefined') {
      return false
    }
    if (plus.os.name === 'Android') {
      return requestAndroidCameraPermission()
    }
    if (plus.os.name === 'iOS') {
      return checkIOSCameraPermission()
    }
    return true
    // #endif

    // #ifdef MP-WEIXIN
    return requestWeixinCameraPermission()
    // #endif

    // #ifdef H5
    return true
    // #endif
  })
}

function waitPlusReady() {
  return new Promise((resolve) => {
    // #ifdef APP-PLUS
    if (typeof plus !== 'undefined') {
      resolve()
      return
    }
    const onReady = () => resolve()
    document.addEventListener('plusready', onReady, { once: true })
    setTimeout(onReady, 5000)
    // #endif
    // #ifndef APP-PLUS
    resolve()
    // #endif
  })
}

function requestAndroidCameraPermission() {
  return new Promise((resolve) => {
    try {
      const main = plus.android.runtimeMainActivity()
      const ContextCompat = plus.android.importClass('androidx.core.content.ContextCompat')
      const PackageManager = plus.android.importClass('android.content.pm.PackageManager')
      const permission = 'android.permission.CAMERA'
      const alreadyGranted =
        ContextCompat.checkSelfPermission(main, permission) === PackageManager.PERMISSION_GRANTED
      if (alreadyGranted) {
        resolve(true)
        return
      }
    } catch (e) {
      // 部分基座缺少 androidx，继续走 requestPermissions
    }

    plus.android.requestPermissions(
      ['android.permission.CAMERA'],
      (result) => {
        const granted = !!(result.granted && result.granted.length)
        if (!granted) {
          showCameraDeniedModal()
        }
        resolve(granted)
      },
      () => {
        uni.showToast({ title: '相机权限申请失败', icon: 'none' })
        resolve(false)
      }
    )
  })
}

function requestWeixinCameraPermission() {
  return new Promise((resolve) => {
    uni.getSetting({
      success(setting) {
        if (setting.authSetting['scope.camera']) {
          resolve(true)
          return
        }
        uni.authorize({
          scope: 'scope.camera',
          success: () => resolve(true),
          fail: () => {
            showCameraDeniedModal('请在设置中允许使用相机')
            resolve(false)
          }
        })
      },
      fail: () => resolve(false)
    })
  })
}

function showCameraDeniedModal(content) {
  uni.showModal({
    title: '需要相机权限',
    content: content || '人脸采集需要使用摄像头，请在系统设置中开启相机权限',
    confirmText: '去设置',
    cancelText: '取消',
    success(res) {
      if (!res.confirm) return
      // #ifdef APP-PLUS
      if (plus.runtime.openSettings) {
        plus.runtime.openSettings()
      }
      // #endif
      // #ifdef MP-WEIXIN
      uni.openSetting()
      // #endif
    }
  })
}

function checkIOSCameraPermission() {
  try {
    const AVCaptureDevice = plus.ios.importClass('AVCaptureDevice')
    const status = AVCaptureDevice.authorizationStatusForMediaType('vide')
    plus.ios.deleteObject(AVCaptureDevice)
    if (status === 3 || status === 0) {
      return true
    }
    showCameraDeniedModal()
    return false
  } catch (e) {
    return true
  }
}

export function chooseCameraImage() {
  return new Promise((resolve, reject) => {
    const t0 = Date.now()
    console.log('[PERF][camera] chooseImage start sizeType=compressed')
    uni.chooseImage({
      count: 1,
      sourceType: ['camera'],
      sizeType: ['compressed'],
      success(res) {
        const path = res.tempFilePaths && res.tempFilePaths[0]
        console.log('[PERF][camera] chooseImage done: ' + (Date.now() - t0) + 'ms path=' + (path || ''))
        if (path && uni.getFileInfo) {
          uni.getFileInfo({
            filePath: path,
            success: (info) => {
              console.log('[PERF][camera] chooseImage size=' + ((info.size || 0) / 1024).toFixed(1) + 'KB')
            }
          })
        }
        if (path) resolve(path)
        else reject(new Error('未获取到照片'))
      },
      fail(err) {
        console.log('[PERF][camera] chooseImage fail: ' + (Date.now() - t0) + 'ms', err)
        reject(new Error(err.errMsg || '拍照失败'))
      }
    })
  })
}
