import { ref } from 'vue'
import { checkAppVersion } from '@/modules/app-update/api/index.js'
import { useAuthStore } from '@/stores/auth.js'

/** 本会话已点「稍后」的可选更新，启动检测不再弹 */
let skippedOptionalThisSession = false

function isAppPlus() {
  return typeof plus !== 'undefined' && !!plus.runtime
}

function readLocalVersion() {
  if (!isAppPlus()) {
    return { versionName: 'dev', versionCode: 0 }
  }
  try {
    return {
      versionName: String(plus.runtime.version || ''),
      versionCode: Number(plus.runtime.versionCode || 0)
    }
  } catch (e) {
    return { versionName: '', versionCode: 0 }
  }
}

function resolveApkPath(tempFilePath) {
  if (isAppPlus() && plus.io && typeof plus.io.convertLocalFileSystemURL === 'function') {
    return plus.io.convertLocalFileSystemURL(tempFilePath)
  }
  return tempFilePath
}

export function useAppUpdate() {
  const authStore = useAuthStore()
  const visible = ref(false)
  const downloading = ref(false)
  const progress = ref(0)
  const checking = ref(false)
  const info = ref({
    needUpdate: false,
    forceUpdate: false,
    versionName: '',
    versionCode: 0,
    apkUrl: '',
    changelog: ''
  })

  const localVersion = readLocalVersion()

  async function check({ manual = false } = {}) {
    if (!isAppPlus()) {
      if (manual) {
        uni.showToast({ title: '仅 App 端支持更新检测', icon: 'none' })
      }
      return false
    }

    authStore.hydrate()
    if (!authStore.isLoggedIn.value) {
      if (manual) uni.showToast({ title: '请先登录', icon: 'none' })
      return false
    }
    if (checking.value || downloading.value || visible.value) return false
    checking.value = true
    try {
      const result = await checkAppVersion(localVersion.versionCode)
      info.value = result
      if (!result.needUpdate) {
        if (manual) uni.showToast({ title: '已是最新版本', icon: 'success' })
        return false
      }
      if (!result.forceUpdate && !manual && skippedOptionalThisSession) {
        return false
      }
      if (!result.apkUrl) {
        if (manual) uni.showToast({ title: '未配置安装包地址', icon: 'none' })
        return false
      }
      visible.value = true
      return true
    } catch (e) {
      if (manual) {
        uni.showToast({ title: e.message || '检查更新失败', icon: 'none' })
      }
      return false
    } finally {
      checking.value = false
    }
  }

  function later() {
    if (info.value.forceUpdate || downloading.value) return
    skippedOptionalThisSession = true
    visible.value = false
  }

  function installApk(filePath) {
    return new Promise((resolve, reject) => {
      if (!isAppPlus()) {
        reject(new Error('当前环境不支持安装'))
        return
      }
      plus.runtime.install(
        filePath,
        { force: false },
        () => resolve(),
        (err) => reject(new Error(err?.message || '安装失败，请允许安装未知应用'))
      )
    })
  }

  function confirmUpdate() {
    if (downloading.value) return
    const url = info.value.apkUrl
    if (!url) {
      uni.showToast({ title: '安装包地址无效', icon: 'none' })
      return
    }
    downloading.value = true
    progress.value = 0
    const task = uni.downloadFile({
      url,
      success: async (res) => {
        if (res.statusCode !== 200 || !res.tempFilePath) {
          downloading.value = false
          uni.showToast({ title: '下载失败', icon: 'none' })
          return
        }
        try {
          await installApk(resolveApkPath(res.tempFilePath))
          visible.value = false
        } catch (e) {
          uni.showToast({ title: e.message || '安装失败', icon: 'none' })
        } finally {
          downloading.value = false
        }
      },
      fail: () => {
        downloading.value = false
        uni.showToast({ title: '下载失败', icon: 'none' })
      }
    })
    if (task && typeof task.onProgressUpdate === 'function') {
      task.onProgressUpdate((e) => {
        progress.value = Math.min(100, Math.max(0, e.progress || 0))
      })
    }
  }

  return {
    visible,
    downloading,
    progress,
    checking,
    info,
    localVersion,
    check,
    later,
    confirmUpdate
  }
}
