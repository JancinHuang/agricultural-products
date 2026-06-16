<template>
  <div class="checkout-panel">
    <div class="checkout-panel__title">
      <el-icon><Location /></el-icon>
      <span>收货信息</span>
    </div>
    <BaseForm ref="formRef" :model="modelValue" :rules="rules" label-width="90px" class="checkout-address-form">
      <BaseFormItem label="收货人" prop="receiverName">
        <BaseInput v-model="modelValue.receiverName" placeholder="请输入收货人姓名" />
      </BaseFormItem>
      <BaseFormItem label="联系电话" prop="receiverPhone">
        <BaseInput v-model="modelValue.receiverPhone" placeholder="请输入联系电话" />
      </BaseFormItem>
      <BaseFormItem label="收货地址" prop="receiverAddress">
        <BaseInput v-model="modelValue.receiverAddress" type="textarea" :rows="2" placeholder="请输入详细地址" />
      </BaseFormItem>
      <BaseFormItem label="备注">
        <BaseInput v-model="modelValue.remark" placeholder="选填，可以告诉卖家您的特殊需求" />
      </BaseFormItem>
    </BaseForm>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Location } from '@element-plus/icons-vue'
import BaseForm from '@/components/base/BaseForm.vue'
import BaseFormItem from '@/components/base/BaseFormItem.vue'
import BaseInput from '@/components/base/BaseInput.vue'

defineProps({
  modelValue: {
    type: Object,
    required: true
  }
})

const formRef = ref(null)

const rules = {
  receiverName: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' }
  ],
  receiverPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  receiverAddress: [
    { required: true, message: '请输入收货地址', trigger: 'blur' }
  ]
}

const validate = () => formRef.value?.validate?.()

defineExpose({
  validate
})
</script>

<style scoped>
.checkout-panel {
  overflow: hidden;
  margin-bottom: var(--spacing-lg);
  background: var(--color-white);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
}

.checkout-panel__title {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-md) var(--spacing-lg);
  color: var(--color-text-primary);
  background: var(--color-bg-section);
  border-bottom: 1px solid var(--color-border-light);
  font-size: var(--font-size-md);
  font-weight: 600;
}

.checkout-panel__title .el-icon {
  color: var(--color-primary);
}

.checkout-address-form {
  padding: var(--spacing-lg) var(--spacing-lg) 4px;
}

.checkout-address-form :deep(.el-form-item__label) {
  font-weight: 500;
}
</style>
