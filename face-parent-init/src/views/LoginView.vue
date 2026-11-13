<template>
  <div class="page">
    <el-card class="card">
      <h2 class="title">{{ isAddMode ? t.bindMoreTitle : t.bindTitle }}</h2>
      <p class="hint" v-if="!isAddMode">{{ t.bindHint }}</p>
      <p class="hint" v-else>{{ t.bindMoreHint }}</p>
      <el-alert
        v-if="entryAlert"
        type="warning"
        :title="entryAlert"
        show-icon
        :closable="false"
        class="wx-entry-alert"
      />

      <el-form :model="form" label-width="88px" @submit.prevent>
        <el-form-item :label="t.bindCode" required>
          <el-input
            v-model="form.bindCode"
            :placeholder="t.bindCodePlaceholder"
            clearable
            maxlength="6"
            class="bind-code-input"
            @input="onBindCodeInput"
          />
        </el-form-item>

        <el-form-item :label="t.phone" required>
          <el-input
            v-model="form.phone"
            :placeholder="t.phonePlaceholder"
            clearable
            maxlength="11"
            :disabled="isAddMode"
          />
        </el-form-item>

        <el-form-item v-if="showNameField" :label="t.parentName">
          <el-input v-model="form.name" :placeholder="t.parentNamePlaceholder" clearable />
        </el-form-item>

        <el-form-item v-if="showPasswordField" :label="t.password" required>
          <el-input
            v-model="form.password"
            type="password"
            :placeholder="t.passwordPlaceholder"
            show-password
          />
          <p class="field-hint">{{ t.passwordForgotHint }}</p>
        </el-form-item>

        <p v-if="!isAddMode && !existingAccount" class="toggle-link">
          <a href="#" @click.prevent="existingAccount = true">{{ t.existingAccountLink }}</a>
        </p>

        <el-button type="primary" class="submit-btn" :loading="loading" @click="handleSubmit">
          {{ loading ? t.binding : (isAddMode ? t.confirmBind : t.bindNow) }}
        </el-button>

        <el-button v-if="isAddMode" class="back-btn" @click="goHome">{{ t.back }}</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { bindByWechat, bindStudentByCode, exchangeWxOpenId, getWxConfig } from '@/api/user'
import { useParentStore } from '@/stores/parent'
import {
  buildWxOAuthUrl,
  cleanWxCodeFromUrl,
  getBindPageUrl,
  isWeChatBrowser
} from '@/utils/wechat'
import { isAjaxSuccess, normalizeAjaxBody, pickAppId, pickOpenIdDeep, unwrapAjaxBody } from '@/utils/ajax'
import t from '@/i18n/zh-CN'

const route = useRoute()
const router = useRouter()
const { session, mergeBindResult, hasSession } = useParentStore()

const isAddMode = computed(() => route.name === 'bind-more')
const loading = ref(false)
const existingAccount = ref(false)
const entryAlert = ref('')
const wxAuthorizing = ref(false)
const wxOpenId = ref('')

const form = ref({
  bindCode: '',
  phone: '',
  name: '',
  password: ''
})

const showNameField = computed(() => !isAddMode.value && !existingAccount.value)
const showPasswordField = computed(() => isAddMode.value || existingAccount.value)

function showNotWeChatEntry() {
  entryAlert.value = t.wechatEntryHint
}

function showWxServiceError(msg) {
  entryAlert.value = msg || t.wxServiceUnavailable
}

function clearEntryAlert() {
  entryAlert.value = ''
}

function resolveApiError(error, fallback) {
  const body = error?.response?.data
  if (body?.msg) return body.msg
  return error?.response ? fallback : t.networkError
}

onMounted(async () => {
  const wxCode = String(route.query.code || new URLSearchParams(window.location.search).get('code') || '')

  if (isAddMode.value) {
    if (!hasSession.value) {
      ElMessage.warning(t.bindFirst)
      router.replace('/login')
      return
    }
    form.value.phone = session.value.phone
    form.value.password = session.value.password
    return
  }

  if (wxCode) {
    const ok = await initWxOpenId(wxCode)
    if (ok) {
      cleanWxCodeFromUrl(router)
    }
  } else {
    const devOpenId = import.meta.env.VITE_WX_DEV_OPENID
    if (import.meta.env.DEV && devOpenId) {
      wxOpenId.value = devOpenId
    } else if (isWeChatBrowser()) {
      await redirectToWxOAuth()
      return
    } else {
      showNotWeChatEntry()
    }
  }

  if (session.value.phone) {
    form.value.phone = session.value.phone
    existingAccount.value = true
    form.value.password = session.value.password || ''
  }
})

async function redirectToWxOAuth() {
  if (wxAuthorizing.value) return
  wxAuthorizing.value = true
  try {
    const body = unwrapAjaxBody(await getWxConfig())
    const appId = pickAppId(body)
    if (!appId) {
      const msg = body?.msg || t.wxServiceUnavailable
      showWxServiceError(msg)
      ElMessage.error(msg)
      return
    }
    clearEntryAlert()
    ElMessage.info(t.wxRedirecting)
    const url = buildWxOAuthUrl(appId, getBindPageUrl())
    window.location.replace(url)
  } catch (error) {
    const msg = resolveApiError(error, t.wxServiceUnavailable)
    showWxServiceError(msg)
    ElMessage.error(msg)
  } finally {
    wxAuthorizing.value = false
  }
}

function extractOpenIdFromResponse(axiosRes) {
  const body = normalizeAjaxBody(axiosRes)
  return (
    pickOpenIdDeep(body) ||
    pickOpenIdDeep(axiosRes) ||
    pickOpenIdDeep(body?.data) ||
    ''
  )
}

async function initWxOpenId(wxCode) {
  try {
    const axiosRes = await exchangeWxOpenId(wxCode)
    const resolved = extractOpenIdFromResponse(axiosRes)
    if (resolved) {
      wxOpenId.value = resolved
      clearEntryAlert()
      return true
    }
    const body = normalizeAjaxBody(axiosRes)
    const msg = body?.msg || t.wxAuthFailed
    showWxServiceError(msg)
    ElMessage.error(msg)
  } catch (error) {
    const msg = resolveApiError(error, t.wxAuthFailed)
    showWxServiceError(msg)
    ElMessage.error(msg)
  }
  return false
}

function getBoundOpenId() {
  return (wxOpenId.value || '').trim()
}

function buildSubmitPayload(boundOpenId) {
  if (isAddMode.value) {
    return {
      phone: form.value.phone,
      password: form.value.password,
      bindCode: form.value.bindCode,
      name: form.value.name || undefined
    }
  }
  const payload = {
    phone: form.value.phone,
    bindCode: form.value.bindCode,
    openId: boundOpenId,
    name: form.value.name || undefined
  }
  if (existingAccount.value) {
    payload.password = form.value.password
  }
  return payload
}

function onBindCodeInput(val) {
  form.value.bindCode = (val || '').toUpperCase().replace(/[^0-9A-Z]/g, '')
}

function validateForm(boundOpenId = '') {
  const code = form.value.bindCode.trim()
  if (!code || code.length < 6) {
    ElMessage.warning(t.bindCodeRequired)
    return false
  }
  if (!/^1[3-9]\d{9}$/.test(form.value.phone)) {
    ElMessage.warning(t.phoneInvalid)
    return false
  }
  if (showPasswordField.value && !form.value.password) {
    ElMessage.warning(t.passwordRequired)
    return false
  }
  if (!isAddMode.value && !boundOpenId) {
    ElMessage.warning(wxOpenId.value ? t.wxAuthFailed : t.wechatEntryRequired)
    return false
  }
  return true
}

async function handleSubmit() {
  const boundOpenId = getBoundOpenId()
  if (!validateForm(boundOpenId)) return

  const payload = buildSubmitPayload(boundOpenId)
  loading.value = true
  try {
    let response
    if (isAddMode.value) {
      response = await bindStudentByCode(payload)
    } else {
      response = await bindByWechat(payload)
    }

    const { data } = response
    if (isAjaxSuccess(data)) {
      const result = data.data || {}
      mergeBindResult(result)

      if (result.newlyCreated && result.password) {
        await ElMessageBox.alert(
          t.passwordDialogContent.replace('{password}', result.password),
          t.passwordDialogTitle,
          { confirmButtonText: t.passwordDialogConfirm, closeOnClickModal: false }
        )
      } else if (result.boundCount === 0) {
        ElMessage.success(t.alreadyBound)
      } else {
        const names = (result.studentNames || []).join('\u3001')
        ElMessage.success(names ? t.bindSuccessNames.replace('{names}', names) : t.bindSuccess)
      }
      router.push('/home')
    } else {
      const msg = data?.msg || t.bindFailed
      if (msg.includes('\u5df2\u6ce8\u518c') || msg.includes('\u5bc6\u7801')) {
        existingAccount.value = true
      }
      ElMessage.error(msg)
    }
  } catch (error) {
    const msg = error.response?.data?.msg
    if (msg && (msg.includes('\u5df2\u6ce8\u518c') || msg.includes('\u5bc6\u7801'))) {
      existingAccount.value = true
    }
    ElMessage.error(msg || t.networkError)
  } finally {
    loading.value = false
  }
}

function goHome() {
  router.push('/home')
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 24px 16px;
  background: linear-gradient(145deg, #e8f4fc 0%, #f0f7ff 50%, #e3f0ff 100%);
}

.card {
  width: 100%;
  max-width: 440px;
  padding: 8px 4px 16px;
  border-radius: 12px;
  box-shadow: 0 4px 24px rgba(64, 158, 255, 0.12);
}

.title {
  text-align: center;
  color: #303133;
  margin: 0 0 8px;
  font-size: 22px;
  font-weight: 600;
}

.hint {
  text-align: center;
  color: #909399;
  font-size: 14px;
  line-height: 1.6;
  margin: 0 0 24px;
  padding: 0 8px;
}

.wx-entry-alert {
  margin-bottom: 20px;
}

.bind-code-input :deep(.el-input__inner) {
  letter-spacing: 4px;
  font-weight: 600;
  text-transform: uppercase;
}

.submit-btn {
  width: 100%;
  margin-top: 8px;
}

.back-btn {
  width: 100%;
  margin-top: 12px;
  margin-left: 0;
}

.toggle-link {
  text-align: center;
  margin: -8px 0 8px;
  font-size: 13px;
}

.toggle-link a {
  color: #409eff;
  text-decoration: none;
}

.field-hint {
  margin: 8px 0 0;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}
</style>
