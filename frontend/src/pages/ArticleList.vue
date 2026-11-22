<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { api } from '../api/client';

const router = useRouter();
const keyword = ref('');
const level = ref('');
const category = ref('');
const articles = ref<any[]>([]);
const loading = ref(false);

const levels = ['', 'A1', 'A2', 'B1', 'B2', 'C1'];
const categories = [
  { v: '', label: '全部' },
  { v: 'NEWS', label: '英文新闻' },
  { v: 'SPEECH', label: '名人演讲稿' },
  { v: 'POEM', label: '诗歌' },
];

async function load() {
  loading.value = true;
  try {
    const q = new URLSearchParams();
    if (keyword.value) q.set('keyword', keyword.value);
    if (level.value) q.set('level', level.value);
    if (category.value) q.set('category', category.value);
    articles.value = await api.get(`/articles?${q.toString()}`);
  } finally {
    loading.value = false;
  }
}

onMounted(load);

function open(a: any) {
  router.push(`/articles/${a.articleId}`);
}
</script>

<template>
  <div class="grid">
    <div class="card hero">
      <h1>英文文章学习</h1>
      <p class="muted">与歌曲同级别的新闻短文与名人演讲稿，阅读 + 生词标注 + 小测，完成后同样获得学习积分。</p>
      <div class="row" style="margin-top:16px; flex-wrap:wrap">
        <input v-model="keyword" placeholder="搜索文章标题" @keyup.enter="load" style="width:220px" />
        <select v-model="level" @change="load">
          <option v-for="l in levels" :key="l" :value="l">{{ l || '全部级别' }}</option>
        </select>
        <select v-model="category" @change="load">
          <option v-for="c in categories" :key="c.v" :value="c.v">{{ c.label }}</option>
        </select>
        <button class="btn" @click="load">搜索</button>
      </div>
    </div>

    <div class="grid arts">
      <div class="card art-card" v-for="a in articles" :key="a.articleId" @click="open(a)">
        <div class="row spread">
          <strong>{{ a.title }}</strong>
          <span class="tag">{{ a.cefr }}</span>
        </div>
        <div class="muted">{{ a.author }} · {{ a.source }} · {{ a.level }}</div>
        <p class="desc">{{ a.summary }}</p>
        <div class="row">
          <span class="tag" :class="{ dim: a.category === 'SPEECH', poem: a.category === 'POEM' }">
            {{ a.category === 'SPEECH' ? '演讲稿' : a.category === 'POEM' ? '诗歌' : '新闻' }}
          </span>
          <span class="tag ext" v-if="a.accessMode === 'SUMMARY_LINK'">摘要+外链</span>
        </div>
      </div>
    </div>
    <p class="muted" v-if="!loading && articles.length === 0">暂无匹配文章。</p>
  </div>
</template>

<style scoped>
.hero { background: linear-gradient(135deg, var(--card), var(--card-2)); }
.arts { grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); }
.art-card { cursor: pointer; transition: transform .15s, border-color .15s; }
.art-card:hover { transform: translateY(-2px); border-color: var(--accent); }
.desc { margin: 10px 0; font-size: 14px; color: var(--text-dim); }
.tag.dim { background: rgba(167,139,250,.12); color: #c4b5fd; }
.tag.poem { background: rgba(96,165,250,.14); color: #93c5fd; }
.tag.ext { background: rgba(240,180,41,.14); color: #f0b429; }
</style>
