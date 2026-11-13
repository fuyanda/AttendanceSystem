<template>
  <el-row :gutter="16" class="dashboard-stat-grid">
    <el-col v-for="item in items" :key="item.key" :xs="12" :sm="8" :lg="4">
      <el-card shadow="never" class="dashboard-stat-card" :class="`dashboard-stat-card--${item.tone}`">
        <div class="dashboard-stat-card__label">{{ item.label }}</div>
        <div class="dashboard-stat-card__value">{{ item.value }}</div>
        <div v-if="item.hint" class="dashboard-stat-card__hint">{{ item.hint }}</div>
      </el-card>
    </el-col>
  </el-row>
</template>
<script setup>
defineProps({
  items: { type: Array, default: () => [] }
})
</script>
<style scoped lang="scss">
.dashboard-stat-grid { margin-bottom: 16px; }
.dashboard-stat-card { margin-bottom: 16px; border: 1px solid var(--el-border-color-lighter); }
.dashboard-stat-card__label { font-size: 13px; color: #64748b; }
.dashboard-stat-card__value { margin-top: 8px; font-size: 28px; font-weight: 700; line-height: 1.2; color: #1e293b; }
.dashboard-stat-card__hint { margin-top: 6px; font-size: 12px; color: #94a3b8; }
.dashboard-stat-card--success .dashboard-stat-card__value { color: #16a34a; }
.dashboard-stat-card--primary .dashboard-stat-card__value { color: #2563eb; }
.dashboard-stat-card--warning .dashboard-stat-card__value { color: #f97316; }
.dashboard-stat-card--info .dashboard-stat-card__value { color: #0ea5e9; }
</style>
