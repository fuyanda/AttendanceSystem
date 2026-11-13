<template>
  <PageContainer :style="fillStyle">
    <AttendHeader :dept-name="deptName" />
    <RecordTabs :model-value="activeTab" @update:model-value="switchTab" />
    <view class="home-toolbar">
      <RecordDateFilter
        compact
        :model-value="queryDate"
        @change="onQueryDateChange"
        @reset="resetQueryDate"
      />
      <RecordSearchBar
        compact
        :model-value="keyword"
        :placeholder="searchPlaceholder"
        @update:model-value="onSearchInput"
        @search="onSearch"
      />
    </view>
    <RecordStatsCard
      :stats="stats"
      :dept-stats="deptStats"
      :stats-loading="statsLoading"
      :unsigned-clickable="unsignedClickable"
      @layout-change="onStatsLayoutChange"
      @unsigned-click="goUnsignedList"
    />
    <RecordList
      ref="recordListRef"
      class="home-record-list"
      :title="sectionTitle"
      :campus-groups="campusGroups"
      :search-records="searchRecords"
      :loading="loading"
      :campus-loading="campusLoading"
      :is-searching="isSearching"
      :is-campus-collapsed="isCampusCollapsed"
      :re-pushing-id="rePushingId"
      @load-more="loadMore"
      @re-push="handleRePush"
      @open="openClassReview"
      @toggle-campus="toggleCampus"
      @campus-load-more="loadCampusRecords"
    />
    <CheckoutActionBar
      v-if="activeTab === 'checkout'"
      inline
      :punch-label="punchLabel"
      @manual="goManualCheckout"
      @punch="startPunch"
    />
    <CheckinActionBar
      v-else
      inline
      :punch-label="punchLabel"
      @live="goPunchLive"
      @punch="startPunch"
    />
    <view class="home-tabbar-spacer" />
    <AppTabBar current="home" />
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
import { onShow } from '@dcloudio/uni-app'
import { ref, watch } from 'vue'
import AttendHeader from '@/modules/record/components/AttendHeader.vue'
import RecordTabs from '@/modules/record/components/RecordTabs.vue'
import RecordDateFilter from '@/modules/record/components/RecordDateFilter.vue'
import RecordStatsCard from '@/modules/record/components/RecordStatsCard.vue'
import RecordSearchBar from '@/modules/record/components/RecordSearchBar.vue'
import RecordList from '@/modules/record/components/RecordList.vue'
import CheckinActionBar from '@/modules/record/components/CheckinActionBar.vue'
import CheckoutActionBar from '@/modules/record/components/CheckoutActionBar.vue'
import AppTabBar from '@/shared/components/layout/AppTabBar.vue'
import UpdateDialog from '@/modules/app-update/components/UpdateDialog.vue'
import { pageFillStyle } from '@/shared/composables/useLayoutInsets.js'
import { useRecordList } from '@/modules/record/composables/useRecordList.js'
import { useAppUpdate } from '@/modules/app-update/composables/useAppUpdate.js'

const fillStyle = pageFillStyle()

const {
  activeTab,
  campusGroups,
  campusLoading,
  searchRecords,
  isSearching,
  keyword,
  queryDate,
  unsignedClickable,
  loading,
  stats,
  deptStats,
  statsLoading,
  deptName,
  sectionTitle,
  searchPlaceholder,
  punchLabel,
  rePushingId,
  switchTab,
  loadMore,
  onSearch,
  onSearchInput,
  onQueryDateChange,
  resetQueryDate,
  loadCampusRecords,
  isCampusCollapsed,
  toggleCampus,
  startPunch,
  goPunchLive,
  goManualCheckout,
  goManualCheckin,
  goUnsignedList,
  handleRePush,
  openClassReview
} = useRecordList()

const {
  visible: updateVisible,
  downloading: updateDownloading,
  progress: updateProgress,
  info: updateInfo,
  check: checkAppUpdate,
  later: onUpdateLater,
  confirmUpdate: onUpdateConfirm
} = useAppUpdate()

const recordListRef = ref(null)

onShow(() => {
  setTimeout(() => checkAppUpdate({ manual: false }), 800)
})

function onStatsLayoutChange() {
  recordListRef.value?.remeasure?.()
  setTimeout(() => recordListRef.value?.remeasure?.(), 320)
}

watch([activeTab, statsLoading], () => {
  setTimeout(() => recordListRef.value?.remeasure?.(), 80)
  setTimeout(() => recordListRef.value?.remeasure?.(), 320)
})
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';

.home-record-list {
  flex: 1;
  min-height: 0;
}

.home-toolbar {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin: 0 $space-32 12rpx;
  flex-shrink: 0;
}

.home-tabbar-spacer {
  flex-shrink: 0;
  height: calc(#{$tabbar-height} + env(safe-area-inset-bottom));
}

:deep(.page-container) {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-sizing: border-box;
}
</style>
