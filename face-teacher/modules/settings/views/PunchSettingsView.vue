<template>
  <PageContainer>
    <NavPageHeader title="打卡时间段" show-back @back="goBack" />
    <view class="punch-page">
      <view class="punch-campus">
        <view class="punch-campus__label">当前校区</view>
        <view class="punch-campus__name">{{ deptName }}</view>
        <view class="punch-campus__desc">仅管理本校区打卡时段</view>
      </view>
      <PageSection card title="时段列表">
        <PunchSettingsList :settings="settings" :loading="loading" @edit="goEdit" @delete="handleDelete" />
      </PageSection>
      <view class="punch-page__footer">
        <AppButton @click="goCreate">新增时段</AppButton>
      </view>
    </view>
  </PageContainer>
</template>
<script setup>
import PageContainer from '@/shared/components/layout/PageContainer.vue'
import NavPageHeader from '@/shared/components/layout/NavPageHeader.vue'
import PageSection from '@/shared/components/layout/PageSection.vue'
import AppButton from '@/shared/components/ui/AppButton.vue'
import PunchSettingsList from '@/modules/settings/components/PunchSettingsList.vue'
import { usePunchSettings } from '@/modules/settings/composables/usePunchSettings.js'
const { loading, settings, deptName, goBack, goCreate, goEdit, handleDelete } = usePunchSettings()
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
.punch-page {
  padding-bottom: calc($space-32 + env(safe-area-inset-bottom));
}
.punch-campus {
  margin: $space-24 $space-32 0;
  padding: $space-24 $space-32;
  border-radius: $radius-lg;
  background: linear-gradient(135deg, $color-primary-soft 0%, $color-bg-card 100%);
  border: 1px solid $color-primary-border;
}
.punch-campus__label {
  font-size: 22rpx;
  color: $color-text-muted;
}
.punch-campus__name {
  margin-top: 8rpx;
  font-size: 34rpx;
  font-weight: 700;
  color: $color-text-primary;
}
.punch-campus__desc {
  margin-top: 8rpx;
  font-size: 24rpx;
  color: $color-text-secondary;
}
.punch-page__footer {
  margin: 0 $space-32;
}
</style>
