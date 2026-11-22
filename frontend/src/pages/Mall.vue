<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { api } from '../api/client';
import { useUserStore } from '../stores/user';

const user = useUserStore();
const items = ref<any[]>([]);
const category = ref('全部');
const msg = ref('');
const loading = ref(false);

const categories = ['全部', 'AI 学习能力', '会员权益', '学习资料', '音乐福利'];

const filtered = computed(() =>
  category.value === '全部' ? items.value : items.value.filter((i) => i.category === category.value));

async function load() {
  items.value = await api.get('/points/items');
  await user.fetchAccount();
}

async function redeem(item: any) {
  msg.value = '';
  loading.value = true;
  try {
    const r = await api.post('/points/redeem', {
      userId: user.userId,
      itemId: item.itemId,
      idempotentKey: `redeem:${user.userId}:${item.itemId}:${Date.now()}`,
    });
    msg.value = r.message;
    await user.fetchAccount();
  } catch (e: any) {
    msg.value = e.message;
  } finally {
    loading.value = false;
  }
}

onMounted(load);
</script>

<template>
  <div class="grid">
    <div class="card row spread">
      <h2 style="margin:0">积分兑换商城</h2>
      <div class="row">
        <button
          v-for="c in categories"
          :key="c"
          class="btn ghost cat"
          :class="{ on: category === c }"
          @click="category = c"
        >{{ c }}</button>
      </div>
    </div>

    <div class="muted" v-if="msg" :style="{ color: msg.includes('成功') ? 'var(--accent)' : 'var(--danger)' }">{{ msg }}</div>

    <div class="grid mall">
      <div class="card item" v-for="i in filtered" :key="i.itemId">
        <div class="row spread">
          <strong>{{ i.name }}</strong>
          <span class="tag">{{ i.category }}</span>
        </div>
        <p class="muted desc">{{ i.description }}</p>
        <div class="row spread price">
          <div><strong class="pts">{{ i.costPoints }}</strong> <span class="muted">积分</span></div>
          <div class="muted">参考价 {{ i.priceCny }}</div>
        </div>
        <div class="row" style="margin-top:14px">
          <button class="btn" @click="redeem(i)" :disabled="loading || (user.account?.balance ?? 0) < i.costPoints">
            积分兑换
          </button>
          <button class="btn ghost" @click="msg = '人民币直购将跳转对应权益购买页（演示）'">人民币购买</button>
        </div>
      </div>
    </div>

    <p class="muted footer-note">
      兑换成功的权益即时发放，已消耗积分不予退还；第三方音乐优惠券的使用规则以合作平台为准。
      积分仅作兑换门槛，不直接用于购买版权内容。
    </p>
  </div>
</template>

<style scoped>
.mall { grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); }
.cat { padding: 6px 12px; font-size: 13px; }
.cat.on { background: var(--accent); color: #06231b; }
.desc { margin: 8px 0; min-height: 40px; }
.price .pts { color: var(--accent); font-size: 22px; }
.footer-note { font-size: 12px; text-align: center; }
</style>
