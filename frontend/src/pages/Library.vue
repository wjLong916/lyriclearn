<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { api } from '../api/client';

const router = useRouter();
const items = ref<any[]>([]);
const loading = ref(false);
const type = ref('');
const level = ref('');
const keyword = ref('');

const types = [
  { v: '', label: '全部类型' },
  { v: 'SONG', label: '歌曲' },
  { v: 'NEWS', label: '英文新闻' },
  { v: 'SPEECH', label: '名人演讲稿' },
  { v: 'POEM', label: '诗歌' },
];
const levels = ['', 'A1', 'A2', 'B1', 'B2', 'C1'];

function kindLabel(it: any) {
  if (it.type === 'SONG') return '歌曲';
  return it.category === 'SPEECH' ? '演讲稿' : it.category === 'POEM' ? '诗歌' : '新闻';
}

function catClass(cat: string) {
  if (cat === 'POEM') return 'poem';
  if (cat === 'SPEECH') return 'dim';
  if (cat === 'SONG') return 'song';
  return '';
}

async function load() {
  loading.value = true;
  try {
    const qA: Record<string, string> = {};
    if (level.value) qA.level = level.value;
    if (type.value && type.value !== 'SONG') qA.category = type.value;
    const [songs, articles] = await Promise.all([
      api.get(`/songs?${new URLSearchParams(level.value ? { level: level.value } : {}).toString()}`),
      api.get(`/articles?${new URLSearchParams(qA).toString()}`),
    ]);
    const songRows = (songs as any[]).map((s) => ({
      type: 'SONG',
      ref: `${s.platform}:${s.songId}`,
      title: s.title,
      subtitle: `${s.artist} · ${s.level}`,
      cefr: s.cefr,
      level: s.level,
      category: 'SONG',
      learnable: s.learnable,
    }));
    const articleRows = (articles as any[]).map((a) => ({
      type: 'ARTICLE',
      ref: a.articleId,
      title: a.title,
      subtitle: `${a.author} · ${a.source}`,
      cefr: a.cefr,
      level: a.level,
      category: a.category,
      accessMode: a.accessMode,
    }));
    let merged = [...songRows, ...articleRows];
    if (type.value === 'SONG') merged = merged.filter((i) => i.type === 'SONG');
    else if (type.value) merged = merged.filter((i) => i.category === type.value);
    if (keyword.value) {
      const k = keyword.value.toLowerCase();
      merged = merged.filter((i) => i.title.toLowerCase().includes(k));
    }
    items.value = merged;
  } finally {
    loading.value = false;
  }
}

function open(it: any) {
  if (it.type === 'SONG') {
    const [platform, songId] = it.ref.split(':');
    router.push(`/learn/${platform}/${songId}`);
  } else {
    router.push(`/articles/${it.ref}`);
  }
}

onMounted(load);
</script>

<template>
  <div class="grid">
    <div class="card hero">
      <h1>学习资料中心</h1>
      <p class="muted">歌曲、英文新闻、名人演讲稿与公有领域诗歌统一在此挑选，按级别与类型筛选，点开即学。学习考核赚取学分，优质资源与路径可用学分兑换。</p>
      <div class="row" style="margin-top:16px; flex-wrap:wrap">
        <input v-model="keyword" placeholder="搜索标题" @keyup.enter="load" style="width:200px" />
        <select v-model="type" @change="load">
          <option v-for="t in types" :key="t.v" :value="t.v">{{ t.label }}</option>
        </select>
        <select v-model="level" @change="load">
          <option v-for="l in levels" :key="l" :value="l">{{ l || '全部级别' }}</option>
        </select>
        <button class="btn" @click="load">搜索</button>
      </div>
    </div>

    <div class="grid lib">
      <div class="card lib-card" v-for="it in items" :key="it.type + it.ref" @click="open(it)">
        <div class="row spread">
          <strong>{{ it.title }}</strong>
          <span class="cefr">{{ it.cefr }}</span>
        </div>
        <div class="muted small">{{ it.subtitle }}</div>
        <div class="row" style="margin-top:10px">
          <span class="tag" :class="catClass(it.category)">{{ kindLabel(it) }}</span>
          <span class="tag dim" v-if="it.accessMode === 'SUMMARY_LINK'">摘要+外链</span>
        </div>
      </div>
    </div>
    <p class="muted" v-if="!loading && items.length === 0">暂无匹配资料。</p>
  </div>
</template>

<style scoped>
.hero { background: linear-gradient(135deg, var(--card), var(--card-2)); }
.lib { grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); }
.lib-card { cursor: pointer; transition: transform .15s, border-color .15s; }
.lib-card:hover { transform: translateY(-2px); border-color: var(--accent); }
.cefr { font-size: 12px; color: var(--accent); }
.small { font-size: 12px; margin-top: 4px; }
.tag.dim { background: rgba(167,139,250,.12); color: #c4b5fd; }
.tag.poem { background: rgba(96,165,250,.14); color: #93c5fd; }
.tag.song { background: rgba(52,211,153,.14); color: #34d399; }
</style>
