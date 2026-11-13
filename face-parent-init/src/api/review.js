import request from './request'

export function fetchClassReview(params) {
  return request({
    url: '/face/parent/review',
    method: 'get',
    params
  })
}
