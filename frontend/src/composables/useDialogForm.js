import { computed, reactive, ref } from 'vue'

export function useDialogForm(options) {
  const {
    defaults,
    normalize = item => item,
    submitCreate,
    submitUpdate,
    title = {
      create: '添加',
      edit: '编辑'
    }
  } = options

  const getDefaults = () => (typeof defaults === 'function' ? defaults() : defaults)

  const visible = ref(false)
  const isEdit = ref(false)
  const submitLoading = ref(false)
  const formRef = ref(null)
  const form = reactive({ ...getDefaults() })
  const dialogTitle = computed(() => (isEdit.value ? title.edit : title.create))

  const resetForm = (extra = {}) => {
    Object.assign(form, getDefaults(), extra)
  }

  const openCreate = (extra = {}) => {
    isEdit.value = false
    resetForm(extra)
    visible.value = true
  }

  const openEdit = (row) => {
    isEdit.value = true
    resetForm(normalize(row))
    visible.value = true
  }

  const close = () => {
    visible.value = false
  }

  const submit = async (payloadBuilder, onSuccess) => {
    const valid = await formRef.value?.validate?.().catch(() => false)
    if (!valid) return false

    submitLoading.value = true
    try {
      const payload = payloadBuilder ? payloadBuilder({ ...form }, isEdit.value) : { ...form }
      if (isEdit.value) {
        await submitUpdate(payload)
      } else {
        await submitCreate(payload)
      }
      visible.value = false
      await onSuccess?.(payload, isEdit.value)
      return true
    } finally {
      submitLoading.value = false
    }
  }

  return {
    visible,
    isEdit,
    submitLoading,
    formRef,
    form,
    dialogTitle,
    resetForm,
    openCreate,
    openEdit,
    close,
    submit
  }
}
