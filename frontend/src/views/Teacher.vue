<template>
  <div class="page-content">
    <div class="container">
      <h1>教师介绍</h1>
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="error" class="error-message">{{ error }}</div>
      <div v-else class="teacher-list">
        <div class="card teacher-card" v-for="teacher in teachers" :key="teacher.teacher_id">
          <h2>{{ teacher.name }}</h2>
          <div class="teacher-info">
            <p><strong>资历：</strong>{{ teacher.qualification }}</p>
            <p v-if="teacher.teaching_style"><strong>教学风格：</strong>{{ teacher.teaching_style }}</p>
            <p v-if="teacher.bio"><strong>个人简介：</strong>{{ teacher.bio }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getTeacherList } from '../api/teacher'

export default {
  name: 'Teacher',
  data() {
    return {
      teachers: [],
      loading: true,
      error: null
    }
  },
  mounted() {
    this.fetchTeachers()
  },
  methods: {
    async fetchTeachers() {
      try {
        const response = await getTeacherList()
        this.teachers = response.data
      } catch (err) {
        this.error = err.message
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.teacher-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
}

.teacher-card {
  transition: transform 0.3s;
  text-align: center;
}

.teacher-card:hover {
  transform: translateY(-5px);
}

.teacher-avatar {
  margin: 1rem 0;
}

.avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #e9ecef;
}

.teacher-info {
  margin-top: 1rem;
  text-align: left;
}

.teacher-info p {
  margin-bottom: 0.5rem;
}

.loading {
  text-align: center;
  padding: 2rem;
  font-size: 1.2rem;
  color: #666;
}
</style>
