import { get, post } from '@/shared/http/request.js'

export function recognizeFace(imageBase64) {
  return post('/face/recognize', { imageBase64 })
}

export function validatePunch(studentId, mode) {
  return get('/face/punch/validate', { studentId, mode }, { showError: false })
}

export function submitCheckin(payload) {
  return post('/face/checkin', payload)
}

export function submitCheckout(payload) {
  return post('/face/checkout', payload)
}
