<script setup lang="ts">
import { RouterLink, useRoute } from 'vue-router';
import { useUserStore } from './stores/user';
import { onMounted } from 'vue';

const user = useUserStore();
const route = useRoute();

onMounted(async () => {
  await user.fetchUsers();
  await user.fetchAccount();
});

function switchUser(e: Event) {
  const v = (e.target as HTMLSelectElement).value;
  user.switchUser(v);
  user.fetchAccount();
}

const nav = [
  { to: '/', label: '歌曲学习' },
  { to: '/library', label: '资料中心' },
  { to: '/paths', label: '学习路径' },
  { to: '/placement', label: '分级测试' },
  { to: '/articles', label: '文章学习' },
  { to: '/points', label: '积分中心' },
  { to: '/mall', label: '兑换商城' },
  { to: '/admin', label: '管理后台', adminOnly: true },
];

function visibleNav() {
  return nav.filter((n) => !n.adminOnly || user.isAdmin);
}
</script>

<template>
  <div class="layout">
    <header class="topbar">
      <div class="container bar-inner">
        <RouterLink to="/" class="brand">
          <span class="logo">♪</span> LyricLearn
        </RouterLink>
        <nav class="row">
          <RouterLink
            v-for="n in visibleNav()"
            :key="n.to"
            :to="n.to"
            class="nav-link"
            :class="{ active: route.path === n.to }"
          >{{ n.label }}</RouterLink>
        </nav>
        <select class="user-switch" :value="user.userId" @change="switchUser" v-if="user.users.length">
          <option v-for="u in user.users" :key="u.userId" :value="u.userId">
            {{ u.name }}{{ u.role === 'ADMIN' ? ' · 管理员' : ' · 学员' }}
          </option>
        </select>
        <div class="row balance-chip" v-if="user.account">
          <span class="muted">可用积分</span>
          <strong>{{ user.account.balance }}</strong>
        </div>
      </div>
    </header>
    <main class="container">
      <router-view />
    </main>
    <footer class="container muted footer">
      本平台仅提供英语学习辅助标注服务，歌曲音频、原版歌词、音乐版权、售卖及会员服务均由第三方音乐平台提供；
      学习积分（LearnPoint）为封闭学习权益，无现金价值，不可转让、不可提现。
    </footer>
  </div>
</template>

<style scoped>
.layout { min-height: 100%; display: flex; flex-direction: column; }
.topbar { background: rgba(11,18,32,.9); border-bottom: 1px solid var(--line); position: sticky; top: 0; z-index: 10; }
.bar-inner { display: flex; align-items: center; justify-content: space-between; padding: 14px 20px; }
.brand { font-size: 19px; font-weight: 700; color: var(--text); display: flex; align-items: center; gap: 8px; }
.logo { color: var(--accent); font-size: 22px; }
.nav-link { color: var(--text-dim); padding: 6px 12px; border-radius: 8px; font-size: 14px; }
.nav-link:hover { color: var(--text); }
.nav-link.active { color: var(--accent); background: var(--accent-soft); }
.balance-chip { background: var(--card); padding: 6px 14px; border-radius: 999px; gap: 8px; }
.user-switch { background: var(--card); color: var(--text); border: 1px solid var(--line); border-radius: 999px; padding: 6px 12px; font-size: 13px; outline: none; }
.balance-chip strong { color: var(--accent); font-size: 16px; }
.footer { margin-top: auto; padding-top: 24px; padding-bottom: 24px; font-size: 12px; text-align: center; }
main { flex: 1; }
</style>
