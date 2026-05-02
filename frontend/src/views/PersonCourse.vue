<template>
  <div class="page-content">
    <div class="container">
      <h1>个人课程</h1>
      <div v-if="loading && courses.length === 0" class="loading">加载中...</div>
      <div v-else-if="error" class="error-message">{{ error }}</div>
      <div v-else class="course-list">
        <div class="course-item" v-for="course in courses" :key="course.enrollmentId">
          <div class="course-info">
            <h3>{{ course.courseTitle }}</h3>
            <p><strong>课程类型：</strong>{{ course.type }}</p>
            <p><strong>教师：</strong>{{ course.teacherName }}</p>
            <p><strong>上课时间：</strong>{{ formatTime(course.startTime) }}</p>
            <p><strong>状态：</strong>{{ getStatusText(course.status) }}</p>
          </div>
          <div class="course-actions">
            <button v-if="course.canCancel" class="btn btn-secondary" @click="showCancelModal = true">取消课程</button>
          </div>
        </div>
        
        <!-- 分页控制 -->
        <div class="pagination" v-if="totalPages > 1">
          <button 
            class="btn btn-secondary" 
            :disabled="currentPage <= 1" 
            @click="loadPage(currentPage - 1)">
            上一页
          </button>
          <span class="page-info">第 {{ currentPage }} / {{ totalPages }} 页（共 {{ total }} 条）</span>
          <button 
            class="btn btn-secondary" 
            :disabled="currentPage >= totalPages" 
            @click="loadPage(currentPage + 1)">
            下一页
          </button>
        </div>
        
        <!-- 加载更多 -->
        <div class="load-more" v-else-if="hasMore && !loading">
          <button class="btn btn-primary" @click="loadMore">加载更多</button>
        </div>
      </div>
      
      <!-- 取消课程弹窗 -->
      <div class="modal" v-if="showCancelModal">
        <div class="modal-content">
          <h3>选择要取消的课程</h3>
          <ul class="list-group">
            <li class="list-group-item" v-for="course in cancelableCourses" :key="course.enrollmentId" @click="confirmCancel(course)">
              <div>
                <strong>{{ course.courseTitle }}</strong>
                <span class="status-badge">{{ getStatusText(course.status) }}</span>
              </div>
            </li>
          </ul>
          <div class="modal-actions">
            <button class="btn btn-secondary" @click="showCancelModal = false">关闭</button>
          </div>
        </div>
      </div>
      
      <!-- 确认取消弹窗 -->
      <div class="modal" v-if="showConfirmModal">
        <div class="modal-content">
          <h3>确定取消该课程吗</h3>
          <p v-if="selectedCourse">{{ selectedCourse.courseTitle }}</p>
          <div class="modal-actions">
            <button class="btn btn-secondary" @click="showConfirmModal = false">否</button>
            <button class="btn btn-primary" @click="cancelCourse">是</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getUserCourses, cancelCourse } from '@/api/user'

export default {
  name: 'PersonCourse',
  data() {
    return {
      courses: [],
      loading: true,
      error: '',
      showCancelModal: false,
      showConfirmModal: false,
      selectedCourse: null,
      // 分页相关
      currentPage: 1,
      pageSize: 5,
      total: 0,
      totalPages: 0
    }
  },
  mounted() {
    this.fetchCourses()
  },
  computed: {
    cancelableCourses() {
      return this.courses.filter(course => course.canCancel)
    },
    hasMore() {
      return this.currentPage < this.totalPages
    }
  },
  methods: {
    async fetchCourses() {
      this.loading = true
      this.error = ''
      
      try {
        const response = await getUserCourses(this.currentPage, this.pageSize)
        if (response.code === 200) {
          const data = response.data
          this.courses = data.records || []
          this.total = data.total || 0
          this.totalPages = data.pages || 0
        } else {
          this.error = response.error || '获取课程列表失败'
        }
      } catch (err) {
        this.error = err.message || '获取课程列表失败，请先登录'
        // 模拟数据用于开发测试
        this.courses = [
          {
            enrollmentId: 1001,
            courseTitle: '雅思口语冲刺班',
            type: 'Speaking',
            teacherName: '张老师',
            startTime: '2026-05-01T09:00:00',
            status: 2,
            canCancel: true
          },
          {
            enrollmentId: 1002,
            courseTitle: '雅思写作高分技巧',
            type: 'Writing',
            teacherName: '李老师',
            startTime: '2026-05-10T14:00:00',
            status: 3,
            canCancel: false
          },
          {
            enrollmentId: 1003,
            courseTitle: '雅思阅读技巧提升',
            type: 'Reading',
            teacherName: '王老师',
            startTime: '2026-05-15T16:00:00',
            status: 2,
            canCancel: true
          },
          {
            enrollmentId: 1004,
            courseTitle: '雅思听力强化训练',
            type: 'Listening',
            teacherName: '刘老师',
            startTime: '2026-04-20T10:00:00',
            status: 4,
            canCancel: false
          }
        ]
        this.total = this.courses.length
        this.totalPages = 1
      } finally {
        this.loading = false
      }
    },
    async loadPage(page) {
      this.currentPage = page
      await this.fetchCourses()
      // 滚动到顶部
      window.scrollTo({ top: 0, behavior: 'smooth' })
    },
    async loadMore() {
      if (this.hasMore) {
        this.currentPage++
        await this.fetchCourses()
      }
    },
    formatTime(time) {
      return new Date(time).toLocaleString()
    },
    getStatusText(status) {
      const statusMap = {
        1: '待支付',
        2: '已预约',
        3: '进行中',
        4: '已完成',
        5: '已取消'
      }
      return statusMap[status] || status
    },
    confirmCancel(course) {
      this.selectedCourse = course
      this.showCancelModal = false
      this.showConfirmModal = true
    },
    async cancelCourse() {
      if (this.selectedCourse) {
        try {
          const response = await cancelCourse(this.selectedCourse.enrollmentId)
          if (response.code === 200) {
            this.showConfirmModal = false
            // 根据课程状态显示不同的提示信息
            if (this.selectedCourse.status === 1) {
              alert('课程已取消')
            } else {
              alert('课程已取消，退款将在1-3个工作日内到账')
            }
            // 重新加载当前页
            this.fetchCourses()
          } else {
            alert(response.error || '取消失败')
          }
        } catch (err) {
          alert(err.message || '取消失败')
        }
      }
    }
  }
}
</script>

<style scoped>
.status-badge {
  margin-left: 1rem;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.875rem;
  background-color: #e9ecef;
}

.list-group-item {
  cursor: pointer;
  transition: background-color 0.3s;
}

.list-group-item:hover {
  background-color: #f8f9fa;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-top: 2rem;
  padding: 1rem 0;
}

.pagination .page-info {
  font-size: 0.9rem;
  color: #666;
}

.load-more {
  text-align: center;
  margin-top: 2rem;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
