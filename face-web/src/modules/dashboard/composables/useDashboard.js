import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import auth from '@/plugins/auth'
import usePermissionStore from '@/store/modules/permission'
import { DASHBOARD_PERMS } from '@/modules/dashboard/constants/permissions.js'
import {
  fetchDashboardUser,
  fetchPunchWindows,
  fetchStudentTotal,
  fetchTodayCheckinTotal,
  fetchTodayCheckoutTotal,
  fetchTodayPushPending,
  fetchTodayRecords,
  fetchTodayUnpushedRecords
} from '@/modules/dashboard/api/index.js'

function padTime(value) {
  if (!value) return ''
  return String(value).slice(0, 5)
}

function isTimeInWindow(now, start, end) {
  const current = padTime(now)
  const from = padTime(start)
  const to = padTime(end)
  if (!from || !to) return false
  return current >= from && current <= to
}

function flattenRoutes(routes, parentPath = '') {
  const result = []
  routes.forEach((route) => {
    const segment = route.path || ''
    const fullPath = `${parentPath}/${segment}`.replace(/\/+/g, '/') || '/'
    if (route.children?.length) {
      result.push(...flattenRoutes(route.children, fullPath))
      return
    }
    if (route.meta?.title) {
      result.push({ path: fullPath, title: route.meta.title })
    }
  })
  return result
}

function buildAccess() {
  return {
    student: auth.hasPermi(DASHBOARD_PERMS.student),
    checkin: auth.hasPermi(DASHBOARD_PERMS.checkin),
    checkout: auth.hasPermi(DASHBOARD_PERMS.checkout),
    punch: auth.hasPermi(DASHBOARD_PERMS.punch)
  }
}

async function safeFetch(enabled, fetchFn, defaultValue) {
  if (!enabled) return defaultValue
  try {
    return await fetchFn()
  } catch {
    return defaultValue
  }
}

export function useDashboard() {
  const router = useRouter()
  const loading = ref(false)
  const user = ref({ nickName: '', deptName: '', deptId: null })
  const stats = ref({
    studentTotal: 0,
    checkinToday: 0,
    checkoutToday: 0,
    pushPendingCheckin: 0,
    pushPendingCheckout: 0
  })
  const punchWindows = ref([])
  const recentCheckins = ref([])
  const recentCheckouts = ref([])
  const pendingRecords = ref([])

  const access = computed(() => buildAccess())

  const todayText = computed(() => {
    const now = new Date()
    const week = ['日', '一', '二', '三', '四', '五', '六']
    return `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日 星期${week[now.getDay()]}`
  })

  const checkinRate = computed(() => {
    if (!access.value.student || !stats.value.studentTotal) return 0
    return Math.round((stats.value.checkinToday / stats.value.studentTotal) * 100)
  })

  const checkoutRate = computed(() => {
    if (!access.value.student || !stats.value.studentTotal) return 0
    return Math.round((stats.value.checkoutToday / stats.value.studentTotal) * 100)
  })

  const unsignedCheckin = computed(() => {
    if (!access.value.student) return 0
    return Math.max(0, stats.value.studentTotal - stats.value.checkinToday)
  })

  const unsignedCheckout = computed(() => {
    if (!access.value.student) return 0
    return Math.max(0, stats.value.studentTotal - stats.value.checkoutToday)
  })

  const activePunchWindow = computed(() => {
    const now = new Date()
    const current = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`
    return punchWindows.value.find((item) => isTimeInWindow(current, item.startTime, item.endTime)) || null
  })

  const chartData = computed(() => ({
    checkin: { signed: stats.value.checkinToday, unsigned: unsignedCheckin.value },
    checkout: { signed: stats.value.checkoutToday, unsigned: unsignedCheckout.value }
  }))

  const showAttendanceChart = computed(
    () => access.value.student && (access.value.checkin || access.value.checkout)
  )

  const showRecentList = computed(() => access.value.checkin || access.value.checkout)

  const showPendingTable = computed(() => access.value.checkin || access.value.checkout)

  const showStatGrid = computed(
    () => access.value.student || access.value.checkin || access.value.checkout
  )

  const hasDashboardData = computed(
    () => showStatGrid.value || showAttendanceChart.value || showRecentList.value
      || showPendingTable.value || access.value.punch
  )

  const statItems = computed(() => {
    const items = []
    if (access.value.student) {
      items.push({ key: 'total', label: '学生总数', value: stats.value.studentTotal, tone: 'default' })
    }
    if (access.value.checkin) {
      items.push({
        key: 'checkin',
        label: '今日签到',
        value: stats.value.checkinToday,
        hint: access.value.student ? `完成率 ${checkinRate.value}%` : '',
        tone: 'success'
      })
    }
    if (access.value.checkout) {
      items.push({
        key: 'checkout',
        label: '今日签退',
        value: stats.value.checkoutToday,
        hint: access.value.student ? `完成率 ${checkoutRate.value}%` : '',
        tone: 'primary'
      })
    }
    if (access.value.student && access.value.checkin) {
      items.push({ key: 'unsigned-in', label: '未签到', value: unsignedCheckin.value, tone: 'warning' })
    }
    if (access.value.student && access.value.checkout) {
      items.push({ key: 'unsigned-out', label: '未签退', value: unsignedCheckout.value, tone: 'warning' })
    }
    if (access.value.checkin || access.value.checkout) {
      items.push({
        key: 'pending',
        label: '待推送',
        value: stats.value.pushPendingCheckin + stats.value.pushPendingCheckout,
        hint: [
          access.value.checkin ? `签到 ${stats.value.pushPendingCheckin}` : '',
          access.value.checkout ? `签退 ${stats.value.pushPendingCheckout}` : ''
        ].filter(Boolean).join(' / '),
        tone: 'info'
      })
    }
    return items
  })

  function resolveMenuPath(keyword) {
    const routes = flattenRoutes(usePermissionStore().sidebarRouters || [])
    const match = routes.find((item) => item.path.includes(keyword))
    return match?.path || ''
  }

  const quickLinks = computed(() => [
    { label: '学生管理', path: resolveMenuPath('student'), icon: 'User' },
    { label: '签到记录', path: resolveMenuPath('checkin'), icon: 'CircleCheck' },
    { label: '签退记录', path: resolveMenuPath('checkout'), icon: 'SwitchButton' },
    { label: '打卡时段', path: resolveMenuPath('punch'), icon: 'Clock' }
  ].filter((item) => item.path))

  async function loadDashboard() {
    loading.value = true
    const perm = buildAccess()
    try {
      const profile = await fetchDashboardUser()
      user.value = profile

      const [
        studentTotal,
        checkinToday,
        checkoutToday,
        pushPendingCheckin,
        pushPendingCheckout,
        windows,
        checkins,
        checkouts,
        pending
      ] = await Promise.all([
        safeFetch(perm.student, fetchStudentTotal, 0),
        safeFetch(perm.checkin, fetchTodayCheckinTotal, 0),
        safeFetch(perm.checkout, fetchTodayCheckoutTotal, 0),
        safeFetch(perm.checkin, () => fetchTodayPushPending('checkin'), 0),
        safeFetch(perm.checkout, () => fetchTodayPushPending('checkout'), 0),
        safeFetch(perm.punch, () => fetchPunchWindows(profile.deptId), []),
        safeFetch(perm.checkin, () => fetchTodayRecords('checkin', 8), []),
        safeFetch(perm.checkout, () => fetchTodayRecords('checkout', 8), []),
        safeFetch(
          perm.checkin || perm.checkout,
          () => fetchTodayUnpushedRecords(10, { checkin: perm.checkin, checkout: perm.checkout }),
          []
        )
      ])

      stats.value = { studentTotal, checkinToday, checkoutToday, pushPendingCheckin, pushPendingCheckout }

      const current = `${String(new Date().getHours()).padStart(2, '0')}:${String(new Date().getMinutes()).padStart(2, '0')}`
      punchWindows.value = windows.map((item) => ({
        ...item,
        startTime: padTime(item.startTime),
        endTime: padTime(item.endTime),
        active: isTimeInWindow(current, item.startTime, item.endTime)
      }))
      recentCheckins.value = checkins
      recentCheckouts.value = checkouts
      pendingRecords.value = pending
    } finally {
      loading.value = false
    }
  }

  function goPath(path) {
    if (!path) return
    router.push(path)
  }

  onMounted(() => { loadDashboard() })

  return {
    loading,
    user,
    stats,
    access,
    statItems,
    todayText,
    checkinRate,
    checkoutRate,
    unsignedCheckin,
    unsignedCheckout,
    punchWindows,
    activePunchWindow,
    chartData,
    recentCheckins,
    recentCheckouts,
    pendingRecords,
    quickLinks,
    showStatGrid,
    showAttendanceChart,
    showRecentList,
    showPendingTable,
    hasDashboardData,
    loadDashboard,
    goPath
  }
}
