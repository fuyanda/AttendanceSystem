<template>
  <PageContainer>
    <NavPageHeader :title="pageTitle" show-back @back="goBack" />
    <PageSection card>
      <view v-if="loading" class="form-hint">加载中...</view>
      <template v-else>
        <FormField label="时段名称">
          <input v-model="form.name" class="app-input" placeholder="如：上午签到" />
        </FormField>
        <FormField label="开始时间">
          <picker mode="time" :value="form.startTime" @change="onStartTimeChange">
            <view class="app-input app-input--picker">{{ form.startTime || '请选择' }}</view>
          </picker>
        </FormField>
        <FormField label="结束时间">
          <picker mode="time" :value="form.endTime" @change="onEndTimeChange">
            <view class="app-input app-input--picker">{{ form.endTime || '请选择' }}</view>
          </picker>
        </FormField>
        <FormField label="启用状态">
          <view class="switch-row">
            <text>{{ enabled ? '启用' : '停用' }}</text>
            <switch :checked="enabled" color="#22c55e" @change="onStatusChange" />
          </view>
        </FormField>
        <AppButton :loading="saving" @click="submitForm">保存</AppButton>
      </template>
    </PageSection>
  </PageContainer>
</template>
<script setup>
import PageContainer from '@/shared/components/layout/PageContainer.vue'
import NavPageHeader from '@/shared/components/layout/NavPageHeader.vue'
import PageSection from '@/shared/components/layout/PageSection.vue'
import AppButton from '@/shared/components/ui/AppButton.vue'
import FormField from '@/shared/components/ui/FormField.vue'
import { usePunchSettingForm } from '@/modules/settings/composables/usePunchSettingForm.js'
const { loading, saving, form, enabled, pageTitle, onStartTimeChange, onEndTimeChange, submitForm, goBack } = usePunchSettingForm()
function onStatusChange(e) { enabled.value = !!e.detail.value }
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
.app-input { height: $input-height; padding: 0 $space-24; background: $color-bg-page; border-radius: $radius-md; border: 1px solid $color-border; line-height: $input-height; font-size: 28rpx; }
.app-input--picker { color: $color-text-primary; }
.switch-row { height: $input-height; padding: 0 $space-24; background: $color-bg-page; border-radius: $radius-md; border: 1px solid $color-border; display: flex; align-items: center; justify-content: space-between; font-size: 28rpx; color: $color-text-primary; }
.form-hint { text-align: center; color: $color-text-muted; padding: 48rpx 0; }
</style>
