import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { createPunchSetting, fetchPunchSetting, updatePunchSetting } from '@/modules/settings/api/index.js'
import { useAuthStore } from '@/stores/auth.js'

function createEmptyForm(deptId = '') {
  return { id: '', deptId, name: '', startTime: '08:00', endTime: '09:00', status: '1' }
}

export function usePunchSettingForm() {
  const authStore = useAuthStore()
  const loading = ref(false)
  const saving = ref(false)
  const form = ref(createEmptyForm())

  const isEdit = computed(() => !!form.value.id)
  const pageTitle = computed(() => (isEdit.value ? '编辑时段' : '新增时段'))
  const enabled = computed({
    get: () => form.value.status === '1',
    set: (value) => { form.value.status = value ? '1' : '0' }
  })

  async function loadDetail(id) {
    loading.value = true
    try {
      const detail = await fetchPunchSetting(id)
      form.value = { ...createEmptyForm(detail.deptId), ...detail }
    } catch (e) {
      uni.showToast({ title: e.message || '加载失败', icon: 'none' })
      setTimeout(() => uni.navigateBack(), 600)
    } finally {
      loading.value = false
    }
  }

  function onStartTimeChange(e) { form.value.startTime = e.detail.value }
  function onEndTimeChange(e) { form.value.endTime = e.detail.value }

  function validateForm() {
    if (!form.value.name.trim()) {
      uni.showToast({ title: '请输入时段名称', icon: 'none' })
      return false
    }
    if (!form.value.startTime || !form.value.endTime) {
      uni.showToast({ title: '请选择开始和结束时间', icon: 'none' })
      return false
    }
    if (form.value.startTime >= form.value.endTime) {
      uni.showToast({ title: '结束时间须晚于开始时间', icon: 'none' })
      return false
    }
    return true
  }

  async function submitForm() {
    if (saving.value || !validateForm()) return
    saving.value = true
    try {
      const payload = {
        id: form.value.id || undefined,
        deptId: form.value.deptId,
        name: form.value.name.trim(),
        startTime: form.value.startTime,
        endTime: form.value.endTime,
        status: form.value.status
      }
      if (isEdit.value) await updatePunchSetting(payload)
      else await createPunchSetting(payload)
      uni.showToast({ title: '保存成功', icon: 'success' })
      setTimeout(() => uni.navigateBack(), 600)
    } catch (e) {
      uni.showToast({ title: e.message || '保存失败', icon: 'none' })
    } finally {
      saving.value = false
    }
  }

  function goBack() { uni.navigateBack() }

  onLoad((query) => {
    authStore.hydrate()
    const deptId = authStore.state.user?.dept?.deptId
    form.value = createEmptyForm(deptId != null ? String(deptId) : '')
    if (query.id) loadDetail(query.id)
  })

  return { loading, saving, form, enabled, pageTitle, onStartTimeChange, onEndTimeChange, submitForm, goBack }
}
