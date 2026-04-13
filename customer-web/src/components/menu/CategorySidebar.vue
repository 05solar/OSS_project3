<script setup>
import "./../../styles/menu/category-sidebar.css"
defineProps({
  categories: Array,
  selectedCategory: String,
  traffic: Object,
  loginForm: Object,
  auth: Object
})
const emit = defineEmits(['selectCategory', 'updateLoginField', 'registerAndLogin'])
</script>

<template>
  <aside class="cat-sidebar">
    <!-- Categories -->
    <div class="sidebar-sec">
      <p class="sec-label">카테고리</p>
      <div class="cat-list">
        <button
          class="cat-item"
          :class="{ active: !selectedCategory }"
          @click="emit('selectCategory', '')"
        >전체</button>
        <button
          v-for="cat in categories"
          :key="cat"
          class="cat-item"
          :class="{ active: selectedCategory === cat }"
          @click="emit('selectCategory', cat)"
        >{{ cat }}</button>
      </div>
    </div>

    <!-- Kitchen Tempo -->
    <div class="tempo-card" v-if="traffic">
      <div class="tempo-hd">
        <span class="tempo-title">Kitchen Tempo</span>
        <span class="tempo-live-dot"></span>
      </div>
      <div class="tempo-wait">
        <span class="tempo-lbl">대기 시간</span>
        <strong class="tempo-val">{{ traffic.estimatedWaitMinutes }}분</strong>
      </div>
      <div class="tempo-track">
        <div class="tempo-fill" :style="{ width: Math.min(traffic.estimatedWaitMinutes * 6, 100) + '%' }"></div>
      </div>
      <p class="tempo-msg">{{ traffic.message }}</p>
    </div>

    <!-- Guest Access / Login -->
    <div class="guest-box">
      <p class="sec-label">{{ auth ? '계정' : '게스트 액세스' }}</p>
      <div v-if="!auth" class="guest-form">
        <input
          class="form-input"
          :value="loginForm.fullName"
          @input="emit('updateLoginField', { fullName: $event.target.value })"
          placeholder="이름"
        />
        <input
          class="form-input"
          :value="loginForm.username"
          @input="emit('updateLoginField', { username: $event.target.value })"
          placeholder="아이디"
        />
        <input
          class="form-input"
          type="password"
          :value="loginForm.password"
          @input="emit('updateLoginField', { password: $event.target.value })"
          placeholder="비밀번호"
        />
        <button class="btn-crimson guest-btn" @click="emit('registerAndLogin')">
          회원가입 / 로그인
        </button>
      </div>
      <div v-else class="guest-authed">
        <span class="authed-name">{{ auth.user.username }}</span>
        <span class="authed-badge">로그인됨</span>
      </div>
    </div>
  </aside>
</template>