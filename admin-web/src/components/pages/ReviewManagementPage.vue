<script setup>
import "./../../styles/pages/reviews.css"
import { ref } from 'vue'

defineProps({ reviews: Array })
const emit = defineEmits(['deleteReview'])

const deletingIds = ref(new Set())

function onDelete(reviewId) {
  if (!window.confirm('이 후기를 삭제하시겠습니까?')) return

  deletingIds.value = new Set([...deletingIds.value, reviewId])
  emit('deleteReview', reviewId, () => {
    deletingIds.value = new Set([...deletingIds.value].filter((id) => id !== reviewId))
  })
}
</script>

<template>
  <div class="rv-page">
    <div class="page-hd">
      <p class="sec-label">CUSTOMER FEEDBACK</p>
      <h2 class="page-title">후기 관리</h2>
    </div>

    <div class="stats-row" v-if="reviews.length">
      <div class="st-card">
        <p class="st-lbl">총 후기</p>
        <strong class="st-val">{{ reviews.length }}</strong>
      </div>
      <div class="st-card">
        <p class="st-lbl">평균 별점</p>
        <strong class="st-val pink">{{ (reviews.reduce((sum, review) => sum + review.rating, 0) / reviews.length).toFixed(1) }}</strong>
      </div>
      <div class="st-card">
        <p class="st-lbl">5점 후기</p>
        <strong class="st-val">{{ reviews.filter((review) => review.rating === 5).length }}</strong>
      </div>
    </div>

    <div class="rv-grid" v-if="reviews.length">
      <div class="rv-card" v-for="review in reviews" :key="review.id">
        <div class="rv-top">
          <strong class="rv-menu">{{ review.menuName }}</strong>
          <div class="rv-stars">
            <span v-for="i in 5" :key="i" class="rv-star" :class="{ on: i <= review.rating }">★</span>
          </div>
        </div>
        <p class="rv-text">"{{ review.content }}"</p>
        <div class="rv-bottom">
          <span class="rv-source">{{ review.source || 'customer-web' }}</span>
          <button
            class="rv-delete-btn"
            type="button"
            :disabled="deletingIds.has(review.id)"
            @click="onDelete(review.id)"
          >
            {{ deletingIds.has(review.id) ? '삭제중…' : '삭제' }}
          </button>
        </div>
      </div>
    </div>
    <div class="empty-state" v-else><p>등록된 후기가 없습니다.</p></div>
  </div>
</template>
