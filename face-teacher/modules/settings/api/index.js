import { del, get, getData, post, put } from '@/shared/http/request.js'

function formatTime(time) {
  if (!time) return ''
  return String(time).slice(0, 5)
}

function mapPunchSetting(item) {
  return {
    id: item.id,
    deptId: item.deptId ? String(item.deptId) : '',
    name: item.name || '',
    startTime: formatTime(item.startTime),
    endTime: formatTime(item.endTime),
    status: item.status != null ? String(item.status) : '1',
    enabled: String(item.status) === '1'
  }
}

export function fetchProfile() {
  return get('/system/user/profile').then((body) => ({
    user: getData(body) || {},
    roleGroup: body.roleGroup || '',
    postGroup: body.postGroup || ''
  }))
}

export function updateProfile(payload) {
  return put('/system/user/profile', payload)
}

export function updatePassword(oldPassword, newPassword) {
  return put('/system/user/profile/updatePwd', {}, { params: { oldPassword, newPassword } })
}

export function fetchPunchSettings(deptId) {
  return get('/face/punch/list', {
    deptId,
    pageNum: 1,
    pageSize: 100,
    reasonable: false
  }).then((body) => ({
    total: body.total || 0,
    rows: (body.rows || []).map(mapPunchSetting)
  }))
}

export function fetchPunchSetting(id) {
  return get(`/face/punch/${id}`).then((body) => mapPunchSetting(getData(body) || {}))
}

export function createPunchSetting(data) {
  return post('/face/punch', data)
}

export function updatePunchSetting(data) {
  return put('/face/punch', data)
}

export function removePunchSetting(id) {
  return del(`/face/punch/${id}`)
}
