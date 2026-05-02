import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'index',
    component: () => import('../views/Index.vue')
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('../views/Register.vue')
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/reset',
    name: 'reset',
    component: () => import('../views/Reset.vue')
  },
  {
    path: '/institution',
    name: 'institution',
    component: () => import('../views/Institution.vue')
  },
  {
    path: '/course',
    name: 'course',
    component: () => import('../views/Course.vue')
  },
  {
    path: '/teacher',
    name: 'teacher',
    component: () => import('../views/Teacher.vue')
  },
  {
    path: '/person_center',
    name: 'person_center',
    component: () => import('../views/PersonCenter.vue')
  },
  {
    path: '/person_course',
    name: 'person_course',
    component: () => import('../views/PersonCourse.vue')
  },
  {
    path: '/IELTS-helper',
    name: 'ielts_helper',
    component: () => import('../views/IELTSHelper.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
