<template>
  <div class="profile-page">
    <div class="profile-wrapper">
      <!-- 个人资料头部 -->
      <div class="profile-banner">
        <div class="banner-inner">
          <el-avatar :size="80" :src="userInfo?.avatar" class="banner-avatar">
            <el-icon :size="36"><UserFilled /></el-icon>
          </el-avatar>
          <div class="banner-info">
            <h2>{{ userInfo?.nickname || userInfo?.username }}</h2>
            <div class="banner-meta">
              <el-tag :type="userInfo?.role === 1 ? 'danger' : ''" size="small" effect="dark">
                {{ userInfo?.role === 1 ? '管理员' : '普通用户' }}
              </el-tag>
              <span class="join-time">
                <el-icon><Clock /></el-icon>
                {{ formatTime(userInfo?.createTime) }} 加入
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 信息面板 -->
      <div class="profile-content">
        <!-- 左侧：基本信息展示 -->
        <div class="info-panel">
          <div class="panel-header">
            <h3>基本信息</h3>
          </div>
          <div class="info-list">
            <div class="info-item">
              <div class="info-icon"><el-icon><User /></el-icon></div>
              <div class="info-detail">
                <span class="info-label">用户名</span>
                <span class="info-value">{{ userInfo?.username }}</span>
              </div>
            </div>
            <div class="info-item">
              <div class="info-icon"><el-icon><Phone /></el-icon></div>
              <div class="info-detail">
                <span class="info-label">手机号</span>
                <span class="info-value">{{ userInfo?.phone || '未设置' }}</span>
              </div>
            </div>
            <div class="info-item">
              <div class="info-icon"><el-icon><Message /></el-icon></div>
              <div class="info-detail">
                <span class="info-label">邮箱</span>
                <span class="info-value">{{ userInfo?.email || '未设置' }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧：编辑区 -->
        <div class="edit-panel">
          <el-tabs v-model="activeTab" class="edit-tabs">
            <el-tab-pane label="修改信息" name="info">
              <el-form ref="infoFormRef" :model="infoForm" :rules="infoRules" label-width="80px" class="edit-form">
                <el-form-item label="昵称" prop="nickname">
                  <el-input v-model="infoForm.nickname" placeholder="请输入昵称" />
                </el-form-item>
                <el-form-item label="手机号" prop="phone">
                  <el-input v-model="infoForm.phone" placeholder="请输入手机号" />
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="infoForm.email" placeholder="请输入邮箱" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="infoLoading" @click="handleUpdateInfo">保存修改</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            <el-tab-pane label="修改密码" name="password">
              <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="100px" class="edit-form">
                <el-form-item label="原密码" prop="oldPassword">
                  <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码" />
                </el-form-item>
                <el-form-item label="确认新密码" prop="confirmPassword">
                  <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="pwdLoading" @click="handleUpdatePassword">修改密码</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { UserFilled, User, Phone, Message, Clock } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { updateUserInfo, updatePassword } from '@/api/user'
import { formatTime } from '@/utils/time'
import { setStoredUser } from '@/utils/auth'

const userStore = useUserStore()
const userInfo = ref(userStore.userInfo)
const activeTab = ref('info')
const infoFormRef = ref(null)
const pwdFormRef = ref(null)
const infoLoading = ref(false)
const pwdLoading = ref(false)

const infoForm = reactive({
  nickname: '',
  phone: '',
  email: ''
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const infoRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ]
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

onMounted(() => {
  if (userInfo.value) {
    infoForm.nickname = userInfo.value.nickname || ''
    infoForm.phone = userInfo.value.phone || ''
    infoForm.email = userInfo.value.email || ''
  }
})

const handleUpdateInfo = async () => {
  const valid = await infoFormRef.value.validate().catch(() => false)
  if (!valid) return

  infoLoading.value = true
  try {
    await updateUserInfo({
      id: userInfo.value.id,
      nickname: infoForm.nickname,
      phone: infoForm.phone,
      email: infoForm.email
    })
    userInfo.value.nickname = infoForm.nickname
    userInfo.value.phone = infoForm.phone
    userInfo.value.email = infoForm.email
    userStore.userInfo = userInfo.value
    setStoredUser(userInfo.value)
    ElMessage.success('修改成功')
  } catch (error) {
    console.error(error)
  } finally {
    infoLoading.value = false
  }
}

const handleUpdatePassword = async () => {
  const valid = await pwdFormRef.value.validate().catch(() => false)
  if (!valid) return

  pwdLoading.value = true
  try {
    await updatePassword({
      userId: userInfo.value.id,
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    userStore.logout()
  } catch (error) {
    console.error(error)
  } finally {
    pwdLoading.value = false
  }
}
</script>

<style scoped>
.profile-page {
  background: var(--color-bg-page);
  min-height: 100%;
}

.profile-wrapper {
  max-width: 960px;
  margin: 0 auto;
  padding: 24px;
}

/* ── 个人资料头部 ── */
.profile-banner {
  background: linear-gradient(135deg, #1b5e20 0%, #2e7d32 50%, #4caf50 100%);
  border-radius: var(--radius-lg);
  padding: 32px;
  margin-bottom: 24px;
  color: #fff;
}

.banner-inner {
  display: flex;
  align-items: center;
  gap: 24px;
}

.banner-avatar {
  border: 3px solid rgba(255, 255, 255, 0.3);
  flex-shrink: 0;
}

.banner-info h2 {
  font-size: 22px;
  font-weight: 700;
  margin: 0 0 8px;
  color: #fff;
}

.banner-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.join-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.8);
}

/* ── 内容区 ── */
.profile-content {
  display: grid;
  grid-template-columns: 1fr 1.6fr;
  gap: 24px;
}

/* ── 左侧信息面板 ── */
.info-panel {
  background: #fff;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

.panel-header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--color-border-light);
}

.panel-header h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
  color: var(--color-text-primary);
}

.info-list {
  padding: 8px 0;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 20px;
  transition: background var(--transition-fast);
}

.info-item:hover {
  background: var(--color-bg-section);
}

.info-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: var(--color-primary-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-primary);
  flex-shrink: 0;
}

.info-detail {
  flex: 1;
  min-width: 0;
}

.info-label {
  display: block;
  font-size: 12px;
  color: var(--color-text-placeholder);
  margin-bottom: 2px;
}

.info-value {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ── 右侧编辑面板 ── */
.edit-panel {
  background: #fff;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  padding: 4px 20px 20px;
}

.edit-tabs :deep(.el-tabs__header) {
  margin-bottom: 20px;
}

.edit-form {
  max-width: 400px;
}

/* ── Responsive ── */
@media (max-width: 768px) {
  .profile-wrapper {
    padding: 16px;
  }

  .banner-inner {
    flex-direction: column;
    text-align: center;
  }

  .banner-meta {
    justify-content: center;
  }

  .profile-content {
    grid-template-columns: 1fr;
  }
}
</style>
