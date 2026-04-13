<script setup>
import "./styles/app.css"
import { computed, onMounted, ref } from 'vue'
import AppSidebar from './components/AppSidebar.vue'
import DashboardPage from './components/pages/DashboardPage.vue'
import MenuEditPage from './components/pages/MenuEditPage.vue'
import OrderManagementPage from './components/pages/OrderManagementPage.vue'
import AnalysisPage from './components/pages/AnalysisPage.vue'
import ReviewManagementPage from './components/pages/ReviewManagementPage.vue'
import AiPage from './components/pages/AiPage.vue'

const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080/api'
const token = ref('')
const dashboard = ref(null)
const orders = ref([])
const reviews = ref([])
const menus = ref([])
const loadError = ref('')
const newMenu = ref({
  name: '',
  description: '',
  category: '한식',
  keywords: '',
  price: 10000,
  imageUrl: '',
  etaMinutes: 15,
  available: true
})
const aiSuggestion = ref('')
const quality = ref('')
const currentPage = ref('dashboard')

const now = new Date()
const currentDate = computed(() => now.toLocaleDateString('ko-KR', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' }))

async function api(path, options = {}) {
  const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) }
  if (token.value) headers.Authorization = `Bearer ${token.value}`
  const response = await fetch(`${API_BASE}${path}`, { ...options, headers })
  if (!response.ok) throw new Error(await response.text())
  return response.json()
}

async function loginAdmin() {
  try {
    loadError.value = ''
    const result = await api('/auth/login', { method: 'POST', body: JSON.stringify({ username: 'admin', password: 'admin1234' }) })
    token.value = result.accessToken
    await loadDashboard()
  } catch (e) {
    loadError.value = '서버에 연결할 수 없습니다. 백엔드가 실행 중인지 확인해 주세요.'
  }
}

async function loadDashboard() {
  try {
    const [dash, ord, rev, men] = await Promise.all([
      api('/admin/dashboard'),
      api('/admin/orders'),
      api('/admin/reviews'),
      api('/admin/menus')
    ])
    dashboard.value = dash
    orders.value = ord
    reviews.value = rev
    menus.value = men
  } catch (e) {
    loadError.value = '데이터를 불러오지 못했습니다. 새로고침을 눌러 주세요.'
  }
}

async function createMenu() {
  await api('/admin/menus', { method: 'POST', body: JSON.stringify(newMenu.value) })
  menus.value = await api('/admin/menus')
  dashboard.value = await api('/admin/dashboard')
}

async function suggestMenu() {
  const result = await api('/admin/new-menu-suggestion', { method: 'POST', body: JSON.stringify({ orders: orders.value }) })
  aiSuggestion.value = result.suggestion
}

async function evaluateQuality() {
  const result = await api('/admin/service-quality', { method: 'POST', body: JSON.stringify({ reviews: reviews.value }) })
  quality.value = result.analysis
}

async function updateOrderStatus(orderId, newStatus, done) {
  try {
    await api(`/admin/orders/${orderId}/status`, {
      method: 'PATCH',
      body: JSON.stringify({ status: newStatus })
    })
    orders.value = await api('/admin/orders')
  } catch (e) {
    loadError.value = '상태 변경에 실패했습니다.'
  } finally {
    if (typeof done === 'function') done()
  }
}

async function deleteReview(reviewId, done) {
  try {
    await api(`/admin/reviews/${reviewId}`, { method: 'DELETE' })
    reviews.value = await api('/admin/reviews')
    dashboard.value = await api('/admin/dashboard')
  } catch (e) {
    loadError.value = '후기 삭제에 실패했습니다.'
  } finally {
    if (typeof done === 'function') done()
  }
}

onMounted(loginAdmin)
</script>

<template>
  <div class="admin-app">
    <AppSidebar :currentPage="currentPage" @navigate="currentPage = $event" @refresh="loginAdmin" />

    <main class="admin-main">
      <div class="admin-hd">
        <div class="hd-left">
          <h1 class="hd-title">Admin Dashboard</h1>
          <p class="hd-date">{{ currentDate }}</p>
        </div>
        <div class="hd-right">
          <button class="hd-icon-btn" @click="loginAdmin" title="새로고침">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/><path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/></svg>
          </button>
          <div class="hd-avatar">A</div>
        </div>
      </div>

      <div class="load-error" v-if="loadError">
        <span>⚠ {{ loadError }}</span>
        <button @click="loginAdmin">재시도</button>
      </div>

      <DashboardPage
        v-if="currentPage === 'dashboard'"
        :dashboard="dashboard"
        :orders="orders"
        :reviews="reviews"
        :menus="menus"
        :newMenu="newMenu"
        :aiSuggestion="aiSuggestion"
        :quality="quality"
        @createMenu="createMenu"
        @suggestMenu="suggestMenu"
        @evaluateQuality="evaluateQuality"
        @updateNewMenu="newMenu = { ...newMenu, ...$event }"
        @updateStatus="updateOrderStatus"
      />
      <MenuEditPage
        v-if="currentPage === 'menu-editor'"
        :menus="menus" :newMenu="newMenu"
        @createMenu="createMenu"
        @updateNewMenu="newMenu = { ...newMenu, ...$event }"
      />
      <OrderManagementPage v-if="currentPage === 'orders'" :orders="orders" @updateStatus="updateOrderStatus" />
      <AnalysisPage v-if="currentPage === 'analytics'" :dashboard="dashboard" :orders="orders" :reviews="reviews" />
      <ReviewManagementPage v-if="currentPage === 'reviews'" :reviews="reviews" @deleteReview="deleteReview" />
      <AiPage v-if="currentPage === 'ai'" :aiSuggestion="aiSuggestion" :quality="quality" @suggestMenu="suggestMenu" @evaluateQuality="evaluateQuality" />

      <footer class="page-footer">
        <span>© 2024 CAT TABLE.</span>
        <div class="footer-links">
          <a>개인정보처리방침</a><a>이용약관</a><a>지속가능성</a><a>채용</a>
        </div>
      </footer>
    </main>
  </div>
</template>
