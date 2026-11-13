/**
 * Android 本地人脸检测门禁封装。
 * 非 Android / 插件未打入基座时 skipped=true，由调用方决定是否降级走后台识别。
 */

// #ifdef APP-PLUS
import { detectFromPath } from '@/uni_modules/cf-face-detect'
// #endif

const MIN_FACE_RATIO = 0.08

function normalizeLocalPath(filePath) {
  if (!filePath) return ''
  let path = String(filePath).trim()
  // #ifdef APP-PLUS
  try {
    if (typeof plus !== 'undefined' && plus.io && plus.io.convertLocalFileSystemURL) {
      const converted = plus.io.convertLocalFileSystemURL(path)
      if (converted) path = converted
    }
  } catch (e) {}
  // #endif
  if (path.startsWith('file://')) {
    path = path.slice(7)
  }
  // Android 偶发 //storage/...
  if (path.startsWith('//')) {
    path = path.slice(1)
  }
  return path
}

function isAndroidApp() {
  // #ifdef APP-PLUS
  try {
    return String((plus.os && plus.os.name) || '').toLowerCase() === 'android'
  } catch (e) {
    return false
  }
  // #endif
  // #ifndef APP-PLUS
  return false
  // #endif
}

/**
 * @param {string} imagePath
 * @param {{ minRatio?: number }} [options]
 * @returns {Promise<{
 *   skipped: boolean,
 *   hasFace: boolean,
 *   faceCount: number,
 *   largestAreaRatio: number,
 *   elapsedMs: number,
 *   pass: boolean,
 *   error?: string
 * }>}
 */
export function detectLocalFace(imagePath, options = {}) {
  const minRatio = options.minRatio != null ? options.minRatio : MIN_FACE_RATIO

  if (!isAndroidApp()) {
    return Promise.resolve({
      skipped: true,
      hasFace: true,
      faceCount: 0,
      largestAreaRatio: 1,
      elapsedMs: 0,
      pass: true
    })
  }

  // #ifndef APP-PLUS
  return Promise.resolve({
    skipped: true,
    hasFace: true,
    faceCount: 0,
    largestAreaRatio: 1,
    elapsedMs: 0,
    pass: true
  })
  // #endif

  // #ifdef APP-PLUS
  const absPath = normalizeLocalPath(imagePath)
  if (typeof detectFromPath !== 'function') {
    return Promise.resolve({
      skipped: true,
      hasFace: true,
      faceCount: 0,
      largestAreaRatio: 1,
      elapsedMs: 0,
      pass: true,
      error: '人脸检测插件未加载，请使用自定义基座打包'
    })
  }

  return detectFromPath(absPath)
    .then((res) => {
      const hasFace = !!res.hasFace
      const faceCount = Number(res.faceCount || 0)
      const largestAreaRatio = Number(res.largestAreaRatio || 0)
      const elapsedMs = Number(res.elapsedMs || 0)
      const pass = hasFace && largestAreaRatio >= minRatio
      console.log(
        `[localFaceDetect] path=${absPath} hasFace=${hasFace} count=${faceCount} ratio=${largestAreaRatio.toFixed(3)} pass=${pass} ${elapsedMs}ms`
      )
      return {
        skipped: false,
        hasFace,
        faceCount,
        largestAreaRatio,
        elapsedMs,
        pass
      }
    })
    .catch((err) => {
      const message = (err && err.message) || String(err)
      console.warn('[localFaceDetect] degrade:', message, 'path=', absPath)
      return {
        skipped: true,
        hasFace: true,
        faceCount: 0,
        largestAreaRatio: 1,
        elapsedMs: 0,
        pass: true,
        error: message
      }
    })
  // #endif
}

export const LOCAL_FACE_MIN_RATIO = MIN_FACE_RATIO
