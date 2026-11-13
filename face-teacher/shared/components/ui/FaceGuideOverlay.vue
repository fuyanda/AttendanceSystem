<template>
  <!-- 实时相机上须用 cover-view，且仅与 camera 同父节点 -->
  <!-- #ifdef APP-PLUS || MP-WEIXIN -->
  <cover-view v-if="native" class="face-guide">
    <cover-view class="face-guide__oval" />
    <cover-view class="face-guide__label">{{ label }}</cover-view>
  </cover-view>
  <!-- #endif -->

  <view v-if="!native" class="face-guide" :class="[`face-guide--${mode}`]">
    <view class="face-guide__frame">
      <view class="face-guide__corner face-guide__corner--tl" />
      <view class="face-guide__corner face-guide__corner--tr" />
      <view class="face-guide__corner face-guide__corner--bl" />
      <view class="face-guide__corner face-guide__corner--br" />
      <view class="face-guide__oval">
        <view class="face-guide__silhouette">
          <view class="face-guide__head" />
          <view class="face-guide__shoulders" />
        </view>
      </view>
    </view>
    <view class="face-guide__label">{{ label }}</view>
    <view v-if="mode === 'capture'" class="face-guide__tips">
      <view v-for="item in captureTips" :key="item" class="face-guide__tip-item">{{ item }}</view>
    </view>
  </view>
</template>
<script setup>
defineProps({
  native: { type: Boolean, default: false },
  mode: { type: String, default: 'capture' },
  label: { type: String, default: '请将面部置于框内' }
})

const captureTips = ['正脸居中', '光线充足', '无口罩遮挡']
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';

.face-guide {
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  pointer-events: none;
  z-index: 2;
}

.face-guide__frame {
  position: relative;
  width: 440rpx;
  height: 540rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.face-guide__oval {
  width: 400rpx;
  height: 500rpx;
  border: 4rpx dashed rgba(34, 197, 94, 0.95);
  border-radius: 50%;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(34, 197, 94, 0.06);
}

.face-guide__silhouette {
  display: flex;
  flex-direction: column;
  align-items: center;
  opacity: 0.45;
}

.face-guide__head {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  border: 3rpx solid rgba(255, 255, 255, 0.7);
  background: rgba(255, 255, 255, 0.08);
}

.face-guide__shoulders {
  width: 200rpx;
  height: 88rpx;
  margin-top: -12rpx;
  border: 3rpx solid rgba(255, 255, 255, 0.7);
  border-bottom: none;
  border-radius: 100rpx 100rpx 0 0;
  background: rgba(255, 255, 255, 0.05);
}

.face-guide__corner {
  position: absolute;
  width: 36rpx;
  height: 36rpx;
  border-color: rgba(34, 197, 94, 0.9);
  border-style: solid;
}
.face-guide__corner--tl {
  left: 0;
  top: 0;
  border-width: 6rpx 0 0 6rpx;
  border-radius: 8rpx 0 0 0;
}
.face-guide__corner--tr {
  right: 0;
  top: 0;
  border-width: 6rpx 6rpx 0 0;
  border-radius: 0 8rpx 0 0;
}
.face-guide__corner--bl {
  left: 0;
  bottom: 0;
  border-width: 0 0 6rpx 6rpx;
  border-radius: 0 0 0 8rpx;
}
.face-guide__corner--br {
  right: 0;
  bottom: 0;
  border-width: 0 6rpx 6rpx 0;
  border-radius: 0 0 8rpx 0;
}

.face-guide__label {
  margin-top: $space-24;
  color: #ffffff;
  font-size: 28rpx;
  font-weight: 600;
  text-align: center;
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.5);
}

.face-guide__tips {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: $space-8 $space-16;
  margin-top: $space-16;
  padding: 0 $space-24;
}

.face-guide__tip-item {
  padding: 6rpx 18rpx;
  border-radius: 999rpx;
  background: rgba(15, 23, 42, 0.55);
  border: 1rpx solid rgba(255, 255, 255, 0.12);
  color: #e2e8f0;
  font-size: 22rpx;
}

.face-guide--preview .face-guide__oval {
  border-style: solid;
  border-color: rgba(250, 204, 21, 0.95);
  background: rgba(250, 204, 21, 0.08);
}

.face-guide--preview .face-guide__corner {
  border-color: rgba(250, 204, 21, 0.9);
}

.face-guide--preview .face-guide__silhouette {
  display: none;
}

.face-guide--preview .face-guide__label {
  color: #fde68a;
}
</style>
