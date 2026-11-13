import request from '@/utils/request'

export function listScheduleTeachers() {
  return request({
    url: '/face/schedule/teachers',
    method: 'get'
  })
}

export function listScheduleStudents(params) {
  return request({
    url: '/face/schedule/students',
    method: 'get',
    params
  })
}

export function teacherCalendar(params) {
  return request({
    url: '/face/schedule/teacher/calendar',
    method: 'get',
    params
  })
}

export function studentCalendar(params) {
  return request({
    url: '/face/schedule/student/calendar',
    method: 'get',
    params
  })
}

export function getSchedule(id) {
  return request({
    url: '/face/schedule/' + id,
    method: 'get'
  })
}

export function addSchedule(data) {
  return request({
    url: '/face/schedule',
    method: 'post',
    data
  })
}

export function updateSchedule(data) {
  return request({
    url: '/face/schedule',
    method: 'put',
    data
  })
}

export function batchSchedule(data) {
  return request({
    url: '/face/schedule/batch',
    method: 'post',
    data
  })
}

export function delSchedule(id) {
  return request({
    url: '/face/schedule/' + id,
    method: 'delete'
  })
}
