<template>
  <div class="page"><el-container>
    <el-header class="mh"><span>CloudMall 商家后台</span>
      <div><router-link to="/merchant/dashboard">看板</router-link><router-link to="/merchant/products">商品</router-link><router-link to="/merchant/orders">订单</router-link><router-link to="/merchant/coupons">优惠券</router-link><router-link to="/merchant/seckill">秒杀</router-link><router-link to="/merchant/groupbuy">拼团</router-link><router-link to="/merchant/presale">预售</router-link><router-link to="/merchant/distribution">分销</router-link><router-link to="/merchant/combo">套餐</router-link><router-link to="/merchant/redenvelope">红包</router-link><router-link to="/" style="color:#ff4d4f">回前台</router-link></div>
    </el-header>
    <el-main>
      <h3>发布商品</h3>
      <el-form :model="form" label-width="80px" style="max-width:600px">
        <el-form-item label="商品名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="品牌"><el-input v-model="form.brand" /></el-form-item>
        <el-form-item label="描述">
          <QuillEditor v-model:content="form.description" content-type="html" theme="snow" toolbar="essential" style="height:200px;margin-bottom:40px" />
        </el-form-item>
        <el-form-item label="类目">
          <el-select v-model="form.categoryId" placeholder="选择类目">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="SKU规格">
          <div v-for="(sku,i) in form.skus" :key="i" style="display:flex;gap:8px;margin-bottom:5px">
            <el-input v-model="sku.specInfo" placeholder='{"颜色":"黑","容量":"256GB"}' style="width:220px" />
            <el-input-number v-model="sku.price" :min="1" placeholder="价格(分)" />
            <el-input-number v-model="sku.stock" :min="0" placeholder="库存" />
            <el-button @click="form.skus.splice(i,1)" type="danger" size="small">删</el-button>
          </div>
          <el-button @click="form.skus.push({specInfo:'{}',price:0,stock:0})" size="small">+ 添加规格</el-button>
        </el-form-item>
        <el-form-item><el-button type="primary" @click="save" :loading="saving">保存并上架</el-button></el-form-item>
      </el-form>
    </el-main>
  </el-container></div>
</template>
<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'; import { useRouter } from 'vue-router'; import { ElMessage } from 'element-plus'; import { QuillEditor } from '@vueup/vue-quill'; import '@vueup/vue-quill/dist/vue-quill.snow.css'; import request from '@/utils/request'
const router = useRouter(); const saving = ref(false); const categories = ref<any[]>([])
const form = reactive<any>({ name:'', brand:'', description:'', categoryId:null, skus:[] })
onMounted(async () => { const r:any = await request.get('/product/category/list'); categories.value = r.data||[] })
async function save() {
  if (!form.name) { ElMessage.warning('请填写商品名称'); return }
  saving.value = true
  try { await request.post('/merchant/product', form); ElMessage.success('发布成功'); router.push('/merchant/products') }
  finally { saving.value = false }
}
</script>
<style scoped>.page{min-height:100vh;background:#f0f2f5}.mh{background:#fff;display:flex;align-items:center;justify-content:space-between;font-size:20px;font-weight:700;color:#1890ff;padding:0 20px;height:60px;box-shadow:0 1px 4px rgba(0,0,0,.06)}.mh a{margin:0 10px;color:#666;text-decoration:none;font-size:14px;font-weight:400}.el-main{max-width:800px;margin:20px auto}</style>
