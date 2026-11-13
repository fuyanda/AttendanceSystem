<template>
  <button
    class="app-button"
    :class="[`app-button--${type}`, { 'app-button--block': block }]"
    :loading="loading"
    :disabled="disabled || loading"
    @click="$emit('click')"
  >
    <slot />
  </button>
</template>
<script setup>
defineProps({
  type: { type: String, default: 'primary' },
  loading: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false },
  block: { type: Boolean, default: true }
})
defineEmits(['click'])
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
.app-button {
  border-radius: $radius-md;
  font-size: 30rpx;
  font-weight: 600;
  margin-top: $space-16;
}
.app-button::after { border: none; }
.app-button--block { width: 100%; }
.app-button--primary {
  background: linear-gradient(135deg, $color-primary 0%, $color-primary-dark 100%);
  color: #fff;
  box-shadow: 0 8rpx 22rpx rgba(34, 197, 94, 0.22);
}
.app-button--primary[disabled] {
  background: $color-primary-soft;
  color: $color-primary-dark;
  border: 1px solid $color-primary-border;
  box-shadow: none;
  opacity: 1;
}
.app-button--ghost {
  background: $color-bg-card;
  color: $color-primary;
  border: 1px solid $color-primary-border;
}
.app-button--success { background: $color-success; color: #fff; }
.app-button--danger {
  background: $color-danger-soft;
  color: $color-danger;
  border: 1px solid rgba(225, 29, 72, 0.15);
}
.app-button--secondary { background: $color-bg-muted; color: #fff; }
</style>
