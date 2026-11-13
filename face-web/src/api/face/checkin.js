import request from '@/utils/request'

// 查询签到列表
export function listCheckin(query) {
  return request({
    url: '/face/checkin/list',
    method: 'get',
    params: query
  })
}

// 查询签到详细
export function getCheckin(id) {
  return request({
    url: '/face/checkin/' + id,
    method: 'get'
  })
}

// 重新签到
export function rePush(id) {
  return request({
    url: '/face/checkin/rePush/' + id,
    method: 'get'
  })
}

// 新增签到
export function addCheckin(data) {
  return request({
    url: '/face/checkin',
    method: 'post',
    data: data
  })
}

// 修改签到
export function updateCheckin(data) {
  return request({
    url: '/face/checkin',
    method: 'put',
    data: data
  })
}

// 删除签到
export function delCheckin(id) {
  return request({
    url: '/face/checkin/' + id,
    method: 'delete'
  })
}
