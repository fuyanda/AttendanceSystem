/**
 * App 端语音播报：优先系统 TTS（Android TextToSpeech / iOS AVSpeech），再尝试 plus.speech。
 * 需真机 App 环境；部分机型需安装「文字转语音」引擎与中文语音包。
 */

/** @type {any} */
let androidTts = null
/** @type {Promise<any>|null} */
let androidTtsInit = null
/** @type {any} */
let iosSynthesizer = null

function isAppPlus() {
  // #ifdef APP-PLUS
  return typeof plus !== 'undefined'
  // #endif
  // #ifndef APP-PLUS
  return false
  // #endif
}

function getOsName() {
  try {
    return (plus.os && plus.os.name) || ''
  } catch (e) {
    return ''
  }
}

function ensureAndroidTts() {
  if (androidTtsInit) return androidTtsInit
  androidTtsInit = new Promise((resolve, reject) => {
    try {
      const main = plus.android.runtimeMainActivity()
      const TextToSpeech = plus.android.importClass('android.speech.tts.TextToSpeech')
      const Locale = plus.android.importClass('java.util.Locale')
      const listener = plus.android.implements('android.speech.tts.TextToSpeech$OnInitListener', {
        onInit(status) {
          // SUCCESS === 0
          if (status !== 0) {
            androidTts = null
            androidTtsInit = null
            reject(new Error('系统 TTS 初始化失败，请检查是否安装文字转语音引擎'))
            return
          }
          try {
            let langResult = -1
            try {
              langResult = androidTts.setLanguage(Locale.SIMPLIFIED_CHINESE)
            } catch (e1) {
              try {
                langResult = androidTts.setLanguage(Locale.CHINESE)
              } catch (e2) {
                langResult = -1
              }
            }
            // LANG_MISSING_DATA=-1, LANG_NOT_SUPPORTED=-2
            if (langResult === -1 || langResult === -2) {
              reject(new Error('未安装中文语音包，请到系统设置安装 TTS 中文数据'))
              return
            }
            resolve(androidTts)
          } catch (e) {
            androidTtsInit = null
            reject(new Error(e.message || '系统 TTS 语言设置失败'))
          }
        }
      })
      androidTts = new TextToSpeech(main, listener)
    } catch (e) {
      androidTtsInit = null
      reject(new Error(e.message || '无法创建系统 TTS'))
    }
  })
  return androidTtsInit
}

function speakAndroid(text) {
  return ensureAndroidTts().then((tts) => {
    try {
      tts.stop()
    } catch (e) {}
    // QUEUE_FLUSH = 0；API21+ 需要 utteranceId
    let code = -1
    try {
      code = tts.speak(text, 0, null, 'cf_tts_' + Date.now())
    } catch (e1) {
      code = tts.speak(text, 0, null)
    }
    // ERROR = -1
    if (code === -1) {
      throw new Error('系统 TTS 播报失败')
    }
    return { engine: 'android-tts' }
  })
}

function speakIos(text) {
  return new Promise((resolve, reject) => {
    try {
      const AVSpeechSynthesizer = plus.ios.importClass('AVSpeechSynthesizer')
      const AVSpeechUtterance = plus.ios.importClass('AVSpeechUtterance')
      const AVSpeechSynthesisVoice = plus.ios.importClass('AVSpeechSynthesisVoice')
      if (!iosSynthesizer) {
        iosSynthesizer = new AVSpeechSynthesizer()
      }
      if (iosSynthesizer.speaking && iosSynthesizer.stopSpeakingAtBoundary) {
        iosSynthesizer.stopSpeakingAtBoundary(0)
      }
      const utterance = plus.ios.invoke(AVSpeechUtterance, 'speechUtteranceWithString:', text)
      const voice = plus.ios.invoke(AVSpeechSynthesisVoice, 'voiceWithLanguage:', 'zh-CN')
      if (voice) {
        plus.ios.invoke(utterance, 'setVoice:', voice)
      }
      plus.ios.invoke(utterance, 'setRate:', 0.52)
      plus.ios.invoke(iosSynthesizer, 'speakUtterance:', utterance)
      resolve({ engine: 'ios-avspeech' })
    } catch (e) {
      reject(new Error(e.message || 'iOS 语音播报失败'))
    }
  })
}

function speakPlusSpeech(text) {
  return new Promise((resolve, reject) => {
    if (!plus.speech || typeof plus.speech.startSpeaking !== 'function') {
      reject(new Error('未启用 Speech 模块（请自定义基座/云打包勾选语音）'))
      return
    }
    try {
      plus.speech.stopSpeaking()
    } catch (e) {}
    let settled = false
    const done = (result) => {
      if (settled) return
      settled = true
      resolve(result)
    }
    const fail = (err) => {
      if (settled) return
      settled = true
      reject(err instanceof Error ? err : new Error(String(err)))
    }
    try {
      plus.speech.startSpeaking(String(text), {
        engine: 'local',
        lang: 'zh-cn',
        success() {
          done({ engine: 'plus-speech' })
        },
        fail(err) {
          fail(new Error((err && err.message) || 'plus.speech 播报失败'))
        }
      })
      // 部分运行时无 success 回调，短延迟后视为已发起
      setTimeout(() => done({ engine: 'plus-speech' }), 400)
    } catch (e) {
      fail(new Error(e.message || 'plus.speech 不可用'))
    }
  })
}

/**
 * @param {string} text
 * @returns {Promise<{ engine: string }>}
 */
export function speakText(text) {
  const content = String(text || '').trim()
  if (!content) {
    return Promise.reject(new Error('播报内容为空'))
  }
  if (!isAppPlus()) {
    return Promise.reject(new Error('语音播报仅支持 App'))
  }

  const os = getOsName().toLowerCase()
  const chain =
    os.indexOf('android') >= 0
      ? [() => speakAndroid(content), () => speakPlusSpeech(content)]
      : os.indexOf('ios') >= 0
        ? [() => speakIos(content), () => speakPlusSpeech(content)]
        : [() => speakPlusSpeech(content), () => speakAndroid(content)]

  let lastError = new Error('语音播报失败')
  const tryNext = (index) => {
    if (index >= chain.length) {
      return Promise.reject(lastError)
    }
    return chain[index]().catch((err) => {
      lastError = err instanceof Error ? err : new Error(String(err))
      return tryNext(index + 1)
    })
  }
  return tryNext(0)
}

/**
 * @param {string} studentName
 */
export function speakCheckinSuccess(studentName) {
  const name = String(studentName || '').trim() || '学员'
  return speakText(`${name}签到成功`).catch(() => null)
}

/**
 * 设置页自检：播报固定文案并返回可读结果
 * @returns {Promise<{ ok: boolean, message: string, engine?: string }>}
 */
export function testSpeech() {
  const sample = '张三签到成功'
  return speakText(sample)
    .then((res) => ({
      ok: true,
      engine: res.engine,
      message: `已请求播报「${sample}」（引擎：${res.engine}），请调大媒体音量收听`
    }))
    .catch((err) => ({
      ok: false,
      message: (err && err.message) || '语音测试失败'
    }))
}

export function stopSpeaking() {
  if (!isAppPlus()) return
  try {
    if (plus.speech && plus.speech.stopSpeaking) {
      plus.speech.stopSpeaking()
    }
  } catch (e) {}
  try {
    if (androidTts) {
      androidTts.stop()
    }
  } catch (e) {}
  try {
    if (iosSynthesizer && iosSynthesizer.speaking && iosSynthesizer.stopSpeakingAtBoundary) {
      iosSynthesizer.stopSpeakingAtBoundary(0)
    }
  } catch (e) {}
}
