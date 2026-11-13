import { onLoad } from '@dcloudio/uni-app'
import { computed, ref } from 'vue'
import { createStandaloneReview, fetchReviewSubjects } from '@/modules/review/api/index.js'

function pad(n) {
  return String(n).padStart(2, '0')
}

function formatNow() {
  const d = new Date()
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

export function useReviewPrepare() {
  const studentId = ref(null)
  const studentName = ref('')
  const subjects = ref([])
  const selectedSubject = ref('')
  const loading = ref(false)
  const submitting = ref(false)

  const canSubmit = computed(
    () => !!studentId.value && !!selectedSubject.value && !submitting.value
  )

  async function loadSubjects() {
    loading.value = true
    try {
      const list = await fetchReviewSubjects()
      subjects.value = Array.isArray(list) ? list : []
      if (!subjects.value.length) {
        uni.showToast({ title: '暂无科目配置', icon: 'none' })
        return
      }
      selectedSubject.value = subjects.value[0].value
    } catch (e) {
      subjects.value = []
    } finally {
      loading.value = false
    }
  }

  async function confirmCreate() {
    if (!canSubmit.value) return
    submitting.value = true
    try {
      const result = await createStandaloneReview({
        studentId: studentId.value,
        subject: selectedSubject.value,
        classTime: formatNow()
      })
      if (!result.sessionId) {
        throw new Error('创建点评失败')
      }
      const itemQ = result.itemId ? `&itemId=${result.itemId}` : ''
      uni.redirectTo({
        url: `/pages/review/edit?sessionId=${result.sessionId}${itemQ}`
      })
    } catch (e) {
      // toast handled
    } finally {
      submitting.value = false
    }
  }

  function goBack() {
    uni.navigateBack()
  }

  onLoad((query) => {
    studentId.value = query.studentId ? Number(query.studentId) : null
    studentName.value = query.studentName ? decodeURIComponent(query.studentName) : ''
    loadSubjects()
  })

  return {
    studentId,
    studentName,
    subjects,
    selectedSubject,
    loading,
    submitting,
    canSubmit,
    confirmCreate,
    goBack
  }
}
