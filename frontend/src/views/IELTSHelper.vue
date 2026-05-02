<template>
  <div class="ielts-helper-container">
    <div class="chat-container">
      <div class="history-panel">
        <div class="history-header">
          <h3>历史记录</h3>
          <button class="new-chat-btn" @click="createNewConversation">新对话</button>
        </div>
        <div class="history-list">
          <div
            v-for="conversation in conversations"
            :key="conversation.id"
            class="history-item"
            :class="{ active: currentConversationId === conversation.id }"
            @click="selectConversation(conversation.id)"
          >
            <div class="conversation-title">{{ conversation.title }}</div>
            <div class="conversation-time">{{ formatTime(conversation.createTime) }}</div>
            <div class="conversation-actions">
              <button class="edit-btn" @click.stop="editConversationTitle(conversation.id, conversation.title)">编辑</button>
              <button class="delete-btn" @click.stop="deleteConversation(conversation.id)">删除</button>
            </div>
          </div>
        </div>
      </div>

      <div class="chat-panel">
        <div class="chat-header">
          <h3>{{ currentConversationTitle }}</h3>
        </div>
        <div class="chat-messages" ref="messagesContainer">
          <div v-if="!isLoggedIn" class="login-prompt">
            <p>请先 <router-link to="/login">登录</router-link> 后使用AI助手</p>
          </div>
          <div
            v-for="(message, index) in messages"
            :key="index"
            class="message"
            :class="{ 'user-message': message.role === 'user', 'assistant-message': message.role === 'assistant' }"
          >
            <div class="message-content">{{ message.content }}</div>
            <div class="message-time">{{ formatTime(message.createTime) }}</div>
          </div>
          <div v-if="isStreaming" class="message assistant-message">
            <div class="message-content streaming-content">{{ streamingContent }}</div>
          </div>
        </div>
        <div class="chat-input-area">
          <input
            type="text"
            v-model="inputMessage"
            placeholder="请输入您的问题..."
            @keyup.enter="sendMessage"
            :disabled="!isLoggedIn || isStreaming"
          />
          <button v-if="isStreaming" class="stop-btn" @click="stopGeneration">
            停止
          </button>
          <button v-else class="send-btn" @click="sendMessage" :disabled="!isLoggedIn || isStreaming">
            {{ isStreaming ? '回答中...' : '发送' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 编辑标题弹窗 -->
    <div v-if="showEditDialog" class="modal-overlay">
      <div class="modal-content">
        <div class="modal-header">
          <h3>编辑对话标题</h3>
          <button class="close-btn" @click="closeEditDialog">&times;</button>
        </div>
        <div class="modal-body">
          <input
            type="text"
            v-model="editingTitle"
            placeholder="请输入新标题"
            class="title-input"
            @keyup.enter="saveTitle"
          />
        </div>
        <div class="modal-footer">
          <button class="cancel-btn" @click="closeEditDialog">取消</button>
          <button class="save-btn" @click="saveTitle" :disabled="!editingTitle.trim()">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { updateConversationTitle,getConversations, createConversation, getConversationMessages, deleteConversation, sendMessageStream, stopMessageStream } from '@/api/ieltshelper'

export default {
  name: 'IELTSHelper',
  data() {
    return {
      conversations: [],
      messages: [],
      currentConversationId: null,
      currentConversationTitle: '新对话',
      inputMessage: '',
      isStreaming: false,
      streamingContent: '',
      showEditDialog: false,
      editingConversationId: null,
      editingTitle: '',
      currentReader: null
    }
  },
  computed: {
    isLoggedIn() {
      return !!localStorage.getItem('token')
    }
  },
  mounted() {
    if (this.isLoggedIn) {
      this.loadConversations()
    }
  },
  methods: {
    formatTime(time) {
      if (!time) return ''
      const date = new Date(time)
      return date.toLocaleString()
    },
    async loadConversations() {
      try {
        const response = await getConversations()
        if (response.code === 200) {
          this.conversations = response.data
          if (this.conversations.length > 0) {
            this.selectConversation(this.conversations[0].id)
          } else {
            this.createNewConversation()
          }
        } else if (response.code === 401) {
          console.warn('请先登录')
        }
      } catch (error) {
        console.error('Error loading conversations:', error)
      }
    },
    async createNewConversation() {
      try {
        const title = new Date().toLocaleString()
        const response = await createConversation(title)
        if (response.code === 200) {
          const newConversation = response.data
          this.conversations.unshift(newConversation)
          this.selectConversation(newConversation.id)
        }
      } catch (error) {
        console.error('Error creating conversation:', error)
      }
    },
    async selectConversation(conversationId) {
      this.currentConversationId = conversationId
      const conversation = this.conversations.find(c => c.id === conversationId)
      if (conversation) {
        this.currentConversationTitle = conversation.title
      }
      await this.loadMessages(conversationId)
    },
    async loadMessages(conversationId) {
      try {
        const response = await getConversationMessages(conversationId)
        if (response.code === 200) {
          this.messages = response.data.map(msg => ({
            role: msg.role,
            content: msg.content,
            createTime: msg.createTime
          }))
          this.scrollToBottom()
        }
      } catch (error) {
        console.error('Error loading messages:', error)
      }
    },
    async deleteConversation(conversationId) {
      try {
        const response = await deleteConversation(conversationId)
        if (response.code === 200) {
          this.conversations = this.conversations.filter(c => c.id !== conversationId)
          if (this.currentConversationId === conversationId) {
            this.currentConversationId = null
            this.currentConversationTitle = '新对话'
            this.messages = []
            if (this.conversations.length > 0) {
              this.selectConversation(this.conversations[0].id)
            }
          }
          console.log('删除对话成功')
        }
      } catch (error) {
        console.error('Error deleting conversation:', error)
        console.error('Error deleting conversation:', error)
        console.error('删除对话失败')
      }
    },
    async sendMessage() {
      if (!this.inputMessage.trim() || !this.currentConversationId || this.isStreaming) return

      if (!this.isLoggedIn) {
        console.warn('请先登录')
        this.$router.push('/login')
        return
      }

      const message = this.inputMessage
      this.inputMessage = ''

      this.messages.push({
        role: 'user',
        content: message,
        createTime: new Date()
      })

      this.scrollToBottom()

      this.isStreaming = true
      this.streamingContent = ''
      let fullContent = ''

      try {
        const response = await sendMessageStream(this.currentConversationId, message)

        if (!response.ok) {
          throw new Error('发送消息失败')
        }

        this.currentReader = response.body.getReader()
        const decoder = new TextDecoder()
        let done = false
        let wasStopped = false

        while (!done) {
          const { value, done: doneReading } = await this.currentReader.read()
          done = doneReading
          if (value) {
            const chunk = decoder.decode(value)
            const lines = chunk.split('\n')
            for (const line of lines) {
              if (line.startsWith('data:')) {
                const data = line.slice(5).trim()
                if (data === '[DONE]' || data === '[STOPPED]') {
                  if (data === '[STOPPED]') {
                    wasStopped = true
                  }
                  done = true
                  break
                }
                if (data) {
                  try {
                    const parsed = JSON.parse(data)
                    if (parsed.data) {
                      fullContent += parsed.data
                      this.streamingContent = fullContent
                      this.scrollToBottom()
                    }
                  } catch (e) {
                    if (data.startsWith('"') && data.endsWith('"')) {
                      const unescaped = data.slice(1, -1).replace(/\\"/g, '"').replace(/\\\\/g, '\\')
                      fullContent += unescaped
                      this.streamingContent = fullContent
                      this.scrollToBottom()
                    } else if (data && data !== '[DONE]' && data !== '[STOPPED]') {
                      fullContent += data
                      this.streamingContent = fullContent
                      this.scrollToBottom()
                    }
                  }
                }
              }
            }
          }
        }

        this.currentReader = null

        if (fullContent) {
          this.messages.push({
            role: 'assistant',
            content: fullContent,
            createTime: new Date()
          })
        }

        await this.loadMessages(this.currentConversationId)

      } catch (error) {
        console.error('Error sending message:', error)
        console.error('发送消息失败，请重试')
      } finally {
        this.isStreaming = false
        this.streamingContent = ''
        this.currentReader = null
      }
    },
    async stopGeneration() {
      if (!this.isStreaming || !this.currentConversationId) return

      try {
        if (this.currentReader) {
          this.currentReader.cancel()
          this.currentReader = null
        }

        await stopMessageStream(this.currentConversationId)
      } catch (error) {
        console.error('Error stopping generation:', error)
      } finally {
        this.isStreaming = false
        this.streamingContent = ''
      }
    },
    editConversationTitle(conversationId, currentTitle) {
      this.editingConversationId = conversationId
      this.editingTitle = currentTitle
      this.showEditDialog = true
    },
    closeEditDialog() {
      this.showEditDialog = false
      this.editingConversationId = null
      this.editingTitle = ''
    },
    async saveTitle() {
      if (!this.editingTitle.trim()) {
        alert('标题不能为空')
        return
      }
      
      try {
        const response = await updateConversationTitle(this.editingConversationId, this.editingTitle.trim())
        console.log('Update title response:', response)
        
        // 更新本地数据
        const conversation = this.conversations.find(c => c.id === this.editingConversationId)
        if (conversation) {
          conversation.title = this.editingTitle.trim()
        }
        if (this.currentConversationId === this.editingConversationId) {
          this.currentConversationTitle = this.editingTitle.trim()
        }
        
        alert('标题更新成功')
        console.log('Title updated successfully')
        this.closeEditDialog()
      } catch (error) {
        console.error('Error updating title:', error)
        alert('更新标题失败：' + error.message)
      }
    },
    scrollToBottom() {
      this.$nextTick(() => {
        const container = this.$refs.messagesContainer
        if (container) {
          container.scrollTop = container.scrollHeight
        }
      })
    }
  }
}
</script>

<style scoped>
.ielts-helper-container {
  width: 100vw;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f5f5;
}

.chat-container {
  width: 90%;
  height: 90%;
  display: flex;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.history-panel {
  width: 300px;
  border-right: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
}

.history-header {
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.history-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.new-chat-btn {
  padding: 6px 12px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.new-chat-btn:hover {
  background-color: #45a049;
}

.history-list {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
}

.history-item {
  padding: 15px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  position: relative;
}

.history-item:hover {
  background-color: #f9f9f9;
}

.history-item.active {
  background-color: #e3f2fd;
}

.conversation-title {
  font-weight: 500;
  margin-bottom: 5px;
  color: #333;
}

.conversation-time {
  font-size: 12px;
  color: #999;
}

.delete-btn {
  padding: 4px 8px;
  background-color: #f44336;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.delete-btn:hover {
  background-color: #d32f2f;
}

.conversation-actions {
  position: absolute;
  top: 10px;
  right: 10px;
  display: flex;
  gap: 5px;
  opacity: 0;
  transition: opacity 0.2s;
}

.edit-btn {
  padding: 4px 8px;
  background-color: #2196F3;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.edit-btn:hover {
  background-color: #0b7dda;
}

.history-item:hover .conversation-actions {
  opacity: 1;
}

.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chat-header {
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
}

.chat-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.login-prompt {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: #666;
}

.login-prompt a {
  color: #2196F3;
  text-decoration: none;
}

.login-prompt a:hover {
  text-decoration: underline;
}

.message {
  margin-bottom: 20px;
  max-width: 80%;
}

.user-message {
  align-self: flex-end;
  margin-left: auto;
}

.assistant-message {
  align-self: flex-start;
  margin-right: auto;
}

.message-content {
  padding: 12px 16px;
  border-radius: 18px;
  font-size: 14px;
  line-height: 1.4;
  white-space: pre-wrap;
  word-break: break-word;
}

.user-message .message-content {
  background-color: #dcf8c6;
  color: #333;
  border-bottom-right-radius: 4px;
}

.assistant-message .message-content {
  background-color: #f1f0f0;
  color: #333;
  border-bottom-left-radius: 4px;
}

.streaming-content {
  border-bottom-left-radius: 4px;
}

.message-time {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
  text-align: right;
}

.assistant-message .message-time {
  text-align: left;
}

.chat-input-area {
  padding: 20px;
  border-top: 1px solid #e0e0e0;
  display: flex;
  gap: 10px;
}

.chat-input-area input {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 24px;
  font-size: 14px;
  outline: none;
}

.chat-input-area input:focus {
  border-color: #2196F3;
}

.chat-input-area input:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.send-btn {
  padding: 12px 24px;
  background-color: #2196F3;
  color: white;
  border: none;
  border-radius: 24px;
  cursor: pointer;
  font-size: 14px;
}

.send-btn:hover:not(:disabled) {
  background-color: #0b7dda;
}

.send-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.stop-btn {
  padding: 12px 24px;
  background-color: #f44336;
  color: white;
  border: none;
  border-radius: 24px;
  cursor: pointer;
  font-size: 14px;
}

.stop-btn:hover {
  background-color: #d32f2f;
}

/* 修改标题弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  border-radius: 8px;
  width: 400px;
  max-width: 90%;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  overflow: hidden;
}

.modal-header {
  padding: 16px 20px;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  color: #333;
}

.modal-body {
  padding: 20px;
}

.title-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

.title-input:focus {
  outline: none;
  border-color: #2196F3;
  box-shadow: 0 0 0 2px rgba(33, 150, 243, 0.2);
}

.modal-footer {
  padding: 16px 20px;
  border-top: 1px solid #e0e0e0;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.cancel-btn, .save-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.cancel-btn {
  background-color: #f5f5f5;
  color: #666;
}

.cancel-btn:hover {
  background-color: #e0e0e0;
}

.save-btn {
  background-color: #2196F3;
  color: white;
}

.save-btn:hover:not(:disabled) {
  background-color: #0b7dda;
}

.save-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}
</style>