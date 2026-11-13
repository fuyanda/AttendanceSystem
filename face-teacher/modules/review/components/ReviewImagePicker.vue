<template>
  <view class="img-picker">
    <view v-for="(path, idx) in modelValue" :key="path + idx" class="img-picker__item">
      <image class="img-picker__thumb" :src="toUrl(path)" mode="aspectFill" @click="preview(idx)" />
      <view v-if="!disabled" class="img-picker__remove" @click.stop="removeAt(idx)">×</view>
    </view>
    <view
      v-if="!disabled && modelValue.length < limit"
      class="img-picker__add"
      hover-class="img-picker__add--hover"
      @click="pick"
    >
      <text class="img-picker__add-icon">＋</text>
      <text class="img-picker__add-text">添加图片</text>
      <text class="img-picker__add-count">{{ modelValue.length }}/{{ limit }}</text>
    </view>
  </view>
</template>
<script setup>
import { uploadFile } from '@/shared/http/upload.js'
import { getHost } from '@/shared/config/host.js'

const props = defineProps({
  modelValue: { type: Array, default: () => [] },
  limit: { type: Number, default: 6 },
  disabled: { type: Boolean, default: false }
})
const emit = defineEmits(['update:modelValue'])

function toUrl(path) {
  if (!path) return ''
  if (path.startsWith('http://') || path.startsWith('https://')) return path
  return `${getHost()}${path}`
}

function preview(idx) {
  const urls = props.modelValue.map(toUrl)
  uni.previewImage({ urls, current: urls[idx] })
}

function removeAt(idx) {
  const next = props.modelValue.slice()
  next.splice(idx, 1)
  emit('update:modelValue', next)
}

function pick() {
  if (props.disabled) return
  const remain = props.limit - props.modelValue.length
  if (remain <= 0) return
  uni.chooseImage({
    count: remain,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      const paths = res.tempFilePaths || []
      const uploaded = []
      uni.showLoading({ title: '上传中', mask: true })
      try {
        for (const p of paths) {
          const fileName = await uploadFile(p)
          uploaded.push(fileName)
        }
        emit('update:modelValue', props.modelValue.concat(uploaded))
      } catch (e) {
        uni.showToast({ title: e.message || '上传失败', icon: 'none' })
      } finally {
        uni.hideLoading()
      }
    }
  })
}
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
.img-picker {
  display: flex;
  flex-wrap: wrap;
  gap: $space-16;
  margin-top: $space-16;
}
.img-picker__item,
.img-picker__add {
  width: 168rpx;
  height: 168rpx;
  border-radius: $radius-md;
  position: relative;
  overflow: hidden;
}
.img-picker__thumb {
  width: 100%;
  height: 100%;
  background: $color-bg-inset;
}
.img-picker__remove {
  position: absolute;
  top: 8rpx;
  right: 8rpx;
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  background: rgba(15, 23, 42, 0.7);
  color: #fff;
  font-size: 28rpx;
  line-height: 40rpx;
  text-align: center;
}
.img-picker__add {
  border: 1px dashed $color-primary-border;
  background: linear-gradient(180deg, #f8fffb 0%, $color-bg-inset 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4rpx;
}
.img-picker__add--hover { opacity: 0.88; }
.img-picker__add-icon {
  font-size: 40rpx;
  color: $color-primary;
  line-height: 1;
  font-weight: 600;
}
.img-picker__add-text {
  font-size: 22rpx;
  color: $color-primary-dark;
  font-weight: 600;
}
.img-picker__add-count {
  font-size: 20rpx;
  color: $color-text-muted;
}
</style>
