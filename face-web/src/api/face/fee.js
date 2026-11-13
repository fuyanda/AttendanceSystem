import request from '@/utils/request'

export function listFee(query) {
  return request({
    url: '/face/fee/list',
    method: 'get',
    params: query
  })
}

export function getFee(id) {
  return request({
    url: '/face/fee/' + id,
    method: 'get'
  })
}

export function listFeeByStudent(studentId) {
  return request({
    url: '/face/fee/student/' + studentId,
    method: 'get'
  })
}

export function listFeeTeachers(deptId) {
  const params = {}
  if (deptId != null && deptId !== '') {
    params.deptId = deptId
  }
  return request({
    url: '/face/fee/teachers',
    method: 'get',
    params
  })
}

export function getFeeTeacherSelect(deptId) {
  const params = {}
  if (deptId != null && deptId !== '') {
    params.deptId = deptId
  }
  return request({
    url: '/face/fee/teachers/select',
    method: 'get',
    params
  })
}

export function addFee(data) {
  return request({
    url: '/face/fee',
    method: 'post',
    data
  })
}

export function updateFee(data) {
  return request({
    url: '/face/fee',
    method: 'put',
    data
  })
}

export function delFee(ids) {
  return request({
    url: '/face/fee/' + ids,
    method: 'delete'
  })
}

export function consumeFee(data) {
  return request({
    url: '/face/fee/consume',
    method: 'post',
    data
  })
}

export function getConsumeContext() {
  return request({
    url: '/face/fee/consume/context',
    method: 'get'
  })
}

export function listConsumeSubjects(teacherUserId) {
  return request({
    url: '/face/fee/consume/subjects',
    method: 'get',
    params: { teacherUserId }
  })
}

export function listConsumeStudents(teacherUserId, subject) {
  return request({
    url: '/face/fee/consume/students',
    method: 'get',
    params: { teacherUserId, subject }
  })
}

export function batchConsumeFee(data) {
  return request({
    url: '/face/fee/consume/batch',
    method: 'post',
    data
  })
}

export function refundFee(data) {
  return request({
    url: '/face/fee/refund',
    method: 'post',
    data
  })
}

export function listFeeLedger(query) {
  return request({
    url: '/face/fee/ledger/list',
    method: 'get',
    params: query
  })
}

export function getFeeLedgerSummary(query) {
  return request({
    url: '/face/fee/ledger/summary',
    method: 'get',
    params: query
  })
}
