import { computed, reactive } from 'vue'
import {
  clearAuth,
  getToken,
  getUserInfo,
  setToken,
  setUserInfo
} from '@/shared/auth/storage.js'

const state = reactive({
  token: getToken(),
  user: getUserInfo()
})

export function useAuthStore() {
  const isLoggedIn = computed(() => !!state.token)
  const deptName = computed(() => state.user?.dept?.deptName || '未获取校区')

  function hydrate() {
    state.token = getToken()
    state.user = getUserInfo()
  }

  function setSession(token, user) {
    state.token = token
    state.user = user
    setToken(token)
    setUserInfo(user)
  }

  function clearSession() {
    state.token = ''
    state.user = null
    clearAuth()
  }

  return { state, isLoggedIn, deptName, hydrate, setSession, clearSession }
}
