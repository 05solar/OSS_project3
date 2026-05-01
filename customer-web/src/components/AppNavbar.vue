<script setup>
import "./../styles/navbar.css"
import { ref } from 'vue'

defineProps({
  auth: Object,
  currentSection: String,
  cartCount: Number
})
const emit = defineEmits(['navigate', 'logout'])

const menuOpen = ref(false)

function navigate(section) {
  emit('navigate', section)
  menuOpen.value = false
}

function logout() {
  emit('logout')
  menuOpen.value = false
}
</script>

<template>
  <nav class="navbar">
    <div class="nb-inner">
      <div class="nb-brand" @click="navigate('menu')">
        <img src="/logo.png" class="nb-logo" alt="logo" />
        CAT TABLE
      </div>

      <!-- 데스크탑 링크 -->
      <div class="nb-links">
        <span class="nb-link" :class="{ active: currentSection === 'menu' }" @click="navigate('menu')">메뉴</span>
        <span class="nb-link" :class="{ active: currentSection === 'orders' }" @click="navigate('orders')">주문 내역</span>
        <span class="nb-link" :class="{ active: currentSection === 'reviews' }" @click="navigate('reviews')">후기</span>
        <span class="nb-link" :class="{ active: currentSection === 'about' }" @click="navigate('about')">우리 이야기</span>
      </div>

      <div class="nb-right">
        <button
          class="nb-icon-btn"
          :aria-label="auth ? '마이페이지' : '로그인'"
          @click="navigate(auth ? 'mypage' : 'login')"
        >
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
        </button>
        <button v-if="auth" class="btn-logout" @click="logout">로그아웃</button>
        <button v-else class="btn-login" @click="navigate('login')">로그인</button>

        <!-- 햄버거 버튼 (모바일 전용) -->
        <button class="nb-hamburger" :class="{ open: menuOpen }" @click="menuOpen = !menuOpen" aria-label="메뉴 열기">
          <span></span>
          <span></span>
          <span></span>
        </button>
      </div>
    </div>

    <!-- 모바일 드로어 -->
    <div class="nb-drawer" :class="{ open: menuOpen }" @click.self="menuOpen = false">
      <div class="nb-drawer-inner">
        <div class="nb-drawer-links">
          <span class="nb-drawer-link" :class="{ active: currentSection === 'menu' }" @click="navigate('menu')">메뉴</span>
          <span class="nb-drawer-link" :class="{ active: currentSection === 'orders' }" @click="navigate('orders')">주문 내역</span>
          <span class="nb-drawer-link" :class="{ active: currentSection === 'reviews' }" @click="navigate('reviews')">후기</span>
          <span class="nb-drawer-link" :class="{ active: currentSection === 'about' }" @click="navigate('about')">우리 이야기</span>
        </div>
        <div class="nb-drawer-bottom">
          <button v-if="auth" class="nb-drawer-btn-logout" @click="logout">로그아웃</button>
          <button v-else class="nb-drawer-btn-login" @click="navigate('login')">로그인하기</button>
        </div>
      </div>
    </div>
  </nav>
</template>
