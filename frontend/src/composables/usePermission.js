import { computed } from 'vue'
import { useUserStore } from '@/store/user'
import { isAdminUser } from '@/utils/auth'

export function usePermission() {
  const userStore = useUserStore()
  const isLoggedIn = computed(() => !!userStore.token)
  const currentUser = computed(() => userStore.userInfo)
  const isAdmin = computed(() => isAdminUser(currentUser.value))
  const isUser = computed(() => isLoggedIn.value && !isAdmin.value)

  return {
    currentUser,
    isLoggedIn,
    isAdmin,
    isUser
  }
}
