<template>
  <div class="auth-page">
    <div class="auth-form">
      <div class="form-container">
        <h2>重置密码</h2>
        <form @submit.prevent="handleResetPassword">
          <div class="form-group">
            <label for="phone">手机号</label>
            <input type="tel" id="phone" v-model="form.phone" placeholder="请输入11位手机号" />
            <div v-if="errors.phone" class="error-message">{{ errors.phone }}</div>
          </div>
          <div class="form-group">
            <label for="code">验证码</label>
            <div class="code-input-group">
              <input type="text" id="code" v-model="form.code" placeholder="请输入验证码" />
              <button type="button" class="btn btn-secondary" @click="handleSendCode" :disabled="isSending">
                {{ isSending ? `${countdown}秒后重新发送` : '发送验证码' }}
              </button>
            </div>
            <div v-if="errors.code" class="error-message">{{ errors.code }}</div>
          </div>
          <div class="form-group">
            <label for="newPassword">新密码</label>
            <input type="password" id="newPassword" v-model="form.newPassword" placeholder="请输入至少6位新密码" />
            <div v-if="errors.newPassword" class="error-message">{{ errors.newPassword }}</div>
          </div>
          <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
          <div class="form-actions">
            <button type="submit" class="btn btn-primary" :disabled="isLoading">
              {{ isLoading ? '重置中...' : '重置' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import { sendCode, resetPassword } from '@/api/auth'

export default {
  name: 'Reset',
  data() {
    return {
      form: {
        phone: '',
        code: '',
        newPassword: ''
      },
      errors: {},
      errorMessage: '',
      isLoading: false,
      isSending: false,
      countdown: 0
    }
  },
  methods: {
    validateForm() {
      this.errors = {}
      this.errorMessage = ''
      if (!this.form.phone || this.form.phone.length !== 11) {
        this.errors.phone = '请输入11位手机号'
      }
      if (!this.form.code || this.form.code.length !== 6) {
        this.errors.code = '请输入6位验证码'
      }
      if (!this.form.newPassword || this.form.newPassword.length < 6) {
        this.errors.newPassword = '新密码至少6位'
      }
      return Object.keys(this.errors).length === 0
    },
    async handleSendCode() {
      if (!this.form.phone || this.form.phone.length !== 11) {
        this.errors.phone = '请输入11位手机号'
        return
      }
      this.isSending = true

      try {
        const response = await sendCode(this.form.phone, 'reset')
        if (response.code === 200) {
          alert('验证码已发送')
          this.startCountdown()
        } else {
          alert(response.error || '发送失败')
        }
      } catch (err) {
        alert(err.message || '发送失败')
      } finally {
        this.isSending = false
      }
    },
    startCountdown() {
      this.countdown = 60
      this.isSending = true
      const timer = setInterval(() => {
        this.countdown--
        if (this.countdown <= 0) {
          clearInterval(timer)
          this.isSending = false
        }
      }, 1000)
    },
    async handleResetPassword() {
      if (!this.validateForm()) {
        return
      }
      this.isLoading = true
      this.errorMessage = ''

      try {
        const response = await resetPassword({
          phone: this.form.phone,
          code: this.form.code,
          newPassword: this.form.newPassword
        })

        if (response.code === 200) {
          alert('密码重置成功')
          this.$router.push('/login')
        } else {
          this.errorMessage = response.error || '重置失败'
        }
      } catch (err) {
        this.errorMessage = err.message || '重置失败，请检查验证码是否正确'
      } finally {
        this.isLoading = false
      }
    }
  }
}
</script>

<style scoped>
.code-input-group {
  display: flex;
  gap: 1rem;
}

.code-input-group input {
  flex: 1;
}
</style>
