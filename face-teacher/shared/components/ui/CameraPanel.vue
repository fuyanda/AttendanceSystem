<template>
  <view class="camera-panel" :class="{ 'camera-panel--live': showLiveCamera }" :style="panelStyle">
    <!-- #ifdef APP-PLUS || MP-WEIXIN -->
    <camera
      v-if="showLiveCamera"
      :id="cameraId"
      class="camera-panel__live"
      :style="liveStyle"
      :device-position="devicePosition"
      flash="off"
      @error="onCameraError"
      @initdone="onCameraInitDone"
    />
    <cover-view v-if="showLiveCamera && showGuide" class="camera-panel__guide-cover">
      <cover-view class="camera-panel__guide-oval" />
      <cover-view class="camera-panel__guide-label">请将面部置于框内</cover-view>
    </cover-view>
    <!-- #endif -->

    <image
      v-if="previewPath"
      class="camera-panel__preview"
      :style="liveStyle"
      :src="previewPath"
      mode="aspectFill"
    />
    <FaceGuideOverlay
      v-if="previewPath && showGuide"
      mode="preview"
      label="请确认面部在框内且清晰"
    />

    <view v-if="!showLiveCamera && !previewPath" class="camera-panel__fallback" :style="liveStyle">
      <FaceGuideOverlay
        v-if="showGuide"
        mode="capture"
        label="正在启动相机..."
      />
    </view>
  </view>
</template>
<script setup>
import { computed } from 'vue'
import FaceGuideOverlay from '@/shared/components/ui/FaceGuideOverlay.vue'

const props = defineProps({
  previewPath: { type: String, default: '' },
  showGuide: { type: Boolean, default: true },
  live: { type: Boolean, default: true },
  devicePosition: { type: String, default: 'front' },
  cameraId: { type: String, default: 'faceCamera' },
  height: { type: String, default: '720rpx' }
})

const emit = defineEmits(['camera-error', 'init-done'])

const showLiveCamera = computed(() => props.live && !props.previewPath)
const panelStyle = computed(() => ({ height: props.height }))
const liveStyle = computed(() => ({
  width: '100%',
  height: props.height
}))

function onCameraInitDone(e) {
  emit('init-done', e)
}

function onCameraError(e) {
  emit('camera-error', e)
}
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
.camera-panel {
  position: relative;
  width: calc(100% - 48rpx);
  margin: 0 $space-24;
  background: #111827;
  box-sizing: border-box;
  overflow: hidden;
}
.camera-panel--live {
  border-radius: 0;
}
.camera-panel__live,
.camera-panel__preview,
.camera-panel__fallback {
  display: block;
}
.camera-panel__preview {
  border-radius: $radius-lg;
}
.camera-panel__fallback {
  position: relative;
  background: linear-gradient(180deg, #1e293b 0%, #0f172a 100%);
  box-sizing: border-box;
  border-radius: $radius-lg;
}
</style>
<!-- cover-view 不支持 scoped 穿透，单独声明 -->
<style lang="scss">
.camera-panel__guide-cover {
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
.camera-panel__guide-oval {
  width: 400rpx;
  height: 500rpx;
  border: 4rpx dashed rgba(34, 197, 94, 0.95);
  border-radius: 50%;
  background-color: rgba(34, 197, 94, 0.06);
}
.camera-panel__guide-label {
  margin-top: 24rpx;
  color: #ffffff;
  font-size: 28rpx;
  text-align: center;
}
</style>
