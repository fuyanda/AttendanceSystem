import { listCheckin } from '@/api/face/checkin'
import { listCheckout } from '@/api/face/checkout'
import { listStudent } from '@/api/face/student'
import { listPunch } from '@/api/face/punch'
import { getInfo } from '@/api/login'

const LIST_OPTS = {
  pageNum: 1,
  reasonable: false
}

export function fetchDashboardUser() {
  return getInfo().then((res) => ({
    nickName: res.user?.nickName || res.user?.userName || '',
    deptName: res.user?.dept?.deptName || '',
    deptId: res.user?.dept?.deptId
  }))
}

export function fetchStudentTotal() {
  return listStudent({ ...LIST_OPTS, pageSize: 1 }).then((res) => res.total || 0)
}

export function fetchTodayCheckinTotal() {
  return listCheckin({ ...LIST_OPTS, pageSize: 1, today: true }).then((res) => res.total || 0)
}

export function fetchTodayCheckoutTotal() {
  return listCheckout({ ...LIST_OPTS, pageSize: 1, today: true }).then((res) => res.total || 0)
}

export function fetchTodayPushPending(type) {
  const api = type === 'checkout' ? listCheckout : listCheckin
  return api({ ...LIST_OPTS, pageSize: 1, today: true, pushFlag: '0' }).then((res) => res.total || 0)
}

export function fetchTodayRecords(type, pageSize = 8) {
  const api = type === 'checkout' ? listCheckout : listCheckin
  return api({
    ...LIST_OPTS,
    pageSize,
    today: true,
    orderByColumn: 'create_time',
    isAsc: 'descending'
  }).then((res) => res.rows || [])
}

/**
 * @param {{ checkin?: boolean, checkout?: boolean }} access
 */
export function fetchTodayUnpushedRecords(pageSize = 10, access = { checkin: true, checkout: true }) {
  const tasks = []
  if (access.checkin) {
    tasks.push(
      listCheckin({ ...LIST_OPTS, pageSize, today: true, pushFlag: '0' }).then((res) => res.rows || [])
    )
  }
  if (access.checkout) {
    tasks.push(
      listCheckout({ ...LIST_OPTS, pageSize, today: true, pushFlag: '0' }).then((res) => res.rows || [])
    )
  }
  if (!tasks.length) {
    return Promise.resolve([])
  }
  return Promise.all(tasks).then((groups) => {
    const checkins = access.checkin
      ? (groups[0] || []).map((row) => ({ ...row, recordType: 'checkin' }))
      : []
    const checkouts = access.checkout
      ? (groups[access.checkin ? 1 : 0] || []).map((row) => ({ ...row, recordType: 'checkout' }))
      : []
    return [...checkins, ...checkouts].sort(
      (a, b) => new Date(b.createTime).getTime() - new Date(a.createTime).getTime()
    )
  })
}

export function fetchPunchWindows(deptId) {
  const params = { ...LIST_OPTS, pageSize: 100, status: '1' }
  if (deptId != null) params.deptId = deptId
  return listPunch(params).then((res) => res.rows || [])
}
