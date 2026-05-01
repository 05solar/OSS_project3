<script setup>
import "./../../styles/pages/menu-edit.css"

defineProps({
  menus: Array,
  categories: Array,
  newMenu: Object,
  editingMenuId: Number,
  categoryDraft: String
})

const emit = defineEmits([
  'saveMenu',
  'updateNewMenu',
  'updateCategoryDraft',
  'createCategory',
  'startEditMenu',
  'cancelEdit'
])
</script>

<template>
  <div class="menu-edit-page">
    <div class="page-hd">
      <p class="sec-label">MENU MANAGEMENT</p>
      <h2 class="page-title">메뉴 편집</h2>
    </div>

    <div class="me-layout">
      <article class="form-card">
        <h3 class="card-title">{{ editingMenuId ? '메뉴 수정' : '신규 메뉴 등록' }}</h3>
        <div class="form-body">
          <div class="fg">
            <label class="flabel">메뉴명</label>
            <input class="finput" :value="newMenu.name" @input="emit('updateNewMenu', { name: $event.target.value })" placeholder="메뉴 이름" />
          </div>
          <div class="fg">
            <label class="flabel">설명</label>
            <input class="finput" :value="newMenu.description" @input="emit('updateNewMenu', { description: $event.target.value })" placeholder="메뉴 설명" />
          </div>
          <div class="form-row">
            <div class="fg">
              <label class="flabel">카테고리</label>
              <select class="finput" :value="newMenu.category" @change="emit('updateNewMenu', { category: $event.target.value })">
                <option v-for="category in categories" :key="category" :value="category">{{ category }}</option>
              </select>
            </div>
            <div class="fg">
              <label class="flabel">키워드</label>
              <input class="finput" :value="newMenu.keywords" @input="emit('updateNewMenu', { keywords: $event.target.value })" placeholder="키워드 (쉼표 구분)" />
            </div>
          </div>
          <div class="inline-category">
            <input class="finput" :value="categoryDraft" @input="emit('updateCategoryDraft', $event.target.value)" placeholder="새 카테고리 이름" />
            <button class="btn-inline" @click="emit('createCategory')">카테고리 추가</button>
          </div>
          <div class="form-row">
            <div class="fg">
              <label class="flabel">가격 (원)</label>
              <input class="finput" type="number" :value="newMenu.price" @input="emit('updateNewMenu', { price: Number($event.target.value) })" />
            </div>
            <div class="fg">
              <label class="flabel">조리 시간 (분)</label>
              <input class="finput" type="number" :value="newMenu.etaMinutes" @input="emit('updateNewMenu', { etaMinutes: Number($event.target.value) })" />
            </div>
          </div>
          <div class="fg">
            <label class="flabel">이미지 URL</label>
            <input class="finput" :value="newMenu.imageUrl" @input="emit('updateNewMenu', { imageUrl: $event.target.value })" placeholder="https://..." />
          </div>
          <label class="toggle-row">
            <input type="checkbox" :checked="newMenu.available" @change="emit('updateNewMenu', { available: $event.target.checked })" />
            <span>판매중 상태로 표시</span>
          </label>
        </div>
        <div class="action-row">
          <button class="btn-register" @click="emit('saveMenu')">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
            {{ editingMenuId ? '메뉴 저장' : '메뉴 등록' }}
          </button>
          <button v-if="editingMenuId" class="btn-secondary" @click="emit('cancelEdit')">취소</button>
        </div>
      </article>

      <article class="list-card">
        <div class="card-hd">
          <h3 class="card-title">등록된 메뉴</h3>
          <span class="cnt-badge">{{ menus.length }}</span>
        </div>
        <div class="menu-list">
          <div class="menu-row" v-for="menu in menus" :key="menu.id">
            <div class="mr-img">
              <img :src="menu.imageUrl" :alt="menu.name" />
            </div>
            <div class="mr-info">
              <strong class="mr-name">{{ menu.name }}</strong>
              <p class="mr-cat">{{ menu.category }} · {{ menu.keywords }}</p>
            </div>
            <div class="mr-right">
              <span class="mr-price">{{ menu.price.toLocaleString() }}원</span>
              <span class="mr-avail" :class="menu.available ? 'avail-on' : 'avail-off'">
                {{ menu.available ? '판매중' : '판매중지' }}
              </span>
              <button class="btn-edit" @click="emit('startEditMenu', menu)">수정</button>
            </div>
          </div>
        </div>
      </article>
    </div>
  </div>
</template>
