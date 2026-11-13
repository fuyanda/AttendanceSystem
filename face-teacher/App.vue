<script>
import { useAuthStore } from '@/stores/auth.js'

export default {
  onLaunch() {
    useAuthStore().hydrate()
  }
}
</script>

<style lang="scss">
/* #ifndef APP-NVUE */
@import '@/shared/styles/tokens.scss';

page {
  height: 100%;
  background-color: $color-bg-page;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}
/* #endif */
</style>
