<script setup>
import { computed, onMounted, ref } from 'vue'
import "../../styles/pages/our-story.css"

const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080/api'

const emit = defineEmits(['navigate'])
const signatureMenus = ref([])

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

async function loadSignatureMenus() {
  try {
    const response = await fetch(`${API_BASE}/customer/menus`)
    if (!response.ok) throw new Error('failed to load menus')
    const data = await response.json()
    signatureMenus.value = data.slice(0, 4)
  } catch (_) {
    signatureMenus.value = []
  }
}

function getMenuImage(menu) {
  return localImageMap[menu.name] || menu.imageUrl || '/hero-character.png'
}

const featureItems = computed(() => [
  {
    iconClass: 'pink',
    icon: '🍽',
    title: '신선한 식재료',
    text: '매일 엄선한 재료로 정직하게 조리하고, 메뉴 본연의 맛을 가장 먼저 생각합니다.'
  },
  {
    iconClass: 'teal',
    icon: '👨‍🍳',
    title: '경력 있는 셰프',
    text: '풍부한 현장 경험을 가진 셰프가 모든 메뉴의 맛과 완성도를 꼼꼼하게 관리합니다.'
  },
  {
    iconClass: 'amber',
    icon: '🤖',
    title: 'AI 맞춤 추천',
    text: '상황과 취향에 맞는 메뉴를 빠르게 추천해 더 편하게 선택하실 수 있습니다.'
  },

])

onMounted(loadSignatureMenus)
</script>

<template>
  <div class="story-page">
    <div class="story-hero">
      <div class="story-hero-inner">
        <p class="hero-label">Our Story</p>
        <h1 class="story-title">맛으로 연결되는<br /><span>특별한 경험</span></h1>
        <p class="story-sub">
          CAT TABLE은 좋은 재료와 정성 어린 조리로 매일 새로운 미식 경험을 전합니다.<br />
          한 끼의 식사가 하나의 추억처럼 남도록 공간과 메뉴를 세심하게 준비합니다.
        </p>
        <div class="story-hero-btns">
          <button class="btn-pink story-btn" @click="emit('navigate', 'menu')">메뉴 보러 가기</button>
          <button class="story-btn-ghost" @click="emit('navigate', 'reviews')">고객 후기 보기</button>
        </div>
        <div class="hero-stats">
          <div class="hs-item">
            <span class="hs-num">12<span>+</span></span>
            <span class="hs-label">Years of Excellence</span>
          </div>
          <div class="hs-item">
            <span class="hs-num">48<span>+</span></span>
            <span class="hs-label">시그니처 메뉴</span>
          </div>
          <div class="hs-item">
            <span class="hs-num">9.4<span>/10</span></span>
            <span class="hs-label">고객 만족도</span>
          </div>
          <div class="hs-item">
            <span class="hs-num">2<span>만</span></span>
            <span class="hs-label">누적 방문 고객</span>
          </div>
        </div>
      </div>
    </div>

    <div class="story-section">
      <div class="story-split">
        <div class="story-img-wrap">
          <span class="story-img-placeholder">shop</span>
          <div class="story-img-badge">Since 2013</div>
        </div>
        <div class="story-text">
          <div class="section-hd">
            <span class="sec-eyebrow">Brand Story</span>
            <h2 class="sec-heading">작은 주방에서<br />시작된 큰 꿈</h2>
          </div>
          <p>
            2013년, 홍대 골목의 작은 주방에서 CAT TABLE의 이야기가 시작되었습니다.
            좋은 재료와 정직한 조리, 그리고 진심 어린 서비스를 가장 중요한 기준으로 삼았습니다.
          </p>
          <p>
            처음의 소박한 마음은 지금도 변하지 않았습니다. 공간은 더 넓어지고 메뉴는 더 풍성해졌지만,
            한 사람의 식사도 소홀히 하지 않는 태도로 오늘의 테이블을 준비합니다.
          </p>
          <div class="story-values">
            <div class="sv-item">
              <span class="sv-icon">🥬</span>
              <div class="sv-body">
                <span class="sv-name">신선한 재료 우선</span>
                <span class="sv-desc">좋은 식재료가 요리의 시작이라는 믿음으로 매일 정성스럽게 준비합니다.</span>
              </div>
            </div>
            <div class="sv-item">
              <span class="sv-icon">🔥</span>
              <div class="sv-body">
                <span class="sv-name">정직한 조리</span>
                <span class="sv-desc">화려함보다 기본에 충실한 조리로 재료 본연의 맛을 살립니다.</span>
              </div>
            </div>
            <div class="sv-item">
              <span class="sv-icon">💗</span>
              <div class="sv-body">
                <span class="sv-name">마음을 담은 서비스</span>
                <span class="sv-desc">기억에 남는 식사 경험이 되도록 세심한 응대와 분위기를 준비합니다.</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <hr class="story-divider" />

    <div class="story-section">
      <div class="section-hd center">
        <span class="sec-eyebrow">Why CAT TABLE</span>
        <h2 class="sec-heading">우리가 지키는 기준</h2>
        <p class="sec-desc">CAT TABLE이 매일 같은 마음으로 손님을 맞이하는 이유입니다.</p>
      </div>
      <div class="feature-grid">
        <div class="feat-card" v-for="item in featureItems" :key="item.title">
          <div class="feat-icon-wrap" :class="item.iconClass">{{ item.icon }}</div>
          <h3 class="feat-title">{{ item.title }}</h3>
          <p class="feat-text">{{ item.text }}</p>
        </div>
      </div>
    </div>

    <div class="chef-section">
      <div class="chef-inner">
        <div class="chef-portrait">
          <div class="chef-avatar">chef</div>
          <h3 class="chef-name">CAT 셰프</h3>
          <p class="chef-role">Head Chef & Founder</p>
          <div class="chef-awards">
            <span class="award-badge">파리 수련</span>
            <span class="award-badge">도쿄 미식 경험</span>
            <span class="award-badge">경력 20년+</span>
          </div>
        </div>
        <div class="chef-info">
          <div class="section-hd">
            <span class="sec-eyebrow">Meet the Chef</span>
            <h2 class="sec-heading">요리로 이야기하는 사람</h2>
          </div>
          <p class="chef-bio">
            CAT TABLE의 셰프는 다양한 도시에서 쌓은 경험을 바탕으로 한식의 깊은 맛과 서양 조리 기법을
            자연스럽게 연결해 왔습니다. 익숙한 음식도 더 정갈하고 특별하게 완성하는 것이 셰프의 가장 큰 강점입니다.
          </p>
          <div class="chef-career">
            <div class="career-item">
              <span class="career-year">2004 ~ 2008</span>
              <div class="career-body">
                <span class="career-title">프랑스 파리 현지 수련</span>
                <span class="career-place">Paris, France</span>
              </div>
            </div>
            <div class="career-item">
              <span class="career-year">2008 ~ 2012</span>
              <div class="career-body">
                <span class="career-title">도쿄 미식 레스토랑 근무</span>
                <span class="career-place">Tokyo, Japan</span>
              </div>
            </div>
            <div class="career-item">
              <span class="career-year">2013 ~ 현재</span>
              <div class="career-body">
                <span class="career-title">CAT TABLE 운영 및 총괄 셰프</span>
                <span class="career-place">Seoul, Korea</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="story-section">
      <div class="section-hd center">
        <span class="sec-eyebrow">Signature</span>
        <h2 class="sec-heading">셰프 추천 시그니처 메뉴</h2>
        <p class="sec-desc">실제 메뉴 DB에서 불러온 대표 메뉴를 바로 확인해보세요.</p>
      </div>
      <div class="sig-grid" v-if="signatureMenus.length">
        <div class="sig-card" v-for="menu in signatureMenus" :key="menu.id">
          <div class="sig-thumb">
            <img :src="getMenuImage(menu)" :alt="menu.name" class="sig-thumb-img" />
          </div>
          <div class="sig-body">
            <span class="sig-category">{{ menu.category }}</span>
            <span class="sig-name">{{ menu.name }}</span>
            <p class="sig-desc">{{ menu.description }}</p>
            <span class="sig-price">{{ menu.price.toLocaleString() }}원</span>
          </div>
        </div>
      </div>
      <div class="empty-state" v-else>
        <p>시그니처 메뉴를 불러오는 중입니다.</p>
      </div>
    </div>

    <div class="info-section">
      <div class="info-inner">
        <div class="info-card">
          <span class="info-icon">🕰</span>
          <h3 class="info-title">영업 시간</h3>
          <div class="info-rows">
            <div class="info-row">
              <span class="info-key">평일</span>
              <span class="info-val open">11:30 ~ 22:00</span>
            </div>
            <div class="info-row">
              <span class="info-key">주말</span>
              <span class="info-val open">11:00 ~ 22:30</span>
            </div>
            <div class="info-row">
              <span class="info-key">브레이크 타임</span>
              <span class="info-val closed">15:00 ~ 17:00</span>
            </div>
          </div>
        </div>
        <div class="info-card">
          <span class="info-icon">📍</span>
          <h3 class="info-title">오시는 길</h3>
          <div class="info-rows">
            <div class="info-row">
              <span class="info-key">주소</span>
              <span class="info-val">서울 마포구 연남동 12</span>
            </div>
            <div class="info-row">
              <span class="info-key">지하철</span>
              <span class="info-val">홍대입구역 도보 3분</span>
            </div>
            <div class="info-row">
              <span class="info-key">전화</span>
              <span class="info-val">02-1234-5678</span>
            </div>
          </div>
        </div>
        <div class="info-card">
          <span class="info-icon">📞</span>
          <h3 class="info-title">예약 안내</h3>
          <div class="info-rows">
            <div class="info-row">
              <span class="info-key">예약 방법</span>
              <span class="info-val">전화 및 온라인</span>
            </div>
            <div class="info-row">
              <span class="info-key">단체 예약</span>
              <span class="info-val">10인 이상 가능</span>
            </div>
            <div class="info-row">
              <span class="info-key">유아 의자</span>
              <span class="info-val open">준비 완료</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="story-cta">
      <h2 class="cta-title">지금 바로 <span>방문</span>하고<br />특별한 식사를 경험해보세요</h2>
      <p class="cta-sub">
        정성스럽게 준비한 메뉴와 공간이 기다리고 있습니다.<br />
        오늘의 식사를 조금 더 특별하게 만들어보세요.
      </p>
      <div class="cta-btns">
        <button class="btn-pink story-btn" @click="emit('navigate', 'menu')">메뉴 보러 가기</button>
        <button class="cta-btn-ghost" @click="emit('navigate', 'reviews')">고객 후기 보기</button>
      </div>
    </div>
  </div>
</template>
