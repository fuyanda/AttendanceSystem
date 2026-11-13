<template>
  <PageContainer>
    <NavPageHeader title="设置" />
    <view class="settings-body">
      <view class="profile-hero">
        <view class="profile-hero__avatar">{{ avatarText }}</view>
        <view class="profile-hero__info">
          <view class="profile-hero__name">{{ displayName }}</view>
          <view class="profile-hero__dept">{{ deptName }}</view>
        </view>
      </view>

      <PageSection card title="账号与校区">
        <SettingsMenuItem icon="👤" tone="primary" title="个人信息" desc="昵称、手机号、邮箱" @click="goProfile" />
        <SettingsMenuItem icon="🔒" tone="blue" title="修改密码" desc="定期更新登录密码" @click="goPassword" />
        <SettingsMenuItem icon="⏱" tone="orange" title="打卡时间段" :desc="punchDesc" @click="goPunchSettings" />
      </PageSection>

      <PageSection card title="设备调试">
        <SettingsMenuItem
          icon="🔊"
          tone="blue"
          title="语音播报测试"
          :desc="speechTestDesc"
          @click="onTestSpeech"
        />
        <SettingsMenuItem
          icon="👁"
          tone="orange"
          title="本地人脸检测测试"
          :desc="faceDetectDesc"
          @click="onTestFaceDetect"
        />
      </PageSection>

      <PageSection card title="关于">
        <SettingsMenuItem
          icon="⬆"
          tone="blue"
          title="检查更新"
          :desc="updateDesc"
          @click="onCheckUpdate"
        />
      </PageSection>

      <view class="settings-footer">
        <AppButton type="danger" @click="handleLogout">退出登录</AppButton>
      </view>
    </view>
    <AppTabBar current="settings" />
    <UpdateDialog
      :visible="updateVisible"
      :info="updateInfo"
      :downloading="updateDownloading"
      :progress="updateProgress"
      @later="onUpdateLater"
      @confirm="onUpdateConfirm"
    />
  </PageContainer>
</template>
<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import PageContainer from '@/shared/components/layout/PageContainer.vue'
import NavPageHeader from '@/shared/components/layout/NavPageHeader.vue'
import PageSection from '@/shared/components/layout/PageSection.vue'
import AppButton from '@/shared/components/ui/AppButton.vue'
import AppTabBar from '@/shared/components/layout/AppTabBar.vue'
import SettingsMenuItem from '@/modules/settings/components/SettingsMenuItem.vue'
import UpdateDialog from '@/modules/app-update/components/UpdateDialog.vue'
import { useServerConfig } from '@/modules/settings/composables/useServerConfig.js'
import { useAppUpdate } from '@/modules/app-update/composables/useAppUpdate.js'
import { useAuthStore } from '@/stores/auth.js'
import { testSpeech } from '@/shared/utils/speech.js'
import { detectLocalFace } from '@/modules/punch-live/utils/localFaceDetect.js'
import { isAndroidApp } from '@/shared/utils/platform.js'

const authStore = useAuthStore()
const { user, deptName, handleLogout } = useServerConfig()
const {
  visible: updateVisible,
  downloading: updateDownloading,
  progress: updateProgress,
  info: updateInfo,
  localVersion,
  check: checkAppUpdate,
  later: onUpdateLater,
  confirmUpdate: onUpdateConfirm
} = useAppUpdate()

onShow(() => {
  authStore.hydrate()
})

const displayName = computed(() => user.value?.nickName || user.value?.userName || '未登录')
const avatarText = computed(() => (displayName.value || '?').slice(0, 1))
const punchDesc = computed(() =>
  deptName.value ? `${deptName.value} 时段管理` : '管理本校区打卡时段'
)
const updateDesc = computed(() =>
  localVersion.versionName ? `当前版本 ${localVersion.versionName}` : '检查教师端最新版本'
)

const speechTesting = ref(false)
const speechTestResult = ref('')
const speechTestDesc = computed(() => {
  if (speechTesting.value) return '正在播报测试语音…'
  if (speechTestResult.value) return speechTestResult.value
  return '点击播报「张三签到成功」，确认 TTS 可用'
})

const faceDetectTesting = ref(false)
const faceDetectResult = ref('')
const faceDetectDesc = computed(() => {
  if (faceDetectTesting.value) return '正在本地检测…'
  if (faceDetectResult.value) return faceDetectResult.value
  return '选一张照片，验证 Android ML Kit 是否可用（需自定义基座）'
})

function goProfile() {
  uni.navigateTo({ url: '/pages/settings/profile' })
}

function goPassword() {
  uni.navigateTo({ url: '/pages/settings/password' })
}

function goPunchSettings() {
  uni.navigateTo({ url: '/pages/settings/punch' })
}

function onCheckUpdate() {
  checkAppUpdate({ manual: true })
}

async function onTestSpeech() {
  if (speechTesting.value) return
  speechTesting.value = true
  speechTestResult.value = ''
  try {
    const result = await testSpeech()
    speechTestResult.value = result.message
    uni.showToast({
      title: result.ok ? '已发起播报' : result.message,
      icon: 'none',
      duration: 3000
    })
  } finally {
    speechTesting.value = false
  }
}

function chooseImagePath() {
  return new Promise((resolve, reject) => {
    uni.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const path = (res.tempFilePaths && res.tempFilePaths[0]) || ''
        if (!path) reject(new Error('未选择图片'))
        else resolve(path)
      },
      fail: (err) => reject(new Error((err && err.errMsg) || '选图失败'))
    })
  })
}

async function onTestFaceDetect() {
  if (faceDetectTesting.value) return
  if (!isAndroidApp()) {
    faceDetectResult.value = '仅 Android App 支持本地检脸'
    uni.showToast({ title: faceDetectResult.value, icon: 'none' })
    return
  }
  faceDetectTesting.value = true
  try {
    const path = await chooseImagePath()
    const result = await detectLocalFace(path)
    if (result.error && result.skipped) {
      faceDetectResult.value = `插件未生效：${result.error}`
    } else if (result.skipped) {
      faceDetectResult.value = '已跳过（非 Android 或未启用）'
    } else {
      const ratio = (result.largestAreaRatio * 100).toFixed(1)
      faceDetectResult.value = result.pass
        ? `通过：检测到 ${result.faceCount} 张脸，最大占比 ${ratio}%（${result.elapsedMs}ms）`
        : `未通过：hasFace=${result.hasFace}，占比 ${ratio}%（${result.elapsedMs}ms）`
    }
    uni.showToast({ title: result.pass ? '本地检测到人脸' : '未通过门禁', icon: 'none', duration: 2500 })
  } catch (e) {
    faceDetectResult.value = (e && e.message) || '检测失败'
    uni.showToast({ title: faceDetectResult.value, icon: 'none' })
  } finally {
    faceDetectTesting.value = false
  }
}
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
.settings-body {
  padding-bottom: calc($tabbar-height + $space-32 + env(safe-area-inset-bottom));
}
.profile-hero {
  margin: $space-16 $space-32 $space-8;
  padding: $space-24 $space-32;
  border-radius: $radius-lg;
  background: linear-gradient(135deg, $color-primary-soft 0%, $color-bg-card 100%);
  border: 1px solid $color-primary-border;
  box-shadow: $shadow-card;
  display: flex;
  align-items: center;
  gap: $space-24;
}
.profile-hero__avatar {
  width: 104rpx;
  height: 104rpx;
  border-radius: 50%;
  background: #fff;
  color: $color-primary;
  font-size: 44rpx;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: $shadow-card;
}
.profile-hero__name {
  font-size: 34rpx;
  font-weight: 700;
  color: $color-text-primary;
}
.profile-hero__dept {
  margin-top: 10rpx;
  font-size: 24rpx;
  color: $color-text-secondary;
}
.settings-footer {
  margin: 0 $space-32 $space-24;
}
</style>
