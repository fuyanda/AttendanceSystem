import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getInfo } from '@/modules/auth/api/index.js'
import { fetchProfile, updateProfile } from '@/modules/settings/api/index.js'
import { useAuthStore } from '@/stores/auth.js'

function createForm(user = {}) {
  return {
    nickName: user.nickName || '',
    phonenumber: user.phonenumber || '',
    email: user.email || '',
    userName: user.userName || '',
    deptName: user.dept?.deptName || ''
  }
}

export function useProfile() {
  const authStore = useAuthStore()
  const loading = ref(false)
  const saving = ref(false)
  const form = ref(createForm())
  const roleGroup = ref('')

  async function loadProfile() {
    loading.value = true
    try {
      const res = await fetchProfile()
      form.value = createForm(res.user)
      roleGroup.value = res.roleGroup || ''
    } catch (e) {
      uni.showToast({ title: e.message || '加载失败', icon: 'none' })
    } finally {
      loading.value = false
    }
  }

  async function saveProfile() {
    if (!form.value.nickName.trim()) {
      uni.showToast({ title: '请输入昵称', icon: 'none' })
      return
    }
    saving.value = true
    try {
      await updateProfile({
        nickName: form.value.nickName.trim(),
        phonenumber: form.value.phonenumber?.trim() || '',
        email: form.value.email?.trim() || '',
        sex: '0'
      })
      const info = await getInfo()
      authStore.setSession(authStore.state.token, info.user)
      uni.showToast({ title: '保存成功', icon: 'success' })
    } catch (e) {
      uni.showToast({ title: e.message || '保存失败', icon: 'none' })
    } finally {
      saving.value = false
    }
  }

  function goBack() { uni.navigateBack() }
  function goPassword() { uni.navigateTo({ url: '/pages/settings/password' }) }

  onShow(() => {
    authStore.hydrate()
    loadProfile()
  })

  return { loading, saving, form, roleGroup, saveProfile, goBack, goPassword }
}
