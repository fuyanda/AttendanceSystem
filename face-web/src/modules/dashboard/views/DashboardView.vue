<template>
  <div class="app-container dashboard-view" v-loading="loading">
    <DashboardWelcome
      :nick-name="user.nickName"
      :dept-name="user.deptName"
      :today-text="todayText"
      @refresh="loadDashboard"
    />

    <DashboardStatGrid
      v-if="showStatGrid"
      :items="statItems"
    />

    <el-empty
      v-if="!loading && !hasDashboardData"
      description="当前账号暂无首页统计数据权限，请从左侧菜单或快捷入口进入可用功能"
      :image-size="96"
      class="dashboard-view__empty"
    />

    <el-row v-if="hasDashboardData" :gutter="16">
      <el-col :xs="24" :lg="16">
        <DashboardAttendanceChart v-if="showAttendanceChart" :chart-data="chartData" />
        <DashboardRecentList
          v-if="showRecentList"
          :checkins="recentCheckins"
          :checkouts="recentCheckouts"
          :show-checkin="access.checkin"
          :show-checkout="access.checkout"
        />
        <DashboardPendingTable
          v-if="showPendingTable"
          :records="pendingRecords"
          @view="handlePendingView"
        />
      </el-col>
      <el-col :xs="24" :lg="8">
        <DashboardQuickLinks
          v-if="quickLinks.length"
          :links="quickLinks"
          @navigate="goPath"
        />
        <DashboardPunchWindows
          v-if="access.punch"
          :windows="punchWindows"
          :active-window="activePunchWindow"
        />
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import DashboardWelcome from '@/modules/dashboard/components/DashboardWelcome.vue'
import DashboardStatGrid from '@/modules/dashboard/components/DashboardStatGrid.vue'
import DashboardQuickLinks from '@/modules/dashboard/components/DashboardQuickLinks.vue'
import DashboardPunchWindows from '@/modules/dashboard/components/DashboardPunchWindows.vue'
import DashboardAttendanceChart from '@/modules/dashboard/components/DashboardAttendanceChart.vue'
import DashboardRecentList from '@/modules/dashboard/components/DashboardRecentList.vue'
import DashboardPendingTable from '@/modules/dashboard/components/DashboardPendingTable.vue'
import { useDashboard } from '@/modules/dashboard/composables/useDashboard.js'

const {
  loading,
  user,
  access,
  statItems,
  todayText,
  punchWindows,
  activePunchWindow,
  chartData,
  recentCheckins,
  recentCheckouts,
  pendingRecords,
  quickLinks,
  showStatGrid,
  showAttendanceChart,
  showRecentList,
  showPendingTable,
  hasDashboardData,
  loadDashboard,
  goPath
} = useDashboard()

function handlePendingView(row) {
  const checkinPath = quickLinks.value.find((item) => item.label === '签到记录')?.path
  const checkoutPath = quickLinks.value.find((item) => item.label === '签退记录')?.path
  goPath(row.recordType === 'checkout' ? checkoutPath : checkinPath)
}
</script>

<style scoped lang="scss">
.dashboard-view {
  background: #f5f7fb;
}
.dashboard-view__empty {
  margin: 24px 0 8px;
  padding: 32px 16px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid var(--el-border-color-lighter);
}
</style>
