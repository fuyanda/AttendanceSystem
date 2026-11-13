import request from '@/utils/request'

export function listStudentParents(studentId) {
  return request({
    url: `/face/student-parent/student/${studentId}`,
    method: 'get'
  })
}

export function listUserStudents(userId) {
  return request({
    url: `/face/student-parent/user/${userId}`,
    method: 'get'
  })
}

export function bindStudentParent(studentId, data) {
  return request({
    url: `/face/student-parent/student/${studentId}`,
    method: 'post',
    data
  })
}

export function unbindStudentParent(studentId, userId) {
  return request({
    url: `/face/student-parent/student/${studentId}/user/${userId}`,
    method: 'delete'
  })
}

export function setPrimaryStudentParent(studentId, userId) {
  return request({
    url: `/face/student-parent/student/${studentId}/user/${userId}/primary`,
    method: 'put'
  })
}

export function replaceStudentParents(studentId, data) {
  return request({
    url: `/face/student-parent/student/${studentId}`,
    method: 'put',
    data
  })
}
