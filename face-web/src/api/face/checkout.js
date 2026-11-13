import request from '@/utils/request'

// 查询签退列表
export function listCheckout(query) {
  return request({
    url: '/face/checkout/list',
    method: 'get',
    params: query
  })
}

// 查询签退详细
export function getCheckout(id) {
  return request({
    url: '/face/checkout/' + id,
    method: 'get'
  })
}
// 重新签退
export function rePush(id) {
  return request({
    url: '/face/checkout/rePush/' + id,
    method: 'get'
  })
}

// 新增签退
export function addCheckout(data) {
  return request({
    url: '/face/checkout',
    method: 'post',
    data: data
  })
}

// 修改签退
export function updateCheckout(data) {
  return request({
    url: '/face/checkout',
    method: 'put',
    data: data
  })
}

// 删除签退
export function delCheckout(id) {
  return request({
    url: '/face/checkout/' + id,
    method: 'delete'
  })
}
