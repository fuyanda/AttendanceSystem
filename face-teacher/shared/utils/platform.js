/**
 * 平台判断工具
 */
export function isAndroidApp() {
  // #ifdef APP-PLUS
  return typeof plus !== 'undefined' && plus.os.name === 'Android'
  // #endif
  return false
}

/** App / 小程序使用应用内 live-pusher 拍照页；H5 走系统选图 */
export function supportsLiveCameraPreview() {
  // #ifdef H5
  return false
  // #endif
  // #ifdef APP-PLUS
  return typeof plus !== 'undefined'
  // #endif
  // #ifdef MP-WEIXIN
  return true
  // #endif
  return false
}
