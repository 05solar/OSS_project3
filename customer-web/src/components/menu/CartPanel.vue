<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import './../../styles/menu/cart-panel.css'

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

const shellRef = ref(null)
const cartRef = ref(null)
const feedbackRef = ref(null)
const shellStyle = ref({})
const cartStyle = ref({})
const cartBelowReviews = ref(false)

function getReviewAuthor(review) {
  return review.username || review.nickname || review.authorName || review.fullName || 'Guest'
}

function handleOrder() {
  if (!props.auth) emit('needLogin')
  else emit('submitOrder')
}

function syncCartPosition() {
  if (!shellRef.value || !cartRef.value || !feedbackRef.value) return

  if (window.innerWidth <= 640) {
    shellStyle.value = {}
    cartStyle.value = {}
    return
  }

  const gap = 14
  const cartHeight = cartRef.value.offsetHeight
  const feedbackHeight = feedbackRef.value.offsetHeight
  const shellTop = window.scrollY + shellRef.value.getBoundingClientRect().top
  const feedbackTop = cartHeight + gap
  const destination = feedbackTop + feedbackHeight + gap
  const triggerLine = shellTop + 220
  const scrollPoint = window.scrollY + 78
  const placeBelow = scrollPoint >= triggerLine
  cartBelowReviews.value = placeBelow

  shellStyle.value = {
    '--cart-slot-height': `${cartHeight + gap}px`,
    minHeight: `${destination + cartHeight}px`
  }

  cartStyle.value = {
    transform: placeBelow ? `translateY(${Math.round(destination)}px)` : 'translateY(0)'
  }
}

onMounted(async () => {
  await nextTick()
  syncCartPosition()
  window.addEventListener('resize', syncCartPosition)
  window.addEventListener('scroll', syncCartPosition, { passive: true })
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', syncCartPosition)
  window.removeEventListener('scroll', syncCartPosition)
})

watch(
  () => [props.cart?.length, props.totalPrice, props.reviews?.length],
  async () => {
    await nextTick()
    syncCartPosition()
  }
)
</script>

<template>
  <div ref="shellRef" class="right-col-shell" :style="shellStyle">
    <div ref="cartRef" class="cart-card cart-card-floating" :style="cartStyle">
      <div class="cart-hd">
        <h3 class="cart-title">
          장바구니
          <span v-if="cart.length" class="cart-cnt">({{ cart.length }})</span>
        </h3>
        <button
          v-if="cart.length"
          class="cart-clear-btn"
          title="Clear cart"
          @click="cart.forEach((item) => emit('removeCart', item.id))"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="3 6 5 6 21 6" />
            <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6" />
            <path d="M10 11v6" />
            <path d="M14 11v6" />
            <path d="M9 6V4h6v2" />
          </svg>
        </button>
      </div>

      <div v-if="cart.length" class="cart-items">
        <div v-for="item in cart" :key="item.id" class="cart-row">
          <div class="cart-info">
            <span class="cart-name">{{ item.name }}</span>
            <span class="cart-item-price">{{ item.price.toLocaleString() }} 원</span>
          </div>
          <div class="cart-ctrl">
            <button class="ctrl-btn" @click="emit('decrease', item)">-</button>
            <span class="ctrl-qty">{{ item.quantity }}</span>
            <button class="ctrl-btn" @click="emit('increase', item)">+</button>
            <button class="cart-remove-btn" title="Remove item" @click="emit('removeCart', item.id)">
              x
            </button>
          </div>
        </div>
      </div>
      <p v-else class="cart-empty">장바구니가 비어있습니다.</p>

      <div class="cart-sum">

        <div class="sum-row total">
          <span>합계</span>
          <strong class="sum-total-val">{{ totalPrice.toLocaleString() }} 원</strong>
        </div>
      </div>

      <button class="btn-place-order" :disabled="!cart.length" @click="handleOrder">
        주문하기
      </button>
    </div>

    <div ref="feedbackRef" class="feedback-card feedback-card-fixed">
      <div class="feedback-hd">
        <p class="sec-label">후기</p>
        <button class="rv-more-btn" @click="emit('navigate', 'reviews')">모두보기</button>
      </div>

      <div v-if="top3Reviews.length" class="rv-list">
        <div
          v-for="review in top3Reviews"
          :key="review.id"
          class="rv-item rv-item-clickable"
          @click="emit('navigate', 'reviews')"
        >
          <div class="rv-head">
            <div class="rv-stars">
              <span
                v-for="i in 5"
                :key="i"
                class="rv-star"
                :class="{ on: i <= review.rating }"
              >*</span>
            </div>
            <span class="rv-rating">{{ review.rating }}/5</span>
          </div>
          <div class="rv-meta">
            <span class="rv-author">{{ getReviewAuthor(review) }}</span>
            <span class="rv-divider">/</span>
            <span class="rv-menu">{{ review.menuName || 'No menu info' }}</span>
          </div>
          <p class="rv-text">{{ review.content }}</p>
        </div>
      </div>

      <p v-else class="cart-empty">No reviews yet.</p>
    </div>
  </div>
</template>
