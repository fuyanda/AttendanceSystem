import { computed, onMounted } from 'vue'
import { logout } from '@/modules/auth/api/index.js'
import { useAuthStore } from '@/stores/auth.js'

export function useServerConfig() {
  const authStore = useAuthStore()

  onMounted(() => {
    authStore.hydrate()
  })

  async function handleLogout() {
    try {
      await logout()
    } catch (e) {
      // ignore
    }
    authStore.clearSession()
    uni.reLaunch({ url: '/pages/login/login' })
  }

  return {
    user: computed(() => authStore.state.user),
    deptName: authStore.deptName,
    handleLogout
  }
}
