<template>
  <div class="page">
    <el-container>
      <el-header class="mh"><span>CloudMall 商家后台</span>
        <div><router-link to="/merchant/dashboard">看板</router-link>
          <router-link to="/merchant/products">商品</router-link>
          <router-link to="/merchant/orders">订单</router-link></div>
      </el-header>
      <el-main>
        <h3>{{ isEdit ? '编辑商品' : '发布商品' }}</h3>
        <el-form :model="form" label-width="80px" style="max-width:600px">
          <el-form-item label="商品名称"><el-input v-model="form.name" /></el-form-item>
          <el-form-item label="品牌"><el-input v-model="form.brand" /></el-form-item>
          <el-form-item label="描述"><el-input v-model="form.description" type="textarea" /></el-form-item>
          <el-form-item label="类目">
            <el-select v-model="form.categoryId" placeholder="选择类目">
              <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="规格SKU">
            <div v-for="(sku, i) in form.skus" :key="i" style="margin-bottom:5px;display:flex;gap:8px">
              <el-input v-model="sku.specInfo" placeholder="规格(曜石黑/256GB)" style="width:180px" />
              <el-input-number v-model="sku.price" :min="1" placeholder="价格(分)" />
              <el-input-number v-model="sku.stock" :min="0" placeholder="库存" />
              <el-button @click="form.skus.splice(i,1)" type="danger" size="small">删</el-button>
            </div>
            <el-button @click="form.skus.push({specInfo:'',price:0,originalPrice:0,stock:0})" size="small">+ 添加</el-button>
          </el-form-item>
          <el-form-item><el-button type="primary" @click="save" :loading="saving">保存</el-button></el-form-item>
        </el-form>
      </el-main>
    </el-container>
  </div>
</template>
<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
const route = useRoute(); const router = useRouter()
const isEdit = ref(false); const saving = ref(false)
const categories = ref<any[]>([])
const form = reactive<any>({ name:'', brand:'', description:'', categoryId:null, skus:[] })
onMounted(async () => {
  const res: any = await request.get('/product/category/list')
  categories.value = res.data || []
  if (route.params.id) { isEdit.value = true; console.log('load', route.params.id) }
})
async function save() {
  saving.value = true
  try { await request.post('/product', form); ElMessage.success('保存成功'); router.push('/merchant/products') }
  finally { saving.value = false }
}
</script>
<style scoped>.page{min-height:100vh;background:#f5f5f5}.mh{background:#fff;box-shadow:0 2px 8px rgba(0,0,0,.08);display:flex;align-items:center;justify-content:space-between;font-size:20px;font-weight:bold;color:#1890ff;padding:0 20px}.mh a{margin:0 10px;color:#666;text-decoration:none;font-size:14px;font-weight:normal}.el-main{max-width:800px;margin:20px auto}</style>
