import { getHost } from '@/shared/config/host.js'
import { clearAuth, getToken } from '@/shared/auth/storage.js'

const CODE_KEY = 'code'
const MSG_KEY = 'msg'
const DATA_KEY = 'data'
const LOGIN_PATH = '/pages/login/login'

function buildQuery(params = {}) {
  return Object.keys(params)
    .filter((key) => params[key] !== undefined && params[key] !== null && params[key] !== '')
    .map((key) => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
    .join('&')
}

export function request(options) {
  const {
    url,
    method = 'GET',
    data = {},
    params = {},
    header = {},
    needAuth = true,
    showError = true
  } = options

  const query = buildQuery(params)
  const fullUrl = `${getHost()}${url}${query ? `?${query}` : ''}`
  const token = getToken()

  const isFaceRecognize = url === '/face/recognize'
  const reqStart = isFaceRecognize ? Date.now() : 0
  if (isFaceRecognize) {
    const b64 = data && data.imageBase64 ? String(data.imageBase64) : ''
    const approxBytes = Math.floor((b64.length * 3) / 4)
    console.log(
      `[PERF][http] POST /face/recognize start, payload≈${(approxBytes / 1024).toFixed(1)}KB base64Len=${b64.length}`
    )
  }

  return new Promise((resolve, reject) => {
    uni.request({
      url: fullUrl,
      method,
      data,
      header: {
        'Content-Type': 'application/json;charset=utf-8',
        ...(needAuth && token ? { Authorization: `Bearer ${token}` } : {}),
        ...header
      },
      success(res) {
        if (isFaceRecognize) {
          console.log(`[PERF][http] POST /face/recognize response: ${Date.now() - reqStart}ms status=${res.statusCode}`)
        }
        let body = res.data
        if (typeof body === 'string') {
          try {
            body = JSON.parse(body)
          } catch (e) {
            reject(new Error('响应解析失败'))
            return
          }
        }
        const code = body[CODE_KEY]
        if (code === 401) {
          clearAuth()
          uni.reLaunch({ url: LOGIN_PATH })
          reject(new Error(body[MSG_KEY] || '登录已过期'))
          return
        }
        if (code !== 200) {
          const message = body[MSG_KEY] || '请求失败'
          if (showError) uni.showToast({ title: message, icon: 'none' })
          reject(new Error(message))
          return
        }
        resolve(body)
      },
      fail(err) {
        if (isFaceRecognize) {
          console.log(`[PERF][http] POST /face/recognize fail: ${Date.now() - reqStart}ms`, err)
        }
        const message = err.errMsg || '网络异常'
        if (showError) uni.showToast({ title: message, icon: 'none' })
        reject(new Error(message))
      }
    })
  })
}

export function get(url, params = {}, options = {}) {
  return request({ url, method: 'GET', params, ...options })
}

export function post(url, data = {}, options = {}) {
  return request({ url, method: 'POST', data, ...options })
}

export function put(url, data = {}, options = {}) {
  return request({ url, method: 'PUT', data, ...options })
}

export function del(url, options = {}) {
  return request({ url, method: 'DELETE', ...options })
}

export function getData(body) {
  return body[DATA_KEY]
}
