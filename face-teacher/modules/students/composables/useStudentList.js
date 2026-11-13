import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { fetchStudentListByDept, fetchDeptTree, flattenDeptTree } from '@/modules/students/api/index.js'
import { useDebouncedSearch } from '@/shared/composables/useDebouncedSearch.js'
import { useAuthStore } from '@/stores/auth.js'

const PAGE_SIZE = 20

/**
 * 每个校区维护独立的分页与学生列表状态。
 * @param {string} deptId
 * @param {string} deptName
 */
function createCampusState(deptId, deptName) {
  return {
    key: deptId,
    name: deptName,
    students: [],
    total: 0,
    pageNum: 1,
    loading: false,
    finished: false,
    loaded: false   // 是否已发起过首次加载
  }
}

export function useStudentList() {
  const authStore = useAuthStore()
  const campusMap = ref(/** @type {Record<string, ReturnType<createCampusState>>} */({}))
  const campusOrder = ref(/** @type {string[]} */([]))
  const campusLoading = ref(false)
  /** 折叠状态：默认全部展开 */
  const collapsedMap = ref(/** @type {Record<string, boolean>> */({}))

  // 搜索模式下退化为全局平铺列表
  const searchStudents = ref([])
  const searchTotal = ref(0)
  const searchLoading = ref(false)
  const searchFinished = ref(false)
  const searchPage = ref(1)

  /** 供模板使用的校区列表（带状态） */
  const campusGroups = computed(() =>
    campusOrder.value.map((key) => campusMap.value[key]).filter(Boolean)
  )

  const total = computed(() => {
    if (isSearching.value) return searchTotal.value
    return campusGroups.value.reduce((sum, g) => sum + g.total, 0)
  })

  const loading = computed(() => {
    if (isSearching.value) return searchLoading.value
    return campusLoading.value
  })

  // ─── 校区初始化 ────────────────────────────────────────────
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

  // ─── 按校区加载学生（懒加载，展开时触发） ──────────────────
  async function loadCampusStudents(key, reset = false) {
    const campus = campusMap.value[key]
    if (!campus) return
    if (campus.loading) return
    if (!reset && campus.finished) return

    campus.loading = true
    if (reset) {
      campus.students = []
      campus.pageNum = 1
      campus.finished = false
    }
    try {
      const res = await fetchStudentListByDept(key, campus.pageNum, '')
      campus.total = res.total
      campus.students = reset ? res.rows : campus.students.concat(res.rows)
      if (campus.students.length >= res.total || res.rows.length === 0) {
        campus.finished = true
      } else {
        campus.pageNum += 1
      }
      campus.loaded = true
    } catch (e) {
      uni.showToast({ title: '加载学生失败', icon: 'none' })
    } finally {
      campus.loading = false
    }
  }

  // ─── 折叠 / 展开 ────────────────────────────────────────────
  function isCampusCollapsed(key) {
    return !!collapsedMap.value[key]
  }

  function toggleCampus(key) {
    const nowCollapsed = collapsedMap.value[key]
    collapsedMap.value = { ...collapsedMap.value, [key]: !nowCollapsed }
    // 展开时若未加载过则触发首次加载
    if (nowCollapsed && !campusMap.value[key]?.loaded) {
      loadCampusStudents(key, true)
    }
  }

  // ─── 搜索模式：全局分页 ─────────────────────────────────────
  async function loadSearchStudents(reset = false) {
    if (searchLoading.value) return
    if (!reset && searchFinished.value) return
    searchLoading.value = true
    if (reset) {
      searchStudents.value = []
      searchPage.value = 1
      searchFinished.value = false
    }
    try {
      const res = await fetchStudentListByDept('', searchPage.value, appliedKeyword.value)
      searchTotal.value = res.total
      searchStudents.value = reset ? res.rows : searchStudents.value.concat(res.rows)
      if (searchStudents.value.length >= res.total || res.rows.length === 0) {
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

  // ─── 公共入口 ────────────────────────────────────────────────
  function applySearch(next) {
    if (next) {
      loadSearchStudents(true)
    } else {
      searchStudents.value = []
      campusOrder.value.forEach((key) => {
        const campus = campusMap.value[key]
        if (campus && !collapsedMap.value[key]) {
          loadCampusStudents(key, true)
        }
      })
    }
  }

  const { keyword, appliedKeyword, isSearching, onSearch, onSearchInput, resetSearch } =
    useDebouncedSearch(applySearch)

  /** 滚动到底部时的分页加载 */
  function loadMore() {
    if (isSearching.value) {
      loadSearchStudents(false)
    }
    // 校区模式下各组内部处理自己的 loadMore
  }

  function goAdd() {
    uni.navigateTo({ url: '/pages/students/form' })
  }

  function goEdit(id) {
    uni.navigateTo({ url: `/pages/students/form?id=${id}` })
  }

  async function init() {
    await loadCampuses()
    // 初始加载所有（展开）校区的第一页学生
    campusOrder.value.forEach((key) => {
      if (!collapsedMap.value[key]) {
        loadCampusStudents(key, true)
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
    resetSearch()
    init()
  })

  return {
    campusGroups,
    campusLoading,
    searchStudents,
    isSearching,
    keyword,
    total,
    loading,
    loadCampusStudents,
    loadSearchStudents,
    onSearch,
    onSearchInput,
    loadMore,
    isCampusCollapsed,
    toggleCampus,
    goAdd,
    goEdit
  }
}
