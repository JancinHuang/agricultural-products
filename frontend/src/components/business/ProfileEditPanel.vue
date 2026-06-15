<template>
  <div class="edit-panel">
    <el-tabs v-model="activeTab" class="edit-tabs">
      <el-tab-pane label="修改信息" name="info">
        <el-form
          ref="infoFormRef"
          :model="infoForm"
          :rules="infoRules"
          label-width="80px"
          class="edit-form"
        >
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
            <el-button type="primary" :loading="infoLoading" @click="$emit('save-info')">
              保存修改
            </el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
      <el-tab-pane label="修改密码" name="password">
        <el-form
          ref="pwdFormRef"
          :model="pwdForm"
          :rules="pwdRules"
          label-width="100px"
          class="edit-form"
        >
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
            <el-button type="primary" :loading="pwdLoading" @click="$emit('save-password')">
              修改密码
            </el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref } from 'vue'

defineProps({
  infoForm: {
    type: Object,
    required: true
  },
  pwdForm: {
    type: Object,
    required: true
  },
  infoRules: {
    type: Object,
    required: true
  },
  pwdRules: {
    type: Object,
    required: true
  },
  infoLoading: {
    type: Boolean,
    default: false
  },
  pwdLoading: {
    type: Boolean,
    default: false
  }
})

defineEmits(['save-info', 'save-password'])

const activeTab = ref('info')
const infoFormRef = ref(null)
const pwdFormRef = ref(null)

defineExpose({
  validateInfo: () => infoFormRef.value?.validate().catch(() => false),
  validatePassword: () => pwdFormRef.value?.validate().catch(() => false)
})
</script>

<style scoped>
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
</style>
