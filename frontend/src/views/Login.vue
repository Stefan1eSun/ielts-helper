<template>
  <div class="auth-page">
    <div class="auth-form">
      <div class="form-container">
        <h2>登录账号</h2>
        <form @submit.prevent="handleLogin">
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
          <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
          <div class="form-actions">
            <button type="submit" class="btn btn-primary" :disabled="isLoading">
              {{ isLoading ? '登录中...' : '登录' }}
            </button>
          </div>
          <div class="form-links">
            <router-link to="/reset" class="form-link">忘记密码</router-link>
            <router-link to="/register" class="form-link">立即注册</router-link>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import { login } from '@/api/auth'

export default {
  name: 'Login',
  data() {
    return {
      form: {
        phone: '',
        password: ''
      },
      errors: {},
      errorMessage: '',
      isLoading: false
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
      return Object.keys(this.errors).length === 0
    },
    async handleLogin() {
      if (!this.validateForm()) {
        return
      }
      this.isLoading = true
      this.errorMessage = ''

      try {
        const response = await login({
          phone: this.form.phone,
          password: this.form.password
        })

        if (response.code === 200) {
          const data = response.data
          localStorage.setItem('token', data.token)
          localStorage.setItem('user_id', data.userId)
          localStorage.setItem('phone', data.phone)
          this.$bus.emit('login-success')
          alert('登录成功')
          this.$router.push('/')
        } else {
          this.errorMessage = response.error || '登录失败'
        }
      } catch (err) {
        this.errorMessage = err.message || '登录失败，请检查手机号和密码'
      } finally {
        this.isLoading = false
      }
    }
  }
}
</script>

<style scoped>
.form-links {
  display: flex;
  justify-content: space-between;
  margin-top: 1rem;
}

.form-link {
  color: #007bff;
  text-decoration: none;
  font-size: 0.875rem;
}

.form-link:hover {
  text-decoration: underline;
}
</style>
