import { computed, nextTick, onBeforeUnmount, ref } from 'vue'
import { Marked } from 'marked'
import { sendAiMessage } from '@/api/ai'

const CHAT_STORAGE_KEY = 'ai_chat_messages'

const marked = new Marked({
  breaks: true,
  gfm: true
})

const renderer = new marked.Renderer()
renderer.link = function (href, title, text) {
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

export const useAiChat = () => {
  const messages = ref([])
  const inputMessage = ref('')
  const waitingResponse = ref(false)
  const isTyping = ref(false)
  const messagesRef = ref(null)
  const isUserAtBottom = ref(true)
  let typingTimer = null

  const conversationHistory = computed(() => {
    return messages.value
      .filter(message => !message.isTyping)
      .slice(-10)
      .map(message => ({ role: message.role, content: message.content }))
  })

  const restoreMessages = () => {
    try {
      const saved = sessionStorage.getItem(CHAT_STORAGE_KEY)
      if (!saved) return
      messages.value = JSON.parse(saved).map(message => ({
        role: message.role,
        content: message.content,
        displayContent: message.content,
        isTyping: false
      }))
    } catch (error) {
      console.error('[AiAssistant] Failed to restore messages:', error)
    }
  }

  const saveMessages = () => {
    try {
      const toSave = messages.value
        .filter(message => !message.isTyping)
        .map(message => ({ role: message.role, content: message.content }))
      sessionStorage.setItem(CHAT_STORAGE_KEY, JSON.stringify(toSave))
    } catch (error) {
      console.error('[AiAssistant] Failed to save messages:', error)
    }
  }

  const renderMarkdown = (text) => {
    if (!text) return ''

    const placeholder = '\x00PRODUCT_CARD\x00'
    const productCards = []
    const processed = text.replace(/\[商品:([^:\]]+):(\d+)\]/g, (_, name, id) => {
      productCards.push(
        `<div class="product-card-inline" data-id="${id}">` +
          `<span class="card-icon">🛒</span>` +
          `<span class="card-name">${name}</span>` +
          `<span class="card-action">查看详情 →</span>` +
        `</div>`
      )
      return placeholder
    })

    let html = marked.parse(processed)
    productCards.forEach(card => {
      html = html.replace(placeholder, card)
    })
    return html
  }

  const checkUserScroll = () => {
    const el = messagesRef.value
    if (!el) return
    isUserAtBottom.value = el.scrollHeight - el.scrollTop - el.clientHeight < 80
  }

  const scrollToBottom = async () => {
    await nextTick()
    if (messagesRef.value && isUserAtBottom.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  }

  const startTypewriter = (fullText) => {
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
    const tick = () => {
      charIndex += 2
      if (charIndex >= fullText.length) {
        messages.value[msgIndex].displayContent = fullText
        messages.value[msgIndex].isTyping = false
        isTyping.value = false
        saveMessages()
        scrollToBottom()
        return
      }

      messages.value[msgIndex].displayContent = fullText.substring(0, charIndex)
      scrollToBottom()
      typingTimer = setTimeout(tick, 20)
    }

    typingTimer = setTimeout(tick, 20)
  }

  const handleSend = async () => {
    const text = inputMessage.value.trim()
    if (!text || waitingResponse.value || isTyping.value) return

    messages.value.push({ role: 'user', content: text })
    inputMessage.value = ''
    waitingResponse.value = true
    await scrollToBottom()

    try {
      const res = await sendAiMessage({
        message: text,
        history: conversationHistory.value.slice(0, -1)
      })

      if (res.data?.message) {
        startTypewriter(res.data.message)
        return
      }

      messages.value.push({
        role: 'assistant',
        content: '抱歉，我暂时无法回答，请稍后再试 😥',
        displayContent: '抱歉，我暂时无法回答，请稍后再试 😥'
      })
      waitingResponse.value = false
      saveMessages()
    } catch (error) {
      console.error('[AiAssistant] Failed to send message:', error)
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

  const sendQuickMessage = (text) => {
    if (waitingResponse.value || isTyping.value) return
    inputMessage.value = text
    handleSend()
  }

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

  const stopTyping = () => {
    if (typingTimer) {
      clearTimeout(typingTimer)
      typingTimer = null
    }
  }

  onBeforeUnmount(() => {
    stopTyping()
    saveMessages()
  })

  return {
    messages,
    inputMessage,
    waitingResponse,
    isTyping,
    messagesRef,
    restoreMessages,
    saveMessages,
    renderMarkdown,
    handleSend,
    sendQuickMessage,
    clearHistory,
    checkUserScroll,
    scrollToBottom
  }
}
