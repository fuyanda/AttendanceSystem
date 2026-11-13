/** ESM adapter for UMD uqrcode.js (Vite/uni-app has no default export). */
import './uqrcode.js'

const UQRCode =
  (typeof globalThis !== 'undefined' && globalThis.UQRCode) ||
  (typeof window !== 'undefined' && window.UQRCode) ||
  (typeof global !== 'undefined' && global.UQRCode)

if (!UQRCode) {
  throw new Error('[uQRCode]: failed to load UMD module')
}

export default UQRCode
