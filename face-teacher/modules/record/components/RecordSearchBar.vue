<template>
  <SearchBar
    class="record-search"
    :class="{ 'record-search--compact': compact }"
    :model-value="modelValue"
    :placeholder="placeholder"
    @update:model-value="$emit('update:modelValue', $event)"
    @search="$emit('search', $event)"
  />
</template>
<script setup>
import SearchBar from '@/shared/components/ui/SearchBar.vue'

defineProps({
  modelValue: { type: String, default: '' },
  placeholder: { type: String, default: '搜索学生姓名...' },
  compact: { type: Boolean, default: false }
})
defineEmits(['update:modelValue', 'search'])
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
.record-search--compact {
  margin: 0 !important;
  flex: 1;
  min-width: 0;
  height: 68rpx !important;
}
</style>
