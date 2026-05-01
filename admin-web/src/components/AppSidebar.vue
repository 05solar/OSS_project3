<script setup>
import "./../styles/sidebar.css"
defineProps({ currentPage: String })
const emit = defineEmits(['navigate', 'refresh'])

const navItems = [
  { id: 'dashboard', label: '대시보드', emoji: '' },
  { id: 'orders', label: '주문 관리', emoji: '' },
  { id: 'menu-editor', label: '메뉴 관리', emoji: '' },
  { id: 'analytics', label: '분석', emoji: '' },
  { id: 'reviews', label: '후기 관리', emoji: '' },
  { id: 'ai', label: 'AI 운영', emoji: '' }
]
</script>

<template>
  <aside class="sidebar">
    <div class="sb-brand">
      <div class="sb-brand-logo">
        <img src="/admin-logo.png" class="sb-logo" alt="admin logo" />
        <p class="sb-title">CAT TABLE</p>
      </div>
      <p class="sb-sub">Kitchen Management</p>
    </div>

    <nav class="sb-nav">
      <button
        v-for="item in navItems"
        :key="item.id"
        class="sb-item"
        :class="{ active: currentPage === item.id }"
        @click="emit('navigate', item.id)"
      >
        <span class="sb-icon">{{ item.emoji }}</span>
        {{ item.label }}
      </button>
    </nav>

    <div class="sb-foot">
      <button class="sb-refresh" @click="emit('refresh')">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <polyline points="23 4 23 10 17 10"/><polyline points="1 20 1 14 7 14"/>
          <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
        </svg>
        새로고침
      </button>
    </div>
  </aside>
</template>
