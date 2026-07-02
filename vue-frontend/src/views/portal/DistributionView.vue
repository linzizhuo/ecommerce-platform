<template>
  <div class="page">
    <header class="top-bar"><div class="inner top-row">
      <router-link to="/" class="logo">🛒 CloudMall</router-link>
      <div class="title">💰 分销中心</div>
      <router-link to="/" class="back">返回首页</router-link>
    </div></header>
    <div class="container">
      <div v-if="!info || !info.id" class="register-box">
        <el-card shadow="hover">
          <h2>成为分销员</h2>
          <p>分享商品赚取佣金，邀请好友一起赚钱！</p>
          <el-form label-width="100px" style="max-width:400px;margin-top:20px">
            <el-form-item label="邀请人ID">
              <el-input v-model="parentId" placeholder="选填，没有可不填" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="doRegister">立即注册</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>
      <div v-else>
        <el-card shadow="hover" class="info-card">
          <h2>我的分销</h2>
          <el-row :gutter="20" style="margin-top:20px">
            <el-col :span="8">
              <el-statistic title="累计佣金" :value="(info.totalCommission || 0) / 100" prefix="¥" :precision="2" />
            </el-col>
            <el-col :span="8">
              <el-statistic title="可提现" :value="(info.availableCommission || 0) / 100" prefix="¥" :precision="2" />
            </el-col>
            <el-col :span="8">
              <el-statistic title="分销等级" :value="info.level || 0" />
            </el-col>
          </el-row>
          <el-button type="success" style="margin-top:20px" @click="doWithdraw">申请提现</el-button>
        </el-card>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getDistributionInfo, registerDistributor, withdraw } from '@/api/distribution'
import { ElMessage } from 'element-plus'

const info = ref<any>(null)
const parentId = ref('')

async function load() {
  try { const r: any = await getDistributionInfo(); info.value = r.data } catch { info.value = null }
}
async function doRegister() {
  try {
    await registerDistributor(parentId.value ? Number(parentId.value) : undefined)
    ElMessage.success('注册成功')
    load()
  } catch {}
}
async function doWithdraw() {
  try { await withdraw(info.value.availableCommission); ElMessage.success('提现申请已提交'); load() } catch {}
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
.container{max-width:800px;margin:0 auto;padding:20px}
.register-box{text-align:center;padding:40px 0}
.info-card{padding:10px}
</style>
