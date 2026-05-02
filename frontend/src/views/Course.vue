<template>
  <div class="page-content">
    <div class="container">
      <h1>课程介绍</h1>
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="error" class="error-message">{{ error }}</div>
      <div v-else class="course-list">
        <div class="card course-card" v-for="course in courses" :key="course.courseId">
          <div class="course-info">
            <h2>{{ course.title }}</h2>
            <p><strong>课程类型：</strong>{{ course.type }}</p>
            <p><strong>时间：</strong>{{ formatTime(course.startTime, course.endTime) }}</p>
            <p><strong>教师：</strong>{{ course.teacherName }}</p>
            <p><strong>价格：</strong>¥{{ course.priceCents }}</p>
            <p v-if="course.description"><strong>描述：</strong>{{ course.description }}</p>
          </div>
          <div class="course-actions">
            <button class="btn btn-primary" @click="handleReserve(course)">预约</button>
          </div>
        </div>
      </div>

      <!-- 支付弹窗 -->
      <div class="modal" v-if="showPaymentModal">
        <div class="modal-content">
          <h3>微信扫码支付</h3>
          <div class="qrcode-container">
            <p>订单号：{{ orderId }}</p>
            <p>请扫描下方二维码完成支付</p>
          </div>
          <div class="modal-actions">
            <button class="btn btn-secondary" @click="cancelPayment">取消</button>
            <button class="btn btn-primary" @click="checkPaymentStatus">已完成支付</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getCourseList, reserveCourse, getPaymentStatus } from '../api/course'

export default {
  name: 'Course',
  data() {
    return {
      courses: [],
      loading: true,
      error: null,
      showPaymentModal: false,
      orderId: null,
      currentCourseId: null
    }
  },
  mounted() {
    this.fetchCourses()
  },
  methods: {
    async fetchCourses() {
      try {
        const response = await getCourseList()
        if (response.code === 200) {
          this.courses = response.data || []
        } else {
          this.error = response.error || '获取课程列表失败'
        }
      } catch (err) {
        this.error = err.message || '获取课程列表失败'
      } finally {
        this.loading = false
      }
    },
    formatTime(startTime, endTime) {
      if (!startTime || !endTime) return ''
      const start = new Date(startTime)
      const end = new Date(endTime)
      return `${start.toLocaleString()} - ${end.toLocaleTimeString()}`
    },
    async handleReserve(course) {
      try {
        const response = await reserveCourse(course.courseId)
        if (response.code === 200) {
          this.orderId = response.data.order_id
          this.currentCourseId = course.courseId
          this.showPaymentModal = true
        } else {
          alert(response.error || '预约失败')
        }
      } catch (err) {
        alert(err.message || '预约失败，请先登录')
      }
    },
    async checkPaymentStatus() {
      if (!this.orderId) return

      try {
        const response = await getPaymentStatus(this.orderId)
        if (response.code === 200) {
          const status = response.data.status
          if (status === 'paid') {
            this.showPaymentModal = false
            alert('预约成功！')
            this.fetchCourses()
          } else if (status === 'pending') {
            alert('支付处理中，请稍后再试...')
          } else {
            alert('支付失败，请重试')
          }
        }
      } catch (err) {
        alert('查询支付状态失败')
      }
    },
    cancelPayment() {
      this.showPaymentModal = false
      this.orderId = null
      this.currentCourseId = null
    }
  }
}
</script>

<style scoped>
.course-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 1.5rem;
}

.course-card {
  display: flex;
  flex-direction: column;
  transition: transform 0.3s;
}

.course-card:hover {
  transform: translateY(-5px);
}

.course-info {
  flex: 1;
}

.course-info h2 {
  margin-bottom: 1rem;
  color: #333;
}

.course-info p {
  margin-bottom: 0.5rem;
  color: #666;
}

.course-actions {
  margin-top: 1.5rem;
  text-align: right;
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

.modal-content {
  text-align: center;
}

.qrcode-container {
  margin: 2rem 0;
}

.qrcode-container p {
  margin-bottom: 0.5rem;
}

.modal-actions {
  display: flex;
  justify-content: center;
  gap: 1rem;
}
</style>
