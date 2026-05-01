<script setup>
import "./../../styles/pages/auth.css"
const props = defineProps({ form: Object, error: String })
const emit = defineEmits(['updateField', 'submit', 'navigate'])
</script>

<template>
  <div class="auth-wrap">
    <div class="auth-card">
      <div class="auth-brand" @click="emit('navigate', 'menu')">
        <img src="/logo.png" class="auth-logo" alt="logo" />
        CAT TABLE
      </div>
      <div class="auth-hd">
        <h2 class="auth-title">로그인</h2>
        <p class="auth-sub">계정에 로그인하여 주문을 시작하세요</p>
      </div>
      <div class="auth-form">
        <div class="form-field">
          <label class="form-label">아이디</label>
          <input
            class="form-input"
            :value="form.username"
            @input="emit('updateField', { username: $event.target.value })"
            placeholder="아이디를 입력하세요"
            @keyup.enter="emit('submit')"
          />
        </div>
        <div class="form-field">
          <label class="form-label">비밀번호</label>
          <input
            class="form-input"
            type="password"
            :value="form.password"
            @input="emit('updateField', { password: $event.target.value })"
            placeholder="비밀번호를 입력하세요"
            @keyup.enter="emit('submit')"
          />
        </div>
        <p class="auth-error" v-if="error">{{ error }}</p>
        <button class="btn-pink auth-btn" @click="emit('submit')">로그인</button>
        <p class="auth-switch">
          계정이 없으신가요?
          <span class="auth-link" @click="emit('navigate', 'signup')">회원가입</span>
        </p>
      </div>
    </div>
  </div>
</template>
