<template>
  <el-card shadow="never" class="dashboard-panel">
    <template #header>
      <div class="dashboard-panel__header">
        <span class="dashboard-panel__title">今日打卡时段</span>
        <el-tag v-if="activeWindow" type="success" effect="light" round>当前：{{ activeWindow.name }}</el-tag>
        <el-tag v-else type="info" effect="light" round>当前不在打卡时段</el-tag>
      </div>
    </template>
    <el-empty v-if="!windows.length" description="暂无启用的打卡时段" :image-size="72" />
    <div v-else class="dashboard-punch-list">
      <div v-for="item in windows" :key="item.id" class="dashboard-punch-item" :class="{ 'dashboard-punch-item--active': item.active }">
        <div class="dashboard-punch-item__name">{{ item.name }}</div>
        <div class="dashboard-punch-item__time">{{ item.startTime }} - {{ item.endTime }}</div>
      </div>
    </div>
  </el-card>
</template>
<script setup>
defineProps({ windows: { type: Array, default: () => [] }, activeWindow: { type: Object, default: null } })
</script>
<style scoped lang="scss">
.dashboard-panel { border: 1px solid var(--el-border-color-lighter); margin-bottom: 16px; }
.dashboard-panel__header { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.dashboard-panel__title { font-size: 15px; font-weight: 700; color: #1e293b; }
.dashboard-punch-list { display: flex; flex-direction: column; gap: 10px; }
.dashboard-punch-item { display: flex; align-items: center; justify-content: space-between; padding: 12px 14px; border-radius: 10px; background: #f8fafc; border: 1px solid #e2e8f0; }
.dashboard-punch-item--active { background: #ecfdf5; border-color: #bbf7d0; }
.dashboard-punch-item__name { font-size: 14px; font-weight: 600; color: #1e293b; }
.dashboard-punch-item__time { font-size: 13px; color: #64748b; }
</style>
