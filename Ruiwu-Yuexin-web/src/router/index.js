import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue';
import Register from '../views/Register.vue';
import Home from '../views/Home.vue';
import Payment from '../views/Payment.vue'

// 配置路由规则
const routes = [
  {
    path: '/',
    redirect: '/home' // 根路径重定向到 /login
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register

  },
  {
    path: '/home',
    name: 'Home',
    component: Home
    
  },
  {
    path: '/detail',
    name: 'Detail',
    component: () => import('../views/Detail.vue'),
  },
  {
    path: '/cart',
    name: 'Cart',
    component: () => import('../views/Cart.vue'),
  },
  {
    path: '/favorite',
    name: 'Favorite',
    component: () => import('../views/Favorite.vue'),
  },
  {
    path: '/order',
    name: 'Order',
    component: () => import('../views/Order.vue'),
  },
  {
    path: '/inform',
    name: 'Inform',
    component: () => import('../components/main/Inform.vue')
  },
  {
    path: '/payment',
    name: 'Payment',
    component: () => import('../views/Payment.vue')
  } 
];


// 创建路由实例
const router = createRouter({
  history: createWebHistory(),  // 使用 HTML5 History 模式
  routes
})

export default router
