<template>
  <el-card shadow="never" class="dashboard-panel">
    <template #header><span class="dashboard-panel__title">今日动态</span></template>
    <el-tabs v-model="activeTab">
      <el-tab-pane v-if="showCheckin" label="签到" name="checkin">
        <el-table :data="checkins" size="small" empty-text="今日暂无签到记录">
          <el-table-column label="学生" prop="studentName" min-width="100" />
          <el-table-column label="时间" prop="createTime" min-width="150" />
          <el-table-column label="推送" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="row.pushFlag === '1' ? 'success' : 'warning'" size="small">{{ row.pushFlag === '1' ? '已推送' : '未推送' }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane v-if="showCheckout" label="签退" name="checkout">
        <el-table :data="checkouts" size="small" empty-text="今日暂无签退记录">
          <el-table-column label="学生" prop="studentName" min-width="100" />
          <el-table-column label="时间" prop="createTime" min-width="150" />
          <el-table-column label="推送" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="row.pushFlag === '1' ? 'success' : 'warning'" size="small">{{ row.pushFlag === '1' ? '已推送' : '未推送' }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>
<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  checkins: { type: Array, default: () => [] },
  checkouts: { type: Array, default: () => [] },
  showCheckin: { type: Boolean, default: true },
  showCheckout: { type: Boolean, default: true }
})

const activeTab = ref(props.showCheckin ? 'checkin' : 'checkout')

watch(
  () => [props.showCheckin, props.showCheckout],
  ([checkin, checkout]) => {
    if (checkin) {
      activeTab.value = 'checkin'
    } else if (checkout) {
      activeTab.value = 'checkout'
    }
  },
  { immediate: true }
)
</script>
<style scoped lang="scss">
.dashboard-panel { border: 1px solid var(--el-border-color-lighter); margin-bottom: 16px; }
.dashboard-panel__title { font-size: 15px; font-weight: 700; color: #1e293b; }
</style>
