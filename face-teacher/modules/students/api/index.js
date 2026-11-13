import { getHost } from '@/shared/config/host.js'
import { get, post, put, del, getData } from '@/shared/http/request.js'

function mapStudent(item) {
  return {
    id: item.id,
    name: item.name || '',
    phone: item.phone || '',
    bindCode: item.bindCode || '',
    deptId: item.deptId || '',
    deptName: item.deptName || '',
    deadline: item.deadline || '',
    remark: item.remark || '',
    avatar: item.avatar || '',
    photo: item.avatar ? `${getHost()}${item.avatar}` : '',
    parents: item.parents || []
  }
}

function buildListParams(pageNum, keyword) {
  const params = { pageNum, pageSize: 20 }
  const trimmed = (keyword || '').trim()
  if (!trimmed) return params
  if (/^\d+$/.test(trimmed)) {
    params.id = trimmed
  } else {
    params.name = trimmed
  }
  return params
}

export function fetchStudentList(pageNum = 1, keyword = '') {
  return get('/face/student/list', buildListParams(pageNum, keyword)).then((body) => ({
    total: body.total || 0,
    rows: (body.rows || []).map(mapStudent)
  }))
}

export function fetchStudentListByDept(deptId, pageNum = 1, keyword = '') {
  const params = buildListParams(pageNum, keyword)
  if (deptId && deptId !== '__none__') params.deptId = deptId
  return get('/face/student/list', params).then((body) => ({
    total: body.total || 0,
    rows: (body.rows || []).map(mapStudent)
  }))
}

export function fetchStudent(id) {
  return get(`/face/student/${id}`).then((body) => mapStudent(getData(body) || {}))
}

export function addStudent(data) {
  return post('/face/student', data).then((body) => ({
    message: body.msg || '保存成功',
    data: mapStudent(getData(body) || data)
  }))
}

export function updateStudent(data) {
  return put('/face/student', data).then((body) => mapStudent(getData(body) || data))
}

export function removeStudent(ids) {
  const idPath = Array.isArray(ids) ? ids.join(',') : ids
  return del(`/face/student/${idPath}`)
}

export function regenerateBindCode(id) {
  return post(`/face/student/${id}/regenerate-bind-code`).then((body) => {
    const data = getData(body)
    if (typeof data === 'string') {
      return { bindCode: data }
    }
    return mapStudent(data || {})
  })
}

export function syncBaiduFace(id) {
  return post(`/face/student/${id}/sync-baidu-face`)
}

export function fetchDeptTree() {
  // 走学生模块专用接口，避免依赖 system:user:list
  return get('/face/student/deptTree').then((body) => getData(body) || [])
}

export function flattenDeptTree(nodes = [], prefix = '') {
  const list = []
  nodes.forEach((node) => {
    const label = prefix ? `${prefix} / ${node.label}` : node.label
    list.push({ id: String(node.id), label })
    if (node.children?.length) {
      list.push(...flattenDeptTree(node.children, label))
    }
  })
  return list
}
