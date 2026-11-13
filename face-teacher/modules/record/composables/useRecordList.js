import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import {
  fetchCheckinListByDept,
  fetchCheckoutListByDept,
  fetchCheckinDeptStats,
  fetchCheckoutDeptStats,
  fetchStudentTotal,
  formatDate,
  rePushCheckin,
  rePushCheckout
} from '@/modules/record/api/index.js'
import { fetchDeptTree, flattenDeptTree } from '@/modules/students/api/index.js'
import { useAuthStore } from '@/stores/auth.js'
import { buildPathQuery, capturePhotoAtEntry } from '@/shared/utils/camera-entry.js'
import { fetchReviewToday } from '@/modules/review/api/index.js'

const PAGE_SIZE = 20

function createCampusState(deptId, deptName) {
  return {
    key: deptId,
    name: deptName,
    records: [],
    total: 0,
    pageNum: 1,
    loading: false,
    finished: false,
    loaded: false
  }
}

export function useRecordList() {
  const authStore = useAuthStore()
  const activeTab = ref('checkin')
  const keyword = ref('')
  /** 已生效的搜索词（与输入框分离，避免输入时提前进入空搜索态） */
  const appliedKeyword = ref('')
  const studentTotal = ref(0)
  const checkinDeptStats = ref([])
  const checkoutDeptStats = ref([])
  const rePushingId = ref(null)
  const punching = ref(false)
  const queryDate = ref(formatDate(new Date()))
  const isQueryToday = computed(() => queryDate.value === formatDate(new Date()))

  // 校区分组状态
  const campusMap = ref({})
  const campusOrder = ref([])
  const campusLoading = ref(false)
  const collapsedMap = ref({})

  // 搜索退化为全局平铺
  const searchRecords = ref([])
  const searchTotal = ref(0)
  const searchLoading = ref(false)
  const searchFinished = ref(false)
  const searchPage = ref(1)
  const isSearching = computed(() => !!appliedKeyword.value.trim())

  const campusGroups = computed(() =>
    campusOrder.value.map((key) => campusMap.value[key]).filter(Boolean)
  )

  // 校区统计（今日接口）；历史日期改用列表各校区 total
  const statsLoading = ref(false)
  const historyDeptStats = computed(() =>
    campusGroups.value.map((g) => ({
      deptId: g.key,
      deptName: g.name,
      count: g.total || 0
    }))
  )
  const deptStats = computed(() => {
    if (!isQueryToday.value) return historyDeptStats.value
    return activeTab.value === 'checkin' ? checkinDeptStats.value : checkoutDeptStats.value
  })

  function sumDeptCount(list) {
    return (list || []).reduce((sum, item) => sum + (Number(item.count) || 0), 0)
  }

  const checkinTodayTotal = computed(() => sumDeptCount(checkinDeptStats.value))
  const checkoutTodayTotal = computed(() => sumDeptCount(checkoutDeptStats.value))

  const total = computed(() => {
    if (isSearching.value) return searchTotal.value
    return campusGroups.value.reduce((sum, g) => sum + g.total, 0)
  })

  const loading = computed(() => {
    if (isSearching.value) return searchLoading.value
    return campusLoading.value
  })

  const stats = computed(() => {
    const isCheckin = activeTab.value === 'checkin'
    if (!isQueryToday.value) {
      const signed = isSearching.value
        ? searchTotal.value
        : campusGroups.value.reduce((sum, g) => sum + (g.total || 0), 0)
      return {
        title: '查询日统计',
        total: signed,
        signed,
        unsigned: 0,
        totalLabel: '记录数',
        signedLabel: isCheckin ? '已签到' : '已签退',
        unsignedLabel: '仅查记录',
        deptSectionTitle: `各校区（合计 ${signed}）`
      }
    }
    if (isCheckin) {
      // 总人数=学生总数；已签到=各校区签到合计；未签到=差集。按自然日，不按打卡时段。
      const signed = checkinTodayTotal.value
      const totalCount = studentTotal.value
      return {
        title: '今日统计',
        total: totalCount,
        signed,
        unsigned: Math.max(0, totalCount - signed),
        totalLabel: '总人数',
        signedLabel: '已签到',
        unsignedLabel: '未签到',
        deptSectionTitle: `各校区已签到（合计 ${signed}）`
      }
    }
    // 已签到=各校区签到合计；已签退=各校区签退合计；未签退=差集。
    const checkedIn = checkinTodayTotal.value
    const signed = checkoutTodayTotal.value
    return {
      title: '今日统计',
      total: checkedIn,
      signed,
      unsigned: Math.max(0, checkedIn - signed),
      totalLabel: '已签到',
      signedLabel: '已签退',
      unsignedLabel: '未签退',
      deptSectionTitle: `各校区已签退（合计 ${signed}）`
    }
  })

  const sectionTitle = computed(() => (activeTab.value === 'checkin' ? '签到记录' : '签退记录'))
  const searchPlaceholder = computed(() =>
    activeTab.value === 'checkin' ? '搜索签到学生姓名...' : '搜索签退学生姓名...'
  )
  const punchLabel = computed(() => (activeTab.value === 'checkin' ? '开始签到' : '拍照签退'))
  const unsignedClickable = computed(() => isQueryToday.value)

  function getRecordFetcher(deptId, pageNum, kw) {
    const day = queryDate.value
    return activeTab.value === 'checkin'
      ? fetchCheckinListByDept(deptId, pageNum, kw, day)
      : fetchCheckoutListByDept(deptId, pageNum, kw, day)
  }

  function resetCampusRecords() {
    campusOrder.value.forEach((key) => {
      const campus = campusMap.value[key]
      if (campus) {
        campus.records = []
        campus.total = 0
        campus.pageNum = 1
        campus.loading = false
        campus.finished = false
        campus.loaded = false
      }
    })
  }

  function reloadVisibleRecords() {
    if (appliedKeyword.value.trim()) {
      loadSearchRecords(true)
      return
    }
    // 换日期后所有校区都按新条件刷新，折叠的也更新条数，展开时直接有数据
    campusOrder.value.forEach((key) => {
      loadCampusRecords(key, true)
    })
  }

  function onQueryDateChange(day) {
    if (!day || day === queryDate.value) return
    queryDate.value = day
    keyword.value = ''
    appliedKeyword.value = ''
    searchRecords.value = []
    resetCampusRecords()
    if (isQueryToday.value) {
      loadDeptStats()
      loadStudentTotal()
    }
    reloadVisibleRecords()
  }

  function resetQueryDate() {
    const today = formatDate(new Date())
    if (queryDate.value === today) return
    onQueryDateChange(today)
  }

  // ── 校区初始化 ────────────────────────────────────────────
  async function loadCampuses() {
    campusLoading.value = true
    try {
      const tree = await fetchDeptTree()
      const flat = flattenDeptTree(tree)
      const newMap = {}
      const order = []
      flat.forEach(({ id, label }) => {
        const key = String(id)
        newMap[key] = createCampusState(key, label)
        order.push(key)
      })
      campusMap.value = newMap
      campusOrder.value = order
    } catch (e) {
      uni.showToast({ title: '加载校区失败', icon: 'none' })
    } finally {
      campusLoading.value = false
    }
  }

  // ── 按校区加载记录（懒加载） ──────────────────────────────
  async function loadCampusRecords(key, reset = false) {
    const campus = campusMap.value[key]
    if (!campus || campus.loading) return
    if (!reset && campus.finished) return

    campus.loading = true
    if (reset) {
      campus.records = []
      campus.pageNum = 1
      campus.finished = false
    }
    try {
      const res = await getRecordFetcher(key, campus.pageNum, '')
      campus.total = res.total
      campus.records = reset ? res.rows : campus.records.concat(res.rows)
      if (campus.records.length >= res.total || res.rows.length === 0) {
        campus.finished = true
      } else {
        campus.pageNum += 1
      }
      campus.loaded = true
    } catch (e) {
      uni.showToast({ title: '加载记录失败', icon: 'none' })
    } finally {
      campus.loading = false
    }
  }

  // ── 折叠 / 展开 ────────────────────────────────────────────
  function isCampusCollapsed(key) {
    return !!collapsedMap.value[key]
  }

  function toggleCampus(key) {
    const nowCollapsed = collapsedMap.value[key]
    collapsedMap.value = { ...collapsedMap.value, [key]: !nowCollapsed }
    if (nowCollapsed && !campusMap.value[key]?.loaded) {
      loadCampusRecords(key, true)
    }
  }

  // ── 搜索模式 ────────────────────────────────────────────────
  async function loadSearchRecords(reset = false) {
    if (searchLoading.value) return
    if (!reset && searchFinished.value) return
    searchLoading.value = true
    if (reset) {
      searchRecords.value = []
      searchPage.value = 1
      searchFinished.value = false
    }
    try {
      const res = await getRecordFetcher('', searchPage.value, appliedKeyword.value.trim())
      searchTotal.value = res.total
      searchRecords.value = reset ? res.rows : searchRecords.value.concat(res.rows)
      if (searchRecords.value.length >= res.total || res.rows.length === 0) {
        searchFinished.value = true
      } else {
        searchPage.value += 1
      }
    } catch (e) {
      uni.showToast({ title: '搜索失败', icon: 'none' })
    } finally {
      searchLoading.value = false
    }
  }

  // ── 今日校区统计（签到/签退各拉一份，顶部与各校区共用） ──
  async function loadDeptStats() {
    statsLoading.value = true
    try {
      const [checkinStats, checkoutStats] = await Promise.all([
        fetchCheckinDeptStats(),
        fetchCheckoutDeptStats()
      ])
      checkinDeptStats.value = checkinStats || []
      checkoutDeptStats.value = checkoutStats || []
    } catch (e) {
      checkinDeptStats.value = []
      checkoutDeptStats.value = []
    } finally {
      statsLoading.value = false
    }
  }

  async function loadStudentTotal() {
    try {
      studentTotal.value = await fetchStudentTotal()
    } catch (e) {
      studentTotal.value = 0
    }
  }

  // ── 公共入口 ────────────────────────────────────────────────
  function applySearch(value) {
    const next = (value ?? keyword.value).trim()
    appliedKeyword.value = next
    if (next) {
      loadSearchRecords(true)
    } else {
      searchRecords.value = []
      campusOrder.value.forEach((key) => {
        if (!collapsedMap.value[key]) {
          loadCampusRecords(key, true)
        }
      })
    }
  }

  function onSearch(value) {
    if (searchDebounceTimer) clearTimeout(searchDebounceTimer)
    keyword.value = value ?? ''
    applySearch(value)
  }

  let searchDebounceTimer = null
  function onSearchInput(value) {
    keyword.value = value ?? ''
    if (searchDebounceTimer) clearTimeout(searchDebounceTimer)
    if (!keyword.value.trim()) {
      applySearch('')
      return
    }
    searchDebounceTimer = setTimeout(() => {
      applySearch(keyword.value)
    }, 300)
  }

  function loadMore() {
    if (isSearching.value) {
      loadSearchRecords(false)
    }
  }

  function switchTab(tab) {
    if (activeTab.value === tab) return
    activeTab.value = tab
    keyword.value = ''
    appliedKeyword.value = ''
    searchRecords.value = []
    campusOrder.value.forEach((key) => {
      const campus = campusMap.value[key]
      if (campus) {
        campus.records = []
        campus.total = 0
        campus.pageNum = 1
        campus.loading = false
        campus.finished = false
        campus.loaded = false
      }
    })
    searchRecords.value = []
    if (isQueryToday.value) {
      loadDeptStats()
    }
    // 重新加载展开的校区
    campusOrder.value.forEach((key) => {
      if (!collapsedMap.value[key]) {
        loadCampusRecords(key, true)
      }
    })
  }

  async function startPunch() {
    if (punching.value) return
    punching.value = true
    try {
      const path = await capturePhotoAtEntry()
      if (!path) return
      uni.navigateTo({
        url: `/pages/punch/punch?mode=${activeTab.value}&${buildPathQuery(path)}`
      })
    } finally {
      punching.value = false
    }
  }

  function goPunchLive() {
    // #ifdef APP-PLUS
    uni.navigateTo({ url: '/pages/punch-live/punch-live' })
    // #endif
    // #ifndef APP-PLUS
    uni.showToast({ title: '常驻签到仅支持 App', icon: 'none' })
    // #endif
  }

  function goManualCheckout() {
    uni.navigateTo({ url: '/pages/manual-checkout/manual-checkout' })
  }

  function goManualCheckin() {
    uni.navigateTo({ url: '/pages/manual-checkin/manual-checkin' })
  }

  function goUnsignedList() {
    if (!isQueryToday.value) {
      uni.showToast({ title: '历史日期仅支持查看记录', icon: 'none' })
      return
    }
    if (activeTab.value === 'checkin') {
      goManualCheckin()
      return
    }
    goManualCheckout()
  }

  async function openClassReview(record) {
    if (!record?.studentId) {
      uni.showToast({ title: '缺少学员信息', icon: 'none' })
      return
    }
    try {
      const today = await fetchReviewToday(record.studentId)
      if (today && today.sessionId) {
        const itemQ = today.itemId ? `&itemId=${today.itemId}` : ''
        uni.navigateTo({
          url: `/pages/review/edit?sessionId=${today.sessionId}${itemQ}`
        })
        return
      }
      const name = encodeURIComponent(record.name || '')
      uni.navigateTo({
        url: `/pages/review/prepare?studentId=${record.studentId}&studentName=${name}`
      })
    } catch (e) {
      // toast handled by request
    }
  }

  async function handleRePush(record) {
    if (!record?.id || rePushingId.value) return
    const isCheckout = activeTab.value === 'checkout'
    const title = isCheckout ? '重新签退' : '重新签到'
    uni.showModal({
      title,
      content: `确认为「${record.name}」${title}并重新推送通知？`,
      success: async (res) => {
        if (!res.confirm) return
        rePushingId.value = record.id
        try {
          const rePush = isCheckout ? rePushCheckout : rePushCheckin
          await rePush(record.id)
          uni.showToast({ title: `${title}成功`, icon: 'success' })
          // 刷新对应校区
          campusOrder.value.forEach((key) => {
            if (!collapsedMap.value[key] && campusMap.value[key]?.loaded) {
              loadCampusRecords(key, true)
            }
          })
        } catch (e) {
          uni.showToast({ title: e.message || `${title}失败`, icon: 'none' })
        } finally {
          rePushingId.value = null
        }
      }
    })
  }

  async function init() {
    await loadCampuses()
    loadDeptStats()
    loadStudentTotal()
    campusOrder.value.forEach((key) => {
      if (!collapsedMap.value[key]) {
        loadCampusRecords(key, true)
      }
    })
  }

  onShow(() => {
    authStore.hydrate()
    if (!authStore.isLoggedIn.value) {
      uni.reLaunch({ url: '/pages/login/login' })
      return
    }
    collapsedMap.value = {}
    init()
  })

  return {
    activeTab,
    campusGroups,
    campusLoading,
    searchRecords,
    isSearching,
    keyword,
    queryDate,
    isQueryToday,
    unsignedClickable,
    total,
    loading,
    stats,
    deptStats,
    statsLoading,
    sectionTitle,
    searchPlaceholder,
    punchLabel,
    rePushingId,
    switchTab,
    loadMore,
    onSearch,
    onSearchInput,
    onQueryDateChange,
    resetQueryDate,
    loadCampusRecords,
    isCampusCollapsed,
    toggleCampus,
    startPunch,
    goPunchLive,
    goManualCheckout,
    goManualCheckin,
    goUnsignedList,
    deptName: authStore.deptName,
    handleRePush,
    openClassReview
  }
}
