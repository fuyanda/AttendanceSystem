import { get, getData } from '@/shared/http/request.js'
import { getHost } from '@/shared/config/host.js'

/**
 * @param {number} versionCode
 * @param {string} [platform='android']
 */
export function checkAppVersion(versionCode, platform = 'android') {
  return get('/face/appVersion/check', { platform, versionCode }).then((body) => {
    const data = getData(body) || {}
    const apkUrl = data.apkUrl || ''
    return {
      needUpdate: !!data.needUpdate,
      forceUpdate: !!data.forceUpdate,
      versionName: data.versionName || '',
      versionCode: data.versionCode != null ? Number(data.versionCode) : 0,
      apkUrl: apkUrl.startsWith('http') ? apkUrl : apkUrl ? `${getHost()}${apkUrl}` : '',
      changelog: data.changelog || ''
    }
  })
}
