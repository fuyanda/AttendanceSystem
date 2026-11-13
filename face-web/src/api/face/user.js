import request from '@/utils/request'

// 查询用户信息列表
export function listUser(query) {
  return request({
    url: '/face/user/list',
    method: 'get',
    params: query
  })
}

// 查询用户信息详细
export function getUser(id) {
  return request({
    url: '/face/user/' + id,
    method: 'get'
  })
}

// 新增用户信息
export function addUser(data) {
  return request({
    url: '/face/user',
    method: 'post',
    data: data
  })
}

// 修改用户信息
export function updateUser(data) {
  return request({
    url: '/face/user',
    method: 'put',
    data: data
  })
}

// 删除用户信息
export function delUser(id) {
  return request({
    url: '/face/user/' + id,
    method: 'delete'
  })
}

export function registerParent(data) {
  return request({
    url: '/face/user/register',
    method: 'post',
    data: data
  })
}

export function bindStudentByCode(data) {
  return request({
    url: '/face/user/bind-student',
    method: 'post',
    data: data
  })
}
