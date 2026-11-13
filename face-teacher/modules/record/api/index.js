import { getHost } from '@/shared/config/host.js'
import { get } from '@/shared/http/request.js'

const PAGE_SIZE = 20

function formatTime(datetime) {
  if (!datetime) return '--:--'
  const part = String(datetime).includes(' ') ? String(datetime).split(' ')[1] : String(datetime)
  return part.slice(0, 5)
}

function normalizeRecord(item, mode) {
  const avatar = item.avatar || ''
  const createTime = item.createTime || ''
  const pushed = item.pushFlag === '1'
  const isCheckout = mode === 'checkout'
  return {
    id: item.id,
    studentId: item.studentId,
    name: item.studentName || '',
    time: createTime,
    displayTime: formatTime(createTime),
    photo: avatar.startsWith('http') ? avatar : `${getHost()}${avatar}`,
    pushFlag: item.pushFlag,
    pushed,
    canRePush: !pushed,
    status: isCheckout ? 'checked_out' : 'checked_in',
    statusText: isCheckout ? '已签退' : '已签到',
    rePushText: isCheckout ? '重新签退' : '重新签到'
  }
}

/**
 * @param {string} [queryDate] YYYY-MM-DD，空则默认今天
 */
function buildListQuery(pageNum, keyword, deptId, queryDate) {
  const params = {
    pageNum,
    pageSize: PAGE_SIZE,
    reasonable: false,
    orderByColumn: 'create_time',
    isAsc: 'descending'
  }
  if (keyword) params.studentName = keyword
  if (deptId) params.deptId = deptId

  const today = formatDate(new Date())
  const day = queryDate || today
  if (day === today) {
    params.today = true
  } else {
    params.today = false
    params['params[beginTime]'] = day
    params['params[endTime]'] = day
  }
  return params
}

export function formatDate(date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

export function fetchCheckinList(pageNum = 1, keyword = '', queryDate = '') {
  return get('/face/checkin/list', buildListQuery(pageNum, keyword, '', queryDate)).then((body) => ({
    total: body.total || 0,
    rows: (body.rows || []).map((item) => normalizeRecord(item, 'checkin'))
  }))
}

export function fetchCheckoutList(pageNum = 1, keyword = '', queryDate = '') {
  return get('/face/checkout/list', buildListQuery(pageNum, keyword, '', queryDate)).then((body) => ({
    total: body.total || 0,
    rows: (body.rows || []).map((item) => normalizeRecord(item, 'checkout'))
  }))
}

export function rePushCheckin(id) {
  return get(`/face/checkin/rePush/${id}`)
}

export function rePushCheckout(id) {
  return get(`/face/checkout/rePush/${id}`)
}

export function fetchCheckinListByDept(deptId, pageNum = 1, keyword = '', queryDate = '') {
  return get('/face/checkin/list', buildListQuery(pageNum, keyword, deptId, queryDate)).then((body) => ({
    total: body.total || 0,
    rows: (body.rows || []).map((item) => normalizeRecord(item, 'checkin'))
  }))
}

export function fetchCheckoutListByDept(deptId, pageNum = 1, keyword = '', queryDate = '') {
  return get('/face/checkout/list', buildListQuery(pageNum, keyword, deptId, queryDate)).then((body) => ({
    total: body.total || 0,
    rows: (body.rows || []).map((item) => normalizeRecord(item, 'checkout'))
  }))
}

export function fetchCheckinDeptStats() {
  return get('/face/checkin/stats/today/dept').then((body) => body.data || [])
}

export function fetchCheckoutDeptStats() {
  return get('/face/checkout/stats/today/dept').then((body) => body.data || [])
}

export function fetchStudentTotal() {
  return get('/face/student/list', { pageNum: 1, pageSize: 1 }).then((body) => body.total || 0)
}
