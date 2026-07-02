<template>
  <div class="page">
    <header class="top-bar"><div class="inner top-row">
      <router-link to="/" class="logo">🛒 CloudMall</router-link>
      <div class="title">⭐ 我的积分 & 会员</div>
      <router-link to="/user/center" class="back">返回</router-link>
    </div></header>
    <div class="container">
      <!-- 积分卡片 -->
      <el-card class="point-card" v-if="point">
        <h2>{{ point.availablePoint || 0 }} <span>可用积分</span></h2>
        <p>累计获得: {{ point.totalPoint || 0 }}</p>
      </el-card>

      <!-- 当前等级 + 进度条 -->
      <el-card class="level-section" v-if="currentLevel">
        <h3>🏅 当前等级: <span style="color:#ff4d4f">{{ currentLevel.levelName }}</span></h3>
        <p style="margin:8px 0">消费折扣: <b>{{ currentLevel.discountRate }}%</b> |
          免邮: <b>{{ currentLevel.freeShipping ? '✅ 包邮' : '❌ 不包邮' }}</b></p>
        <div v-if="nextLevel" style="margin:12px 0">
          <span>距{{ nextLevel.levelName }}还需消费 <b style="color:#ff4d4f">¥{{ fmtGap }}</b></span>
          <el-progress :percentage="progressPercent" :stroke-width="16" :color="progressColor" style="margin-top:8px"/>
        </div>
      </el-card>

      <!-- 会员等级 & 权益 -->
      <el-card style="margin-top:16px">
        <template #header><span>🎖️ 会员等级 & 权益</span></template>
        <el-table :data="levels" :row-class-name="rowClass">
          <el-table-column prop="levelName" label="等级" width="100"/>
          <el-table-column label="升级条件" width="140">
            <template #default="{row}">累计消费满 ¥{{ (row.minAmount/100).toFixed(0) }}</template>
          </el-table-column>
          <el-table-column label="折扣" width="80"><template #default="{row}">{{ row.discountRate }}%</template></el-table-column>
          <el-table-column label="免邮" width="80"><template #default="{row}">{{ row.freeShipping ? '✅' : '❌' }}</template></el-table-column>
          <el-table-column label="专属权益">
            <template #default="{row}">
              <template v-if="row.levelCode===0">基础服务</template>
              <template v-else-if="row.levelCode===1">优先客服 · 积分加速10%</template>
              <template v-else-if="row.levelCode===2">优先客服 · 积分加速20% · 生日礼包 · 极速退款</template>
              <template v-else-if="row.levelCode===3">专属客服 · 积分加速50% · 生日礼包 · 极速退款 · 年度礼盒 · 新品优先购</template>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 积分兑换 -->
      <el-card style="margin-top:16px">
        <template #header><span>🎁 积分兑换</span></template>
        <div style="display:flex;gap:12px;flex-wrap:wrap">
          <el-card v-for="item in exchangeList" :key="item.points" class="exchange-card" shadow="hover">
            <div class="ex-icon">{{ item.icon }}</div>
            <div class="ex-name">{{ item.name }}</div>
            <div class="ex-cost">{{ item.points }} 积分</div>
            <el-button size="small" type="danger" @click="doExchange(item.points)" :disabled="(point?.availablePoint||0)<item.points">兑换</el-button>
          </el-card>
        </div>
      </el-card>

      <!-- 积分明细 -->
      <el-card style="margin-top:16px">
        <template #header><span>📋 积分明细</span></template>
        <el-table :data="logs" v-loading="loadingLogs">
          <el-table-column prop="createTime" label="时间" width="180" />
          <el-table-column prop="description" label="说明" />
          <el-table-column label="积分"><template #default="{row}"><span :style="{color:row.point>0?'#67c23a':'#f56c6c'}">{{row.point>0?'+':''}}{{row.point}}</span></template></el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyPoints, getPointLogs, getLevels } from '@/api/point'
import request from '@/utils/request'

const point = ref<any>(null)
const logs = ref<any[]>([])
const levels = ref<any[]>([])
const loadingLogs = ref(false)

const exchangeList = [
  { icon:'🎫', name:'满200减20优惠券', points:500 },
  { icon:'🎫', name:'满500减50优惠券', points:1000 },
  { icon:'🎁', name:'50元红包', points:2000 },
  { icon:'📦', name:'包邮券', points:300 },
]

const currentLevel = computed(() => {
  if (!point.value || !levels.value.length) return null
  const totalSpent = point.value.totalSpent || 0
  let best = levels.value[0]
  for (const l of levels.value) {
    if (totalSpent >= (l.minAmount || 0)) best = l
  }
  return best
})

const nextLevel = computed(() => {
  if (!currentLevel.value) return null
  const idx = levels.value.findIndex(l => l.id === currentLevel.value.id)
  return idx >= 0 && idx < levels.value.length - 1 ? levels.value[idx + 1] : null
})

const fmtGap = computed(() => {
  if (!nextLevel.value || !currentLevel.value) return '0'
  const totalSpent = point.value?.totalSpent || 0
  return ((nextLevel.value.minAmount - totalSpent) / 100).toFixed(0)
})

const progressPercent = computed(() => {
  if (!nextLevel.value || !currentLevel.value) return 100
  const total = nextLevel.value.minAmount - currentLevel.value.minAmount
  const done = (point.value?.totalSpent || 0) - currentLevel.value.minAmount
  return Math.min(100, Math.round(done * 100 / total))
})

const progressColor = computed(() => {
  if (progressPercent.value > 80) return '#67c23a'
  if (progressPercent.value > 40) return '#e6a23c'
  return '#f56c6c'
})

function rowClass({ row }: any) {
  return row.id === currentLevel.value?.id ? 'current-level-row' : ''
}

async function doExchange(points: number) {
  try {
    await request.post('/point/exchange', null, { params: { pointCost: points } })
    ElMessage.success('兑换成功！')
    const [p, l]: any[] = await Promise.all([getMyPoints(), getPointLogs()])
    point.value = p.data; logs.value = l.data || []
  } catch (e: any) {
    ElMessage.error(e?.message || '兑换失败')
  }
}

async function load() {
  const [p, l, ls]: any[] = await Promise.all([getMyPoints(), getPointLogs(), getLevels()])
  point.value = p.data; logs.value = l.data || []; levels.value = ls.data || []
}
onMounted(load)
</script>
<style scoped>
.page{background:#f0f2f5;min-height:100vh}
.top-bar{background:#fff;box-shadow:0 1px 4px rgba(0,0,0,0.06);position:sticky;top:0;z-index:100}
.inner{max-width:1200px;margin:0 auto;padding:0 20px}
.top-row{display:flex;align-items:center;gap:24px;height:60px}
.logo{font-size:22px;font-weight:700;color:#ff4d4f;text-decoration:none}
.title{font-size:20px;font-weight:700;color:#333;flex:1}
.back{color:#999;text-decoration:none;font-size:14px}
.container{max-width:900px;margin:0 auto;padding:20px}
.point-card{border-radius:12px;text-align:center;background:linear-gradient(135deg,#ff4d4f,#ff7875);color:#fff}
.point-card h2{font-size:36px;margin:0}
.point-card span{font-size:14px;opacity:0.9}
.level-section{border-radius:12px;text-align:center;margin-top:16px}
.exchange-card{border-radius:8px;width:150px;text-align:center}
.exchange-card:hover{transform:translateY(-2px)}
.ex-icon{font-size:32px}
.ex-name{font-size:13px;margin:4px 0}
.ex-cost{font-size:12px;color:#999;margin-bottom:8px}
:deep(.current-level-row){background:#fff1f0 !important;font-weight:700}
</style>
