<script setup>
import { reactive } from 'vue'
import "./../../styles/menu/menu-grid.css"

defineProps({
  menus: Array,
  selectedCategory: String,
  categories: Array,
  traffic: Object
})

const emit = defineEmits(['addToCart', 'selectCategory'])

const localImageMap = {
  '제육볶음' : '/spicy-pork.png', 
  '비빔밥' : '/bibimbap.png',
  '된장찌개' : '/miso-soup.png',
  '갈비탕' : '/galbitang.png',
  '새우 튀김 우동' : '/fried-shrimp-udon.png',
  '트러플 크림 파스타': '/cream-pasta.png',
  '돈카츠 정식': '/cuttlet.png',
  '아이스 아메리카노': '/americano.png',
  '아메리카노': '/americano.png',
  '카페라떼': '/cafe-latte.png',
  '라떼': '/cafe-latte.png',
  '마르게리타 피자': '/pizza.png',
  '레몬에이드': '/lemonade.png',
  '생과일 주스': '/fruit-juice.png',
  '연어 사시미': '/salmon.png',
  '안심 스테이크': '/stake.png',
  '해산물 토마토 파스타': '/tomato-pasta.png',
  '규동': '/beef-rice.png'
}

const failedImageMenus = reactive({})

function getMenuImage(menu) {
  if (failedImageMenus[menu.id]) {
    return '/hero-character.png'
  }

  return localImageMap[menu.name] || menu.imageUrl || '/hero-character.png'
}

function handleImageError(menu) {
  failedImageMenus[menu.id] = true
}
</script>

<template>
  <div class="menu-section">
    <div class="cat-row">
      <div class="cat-tabs">
        <button
          class="cat-tab"
          :class="{ active: !selectedCategory }"
          @click="emit('selectCategory', '')"
        >전체</button>
        <button
          v-for="cat in categories"
          :key="cat"
          class="cat-tab"
          :class="{ active: selectedCategory === cat }"
          @click="emit('selectCategory', cat)"
        >{{ cat }}</button>
      </div>
      <div class="wait-badge" v-if="traffic">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/>
        </svg>
        대기 {{ traffic.estimatedWaitMinutes }}분
      </div>
    </div>

    <div class="menu-grid" v-if="menus.length">
      <div class="menu-item" v-for="menu in menus" :key="menu.id">
        <div class="mi-img-wrap">
          <img
            :src="getMenuImage(menu)"
            :alt="menu.name"
            class="mi-img"
            @error="handleImageError(menu)"
          />
        </div>
        <div class="mi-body">
          <div class="mi-top">
            <h3 class="mi-name">{{ menu.name }}</h3>
            <span class="mi-price">{{ menu.price.toLocaleString() }}원</span>
          </div>
          <p class="mi-desc">{{ menu.description }}</p>
          <div class="mi-footer">
            <span class="mi-eta">{{ menu.etaMinutes }}분 소요</span>
            <button class="btn-add" @click="emit('addToCart', menu)">담기</button>
          </div>
        </div>
      </div>
    </div>

    <div class="empty-menus" v-else>
      <p>이 카테고리에는 메뉴가 없습니다.</p>
    </div>
  </div>
</template>
