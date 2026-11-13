/**
 * 家长 H5 扫码绑定页根地址
 */
export const PARENT_H5_BASE = import.meta.env.VITE_PARENT_H5_BASE || 'https://student.example.com'

export function buildStudentBindQrUrl(bindCode) {
  const code = String(bindCode || '').trim()
  if (!code) return ''
  const base = PARENT_H5_BASE.replace(/\/$/, '')
  return `${base}/qr-bind?c=${encodeURIComponent(code)}`
}
