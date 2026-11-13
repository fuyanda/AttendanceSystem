/** RuoYi AjaxResult: code may be number or string */
export function isAjaxSuccess(body) {
  return body != null && Number(body.code) === 200
}

export function unwrapAjaxBody(axiosResponse) {
  return axiosResponse?.data ?? {}
}

/** Parse appId from wx-config API */
export function pickAppId(body) {
  if (!isAjaxSuccess(body)) return ''
  const payload = body.data
  if (payload && typeof payload === 'object' && payload.appId) {
    return String(payload.appId).trim()
  }
  return ''
}

/** Deep-search nested API response for WeChat openId */
export function pickOpenIdDeep(raw) {
  const visited = new Set()

  function walk(node) {
    if (node == null || visited.has(node)) return ''
    if (typeof node === 'string') {
      const value = node.trim()
      return value.startsWith('o') && value.length >= 20 ? value : ''
    }
    if (typeof node !== 'object') return ''
    visited.add(node)
    const list = Array.isArray(node) ? node : Object.values(node)
    for (const item of list) {
      const found = walk(item)
      if (found) return found
    }
    return ''
  }

  return walk(raw)
}

/** Accept axios response or unwrapped AjaxResult */
export function normalizeAjaxBody(maybeResponse) {
  if (maybeResponse?.data && typeof maybeResponse.data === 'object' && 'code' in maybeResponse.data) {
    return maybeResponse.data
  }
  if (maybeResponse && typeof maybeResponse === 'object' && 'code' in maybeResponse) {
    return maybeResponse
  }
  return maybeResponse?.data ?? maybeResponse ?? {}
}

/** Parse openId from wx-openid and similar APIs */
export function pickOpenId(body) {
  if (!isAjaxSuccess(body)) return ''
  const payload = body.data
  if (typeof payload === 'string' && payload.trim()) {
    return payload.trim()
  }
  if (payload && typeof payload === 'object') {
    const nested = payload.openId || payload.openid || ''
    return typeof nested === 'string' ? nested.trim() : ''
  }
  return ''
}
