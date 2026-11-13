import { ref, computed } from 'vue'

const STORAGE_KEY = 'face-parent-session'

function loadSession() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

function saveSession(data) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(data))
  } catch {
    // ignore
  }
}

const session = ref(loadSession() || {
  phone: '',
  password: '',
  openId: '',
  studentNames: []
})

export function useParentStore() {
  const phone = computed(() => session.value.phone)
  const password = computed(() => session.value.password)
  const studentNames = computed(() => session.value.studentNames || [])
  const hasSession = computed(() => Boolean(session.value.phone))

  function mergeBindResult(result) {
    const names = new Set(session.value.studentNames || [])
    ;(result.studentNames || []).forEach((n) => names.add(n))
    session.value = {
      ...session.value,
      phone: result.phone || session.value.phone,
      password: result.password || session.value.password,
      studentNames: [...names]
    }
    saveSession(session.value)
  }

  function clearSession() {
    session.value = { phone: '', password: '', openId: '', studentNames: [] }
    try {
      localStorage.removeItem(STORAGE_KEY)
    } catch {
      // ignore
    }
  }

  return {
    session,
    phone,
    password,
    studentNames,
    hasSession,
    mergeBindResult,
    clearSession
  }
}
