<template>
  <nav class="navbar">
    <div class="container">
      <router-link to="/" class="navbar-logo">IELTS-helper</router-link>
      <ul class="navbar-links">
        <li><router-link to="/institution">机构介绍</router-link></li>
        <li><router-link to="/teacher">教师介绍</router-link></li>
        <li><router-link to="/course">预约课程</router-link></li>
        <li><router-link to="/IELTS-helper">雅思智能助手</router-link></li>
      </ul>
      <div class="auth-buttons" v-if="!isLoggedIn">
        <router-link to="/login" class="btn btn-secondary">登录</router-link>
        <router-link to="/register" class="btn btn-primary">注册</router-link>
      </div>
      <div class="user-menu" v-else>
        <div class="user-dropdown">
          <button class="btn btn-secondary" @click="toggleDropdown">个人中心</button>
          <div class="dropdown-menu" v-if="showDropdown">
            <router-link to="/person_center" class="dropdown-item" @click="showDropdown = false">个人信息</router-link>
            <router-link to="/person_course" class="dropdown-item" @click="showDropdown = false">个人课程</router-link>
            <button class="dropdown-item" @click="logout">退出登录</button>
          </div>
        </div>
      </div>
    </div>
  </nav>
</template>

<script>
export default {
  name: 'Navbar',
  data() {
    return {
      isLoggedIn: localStorage.getItem('token') !== null,
      showDropdown: false
    }
  },
  mounted() {
    this.$bus.on('login-success', this.handleLoginSuccess)
  },
  beforeUnmount() {
    this.$bus.off('login-success', this.handleLoginSuccess)
  },
  methods: {
    toggleDropdown() {
      this.showDropdown = !this.showDropdown
    },
    handleLoginSuccess() {
      this.isLoggedIn = true
      this.showDropdown = false
    },
    logout() {
      localStorage.removeItem('token')
      localStorage.removeItem('user_id')
      localStorage.removeItem('phone')
      this.isLoggedIn = false
      this.showDropdown = false
      this.$router.push('/')
    }
  }
}
</script>

<style scoped>
.user-menu {
  position: relative;
}

.user-dropdown {
  position: relative;
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  background-color: #fff;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  border-radius: 4px;
  min-width: 150px;
  margin-top: 0.5rem;
  z-index: 1000;
}

.dropdown-item {
  display: block;
  width: 100%;
  padding: 0.75rem 1rem;
  text-align: left;
  border: none;
  background: none;
  cursor: pointer;
  font-size: 0.9rem;
  color: #333;
  text-decoration: none;
}

.dropdown-item:hover {
  background-color: #f8f9fa;
}
</style>
