import { computed, getCurrentInstance, ref, watch } from 'vue'
import { getToken } from '@/utils/auth'
import axios from 'axios'

const baseUrl = import.meta.env.VITE_APP_BASE_API

function parsePaths(value) {
  if (!value) return []
  if (Array.isArray(value)) return value.filter(Boolean)
  return String(value).split(',').map(s => s.trim()).filter(Boolean)
}

function toDisplayUrl(path) {
  if (!path) return ''
  if (path.startsWith('http://') || path.startsWith('https://') || path.startsWith('blob:')) {
    return path
  }
  return baseUrl + path
}

export function useReviewImageUpload(props, emit) {
  const { proxy } = getCurrentInstance()
  const uploading = ref(false)
  const paths = ref([])

  watch(
    () => props.modelValue,
    (val) => {
      paths.value = parsePaths(val)
    },
    { immediate: true }
  )

  const displayList = computed(() =>
    paths.value.map((path) => ({
      path,
      url: toDisplayUrl(path)
    }))
  )

  const previewList = computed(() => displayList.value.map((item) => item.url))

  function emitPaths() {
    emit('update:modelValue', paths.value.join(','))
  }

  function validateFile(file) {
    const allowed = props.fileType || ['png', 'jpg', 'jpeg']
    const ext = file.name.includes('.') ? file.name.split('.').pop().toLowerCase() : ''
    const typeOk = allowed.some((t) => file.type.includes(t) || ext === t)
    if (!typeOk) {
      proxy.$modal.msgError(`请上传 ${allowed.join('/')} 格式图片`)
      return false
    }
    const maxMb = props.fileSize || 10
    if (file.size / 1024 / 1024 > maxMb) {
      proxy.$modal.msgError(`图片大小不能超过 ${maxMb}MB`)
      return false
    }
    return true
  }

  async function uploadImage(file) {
    const formData = new FormData()
    formData.append('file', file)
    const res = await axios.post(`${baseUrl}/common/upload`, formData, {
      headers: {
        Authorization: 'Bearer ' + getToken(),
        'Content-Type': 'multipart/form-data'
      }
    })
    if (res.data?.code === 200 && res.data.fileName) {
      return res.data.fileName
    }
    throw new Error(res.data?.msg || '上传失败')
  }

  async function handleFiles(fileList) {
    if (!fileList?.length || props.disabled) return
    const remain = props.limit - paths.value.length
    if (remain <= 0) {
      proxy.$modal.msgError(`最多上传 ${props.limit} 张图片`)
      return
    }
    const files = Array.from(fileList).slice(0, remain)
    uploading.value = true
    proxy.$modal.loading('正在上传图片，请稍候...')
    try {
      for (const file of files) {
        if (!validateFile(file)) continue
        const fileName = await uploadImage(file)
        paths.value.push(fileName)
      }
      emitPaths()
    } catch (e) {
      proxy.$modal.msgError(e.message || '上传失败')
    } finally {
      uploading.value = false
      proxy.$modal.closeLoading()
    }
  }

  function removeImage(index) {
    if (props.disabled) return
    paths.value.splice(index, 1)
    emitPaths()
  }

  return {
    uploading,
    displayList,
    previewList,
    handleFiles,
    removeImage
  }
}
