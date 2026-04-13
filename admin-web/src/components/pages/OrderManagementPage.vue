<script setup>
import "./../../styles/pages/orders.css"
import { ref } from 'vue'

defineProps({ orders: Array })
const emit = defineEmits(['updateStatus'])

const updating = ref(new Set())

const STATUS_OPTIONS = [
  { value: 'RECEIVED', label: '접수', cls: 'st-received' },
  { value: 'COOKING', label: '조리', cls: 'st-cooking' },
  { value: 'READY', label: '준비', cls: 'st-ready' },
  { value: 'DELIVERED', label: '완료', cls: 'st-delivered' }
]

function statusClass(status) {
  return STATUS_OPTIONS.find((option) => option.value === status)?.cls ?? 'st-received'
}

function formatTime(iso) {
  if (!iso) return '-'
  return new Date(iso).toLocaleString('ko-KR', {
    month: 'numeric',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function onStatusChange(order, event) {
  const newStatus = event.target.value
  if (newStatus === order.status) return

  updating.value = new Set([...updating.value, order.id])
  emit('updateStatus', order.id, newStatus, () => {
    updating.value = new Set([...updating.value].filter((id) => id !== order.id))
  })
}
</script>

<template>
  <div class="orders-page">
    <div class="page-hd">
      <p class="sec-label">LIVE OPERATIONS</p>
      <h2 class="page-title">주문 관리</h2>
    </div>

    <div class="summary-row" v-if="orders.length">
      <div class="sum-badge"><span class="sum-dot amber"></span>접수 {{ orders.filter((o) => o.status === 'RECEIVED').length }}</div>
      <div class="sum-badge"><span class="sum-dot pink"></span>조리 {{ orders.filter((o) => o.status === 'COOKING').length }}</div>
      <div class="sum-badge"><span class="sum-dot green"></span>준비 {{ orders.filter((o) => o.status === 'READY').length }}</div>
      <div class="sum-badge"><span class="sum-dot muted"></span>완료 {{ orders.filter((o) => o.status === 'DELIVERED').length }}</div>
      <span class="total-cnt">총 {{ orders.length }}건</span>
    </div>

    <article class="orders-card" v-if="orders.length">
      <div class="ot-header">
        <span>주문 번호</span>
        <span>고객</span>
        <span>메뉴</span>
        <span>금액</span>
        <span>상태</span>
        <span>시간</span>
      </div>

      <div class="ot-row" v-for="order in orders" :key="order.id">
        <span class="ot-id">#{{ order.id }}</span>

        <div class="ot-customer">
          <div class="ot-avatar" :style="{ background: '#B2DFDB' }">
            {{ order.username ? order.username[0].toUpperCase() : 'U' }}
          </div>
          <span>{{ order.username }}</span>
        </div>

        <p class="ot-items">
          {{ order.items.map((it) => `${it.quantity}x ${it.menuName}`).join(', ') }}
        </p>

        <span class="ot-amount">{{ order.totalAmount?.toLocaleString() }}원</span>

        <div class="ot-select-wrap" :class="{ saving: updating.has(order.id) }">
          <select
            class="status-select"
            :class="statusClass(order.status)"
            :value="order.status"
            :disabled="updating.has(order.id)"
            @change="onStatusChange(order, $event)"
          >
            <option v-for="opt in STATUS_OPTIONS" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </option>
          </select>
          <span class="status-select-arrow" aria-hidden="true">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
              <path d="M6 9L12 15L18 9" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </span>
          <span v-if="updating.has(order.id)" class="saving-indicator">저장중…</span>
        </div>

        <span class="ot-time">{{ formatTime(order.createdAt) }}</span>
      </div>
    </article>

    <div class="empty-state" v-else>
      <p>주문 내역이 없습니다.</p>
    </div>
  </div>
</template>
