import request from './request'

/** 家长端查看打卡详情（需携带微信推送链接中的 sign、exp） */
export function getParentRecord(params) {
  return request({
    url: '/face/parent/record',
    method: 'get',
    params
  })
}
