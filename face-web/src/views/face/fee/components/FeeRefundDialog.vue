<template>
  <el-dialog title="中道退费" v-model="visible" width="480px" append-to-body @closed="handleClosed">
    <el-descriptions :column="1" border size="small" class="mb16">
      <el-descriptions-item label="学生">{{ summary.studentName }}</el-descriptions-item>
      <el-descriptions-item label="科目">
        <dict-tag :options="cf_fee_subject" :value="summary.subject" />
      </el-descriptions-item>
      <el-descriptions-item label="已消/总课时">{{ summary.usedHours }}/{{ summary.totalHours }}</el-descriptions-item>
      <el-descriptions-item label="剩余课时">{{ summary.remainingHours }}</el-descriptions-item>
      <el-descriptions-item label="剩余课时费">¥{{ remainingAmount }}</el-descriptions-item>
      <el-descriptions-item label="单价">¥{{ unitPrice }}/节</el-descriptions-item>
    </el-descriptions>

    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="退费课时" prop="hours">
        <el-input-number v-model="form.hours" :min="1" :max="maxHours" @change="syncAmount" />
      </el-form-item>
      <el-form-item label="退费金额" prop="amount">
        <el-input-number v-model="form.amount" :min="0.01" :precision="2" :step="10" :max="maxAmount" />
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="form.remark" type="textarea" placeholder="退费原因（可选）" />
      </el-form-item>
    </el-form>

    <el-alert
      v-if="form.hours === maxHours"
      title="退全部剩余课时后，该缴费记录将自动关闭"
      type="warning"
      :closable="false"
      show-icon
      class="mb8"
    />

    <template #footer>
      <el-button type="primary" :loading="submitting" @click="submit">确认退费</el-button>
      <el-button @click="visible = false">取 消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, getCurrentInstance, reactive, ref } from 'vue'
import { refundFee } from '@/api/face/fee'

const emit = defineEmits(['success'])

const { proxy } = getCurrentInstance()
const { cf_fee_subject } = proxy.useDict('cf_fee_subject')

const visible = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const maxHours = ref(1)
const maxAmount = ref(0)

const summary = reactive({
  studentName: '',
  subject: '',
  usedHours: 0,
  totalHours: 0,
  remainingHours: 0,
  totalAmount: 0
})

const form = reactive({
  feeId: null,
  hours: 1,
  amount: 0,
  remark: ''
})

const unitPrice = computed(() => {
  if (!summary.totalHours) return '0.00'
  const price = Number(summary.totalAmount) / summary.totalHours
  return price.toFixed(2)
})

const remainingAmount = computed(() => {
  return calcAmount(summary.remainingHours, summary.totalAmount, summary.totalHours).toFixed(2)
})

const rules = {
  hours: [{ required: true, message: '请填写退费课时', trigger: 'blur' }],
  amount: [{ required: true, message: '请填写退费金额', trigger: 'blur' }]
}

function calcAmount(hours, totalAmount, totalHours) {
  if (!totalHours || totalHours < 1) return 0
  return Math.round((Number(totalAmount) * hours / totalHours) * 100) / 100
}

function syncAmount() {
  form.amount = calcAmount(form.hours, summary.totalAmount, summary.totalHours)
}

function open(row) {
  form.feeId = row.id
  form.remark = ''
  summary.studentName = row.studentName || ''
  summary.subject = row.subject
  summary.usedHours = row.usedHours || 0
  summary.totalHours = row.totalHours || 0
  summary.remainingHours = row.remainingHours || 0
  summary.totalAmount = row.totalAmount || 0
  maxHours.value = summary.remainingHours > 0 ? summary.remainingHours : 1
  maxAmount.value = calcAmount(summary.remainingHours, summary.totalAmount, summary.totalHours)
  form.hours = maxHours.value
  syncAmount()
  visible.value = true
}

function handleClosed() {
  form.feeId = null
  form.hours = 1
  form.amount = 0
  form.remark = ''
}

function submit() {
  formRef.value.validate(valid => {
    if (!valid) return
    const tip = form.hours === maxHours.value
      ? `确认退费 ${form.hours} 节、金额 ¥${form.amount}？退完后将关闭该缴费记录。`
      : `确认退费 ${form.hours} 节、金额 ¥${form.amount}？`
    proxy.$modal.confirm(tip).then(() => {
      submitting.value = true
      return refundFee({ ...form })
    }).then(() => {
      proxy.$modal.msgSuccess('退费成功')
      visible.value = false
      emit('success')
    }).catch(() => {}).finally(() => {
      submitting.value = false
    })
  })
}

defineExpose({ open })
</script>

<style scoped>
.mb16 {
  margin-bottom: 16px;
}
.mb8 {
  margin-bottom: 8px;
}
</style>
