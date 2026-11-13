/** 后台服务根地址；本机覆盖见 host.local.js（勿提交） */
import { DEFAULT_HOST as LOCAL_HOST, PARENT_H5_BASE as LOCAL_H5 } from './host.local.js'

export const DEFAULT_HOST = LOCAL_HOST
export const PARENT_H5_BASE = LOCAL_H5

export function getHost() {
  return DEFAULT_HOST
}

export function buildStudentBindQrUrl(bindCode) {
  const code = String(bindCode || '').trim()
  if (!code) return ''
  const base = PARENT_H5_BASE.replace(/\/$/, '')
  return `${base}/qr-bind?c=${encodeURIComponent(code)}`
}
