import request from '@/utils/request'

// 查询打卡设置列表
export function listPunch(query) {
  return request({
    url: '/face/punch/list',
    method: 'get',
    params: query
  })
}

// 查询打卡设置详细
export function getPunch(id) {
  return request({
    url: '/face/punch/' + id,
    method: 'get'
  })
}

// 新增打卡设置
export function addPunch(data) {
  return request({
    url: '/face/punch',
    method: 'post',
    data: data
  })
}

// 修改打卡设置
export function updatePunch(data) {
  return request({
    url: '/face/punch',
    method: 'put',
    data: data
  })
}

// 删除打卡设置
export function delPunch(id) {
  return request({
    url: '/face/punch/' + id,
    method: 'delete'
  })
}
