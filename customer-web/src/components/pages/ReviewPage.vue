<script setup>
import "./../../styles/pages/review-page.css"
defineProps({ reviews: Array, auth: Object, reviewForm: Object })
const emit = defineEmits(['updateReviewForm', 'generateReview', 'submitReview'])
</script>

<template>
  <div class="page-wrap">
    <div class="page-inner">
      <div class="page-hd">
        <p class="sec-label">COMMUNITY</p>
        <h2 class="page-title">고객 후기</h2>
      </div>

      <div class="rv-grid" v-if="reviews.length">
        <div class="rv-card" v-for="review in reviews" :key="review.id">
          <div class="rv-top">
            <div class="rv-stars">
              <span v-for="i in 5" :key="i" class="star" :class="{ on: i <= review.rating }">★</span>
            </div>
            <strong class="rv-menu">{{ review.menuName }}</strong>
          </div>
          <p class="rv-text">"{{ review.content }}"</p>
        </div>
      </div>
      <div class="empty-state" v-else>
        <p>등록된 후기가 없습니다.</p>
      </div>

      <div class="write-card">
        <div class="write-hd">
          <p class="sec-label">후기 작성</p>
          <h3 class="write-title">경험을 공유해 주세요</h3>
        </div>
        <div class="write-form">
          <div class="form-field">
            <label class="form-label">메뉴명</label>
            <input class="form-input" :value="reviewForm.menuName" @input="emit('updateReviewForm', { menuName: $event.target.value })" placeholder="어떤 메뉴를 드셨나요?" />
          </div>
          <button class="btn-ai-gen" @click="emit('generateReview')">✦ AI 초안 생성</button>
          <div class="form-field">
            <label class="form-label">후기</label>
            <textarea class="form-input rv-ta" :value="reviewForm.content" @input="emit('updateReviewForm', { content: $event.target.value })" rows="4" placeholder="식사 경험을 작성해보세요..."></textarea>
          </div>
          <button class="btn-pink submit-btn" :disabled="!auth || !reviewForm.content" @click="emit('submitReview')">후기 등록</button>
          <p class="auth-hint" v-if="!auth">후기를 등록하려면 로그인이 필요합니다</p>
        </div>
      </div>
    </div>
  </div>
</template>