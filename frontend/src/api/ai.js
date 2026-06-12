import request from '@/utils/request'

/**
 * 发送消息给 AI 购物助手
 * @param {Object} data - { message: string, history: Array<{role: string, content: string}> }
 */
export function sendAiMessage(data) {
  return request.post('/ai/chat', data, { timeout: 30000 })
}
