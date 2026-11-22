<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { api } from '../api/client';
import { useUserStore } from '../stores/user';

const user = useUserStore();
const router = useRouter();
const quiz = ref<any[]>([]);
const answers = ref<Record<string, string>>({});
const result = ref<any | null>(null);
const submitting = ref(false);

onMounted(async () => {
  quiz.value = await api.get('/placement/quiz');
});

function choose(qid: string, opt: string) {
  answers.value[qid] = opt;
}

async function submit() {
  if (Object.keys(answers.value).length < quiz.value.length) return;
  submitting.value = true;
  try {
    result.value = await api.post('/placement/submit', {
      userId: user.userId,
      answers: answers.value,
    });
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <div class="grid">
    <div class="card hero">
      <h1>入学分级测试</h1>
      <p class="muted">按 A1 → C1 逐级考核，每级 2 题、全部答对即晋级；定级结果用于为你推荐匹配的学习路径。</p>
    </div>

    <div class="card" v-if="!result">
      <div class="q" v-for="(q, i) in quiz" :key="q.id">
        <div class="q-stem"><span class="q-no">{{ i + 1 }}.</span> {{ q.stem }} <span class="muted">({{ q.cefr }})</span></div>
        <div class="opts">
          <label v-for="opt in q.options" :key="opt" class="opt" :class="{ on: answers[q.id] === opt }">
            <input type="radio" :name="q.id" :checked="answers[q.id] === opt" @change="choose(q.id, opt)">
            {{ opt }}
          </label>
        </div>
      </div>
      <div class="row spread">
        <span class="muted">已答 {{ Object.keys(answers).length }} / {{ quiz.length }}</span>
        <button class="btn" :disabled="submitting || Object.keys(answers).length < quiz.length" @click="submit">
          {{ submitting ? '判分中…' : '提交判分' }}
        </button>
      </div>
    </div>

    <div class="card result" v-else>
      <h2>定级结果：{{ result.cefr }}</h2>
      <p class="muted">答对 {{ result.correct }} / {{ result.total }} 题，来源：入学测试。</p>
      <div class="row">
        <button class="btn" @click="router.push('/paths')">查看推荐路径</button>
        <button class="btn ghost" @click="result = null; answers = {}">重新测试</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.hero { background: linear-gradient(135deg, var(--card), var(--card-2)); }
.q { padding: 16px 0; border-bottom: 1px dashed var(--line); }
.q-stem { font-size: 16px; margin-bottom: 10px; }
.q-no { color: var(--accent); font-weight: 700; }
.opts { display: flex; flex-direction: column; gap: 8px; }
.opt { display: flex; align-items: center; gap: 10px; background: var(--bg-soft); border-radius: 8px; padding: 10px 12px; cursor: pointer; font-size: 14px; }
.opt.on { background: var(--accent-soft); color: var(--accent); }
.result { text-align: center; padding: 40px; }
.result h2 { font-size: 32px; color: var(--accent); margin: 0 0 8px; }
</style>
