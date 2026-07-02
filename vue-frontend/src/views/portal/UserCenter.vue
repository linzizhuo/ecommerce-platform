<template>
  <div class="page">
    <header class="top-bar"><div class="top-inner"><router-link to="/" class="logo">CloudMall</router-link></div></header>
    <div class="content-inner">
      <h2>个人中心</h2>
      <el-card style="margin-bottom:20px">
        <p>👤 {{ userStore.user?.nickname }} | 📱 {{ userStore.user?.phone }}</p>
        <p v-if="levelInfo" style="margin-top:8px">
          🏅 会员等级: <el-tag type="danger" size="large">{{ levelInfo.levelName }}</el-tag>
          消费折扣 {{ levelInfo.discountRate }}%
          {{ levelInfo.freeShipping ? ' · 包邮' : '' }}
        </p>
      </el-card>

      <!-- 快捷入口 -->
      <div style="display:flex;gap:12px;margin-bottom:20px;flex-wrap:wrap">
        <el-button @click="$router.push('/orders')">📋 我的订单</el-button>
        <el-button @click="$router.push('/favorites')">❤️ 我的收藏</el-button>
        <el-button @click="$router.push('/points')">⭐ 积分中心</el-button>
        <el-button @click="$router.push('/messages')">🔔 消息中心</el-button>
        <el-button @click="$router.push('/aftersale')">🔄 售后申请</el-button>
        <el-button @click="$router.push('/seckill')" type="danger">⚡ 秒杀专区</el-button>
        <el-button @click="$router.push('/presale')">📦 预售活动</el-button>
        <el-button @click="$router.push('/distribution')">💰 分销中心</el-button>
        <el-button @click="$router.push('/combo')">🎁 优惠套餐</el-button>
        <el-button @click="$router.push('/red-envelope')">🧧 红包</el-button>
      </div>

      <!-- 我的优惠券 -->
      <el-card style="margin-bottom:20px">
        <template #header><span>🎫 我的优惠券 ({{ myCoupons.length }})</span>
          <el-button size="small" type="danger" style="float:right" @click="openReceive">领券中心</el-button>
        </template>
        <div v-if="myCoupons.length" style="display:flex;gap:12px;flex-wrap:wrap">
          <div v-for="c in myCoupons" :key="c.id" class="coupon-card" :class="{used:c.status!==0}">
            <div class="coupon-value">¥{{ (c.value/100).toFixed(0) }}</div>
            <div class="coupon-info">
              <div>{{ c.name }}</div>
              <small>满¥{{ (c.threshold/100).toFixed(0) }}可用</small>
              <small v-if="c.status!==0" style="color:#999">已使用</small>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无优惠券，去领券中心看看吧" :image-size="60" />
      </el-card>

      <!-- 收货地址 -->
      <el-card>
        <template #header>📍 收货地址 <el-button size="small" @click="addrVisible=true" style="float:right">+ 新增</el-button></template>
        <el-table :data="addresses">
          <el-table-column prop="receiver" label="收货人" /><el-table-column prop="phone" label="电话" />
          <el-table-column label="地址"><template #default="{row}">{{ row.province }}{{ row.city }}{{ row.district }} {{ row.detail }}</template></el-table-column>
        </el-table>
      </el-card>

      <!-- 领券中心弹窗 -->
      <el-dialog v-model="showReceive" title="🎫 领券中心" width="500px">
        <div v-if="availCoupons.length">
          <div v-for="c in availCoupons" :key="c.id" class="coupon-card receive" @click="receiveCoupon(c.id)">
            <div class="coupon-value" style="color:#ff4d4f">¥{{ (c.value/100).toFixed(0) }}</div>
            <div class="coupon-info">
              <div>{{ c.name }}</div>
              <small>满¥{{ (c.threshold/100).toFixed(0) }}可用 | 剩余{{ c.totalCount - (c.receivedCount||0) }}张</small>
              <small style="color:#ff4d4f">点击领取</small>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无可领优惠券" :image-size="80" />
      </el-dialog>

      <!-- 新增地址弹窗 -->
      <el-dialog v-model="addrVisible" title="新增地址" width="450px">
        <el-form><el-form-item label="收货人"><el-input v-model="addr.receiver" /></el-form-item>
          <el-form-item label="电话"><el-input v-model="addr.phone" /></el-form-item>
          <el-form-item label="省"><el-input v-model="addr.province" /></el-form-item>
          <el-form-item label="市"><el-input v-model="addr.city" /></el-form-item>
          <el-form-item label="区"><el-input v-model="addr.district" /></el-form-item>
          <el-form-item label="详细"><el-input v-model="addr.detail" /></el-form-item>
        </el-form>
        <template #footer><el-button @click="addrVisible=false">取消</el-button><el-button type="primary" @click="saveAddr">保存</el-button></template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'; import { ElMessage } from 'element-plus'; import { useUserStore } from '@/stores/userStore'; import request from '@/utils/request'; import { getMyPoints, getLevels } from '@/api/point'
const userStore = useUserStore()
const addresses = ref<any[]>([]); const addrVisible = ref(false)
const myCoupons = ref<any[]>([]); const availCoupons = ref<any[]>([]); const showReceive = ref(false)
const addr = reactive({ receiver:'', phone:'', province:'', city:'', district:'', detail:'' })
const levels = ref<any[]>([]); const myPoint = ref<any>({})

const levelInfo = computed(() => {
  if (!myPoint.value || !levels.value.length) return null
  const totalSpent = myPoint.value.totalSpent || 0
  let best = levels.value[0]
  for (const l of levels.value) { if (totalSpent >= (l.minAmount || 0)) best = l }
  return best
})

async function loadAddr() { const r:any = await request.get('/address'); addresses.value=r.data||[] }
async function saveAddr() { await request.post('/address', addr); ElMessage.success('已保存'); addrVisible.value=false; loadAddr() }
async function loadMyCoupons() { try { const r:any = await request.get('/coupon/my'); myCoupons.value=r.data||[] } catch{} }
async function loadAvail() { try { const r:any = await request.get('/coupon/available'); availCoupons.value=r.data||[] } catch{} }
function openReceive() { showReceive.value = true; loadAvail() }
async function receiveCoupon(id:number) {
  await request.post(`/coupon/receive/${id}`); ElMessage.success('领取成功'); loadMyCoupons(); loadAvail()
}
async function loadLevelInfo() {
  try {
    const [pRes, lRes] = await Promise.all([getMyPoints(), getLevels()])
    myPoint.value = pRes.data || {}; levels.value = lRes.data || []
  } catch {}
}
onMounted(() => { userStore.fetchUser(); loadAddr(); loadMyCoupons(); loadLevelInfo() })
</script>

<style scoped>
.page{min-height:100vh;background:#f0f2f5}.top-bar{background:#fff;box-shadow:0 1px 4px rgba(0,0,0,.06)}.top-inner{display:flex;align-items:center;max-width:900px;margin:0 auto;padding:12px 20px}.content-inner{max-width:900px;margin:0 auto;padding:20px}.logo{font-size:22px;font-weight:700;color:#ff4d4f;text-decoration:none}
.coupon-card{display:flex;border:1px solid #f0f0f0;border-radius:8px;overflow:hidden;width:220px;cursor:pointer;transition:all .2s}.coupon-card:hover{transform:translateY(-2px);box-shadow:0 4px 12px rgba(0,0,0,.1)}.coupon-card.used{opacity:.5}.coupon-card.receive{border-color:#ff4d4f}.coupon-value{width:60px;background:#fff1f0;display:flex;align-items:center;justify-content:center;font-size:20px;font-weight:700}.coupon-info{flex:1;padding:10px;font-size:13px;line-height:1.6}
</style>
