<template>
  <PageContainer variant="gradient">
    <PageSection card>
      <view class="login-title">打卡终端</view>
      <view class="login-subtitle">请使用后台账号登录</view>

      <FormField label="账号">
        <input v-model="form.username" class="app-input" placeholder="请输入账号" />
      </FormField>
      <FormField label="密码">
        <input v-model="form.password" class="app-input" password placeholder="请输入密码" />
      </FormField>
      <FormField label="验证码">
        <view class="captcha-row">
          <input v-model="form.code" class="app-input captcha-row__input" placeholder="请输入验证码" />
          <image v-if="captchaImg" class="captcha-row__img" :src="captchaImg" mode="aspectFit" @click="loadCaptcha" />
          <view v-else class="captcha-row__placeholder" @click="loadCaptcha">点击刷新</view>
        </view>
      </FormField>

      <view class="remember-row">
        <switch :checked="form.rememberMe" color="#22c55e" @change="onRememberChange" />
        <text>记住账号</text>
      </view>

      <AppButton :loading="loading" @click="submitLogin">登录</AppButton>
    </PageSection>
  </PageContainer>
</template>
<script setup>
import { useLogin } from '@/modules/auth/composables/useLogin.js'
const {
  loading, captchaImg, form, onRememberChange,
  loadCaptcha, submitLogin
} = useLogin()
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
.login-title { font-size: 44rpx; font-weight: 700; color: $color-text-primary; }
.login-subtitle { margin: 12rpx 0 40rpx; color: $color-text-secondary; font-size: 28rpx; }
.app-input {
  height: $input-height;
  padding: 0 $space-24;
  background: $color-bg-page;
  border-radius: $radius-md;
  border: 1px solid $color-border;
}
.captcha-row { display: flex; gap: $space-16; align-items: center; }
.captcha-row__input { flex: 1; }
.captcha-row__img, .captcha-row__placeholder {
  width: 220rpx; height: $input-height; border-radius: $radius-md; background: #f1f5f9;
}
.captcha-row__placeholder {
  display: flex; align-items: center; justify-content: center;
  color: $color-text-secondary; font-size: 24rpx;
}
.remember-row { display: flex; align-items: center; gap: 12rpx; margin-bottom: $space-32; color: $color-text-body; font-size: 26rpx; }
</style>
