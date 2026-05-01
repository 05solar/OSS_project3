<script setup>
import "./../../styles/pages/dashboard.css"
import { ref } from 'vue'

defineProps({
  dashboard: Object,
  orders: Array,
  reviews: Array,
  menus: Array,
  categories: Array,
  newMenu: Object,
  editingMenuId: Number,
  categoryDraft: String,
  aiSuggestion: String,
  quality: String
})

const emit = defineEmits([
  'saveMenu',
  'suggestMenu',
  'evaluateQuality',
  'updateNewMenu',
  'updateStatus',
  'updateCategoryDraft',
  'createCategory',
  'startEditMenu',
  'cancelEdit'
])
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
  <div class="dashboard">
    <div class="stats-row" v-if="dashboard">
      <article class="stat-card">
        <div class="sc-top">
          <div class="sc-icon pink">매출</div>
          <span class="sc-trend neutral">오늘</span>
        </div>
        <p class="sc-label">오늘 매출</p>
        <strong class="sc-val">{{ dashboard.sales.todaySales.toLocaleString() }}원</strong>
      </article>
      <article class="stat-card">
        <div class="sc-top">
          <div class="sc-icon teal">주문</div>
          <span class="sc-trend neutral">누적</span>
        </div>
        <p class="sc-label">총 주문</p>
        <strong class="sc-val">{{ dashboard.sales.totalOrders }}</strong>
      </article>
      <article class="stat-card">
        <div class="sc-top">
          <div class="sc-icon amber">평점</div>
          <span class="sc-trend neutral">평균</span>
        </div>
        <p class="sc-label">평균 별점</p>
        <strong class="sc-val">{{ Number(dashboard.reviews.averageRating).toFixed(1) }}</strong>
      </article>
      <article class="stat-card">
        <div class="sc-top">
          <div class="sc-icon green">메뉴</div>
          <span class="sc-trend neutral">등록</span>
        </div>
        <p class="sc-label">총 메뉴</p>
        <strong class="sc-val">{{ dashboard.menus.length }}</strong>
      </article>
    </div>
    <div class="stats-row loading-row" v-else>
      <div class="stat-card skeleton" v-for="i in 4" :key="i"></div>
    </div>

    <div class="mini-grid" v-if="dashboard">
      <article class="mini-card">
        <p class="mini-label">누적 매출</p>
        <strong>{{ dashboard.sales.totalSales.toLocaleString() }}원</strong>
      </article>
      <article class="mini-card">
        <p class="mini-label">오늘 주문</p>
        <strong>{{ dashboard.sales.todayOrders }}건</strong>
      </article>
      <article class="mini-card">
        <p class="mini-label">대기 주문</p>
        <strong>{{ dashboard.sales.pendingOrders }}건</strong>
      </article>
      <article class="mini-card">
        <p class="mini-label">5점 후기</p>
        <strong>{{ dashboard.reviews.ratingDistribution.find((item) => item.rating === 5)?.count || 0 }}개</strong>
      </article>
    </div>

    <div class="ai-section">
      <h2 class="ai-section-title">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M12 2L9.5 9.5 2 12l7.5 2.5L12 22l2.5-7.5L22 12l-7.5-2.5z"/>
        </svg>
        AI Operations
      </h2>
      <div class="ai-cards">
        <article class="ai-op-card">
          <div class="ai-op-body">
            <h3 class="ai-op-title">AI 신메뉴 추천</h3>
            <p class="ai-op-desc" v-if="aiSuggestion">{{ aiSuggestion }}</p>
            <p class="ai-op-desc placeholder" v-else>주문 데이터를 바탕으로 새로운 메뉴 아이디어를 추천합니다.</p>
          </div>
          <button class="btn-teal ai-run-btn" @click="emit('suggestMenu')">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/></svg>
            분석 실행
          </button>
        </article>
        <article class="ai-op-card">
          <div class="ai-op-body">
            <h3 class="ai-op-title">서비스 품질 분석</h3>
            <p class="ai-op-desc" v-if="quality">{{ quality }}</p>
            <p class="ai-op-desc placeholder" v-else>고객 리뷰를 분석해 서비스 개선 포인트를 정리합니다.</p>
          </div>
          <button class="btn-teal ai-run-btn" @click="emit('evaluateQuality')">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>
            분석 실행
          </button>
        </article>
      </div>
    </div>

    <div class="bottom-grid">
      <article class="orders-card">
        <div class="card-hd">
          <h3 class="card-title">최근 주문 현황</h3>
          <span class="cnt-badge" v-if="orders.length">{{ orders.length }}건</span>
        </div>

        <div class="orders-table" v-if="orders.length">
          <div class="ot-header db-header">
            <span>주문 번호</span>
            <span>고객</span>
            <span>메뉴</span>
            <span>금액</span>
            <span>상태</span>
            <span>시간</span>
          </div>

          <div class="ot-row db-row" v-for="order in orders.slice(0, 6)" :key="order.id">
            <span class="ot-id">#{{ order.id }}</span>
            <div class="ot-customer">
              <div class="ot-avatar" style="background:#B2DFDB">
                {{ order.username ? order.username[0].toUpperCase() : 'U' }}
              </div>
              <span>{{ order.username }}</span>
            </div>
            <p class="ot-items">{{ order.items.map((it) => `${it.quantity}x ${it.menuName}`).join(', ') }}</p>
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
        </div>

        <div class="empty-orders" v-else>
          <p>주문 내역이 없습니다.</p>
        </div>
      </article>

      <article class="quick-add-card">
        <h3 class="card-title">{{ editingMenuId ? '빠른 메뉴 수정' : '빠른 메뉴 등록' }}</h3>
        <div class="qa-form">
          <div class="fg">
            <label class="flabel">메뉴명</label>
            <input class="finput" :value="newMenu.name" @input="emit('updateNewMenu', { name: $event.target.value })" placeholder="메뉴 이름" />
          </div>
          <div class="form-row">
            <div class="fg">
              <label class="flabel">카테고리</label>
              <select class="finput" :value="newMenu.category" @change="emit('updateNewMenu', { category: $event.target.value })">
                <option v-for="category in categories" :key="category" :value="category">{{ category }}</option>
              </select>
            </div>
            <div class="fg">
              <label class="flabel">가격</label>
              <input class="finput" type="number" :value="newMenu.price" @input="emit('updateNewMenu', { price: Number($event.target.value) })" />
            </div>
          </div>
          <div class="inline-category">
            <input class="finput" :value="categoryDraft" @input="emit('updateCategoryDraft', $event.target.value)" placeholder="새 카테고리 이름" />
            <button class="btn-inline" @click="emit('createCategory')">추가</button>
          </div>
          <div class="fg">
            <label class="flabel">조리 시간 (분)</label>
            <input class="finput" type="number" :value="newMenu.etaMinutes" @input="emit('updateNewMenu', { etaMinutes: Number($event.target.value) })" />
          </div>
          <div class="fg">
            <label class="flabel">설명</label>
            <input class="finput" :value="newMenu.description" @input="emit('updateNewMenu', { description: $event.target.value })" placeholder="메뉴 설명" />
          </div>
          <div class="fg">
            <label class="flabel">키워드</label>
            <input class="finput" :value="newMenu.keywords" @input="emit('updateNewMenu', { keywords: $event.target.value })" placeholder="쉼표로 구분" />
          </div>
          <div class="fg">
            <label class="flabel">이미지 URL</label>
            <input class="finput" :value="newMenu.imageUrl" @input="emit('updateNewMenu', { imageUrl: $event.target.value })" placeholder="https://..." />
          </div>
        </div>
        <div class="action-stack">
          <button class="btn-add-menu" @click="emit('saveMenu')">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
            {{ editingMenuId ? '메뉴 저장' : '메뉴 등록' }}
          </button>
          <button v-if="editingMenuId" class="btn-ghost" @click="emit('cancelEdit')">수정 취소</button>
        </div>

        <div class="recent-menus" v-if="menus.length">
          <div class="recent-row" v-for="menu in menus.slice(0, 4)" :key="menu.id">
            <div>
              <strong>{{ menu.name }}</strong>
              <p>{{ menu.category }} · {{ menu.price.toLocaleString() }}원</p>
            </div>
            <button class="btn-text" @click="emit('startEditMenu', menu)">수정</button>
          </div>
        </div>
      </article>
    </div>
  </div>
</template>
