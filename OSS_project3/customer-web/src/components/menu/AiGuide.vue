<script setup>
import { computed, ref, watch } from 'vue'
import "./../../styles/menu/ai-guide.css"

const props = defineProps({
  chat: String,
  recommendResult: Object
})

const emit = defineEmits(['update:chat', 'askAi'])

const quickTags = ['#매운맛', '#빠른식사', '#다이어트', '#건강식', '#든든한식사']
const isResultOpen = ref(true)

watch(
  () => props.recommendResult,
  (next, prev) => {
    if (next && next !== prev) {
      isResultOpen.value = true
    }
  }
)

function splitBoldSegments(text) {
  return text.split(/(\*\*.*?\*\*)/g).filter(Boolean).map((part) => ({
    text: part.replace(/^\*\*|\*\*$/g, ''),
    bold: /^\*\*.*\*\*$/.test(part)
  }))
}

const formattedMessageLines = computed(() => {
  const raw = props.recommendResult?.message || ''
  const normalized = raw
    .replace(/([.!?])\s+/g, '$1\n')
    .replace(/([가-힣]+:)\s*/g, '\n$1 ')
    .replace(/\s*-\s*/g, '\n- ')
    .replace(/\n{3,}/g, '\n\n')
    .trim()

  return normalized
    .split('\n')
    .filter((line) => line.trim().length)
    .map((line) => splitBoldSegments(line))
})
</script>

<template>
  <div class="ai-card">
    <div class="ai-hd">
      <div class="ai-icon">
        <img src="/ai-icon.png" alt="CAT TABLE logo" class="ai-icon-img" />
      </div>
      <div>
        <h3 class="ai-title">오늘 뭐먹지?</h3>
        <p class="ai-sub">AI에게 메뉴추천을 받아보세요!</p>
      </div>
    </div>

    <div class="ai-input-row">
      <input
        class="ai-input"
        :value="chat"
        @input="emit('update:chat', $event.target.value)"
        placeholder="배는 안고픈데 밥은 먹어야겠어 밥은 별로 안먹고 싶어"
        @keyup.enter="emit('askAi')"
      />

      <div class="ai-actions">
        <div class="ai-tags">
          <button
            v-for="tag in quickTags"
            :key="tag"
            class="ai-tag"
            @click="emit('update:chat', tag.replace('#', '')); emit('askAi')"
          >{{ tag }}</button>
        </div>

        <button class="btn-pink ai-run-btn" @click="emit('askAi')">추천받기</button>
      </div>
    </div>

    <div v-if="recommendResult" class="ai-result">
      <div class="ai-result-head">
        <p class="ai-result-label">AI 추천 결과</p>
      </div>

      <Transition name="ai-result-collapse">
        <div v-if="isResultOpen" class="ai-result-body">
          <div class="ai-result-text">
            <p v-for="(line, lineIndex) in formattedMessageLines" :key="lineIndex" class="ai-result-line">
              <template v-for="(segment, segmentIndex) in line" :key="segmentIndex">
                <strong v-if="segment.bold" class="ai-result-strong">{{ segment.text }}</strong>
                <template v-else>{{ segment.text }}</template>
              </template>
            </p>
          </div>

          <div class="ai-chips-wrap" v-if="recommendResult.recommendedMenus?.length">
            <p class="ai-result-label ai-menu-label">추천 메뉴</p>
            <div class="ai-chips">
              <span class="ai-chip" v-for="item in recommendResult.recommendedMenus" :key="item.id">{{ item.name }}</span>
            </div>
          </div>
        </div>
      </Transition>

      <button class="ai-result-toggle" type="button" @click="isResultOpen = !isResultOpen">
        {{ isResultOpen ? '접기' : '펼치기' }}
      </button>
    </div>
  </div>
</template>
