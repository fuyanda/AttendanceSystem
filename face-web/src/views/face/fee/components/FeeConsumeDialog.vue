<template>
  <el-dialog title="手动消课" v-model="visible" width="420px" append-to-body @closed="handleClosed">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="消课节数" prop="hours">
        <el-input-number v-model="form.hours" :min="1" :max="maxHours" />
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="form.remark" type="textarea" placeholder="可选" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button type="primary" @click="submit">确 定</el-button>
      <el-button @click="visible = false">取 消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { getCurrentInstance, reactive, ref } from 'vue'
import { consumeFee } from '@/api/face/fee'

const emit = defineEmits(['success'])

const { proxy } = getCurrentInstance()
const visible = ref(false)
const formRef = ref(null)
const maxHours = ref(99)

const form = reactive({
  feeId: null,
  hours: 1,
  remark: ''
})

const rules = {
  hours: [{ required: true, message: '请填写消课节数', trigger: 'blur' }]
}

function open(row) {
  form.feeId = row.id
  form.hours = 1
  form.remark = ''
  maxHours.value = row.remainingHours > 0 ? row.remainingHours : 99
  visible.value = true
}

function handleClosed() {
  form.feeId = null
  form.hours = 1
  form.remark = ''
}

function submit() {
  formRef.value.validate(valid => {
    if (!valid) return
    consumeFee({ ...form }).then(() => {
      proxy.$modal.msgSuccess('消课成功')
      visible.value = false
      emit('success')
    })
  })
}

defineExpose({ open })
</script>
