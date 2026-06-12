<template>
  <div class="ai-container">
    <div class="chat-wrapper">
      <!-- 头部 -->
      <div class="chat-header">
        <div class="header-info">
          <div class="header-icon">🌿</div>
          <div>
            <h2>AI 购物助手</h2>
            <p>智能推荐，帮你选好物</p>
          </div>
        </div>
        <el-button text @click="clearHistory" :disabled="messages.length === 0">
          <el-icon><Delete /></el-icon>
          清空对话
        </el-button>
      </div>

      <!-- 消息列表 -->
      <div class="chat-messages" ref="messagesRef">
        <!-- 欢迎消息 -->
        <div class="message ai-message">
          <div class="message-avatar">🤖</div>
          <div class="message-content welcome-content">
            <p>你好！我是助农商城的AI购物助手 🌿</p>
            <p>我可以帮你推荐商品、比较价格、了解产地信息。试试下面的问题吧：</p>
            <div class="quick-actions">
              <el-button size="small" round @click="sendQuickMessage('推荐一些热销的水果')">
                🔥 热销水果
              </el-button>
              <el-button size="small" round @click="sendQuickMessage('有什么适合送礼的农产品？')">
                🎁 送礼推荐
              </el-button>
              <el-button size="small" round @click="sendQuickMessage('帮我找一些有机蔬菜')">
                🥬 有机蔬菜
              </el-button>
              <el-button size="small" round @click="sendQuickMessage('最近有什么新品上架？')">
                🌱 新品推荐
              </el-button>
              <el-button size="small" round @click="sendQuickMessage('100元以内有什么好物推荐？')">
                💰 百元好物
              </el-button>
            </div>
          </div>
        </div>

        <!-- 动态消息 -->
        <div
          v-for="(msg, index) in messages"
          :key="index"
          :class="['message', msg.role === 'user' ? 'user-message' : 'ai-message']"
        >
          <div class="message-avatar">{{ msg.role === 'user' ? '😊' : '🤖' }}</div>
          <!-- AI 消息用 Markdown 渲染 -->
          <div
            v-if="msg.role === 'assistant'"
            class="message-content markdown-body"
            v-html="renderMarkdown(msg.displayContent || '')"
          ></div>
          <!-- 用户消息纯文本 -->
          <div v-else class="message-content">{{ msg.content }}</div>
        </div>

        <!-- 打字指示器（等待 API 响应时） -->
        <div v-if="waitingResponse" class="message ai-message">
          <div class="message-avatar">🤖</div>
          <div class="message-content typing-indicator">
            <span></span><span></span><span></span>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="chat-input-area">
        <div class="input-wrapper">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="2"
            placeholder="输入你想了解的商品或需求，按 Enter 发送..."
            resize="none"
            @keydown.enter.exact.prevent="handleSend"
          />
          <el-button
            type="primary"
            :loading="waitingResponse || isTyping"
            :disabled="!inputMessage.trim() || waitingResponse || isTyping"
            @click="handleSend"
          >
            <el-icon v-if="!waitingResponse && !isTyping"><Promotion /></el-icon>
            {{ waitingResponse ? '思考中...' : isTyping ? '回复中...' : '发送' }}
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { Delete, Promotion } from '@element-plus/icons-vue'
import { Marked } from 'marked'
import { sendAiMessage } from '@/api/ai'

const CHAT_STORAGE_KEY = 'ai_chat_messages'

const router = useRouter()
const messages = ref([])
const inputMessage = ref('')
const waitingResponse = ref(false)
const isTyping = ref(false)
const messagesRef = ref(null)
let typingTimer = null

// ── 从 sessionStorage 恢复聊天记录 ──
const restoreMessages = () => {
  try {
    const saved = sessionStorage.getItem(CHAT_STORAGE_KEY)
    if (saved) {
      const parsed = JSON.parse(saved)
      // 只恢复已完成的消息（不含正在打字的）
      messages.value = parsed.map(m => ({
        role: m.role,
        content: m.content,
        displayContent: m.content, // 恢复时直接显示完整内容
        isTyping: false
      }))
    }
  } catch (e) {
    // 忽略解析错误
  }
}

// ── 保存聊天记录到 sessionStorage ──
const saveMessages = () => {
  try {
    const toSave = messages.value
      .filter(m => !m.isTyping) // 不保存正在打字的消息
      .map(m => ({ role: m.role, content: m.content }))
    sessionStorage.setItem(CHAT_STORAGE_KEY, JSON.stringify(toSave))
  } catch (e) {
    // 忽略存储错误
  }
}

// ── Marked 实例配置 ──
const marked = new Marked({
  breaks: true,
  gfm: true
})

// 自定义 renderer：让链接在新标签打开
const renderer = new marked.Renderer()
const originalLinkRenderer = renderer.link
renderer.link = function (href, title, text) {
  // marked v15+ passes a token object
  if (typeof href === 'object') {
    const token = href
    const attrs = []
    if (token.href) attrs.push(`href="${token.href}"`)
    if (token.title) attrs.push(`title="${token.title}"`)
    attrs.push('target="_blank" rel="noopener noreferrer"')
    return `<a ${attrs.join(' ')}>${token.text || token.tokens?.map(t => t.raw).join('') || ''}</a>`
  }
  return `<a href="${href}" ${title ? `title="${title}"` : ''} target="_blank" rel="noopener noreferrer">${text}</a>`
}
marked.use({ renderer })

// 对话历史
const conversationHistory = computed(() => {
  return messages.value
    .filter(m => !m.isTyping)
    .slice(-10)
    .map(m => ({ role: m.role, content: m.content }))
})

// ── Markdown 渲染 ──
const renderMarkdown = (text) => {
  if (!text) return ''

  // 先把 [商品:名称:ID] 转为 HTML 占位（在 marked 解析之前）
  const placeholder = '\x00PRODUCT_CARD\x00'
  const productCards = []
  let processed = text.replace(/\[商品:([^:\]]+):(\d+)\]/g, (_, name, id) => {
    productCards.push(
      `<div class="product-card-inline" data-id="${id}">` +
        `<span class="card-icon">🛒</span>` +
        `<span class="card-name">${name}</span>` +
        `<span class="card-action">查看详情 →</span>` +
      `</div>`
    )
    return placeholder
  })

  // marked 解析
  let html = marked.parse(processed)

  // 还原商品卡片
  productCards.forEach(card => {
    html = html.replace(placeholder, card)
  })

  return html
}

// ── 发送消息 ──
const handleSend = async () => {
  const text = inputMessage.value.trim()
  if (!text || waitingResponse.value || isTyping.value) return

  // 添加用户消息
  messages.value.push({ role: 'user', content: text })
  inputMessage.value = ''
  waitingResponse.value = true
  await scrollToBottom()

  try {
    const res = await sendAiMessage({
      message: text,
      history: conversationHistory.value.slice(0, -1)
    })

    if (res.data && res.data.message) {
      // 添加 AI 消息并启动打字机效果
      startTypewriter(res.data.message)
    } else {
      messages.value.push({
        role: 'assistant',
        content: '抱歉，我暂时无法回答，请稍后再试 😥',
        displayContent: '抱歉，我暂时无法回答，请稍后再试 😥'
      })
      waitingResponse.value = false
      saveMessages()
    }
  } catch (error) {
    messages.value.push({
      role: 'assistant',
      content: '网络似乎有点问题，请检查网络后重试 🔄',
      displayContent: '网络似乎有点问题，请检查网络后重试 🔄'
    })
    waitingResponse.value = false
    saveMessages()
    await scrollToBottom()
  }
}

// ── 打字机效果 ──
const startTypewriter = (fullText) => {
  // 先添加一条空的 AI 消息
  const msgIndex = messages.value.length
  messages.value.push({
    role: 'assistant',
    content: fullText,
    displayContent: '',
    isTyping: true
  })

  waitingResponse.value = false
  isTyping.value = true

  let charIndex = 0
  const speed = 20 // 每个字符的间隔（ms）
  const charsPerTick = 2 // 每次显示的字符数

  const tick = () => {
    charIndex += charsPerTick
    if (charIndex >= fullText.length) {
      // 打字完成
      messages.value[msgIndex].displayContent = fullText
      messages.value[msgIndex].isTyping = false
      isTyping.value = false
      saveMessages()
      scrollToBottom()
      return
    }

    messages.value[msgIndex].displayContent = fullText.substring(0, charIndex)
    scrollToBottom()
    typingTimer = setTimeout(tick, speed)
  }

  typingTimer = setTimeout(tick, speed)
}

// 快捷提问
const sendQuickMessage = (text) => {
  if (waitingResponse.value || isTyping.value) return
  inputMessage.value = text
  handleSend()
}

// 清空对话
const clearHistory = () => {
  if (typingTimer) {
    clearTimeout(typingTimer)
    typingTimer = null
  }
  messages.value = []
  isTyping.value = false
  waitingResponse.value = false
  sessionStorage.removeItem(CHAT_STORAGE_KEY)
}

// ── 智能滚动：用户在底部附近时才自动跟随 ──
const isUserAtBottom = ref(true)

const checkUserScroll = () => {
  const el = messagesRef.value
  if (!el) return
  // 距离底部 80px 以内视为"在底部"
  const threshold = 80
  isUserAtBottom.value = el.scrollHeight - el.scrollTop - el.clientHeight < threshold
}

const scrollToBottom = async () => {
  await nextTick()
  // 只有用户当前在底部附近时才自动跟随
  if (messagesRef.value && isUserAtBottom.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}

// 事件委托：点击商品卡片跳转
const handleMessageClick = (e) => {
  const card = e.target.closest('.product-card-inline')
  if (card) {
    const id = card.dataset.id
    if (id) {
      router.push(`/product/${id}`)
    }
  }
}

onMounted(async () => {
  // 恢复聊天记录
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
  if (typingTimer) {
    clearTimeout(typingTimer)
    typingTimer = null
  }
  // 保存聊天记录
  saveMessages()
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

/* ── 头部 ── */
.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
  color: #fff;
  flex-shrink: 0;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon {
  font-size: 32px;
  line-height: 1;
}

.chat-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.chat-header p {
  margin: 2px 0 0;
  font-size: 12px;
  opacity: 0.85;
}

.chat-header .el-button {
  color: rgba(255, 255, 255, 0.85);
}

.chat-header .el-button:hover {
  color: #fff;
}

/* ── 消息列表 ── */
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

/* ── Markdown 内容样式 ── */
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

/* 快捷提问按钮 */
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

/* 商品推荐卡片（通过 v-html 渲染，需要 :deep 穿透 scoped） */
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

/* 打字指示器 */
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

/* ── 输入区域 ── */
.chat-input-area {
  padding: 16px 24px;
  background: #fff;
  border-top: 1px solid #ebeef5;
  flex-shrink: 0;
}

.input-wrapper {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.input-wrapper :deep(.el-textarea__inner) {
  border-radius: var(--radius-md);
  font-size: 14px;
}

.input-wrapper :deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 1px var(--color-primary) inset;
}

.input-wrapper .el-button {
  height: 64px;
  border-radius: var(--radius-md);
  padding: 0 20px;
  font-size: 15px;
  flex-shrink: 0;
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

  .chat-messages {
    padding: 16px;
  }

  .message {
    max-width: 96%;
  }

  .user-message {
    max-width: 82%;
  }

  .chat-header {
    padding: 12px 16px;
  }

  .chat-input-area {
    padding: 12px 16px;
  }

  .input-wrapper .el-button {
    height: 56px;
    padding: 0 16px;
  }

  .quick-actions {
    gap: 6px;
  }
}
</style>
