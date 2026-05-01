<script setup>
import { ref, computed } from 'vue'
import "./../../styles/pages/auth.css"

const props = defineProps({ form: Object, error: String })
const emit = defineEmits(['updateField', 'submit', 'navigate'])

const confirmPassword = ref('')
const confirmError = computed(() =>
  confirmPassword.value && props.form.password !== confirmPassword.value
    ? '비밀번호가 일치하지 않습니다.' : ''
)

function handleSubmit() {
  if (confirmError.value || !confirmPassword.value) return
  emit('submit')
}
</script>

<template>
  <div class="auth-wrap">
    <div class="auth-card">
      <div class="auth-brand" @click="emit('navigate', 'menu')">
        <img src="/logo.png" class="auth-logo" alt="logo" />
        CAT TABLE
      </div>
      <div class="auth-hd">
        <h2 class="auth-title">회원가입</h2>
        <p class="auth-sub">새 계정을 만들어 서비스를 이용해보세요</p>
      </div>
      <div class="auth-form">
        <div class="form-field">
          <label class="form-label">이름</label>
          <input
            class="form-input"
            :value="form.fullName"
            @input="emit('updateField', { fullName: $event.target.value })"
            placeholder="실명을 입력하세요"
          />
        </div>
        <div class="form-field">
          <label class="form-label">아이디</label>
          <input
            class="form-input"
            :value="form.username"
            @input="emit('updateField', { username: $event.target.value })"
            placeholder="사용할 아이디를 입력하세요"
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
          />
        </div>
        <div class="form-field">
          <label class="form-label">비밀번호 확인</label>
          <input
            class="form-input"
            :class="{ 'input-error': confirmError }"
            type="password"
            v-model="confirmPassword"
            placeholder="비밀번호를 다시 입력하세요"
            @keyup.enter="handleSubmit"
          />
          <p class="field-error" v-if="confirmError">{{ confirmError }}</p>
        </div>
        <p class="auth-error" v-if="error">{{ error }}</p>
        <button class="btn-pink auth-btn" :disabled="!!confirmError || !confirmPassword" @click="handleSubmit">
          회원가입
        </button>
        <p class="auth-switch">
          이미 계정이 있으신가요?
          <span class="auth-link" @click="emit('navigate', 'login')">로그인</span>
        </p>
      </div>
    </div>
  </div>
</template>
