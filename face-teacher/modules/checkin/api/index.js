import { getHost } from '@/shared/config/host.js'
import { get, post } from '@/shared/http/request.js'

function normalizeCandidate(item) {
  const avatar = item.avatar || ''
  return {
    studentId: item.studentId,
    name: item.studentName || '',
    deptId: item.deptId || '',
    deptName: item.deptName || '',
    photo: avatar.startsWith('http') ? avatar : `${getHost()}${avatar}`
  }
}

export function fetchManualCheckinCandidates(deptId, keyword = '') {
  const params = { deptId }
  if (keyword) params.studentName = keyword
  return get('/face/checkin/manual/candidates', params).then((body) =>
    (body.data || []).map(normalizeCandidate)
  )
}

export function submitManualCheckin(studentId) {
  return post('/face/checkin/manual', { studentId }, { showError: false })
}
