<script setup>
import "./../../styles/pages/order-history.css"
defineProps({ orders: Array, auth: Object })
</script>

<template>
  <div class="page-wrap">
    <div class="page-inner">
      <div class="page-hd">
        <p class="sec-label">ACCOUNT</p>
        <h2 class="page-title">주문 내역</h2>
      </div>

      <div class="empty-state" v-if="!auth">
        <div class="empty-icon">🔒</div>
        <p>로그인 후 주문 내역을 확인할 수 있습니다.</p>
      </div>
      <div class="empty-state" v-else-if="!orders.length">
        <div class="empty-icon">🛒</div>
        <p>아직 주문 내역이 없습니다.</p>
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
            <span class="status-badge done">완료</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>