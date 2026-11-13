<template>
  <PageContainer :style="fillStyle">
    <NavPageHeader title="手动签到" show-back @back="goBack" />
    <ManualCheckinCampusPicker
      v-model="deptId"
      :options="deptOptions"
      @change="onCampusChange"
    />
    <SearchBar
      :model-value="keyword"
      placeholder="搜索学生姓名..."
      @update:model-value="onSearchInput"
      @search="onSearch"
    />
    <ManualCheckinList
      :candidates="candidates"
      :loading="loading || campusLoading"
      :submitting="submitting"
      :all-selected="allSelected"
      :campus-label="deptLabel"
      :is-selected="isSelected"
      @toggle="toggleSelect"
      @toggle-all="toggleSelectAll"
    />
    <view class="manual-footer">
      <AppButton
        type="primary"
        :loading="submitting"
        :disabled="!selectedCount"
        @click="confirmCheckin"
      >
        {{ submitText }}
      </AppButton>
    </view>
  </PageContainer>
</template>
<script setup>
import PageContainer from '@/shared/components/layout/PageContainer.vue'
import NavPageHeader from '@/shared/components/layout/NavPageHeader.vue'
import SearchBar from '@/shared/components/ui/SearchBar.vue'
import AppButton from '@/shared/components/ui/AppButton.vue'
import ManualCheckinCampusPicker from '@/modules/checkin/components/ManualCheckinCampusPicker.vue'
import ManualCheckinList from '@/modules/checkin/components/ManualCheckinList.vue'
import { pageFillStyle } from '@/shared/composables/useLayoutInsets.js'
import { useManualCheckin } from '@/modules/checkin/composables/useManualCheckin.js'

const fillStyle = pageFillStyle()

const {
  candidates,
  keyword,
  deptId,
  deptOptions,
  deptLabel,
  campusLoading,
  loading,
  submitting,
  selectedCount,
  allSelected,
  submitText,
  isSelected,
  toggleSelect,
  toggleSelectAll,
  onCampusChange,
  onSearch,
  onSearchInput,
  goBack,
  confirmCheckin
} = useManualCheckin()
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
:deep(.page-container) {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-sizing: border-box;
}
.manual-footer {
  position: fixed;
  left: $space-32;
  right: $space-32;
  bottom: calc(24rpx + env(safe-area-inset-bottom));
  z-index: 20;
}
.manual-footer :deep(.app-button) {
  margin-top: 0;
  height: 92rpx;
  line-height: 92rpx;
  border-radius: 999rpx;
  font-size: 30rpx;
}
.manual-footer :deep(.app-button--primary) {
  box-shadow: $shadow-float;
}
.manual-footer :deep(.app-button--primary[disabled]) {
  background: $color-primary-soft;
  color: $color-primary-dark;
  border: 1px solid $color-primary-border;
  box-shadow: none;
}
</style>
