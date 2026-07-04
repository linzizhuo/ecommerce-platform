<template>
  <div class="page">
    <header class="top-bar"><div class="inner top-row">
      <router-link to="/" class="logo">🛒 CloudMall</router-link>
      <div class="title">🧧 红包</div>
      <router-link to="/" class="back">返回首页</router-link>
    </div></header>
    <div class="container">
      <el-tabs v-model="tab" style="margin-top:20px">
        <el-tab-pane label="收到的红包" name="received">
          <el-table :data="received" stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column label="金额" width="120">
              <template #default="{ row }">¥{{ (row.amount / 100).toFixed(2) }}</template>
            </el-table-column>
            <el-table-column prop="message" label="祝福语" />
            <el-table-column prop="createTime" label="发送时间" width="180" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status===1?'success':row.status===2?'info':'warning'">
                  {{ row.status===1?'已领取':row.status===2?'已过期':'待领取' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button v-if="row.status===0" type="primary" size="small" @click="doReceive(row.id)">领取</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="发送的红包" name="sent">
          <el-table :data="sent" stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column label="金额" width="120">
              <template #default="{ row }">¥{{ (row.amount / 100).toFixed(2) }}</template>
            </el-table-column>
            <el-table-column prop="message" label="祝福语" />
            <el-table-column prop="createTime" label="发送时间" width="180" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status===1?'success':row.status===2?'info':'warning'">
                  {{ row.status===1?'已领取':row.status===2?'已过期':'待领取' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
      <el-button type="danger" style="margin-top:16px" @click="showSend = true">🧧 发红包</el-button>
    </div>
    <el-dialog v-model="showSend" title="发红包" width="400px">
      <el-form label-width="80px">
        <el-form-item label="金额(元)">
          <el-input-number v-model="sendForm.amount" :min="0.01" :precision="2" />
        </el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="sendForm.type">
            <el-radio :value="1">普通红包</el-radio>
            <el-radio :value="2">订单返利</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="订单编号">
          <el-input v-model="sendForm.orderNo" placeholder="输入关联订单编号" />
        </el-form-item>
        <el-form-item label="祝福语">
          <el-input v-model="sendForm.message" placeholder="恭喜发财！" />
        </el-form-item>
        <el-form-item>
          <el-button type="danger" @click="doSend">发送</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getReceivedEnvelopes, getSentEnvelopes, receiveRedEnvelope, sendRedEnvelope } from '@/api/redenvelope'
import { ElMessage } from 'element-plus'

const tab = ref('received')
const received = ref<any[]>([])
const sent = ref<any[]>([])
const showSend = ref(false)
const sendForm = ref({ amount: 10, type: 1, message: '恭喜发财！', orderNo: '' })

async function loadReceived() { const r: any = await getReceivedEnvelopes(); received.value = r.data || [] }
async function loadSent() { const r: any = await getSentEnvelopes(); sent.value = r.data || [] }
async function doReceive(id: number) {
  try { await receiveRedEnvelope(id); ElMessage.success('领取成功'); loadReceived() } catch {}
}
async function doSend() {
  const orderNo = sendForm.value.orderNo || null
  if (!orderNo && sendForm.value.type === 2) {
    ElMessage.warning('订单返利红包必须填写订单编号')
    return
  }
  try {
    await sendRedEnvelope({ amount: Math.round(sendForm.value.amount * 100), type: sendForm.value.type, message: sendForm.value.message, orderNo })
    ElMessage.success('红包已发送'); showSend.value = false; loadSent()
  } catch { /* 错误已由 request 拦截器统一提示 */ }
}
onMounted(() => { loadReceived(); loadSent() })
</script>
<style scoped>
.page{background:#f0f2f5;min-height:100vh}
.top-bar{background:#fff;box-shadow:0 1px 4px rgba(0,0,0,0.06);position:sticky;top:0;z-index:100}
.inner{max-width:1200px;margin:0 auto;padding:0 20px}
.top-row{display:flex;align-items:center;gap:24px;height:60px}
.logo{font-size:22px;font-weight:700;color:#ff4d4f;text-decoration:none}
.title{font-size:20px;font-weight:700;color:#333;flex:1}
.back{color:#999;text-decoration:none;font-size:14px}
.container{max-width:1000px;margin:0 auto;padding:20px}
</style>
