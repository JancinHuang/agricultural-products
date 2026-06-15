<template>
  <BaseForm ref="formRef" :model="modelValue" :rules="rules" label-width="80px">
    <BaseFormItem label="用户名" prop="username">
      <BaseInput v-model="modelValue.username" :disabled="isEdit" placeholder="请输入用户名" />
    </BaseFormItem>
    <BaseFormItem label="昵称" prop="nickname">
      <BaseInput v-model="modelValue.nickname" placeholder="请输入昵称" />
    </BaseFormItem>
    <BaseFormItem label="密码" prop="password">
      <BaseInput v-model="modelValue.password" type="password" show-password :placeholder="isEdit ? '不修改请留空' : '请输入密码'" />
    </BaseFormItem>
    <BaseFormItem label="手机号" prop="phone">
      <BaseInput v-model="modelValue.phone" placeholder="请输入手机号" />
    </BaseFormItem>
    <BaseFormItem label="邮箱" prop="email">
      <BaseInput v-model="modelValue.email" placeholder="请输入邮箱" />
    </BaseFormItem>
    <BaseFormItem label="角色" prop="role">
      <BaseSelect v-model="modelValue.role" placeholder="请选择角色">
        <BaseOption label="普通用户" :value="0" />
        <BaseOption label="管理员" :value="1" />
      </BaseSelect>
    </BaseFormItem>
  </BaseForm>
</template>

<script setup>
import { ref } from 'vue'
import BaseForm from '@/components/base/BaseForm.vue'
import BaseFormItem from '@/components/base/BaseFormItem.vue'
import BaseInput from '@/components/base/BaseInput.vue'
import BaseOption from '@/components/base/BaseOption.vue'
import BaseSelect from '@/components/base/BaseSelect.vue'

const props = defineProps({
  modelValue: {
    type: Object,
    required: true
  },
  isEdit: {
    type: Boolean,
    default: false
  }
})

const formRef = ref(null)

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  password: [
    {
      validator: (rule, value, callback) => {
        if (!props.isEdit && !value) {
          callback(new Error('请输入密码'))
        } else if (value && (value.length < 6 || value.length > 20)) {
          callback(new Error('密码长度为6-20个字符'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

defineExpose({
  validate: () => formRef.value?.validate?.()
})
</script>
