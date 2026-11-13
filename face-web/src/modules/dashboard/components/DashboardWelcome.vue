<template>
  <el-card shadow="never" class="dashboard-welcome">
    <div class="dashboard-welcome__main">
      <div>
        <div class="dashboard-welcome__title">你好，{{ nickName || '管理员' }}</div>
        <div class="dashboard-welcome__sub">{{ todayText }}</div>
      </div>
      <div class="dashboard-welcome__meta">
        <el-tag v-if="deptName" type="success" effect="light" round>{{ deptName }}</el-tag>
        <el-button type="primary" link icon="Refresh" @click="$emit('refresh')">刷新数据</el-button>
      </div>
    </div>
  </el-card>
</template>
<script setup>
defineProps({
  nickName: { type: String, default: '' },
  deptName: { type: String, default: '' },
  todayText: { type: String, default: '' }
})
defineEmits(['refresh'])
</script>
<style scoped lang="scss">
.dashboard-welcome { border: 1px solid var(--el-border-color-lighter); background: linear-gradient(135deg, #ecfdf5 0%, #ffffff 55%); }
.dashboard-welcome__main { display: flex; align-items: center; justify-content: space-between; gap: 16px; }
.dashboard-welcome__title { font-size: 22px; font-weight: 700; color: #1e293b; }
.dashboard-welcome__sub { margin-top: 8px; font-size: 14px; color: #64748b; }
.dashboard-welcome__meta { display: flex; align-items: center; gap: 12px; }
</style>
