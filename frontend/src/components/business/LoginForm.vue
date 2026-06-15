<template>
  <AuthFormShell title="助农商城" compact>
    <template #icon>
      <div class="brand-icon">
        <el-icon :size="28"><Shop /></el-icon>
      </div>
    </template>
    <el-form ref="formRef" :model="model" :rules="rules" class="login-form">
      <el-form-item prop="username">
        <el-input v-model="model.username" placeholder="请输入用户名" :prefix-icon="User" />
      </el-form-item>
      <el-form-item prop="password">
        <el-input
          v-model="model.password"
          type="password"
          placeholder="请输入密码"
          :prefix-icon="Lock"
          show-password
          @keyup.enter="$emit('submit')"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" class="login-btn" :loading="loading" @click="$emit('submit')">
          登录
        </el-button>
      </el-form-item>
    </el-form>
    <template #footer>
      <span>还没有账号？</span>
      <router-link to="/register">立即注册</router-link>
    </template>
  </AuthFormShell>
</template>

<script setup>
import { ref } from 'vue'
import { Lock, Shop, User } from '@element-plus/icons-vue'
import AuthFormShell from '@/components/business/AuthFormShell.vue'

defineProps({
  model: {
    type: Object,
    required: true
  },
  rules: {
    type: Object,
    required: true
  },
  loading: {
    type: Boolean,
    default: false
  }
})

defineEmits(['submit'])

const formRef = ref(null)

defineExpose({
  validate: () => formRef.value?.validate().catch(() => false)
})
</script>

<style scoped>
.brand-icon {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #1b3a26, #4caf50);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  color: #fff;
  box-shadow: 0 8px 20px rgba(27, 58, 38, 0.3);
}

.login-form {
  margin-bottom: 12px;
}

.login-btn {
  width: 100%;
  height: 40px;
  font-size: 14px;
  background: linear-gradient(135deg, #1b3a26 0%, #2e5a3a 50%, #3d7a4a 100%);
  border: none;
  letter-spacing: 4px;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(27, 58, 38, 0.3);
}

:deep(.el-input__wrapper) {
  padding: 0 12px;
  height: 38px;
  box-shadow: 0 0 0 1px #e0e0e0 inset;
  border-radius: 6px;
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--color-primary) inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(46, 125, 50, 0.2) inset, 0 0 0 1px var(--color-primary) inset;
}

:deep(.el-input__inner) {
  font-size: 13px;
}

:deep(.el-form-item) {
  margin-bottom: 18px;
}
</style>
