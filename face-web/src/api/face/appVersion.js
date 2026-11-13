import request from '@/utils/request'

export function listAppVersion(query) {
  return request({
    url: '/face/appVersion/list',
    method: 'get',
    params: query
  })
}

export function getAppVersion(id) {
  return request({
    url: '/face/appVersion/' + id,
    method: 'get'
  })
}

export function addAppVersion(data) {
  return request({
    url: '/face/appVersion',
    method: 'post',
    data
  })
}

export function updateAppVersion(data) {
  return request({
    url: '/face/appVersion',
    method: 'put',
    data
  })
}

export function delAppVersion(id) {
  return request({
    url: '/face/appVersion/' + id,
    method: 'delete'
  })
}
