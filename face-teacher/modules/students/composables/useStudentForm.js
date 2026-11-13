import { computed, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import {
  addStudent,
  fetchDeptTree,
  fetchStudent,
  flattenDeptTree,
  regenerateBindCode,
  removeStudent,
  updateStudent
} from '@/modules/students/api/index.js'
import { useAuthStore } from '@/stores/auth.js'
import { buildPathQuery, capturePhotoAtEntry } from '@/shared/utils/camera-entry.js'

function createEmptyForm(defaultDeptId = '') {
  return {
    id: null,
    name: '',
    phone: '',
    bindCode: '',
    deptId: defaultDeptId ? String(defaultDeptId) : '',
    deadline: '',
    avatar: '',
    photo: '',
    remark: '',
    parents: []
  }
}

export function useStudentForm() {
  const authStore = useAuthStore()
  const form = ref(createEmptyForm())
  const deptOptions = ref([])
  const saving = ref(false)
  const loading = ref(false)
  const regeneratingBindCode = ref(false)
  const isEdit = computed(() => !!form.value.id)
  const pageTitle = computed(() => (isEdit.value ? '编辑学生' : '添加学生'))

  const deptIndex = computed(() => {
    const idx = deptOptions.value.findIndex((item) => item.id === form.value.deptId)
    return idx >= 0 ? idx : 0
  })

  const deptLabel = computed(() => {
    const current = deptOptions.value.find((item) => item.id === form.value.deptId)
    return current?.label || '请选择归属校区'
  })

  async function loadDeptOptions() {
    const tree = await fetchDeptTree()
    deptOptions.value = flattenDeptTree(tree)
  }

  async function loadStudent(id) {
    loading.value = true
    try {
      form.value = await fetchStudent(id)
    } finally {
      loading.value = false
    }
  }

  function onDeptChange(e) {
    const option = deptOptions.value[e.detail.value]
    if (option) form.value.deptId = option.id
  }

  function onDeadlineChange(e) {
    form.value.deadline = e.detail.value
  }

  function validateForm() {
    if (!form.value.deptId) {
      uni.showToast({ title: '请选择归属校区', icon: 'none' })
      return false
    }
    if (!form.value.name.trim()) {
      uni.showToast({ title: '请输入学生姓名', icon: 'none' })
      return false
    }
    return true
  }

  async function submitForm() {
    if (saving.value || !validateForm()) return
    saving.value = true
    try {
      const payload = buildPayload()
      if (isEdit.value) {
        await updateStudent(payload)
        uni.showToast({ title: '保存成功', icon: 'success' })
        setTimeout(() => uni.navigateBack(), 600)
      } else {
        const res = await addStudent(payload)
        form.value.id = res.data.id
        form.value.bindCode = res.data.bindCode || form.value.bindCode
        uni.showModal({
          title: '请保存绑定码',
          content: res.data.bindCode
            ? `绑定码：${res.data.bindCode}\n请将此绑定码告知家长，然后可拍照采集人脸。`
            : res.message,
          showCancel: false
        })
      }
    } catch (e) {
      uni.showToast({ title: e.message || '保存失败', icon: 'none' })
    } finally {
      saving.value = false
    }
  }

  async function handleRegenerateBindCode() {
    if (!form.value.id || regeneratingBindCode.value) return
    regeneratingBindCode.value = true
    try {
      const student = await regenerateBindCode(form.value.id)
      const bindCode = student.bindCode || ''
      if (!bindCode) {
        uni.showToast({ title: '未获取到绑定码', icon: 'none' })
        return
      }
      form.value.bindCode = bindCode
      uni.showToast({ title: '绑定码已重新生成', icon: 'success' })
    } catch (e) {
      uni.showToast({ title: e.message || '操作失败', icon: 'none' })
    } finally {
      regeneratingBindCode.value = false
    }
  }

  function copyBindCode() {
    if (!form.value.bindCode) return
    uni.setClipboardData({
      data: form.value.bindCode,
      success() {
        uni.showToast({ title: '绑定码已复制', icon: 'success' })
      }
    })
  }

  function handleArchive() {
    if (!form.value.id) return
    uni.showModal({
      title: '确认归档',
      content: `确认归档【${form.value.name}】吗？`,
      success: async (res) => {
        if (!res.confirm) return
        try {
          await removeStudent(form.value.id)
          uni.showToast({ title: '归档成功', icon: 'success' })
          setTimeout(() => uni.navigateBack(), 600)
        } catch (e) {
          uni.showToast({ title: e.message || '归档失败', icon: 'none' })
        }
      }
    })
  }

  function buildPayload() {
    return {
      id: form.value.id,
      name: form.value.name.trim(),
      phone: form.value.phone?.trim() || '',
      deptId: form.value.deptId,
      deadline: form.value.deadline || null,
      avatar: form.value.avatar || null,
      remark: form.value.remark?.trim() || '',
      bindCode: form.value.bindCode || null
    }
  }

  async function ensureStudentSaved() {
    if (form.value.id) return true
    if (!validateForm()) return false
    saving.value = true
    try {
      const res = await addStudent(buildPayload())
      form.value.id = res.data.id
      form.value.bindCode = res.data.bindCode || form.value.bindCode
      if (res.data.photo) form.value.photo = res.data.photo
      if (res.data.avatar) form.value.avatar = res.data.avatar
      return true
    } catch (e) {
      uni.showToast({ title: e.message || '保存失败', icon: 'none' })
      return false
    } finally {
      saving.value = false
    }
  }

  async function goFaceCapture() {
    const saved = await ensureStudentSaved()
    if (!saved || !form.value.id) return
    const path = await capturePhotoAtEntry()
    if (!path) return
    const name = encodeURIComponent(form.value.name || '')
    uni.navigateTo({
      url: `/pages/students/face-capture?id=${form.value.id}&name=${name}&${buildPathQuery(path)}`
    })
  }

  function goBack() {
    uni.navigateBack()
  }

  onLoad(async (query) => {
    authStore.hydrate()
    const defaultDeptId = authStore.state.user?.dept?.deptId || ''
    form.value = createEmptyForm(defaultDeptId)
    try {
      await loadDeptOptions()
      if (query.id) {
        await loadStudent(query.id)
      }
    } catch (e) {
      uni.showToast({ title: e.message || '加载失败', icon: 'none' })
    }
  })

  async function refreshStudentSilently() {
    if (!form.value.id || regeneratingBindCode.value || saving.value) return
    try {
      const student = await fetchStudent(form.value.id)
      const current = form.value
      form.value = {
        ...current,
        ...student,
        name: current.name || student.name,
        phone: current.phone ?? student.phone,
        deptId: current.deptId || student.deptId,
        deadline: current.deadline || student.deadline,
        remark: current.remark ?? student.remark,
        bindCode: student.bindCode || current.bindCode
      }
    } catch (e) {
      // 静默刷新，不打断表单编辑
    }
  }

  onShow(() => {
    refreshStudentSilently()
  })

  return {
    form,
    deptOptions,
    deptIndex,
    deptLabel,
    saving,
    loading,
    isEdit,
    pageTitle,
    onDeptChange,
    onDeadlineChange,
    submitForm,
    handleRegenerateBindCode,
    copyBindCode,
    handleArchive,
    goFaceCapture,
    goBack
  }
}
