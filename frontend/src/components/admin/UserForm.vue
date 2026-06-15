<template>
  <el-form ref="formRef" :model="modelValue" :rules="rules" label-width="80px">
    <el-form-item label="用户名" prop="username">
      <el-input v-model="modelValue.username" :disabled="isEdit" placeholder="请输入用户名" />
    </el-form-item>
    <el-form-item label="昵称" prop="nickname">
      <el-input v-model="modelValue.nickname" placeholder="请输入昵称" />
    </el-form-item>
    <el-form-item label="密码" prop="password">
      <el-input v-model="modelValue.password" type="password" show-password :placeholder="isEdit ? '不修改请留空' : '请输入密码'" />
    </el-form-item>
    <el-form-item label="手机号" prop="phone">
      <el-input v-model="modelValue.phone" placeholder="请输入手机号" />
    </el-form-item>
    <el-form-item label="邮箱" prop="email">
      <el-input v-model="modelValue.email" placeholder="请输入邮箱" />
    </el-form-item>
    <el-form-item label="角色" prop="role">
      <el-select v-model="modelValue.role" placeholder="请选择角色">
        <el-option label="普通用户" :value="0" />
        <el-option label="管理员" :value="1" />
      </el-select>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { ref } from 'vue'

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
