export function filePathToBase64(filePath) {
  if (!filePath) {
    return Promise.reject(new Error('图片路径为空'))
  }

  const startedAt = Date.now()
  const wrap = (promise, engine) =>
    promise
      .then((base64) => {
        console.log(
          `[PERF][toBase64] engine=${engine} cost=${Date.now() - startedAt}ms len=${String(base64 || '').length}`
        )
        return base64
      })
      .catch((err) => {
        console.log(`[PERF][toBase64] engine=${engine} fail cost=${Date.now() - startedAt}ms`, err)
        return Promise.reject(err)
      })

  // #ifdef MP-WEIXIN
  return wrap(readBase64ByFileSystemManager(filePath), 'mp-fs')
  // #endif

  // #ifdef APP-PLUS
  return wrap(readBase64ByPlusIo(filePath), 'app-plus-io')
  // #endif

  // #ifdef H5
  return wrap(readBase64ByFetch(filePath), 'h5-fetch')
  // #endif

  return Promise.reject(new Error('当前环境不支持读取图片'))
}

function readBase64ByFileSystemManager(filePath) {
  return new Promise((resolve, reject) => {
    uni.getFileSystemManager().readFile({
      filePath,
      encoding: 'base64',
      success(res) {
        resolve(res.data)
      },
      fail(err) {
        reject(new Error(err.errMsg || '读取图片失败'))
      }
    })
  })
}

function readBase64ByPlusIo(filePath) {
  return new Promise((resolve, reject) => {
    if (typeof plus === 'undefined') {
      reject(new Error('App 环境未就绪'))
      return
    }

    const localPath = normalizeAppPath(filePath)

    plus.io.resolveLocalFileSystemURL(
      localPath,
      (entry) => {
        entry.file(
          (file) => {
            const reader = new plus.io.FileReader()
            reader.onloadend = (evt) => {
              const result = evt.target?.result || ''
              const base64 = String(result).split(',')[1] || ''
              if (base64) resolve(base64)
              else reject(new Error('读取图片失败'))
            }
            reader.onerror = () => reject(new Error('读取图片失败'))
            reader.readAsDataURL(file)
          },
          () => readBase64ByBitmap(localPath).then(resolve).catch(reject)
        )
      },
      () => readBase64ByBitmap(localPath).then(resolve).catch(reject)
    )
  })
}

function readBase64ByBitmap(filePath) {
  return new Promise((resolve, reject) => {
    const bitmapId = `img_${Date.now()}`
    const bitmap = new plus.nativeObj.Bitmap(bitmapId)
    bitmap.load(
      filePath,
      () => {
        try {
          const dataUrl = bitmap.toBase64Data()
          bitmap.clear()
          const base64 = String(dataUrl).replace(/^data:image\/\w+;base64,/, '')
          if (base64) resolve(base64)
          else reject(new Error('读取图片失败'))
        } catch (e) {
          bitmap.clear()
          reject(new Error('读取图片失败'))
        }
      },
      (e) => {
        bitmap.clear()
        reject(new Error(e.message || '读取图片失败'))
      }
    )
  })
}

function toAbsoluteLocalPath(filePath) {
  if (!filePath) return filePath
  const path = String(filePath)
  if (
    path.startsWith('/') ||
    path.startsWith('file://') ||
    path.startsWith('http://') ||
    path.startsWith('https://') ||
    path.startsWith('/storage') ||
    path.startsWith('/data')
  ) {
    return path
  }
  if (typeof plus !== 'undefined' && plus.io && plus.io.convertLocalFileSystemURL) {
    try {
      return plus.io.convertLocalFileSystemURL(path) || path
    } catch (e) {
      return path
    }
  }
  return path
}

function normalizeAppPath(filePath) {
  if (
    filePath.startsWith('http://') ||
    filePath.startsWith('https://') ||
    filePath.startsWith('file://') ||
    filePath.startsWith('/storage') ||
    filePath.startsWith('/data')
  ) {
    return filePath
  }
  if (plus.io.convertLocalFileSystemURL) {
    return plus.io.convertLocalFileSystemURL(filePath)
  }
  return filePath
}

function readBase64ByFetch(filePath) {
  return fetch(filePath)
    .then((response) => response.blob())
    .then(
      (blob) =>
        new Promise((resolve, reject) => {
          const reader = new FileReader()
          reader.onloadend = () => {
            const result = reader.result || ''
            resolve(String(result).split(',')[1] || '')
          }
          reader.onerror = () => reject(new Error('读取图片失败'))
          reader.readAsDataURL(blob)
        })
    )
    .catch(() => Promise.reject(new Error('读取图片失败')))
}


/** Face recognize: long side <= maxSide, JPEG quality. Returns compressed local path (or original on failure). */
export function compressImageForFace(filePath, options = {}) {
  const maxSide = options.maxSide || 720
  const quality = options.quality || 80
  if (!filePath) {
    return Promise.reject(new Error('图片路径为空'))
  }

  const startedAt = Date.now()
  return getImageSize(filePath)
    .then(({ width, height }) => {
      const longSide = Math.max(width, height)
      let destW = width
      let destH = height
      if (longSide > maxSide && longSide > 0) {
        const scale = maxSide / longSide
        destW = Math.max(1, Math.round(width * scale))
        destH = Math.max(1, Math.round(height * scale))
      }
      return compressWithUni(filePath, destW, destH, quality)
        .catch(() => compressWithBitmap(filePath, destW, destH, quality))
        .catch(() => filePath)
        .then((outPath) => {
          const abs = toAbsoluteLocalPath(outPath)
          console.log(
            `[PERF][compress] ${width}x${height} -> ${destW}x${destH} cost=${Date.now() - startedAt}ms out=${abs}`
          )
          return abs
        })
    })
    .catch((err) => {
      console.log(`[PERF][compress] skip cost=${Date.now() - startedAt}ms`, err)
      return filePath
    })
}

function getImageSize(filePath) {
  return new Promise((resolve, reject) => {
    uni.getImageInfo({
      src: filePath,
      success: (res) => resolve({ width: res.width || 0, height: res.height || 0 }),
      fail: (err) => reject(new Error(err.errMsg || '读取图片尺寸失败'))
    })
  })
}

function compressWithUni(filePath, width, height, quality) {
  return new Promise((resolve, reject) => {
    if (typeof uni.compressImage !== 'function') {
      reject(new Error('uni.compressImage unavailable'))
      return
    }
    uni.compressImage({
      src: filePath,
      quality,
      compressedWidth: width,
      compressedHeight: height,
      success: (res) => {
        const out = res.tempFilePath || res.compressPath || ''
        if (out) resolve(toAbsoluteLocalPath(out))
        else reject(new Error('compressImage empty path'))
      },
      fail: (err) => reject(new Error(err.errMsg || 'compressImage fail'))
    })
  })
}

function compressWithBitmap(filePath, width, height, quality) {
  return new Promise((resolve, reject) => {
    if (typeof plus === 'undefined' || !plus.nativeObj || !plus.nativeObj.Bitmap) {
      reject(new Error('Bitmap unavailable'))
      return
    }
    const localPath = normalizeAppPath(filePath)
    const bitmapId = 'face_c_' + Date.now()
    const bitmap = new plus.nativeObj.Bitmap(bitmapId)
    bitmap.load(
      localPath,
      () => {
        const dst = '_doc/face_compress_' + Date.now() + '.jpg'
        bitmap.save(
          dst,
          {
            overwrite: true,
            quality,
            width,
            height,
            format: 'jpg'
          },
          () => {
            bitmap.clear()
            const out = plus.io.convertLocalFileSystemURL
              ? plus.io.convertLocalFileSystemURL(dst)
              : dst
            resolve(out)
          },
          (e) => {
            bitmap.clear()
            reject(new Error((e && e.message) || 'bitmap save fail'))
          }
        )
      },
      (e) => {
        bitmap.clear()
        reject(new Error((e && e.message) || 'bitmap load fail'))
      }
    )
  })
}
