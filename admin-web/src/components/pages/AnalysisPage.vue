<script setup>
import "./../../styles/pages/analysis.css"
defineProps({ dashboard: Object, orders: Array, reviews: Array })

function statusLabel(status) {
  const map = { RECEIVED: '접수됨', COOKING: '조리중', READY: '준비완료', DELIVERED: '완료' }
  return map[status] || status
}
function statusClass(status) {
  const map = { RECEIVED: 'st-received', COOKING: 'st-cooking', READY: 'st-ready', DELIVERED: 'st-delivered' }
  return map[status] || 'st-received'
}
</script>

<template>
  <div class="analysis-page">
    <div class="page-hd">
      <p class="sec-label">BUSINESS INTELLIGENCE</p>
      <h2 class="page-title">분석</h2>
    </div>

    <div v-if="dashboard">
      <div class="stats-row">
        <article class="stat-card">
          <div class="sc-top">
            <div class="sc-icon pink">💰</div>
            <span class="sc-trend up">+12.5%</span>
          </div>
          <p class="sc-label">일일 매출</p>
          <strong class="sc-val">{{ dashboard.sales.totalSales.toLocaleString() }}원</strong>
        </article>
        <article class="stat-card">
          <div class="sc-top">
            <div class="sc-icon teal">🛍️</div>
            <span class="sc-trend up">+8.2%</span>
          </div>
          <p class="sc-label">총 주문</p>
          <strong class="sc-val">{{ dashboard.sales.totalOrders }}</strong>
        </article>
        <article class="stat-card">
          <div class="sc-top">
            <div class="sc-icon amber">⭐</div>
            <span class="sc-trend neutral">Stable</span>
          </div>
          <p class="sc-label">평균 평점</p>
          <strong class="sc-val">{{ Number(dashboard.reviews.averageRating).toFixed(2) }}</strong>
        </article>
        <article class="stat-card">
          <div class="sc-top">
            <div class="sc-icon green">🍴</div>
            <span class="sc-trend neutral">등록됨</span>
          </div>
          <p class="sc-label">총 메뉴</p>
          <strong class="sc-val">{{ dashboard.menus.length }}</strong>
        </article>
      </div>

      <div class="data-grid">
        <article class="data-card">
          <div class="card-hd">
            <h3 class="card-title">주문 현황</h3>
            <span class="cnt-badge">{{ orders.length }}</span>
          </div>
          <div class="order-list">
            <div class="order-row" v-for="(order, idx) in orders.slice(0, 8)" :key="order.id">
              <div class="ot-avatar" :style="{ background: ['#FFB3C6','#B3E5FC','#C8E6C9'][idx % 3] }">
                {{ order.username ? order.username[0].toUpperCase() : 'U' }}
              </div>
              <div class="or-info">
                <span class="or-id">#{{ order.id }}</span>
                <span class="or-name">{{ order.username }}</span>
              </div>
              <p class="or-items">{{ order.items.map(it => `${it.menuName} x${it.quantity}`).join(', ') }}</p>
              <span class="ot-status" :class="statusClass(order.status)">
                {{ statusLabel(order.status) }}
              </span>
            </div>
          </div>
        </article>

        <article class="data-card">
          <div class="card-hd">
            <h3 class="card-title">리뷰 요약</h3>
            <span class="cnt-badge">{{ reviews.length }}</span>
          </div>
          <div class="rv-list">
            <div class="rv-row" v-for="review in reviews.slice(0, 8)" :key="review.id">
              <div class="rv-top">
                <strong class="rv-menu">{{ review.menuName }}</strong>
                <div class="rv-stars">
                  <span v-for="i in 5" :key="i" class="rv-star" :class="{ on: i <= review.rating }">★</span>
                </div>
              </div>
              <p class="rv-text">{{ review.content }}</p>
            </div>
          </div>
        </article>
      </div>
    </div>
    <div class="empty-state" v-else><p>데이터를 불러오는 중입니다...</p></div>
  </div>
</template>