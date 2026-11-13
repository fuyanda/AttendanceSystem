import { getHost } from '@/shared/config/host.js'
import { getToken } from '@/shared/auth/storage.js'

function parseUploadBody(raw) {
  if (typeof raw === 'string') {
    return JSON.parse(raw)
  }
  return raw
}

function resolveAvatarPath(body) {
  const data = body.data || {}
  return body.fileName || body.url || data.fileName || data.url || ''
}

export function uploadFile(filePath) {
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: `${getHost()}/common/upload`,
      filePath,
      name: 'file',
      header: {
        Authorization: `Bearer ${getToken()}`
      },
      success(res) {
        try {
          const body = parseUploadBody(res.data)
          if (body.code !== 200) {
            reject(new Error(body.msg || '上传失败'))
            return
          }
          const avatarPath = resolveAvatarPath(body)
          if (!avatarPath) {
            reject(new Error('上传返回缺少文件地址'))
            return
          }
          resolve(avatarPath)
        } catch (e) {
          reject(new Error('上传响应解析失败'))
        }
      },
      fail(err) {
        reject(new Error(err.errMsg || '上传失败'))
      }
    })
  })
}
