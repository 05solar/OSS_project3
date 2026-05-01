<script setup>
import "./../../styles/pages/ai.css"
defineProps({ aiSuggestion: String, quality: String })
const emit = defineEmits(['suggestMenu', 'evaluateQuality'])

const infoItems = [
  { icon: '🎯', title: '데이터 기반 추천', desc: '실제 주문 데이터를 분석하여 고객 선호도에 맞는 신메뉴를 제안합니다.' },
  { icon: '⭐', title: '품질 모니터링', desc: '고객 리뷰를 자동 분석하여 서비스 개선 포인트를 파악합니다.' },
  { icon: '✍️', title: '리뷰 초안 생성', desc: '고객 웹에서 AI가 메뉴별 리뷰 초안을 자동으로 작성해 드립니다.' }
]
</script>

<template>
  <div class="ai-page">
    <div class="page-hd">
      <p class="sec-label">AI INTELLIGENCE HUB</p>
      <h2 class="page-title">AI 운영</h2>
    </div>

    <div class="ai-grid">
      <article class="ai-card">
        <div class="ai-card-hd">
          <div class="ai-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2L9.5 9.5 2 12l7.5 2.5L12 22l2.5-7.5L22 12l-7.5-2.5z"/></svg>
          </div>
          <div>
            <h3 class="ai-card-title">AI 신메뉴 추천</h3>
            <p class="ai-card-sub">주문 데이터를 분석하여 새로운 메뉴를 제안합니다</p>
          </div>
        </div>
        <div class="ai-result" v-if="aiSuggestion">
          <p class="ai-result-label">분석 결과</p>
          <p class="ai-result-text">{{ aiSuggestion }}</p>
        </div>
        <div class="ai-empty" v-else>
          <p>분석 실행 버튼을 클릭하면 주문 데이터 기반으로 신메뉴 아이디어를 추천해 드립니다.</p>
        </div>
        <button class="btn-teal" @click="emit('suggestMenu')">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/></svg>
          분석 실행
        </button>
      </article>

      <article class="ai-card">
        <div class="ai-card-hd">
          <div class="ai-icon quality">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>
          </div>
          <div>
            <h3 class="ai-card-title">서비스 품질 분석</h3>
            <p class="ai-card-sub">고객 후기를 분석하여 서비스 품질을 평가합니다</p>
          </div>
        </div>
        <div class="ai-result" v-if="quality">
          <p class="ai-result-label">평가 결과</p>
          <p class="ai-result-text">{{ quality }}</p>
        </div>
        <div class="ai-empty" v-else>
          <p>품질 평가 버튼을 클릭하면 고객 리뷰 데이터 기반으로 서비스 품질을 분석합니다.</p>
        </div>
        <button class="btn-teal" @click="emit('evaluateQuality')">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>
          품질 평가
        </button>
      </article>
    </div>

    <article class="info-card">
      <p class="sec-label">AI 기능 안내</p>
      <div class="info-grid">
        <div class="info-item" v-for="item in infoItems" :key="item.icon">
          <span class="info-icon">{{ item.icon }}</span>
          <div>
            <h4 class="info-title">{{ item.title }}</h4>
            <p class="info-desc">{{ item.desc }}</p>
          </div>
        </div>
      </div>
    </article>
  </div>
</template>
