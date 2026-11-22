<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { api } from '../api/client';
import { useUserStore } from '../stores/user';

const user = useUserStore();
const ledger = ref<any[]>([]);
const page = ref(0);
const total = ref(0);
const packages = ref<any[]>([]);
const selected = ref<any | null>(null);
const paying = ref(false);
const notice = ref('');

async function load() {
  const [acc, led, pkgs] = await Promise.all([
    api.get(`/points/account?userId=${user.userId}`),
    api.get(`/points/ledger?userId=${user.userId}&page=${page.value}&size=20`),
    api.get('/points/packages'),
  ]);
  user.account = acc;
  ledger.value = led.rows;
  total.value = led.total;
  packages.value = pkgs;
}

onMounted(load);

function bizLabel(t: string) {
  return t === 'GRANT' ? '学习发放' : t === 'REDEEM' ? '兑换消耗' : t === 'TOPUP' ? '演示充值' : t;
}

function openPay(p: any) {
  selected.value = p;
  notice.value = '';
}

async function confirmPay() {
  if (!selected.value) return;
  paying.value = true;
  try {
    await api.post('/points/top-up', {
      userId: user.userId,
      packageId: selected.value.packageId,
      idempotentKey: `topup-${selected.value.packageId}-${Date.now()}`,
    });
    notice.value = `已到账 ${selected.value.credits} 学分（mock 演示，未发生真实扣款）`;
    selected.value = null;
    await load();
  } catch (e: any) {
    notice.value = e.message || '充值失败';
  } finally {
    paying.value = false;
  }
}
</script>

<template>
  <div class="grid pc">
    <!-- 余额卡 -->
    <div class="card balance">
      <div class="muted">可用学习积分</div>
      <div class="big">{{ user.account?.balance ?? 0 }}</div>
      <div class="muted">有效期至 {{ user.account?.validUntil || '-' }} · 累计获得 {{ user.account?.totalEarned ?? 0 }} · 累计消耗 {{ user.account?.totalSpent ?? 0 }}</div>
      <div class="progress">
        <div class="bar" :style="{ width: Math.min(100, ((user.account?.todayEarned ?? 0) / (user.account?.dailyCap ?? 1)) * 100) + '%' }"></div>
      </div>
      <div class="muted">今日已获取 {{ user.account?.todayEarned ?? 0 }} / {{ user.account?.dailyCap ?? 30 }}</div>
      <p class="compliance">LearnPoint 为平台内封闭学习权益：无现金价值，不可提现、不可转让。以下充值仅为 mock 演示，不发生真实扣款、不构成兑付。</p>
    </div>

    <!-- mock 充值档位 -->
    <div class="card">
      <h2>学分充值（mock 演示）</h2>
      <p class="muted">演示档位仅用于体验购买到账流程，不接入真实支付。</p>
      <div class="pkgs">
        <div class="pkg" v-for="p in packages" :key="p.packageId">
          <div class="pkg-credits">{{ p.credits }} 学分</div>
          <div class="muted">{{ p.priceLabel }}</div>
          <button class="btn" @click="openPay(p)">购买</button>
        </div>
      </div>
      <p class="ok" v-if="notice">{{ notice }}</p>
    </div>

    <!-- 账本 -->
    <div class="card">
      <h2>积分账本</h2>
      <div class="tx" v-for="t in ledger" :key="t.txNo">
        <div>
          <strong>{{ bizLabel(t.bizType) }}</strong>
          <div class="muted">{{ t.createdAt }} · {{ t.sourceBiz }}</div>
          <div class="muted break">哈希 {{ t.evidenceHash }}</div>
        </div>
        <div class="amount" :class="{ out: t.direction === 'DEBIT' }">
          {{ t.direction === 'CREDIT' ? '+' : '-' }}{{ t.amount }}
        </div>
      </div>
      <p class="muted" v-if="ledger.length === 0">暂无收支记录，完成一次刻意学习即可获得积分。</p>
    </div>

    <!-- 辅助卡 -->
    <div class="grid side">
      <div class="card">
        <h2>获取途径</h2>
        <ul class="tips">
          <li>歌曲完整刻意学习：8–14 分（质量≥0.4）</li>
          <li>间隔复习任务达标：3 分</li>
          <li>阶段测评通关：20 分</li>
          <li>连续学习成就：10 分</li>
          <li>mock 演示充值：档位到账（不扣款）</li>
        </ul>
      </div>
      <div class="card">
        <h2>结算规则</h2>
        <ul class="tips">
          <li>本次 = 基础 × CEFR × 质量 × 重复衰减</li>
          <li>质量 &lt; 0.4 不发放；重复刷同一首衰减 0.3</li>
          <li>单日额度硬截断，超出部分不发放；充值不受单日上限约束</li>
        </ul>
      </div>
      <div class="card">
        <h2>合规提示</h2>
        <p class="muted">积分不售卖、不提现、不转账；人民币参考价仅作价值对标，不构成兑付承诺。SBT 与链上哈希不构成数字资产。</p>
      </div>
    </div>
  </div>

  <!-- 确认弹窗 -->
  <div class="modal-mask" v-if="selected" @click.self="selected = null">
    <div class="modal">
      <h3>确认购买（mock 演示）</h3>
      <p>档位：{{ selected.credits }} 学分 · {{ selected.priceLabel }}</p>
      <p class="muted">本流程为演示，不发生真实扣款。</p>
      <div class="row spread">
        <button class="btn ghost" @click="selected = null">取消</button>
        <button class="btn" :disabled="paying" @click="confirmPay">{{ paying ? '处理中…' : '确认到账' }}</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.pc { grid-template-columns: 1.4fr 1fr; }
.side { grid-template-columns: 1fr; }
.balance .big { font-size: 46px; font-weight: 800; color: var(--accent); margin: 6px 0; }
.progress { height: 8px; background: var(--bg-soft); border-radius: 999px; margin: 14px 0 6px; overflow: hidden; }
.bar { height: 100%; background: var(--accent-2); border-radius: 999px; transition: width .3s; }
.compliance { margin-top: 14px; padding: 10px 12px; background: var(--accent-soft); border-radius: 10px; font-size: 13px; color: var(--accent); }
.pkgs { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin-top: 12px; }
.pkg { background: var(--bg-soft); border-radius: 12px; padding: 14px; text-align: center; display: flex; flex-direction: column; gap: 6px; align-items: center; }
.pkg-credits { font-size: 22px; font-weight: 800; color: var(--accent); }
.ok { margin-top: 12px; color: #34d399; font-size: 14px; }
.tx { display: flex; justify-content: space-between; padding: 12px 0; border-bottom: 1px dashed var(--line); }
.amount { font-weight: 700; color: var(--accent); }
.amount.out { color: var(--warn); }
.break { word-break: break-all; font-size: 12px; }
.tips { padding-left: 18px; color: var(--text-dim); font-size: 14px; display: grid; gap: 8px; }
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,.5); display: flex; align-items: center; justify-content: center; z-index: 50; }
.modal { background: var(--card); padding: 24px; border-radius: 14px; width: 340px; display: grid; gap: 12px; }
.ghost { background: transparent; border: 1px solid var(--line); color: var(--text-dim); }
@media (max-width: 860px) { .pc { grid-template-columns: 1fr; } }
</style>
