const TOKEN_KEY = 'token'
const USER_INFO_KEY = 'userInfo'
const USERNAME_KEY = 'userName'
const PASSWORD_KEY = 'password'
const REMEMBER_KEY = 'rememberMe'

export function getToken() {
  return uni.getStorageSync(TOKEN_KEY) || ''
}

export function setToken(token) {
  uni.setStorageSync(TOKEN_KEY, token || '')
}

export function getUserInfo() {
  const raw = uni.getStorageSync(USER_INFO_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch (e) {
    return null
  }
}

export function setUserInfo(user) {
  uni.setStorageSync(USER_INFO_KEY, JSON.stringify(user || {}))
}

export function saveLoginForm({ username, password, rememberMe }) {
  if (rememberMe) {
    uni.setStorageSync(USERNAME_KEY, username)
    uni.setStorageSync(PASSWORD_KEY, password)
    uni.setStorageSync(REMEMBER_KEY, true)
  } else {
    uni.removeStorageSync(PASSWORD_KEY)
    uni.setStorageSync(REMEMBER_KEY, false)
  }
}

export function getSavedLoginForm() {
  return {
    username: uni.getStorageSync(USERNAME_KEY) || '',
    password: uni.getStorageSync(PASSWORD_KEY) || '',
    rememberMe: !!uni.getStorageSync(REMEMBER_KEY)
  }
}

export function clearAuth() {
  uni.removeStorageSync(TOKEN_KEY)
  uni.removeStorageSync(USER_INFO_KEY)
}
