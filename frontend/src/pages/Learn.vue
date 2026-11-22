<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api } from '../api/client';
import { useUserStore } from '../stores/user';

const route = useRoute();
const router = useRouter();
const user = useUserStore();

const platform = route.params.platform as string;
const songId = route.params.songId as string;

const detail = ref<any>(null);
const mode = ref<'FOCUS' | 'CASUAL'>('FOCUS');
const sessionId = ref('');
const stage = ref('PREVIEW');
const quiz = ref<any[]>([]);
const answers = ref<Record<string, string>>({});
const submitted = ref(false);
const loading = ref(false);

const stages = [
  { key: 'PREVIEW', label: '预习' },
  { key: 'LISTEN', label: '精听' },
  { key: 'FOLLOW', label: '跟读' },
  { key: 'PRACTICE', label: '练习' },
  { key: 'REVIEW', label: '复习结算' },
];

async function loadDetail() {
  detail.value = await api.get(`/songs/${platform}/${songId}`);
}

async function startSession() {
  loading.value = true;
  try {
    const s = await api.post('/sessions', {
      userId: user.userId,
      platform,
      songId,
      mode: mode.value,
      cefr: detail.value.cefr,
    });
    sessionId.value = s.sessionId;
    if (mode.value === 'FOCUS') await loadQuiz();
  } finally {
    loading.value = false;
  }
}

async function loadQuiz() {
  quiz.value = await api.get(`/practice/${sessionId.value}/quiz`);
}

function nextStage() {
  const idx = stages.findIndex((s) => s.key === stage.value);
  if (idx < stages.length - 1) {
    stage.value = stages[idx + 1].key;
    if (stage.value === 'PRACTICE' && sessionId.value) loadQuiz();
  }
}

async function submitPractice() {
  loading.value = true;
  try {
    const grade = await api.post(`/practice/${sessionId.value}/submit`, { answers: answers.value });
    sessionStorage.setItem('lastGrade', JSON.stringify({
      sessionId: sessionId.value,
      grade,
      cefr: detail.value.cefr,
      repeatCount: 0,
    }));
    submitted.value = true;
    router.push(`/settlement/${sessionId.value}`);
  } finally {
    loading.value = false;
  }
}

onMounted(loadDetail);
</script>

<template>
  <div class="grid" v-if="detail">
    <div class="card row spread">
      <div>
        <h1>{{ detail.title }}</h1>
        <div class="muted">{{ detail.artist }} · {{ detail.cefr }} · {{ detail.level }}</div>
      </div>
      <div class="row">
        <button class="btn" :class="mode === 'FOCUS' ? '' : 'ghost'" @click="mode = 'FOCUS'">刻意学习</button>
        <button class="btn" :class="mode === 'CASUAL' ? '' : 'ghost'" @click="mode = 'CASUAL'">休闲听歌</button>
      </div>
    </div>

    <!-- 阶段条 -->
    <div class="card steps">
      <div
        v-for="(s, i) in stages"
        :key="s.key"
        class="step"
        :class="{ active: stage === s.key, done: i < stages.findIndex((x) => x.key === stage) }"
      >
        <span class="dot">{{ i + 1 }}</span>{{ s.label }}
      </div>
    </div>

    <!-- 未开始：开始面板 -->
    <div class="card" v-if="!sessionId">
      <p class="muted" v-if="mode === 'CASUAL'">休闲模式仅浏览歌词与乐谱，不生成练习、不产出积分。</p>
      <p class="muted" v-else>完成预习 → 精听 → 跟读 → 练习整条闭环，经中台校验后可获得学习积分。</p>
      <div class="row" style="margin-top:14px">
        <button class="btn" @click="startSession" :disabled="loading">
          {{ mode === 'CASUAL' ? '进入收听' : '开始刻意学习' }}
        </button>
        <a class="btn ghost" :href="detail.playUrl" target="_blank" rel="noopener">跳转官方播放器 ↗</a>
      </div>
    </div>

    <!-- 预习 -->
    <div class="card" v-else-if="stage === 'PREVIEW'">
      <h2>预习 · 生词与发音难点</h2>
      <div class="grid vocab">
        <div class="vocab-item" v-for="v in detail.vocab" :key="v.word">
          <strong>{{ v.word }}</strong>
          <span class="muted">{{ v.phonetic }}</span>
          <div>{{ v.meaning }}</div>
        </div>
      </div>
      <div class="row" style="margin-top:16px">
        <button class="btn" @click="nextStage">进入精听 →</button>
      </div>
    </div>

    <!-- 精听 -->
    <div class="card" v-else-if="stage === 'LISTEN'">
      <h2>精听 · 分句循环与倍速</h2>
      <div class="lyric-line" v-for="(l, i) in detail.lines" :key="i">
        <div class="en">{{ l.original }}</div>
        <div class="zh muted">{{ l.translation }}</div>
      </div>
      <div class="row" style="margin-top:16px">
        <a class="btn ghost" :href="detail.playUrl" target="_blank" rel="noopener">打开官方播放器 ↗</a>
        <button class="btn" @click="nextStage">进入跟读 →</button>
      </div>
    </div>

    <!-- 跟读 -->
    <div class="card" v-else-if="stage === 'FOLLOW'">
      <h2>跟读 · 影子跟读与延时跟读</h2>
      <p class="muted">跟随原声逐句模仿重音、连读与弱读；中台将对音素偏差给出针对性纠正（本阶段为演示，语音评分由中台提供）。</p>
      <div class="row" style="margin-top:16px">
        <button class="btn" @click="nextStage">进入练习 →</button>
      </div>
    </div>

    <!-- 练习 -->
    <div class="card" v-else-if="stage === 'PRACTICE'">
      <h2>练习 · 动态组卷</h2>
      <div class="grid quiz">
        <div class="q" v-for="q in quiz" :key="q.id">
          <div class="q-stem">{{ q.stem }}</div>

          <!-- 选择类：听力填空 / 词义检索 -->
          <div class="row opts" v-if="q.type === 'FILL_IN_BLANK' || q.type === 'WORD_MATCH'">
            <label v-for="opt in q.options" :key="opt" class="opt">
              <input type="radio" :name="q.id" :value="opt" v-model="answers[q.id]" />
              {{ opt }}
            </label>
          </div>

          <!-- 句式仿写：文本输入 -->
          <textarea
            v-else-if="q.type === 'REWRITE'"
            v-model="answers[q.id]"
            class="rewrite-input"
            rows="2"
            placeholder="请用 “up above” 仿写一句描述高处的句子，例如：Up above the clouds flies a bird."
          ></textarea>

          <!-- 口语复述：标记完成（语音评分由中台提供） -->
          <div v-else class="oral">
            <button
              class="btn ghost"
              :class="{ done: answers[q.id] }"
              @click="answers[q.id] = answers[q.id] ? '' : 'recalled'"
            >{{ answers[q.id] ? '✓ 已跟读并复述' : '我已跟读并复述这句' }}</button>
          </div>

          <div class="muted" v-if="q.hint">提示：{{ q.hint }}</div>
        </div>
      </div>
      <div class="row" style="margin-top:16px">
        <button class="btn" @click="submitPractice" :disabled="loading || submitted">提交作答并结算</button>
      </div>
    </div>

    <!-- 结算 -->
    <div class="card" v-else-if="stage === 'REVIEW'">
      <h2>复习与结算</h2>
      <p class="muted">生词已进入间隔重复队列，完成后生成质量评分与学习断言。</p>
    </div>

    <p class="muted tip">本平台仅提供英语学习辅助标注服务，音频、原版歌词与版权由第三方音乐平台提供。</p>
  </div>
</template>

<style scoped>
.steps { display: flex; gap: 8px; flex-wrap: wrap; }
.step { display: flex; align-items: center; gap: 8px; color: var(--text-dim); font-size: 14px; padding: 6px 10px; }
.step.active { color: var(--accent); font-weight: 600; }
.step.done { color: var(--accent-2); }
.dot { width: 22px; height: 22px; border-radius: 50%; background: var(--card-2); display: flex; align-items: center; justify-content: center; font-size: 12px; }
.step.active .dot { background: var(--accent); color: #06231b; }
.vocab { grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); }
.vocab-item { background: var(--bg-soft); border-radius: 10px; padding: 12px; }
.lyric-line { padding: 10px 0; border-bottom: 1px dashed var(--line); }
.en { font-size: 16px; }
.zh { font-size: 13px; }
.quiz { gap: 18px; }
.q { background: var(--bg-soft); border-radius: 10px; padding: 14px; }
.q-stem { margin-bottom: 10px; }
.opts { flex-direction: column; align-items: flex-start; gap: 8px; }
.opt { display: flex; gap: 8px; align-items: center; cursor: pointer; font-size: 14px; }
.rewrite-input {
  width: 100%; background: var(--card); border: 1px solid var(--line); color: var(--text);
  border-radius: 10px; padding: 10px 12px; font-size: 14px; resize: vertical; outline: none;
}
.rewrite-input:focus { border-color: var(--accent); }
.oral { margin-top: 4px; }
.oral .btn.done { background: var(--accent-soft); color: var(--accent); border-color: var(--accent); }
.tip { font-size: 12px; margin-top: 8px; }
</style>
