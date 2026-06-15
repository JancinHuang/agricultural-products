<template>
  <div class="profile-page">
    <div class="profile-wrapper">
      <ProfileBanner :user="userInfo" />

      <div class="profile-content">
        <ProfileInfoPanel :user="userInfo" />
        <ProfileEditPanel
          ref="editPanelRef"
          :info-form="infoForm"
          :pwd-form="pwdForm"
          :info-rules="infoRules"
          :pwd-rules="pwdRules"
          :info-loading="infoLoading"
          :pwd-loading="pwdLoading"
          @save-info="handleUpdateInfo"
          @save-password="handleUpdatePassword"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { updateUserInfo, updatePassword } from '@/api/user'
import { setStoredUser } from '@/utils/auth'
import ProfileBanner from '@/components/business/ProfileBanner.vue'
import ProfileEditPanel from '@/components/business/ProfileEditPanel.vue'
import ProfileInfoPanel from '@/components/business/ProfileInfoPanel.vue'
import { notify } from '@/services/uiFeedback'

const userStore = useUserStore()
const userInfo = ref(userStore.userInfo)
const editPanelRef = ref(null)
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
  const valid = await editPanelRef.value?.validateInfo()
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
    notify.success('修改成功')
  } catch (error) {
    console.error(error)
  } finally {
    infoLoading.value = false
  }
}

const handleUpdatePassword = async () => {
  const valid = await editPanelRef.value?.validatePassword()
  if (!valid) return

  pwdLoading.value = true
  try {
    await updatePassword({
      userId: userInfo.value.id,
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    notify.success('密码修改成功，请重新登录')
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

/* ── 内容区 ── */
.profile-content {
  display: grid;
  grid-template-columns: 1fr 1.6fr;
  gap: 24px;
}

/* ── Responsive ── */
@media (max-width: 768px) {
  .profile-wrapper {
    padding: 16px;
  }

  .profile-content {
    grid-template-columns: 1fr;
  }
}
</style>
