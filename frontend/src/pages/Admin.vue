<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { api } from '../api/client';
import { useUserStore } from '../stores/user';

const user = useUserStore();
const paths = ref<any[]>([]);
const resources = ref<any[]>([]);
const editing = ref<any | null>(null);
const blank = () => ({
  pathId: '', title: '', description: '', cefr: 'A1', level: 'L0',
  priceCredits: 0, published: true, sortOrder: 0,
});
const form = ref<any>(blank());
const checked = ref<Set<string>>(new Set());
const error = ref('');

const CEFR = ['A1', 'A2', 'B1', 'B2', 'C1'];
const LEVEL = ['L0', 'L1', 'L2', 'L3', 'L4'];

async function load() {
  [paths.value, resources.value] = await Promise.all([
    api.get('/admin/paths'),
    api.get('/admin/paths/resources'),
  ]);
}

function itemLabel(it: any) {
  return it.type === 'SONG' ? '歌曲' : it.ref.startsWith('art-poem') ? '诗歌' : it.ref.startsWith('art-getty') ? '演讲稿' : '新闻';
}

function newForm() {
  editing.value = null;
  checked.value = new Set();
  form.value = blank();
}

function edit(p: any) {
  editing.value = p;
  form.value = {
    pathId: p.pathId, title: p.title, description: p.description,
    cefr: p.cefr, level: p.level, priceCredits: p.priceCredits,
    published: p.published, sortOrder: 0,
  };
  checked.value = new Set(p.items.map((i: any) => i.ref));
}

function toggleCheck(ref: string) {
  if (checked.value.has(ref)) checked.value.delete(ref);
  else checked.value.add(ref);
}

async function togglePublish(p: any) {
  await api.put(`/admin/paths/${p.pathId}`, { published: !p.published });
  await load();
}

async function remove(p: any) {
  if (!confirm(`删除路径「${p.title}」？`)) return;
  await api.del(`/admin/paths/${p.pathId}`);
  await load();
}

function buildItems() {
  return resources.value
    .filter((r) => checked.value.has(r.ref))
    .map((r) => ({ type: r.type, ref: r.ref, title: r.title, cefr: r.cefr }));
}

async function save() {
  error.value = '';
  if (!form.value.title) { error.value = '请填写标题'; return; }
  const body = { ...form.value, items: buildItems() };
  try {
    if (editing.value) {
      await api.put(`/admin/paths/${editing.value.pathId}`, body);
    } else {
      await api.post('/admin/paths', body);
    }
    newForm();
    await load();
  } catch (e: any) {
    error.value = e.message || '保存失败';
  }
}

const selectedCount = computed(() => checked.value.size);

onMounted(load);
</script>

<template>
  <div class="grid admin">
    <div class="card hero">
      <h1>管理员 · 路径编排</h1>
      <p class="muted">勾选歌曲、新闻、演讲稿、诗歌组合成混合学习路径，设定学分定价与上下架；学员仅能看到已发布路径。</p>
    </div>

    <!-- 路径表格 -->
    <div class="card">
      <h2>全部路径（{{ paths.length }}）</h2>
      <table class="tbl">
        <thead>
          <tr><th>标题</th><th>级别</th><th>学分价</th><th>资源数</th><th>状态</th><th>操作</th></tr>
        </thead>
        <tbody>
          <tr v-for="p in paths" :key="p.pathId">
            <td><strong>{{ p.title }}</strong><div class="muted small">{{ p.pathId }}</div></td>
            <td>{{ p.cefr }} · {{ p.level }}</td>
            <td>{{ p.priceCredits }}</td>
            <td>{{ p.items.length }}</td>
            <td>
              <label class="switch">
                <input type="checkbox" :checked="p.published" @change="togglePublish(p)">
                <span>{{ p.published ? '已上架' : '已下架' }}</span>
              </label>
            </td>
            <td class="ops">
              <button class="btn ghost" @click="edit(p)">编辑</button>
              <button class="btn danger" @click="remove(p)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 新建/编辑表单 -->
    <div class="card">
      <h2>{{ editing ? '编辑路径' : '新建路径' }}</h2>
      <div class="form-grid">
        <label>标题<input v-model="form.title" placeholder="如：启蒙入门包（歌曲+童谣）"></label>
        <label>CEFR<select v-model="form.cefr"><option v-for="c in CEFR" :key="c">{{ c }}</option></select></label>
        <label>级别<select v-model="form.level"><option v-for="l in LEVEL" :key="l">{{ l }}</option></select></label>
        <label>学分定价<input type="number" v-model.number="form.priceCredits" min="0"></label>
        <label>排序<input type="number" v-model.number="form.sortOrder" min="0"></label>
        <label class="chk">上架<input type="checkbox" v-model="form.published"></label>
      </div>
      <label class="block">描述<textarea v-model="form.description" rows="2" placeholder="路径适合人群与学习目标"></textarea></label>

      <h3>选择资源（已选 {{ selectedCount }}）</h3>
      <div class="res-list">
        <label class="res" v-for="r in resources" :key="r.ref">
          <input type="checkbox" :checked="checked.has(r.ref)" @change="toggleCheck(r.ref)">
          <span class="tag">{{ r.type === 'SONG' ? '歌曲' : '文章' }}</span>
          <span class="res-title">{{ r.title }}</span>
          <span class="muted">{{ r.cefr }}</span>
        </label>
      </div>

      <p class="err" v-if="error">{{ error }}</p>
      <div class="row spread">
        <button class="btn ghost" @click="newForm">清空表单</button>
        <button class="btn" @click="save">保存路径</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin { grid-template-columns: 1fr; gap: 16px; }
.hero { background: linear-gradient(135deg, var(--card), var(--card-2)); }
.tbl { width: 100%; border-collapse: collapse; font-size: 14px; }
.tbl th, .tbl td { text-align: left; padding: 10px 8px; border-bottom: 1px solid var(--line); }
.small { font-size: 12px; }
.ops { display: flex; gap: 8px; }
.form-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin: 12px 0; }
.form-grid label, .block { display: flex; flex-direction: column; gap: 6px; font-size: 13px; color: var(--text-dim); }
.form-grid input, .form-grid select, .block textarea {
  background: var(--bg-soft); color: var(--text); border: 1px solid var(--line); border-radius: 8px; padding: 8px 10px; outline: none;
}
.chk { flex-direction: row !important; align-items: center; gap: 8px; }
.block { margin-bottom: 12px; }
.res-list { display: grid; grid-template-columns: repeat(2, 1fr); gap: 8px; margin: 10px 0 16px; max-height: 320px; overflow: auto; }
.res { display: flex; align-items: center; gap: 10px; background: var(--bg-soft); border-radius: 8px; padding: 8px 10px; cursor: pointer; }
.res-title { flex: 1; font-size: 14px; }
.err { color: #f87171; font-size: 14px; }
.danger { background: rgba(248,113,113,.15); color: #f87171; border: 1px solid rgba(248,113,113,.3); }
.tag { background: rgba(52,211,153,.14); color: #34d399; font-size: 12px; padding: 2px 8px; border-radius: 999px; }
</style>
