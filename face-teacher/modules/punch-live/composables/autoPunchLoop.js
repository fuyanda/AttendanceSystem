import { recognizeFace, validateCheckin, submitCheckin } from '@/modules/punch-live/api/index.js'
import { compressImageForFace, filePathToBase64 } from '@/shared/utils/image.js'
import { detectLocalFace } from '@/modules/punch-live/utils/localFaceDetect.js'

/** 无人脸时的扫描间隔 */
const IDLE_INTERVAL_MS = 1200
/** 已检出人脸/识别过程中的短间隔 */
const FAST_INTERVAL_MS = 400
/** 识别请求结束后的间隔 */
const AFTER_RECOGNIZE_MS = 1500
const DEFAULT_COOLDOWN_MS = 5000
/** 身份连续匹配次数：本地已检脸，身份确认保留 2 次降低误签 */
const DEFAULT_MATCH_STREAK = 2
const IDLE_HINT = '请将面部对准镜头'
const WAIT_FACE_HINT = '等待人脸…'
const BUSY_HINT = '识别中...'

/**
 * 常驻签到循环控制器（供 nvue Options API 使用，无 Vue 依赖）
 * Android：本地 ML Kit 检测到人脸后立即请求后台比对（不再额外等一轮确认）
 */
export function createAutoPunchLoop(options) {
  const cooldownMs = options.cooldownMs || DEFAULT_COOLDOWN_MS
  const matchStreak = options.matchStreak || DEFAULT_MATCH_STREAK

  let running = false
  let busy = false
  let timer = null
  let candidateId = null
  let streak = 0
  let degradeNoticed = false
  let nextDelayMs = IDLE_INTERVAL_MS
  /** @type {Map<string, number>} */
  const cooldownUntil = new Map()

  function clearTimer() {
    if (timer) {
      clearTimeout(timer)
      timer = null
    }
  }

  function scheduleNext(delay = nextDelayMs) {
    clearTimer()
    if (!running) return
    timer = setTimeout(() => {
      tick()
    }, delay)
  }

  function resetStreak() {
    candidateId = null
    streak = 0
  }

  function isCooling(studentId) {
    const key = String(studentId)
    const until = cooldownUntil.get(key) || 0
    return Date.now() < until
  }

  function markCooldown(studentId) {
    cooldownUntil.set(String(studentId), Date.now() + cooldownMs)
  }

  async function processPath(path) {
    const local = await detectLocalFace(path)
    if (local.error && !degradeNoticed) {
      degradeNoticed = true
      options.onNotice(local.error)
    }

    // 本地门禁未通过：不请求后台
    if (!local.skipped && !local.pass) {
      resetStreak()
      nextDelayMs = IDLE_INTERVAL_MS
      options.onStatus(local.hasFace ? '人脸过远或过小，请靠近' : WAIT_FACE_HINT)
      return
    }

    // 有脸（或非 Android 降级）→ 立刻后台识别
    options.onStatus(BUSY_HINT)
    nextDelayMs = FAST_INTERVAL_MS

    const compressedPath = await compressImageForFace(path, { maxSide: 720, quality: 80 })
    const base64 = await filePathToBase64(compressedPath)
    const res = await recognizeFace(base64)
    const data = res && res.data
    if (!data || data.studentId == null) {
      resetStreak()
      nextDelayMs = AFTER_RECOGNIZE_MS
      options.onStatus('未匹配到学生')
      return
    }

    const studentId = data.studentId
    const studentName = data.studentName || '学员'

    if (isCooling(studentId)) {
      resetStreak()
      nextDelayMs = AFTER_RECOGNIZE_MS
      options.onStatus(`${studentName} 冷却中`)
      return
    }

    if (candidateId != null && String(candidateId) === String(studentId)) {
      streak += 1
    } else {
      candidateId = studentId
      streak = 1
    }

    if (streak < matchStreak) {
      nextDelayMs = FAST_INTERVAL_MS
      options.onStatus(`识别到 ${studentName}，请保持`)
      return
    }

    try {
      await validateCheckin(studentId)
      await submitCheckin({ studentId, imageBase64: base64 })
    } catch (e) {
      markCooldown(studentId)
      resetStreak()
      nextDelayMs = AFTER_RECOGNIZE_MS
      throw e
    }
    markCooldown(studentId)
    resetStreak()
    nextDelayMs = AFTER_RECOGNIZE_MS
    options.onSuccess({
      studentId,
      studentName,
      score: data.score
    })
    options.onStatus(`${studentName} 签到成功`)
  }

  async function tick() {
    if (!running || busy) return
    busy = true
    try {
      const path = await options.snapshot()
      if (!running) return
      if (!path) {
        nextDelayMs = IDLE_INTERVAL_MS
        options.onStatus(IDLE_HINT)
        return
      }
      await processPath(path)
    } catch (e) {
      resetStreak()
      nextDelayMs = AFTER_RECOGNIZE_MS
      const msg = (e && e.message) || '识别失败'
      options.onNotice(msg)
      options.onStatus(msg)
    } finally {
      busy = false
      if (running) scheduleNext(nextDelayMs)
    }
  }

  function start() {
    if (running) return
    running = true
    nextDelayMs = IDLE_INTERVAL_MS
    options.onStatus(IDLE_HINT)
    scheduleNext(400)
  }

  function stop() {
    running = false
    busy = false
    clearTimer()
    resetStreak()
  }

  return {
    start,
    stop,
    get running() {
      return running
    }
  }
}
