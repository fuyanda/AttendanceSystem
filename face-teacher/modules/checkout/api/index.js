import { getHost } from '@/shared/config/host.js'
import { get, post } from '@/shared/http/request.js'

function formatTime(datetime) {
  if (!datetime) return '--:--'
  const part = String(datetime).includes(' ') ? String(datetime).split(' ')[1] : String(datetime)
  return part.slice(0, 5)
}

function normalizeCandidate(item) {
  const avatar = item.avatar || ''
  return {
    checkinId: item.checkinId,
    studentId: item.studentId,
    name: item.studentName || '',
    deptId: item.deptId || '',
    deptName: item.deptName || '',
    photo: avatar.startsWith('http') ? avatar : `${getHost()}${avatar}`,
    checkinTime: item.checkinTime || '',
    displayTime: formatTime(item.checkinTime)
  }
}

export function fetchManualCheckoutCandidates(deptId, keyword = '') {
  const params = { deptId }
  if (keyword) params.studentName = keyword
  return get('/face/checkout/manual/candidates', params).then((body) =>
    (body.data || []).map(normalizeCandidate)
  )
}

export function submitManualCheckout(studentId) {
  return post('/face/checkout/manual', { studentId }, { showError: false })
}
