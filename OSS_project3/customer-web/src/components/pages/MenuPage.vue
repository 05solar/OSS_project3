<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import "./../../styles/pages/menu-page.css"
import MenuGrid from '../menu/MenuGrid.vue'
import AiGuide from '../menu/AiGuide.vue'
import CartPanel from '../menu/CartPanel.vue'

const props = defineProps({
  categories: Array,
  selectedCategory: String,
  menus: Array,
  cart: Array,
  auth: Object,
  totalPrice: Number,
  traffic: Object,
  chat: String,
  recommendResult: Object,
  reviews: Array,
  orders: Array
})

const emit = defineEmits([
  'selectCategory',
  'addToCart',
  'increase',
  'decrease',
  'removeCart',
  'submitOrder',
  'needLogin',
  'update:chat',
  'askAi',
  'navigate'
])

const currentSlide = ref(0)
let slideTimer = null

const heroSlides = computed(() => [
  {
    key: 'welcome',
    className: 'hero-slide-welcome',
    align: 'left',
    clickable: false,
    title: props.auth
      ? `환영합니다,${props.auth.user.username}님!`
      : '오늘의 인기 메뉴를\n만나보세요!',
    subtitle: '캣테이블이 엄선한 \n오늘의 추천 메뉴를 확인해보세요.'
  },
  {
    key: 'review',
    className: 'hero-slide-review',
    align: 'right',
    clickable: true,
    target: 'reviews'
  },
  {
    key: 'about',
    className: 'hero-slide-about',
    align: 'right',
    clickable: true,
    target: 'about'
  }
])

function moveSlide(direction) {
  const total = heroSlides.value.length
  currentSlide.value = (currentSlide.value + direction + total) % total
}
function goToSlide(index) { currentSlide.value = index }
function resetAutoSlide() {
  if (slideTimer) clearInterval(slideTimer)
  slideTimer = setInterval(() => moveSlide(1), 15000)
}
function handleSlideAction(slide) {
  if (slide.clickable && slide.target) emit('navigate', slide.target)
}
function handleArrow(direction) { moveSlide(direction); resetAutoSlide() }

onMounted(() => resetAutoSlide())
onBeforeUnmount(() => { if (slideTimer) clearInterval(slideTimer) })
</script>

<template>
  <div class="menu-page">
    <div class="mp-layout">
      <div class="mp-left">
        <div class="hero-slider">
          <div class="hero-viewport">
            <div class="hero-track" :style="{ transform: `translateX(-${currentSlide * 100}%)` }">
              <article
                v-for="(slide, index) in heroSlides"
                :key="slide.key"
                class="hero-card"
                :class="[slide.className, `is-${slide.align}`, { 'is-clickable': slide.clickable }]"
                @click="handleSlideAction(slide)"
              >
                <div class="hero-content">
                  <template v-if="index === 0">
                    <h1 class="hero-title">{{ slide.title }}</h1>
                    <p class="hero-sub">{{ slide.subtitle }}</p>
                  </template>
                  <template v-else-if="index === 1">
                    <div class="hero-stars" aria-label="5점 만점">
                      <span v-for="star in 5" :key="star">★</span>
                    </div>
                    <p class="hero-quote">"지금까지 먹어본것중에 <br />제일 맛있어요"</p>
                    <p class="hero-credit">-맛있으면 우는 고양이</p>
                  </template>
                  <template v-else>
                    <p class="hero-story">
                      내가 먹는다는 생각으로<br />
                      <span class="hero-accent">최고의 재료</span>와 함께<br />
                      <span class="hero-accent">정성</span>을 담아 만들고 있습니다.
                    </p>
                  </template>
                </div>
              </article>
            </div>
          </div>
          <button class="hero-arrow hero-arrow-prev" type="button" @click.stop="handleArrow(-1)" aria-label="이전 배너">‹</button>
          <button class="hero-arrow hero-arrow-next" type="button" @click.stop="handleArrow(1)" aria-label="다음 배너">›</button>
          <div class="hero-dots">
            <button
              v-for="(slide, index) in heroSlides"
              :key="`${slide.key}-dot`"
              class="hero-dot"
              :class="{ active: currentSlide === index }"
              type="button"
              :aria-label="`${index + 1}번 배너 보기`"
              @click="goToSlide(index); resetAutoSlide()"
            />
          </div>
        </div>

        <AiGuide
          :chat="props.chat"
          :recommendResult="props.recommendResult"
          @update:chat="emit('update:chat', $event)"
          @askAi="emit('askAi')"
        />

        <MenuGrid
          :menus="props.menus"
          :selectedCategory="props.selectedCategory"
          :categories="props.categories"
          :traffic="props.traffic"
          @addToCart="emit('addToCart', $event)"
          @selectCategory="emit('selectCategory', $event)"
        />

        <div class="recent-orders" v-if="props.orders && props.orders.length">
          <div class="ro-hd">
            <p class="sec-label">최근 주문 내역</p>
          </div>
          <div class="ro-list">
            <div class="ro-item" v-for="order in props.orders.slice(0, 4)" :key="order.id">
              <span class="ro-id">#{{ order.id }}</span>
              <span class="ro-menus">{{ order.items.map(i => i.menuName).join(', ') }}</span>
              <span class="ro-price">{{ order.totalAmount.toLocaleString() }}원</span>
            </div>
          </div>
        </div>
      </div>

      <CartPanel
        :cart="props.cart"
        :auth="props.auth"
        :totalPrice="props.totalPrice"
        :reviews="props.reviews"
        @increase="emit('increase', $event)"
        @decrease="emit('decrease', $event)"
        @removeCart="emit('removeCart', $event)"
        @submitOrder="emit('submitOrder')"
        @needLogin="emit('needLogin')"
        @navigate="emit('navigate', $event)"
      />
    </div>
  </div>
</template>
