<template>
  <div class="page">
    <header class="top-bar"><div class="top-inner"><router-link to="/" class="logo">CloudMall</router-link></div></header>
    <div class="content-inner">
      <h2>个人中心</h2>
      <el-card style="margin-bottom:20px">
        <p>用户ID: {{ userStore.user?.id }}</p>
        <p>手机号: {{ userStore.user?.phone }}</p>
        <p>昵称: {{ userStore.user?.nickname }}</p>
      </el-card>
      <el-card><template #header>收货地址</template>
        <el-button @click="dialogVisible=true" type="primary" size="small">新增地址</el-button>
        <el-table :data="addresses" style="margin-top:10px">
          <el-table-column prop="receiver" label="收货人" />
          <el-table-column prop="phone" label="电话" />
          <el-table-column label="地址"><template #default="{row}">{{ row.province }}{{ row.city }}{{ row.district }} {{ row.detail }}</template></el-table-column>
        </el-table>
      </el-card>
      <!-- 地址弹窗 -->
      <el-dialog v-model="dialogVisible" title="新增地址" width="500px">
        <el-form>
          <el-form-item label="收货人"><el-input v-model="addrForm.receiver" /></el-form-item>
          <el-form-item label="电话"><el-input v-model="addrForm.phone" /></el-form-item>
          <el-form-item label="省"><el-input v-model="addrForm.province" /></el-form-item>
          <el-form-item label="市"><el-input v-model="addrForm.city" /></el-form-item>
          <el-form-item label="区"><el-input v-model="addrForm.district" /></el-form-item>
          <el-form-item label="详细地址"><el-input v-model="addrForm.detail" /></el-form-item>
        </el-form>
        <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="saveAddress">保存</el-button></template>
      </el-dialog>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/userStore'
import request from '@/utils/request'
const userStore = useUserStore()
const addresses = ref<any[]>([])
const dialogVisible = ref(false)
const addrForm = reactive({ receiver: '', phone: '', province: '', city: '', district: '', detail: '' })
async function loadAddresses() { const res: any = await request.get('/address'); addresses.value = res.data || [] }
async function saveAddress() {
  await request.post('/address', addrForm)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadAddresses()
}
onMounted(() => { userStore.fetchUser(); loadAddresses() })
</script>
<style scoped>.page { min-height: 100vh; background: #f5f5f5; }
.top-bar { background: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
.top-inner { display: flex; align-items: center; max-width: 900px; margin: 0 auto; padding: 12px 20px; }
.content-inner { max-width: 900px; margin: 0 auto; padding: 20px; }
.logo { font-size: 24px; font-weight: bold; color: #ff4d4f; text-decoration: none; }</style>
