<template>
  <el-container style="min-height:100vh">
    <el-aside width="200px" style="background:#304156">
      <div style="color:#fff;text-align:center;padding:20px 0;font-size:18px;font-weight:700">运营后台</div>
      <el-menu :default-active="$route.path" router background-color="#304156" text-color="#bfcbd9" active-text-color="#ff4d4f">
        <el-menu-item index="/admin/dashboard">📊 数据看板</el-menu-item>
        <el-menu-item index="/admin/statistics">📈 数据统计</el-menu-item>
        <el-menu-item index="/admin/users">👤 用户管理</el-menu-item>
        <el-menu-item index="/admin/merchants">🏪 商家管理</el-menu-item>
        <el-menu-item index="/admin/products">📦 商品审核</el-menu-item>
        <el-menu-item index="/admin/activities">🎯 活动管理</el-menu-item>
        <el-menu-item index="/admin/roles">🔐 角色权限</el-menu-item>
        <el-menu-item index="/admin/dict">📋 数据字典</el-menu-item>
        <el-menu-item index="/admin/violations">⚠️ 违规处罚</el-menu-item>
        <el-menu-item index="/admin/config">⚙️ 系统配置</el-menu-item>
      </el-menu>
    </el-aside>
    <el-main style="background:#f0f2f5;padding:20px">
      <h2 style="margin-bottom:16px">📋 数据字典</h2>
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card shadow="hover">
            <template #header><span>字典类型</span><el-button size="small" type="primary" style="float:right" @click="addType">+ 新增</el-button></template>
            <div v-for="t in types" :key="t.id" :class="['type-item', selectedType?.id===t.id ? 'active' : '']" @click="selectType(t)">
              {{ t.dictName }} <small style="color:#999">({{ t.dictCode }})</small>
              <el-button size="small" type="danger" circle @click.stop="delType(t.id)">×</el-button>
            </div>
            <el-empty v-if="!types.length" description="暂无类型" />
          </el-card>
        </el-col>
        <el-col :span="16">
          <el-card shadow="hover" v-if="selectedType">
            <template #header><span>字典项 - {{ selectedType.dictName }}</span><el-button size="small" type="primary" style="float:right" @click="addItem">+ 新增项</el-button></template>
            <el-table :data="items" stripe>
              <el-table-column prop="label" label="标签"/>
              <el-table-column prop="value" label="值"/>
              <el-table-column prop="sort" label="排序" width="80"/>
              <el-table-column label="操作" width="150">
                <template #default="{row}">
                  <el-button size="small" @click="editItem(row)">编辑</el-button>
                  <el-button size="small" type="danger" @click="delItem(row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
          <el-empty v-else description="请选择字典类型" />
        </el-col>
      </el-row>
      <el-dialog v-model="typeDialog" title="字典类型" width="400px">
        <el-form :model="typeForm" label-width="80px">
          <el-form-item label="名称"><el-input v-model="typeForm.dictName"/></el-form-item>
          <el-form-item label="编码"><el-input v-model="typeForm.dictCode"/></el-form-item>
          <el-form-item label="描述"><el-input v-model="typeForm.description"/></el-form-item>
        </el-form>
        <template #footer><el-button @click="typeDialog=false">取消</el-button><el-button type="primary" @click="saveType">保存</el-button></template>
      </el-dialog>
      <el-dialog v-model="itemDialog" title="字典项" width="400px">
        <el-form :model="itemForm" label-width="60px">
          <el-form-item label="标签"><el-input v-model="itemForm.label"/></el-form-item>
          <el-form-item label="值"><el-input v-model="itemForm.value"/></el-form-item>
          <el-form-item label="排序"><el-input-number v-model="itemForm.sort" :min="0"/></el-form-item>
        </el-form>
        <template #footer><el-button @click="itemDialog=false">取消</el-button><el-button type="primary" @click="saveItem">保存</el-button></template>
      </el-dialog>
    </el-main>
  </el-container>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getDictTypes, saveDictType, updateDictType, deleteDictType, getDictItems, saveDictItem, updateDictItem, deleteDictItem } from '@/api/dict'
import { ElMessage } from 'element-plus'

const types = ref<any[]>([])
const items = ref<any[]>([])
const selectedType = ref<any>(null)
const typeDialog = ref(false)
const itemDialog = ref(false)
const typeForm = ref({ dictName:'', dictCode:'', description:'' })
const itemForm = ref({ label:'', value:'', sort:0 })

async function loadTypes() { const r: any = await getDictTypes(); types.value = r.data || [] }
function selectType(t: any) { selectedType.value = t; loadItems() }
async function loadItems() {
  if (!selectedType.value) return
  const r: any = await getDictItems(selectedType.value.dictCode); items.value = r.data || []
}
function addType() { typeForm.value = { dictName:'', dictCode:'', description:'' }; typeDialog.value = true }
async function saveType() {
  try { await saveDictType(typeForm.value); ElMessage.success('已保存'); typeDialog.value = false; loadTypes() } catch {}
}
async function delType(id: number) { await deleteDictType(id); ElMessage.success('已删除'); loadTypes(); selectedType.value = null }
function addItem() { itemForm.value = { label:'', value:'', sort:0 }; itemDialog.value = true }
function editItem(row: any) { itemForm.value = { ...row }; itemDialog.value = true }
async function saveItem() {
  if (!selectedType.value) return
  const data = { ...itemForm.value, dictTypeId: selectedType.value.id }
  try {
    if ((itemForm.value as any).id) { await updateDictItem((itemForm.value as any).id, data) }
    else { await saveDictItem(data) }
    ElMessage.success('已保存'); itemDialog.value = false; loadItems()
  } catch {}
}
async function delItem(id: number) { await deleteDictItem(id); ElMessage.success('已删除'); loadItems() }
onMounted(loadTypes)
</script>
<style scoped>
.type-item{padding:10px 12px;cursor:pointer;border-radius:6px;display:flex;align-items:center;justify-content:space-between;margin:4px 0}
.type-item:hover{background:#f0f2f5}
.type-item.active{background:#e6f7ff;border:1px solid #91d5ff}
</style>
