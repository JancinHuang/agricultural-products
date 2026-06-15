<template>
  <div class="login-container">
    <AuthAnimatedBackground />
    <LoginForm ref="loginFormRef" :model="form" :rules="rules" :loading="loading" @submit="handleLogin" />
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import AuthAnimatedBackground from '@/components/business/AuthAnimatedBackground.vue'
import LoginForm from '@/components/business/LoginForm.vue'
import { notify } from '@/services/uiFeedback'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  const valid = await loginFormRef.value?.validate()
  if (!valid) return

  loading.value = true
  try {
    await userStore.loginAction(form)
    notify.success('登录成功')
    router.push('/')
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background: linear-gradient(135deg, #1b3a26 0%, #2e5a3a 50%, #3d7a4a 100%);
  position: relative;
  overflow: hidden;
}

</style>
