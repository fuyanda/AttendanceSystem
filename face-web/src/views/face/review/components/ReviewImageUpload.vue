<template>
  <div class="review-image-upload" v-loading="uploading">
    <div v-if="displayList.length" class="image-grid">
      <div
        v-for="(img, index) in displayList"
        :key="img.path + index"
        class="image-item"
      >
        <el-image
          :src="img.url"
          fit="cover"
          class="image-item__img"
          :preview-src-list="previewList"
          :initial-index="index"
          preview-teleported
        />
        <button
          v-if="!disabled"
          type="button"
          class="image-item__remove"
          aria-label="删除图片"
          @click.stop="removeImage(index)"
        >
          <el-icon><Close /></el-icon>
        </button>
      </div>
    </div>

    <div
      v-if="!disabled && displayList.length < limit"
      class="action-row"
    >
      <button
        type="button"
        class="action-btn action-btn--camera"
        :disabled="uploading"
        @click="triggerCamera"
      >
        <el-icon><Camera /></el-icon>
        <span>拍照</span>
      </button>
      <button
        type="button"
        class="action-btn action-btn--album"
        :disabled="uploading"
        @click="triggerAlbum"
      >
        <el-icon><Picture /></el-icon>
        <span>相册</span>
      </button>
    </div>

    <p v-if="!disabled" class="upload-tip">
      最多 {{ limit }} 张，单张不超过 {{ fileSize }}MB
    </p>

    <input
      ref="cameraInputRef"
      type="file"
      accept="image/jpeg,image/png,image/jpg"
      capture="environment"
      class="hidden-input"
      @change="onInputChange"
    />
    <input
      ref="albumInputRef"
      type="file"
      accept="image/jpeg,image/png,image/jpg"
      multiple
      class="hidden-input"
      @change="onInputChange"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useReviewImageUpload } from '../composables/useReviewImageUpload'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  limit: {
    type: Number,
    default: 6
  },
  fileSize: {
    type: Number,
    default: 10
  },
  fileType: {
    type: Array,
    default: () => ['png', 'jpg', 'jpeg']
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue'])

const cameraInputRef = ref(null)
const albumInputRef = ref(null)

const {
  uploading,
  displayList,
  previewList,
  handleFiles,
  removeImage
} = useReviewImageUpload(props, emit)

function resetInput(el) {
  if (el) el.value = ''
}

function triggerCamera() {
  resetInput(cameraInputRef.value)
  cameraInputRef.value?.click()
}

function triggerAlbum() {
  resetInput(albumInputRef.value)
  albumInputRef.value?.click()
}

function onInputChange(event) {
  const files = event.target?.files
  handleFiles(files)
  resetInput(event.target)
}
</script>

<style scoped lang="scss">
.review-image-upload {
  width: 100%;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  margin-bottom: 12px;
}

.image-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f7fa;
}

.image-item__img {
  width: 100%;
  height: 100%;
}

.image-item__remove {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 22px;
  height: 22px;
  border: none;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  cursor: pointer;
}

.action-row {
  display: flex;
  gap: 12px;
}

.action-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  height: 44px;
  border-radius: 8px;
  border: 1px dashed #c0c4cc;
  background: #fafafa;
  color: #606266;
  font-size: 14px;
  cursor: pointer;
  transition: border-color 0.2s, color 0.2s;

  &:active:not(:disabled) {
    border-color: #409eff;
    color: #409eff;
  }

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
}

.action-btn--camera {
  border-color: #b3d8ff;
  background: #ecf5ff;
  color: #409eff;
}

.upload-tip {
  margin: 8px 0 0;
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}

.hidden-input {
  display: none;
}
</style>
