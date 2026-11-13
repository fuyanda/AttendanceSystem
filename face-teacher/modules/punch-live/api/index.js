import { get, post } from '@/shared/http/request.js'

/** 常驻签到循环内不弹全局 Toast，由页面状态区展示 */
export function recognizeFace(imageBase64) {
  return post('/face/recognize', { imageBase64 }, { showError: false })
}

export function validateCheckin(studentId) {
  return get('/face/punch/validate', { studentId, mode: 'checkin' }, { showError: false })
}

export function submitCheckin(payload) {
  return post('/face/checkin', payload, { showError: false })
}
