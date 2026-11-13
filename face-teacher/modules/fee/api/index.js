import { get } from '@/shared/http/request.js'

export function fetchFeeOptions(studentId) {
  return get('/face/fee/options', { studentId })
}
