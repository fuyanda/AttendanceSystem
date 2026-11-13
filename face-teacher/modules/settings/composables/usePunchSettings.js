import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { fetchPunchSettings, removePunchSetting } from '@/modules/settings/api/index.js'
import { useAuthStore } from '@/stores/auth.js'

export function usePunchSettings() {
  const authStore = useAuthStore()
  const loading = ref(false)
  const settings = ref([])

  const deptId = computed(() => {
    const id = authStore.state.user?.dept?.deptId
    return id != null ? String(id) : ''
  })

  const deptName = computed(() => authStore.state.user?.dept?.deptName || '未获取校区')

  async function loadSettings() {
    if (!deptId.value) {
      settings.value = []
      return
    }
    loading.value = true
    try {
      const res = await fetchPunchSettings(deptId.value)
      settings.value = res.rows
    } catch (e) {
      settings.value = []
      uni.showToast({ title: e.message || '加载失败', icon: 'none' })
    } finally {
      loading.value = false
    }
  }

  function goBack() { uni.navigateBack() }
  function goCreate() { uni.navigateTo({ url: '/pages/settings/punch-form' }) }
  function goEdit(item) { uni.navigateTo({ url: `/pages/settings/punch-form?id=${item.id}` }) }

  function handleDelete(item) {
    uni.showModal({
      title: '删除时段',
      content: `确认删除「${item.name}」吗？`,
      success: async (res) => {
        if (!res.confirm) return
        try {
          await removePunchSetting(item.id)
          uni.showToast({ title: '已删除', icon: 'success' })
          await loadSettings()
        } catch (e) {
          uni.showToast({ title: e.message || '删除失败', icon: 'none' })
        }
      }
    })
  }

  onShow(() => {
    authStore.hydrate()
    loadSettings()
  })

  return { loading, settings, deptName, loadSettings, goBack, goCreate, goEdit, handleDelete }
}
