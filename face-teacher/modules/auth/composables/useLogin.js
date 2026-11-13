import { onMounted, reactive, ref } from 'vue'
import { fetchCaptcha, getInfo, login } from '@/modules/auth/api/index.js'
import { getSavedLoginForm, saveLoginForm, setToken } from '@/shared/auth/storage.js'
import { useAuthStore } from '@/stores/auth.js'

export function useLogin() {
  const authStore = useAuthStore()
  const loading = ref(false)
  const captchaImg = ref('')
  const uuid = ref('')
  const form = reactive({ username: '', password: '', code: '', rememberMe: true })

  function onRememberChange(e) {
    form.rememberMe = !!e.detail.value
  }

  async function loadCaptcha() {
    try {
      const res = await fetchCaptcha()
      uuid.value = res.uuid || ''
      captchaImg.value = res.img ? `data:image/gif;base64,${res.img}` : ''
    } catch (e) {
      uni.showToast({ title: e.message || '验证码加载失败', icon: 'none' })
    }
  }

  async function submitLogin() {
    if (!form.username || !form.password || !form.code) {
      uni.showToast({ title: '请完整填写登录信息', icon: 'none' })
      return false
    }
    loading.value = true
    try {
      const res = await login({
        username: form.username,
        password: form.password,
        code: form.code,
        uuid: uuid.value,
        rememberMe: form.rememberMe
      })
      const token = res.token
      if (!token) {
        throw new Error('登录失败，未获取到 token')
      }
      saveLoginForm(form)
      // 必须先写入 token，后续 getInfo 请求才会携带 Authorization
      setToken(token)
      authStore.state.token = token
      const info = await getInfo()
      authStore.setSession(token, info.user)
      uni.reLaunch({ url: '/pages/home/home' })
      return true
    } catch (e) {
      uni.showToast({ title: e.message || '登录失败', icon: 'none' })
      await loadCaptcha()
      return false
    } finally {
      loading.value = false
    }
  }

  async function tryAutoLogin() {
    authStore.hydrate()
    if (!authStore.isLoggedIn.value) return false
    try {
      const info = await getInfo()
      authStore.setSession(authStore.state.token, info.user)
      uni.reLaunch({ url: '/pages/home/home' })
      return true
    } catch (e) {
      authStore.clearSession()
      return false
    }
  }

  onMounted(async () => {
    const saved = getSavedLoginForm()
    form.username = saved.username
    form.password = saved.password
    form.rememberMe = saved.rememberMe
    const ok = await tryAutoLogin()
    if (!ok) await loadCaptcha()
  })

  return {
    loading,
    captchaImg,
    form,
    onRememberChange,
    loadCaptcha,
    submitLogin
  }
}
