import { getHost } from '@/shared/config/host.js'
import { get, post } from '@/shared/http/request.js'

export function fetchCaptcha() {
  const timestamp = Date.now()
  return new Promise((resolve, reject) => {
    uni.request({
      url: `${getHost()}/captchaImage?t=${timestamp}`,
      method: 'GET',
      success(res) { resolve(res.data) },
      fail(err) { reject(new Error(err.errMsg || '验证码加载失败')) }
    })
  })
}

export function login(data) {
  return post('/login', data, { needAuth: false })
}

export function getInfo() {
  return get('/getInfo')
}

export function logout() {
  return post('/logout')
}
