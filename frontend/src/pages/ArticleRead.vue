<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api } from '../api/client';
import { useUserStore } from '../stores/user';

const route = useRoute();
const router = useRouter();
const user = useUserStore();

const article = ref<any>(null);
const sessionId = ref('');
const quiz = ref<any[]>([]);
const answers = ref<Record<string, string>>({});
const started = ref(false);
const submitted = ref(false);
const loading = ref(false);
const error = ref('');

async function load() {
  article.value = await api.get(`/articles/${route.params.articleId}`);
}

async function startStudy() {
  loading.value = true;
  error.value = '';
  try {
    const s = await api.post('/sessions', {
      userId: user.userId,
      platform: 'article',
      songId: article.value.articleId,
      mode: 'FOCUS',
      cefr: article.value.cefr,
    });
    sessionId.value = s.sessionId;
    quiz.value = await api.get(`/practice/${sessionId.value}/quiz`);
    started.value = true;
  } catch (e: any) {
    error.value = e.message;
  } finally {
    loading.value = false;
  }
}

async function submitPractice() {
  loading.value = true;
  error.value = '';
  try {
    const grade = await api.post(`/practice/${sessionId.value}/submit`, { answers: answers.value });
    sessionStorage.setItem('lastGrade', JSON.stringify({
      sessionId: sessionId.value,
      grade,
      cefr: article.value.cefr,
      repeatCount: 0,
    }));
    submitted.value = true;
    router.push(`/settlement/${sessionId.value}`);
  } catch (e: any) {
    error.value = e.message;
  } finally {
    loading.value = false;
  }
}

onMounted(load);
</script>

<template>
  <div class="grid" v-if="article">
    <div class="card row spread">
      <div>
        <h1>{{ article.title }}</h1>
        <div class="muted">{{ article.author }} · {{ article.source }} · {{ article.cefr }} · {{ article.level }}</div>
      </div>
      <span class="tag">{{ article.category === 'SPEECH' ? '演讲稿' : article.category === 'POEM' ? '诗歌' : '新闻' }}</span>
    </div>

    <!-- 阅读态 -->
    <div class="card" v-if="!started">
      <!-- 摘要+外链模式：仅平台导读，不展示/存储第三方原文 -->
      <template v-if="article.accessMode === 'SUMMARY_LINK'">
        <h2>平台导读</h2>
        <div class="paragraph summary-box">{{ article.summary }}</div>
        <div class="notice">
          原文由第三方版权方提供，本站仅提供导读摘要与核心生词，不存储、不转载、不提供全文下载。
        </div>
        <a
          v-if="article.sourceUrl"
          class="btn link-btn"
          :href="article.sourceUrl"
          target="_blank"
          rel="noopener noreferrer"
        >阅读原文（新标签页跳转）↗</a>
      </template>

      <!-- 站内全文模式：原创 / 公有领域 -->
      <template v-else>
        <h2>{{ article.category === 'POEM' ? '诗歌原文' : '正文' }}</h2>
        <div class="paragraph" :class="{ 'poem-line': article.category === 'POEM' }"
             v-for="(p, i) in article.paragraphs" :key="i">{{ p.p }}</div>
      </template>

      <h2 style="margin-top:18px">核心生词</h2>
      <div class="grid vocab">
        <div class="vocab-item" v-for="v in article.vocab" :key="v.word">
          <strong>{{ v.word }}</strong>
          <span class="muted">{{ v.phonetic }}</span>
          <div>{{ v.meaning }}</div>
        </div>
      </div>

      <p class="muted" style="margin-top:14px">读完导读与生词后，完成 4 道小测，经中台校验可获得学习积分。</p>
      <div class="row" style="margin-top:14px">
        <button class="btn" @click="startStudy" :disabled="loading">开始学习并答题</button>
      </div>
      <div class="muted err" v-if="error">{{ error }}</div>
    </div>

    <!-- 答题态 -->
    <div class="card" v-else>
      <h2>阅读理解小测</h2>
      <div class="grid quiz">
        <div class="q" v-for="q in quiz" :key="q.id">
          <div class="q-stem">{{ q.stem }}</div>

          <label v-if="q.type === 'ORAL_RECALL'" class="oral-recall">
            <input
              type="checkbox"
              :checked="!!answers[q.id]"
              @change="answers[q.id] = answers[q.id] ? '' : 'recalled'"
            />
            我已跟读并复述这句
          </label>

          <textarea
            v-else-if="q.type === 'REWRITE'"
            v-model="answers[q.id]"
            class="rewrite-input"
            rows="2"
            placeholder="请用题目提示的短语开放作答…"
          ></textarea>

          <div class="row opts" v-else>
            <label v-for="opt in q.options" :key="opt" class="opt">
              <input type="radio" :name="q.id" :value="opt" v-model="answers[q.id]" />
              {{ opt }}
            </label>
          </div>

          <div class="muted" v-if="q.hint">提示：{{ q.hint }}</div>
        </div>
      </div>
      <div class="row" style="margin-top:16px">
        <button class="btn" @click="submitPractice" :disabled="loading || submitted">提交作答并结算</button>
      </div>
      <div class="muted err" v-if="error">{{ error }}</div>
    </div>
  </div>
</template>

<style scoped>
.paragraph { font-size: 16px; line-height: 1.9; margin-bottom: 12px; }
.poem-line { font-style: italic; padding-left: 8px; border-left: 2px solid var(--accent-soft); }
.oral-recall { display: flex; gap: 8px; align-items: center; cursor: pointer; padding: 10px 12px; background: var(--bg-soft); border-radius: 10px; font-size: 14px; }
.summary-box { background: var(--bg-soft); border-radius: 10px; padding: 14px 16px; }
.notice { font-size: 13px; color: var(--text-dim); background: rgba(255,193,71,.08); border-left: 3px solid #f0b429; border-radius: 6px; padding: 10px 12px; margin: 12px 0; }
.link-btn { text-decoration: none; display: inline-block; }
.vocab { grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); }
.vocab-item { background: var(--bg-soft); border-radius: 10px; padding: 12px; }
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
.err { color: var(--danger); margin-top: 10px; }
</style>
