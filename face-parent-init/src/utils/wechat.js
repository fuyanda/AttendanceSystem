/** 是否在微信内置浏览器 */
export function isWeChatBrowser() {
  return /micromessenger/i.test(navigator.userAgent || '')
}

/** 构建微信网页授权 URL（snsapi_base 静默授权） */
export function buildWxOAuthUrl(appId, redirectUri, state = 'bind') {
  const uri = encodeURIComponent(redirectUri)
  return (
    'https://open.weixin.qq.com/connect/oauth2/authorize' +
    `?appid=${appId}&redirect_uri=${uri}&response_type=code&scope=snsapi_base&state=${state}#wechat_redirect`
  )
}

/** 授权回调后去掉 URL 中的 code，避免刷新重复兑换 */
export function cleanWxCodeFromUrl(router) {
  const query = { ...router.currentRoute.value.query }
  if (!query.code && !query.state) return
  delete query.code
  delete query.state
  router.replace({ path: router.currentRoute.value.path, query })
}

/** OAuth redirect_uri; same as service account menu entry (/) */
export function getBindPageUrl() {
  const base = import.meta.env.BASE_URL || '/'
  const path = base.endsWith('/') ? base : `${base}/`
  return `${window.location.origin}${path}`
}

/** 扫码绑定页 OAuth 回调地址（保留绑定码参数 c） */
export function getQrBindPageUrl(bindCode) {
  const base = import.meta.env.BASE_URL || '/'
  const path = base.endsWith('/') ? `${base}qr-bind` : `${base}/qr-bind`
  const code = encodeURIComponent(bindCode || '')
  return `${window.location.origin}${path}?c=${code}`
}
