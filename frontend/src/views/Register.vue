<template>
  <div class="register-container">
    <AuthBrandPanel />
    <div class="register-right">
      <AuthFormShell title="创建账号" subtitle="填写以下信息完成注册">
        <BaseForm ref="formRef" :model="form" :rules="rules" class="register-form">
          <BaseFormItem prop="username">
            <BaseInput
              v-model="form.username"
              placeholder="请输入用户名"
              size="large"
              :prefix-icon="User"
            />
          </BaseFormItem>
          <BaseFormItem prop="nickname">
            <BaseInput
              v-model="form.nickname"
              placeholder="请输入昵称"
              size="large"
              :prefix-icon="UserFilled"
            />
          </BaseFormItem>
          <BaseFormItem prop="password">
            <BaseInput
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              :prefix-icon="Lock"
              show-password
            />
          </BaseFormItem>
          <BaseFormItem prop="confirmPassword">
            <BaseInput
              v-model="form.confirmPassword"
              type="password"
              placeholder="请确认密码"
              size="large"
              :prefix-icon="Lock"
              show-password
            />
          </BaseFormItem>
          <BaseFormItem prop="phone">
            <BaseInput
              v-model="form.phone"
              placeholder="请输入手机号"
              size="large"
              :prefix-icon="Phone"
            />
          </BaseFormItem>
          <BaseFormItem prop="email">
            <BaseInput
              v-model="form.email"
              placeholder="请输入邮箱"
              size="large"
              :prefix-icon="Message"
            />
          </BaseFormItem>
          <BaseFormItem>
            <BaseButton
              type="primary"
              size="large"
              class="register-btn"
              :loading="loading"
              @click="handleRegister"
            >
              注 册
            </BaseButton>
          </BaseFormItem>
        </BaseForm>
        <template #footer>
          <span>已有账号？</span>
          <router-link to="/login">立即登录</router-link>
        </template>
      </AuthFormShell>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, UserFilled, Lock, Phone, Message } from '@element-plus/icons-vue'
import { register } from '@/api/auth'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseForm from '@/components/base/BaseForm.vue'
import BaseFormItem from '@/components/base/BaseFormItem.vue'
import BaseInput from '@/components/base/BaseInput.vue'
import AuthBrandPanel from '@/components/business/AuthBrandPanel.vue'
import AuthFormShell from '@/components/business/AuthFormShell.vue'
import { notify } from '@/services/uiFeedback'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: '',
  phone: '',
  email: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const validatePhone = (rule, value, callback) => {
  if (value && !/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号'))
  } else {
    callback()
  }
}

const validateEmail = (rule, value, callback) => {
  if (value && !/^[\w-]+(\.[\w-]+)*@[\w-]+(\.[\w-]+)+$/.test(value)) {
    callback(new Error('请输入正确的邮箱'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  phone: [
    { validator: validatePhone, trigger: 'blur' }
  ],
  email: [
    { validator: validateEmail, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  try {
    await register({
      username: form.username,
      nickname: form.nickname,
      password: form.password,
      phone: form.phone,
      email: form.email
    })
    notify.success('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  height: 100vh;
  background: #f0f2f5;
}

.register-right {
  width: 520px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  padding: 40px;
  overflow-y: auto;
}

.register-form {
  margin-bottom: 20px;
}

.register-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  background: linear-gradient(135deg, #1b3a26 0%, #2e7d32 100%);
  border: none;
  letter-spacing: 4px;
}

.register-btn:hover {
  background: linear-gradient(135deg, #2e7d32 0%, #4caf50 100%);
}

:deep(.el-input__wrapper) {
  padding: 0 15px;
  height: 44px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--color-primary) inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--color-primary) inset;
}

:deep(.el-form-item) {
  margin-bottom: 18px;
}
</style>
