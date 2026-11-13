<template>
  <view class="native-camera-slot" :style="slotStyle">
    <!-- #ifdef APP-PLUS || MP-WEIXIN -->
    <camera
      v-if="cameraMounted"
      v-show="showLive"
      :id="cameraId"
      class="native-camera-slot__camera"
      device-position="front"
      flash="off"
      @initdone="onInitDone"
      @error="onError"
    />
    <cover-view v-if="cameraMounted" v-show="showLive" class="native-camera-slot__guide">
      <cover-view class="native-camera-slot__oval" />
      <cover-view class="native-camera-slot__label">请将面部置于框内</cover-view>
    </cover-view>
    <!-- #endif -->

    <image
      v-show="!!previewPath"
      class="native-camera-slot__preview"
      :src="previewPath"
      mode="aspectFill"
    />
    <view v-show="!!previewPath" class="native-camera-slot__preview-frame">
      <view class="native-camera-slot__preview-oval" />
      <view class="native-camera-slot__preview-label">请确认面部在框内且清晰</view>
    </view>

    <view v-show="!cameraMounted && !previewPath" class="native-camera-slot__waiting">
      <view class="native-camera-slot__waiting-text">{{ waitingText }}</view>
    </view>
  </view>
</template>
<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  previewPath: { type: String, default: '' },
  cameraId: { type: String, default: 'faceCamera' },
  enable: { type: Boolean, default: false },
  height: { type: String, default: '680rpx' },
  waitingText: { type: String, default: '正在启动相机...' }
})

const emit = defineEmits(['init-done', 'camera-error'])

const cameraMounted = ref(false)
const showLive = computed(() => props.enable && !props.previewPath)
const slotStyle = computed(() => ({ height: props.height }))

watch(
  () => props.enable,
  (val) => {
    if (val && !cameraMounted.value) {
      setTimeout(() => {
        cameraMounted.value = true
      }, 400)
    }
  },
  { immediate: true }
)

function onInitDone(e) {
  emit('init-done', e)
}

function onError(e) {
  emit('camera-error', e)
}
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
.native-camera-slot {
  position: relative;
  width: calc(100% - 48rpx);
  margin: 0 $space-24;
  background: #111827;
  overflow: hidden;
  box-sizing: border-box;
}
.native-camera-slot__camera,
.native-camera-slot__preview {
  width: 100%;
  height: 100%;
  display: block;
}
.native-camera-slot__preview {
  border-radius: $radius-lg;
}
.native-camera-slot__waiting {
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, #1e293b 0%, #0f172a 100%);
  border-radius: $radius-lg;
}
.native-camera-slot__waiting-text {
  color: #94a3b8;
  font-size: 28rpx;
}
.native-camera-slot__preview-frame {
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
}
.native-camera-slot__preview-oval {
  width: 400rpx;
  height: 500rpx;
  border: 4rpx solid rgba(250, 204, 21, 0.95);
  border-radius: 50%;
  background: rgba(250, 204, 21, 0.08);
}
.native-camera-slot__preview-label {
  margin-top: $space-24;
  color: #fde68a;
  font-size: 26rpx;
  text-align: center;
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.6);
}
</style>
<style lang="scss">
.native-camera-slot__guide {
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.native-camera-slot__oval {
  width: 400rpx;
  height: 500rpx;
  border: 4rpx dashed rgba(34, 197, 94, 0.95);
  border-radius: 50%;
  background-color: rgba(34, 197, 94, 0.06);
}
.native-camera-slot__label {
  margin-top: 24rpx;
  color: #ffffff;
  font-size: 28rpx;
  text-align: center;
}
</style>
