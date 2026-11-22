<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { api } from '../api/client';
import { useUserStore } from '../stores/user';

const router = useRouter();
const user = useUserStore();

const stages = ref<any[]>([]);
const passScore = ref(0.7);
const loading = ref(false);
const error = ref('');

const LEVEL_LABEL: Record<string, string> = {
  L0: '入门 A1', L1: '初级 A2', L2: '中级 B1', L3: '中高 B2', L4: '高级 C1',
};

async function load() {
  loading.value = true;
  error.value = '';
  try {
    const data = await api.get(`/path?userId=${user.userId}`);
    stages.value = data.stages;
    passScore.value = data.passScore;
  } catch (e: any) {
    error.value = e.message;
  } finally {
    loading.value = false;
  }
}

function open(item: any) {
  if (item.status !== 'available') return;
  router.push(`/articles/${item.articleId}`);
}

onMounted(load);
</script>

<template>
  <div class="grid">
    <div class="card hero">
      <h1>学习路径</h1>
      <p class="muted">L0 → L4 线性进阶：一级篇目全部达标（质量分 ≥ {{ passScore }}）后解锁下一级。先从公有领域诗歌短章开始。</p>
    </div>

    <div class="chain" v-if="!loading">
      <div class="card stage" v-for="stage in stages" :key="stage.level"
           :class="{ locked: !stage.unlocked }">
        <div class="row spread head">
          <div>
            <h2>{{ stage.level }} · {{ LEVEL_LABEL[stage.level] || stage.level }}</h2>
            <div class="muted">
              {{ stage.items.filter((i: any) => i.status === 'done').length }} / {{ stage.items.length }} 篇达标
            </div>
          </div>
          <span class="tag" :class="stage.unlocked ? 'on' : 'off'">
            {{ stage.unlocked ? (stage.allPassed ? '已完成' : '进行中') : '未解锁' }}
          </span>
        </div>

        <div class="grid items">
          <div v-for="item in stage.items" :key="item.articleId"
               class="item" :class="item.status" @click="open(item)">
            <div class="row spread">
              <strong class="title">{{ item.title }}</strong>
              <span class="cefr">{{ item.cefr }}</span>
            </div>
            <div class="muted small">
              {{ item.category === 'POEM' ? '诗歌' : item.category === 'NOVEL' ? '小说' : '文章' }}
              · 最高 {{ item.bestScore }}
            </div>
            <div class="state" v-if="item.status === 'done'">✓ 已达标</div>
            <div class="state" v-else-if="item.status === 'available'">开始学习 →</div>
            <div class="state" v-else>🔒 未解锁</div>
          </div>
          <div class="item empty muted" v-if="stage.items.length === 0">本级暂无篇目。</div>
        </div>
      </div>
    </div>

    <p class="muted err" v-if="error">{{ error }}</p>
  </div>
</template>

<style scoped>
.hero { background: linear-gradient(135deg, var(--card), var(--card-2)); }
.chain { display: flex; flex-direction: column; gap: 16px; }
.stage.locked { opacity: .55; }
.head { margin-bottom: 12px; }
.head h2 { margin: 0; font-size: 18px; }
.tag.on { background: rgba(52,211,153,.15); color: #34d399; }
.tag.off { background: rgba(148,163,184,.15); color: #94a3b8; }
.items { grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 12px; }
.item { background: var(--bg-soft); border-radius: 10px; padding: 14px; cursor: pointer; transition: transform .15s, border-color .15s; border: 1px solid transparent; }
.item.available:hover { transform: translateY(-2px); border-color: var(--accent); }
.item.done { border-color: rgba(52,211,153,.4); }
.item.locked { cursor: not-allowed; }
.item.empty { cursor: default; }
.title { font-size: 15px; }
.cefr { font-size: 12px; color: var(--accent); }
.small { font-size: 12px; margin-top: 4px; }
.state { margin-top: 10px; font-size: 13px; }
.item.done .state { color: #34d399; }
.item.available .state { color: var(--accent); }
.err { color: var(--danger); }
</style>
