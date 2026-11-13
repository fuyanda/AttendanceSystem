import request from '@/utils/request'

// 查询学生管理列表
export function listStudent(query) {
  return request({
    url: '/face/student/list',
    method: 'get',
    params: query
  })
}

// 查询学生管理详细
export function getStudent(id) {
  return request({
    url: '/face/student/' + id,
    method: 'get'
  })
}

// 新增学生管理
export function addStudent(data) {
  return request({
    url: '/face/student',
    method: 'post',
    data: data
  })
}

// 修改学生管理
export function updateStudent(data) {
  return request({
    url: '/face/student',
    method: 'put',
    data: data
  })
}

// 删除学生管理
export function delStudent(id) {
  return request({
    url: '/face/student/' + id,
    method: 'delete'
  })
}

export function regenerateBindCode(id) {
  return request({
    url: '/face/student/' + id + '/regenerate-bind-code',
    method: 'post'
  })
}

/** 清理本地多余人脸照片 */
export function cleanupLocalFaces() {
  return request({
    url: '/face/student/cleanup-local-faces',
    method: 'post'
  })
}
