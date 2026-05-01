<script setup>
import "./../../styles/pages/mypage.css"
import "./../../styles/pages/order-history.css"
defineProps({ auth: Object, orders: Array })
const emit = defineEmits(['navigate', 'logout'])
</script>

<template>
  <div class="page-wrap">
    <div class="page-inner">

      <div class="page-hd">
        <p class="sec-label">ACCOUNT</p>
        <h2 class="page-title">마이페이지</h2>
      </div>

      <!-- 프로필 카드 -->
      <div class="profile-card">
        <div class="profile-avatar">{{ auth?.user?.username?.[0]?.toUpperCase() ?? 'U' }}</div>
        <div class="profile-info">
          <p class="profile-name">{{ auth?.user?.username }}</p>
          <p class="profile-role">{{ auth?.user?.role === 'ADMIN' ? '관리자' : '일반 회원' }}</p>
        </div>
        <button class="btn-outline profile-logout" @click="emit('logout')">로그아웃</button>
      </div>

      <!-- 주문 통계 -->
      <div class="myp-stats" v-if="orders && orders.length">
        <div class="myp-stat">
          <p class="myp-stat-label">총 주문 수</p>
          <strong class="myp-stat-val">{{ orders.length }}건</strong>
        </div>
        <div class="myp-stat">
          <p class="myp-stat-label">총 결제 금액</p>
          <strong class="myp-stat-val">{{ orders.reduce((s, o) => s + o.totalAmount, 0).toLocaleString() }}원</strong>
        </div>
        <div class="myp-stat">
          <p class="myp-stat-label">평균 주문 금액</p>
          <strong class="myp-stat-val">{{ Math.round(orders.reduce((s, o) => s + o.totalAmount, 0) / orders.length).toLocaleString() }}원</strong>
        </div>
      </div>

      <!-- 주문 내역 -->
      <div class="myp-section">
        <div class="myp-section-hd">
          <p class="sec-label">주문 내역</p>
          <span class="myp-count" v-if="orders && orders.length">총 {{ orders.length }}건</span>
        </div>

        <div class="empty-state" v-if="!orders || !orders.length">
          <div class="empty-icon">🛒</div>
          <p>아직 주문 내역이 없습니다.</p>
          <button class="btn-pink" style="margin-top:8px" @click="emit('navigate', 'menu')">메뉴 보러 가기</button>
        </div>

        <div class="orders-grid" v-else>
          <div class="order-card" v-for="order in orders" :key="order.id">
            <div class="oc-top">
              <span class="oc-id">#{{ order.id }}</span>
              <span class="oc-amount">{{ order.totalAmount.toLocaleString() }}원</span>
            </div>
            <div class="oc-items">
              <span class="oc-tag" v-for="item in order.items" :key="item.menuName">
                {{ item.menuName }} × {{ item.quantity }}
              </span>
            </div>
            <div class="oc-status">
              <span class="status-badge done">{{ order.status === 'RECEIVED' ? '접수됨' : order.status }}</span>
            </div>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>
