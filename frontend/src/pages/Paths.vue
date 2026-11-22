<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { api } from '../api/client';
import { useUserStore } from '../stores/user';

const router = useRouter();
const user = useUserStore();
const paths = ref<any[]>([]);
const loading = ref(false);
const level = ref('');
const recommended = ref<string[]>([]);
const evaluating = ref(false);
const evalMsg = ref('');

function itemLabel(it: any) {
  return it.type === 'SONG' ? '歌曲' : it.ref.startsWith('art-poem') ? '诗歌' : it.ref.startsWith('art-getty') ? '演讲稿' : '新闻';
}

async function load() {
  loading.value = true;
  try {
    const [list, st, rec] = await Promise.all([
      api.get('/learning-paths'),
      api.get(`/level/status?userId=${user.userId}`),
      api.get(`/paths/recommend?userId=${user.userId}`),
    ]);
    paths.value = list;
    level.value = st.cefr;
    recommended.value = (rec.recommended || []).map((p: any) => p.pathId);
  } finally {
    loading.value = false;
  }
}

async function autoEvaluate() {
  evaluating.value = true;
  evalMsg.value = '';
  try {
    const r = await api.get(`/level/evaluate?userId=${user.userId}`);
    level.value = r.cefr;
    evalMsg.value = r.evaluated ? `已按成绩评定为 ${r.cefr}` : '暂无达标完成记录，沿用当前级别';
    const rec = await api.get(`/paths/recommend?userId=${user.userId}`);
    recommended.value = (rec.recommended || []).map((p: any) => p.pathId);
  } finally {
    evaluating.value = false;
  }
}

function openFirst(p: any) {
  const first = p.items[0];
  if (!first) return;
  if (first.type === 'SONG') {
    const [platform, songId] = first.ref.split(':');
    router.push(`/learn/${platform}/${songId}`);
  } else {
    router.push(`/articles/${first.ref}`);
  }
}

onMounted(load);
</script>

<template>
  <div class="grid">
    <div class="card hero">
      <h1>学习路径</h1>
      <p class="muted">由歌曲、英文新闻、名人演讲稿与诗歌组合而成的成套学习路径，按级别挑选；完成路径内资源、考核达标即可赚得学分，优质路径以学分兑换。</p>
      <div class="level-row">
        <div class="level-box">
          <span class="muted">当前级别</span>
          <strong class="cefr-big">{{ level || '未定级' }}</strong>
        </div>
        <button class="btn" :disabled="evaluating" @click="autoEvaluate">
          {{ evaluating ? '评定中…' : '按成绩自动定级' }}
        </button>
        <RouterLink to="/placement" class="btn ghost">参加入学测试</RouterLink>
      </div>
      <p class="muted" v-if="evalMsg">{{ evalMsg }}</p>
    </div>

    <div class="grid paths">
      <div class="card path-card" v-for="p in paths" :key="p.pathId" :class="{ rec: recommended.includes(p.pathId) }">
        <div class="row spread">
          <h2 class="title">{{ p.title }}</h2>
          <div>
            <span class="rec-tag" v-if="recommended.includes(p.pathId)">为你推荐</span>
            <span class="cefr">{{ p.cefr }} · {{ p.level }}</span>
          </div>
        </div>
        <p class="muted desc">{{ p.description }}</p>
        <div class="items">
          <div class="item" v-for="it in p.items" :key="it.ref">
            <span class="tag">{{ itemLabel(it) }}</span>
            <span class="item-title">{{ it.title }}</span>
            <span class="muted item-cefr">{{ it.cefr }}</span>
          </div>
        </div>
        <div class="row spread foot">
          <span class="price">{{ p.priceCredits }} 学分</span>
          <button class="btn" @click="openFirst(p)">开始学习</button>
        </div>
      </div>
    </div>
    <p class="muted" v-if="!loading && paths.length === 0">暂无路径。</p>
  </div>
</template>

<style scoped>
.hero { background: linear-gradient(135deg, var(--card), var(--card-2)); }
.level-row { display: flex; align-items: center; gap: 16px; margin: 14px 0; flex-wrap: wrap; }
.level-box { display: flex; align-items: baseline; gap: 10px; }
.cefr-big { font-size: 26px; color: var(--accent); }
.paths { grid-template-columns: repeat(auto-fill, minmax(340px, 1fr)); gap: 16px; }
.path-card { display: flex; flex-direction: column; }
.path-card.rec { border-color: var(--accent); box-shadow: 0 0 0 1px var(--accent); }
.title { margin: 0; font-size: 18px; }
.cefr { font-size: 12px; color: var(--accent); margin-left: 8px; }
.rec-tag { background: var(--accent); color: #0b1220; font-size: 12px; padding: 2px 8px; border-radius: 999px; margin-right: 6px; }
.desc { font-size: 13px; margin: 10px 0; }
.items { display: flex; flex-direction: column; gap: 8px; margin-bottom: 14px; }
.item { display: flex; align-items: center; gap: 10px; background: var(--bg-soft); border-radius: 8px; padding: 8px 10px; }
.item-title { flex: 1; font-size: 14px; }
.item-cefr { font-size: 12px; }
.foot { margin-top: auto; }
.price { color: var(--accent); font-weight: 700; }
.tag { background: rgba(52,211,153,.14); color: #34d399; font-size: 12px; padding: 2px 8px; border-radius: 999px; }
</style>
