import { get, getData, post, put } from '@/shared/http/request.js'

export function fetchReviewSubjects() {
  return get('/face/review/subjects').then((body) => getData(body) || [])
}

export function createStandaloneReview(payload) {
  return post('/face/review/create', payload).then((body) => getData(body) || {})
}

export function fetchReviewToday(studentId) {
  return get(`/face/review/student/${studentId}/today`).then((body) => getData(body) || null)
}

export function fetchReviewSession(sessionId) {
  return get(`/face/review/${sessionId}`).then((body) => getData(body) || {})
}

export function saveReviewDraft(payload) {
  return put('/face/review', payload)
}

export function sendReview(sessionId) {
  return post(`/face/review/send/${sessionId}`)
}
