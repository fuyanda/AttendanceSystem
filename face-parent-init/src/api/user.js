import request from './request'

/** 微信服务号首次/追加绑定（需携带微信 OAuth code） */
export function bindByWechat(data) {
  return request({
    url: '/face/user/bind',
    method: 'put',
    data
  })
}

/** 获取微信服务号 appId（用于前端 OAuth 跳转） */
export function getWxConfig() {
  return request({
    url: '/face/user/wx-config',
    method: 'get'
  })
}

/** 进入页面时用 OAuth code 换取 openId */
export function exchangeWxOpenId(code) {
  return request({
    url: '/face/user/wx-openid',
    method: 'get',
    params: { code }
  })
}

/** 已注册家长追加绑定其他孩子 */
export function bindStudentByCode(data) {
  return request({
    url: '/face/user/bind-student',
    method: 'post',
    data
  })
}

/** 扫码绑定预览 */
export function fetchBindPreview(bindCode) {
  return request({
    url: '/face/user/bind-preview',
    method: 'get',
    params: { bindCode }
  })
}

/** 按 openId 查询是否已有家长账号 */
export function lookupByOpenId(openId) {
  return request({
    url: '/face/user/by-openid',
    method: 'get',
    params: { openId }
  })
}

/** 扫码确认绑定 */
export function bindByQr(data) {
  return request({
    url: '/face/user/bind-qr',
    method: 'put',
    data
  })
}
