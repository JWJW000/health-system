import type { RouteRecordRaw } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import ProfileView from '../views/ProfileView.vue'
import DietView from '../views/DietView.vue'
import TrainView from '../views/TrainView.vue'
import CheckInView from '../views/CheckInView.vue'
import WeeklyReportView from '../views/WeeklyReportView.vue'
import AccountSettingsView from '../views/AccountSettingsView.vue'
import LoginView from '../views/LoginView.vue'
import AdminLoginView from '../views/admin/AdminLoginView.vue'
import AdminDashboardView from '../views/admin/AdminDashboardView.vue'
import AdminDietView from '../views/admin/AdminDietView.vue'
import AdminTrainView from '../views/admin/AdminTrainView.vue'
import AdminUserView from '../views/admin/AdminUserView.vue'

export const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: LoginView,
  },
  {
    path: '/admin',
    name: 'admin-login',
    component: AdminLoginView,
  },
  {
    path: '/admin/dashboard',
    name: 'admin-dashboard',
    component: AdminDashboardView,
    meta: { requiresAuth: true, requiresAdmin: true },
  },
  {
    path: '/',
    name: 'home',
    component: HomeView,
    meta: { requiresAuth: true },
  },
  {
    path: '/profile',
    name: 'profile',
    component: ProfileView,
    meta: { requiresAuth: true },
  },
  {
    path: '/diet',
    name: 'diet',
    component: DietView,
    meta: { requiresAuth: true },
  },
  {
    path: '/train',
    name: 'train',
    component: TrainView,
    meta: { requiresAuth: true },
  },
  {
    path: '/checkin',
    name: 'checkin',
    component: CheckInView,
    meta: { requiresAuth: true },
  },
  {
    path: '/weekly-report',
    name: 'weekly-report',
    component: WeeklyReportView,
    meta: { requiresAuth: true },
  },
  {
    path: '/account',
    name: 'account',
    component: AccountSettingsView,
    meta: { requiresAuth: true },
  },
  {
    path: '/admin/diets',
    name: 'admin-diets',
    component: AdminDietView,
    meta: { requiresAuth: true, requiresAdmin: true },
  },
  {
    path: '/admin/train-actions',
    name: 'admin-train-actions',
    component: AdminTrainView,
    meta: { requiresAuth: true, requiresAdmin: true },
  },
  {
    path: '/admin/users',
    name: 'admin-users',
    component: AdminUserView,
    meta: { requiresAuth: true, requiresAdmin: true },
  },
]

