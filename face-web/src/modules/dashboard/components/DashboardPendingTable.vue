<template>
  <el-card shadow="never" class="dashboard-panel">
    <template #header>
      <div class="dashboard-panel__header">
        <span class="dashboard-panel__title">待推送记录</span>
        <el-tag type="warning" effect="light" round>{{ records.length }} 条</el-tag>
      </div>
    </template>
    <el-empty v-if="!records.length" description="今日暂无待推送记录" :image-size="72" />
    <el-table v-else :data="records" size="small">
      <el-table-column label="类型" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.recordType === 'checkout' ? 'primary' : 'success'" size="small">{{ row.recordType === 'checkout' ? '签退' : '签到' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="学生" prop="studentName" min-width="100" />
      <el-table-column label="时间" prop="createTime" min-width="150" />
      <el-table-column label="操作" width="100" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="$emit('view', row)">去处理</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>
<script setup>
defineProps({ records: { type: Array, default: () => [] } })
defineEmits(['view'])
</script>
<style scoped lang="scss">
.dashboard-panel { border: 1px solid var(--el-border-color-lighter); margin-bottom: 16px; }
.dashboard-panel__header { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.dashboard-panel__title { font-size: 15px; font-weight: 700; color: #1e293b; }
</style>
