<template>
  <div class="auth-page">
    <div class="auth-form">
      <div class="form-container">
        <h2>注册账号</h2>
        <form @submit.prevent="handleRegister">
          <div class="form-group">
            <label for="phone">手机号</label>
            <input type="tel" id="phone" v-model="form.phone" placeholder="请输入11位手机号" />
            <div v-if="errors.phone" class="error-message">{{ errors.phone }}</div>
          </div>
          <div class="form-group">
            <label for="password">密码</label>
            <input type="password" id="password" v-model="form.password" placeholder="请输入至少6位密码" />
            <div v-if="errors.password" class="error-message">{{ errors.password }}</div>
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
          <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
          <div class="form-actions">
            <button type="submit" class="btn btn-primary" :disabled="isLoading">
              {{ isLoading ? '注册中...' : '注册' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import { register, sendCode } from '@/api/auth'

export default {
  name: 'Register',
  data() {
    return {
      form: {
        phone: '',
        password: '',
        code: ''
      },
      errors: {},
      errorMessage: '',
      isLoading: false,
      isSending: false,
      countdown: 60
    }
  },
  methods: {
    validateForm() {
      this.errors = {}
      this.errorMessage = ''
      if (!this.form.phone || this.form.phone.length !== 11) {
        this.errors.phone = '请输入11位手机号'
      }
      if (!this.form.password || this.form.password.length < 6) {
        this.errors.password = '密码至少6位'
      }
      if (!this.form.code || this.form.code.length !== 6) {
        this.errors.code = '请输入6位验证码'
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
          const response = await sendCode(this.form.phone, 'register')
          if (response.code === 200) {
            alert('您的验证码是' + response.data.code)
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
      const timer = setInterval(() => {
        this.countdown--
        if (this.countdown <= 0) {
          clearInterval(timer)
          this.isSending = false
        }
      }, 1000)
    },
    async handleRegister() {
      if (!this.validateForm()) {
        return
      }
      this.isLoading = true
      this.errorMessage = ''

      try {
        const response = await register({
          phone: this.form.phone,
          password: this.form.password,
          code: this.form.code
        })

        if (response.code === 200) {
          alert('注册成功')
          this.$router.push('/login')
        } else {
          this.errorMessage = response.error || '注册失败'
        }
      } catch (err) {
        this.errorMessage = err.message || '注册失败，请检查验证码是否正确'
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
