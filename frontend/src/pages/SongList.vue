<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { api } from '../api/client';

const router = useRouter();
const keyword = ref('');
const level = ref('');
const songs = ref<any[]>([]);
const loading = ref(false);

async function search() {
  loading.value = true;
  try {
    const q = new URLSearchParams();
    if (keyword.value) q.set('keyword', keyword.value);
    if (level.value) q.set('level', level.value);
    songs.value = await api.get(`/songs?${q.toString()}`);
  } finally {
    loading.value = false;
  }
}

onMounted(search);

function open(item: any) {
  router.push(`/learn/${item.platform}/${item.songId}`);
}
</script>

<template>
  <div class="grid">
    <div class="card hero">
      <h1>用英文歌曲，把听歌变成可量化的学习</h1>
      <p class="muted">逐词音标 · 逐句释义 · 乐谱双视图 · 刻意练习闭环。选择一首生词率适中的歌曲开始 i+1 学习。</p>
      <div class="row" style="margin-top:16px">
        <input v-model="keyword" placeholder="搜索歌名或歌手" @keyup.enter="search" style="width:240px" />
        <select v-model="level" @change="search">
          <option value="">全部难度</option>
          <option value="A1">A1 启蒙</option>
          <option value="A2">A2 初级</option>
          <option value="B1">B1 中级</option>
          <option value="B2">B2 高级</option>
        </select>
        <button class="btn" @click="search">搜索</button>
      </div>
    </div>

    <div class="grid songs">
      <div class="card song-card" v-for="s in songs" :key="s.songId" @click="open(s)">
        <div class="cover">{{ s.title.slice(0, 1) }}</div>
        <div class="info">
          <div class="row spread">
            <strong>{{ s.title }}</strong>
            <span class="tag" :class="{ dim: !s.learnable }">{{ s.cefr }}</span>
          </div>
          <div class="muted">{{ s.artist }} · {{ s.level }}</div>
          <div class="row" style="margin-top:8px">
            <span class="tag" v-if="s.learnable">可刻意学习</span>
            <span class="tag dim" v-else>仅可跳转收听</span>
          </div>
        </div>
      </div>
    </div>
    <p class="muted" v-if="!loading && songs.length === 0">暂无匹配歌曲。</p>
  </div>
</template>

<style scoped>
.hero { background: linear-gradient(135deg, var(--card), var(--card-2)); }
.songs { grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); }
.song-card { display: flex; gap: 14px; cursor: pointer; transition: transform .15s, border-color .15s; }
.song-card:hover { transform: translateY(-2px); border-color: var(--accent); }
.cover {
  width: 64px; height: 64px; border-radius: 12px; flex-shrink: 0;
  background: var(--accent-soft); color: var(--accent);
  display: flex; align-items: center; justify-content: center; font-size: 26px; font-weight: 700;
}
.info { flex: 1; }
</style>
