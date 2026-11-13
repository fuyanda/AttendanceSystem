<template>
  <view v-if="bindCode" class="bind-qr">
    <canvas
      class="bind-qr__canvas"
      canvas-id="studentBindQr"
      :style="{ width: size + 'px', height: size + 'px' }"
      :width="size"
      :height="size"
    />
    <view class="bind-qr__actions">
      <button class="bind-qr__btn" :disabled="saving" @click="saveToAlbum">保存二维码</button>
    </view>
    <view class="bind-qr__tip">家长微信扫码即可绑定；重新生成绑定码后旧码失效</view>
  </view>
</template>

<script setup>
import { getCurrentInstance, nextTick, ref, watch } from 'vue'
import UQRCode from '@/shared/vendor/uqrcode-entry.js'
import { buildStudentBindQrUrl } from '@/shared/config/host.js'

const props = defineProps({
  bindCode: { type: String, default: '' }
})

const size = 180
const saving = ref(false)
const instance = getCurrentInstance()

async function drawQr() {
  const url = buildStudentBindQrUrl(props.bindCode)
  if (!url) return
  await nextTick()
  const qr = new UQRCode()
  qr.data = url
  qr.size = size
  qr.margin = 8
  qr.errorCorrectLevel = UQRCode.errorCorrectLevel.M
  qr.make()
  const ctx = uni.createCanvasContext('studentBindQr', instance?.proxy)
  qr.canvasContext = ctx
  await qr.draw()
}

function saveToAlbum() {
  if (saving.value || !props.bindCode) return
  saving.value = true
  uni.canvasToTempFilePath(
    {
      canvasId: 'studentBindQr',
      success(res) {
        uni.saveImageToPhotosAlbum({
          filePath: res.tempFilePath,
          success() {
            uni.showToast({ title: '已保存到相册', icon: 'success' })
          },
          fail() {
            uni.showToast({ title: '保存失败，请检查相册权限', icon: 'none' })
          },
          complete() {
            saving.value = false
          }
        })
      },
      fail() {
        saving.value = false
        uni.showToast({ title: '生成图片失败', icon: 'none' })
      }
    },
    instance?.proxy
  )
}

watch(
  () => props.bindCode,
  () => {
    setTimeout(drawQr, 80)
  },
  { immediate: true }
)
</script>

<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
.bind-qr {
  margin-top: $space-16;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.bind-qr__canvas {
  width: 180px;
  height: 180px;
  background: #fff;
  border-radius: $radius-md;
  border: 1px solid $color-border;
}
.bind-qr__actions {
  margin-top: $space-16;
  width: 100%;
}
.bind-qr__btn {
  width: 100%;
  height: 72rpx;
  line-height: 72rpx;
  border-radius: 999rpx;
  background: $color-primary-soft;
  color: $color-primary;
  font-size: 26rpx;
  font-weight: 600;
  border: 1px solid $color-primary-border;
}
.bind-qr__btn::after {
  border: none;
}
.bind-qr__tip {
  margin-top: $space-8;
  font-size: 22rpx;
  color: $color-text-muted;
  text-align: center;
  line-height: 1.5;
}
</style>
