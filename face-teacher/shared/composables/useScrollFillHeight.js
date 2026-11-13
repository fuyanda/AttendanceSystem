import { getCurrentInstance, nextTick, onMounted, onUnmounted, ref } from 'vue'
import { useLayoutInsets } from '@/shared/composables/useLayoutInsets.js'

/**
 * 给 scroll-view 计算明确像素高度。
 * Android App WebView 上 flex:1 + height:0 经常算成 0，列表整块空白。
 *
 * @param {object} options
 * @param {string} options.anchorSelector 列表上方锚点（用其 bottom 起算）
 * @param {string} [options.containerSelector] 父容器；传入时用容器高度减去锚点以下空间
 * @param {number} [options.bottomReserveRpx=0] 底部预留（tabbar/FAB 等），单位 rpx
 * @param {number} [options.minHeightPx=160] 最小高度兜底
 */
export function useScrollFillHeight(options = {}) {
  const {
    anchorSelector,
    containerSelector,
    bottomReserveRpx = 0,
    minHeightPx = 160
  } = options

  const { windowHeightPx, safeAreaBottomPx } = useLayoutInsets()
  const scrollHeightPx = ref(Math.max(minHeightPx, Math.floor(windowHeightPx.value * 0.45)))
  const instance = getCurrentInstance()

  function applyFallback(winH, bottomPx) {
    scrollHeightPx.value = Math.max(minHeightPx, Math.floor(winH - bottomPx - uni.upx2px(320)))
  }

  function measure() {
    nextTick(() => {
      let winH = windowHeightPx.value
      try {
        const sys = uni.getSystemInfoSync()
        winH = Number(sys.windowHeight) || winH
        windowHeightPx.value = winH
      } catch (_) {
        /* ignore */
      }
      const bottomPx = uni.upx2px(bottomReserveRpx) + (safeAreaBottomPx.value || 0)

      if (containerSelector && anchorSelector && instance?.proxy) {
        uni.createSelectorQuery()
          .in(instance.proxy)
          .select(containerSelector)
          .boundingClientRect()
          .select(anchorSelector)
          .boundingClientRect()
          .exec((res) => {
            const container = res && res[0]
            const header = res && res[1]
            if (container?.height != null && header?.bottom != null && container.top != null) {
              const headerOffset = header.bottom - container.top
              scrollHeightPx.value = Math.max(
                minHeightPx,
                Math.floor(container.height - headerOffset - uni.upx2px(bottomReserveRpx))
              )
            } else {
              applyFallback(winH, bottomPx)
            }
          })
        return
      }

      if (!anchorSelector || !instance?.proxy) {
        applyFallback(winH, bottomPx)
        return
      }

      uni.createSelectorQuery()
        .in(instance.proxy)
        .select(anchorSelector)
        .boundingClientRect()
        .exec((res) => {
          const rect = res && res[0]
          if (rect && typeof rect.bottom === 'number') {
            scrollHeightPx.value = Math.max(
              minHeightPx,
              Math.floor(winH - rect.bottom - bottomPx)
            )
          } else {
            applyFallback(winH, bottomPx)
          }
        })
    })
  }

  onMounted(() => {
    measure()
    // 部分 Android 机型首帧布局未完成，延迟再量一次
    setTimeout(measure, 80)
    setTimeout(measure, 320)
  })

  // #ifdef APP-PLUS
  const onResize = () => measure()
  onMounted(() => {
    if (typeof uni.onWindowResize === 'function') {
      uni.onWindowResize(onResize)
    }
  })
  onUnmounted(() => {
    if (typeof uni.offWindowResize === 'function') {
      uni.offWindowResize(onResize)
    }
  })
  // #endif

  return {
    scrollHeightPx,
    measure
  }
}
