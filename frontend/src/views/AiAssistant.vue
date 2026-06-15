<template>
  <div class="ai-container">
    <div class="chat-wrapper">
      <AiChatHeader :disabled="messages.length === 0" @clear="clearHistory" />

      <AiMessageList
        v-model="messagesRef"
        :messages="messages"
        :waiting="waitingResponse"
        :render-markdown="renderMarkdown"
        @quick="sendQuickMessage"
      />

      <AiChatInput
        v-model="inputMessage"
        :waiting="waitingResponse"
        :typing="isTyping"
        @send="handleSend"
      />
    </div>
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AiChatHeader from '@/components/business/AiChatHeader.vue'
import AiChatInput from '@/components/business/AiChatInput.vue'
import AiMessageList from '@/components/business/AiMessageList.vue'
import { useAiChat } from '@/composables/useAiChat'

const router = useRouter()
const {
  messages,
  inputMessage,
  waitingResponse,
  isTyping,
  messagesRef,
  restoreMessages,
  renderMarkdown,
  handleSend,
  sendQuickMessage,
  clearHistory,
  checkUserScroll,
  scrollToBottom
} = useAiChat()

const handleMessageClick = (event) => {
  const card = event.target.closest('.product-card-inline')
  const id = card?.dataset?.id
  if (id) router.push(`/product/${id}`)
}

onMounted(async () => {
  restoreMessages()
  await nextTick()
  if (messages.value.length > 0) {
    scrollToBottom()
  }
  if (messagesRef.value) {
    messagesRef.value.addEventListener('click', handleMessageClick)
    messagesRef.value.addEventListener('scroll', checkUserScroll, { passive: true })
  }
})

onBeforeUnmount(() => {
  if (messagesRef.value) {
    messagesRef.value.removeEventListener('click', handleMessageClick)
    messagesRef.value.removeEventListener('scroll', checkUserScroll)
  }
})
</script>

<style scoped>
.ai-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 64px - 120px);
  padding: 12px 16px;
  background: var(--color-bg-page);
}

.chat-wrapper {
  width: 100%;
  max-width: 1100px;
  height: calc(100vh - 64px - 120px - 24px);
  min-height: 500px;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  overflow: hidden;
}

/* ── 响应式 ── */
@media (max-width: 768px) {
  .ai-container {
    padding: 0;
  }

  .chat-wrapper {
    height: calc(100vh - 64px);
    border-radius: 0;
    box-shadow: none;
    min-height: auto;
  }

}
</style>
