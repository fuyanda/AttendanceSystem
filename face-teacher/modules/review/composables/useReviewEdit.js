import { onLoad } from '@dcloudio/uni-app'
import { computed, reactive, ref } from 'vue'
import { fetchReviewSession, saveReviewDraft, sendReview } from '@/modules/review/api/index.js'
import { getHost } from '@/shared/config/host.js'

function parseImagesFromJson(json) {
  if (!json) return []
  if (Array.isArray(json)) return json.filter(Boolean)
  try {
    const arr = JSON.parse(json)
    return Array.isArray(arr) ? arr.filter(Boolean) : []
  } catch (e) {
    return String(json).split(',').filter(Boolean)
  }
}

function imagesToJson(list) {
  const arr = Array.isArray(list) ? list.filter(Boolean) : []
  return arr.length ? JSON.stringify(arr) : ''
}

function toDisplayUrl(path) {
  if (!path) return ''
  if (path.startsWith('http://') || path.startsWith('https://')) return path
  return `${getHost()}${path}`
}

function pad(n) {
  return String(n).padStart(2, '0')
}

function formatDateTime(d) {
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

function normalizeClassTime(raw) {
  if (!raw) return formatDateTime(new Date())
  const text = String(raw).replace('T', ' ').trim()
  if (/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/.test(text)) return text
  if (/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}$/.test(text)) return `${text}:00`
  if (/^\d{4}-\d{2}-\d{2}$/.test(text)) return `${text} 00:00:00`
  const parsed = new Date(text.replace(/-/g, '/'))
  if (!Number.isNaN(parsed.getTime())) return formatDateTime(parsed)
  return formatDateTime(new Date())
}

function splitClassTime(value) {
  const normalized = normalizeClassTime(value)
  const [datePart, timePart = '00:00:00'] = normalized.split(' ')
  return {
    datePart,
    timePart: timePart.slice(0, 5)
  }
}

export function useReviewEdit() {
  const loading = ref(false)
  const saving = ref(false)
  const sending = ref(false)
  const sessionId = ref(null)
  const focusItemId = ref(null)
  const session = ref({})
  const classTime = ref('')
  const classContent = ref('')
  const classImages = ref([])
  const item = reactive({
    id: null,
    studentName: '',
    studentAvatar: '',
    feeId: null,
    consumeId: null,
    remainingHours: 0,
    consumeHours: 0,
    disciplineScore: 5,
    participationScore: 5,
    content: '',
    images: []
  })

  const readonly = computed(() => session.value.status === '1')
  const subjectLabel = computed(() => session.value.subjectLabel || session.value.subject || '')
  const teacherName = computed(() => session.value.teacherName || '')
  const classDate = computed(() => splitClassTime(classTime.value).datePart)
  const classClock = computed(() => splitClassTime(classTime.value).timePart)
  const classTimeDisplay = computed(() => {
    if (!classTime.value) return '--'
    const { datePart, timePart } = splitClassTime(classTime.value)
    return `${datePart} ${timePart}`
  })

  function applyItem(raw) {
    item.id = raw.id
    item.studentName = raw.studentName || ''
    item.studentAvatar = raw.studentAvatar || ''
    item.feeId = raw.feeId || null
    item.consumeId = raw.consumeId || null
    item.remainingHours = raw.remainingHours || 0
    item.consumeHours = raw.consumeHours || 0
    item.disciplineScore = raw.disciplineScore || 5
    item.participationScore = raw.participationScore || 5
    item.content = raw.content || ''
    item.images = parseImagesFromJson(raw.images)
  }

  function mergeClassTime(nextDate, nextClock) {
    const datePart = nextDate || classDate.value
    const timePart = nextClock || classClock.value
    classTime.value = `${datePart} ${timePart}:00`
  }

  function onClassDateChange(e) {
    if (readonly.value) return
    mergeClassTime(e.detail.value, classClock.value)
  }

  function onClassTimeChange(e) {
    if (readonly.value) return
    mergeClassTime(classDate.value, e.detail.value)
  }

  async function loadDetail() {
    if (!sessionId.value) {
      uni.showToast({ title: '缺少点评批次', icon: 'none' })
      return
    }
    loading.value = true
    try {
      const data = await fetchReviewSession(sessionId.value)
      session.value = data || {}
      classTime.value = normalizeClassTime(data.classTime)
      classContent.value = data.classContent || ''
      classImages.value = parseImagesFromJson(data.classImages)
      const items = data.items || []
      let target = null
      if (focusItemId.value) {
        target = items.find((it) => String(it.id) === String(focusItemId.value))
      }
      if (!target) target = items[0]
      if (!target) {
        uni.showToast({ title: '无学员点评项', icon: 'none' })
        return
      }
      focusItemId.value = target.id
      applyItem(target)
    } catch (e) {
      // toast handled
    } finally {
      loading.value = false
    }
  }

  function buildPayload() {
    return {
      sessionId: sessionId.value,
      classTime: normalizeClassTime(classTime.value),
      classContent: classContent.value,
      classImages: imagesToJson(classImages.value),
      items: [
        {
          id: item.id,
          disciplineScore: item.disciplineScore,
          participationScore: item.participationScore,
          content: item.content,
          images: imagesToJson(item.images)
        }
      ]
    }
  }

  async function handleSave() {
    if (readonly.value || saving.value || !item.id) return
    saving.value = true
    try {
      await saveReviewDraft(buildPayload())
      uni.showToast({ title: '草稿已保存', icon: 'success' })
    } catch (e) {
      // toast handled
    } finally {
      saving.value = false
    }
  }

  async function handleSend() {
    if (readonly.value || sending.value || !item.id) return
    uni.showModal({
      title: '确认发送',
      content: '确认发送课堂点评？家长将收到微信服务号通知。',
      success: async (res) => {
        if (!res.confirm) return
        sending.value = true
        try {
          await saveReviewDraft(buildPayload())
          await sendReview(sessionId.value)
          uni.showToast({ title: '发送成功', icon: 'success' })
          await loadDetail()
        } catch (e) {
          // toast handled
        } finally {
          sending.value = false
        }
      }
    })
  }

  function goBack() {
    uni.navigateBack()
  }

  onLoad((query) => {
    sessionId.value = query.sessionId ? Number(query.sessionId) : null
    focusItemId.value = query.itemId ? Number(query.itemId) : null
    loadDetail()
  })

  return {
    loading,
    saving,
    sending,
    session,
    readonly,
    subjectLabel,
    classTime,
    classDate,
    classClock,
    classTimeDisplay,
    teacherName,
    classContent,
    classImages,
    item,
    toDisplayUrl,
    onClassDateChange,
    onClassTimeChange,
    handleSave,
    handleSend,
    goBack
  }
}
