<template>
  <div class="edit-panel">
    <el-tabs v-model="activeTab" class="edit-tabs">
      <el-tab-pane label="修改信息" name="info">
        <BaseForm
          ref="infoFormRef"
          :model="infoForm"
          :rules="infoRules"
          label-width="80px"
          class="edit-form"
        >
          <BaseFormItem label="昵称" prop="nickname">
            <BaseInput v-model="infoForm.nickname" placeholder="请输入昵称" />
          </BaseFormItem>
          <BaseFormItem label="手机号" prop="phone">
            <BaseInput v-model="infoForm.phone" placeholder="请输入手机号" />
          </BaseFormItem>
          <BaseFormItem label="邮箱" prop="email">
            <BaseInput v-model="infoForm.email" placeholder="请输入邮箱" />
          </BaseFormItem>
          <BaseFormItem>
            <BaseButton type="primary" :loading="infoLoading" @click="$emit('save-info')">
              保存修改
            </BaseButton>
          </BaseFormItem>
        </BaseForm>
      </el-tab-pane>
      <el-tab-pane label="修改密码" name="password">
        <BaseForm
          ref="pwdFormRef"
          :model="pwdForm"
          :rules="pwdRules"
          label-width="100px"
          class="edit-form"
        >
          <BaseFormItem label="原密码" prop="oldPassword">
            <BaseInput v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
          </BaseFormItem>
          <BaseFormItem label="新密码" prop="newPassword">
            <BaseInput v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码" />
          </BaseFormItem>
          <BaseFormItem label="确认新密码" prop="confirmPassword">
            <BaseInput v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
          </BaseFormItem>
          <BaseFormItem>
            <BaseButton type="primary" :loading="pwdLoading" @click="$emit('save-password')">
              修改密码
            </BaseButton>
          </BaseFormItem>
        </BaseForm>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseForm from '@/components/base/BaseForm.vue'
import BaseFormItem from '@/components/base/BaseFormItem.vue'
import BaseInput from '@/components/base/BaseInput.vue'

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
