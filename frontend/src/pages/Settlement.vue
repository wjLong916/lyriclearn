<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { api } from '../api/client';
import { useUserStore } from '../stores/user';

const route = useRoute();
const router = useRouter();
const user = useUserStore();

const grade = ref<any>(null);
const ctx = ref<any>({});
const grantResult = ref<any>(null);
const error = ref('');
const minted = ref<any>(null);
const loading = ref(false);

async function doGrant() {
  loading.value = true;
  error.value = '';
  try {
    const a = grade.value.assertion;
    grantResult.value = await api.post('/points/grant', {
      userId: user.userId,
      sessionId: route.params.sessionId,
      cefr: ctx.value.cefr || 'A2',
      repeatCount: ctx.value.repeatCount || 0,
      assertionId: a.assertionId,
      songId: a.songId,
      snapshotVersion: a.snapshotVersion,
      qualityScore: a.qualityScore,
      masteredWords: a.masteredWords,
      signedAt: a.signedAt,
      signature: a.signature,
      idempotentKey: `grant:${user.userId}:${route.params.sessionId}`,
    });
    await user.fetchAccount();
  } catch (e: any) {
    error.value = e.message || '积分发放失败';
  } finally {
    loading.value = false;
  }
}

async function mintSbt() {
  try {
    const a = grade.value.assertion;
    minted.value = await api.post('/evidence/mint', {
      userId: user.userId,
      claimHash: a.assertionId,
    });
  } catch (e: any) {
    error.value = e.message;
  }
}

onMounted(() => {
  const raw = sessionStorage.getItem('lastGrade');
  if (!raw) {
    error.value = '未找到本次学习记录，请先完成一首歌曲的刻意学习。';
    return;
  }
  const stored = JSON.parse(raw);
  grade.value = stored.grade;
  ctx.value = stored;
  doGrant();
});
</script>

<template>
  <div class="grid settle" v-if="grade">
    <!-- 左：学习质量 -->
    <div class="card">
      <h2>学习质量</h2>
      <div class="score-big">{{ (grade.qualityScore * 100).toFixed(0) }}<span>分</span></div>
      <div class="muted">答对 {{ grade.correctCount }} / {{ grade.total }} · 正确率 {{ (grade.accuracy * 100).toFixed(0) }}%</div>
      <p style="margin-top:10px">{{ grade.feedback }}</p>
    </div>

    <!-- 中：积分结算明细 -->
    <div class="card">
      <h2>积分计算与发放</h2>
      <div v-if="error && !grantResult" class="err">{{ error }}</div>
      <template v-else-if="grantResult">
        <div class="calc-row"><span>基础奖励</span><strong>{{ grantResult.base }} 分</strong></div>
        <div class="calc-row"><span>CEFR 难度系数</span><strong>× {{ grantResult.cefrCoeff }}</strong></div>
        <div class="calc-row"><span>学习质量系数</span><strong>× {{ grantResult.qualityCoeff }}</strong></div>
        <div class="calc-row"><span>重复学习衰减</span><strong>× {{ grantResult.repeatCoeff }}</strong></div>
        <div class="calc-row total">
          <span>本次发放</span><strong>{{ grantResult.granted }} 积分</strong>
        </div>
        <div class="muted" v-if="grantResult.capped">已达单日上限，部分发放。</div>
        <div class="muted">发放后余额：{{ grantResult.balance }} · 流水号 {{ grantResult.txNo }}</div>
        <div class="muted break">存证哈希：{{ grantResult.evidenceHash }}</div>
      </template>
      <div v-else class="muted">正在校验断言并结算…</div>
    </div>

    <!-- 右：SBT -->
    <div class="card">
      <h2>SBT 学习凭证</h2>
      <p class="muted">凭证与账户绑定，不可转让、不可拆分、不可交易；链上仅记录断言摘要。</p>
      <div v-if="minted" class="mint-ok">
        已铸造：{{ minted.credentialId }}
        <div class="muted break">{{ minted.txHash }}</div>
      </div>
      <button v-else class="btn ghost" style="margin-top:12px" @click="mintSbt">铸造 SBT 凭证（20 积分）</button>
      <div class="row" style="margin-top:18px">
        <button class="btn" @click="router.push('/')">继续学习下一首</button>
        <button class="btn ghost" @click="router.push('/points')">查看积分中心</button>
      </div>
    </div>
  </div>

  <div class="card" v-else>
    <p>{{ error || '加载中…' }}</p>
    <button class="btn" style="margin-top:12px" @click="router.push('/')">返回歌曲列表</button>
  </div>
</template>

<style scoped>
.settle { grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); }
.score-big { font-size: 44px; font-weight: 800; color: var(--accent); }
.score-big span { font-size: 16px; color: var(--text-dim); font-weight: 400; }
.calc-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px dashed var(--line); }
.calc-row.total { color: var(--accent); font-size: 18px; border-bottom: none; }
.break { word-break: break-all; margin-top: 6px; }
.err { color: var(--danger); }
.mint-ok { margin-top: 12px; color: var(--accent); }
</style>
