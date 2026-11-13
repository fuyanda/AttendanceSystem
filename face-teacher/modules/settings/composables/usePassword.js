import { ref } from 'vue'
import { updatePassword } from '@/modules/settings/api/index.js'

export function usePassword() {
  const saving = ref(false)
  const form = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })

  function goBack() { uni.navigateBack() }

  async function submitPassword() {
    const { oldPassword, newPassword, confirmPassword } = form.value
    if (!oldPassword || !newPassword || !confirmPassword) {
      uni.showToast({ title: '请填写完整', icon: 'none' })
      return
    }
    if (newPassword.length < 5) {
      uni.showToast({ title: '新密码至少 5 位', icon: 'none' })
      return
    }
    if (newPassword !== confirmPassword) {
      uni.showToast({ title: '两次新密码不一致', icon: 'none' })
      return
    }
    saving.value = true
    try {
      await updatePassword(oldPassword, newPassword)
      uni.showToast({ title: '密码已修改', icon: 'success' })
      form.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
      setTimeout(() => uni.navigateBack(), 600)
    } catch (e) {
      uni.showToast({ title: e.message || '修改失败', icon: 'none' })
    } finally {
      saving.value = false
    }
  }

  return { saving, form, goBack, submitPassword }
}
