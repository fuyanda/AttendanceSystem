/**
 * Lightweight timing helper for face recognize pipeline.
 * Logs are prefixed with [PERF] so they are easy to filter in console / logcat.
 */
export function createPerf(tag) {
  const startedAt = Date.now()
  let lastAt = startedAt
  const steps = []

  function mark(name, extra) {
    const now = Date.now()
    const stepMs = now - lastAt
    const totalMs = now - startedAt
    lastAt = now
    const item = { name, stepMs, totalMs, ...(extra || {}) }
    steps.push(item)
    const extraText = extra ? ` ${JSON.stringify(extra)}` : ''
    console.log(`[PERF][${tag}] ${name}: +${stepMs}ms (total ${totalMs}ms)${extraText}`)
    return item
  }

  function done(extra) {
    const totalMs = Date.now() - startedAt
    const summary = steps.map((s) => `${s.name}=${s.stepMs}ms`).join(', ')
    const extraText = extra ? ` ${JSON.stringify(extra)}` : ''
    console.log(`[PERF][${tag}] DONE total=${totalMs}ms | ${summary}${extraText}`)
    return { totalMs, steps }
  }

  return { mark, done, startedAt }
}

export function estimateBase64Bytes(base64) {
  if (!base64) return 0
  const len = String(base64).length
  const padding = base64.endsWith('==') ? 2 : base64.endsWith('=') ? 1 : 0
  return Math.max(0, Math.floor((len * 3) / 4) - padding)
}

export function formatKb(bytes) {
  return `${(bytes / 1024).toFixed(1)}KB`
}

export function getFileSize(filePath) {
  return new Promise((resolve) => {
    if (!filePath || typeof uni === 'undefined' || !uni.getFileInfo) {
      resolve(0)
      return
    }
    let path = String(filePath)
    // _doc / relative paths can trip App view-layer; convert when possible
    if (
      typeof plus !== 'undefined' &&
      plus.io &&
      plus.io.convertLocalFileSystemURL &&
      !path.startsWith('/') &&
      !path.startsWith('file://') &&
      !path.startsWith('http')
    ) {
      try {
        path = plus.io.convertLocalFileSystemURL(path) || path
      } catch (e) {
        /* keep original */
      }
    }
    uni.getFileInfo({
      filePath: path,
      success: (res) => resolve(Number(res.size) || 0),
      fail: () => resolve(0)
    })
  })
}
