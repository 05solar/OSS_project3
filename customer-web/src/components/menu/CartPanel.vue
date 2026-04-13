<script setup>
import { computed } from 'vue'
import "./../../styles/menu/cart-panel.css"

const props = defineProps({
  cart: Array,
  auth: Object,
  totalPrice: Number,
  reviews: Array
})

const emit = defineEmits([
  'increase',
  'decrease',
  'removeCart',
  'submitOrder',
  'needLogin',
  'navigate'
])

const top3Reviews = computed(() =>
  [...(props.reviews || [])].sort((a, b) => b.rating - a.rating).slice(0, 3)
)

function getReviewAuthor(review) {
  return review.username || review.nickname || review.authorName || review.fullName || '익명'
}

function handleOrder() {
  if (!props.auth) {
    emit('needLogin')
  } else {
    emit('submitOrder')
  }
}
</script>

<template>
  <div class="right-col">
    <div class="cart-card">
      <div class="cart-hd">
        <h3 class="cart-title">
          장바구니
          <span class="cart-cnt" v-if="cart.length">({{ cart.length }})</span>
        </h3>
        <button class="cart-clear-btn" v-if="cart.length" @click="cart.forEach(i => emit('removeCart', i.id))" title="전체 비우기">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/><path d="M9 6V4h6v2"/></svg>
        </button>
      </div>

      <div class="cart-items" v-if="cart.length">
        <div class="cart-row" v-for="item in cart" :key="item.id">
          <div class="cart-info">
            <span class="cart-name">{{ item.name }}</span>
            <span class="cart-item-price">{{ item.price.toLocaleString() }}원</span>
          </div>
          <div class="cart-ctrl">
            <button class="ctrl-btn" @click="emit('decrease', item)">-</button>
            <span class="ctrl-qty">{{ item.quantity }}</span>
            <button class="ctrl-btn" @click="emit('increase', item)">+</button>
            <button class="cart-remove-btn" @click="emit('removeCart', item.id)" title="메뉴 제거">×</button>
          </div>
        </div>
      </div>
      <p class="cart-empty" v-else>장바구니가 비어 있습니다.</p>

      <div class="cart-sum">
        <div class="sum-row sub"><span>소계</span><span>{{ totalPrice.toLocaleString() }}원</span></div>
        <div class="sum-row total"><span>합계</span><strong class="sum-total-val">{{ totalPrice.toLocaleString() }}원</strong></div>
      </div>
      <button class="btn-place-order" :disabled="!cart.length" @click="handleOrder">
        주문하기
      </button>
    </div>

    <div class="feedback-card">
      <div class="feedback-hd">
        <p class="sec-label">음식 후기</p>
        <button class="rv-more-btn" @click="emit('navigate', 'reviews')">전체 보기 ›</button>
      </div>

      <div class="rv-list" v-if="top3Reviews.length">
        <div class="rv-item rv-item-clickable" v-for="review in top3Reviews" :key="review.id" @click="emit('navigate', 'reviews')">
          <div class="rv-head">
            <div class="rv-stars">
              <span v-for="i in 5" :key="i" class="rv-star" :class="{ on: i <= review.rating }">★</span>
            </div>
            <span class="rv-rating">{{ review.rating }}/5</span>
          </div>
          <div class="rv-meta">
            <span class="rv-author">{{ getReviewAuthor(review) }}</span>
            <span class="rv-divider">•</span>
            <span class="rv-menu">{{ review.menuName || '메뉴 정보 없음' }}</span>
          </div>
          <p class="rv-text">{{ review.content }}</p>
        </div>
      </div>

      <p class="cart-empty" v-else>등록된 후기가 아직 없습니다.</p>
    </div>
  </div>
</template>
