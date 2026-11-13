<template>
  <div v-if="bindCode" class="bind-qr">
    <img v-if="dataUrl" class="bind-qr__img" :src="dataUrl" alt="绑定二维码" />
    <div class="bind-qr__actions">
      <el-button type="primary" plain size="small" :loading="rendering" @click="download">下载二维码</el-button>
    </div>
    <div class="bind-qr__tip">家长微信扫码即可打开绑定确认页；重新生成绑定码后旧码失效</div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import QRCode from 'qrcode'
import { buildStudentBindQrUrl } from '@/utils/parentH5'
import { saveAs } from 'file-saver'

const props = defineProps({
  bindCode: { type: String, default: '' },
  studentName: { type: String, default: '' }
})

const dataUrl = ref('')
const rendering = ref(false)

async function render() {
  const url = buildStudentBindQrUrl(props.bindCode)
  if (!url) {
    dataUrl.value = ''
    return
  }
  rendering.value = true
  try {
    dataUrl.value = await QRCode.toDataURL(url, {
      width: 220,
      margin: 2,
      errorCorrectionLevel: 'M'
    })
  } catch (e) {
    dataUrl.value = ''
  } finally {
    rendering.value = false
  }
}

function download() {
  if (!dataUrl.value) return
  const name = (props.studentName || '学生').replace(/[\\/:*?"<>|]/g, '_')
  const code = props.bindCode || 'code'
  const arr = dataUrl.value.split(',')
  const mime = arr[0].match(/:(.*?);/)[1]
  const bstr = atob(arr[1])
  let n = bstr.length
  const u8 = new Uint8Array(n)
  while (n--) u8[n] = bstr.charCodeAt(n)
  saveAs(new Blob([u8], { type: mime }), `绑定二维码_${name}_${code}.png`)
}

watch(() => props.bindCode, render, { immediate: true })
</script>

<style scoped>
.bind-qr {
  margin-top: 12px;
}
.bind-qr__img {
  width: 180px;
  height: 180px;
  display: block;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #fff;
}
.bind-qr__actions {
  margin-top: 8px;
}
.bind-qr__tip {
  margin-top: 6px;
  color: #909399;
  font-size: 12px;
  line-height: 1.5;
  max-width: 360px;
}
</style>
