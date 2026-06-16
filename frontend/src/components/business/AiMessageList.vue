<template>
  <div ref="listRef" class="chat-messages">
    <div class="message ai-message">
      <div class="message-avatar">🤖</div>
      <div class="message-content welcome-content">
        <p>你好！我是助农商城的AI购物助手 🌿</p>
        <p>我可以帮你推荐商品、比较价格、了解产地信息。试试下面的问题吧：</p>
        <div class="quick-actions">
          <BaseButton
            v-for="action in quickActions"
            :key="action.text"
            size="small"
            round
            @click="$emit('quick', action.prompt)"
          >
            {{ action.text }}
          </BaseButton>
        </div>
      </div>
    </div>

    <div
      v-for="(msg, index) in messages"
      :key="index"
      :class="['message', msg.role === 'user' ? 'user-message' : 'ai-message']"
    >
      <div class="message-avatar">{{ msg.role === 'user' ? '😊' : '🤖' }}</div>
      <div
        v-if="msg.role === 'assistant'"
        class="message-content markdown-body"
        v-html="renderMarkdown(msg.displayContent || '')"
      ></div>
      <div v-else class="message-content">{{ msg.content }}</div>
    </div>

    <div v-if="waiting" class="message ai-message">
      <div class="message-avatar">🤖</div>
      <div class="message-content typing-indicator">
        <span></span><span></span><span></span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import BaseButton from '@/components/base/BaseButton.vue'

const props = defineProps({
  modelValue: {
    type: Object,
    default: null
  },
  messages: {
    type: Array,
    default: () => []
  },
  waiting: {
    type: Boolean,
    default: false
  },
  renderMarkdown: {
    type: Function,
    required: true
  }
})

const emit = defineEmits(['update:modelValue', 'quick'])

const quickActions = [
  { text: '🔥 热销水果', prompt: '推荐一些热销的水果' },
  { text: '🎁 送礼推荐', prompt: '有什么适合送礼的农产品？' },
  { text: '🥬 有机蔬菜', prompt: '帮我找一些有机蔬菜' },
  { text: '🌱 新品推荐', prompt: '最近有什么新品上架？' },
  { text: '💰 百元好物', prompt: '100元以内有什么好物推荐？' }
]

const listRef = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})
</script>

<style scoped>
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: var(--color-bg-section);
}

.message {
  display: flex;
  gap: 10px;
  max-width: 92%;
  animation: fadeIn 0.3s ease;
}

.user-message {
  max-width: 75%;
  align-self: flex-end;
  flex-direction: row-reverse;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.ai-message {
  align-self: flex-start;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  background: #fff;
  box-shadow: var(--shadow-sm);
  flex-shrink: 0;
}

.user-message .message-avatar {
  background: var(--color-primary-bg);
}

.message-content {
  padding: 12px 16px;
  border-radius: var(--radius-lg);
  font-size: 14px;
  line-height: 1.7;
  word-break: break-word;
}

.ai-message .message-content {
  background: #fff;
  color: var(--color-text-primary);
  border-top-left-radius: var(--radius-sm);
  box-shadow: var(--shadow-sm);
}

.user-message .message-content {
  background: var(--color-primary);
  color: #fff;
  border-top-right-radius: var(--radius-sm);
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3),
.markdown-body :deep(h4) {
  margin: 16px 0 8px;
  font-weight: 600;
  color: var(--color-text-primary);
  line-height: 1.4;
}

.markdown-body :deep(h1) { font-size: 20px; }
.markdown-body :deep(h2) { font-size: 17px; }
.markdown-body :deep(h3) { font-size: 15px; }
.markdown-body :deep(h4) { font-size: 14px; }

.markdown-body :deep(h1:first-child),
.markdown-body :deep(h2:first-child),
.markdown-body :deep(h3:first-child) {
  margin-top: 0;
}

.markdown-body :deep(p) {
  margin: 0 0 10px;
  line-height: 1.7;
}

.markdown-body :deep(p:last-child) {
  margin-bottom: 0;
}

.markdown-body :deep(strong) {
  font-weight: 600;
  color: var(--color-primary-dark);
}

.markdown-body :deep(em) {
  font-style: italic;
  color: var(--color-text-secondary);
}

.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  margin: 8px 0;
  padding-left: 20px;
}

.markdown-body :deep(li) {
  margin: 4px 0;
  line-height: 1.7;
}

.markdown-body :deep(ul li) {
  list-style-type: disc;
}

.markdown-body :deep(ol li) {
  list-style-type: decimal;
}

.markdown-body :deep(code) {
  background: #f0f2e9;
  color: var(--color-primary-dark);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
}

.markdown-body :deep(pre) {
  background: #1e1e1e;
  border-radius: var(--radius-md);
  padding: 14px 16px;
  margin: 10px 0;
  overflow-x: auto;
}

.markdown-body :deep(pre code) {
  background: none;
  color: #d4d4d4;
  padding: 0;
  font-size: 13px;
  line-height: 1.5;
}

.markdown-body :deep(blockquote) {
  margin: 10px 0;
  padding: 8px 14px;
  border-left: 3px solid var(--color-primary);
  background: var(--color-primary-bg);
  border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
  color: var(--color-text-secondary);
}

.markdown-body :deep(blockquote p) {
  margin: 0;
}

.markdown-body :deep(hr) {
  border: none;
  border-top: 1px solid #e8e8e8;
  margin: 12px 0;
}

.markdown-body :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 10px 0;
  font-size: 13px;
}

.markdown-body :deep(th),
.markdown-body :deep(td) {
  border: 1px solid #e0e0e0;
  padding: 8px 12px;
  text-align: left;
}

.markdown-body :deep(th) {
  background: var(--color-bg-section);
  font-weight: 600;
}

.markdown-body :deep(a) {
  color: var(--color-primary);
  text-decoration: none;
}

.markdown-body :deep(a:hover) {
  text-decoration: underline;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.quick-actions .el-button {
  border-color: var(--color-primary-lighter);
  color: var(--color-primary);
}

.quick-actions .el-button:hover {
  background: var(--color-primary-bg);
  border-color: var(--color-primary);
}

.markdown-body :deep(.product-card-inline) {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  margin: 8px 0;
  background: var(--color-primary-bg);
  border-left: 3px solid var(--color-primary);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.markdown-body :deep(.product-card-inline:hover) {
  box-shadow: var(--shadow-md);
  transform: translateX(2px);
}

.markdown-body :deep(.card-icon) {
  font-size: 18px;
}

.markdown-body :deep(.card-name) {
  flex: 1;
  font-weight: 500;
  color: var(--color-primary-dark);
}

.markdown-body :deep(.card-action) {
  font-size: 12px;
  color: var(--color-primary);
  white-space: nowrap;
}

.typing-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 16px 20px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-primary-lighter);
  animation: bounce 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes bounce {
  0%, 80%, 100% {
    transform: scale(0.6);
    opacity: 0.4;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

@media (max-width: 768px) {
  .chat-messages {
    padding: 16px;
  }

  .message {
    max-width: 96%;
  }

  .user-message {
    max-width: 82%;
  }

  .quick-actions {
    gap: 6px;
  }
}
</style>
