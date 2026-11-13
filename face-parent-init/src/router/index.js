import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import HomeView from '../views/HomeView.vue'
import SignInRecordView from '../views/SignInRecordView.vue'
import ClassReviewView from '../views/ClassReviewView.vue'
import QrBindView from '../views/QrBindView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      // preserve ?code= from OAuth when menu links to /
      redirect: (to) => ({ path: '/login', query: to.query })
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView
    },
    {
      path: '/qr-bind',
      name: 'qr-bind',
      component: QrBindView
    },
    {
      path: '/bind-more',
      name: 'bind-more',
      component: LoginView
    },
    {
      path: '/home',
      name: 'home',
      component: HomeView
    },
    {
      path: '/signin-record',
      name: 'signin-record',
      component: SignInRecordView
    },
    {
      path: '/class-review',
      name: 'class-review',
      component: ClassReviewView
    }
  ]
})

export default router
