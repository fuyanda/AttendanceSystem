import { ref } from 'vue'

let cached = null

function loadInsets() {
  if (cached) return cached
  try {
    const sys = uni.getSystemInfoSync()
    const statusBar = Number(sys.statusBarHeight) || 0
    const safeTop = Number(sys.safeAreaInsets?.top) || 0
    const safeBottom = Number(sys.safeAreaInsets?.bottom) || 0
    const windowHeight = Number(sys.windowHeight) || 667
    cached = {
      statusBarHeightPx: ref(Math.max(statusBar, safeTop)),
      safeAreaBottomPx: ref(safeBottom),
      windowHeightPx: ref(windowHeight)
    }
  } catch {
    cached = {
      statusBarHeightPx: ref(0),
      safeAreaBottomPx: ref(0),
      windowHeightPx: ref(667)
    }
  }
  return cached
}

/** 状态栏 / 安全区；Android 全屏依赖 statusBarHeight，不一定认 env(safe-area-inset-top) */
export function useLayoutInsets() {
  return loadInsets()
}

export function headerPaddingTop(extraRpx = 16) {
  const { statusBarHeightPx } = loadInsets()
  return {
    paddingTop: `${statusBarHeightPx.value + uni.upx2px(extraRpx)}px`
  }
}

/** 页面容器高度：用 windowHeight 像素，避免 Android WebView 上 100vh 异常 */
export function pageFillStyle() {
  const { windowHeightPx } = loadInsets()
  return {
    height: `${windowHeightPx.value}px`,
    overflow: 'hidden',
    display: 'flex',
    flexDirection: 'column',
    boxSizing: 'border-box'
  }
}
