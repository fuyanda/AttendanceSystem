<template>
  <el-card shadow="never" class="dashboard-panel">
    <template #header><span class="dashboard-panel__title">快捷入口</span></template>
    <div class="dashboard-quick-links">
      <el-button v-for="item in links" :key="item.label" class="dashboard-quick-links__item" :disabled="!item.path" @click="$emit('navigate', item.path)">
        <el-icon><component :is="iconMap[item.icon]" /></el-icon>
        <span>{{ item.label }}</span>
      </el-button>
    </div>
  </el-card>
</template>
<script setup>
import { CircleCheck, Clock, SwitchButton, User } from '@element-plus/icons-vue'
const iconMap = { User, CircleCheck, SwitchButton, Clock }
defineProps({ links: { type: Array, default: () => [] } })
defineEmits(['navigate'])
</script>
<style scoped lang="scss">
.dashboard-panel { border: 1px solid var(--el-border-color-lighter); margin-bottom: 16px; }
.dashboard-panel__title { font-size: 15px; font-weight: 700; color: #1e293b; }
.dashboard-quick-links { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12px; }
.dashboard-quick-links__item { height: 44px; margin: 0; justify-content: flex-start; }
</style>
