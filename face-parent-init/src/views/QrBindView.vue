<template>
  <div class="page">
    <el-card class="card">
      <h2 class="title">{{ t.qrBindTitle }}</h2>
      <p class="hint">{{ t.qrBindHint }}</p>
      <el-alert
        v-if="entryAlert"
        type="warning"
        :title="entryAlert"
        show-icon
        :closable="false"
        class="entry-alert"
      />

      <div v-if="loadingPage" class="loading">{{ t.loading }}</div>

      <template v-else-if="studentName">
        <div class="student-block">
          <div class="student-label">{{ t.qrStudentLabel }}</div>
          <div class="student-name">{{ studentName }}</div>
        </div>

        <p v-if="returningUser" class="returning">{{ t.qrReturningHint }}</p>

        <el-form v-else :model="form" label-width="88px" @submit.prevent>
          <el-form-item :label="t.phone" required>
            <el-input v-model="form.phone" :placeholder="t.phonePlaceholder" clearable maxlength="11" />
          </el-form-item>
          <el-form-item :label="t.parentName">
            <el-input v-model="form.name" :placeholder="t.parentNamePlaceholder" clearable />
          </el-form-item>
        </el-form>

        <el-button type="primary" class="submit-btn" :loading="submitting" @click="handleConfirm">
          {{ submitting ? t.binding : t.confirmBind }}
        </el-button>
      </template>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  bindByQr,
  exchangeWxOpenId,
  fetchBindPreview,
  getWxConfig,
  lookupByOpenId
} from '@/api/user'
import { useParentStore } from '@/stores/parent'
import {
  buildWxOAuthUrl,
  cleanWxCodeFromUrl,
  getQrBindPageUrl,
  isWeChatBrowser
} from '@/utils/wechat'
import { isAjaxSuccess, normalizeAjaxBody, pickAppId, pickOpenIdDeep, unwrapAjaxBody } from '@/utils/ajax'
import t from '@/i18n/zh-CN'

const route = useRoute()
const router = useRouter()
const { mergeBindResult } = useParentStore()

const entryAlert = ref('')
const loadingPage = ref(true)
const submitting = ref(false)
const wxAuthorizing = ref(false)
const wxOpenId = ref('')
const bindCode = ref('')
const studentName = ref('')
const returningUser = ref(false)
const form = ref({ phone: '', name: '' })

function resolveApiError(error, fallback) {
  const body = error?.response?.data
  if (body?.msg) return body.msg
  return error?.response ? fallback : t.networkError
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

async function redirectToWxOAuth(code) {
  if (wxAuthorizing.value) return
  wxAuthorizing.value = true
  try {
    const body = unwrapAjaxBody(await getWxConfig())
    const appId = pickAppId(body)
    if (!appId) {
      entryAlert.value = body?.msg || t.wxServiceUnavailable
      return
    }
    ElMessage.info(t.wxRedirecting)
    window.location.replace(buildWxOAuthUrl(appId, getQrBindPageUrl(code), 'qrbind'))
  } catch (error) {
    entryAlert.value = resolveApiError(error, t.wxServiceUnavailable)
  } finally {
    wxAuthorizing.value = false
  }
}

async function initWxOpenId(wxCode) {
  try {
    const axiosRes = await exchangeWxOpenId(wxCode)
    const resolved = extractOpenIdFromResponse(axiosRes)
    if (resolved) {
      wxOpenId.value = resolved
      return true
    }
    entryAlert.value = normalizeAjaxBody(axiosRes)?.msg || t.wxAuthFailed
  } catch (error) {
    entryAlert.value = resolveApiError(error, t.wxAuthFailed)
  }
  return false
}

async function loadPreview(code) {
  const res = await fetchBindPreview(code)
  const body = normalizeAjaxBody(res)
  if (!isAjaxSuccess(body)) {
    throw new Error(body?.msg || t.qrPreviewFailed)
  }
  const data = body.data || {}
  studentName.value = data.studentName || ''
  bindCode.value = data.bindCode || code
}

async function loadAccountState(openId) {
  const res = await lookupByOpenId(openId)
  const body = normalizeAjaxBody(res)
  if (!isAjaxSuccess(body)) return
  returningUser.value = !!(body.data && body.data.exists)
}

onMounted(async () => {
  const code = String(route.query.c || '').trim().toUpperCase()
  if (!code) {
    entryAlert.value = t.qrCodeMissing
    loadingPage.value = false
    return
  }
  bindCode.value = code

  const wxCode = String(route.query.code || '')
  try {
    if (wxCode) {
      const ok = await initWxOpenId(wxCode)
      if (ok) cleanWxCodeFromUrl(router)
      else {
        loadingPage.value = false
        return
      }
    } else {
      const devOpenId = import.meta.env.VITE_WX_DEV_OPENID
      if (import.meta.env.DEV && devOpenId) {
        wxOpenId.value = devOpenId
      } else if (isWeChatBrowser()) {
        await redirectToWxOAuth(code)
        return
      } else {
        entryAlert.value = t.qrWechatOnly
        loadingPage.value = false
        return
      }
    }

    await loadPreview(code)
    if (wxOpenId.value) {
      await loadAccountState(wxOpenId.value)
    }
  } catch (error) {
    entryAlert.value = resolveApiError(error, t.qrPreviewFailed)
    studentName.value = ''
  } finally {
    loadingPage.value = false
  }
})

async function handleConfirm() {
  const openId = (wxOpenId.value || '').trim()
  if (!openId) {
    ElMessage.warning(t.wechatEntryRequired)
    return
  }
  if (!bindCode.value) {
    ElMessage.warning(t.qrCodeMissing)
    return
  }
  if (!returningUser.value && !/^1[3-9]\d{9}$/.test(form.value.phone)) {
    ElMessage.warning(t.phoneInvalid)
    return
  }

  submitting.value = true
  try {
    const payload = {
      openId,
      bindCode: bindCode.value,
      phone: returningUser.value ? undefined : form.value.phone,
      name: returningUser.value ? undefined : (form.value.name || undefined)
    }
    const response = await bindByQr(payload)
    const { data } = response
    if (!isAjaxSuccess(data)) {
      ElMessage.error(data?.msg || t.bindFailed)
      return
    }
    const result = data.data || {}
    mergeBindResult(result)
    if (result.newlyCreated && result.password) {
      await ElMessageBox.alert(
        t.passwordDialogContent.replace('{password}', result.password),
        t.passwordDialogTitle,
        { confirmButtonText: t.passwordDialogConfirm }
      )
    } else if ((result.boundCount || 0) === 0) {
      ElMessage.success(t.alreadyBound)
    } else {
      ElMessage.success(
        result.studentNames?.length
          ? t.bindSuccessNames.replace('{names}', result.studentNames.join('、'))
          : t.bindSuccess
      )
    }
    router.replace('/home')
  } catch (error) {
    ElMessage.error(resolveApiError(error, t.bindFailed))
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding: 24px 16px 48px;
  background: #f5f7fa;
  box-sizing: border-box;
}
.card {
  max-width: 480px;
  margin: 0 auto;
  border-radius: 16px;
}
.title {
  margin: 0 0 8px;
  font-size: 22px;
  text-align: center;
}
.hint {
  margin: 0 0 16px;
  color: #909399;
  font-size: 14px;
  text-align: center;
}
.entry-alert {
  margin-bottom: 16px;
}
.loading {
  text-align: center;
  color: #909399;
  padding: 32px 0;
}
.student-block {
  text-align: center;
  padding: 24px 12px;
  margin-bottom: 16px;
  background: #f0f7ff;
  border-radius: 12px;
}
.student-label {
  font-size: 14px;
  color: #909399;
}
.student-name {
  margin-top: 8px;
  font-size: 28px;
  font-weight: 700;
  color: #303133;
}
.returning {
  text-align: center;
  color: #67c23a;
  font-size: 14px;
  margin-bottom: 16px;
}
.submit-btn {
  width: 100%;
  margin-top: 8px;
}
</style>
