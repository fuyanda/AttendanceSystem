import request from '@/utils/request'

export function listReviewSession(query) {
  return request({
    url: '/face/review/list',
    method: 'get',
    params: query
  })
}

export function getReviewSession(sessionId) {
  return request({
    url: '/face/review/' + sessionId,
    method: 'get'
  })
}

export function saveReviewDraft(data) {
  return request({
    url: '/face/review',
    method: 'put',
    data
  })
}

export function sendReview(sessionId) {
  return request({
    url: '/face/review/send/' + sessionId,
    method: 'post'
  })
}
