<script setup>
import "./styles/app.css"
import { computed, onMounted, ref } from 'vue'
import AppNavbar from './components/AppNavbar.vue'
import MenuPage from './components/pages/MenuPage.vue'
import OrderHistoryPage from './components/pages/OrderHistoryPage.vue'
import ReviewPage from './components/pages/ReviewPage.vue'
import OurStoryPage from './components/pages/OurStoryPage.vue'
import LoginPage from './components/pages/LoginPage.vue'
import SignupPage from './components/pages/SignupPage.vue'
import MyPage from './components/pages/MyPage.vue'

const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080/api'

const categories = ref([])
const selectedCategory = ref('')
const menus = ref([])
const reviews = ref([])
const orders = ref([])
const auth = ref(JSON.parse(localStorage.getItem('customer-auth') || 'null'))
const cart = ref(JSON.parse(localStorage.getItem('customer-cart') || '[]'))
const chat = ref('')
const recommendResult = ref(null)
const traffic = ref(null)
const currentSection = ref('menu')

// 인증 폼
const authForm = ref({ username: '', password: '', fullName: '' })
const authError = ref('')

// 로그인 필요 팝업
const showLoginModal = ref(false)

import { watch } from 'vue'
watch(cart, (v) => localStorage.setItem('customer-cart', JSON.stringify(v)), { deep: true })
watch(auth, (v) => localStorage.setItem('customer-auth', v ? JSON.stringify(v) : 'null'))

// 리뷰 폼
const reviewForm = ref({ menuName: '', content: '', rating: 5 })

const totalPrice = computed(() => cart.value.reduce((sum, item) => sum + item.price * item.quantity, 0))

async function api(path, options = {}) {
  const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) }
  if (auth.value?.accessToken) headers.Authorization = `Bearer ${auth.value.accessToken}`
  const response = await fetch(`${API_BASE}${path}`, { ...options, headers })
  if (!response.ok) throw new Error(await response.text())
  return response.json()
}

async function loadMenus() {
  const categoryQuery = selectedCategory.value ? `?category=${encodeURIComponent(selectedCategory.value)}` : ''
  menus.value = await api(`/customer/menus${categoryQuery}`)
}

async function bootstrap() {
  categories.value = await api('/customer/categories')
  await loadMenus()
  reviews.value = await api('/customer/reviews')
  traffic.value = await api('/customer/traffic')
  if (auth.value?.accessToken) {
    try { orders.value = await api('/customer/orders') } catch (_) {}
  }
}

async function login() {
  authError.value = ''
  try {
    const result = await api('/auth/login', { method: 'POST', body: JSON.stringify({ username: authForm.value.username, password: authForm.value.password }) })
    auth.value = result
    orders.value = await api('/customer/orders')
    authForm.value = { username: '', password: '', fullName: '' }
    currentSection.value = 'menu'
  } catch (e) {
    authError.value = '아이디 또는 비밀번호가 올바르지 않습니다.'
  }
}

async function signup() {
  authError.value = ''
  if (!authForm.value.fullName || !authForm.value.username || !authForm.value.password) {
    authError.value = '모든 항목을 입력해 주세요.'
    return
  }
  try {
    await api('/auth/register', { method: 'POST', body: JSON.stringify(authForm.value) })
    const result = await api('/auth/login', { method: 'POST', body: JSON.stringify({ username: authForm.value.username, password: authForm.value.password }) })
    auth.value = result
    orders.value = await api('/customer/orders')
    authForm.value = { username: '', password: '', fullName: '' }
    currentSection.value = 'menu'
  } catch (e) {
    authError.value = '이미 사용 중인 아이디이거나 오류가 발생했습니다.'
  }
}

function logout() {
  auth.value = null
  orders.value = []
  localStorage.removeItem('customer-auth')
  currentSection.value = 'menu'
}

function addToCart(menu) {
  const existing = cart.value.find((item) => item.id === menu.id)
  if (existing) existing.quantity += 1
  else cart.value.push({ ...menu, quantity: 1 })
}

function increase(item) { item.quantity += 1 }
function decrease(item) { if (item.quantity > 1) item.quantity -= 1 }
function removeCart(itemId) { cart.value = cart.value.filter((item) => item.id !== itemId) }

async function submitOrder() {
  if (!cart.value.length) return
  await api('/customer/orders', {
    method: 'POST',
    body: JSON.stringify({
      items: cart.value.map((item) => ({ menuId: item.id, menuName: item.name, quantity: item.quantity, unitPrice: item.price, etaMinutes: item.etaMinutes })),
      totalAmount: totalPrice.value,
      notes: '웹 주문'
    })
  })
  cart.value = []
  orders.value = await api('/customer/orders')
  traffic.value = await api('/customer/traffic')
}

async function generateReview() {
  const result = await api('/customer/review-draft', {
    method: 'POST',
    body: JSON.stringify({ menuName: reviewForm.value.menuName, keywords: ['친절함', '깔끔함'], rating: reviewForm.value.rating })
  })
  reviewForm.value.content = result.content
}

async function submitReview() {
  await api('/customer/reviews', { method: 'POST', body: JSON.stringify({ ...reviewForm.value, source: 'customer-web' }) })
  reviewForm.value.content = ''
  reviews.value = await api('/customer/reviews')
}

async function askAi() {
  recommendResult.value = await api('/customer/recommend-menu', {
    method: 'POST',
    body: JSON.stringify({ message: chat.value, menus: menus.value })
  })
}

function handleSelectCategory(cat) {
  selectedCategory.value = cat
  loadMenus()
}

onMounted(bootstrap)
</script>

<template>
  <div class="app">
    <AppNavbar
      :auth="auth"
      :currentSection="currentSection"
      :cartCount="cart.length"
      @navigate="currentSection = $event"
      @logout="logout"
    />

    <MenuPage
      v-if="currentSection === 'menu'"
      :categories="categories"
      :selectedCategory="selectedCategory"
      :menus="menus"
      :cart="cart"
      :auth="auth"
      :totalPrice="totalPrice"
      :traffic="traffic"
      :chat="chat"
      :recommendResult="recommendResult"
      :reviews="reviews"
      :orders="orders"
      @selectCategory="handleSelectCategory"
      @addToCart="addToCart"
      @increase="increase"
      @decrease="decrease"
      @removeCart="removeCart"
      @submitOrder="submitOrder"
      @needLogin="showLoginModal = true"
      @update:chat="chat = $event"
      @askAi="askAi"
      @navigate="currentSection = $event"
    />

    <OrderHistoryPage
      v-if="currentSection === 'orders'"
      :orders="orders"
      :auth="auth"
    />

    <ReviewPage
      v-if="currentSection === 'reviews'"
      :reviews="reviews"
      :auth="auth"
      :reviewForm="reviewForm"
      @updateReviewForm="reviewForm = { ...reviewForm, ...$event }"
      @generateReview="generateReview"
      @submitReview="submitReview"
    />

    <OurStoryPage v-if="currentSection === 'about'" @navigate="currentSection = $event" />

    <LoginPage
      v-if="currentSection === 'login'"
      :form="authForm"
      :error="authError"
      @updateField="authForm = { ...authForm, ...$event }"
      @submit="login"
      @navigate="currentSection = $event; authError = ''"
    />

    <SignupPage
      v-if="currentSection === 'signup'"
      :form="authForm"
      :error="authError"
      @updateField="authForm = { ...authForm, ...$event }"
      @submit="signup"
      @navigate="currentSection = $event; authError = ''"
    />

    <MyPage
      v-if="currentSection === 'mypage'"
      :auth="auth"
      :orders="orders"
      @navigate="currentSection = $event"
      @logout="logout"
    />

    <!-- 로그인 필요 팝업 -->
    <div class="modal-overlay" v-if="showLoginModal" @click.self="showLoginModal = false">
      <div class="modal-box">
        <div class="modal-icon">🔒</div>
        <h3 class="modal-title">로그인이 필요한 서비스입니다</h3>
        <p class="modal-desc">주문하려면 로그인이 필요합니다.</p>
        <div class="modal-btns">
          <button class="btn-pink" @click="currentSection = 'login'; showLoginModal = false">로그인하기</button>
          <button class="btn-outline" @click="showLoginModal = false">닫기</button>
        </div>
      </div>
    </div>
  </div>
</template>
