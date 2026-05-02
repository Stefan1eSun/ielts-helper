<template>
  <div class="page-content">
    <div class="container">
      <h1>个人中心</h1>
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="error" class="error-message">{{ error }}</div>
      <div v-else class="profile-section">
        <div class="avatar-upload">
          <img :src="userInfo.avatarUrl || defaultAvatar" alt="头像" class="avatar-preview" />
          <input type="file" @change="handleAvatarUpload" style="display: none;" ref="fileInput" />
          <button class="btn btn-secondary" @click="$refs.fileInput.click()">上传头像</button>
        </div>
        <form @submit.prevent="handleUpdateProfile">
          <div class="form-group">
            <label for="phone">手机号</label>
            <input type="tel" id="phone" v-model="userInfo.phone" disabled />
          </div>
          <div class="form-group">
            <label for="username">用户名</label>
            <input type="text" id="username" v-model="userInfo.username" placeholder="请输入用户名" />
          </div>
          <div class="form-group">
            <label for="gender">性别</label>
            <select id="gender" v-model="userInfo.gender">
              <option :value="null">请选择</option>
              <option :value="1">男</option>
              <option :value="2">女</option>
            </select>
          </div>
          <div class="form-group">
            <label for="age">年龄</label>
            <input type="number" id="age" v-model="userInfo.age" placeholder="请输入年龄" />
          </div>
          <div class="form-actions">
            <button type="submit" class="btn btn-primary" :disabled="isLoading">
              {{ isLoading ? '更新中...' : '更新信息' }}
            </button>
            <router-link to="/reset" class="btn btn-secondary">重置密码</router-link>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import { getProfile, updateProfile, uploadAvatar } from '@/api/user'

export default {
  name: 'PersonCenter',
  data() {
    return {
      userInfo: {
        userId: null,
        phone: '',
        username: '',
        avatarUrl: '',
        gender: null,
        age: null
      },
      defaultAvatar: 'src/assets/images/default.png',
      loading: true,
      error: '',
      isLoading: false
    }
  },
  mounted() {
    this.fetchProfile()
  },
  methods: {
    async fetchProfile() {
      this.loading = true
      this.error = ''

      try {
        const response = await getProfile()
        if (response.code === 200) {
          this.userInfo = response.data
        } else {
          this.error = response.error || '获取个人信息失败'
        }
      } catch (err) {
        this.error = err.message || '获取个人信息失败，请先登录'
      } finally {
        this.loading = false
      }
    },
    handleAvatarUpload(e) {
      const file = e.target.files[0]
      if (file) {
        this.isLoading = true
        this.error = ''
        
        try {
          uploadAvatar(file)
            .then(response => {
              if (response.code === 200) {
                this.userInfo.avatarUrl = response.data.avatarUrl
                alert('头像上传成功')
              } else {
                this.error = response.error || '头像上传失败'
              }
            })
            .catch(err => {
              this.error = err.message || '头像上传失败'
            })
            .finally(() => {
              this.isLoading = false
            })
        } catch (err) {
          this.error = err.message || '头像上传失败'
          this.isLoading = false
        }
      }
    },
    async handleUpdateProfile() {
      this.isLoading = true
      this.error = ''

      try {
        const response = await updateProfile({
          username: this.userInfo.username,
          gender: this.userInfo.gender,
          age: this.userInfo.age,
          avatarUrl: this.userInfo.avatarUrl
        })

        if (response.code === 200) {
          alert('个人信息更新成功')
        } else {
          this.error = response.error || '更新失败'
        }
      } catch (err) {
        this.error = err.message || '更新失败，请重试'
      } finally {
        this.isLoading = false
      }
    }
  }
}
</script>

<style scoped>
.profile-section {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  padding: 2rem;
  margin-top: 2rem;
}

.avatar-upload {
  text-align: center;
  margin-bottom: 2rem;
}

.avatar-preview {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  object-fit: cover;
  margin-bottom: 1rem;
  border: 3px solid #e9ecef;
}

.loading {
  text-align: center;
  padding: 2rem;
  font-size: 1.2rem;
  color: #666;
}

.error-message {
  color: #dc3545;
  padding: 1rem;
  text-align: center;
}

.form-actions {
  margin-top: 2rem;
  display: flex;
  gap: 1rem;
}

.form-actions .btn {
  flex: 1;
}
</style>
