import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import {
  fetchManualCheckoutCandidates,
  submitManualCheckout
} from '@/modules/checkout/api/index.js'
import { fetchDeptTree, flattenDeptTree } from '@/modules/students/api/index.js'
import { useDebouncedSearch } from '@/shared/composables/useDebouncedSearch.js'
import { useAuthStore } from '@/stores/auth.js'

export function useManualCheckout() {
  const authStore = useAuthStore()
  const candidates = ref([])
  const deptId = ref('')
  const deptOptions = ref([])
  const campusLoading = ref(false)
  const loading = ref(false)
  const submitting = ref(false)
  const selectedIds = ref([])

  const deptLabel = computed(() => {
    const current = deptOptions.value.find((item) => item.id === deptId.value)
    return current?.label || ''
  })

  const selectedCount = computed(() => selectedIds.value.length)
  const allSelected = computed(
    () => candidates.value.length > 0 && selectedIds.value.length === candidates.value.length
  )
  const submitText = computed(() =>
    selectedCount.value > 0 ? `确认签退（${selectedCount.value}）` : '确认签退'
  )

  function isSelected(studentId) {
    return selectedIds.value.includes(studentId)
  }

  function toggleSelect(studentId) {
    if (submitting.value) return
    const index = selectedIds.value.indexOf(studentId)
    if (index >= 0) {
      selectedIds.value = selectedIds.value.filter((id) => id !== studentId)
    } else {
      selectedIds.value = [...selectedIds.value, studentId]
    }
  }

  function toggleSelectAll() {
    if (submitting.value || !candidates.value.length) return
    if (allSelected.value) {
      selectedIds.value = []
      return
    }
    selectedIds.value = candidates.value.map((item) => item.studentId)
  }

  async function loadCampusOptions() {
    campusLoading.value = true
    try {
      const tree = await fetchDeptTree()
      deptOptions.value = flattenDeptTree(tree)
      if (!deptId.value && deptOptions.value.length) {
        const userDeptId = authStore.state.user?.dept?.deptId
        const matched = deptOptions.value.find((item) => item.id === String(userDeptId))
        deptId.value = matched?.id || deptOptions.value[0].id
      }
    } catch (e) {
      deptOptions.value = []
      uni.showToast({ title: '加载校区失败', icon: 'none' })
    } finally {
      campusLoading.value = false
    }
  }

  async function loadCandidates() {
    if (!deptId.value) {
      candidates.value = []
      selectedIds.value = []
      return
    }
    loading.value = true
    try {
      const rows = await fetchManualCheckoutCandidates(deptId.value, appliedKeyword.value)
      candidates.value = rows
      selectedIds.value = selectedIds.value.filter((id) => rows.some((item) => item.studentId === id))
    } catch (e) {
      candidates.value = []
      uni.showToast({ title: e.message || '加载失败', icon: 'none' })
    } finally {
      loading.value = false
    }
  }

  const { keyword, appliedKeyword, onSearch, onSearchInput, resetSearch } = useDebouncedSearch(() => {
    loadCandidates()
  })

  function onCampusChange() {
    selectedIds.value = []
    resetSearch()
    loadCandidates()
  }

  function goBack() {
    uni.navigateBack()
  }

  async function confirmCheckout() {
    if (!selectedIds.value.length || submitting.value) return
    submitting.value = true
    let successCount = 0
    const failed = []
    try {
      for (const studentId of selectedIds.value) {
        const student = candidates.value.find((item) => item.studentId === studentId)
        try {
          await submitManualCheckout(studentId)
          successCount += 1
        } catch (e) {
          failed.push(`${student?.name || studentId}：${e.message || '签退失败'}`)
        }
      }
      if (successCount > 0) {
        uni.showToast({ title: `成功签退 ${successCount} 人`, icon: 'success' })
      }
      if (failed.length) {
        uni.showModal({
          title: '部分签退失败',
          content: failed.join('\n'),
          showCancel: false
        })
      }
      selectedIds.value = []
      await loadCandidates()
      if (successCount > 0 && !candidates.value.length) {
        setTimeout(() => uni.navigateBack(), 600)
      }
    } finally {
      submitting.value = false
    }
  }

  async function init() {
    await loadCampusOptions()
    await loadCandidates()
  }

  onShow(() => {
    authStore.hydrate()
    if (!authStore.isLoggedIn.value) {
      uni.reLaunch({ url: '/pages/login/login' })
      return
    }
    init()
  })

  return {
    candidates,
    keyword,
    deptId,
    deptOptions,
    deptLabel,
    campusLoading,
    loading,
    submitting,
    selectedCount,
    allSelected,
    submitText,
    isSelected,
    toggleSelect,
    toggleSelectAll,
    onCampusChange,
    onSearch,
    onSearchInput,
    goBack,
    confirmCheckout
  }
}
