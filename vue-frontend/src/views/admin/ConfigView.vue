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
        <el-menu-item index="/admin/roles">🔐 角色权限</el-menu-item>
        <el-menu-item index="/admin/activities">🎯 活动管理</el-menu-item>
        <el-menu-item index="/admin/dict">📋 数据字典</el-menu-item>
        <el-menu-item index="/admin/violations">⚠️ 违规处罚</el-menu-item>
        <el-menu-item index="/admin/config">⚙️ 系统配置</el-menu-item>
      </el-menu>
    </el-aside>
    <el-main style="background:#f0f2f5;padding:20px">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="系统配置" name="config">
          <el-table :data="configs">
            <el-table-column prop="configKey" label="Key" width="200"/>
            <el-table-column prop="configValue" label="Value"/>
            <el-table-column prop="description" label="说明"/>
            <el-table-column label="操作" width="120"><template #default="{row}"><el-button size="small" @click="editConfig(row)">编辑</el-button></template></el-table-column>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="首页配置" name="home">
          <el-card>
            <el-form :model="homeForm" label-width="120px" style="max-width:600px">
              <el-form-item label="Banner文字">
                <el-input v-model="homeForm.bannerText" placeholder="例如：⚡ 限时秒杀火热进行中 · 手慢无 · 点击抢购 >"/>
              </el-form-item>
              <el-form-item label="Banner跳转链接">
                <el-input v-model="homeForm.bannerLink" placeholder="例如：/seckill"/>
              </el-form-item>
              <el-form-item label="推荐商品ID">
                <el-input v-model="homeForm.featuredIds" placeholder="多个ID用逗号分隔，例如：1,2,3"/>
                <span style="color:#999;font-size:12px">仅显示已上架商品，无效ID自动忽略</span>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="saveHomeConfig" :loading="homeSaving">保存首页配置</el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </el-tab-pane>
        <el-tab-pane label="公告管理" name="notice">
          <el-button type="danger" @click="noticeDialog=true;noticeForm={}" style="margin-bottom:12px">发布公告</el-button>
          <el-table :data="notices">
            <el-table-column prop="title" label="标题"/>
            <el-table-column label="类型"><template #default="{row}">{{['','系统','活动','弹窗'][row.type]||'其他'}}</template></el-table-column>
            <el-table-column label="状态"><template #default="{row}">{{row.status?'显示':'隐藏'}}</template></el-table-column>
            <el-table-column label="操作" width="120"><template #default="{row}"><el-button size="small" type="danger" @click="delNotice(row.id)">删除</el-button></template></el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
      <el-dialog v-model="noticeDialog" title="编辑公告" width="500px"><el-form :model="noticeForm" label-width="80px">
        <el-form-item label="标题"><el-input v-model="noticeForm.title"/></el-form-item>
        <el-form-item label="类型"><el-select v-model="noticeForm.type"><el-option label="系统" :value="1"/><el-option label="活动" :value="2"/><el-option label="弹窗" :value="3"/></el-select></el-form-item>
        <el-form-item label="内容"><el-input v-model="noticeForm.content" type="textarea"/></el-form-item>
        <el-form-item label="开始时间"><el-date-picker v-model="noticeForm.startTime" type="datetime" placeholder="选填" style="width:100%"/></el-form-item>
        <el-form-item label="结束时间"><el-date-picker v-model="noticeForm.endTime" type="datetime" placeholder="选填" style="width:100%"/></el-form-item>
      </el-form><template #footer><el-button @click="noticeDialog=false">取消</el-button><el-button type="danger" @click="saveNotice">保存</el-button></template></el-dialog>
    </el-main>
  </el-container>
</template>
<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
const activeTab = ref('config')
const configs = ref<any[]>([])
const notices = ref<any[]>([])
const noticeDialog = ref(false)
const noticeForm = ref<any>({})
const homeSaving = ref(false)
const homeForm = reactive({ bannerText: '', bannerLink: '', featuredIds: '' })

async function loadConfigs(){const r:any=await request.get('/admin/config/list');configs.value=r.data||[]
  // 加载首页配置
  const bc = configs.value.find((c:any) => c.configKey === 'home_banner_text')
  const bl = configs.value.find((c:any) => c.configKey === 'home_banner_link')
  const fp = configs.value.find((c:any) => c.configKey === 'home_featured_product_ids')
  if (bc) homeForm.bannerText = bc.configValue || ''
  if (bl) homeForm.bannerLink = bl.configValue || ''
  if (fp) homeForm.featuredIds = fp.configValue || ''
}
async function loadNotices(){const r:any=await request.get('/admin/notice/list');notices.value=r.data||[]}
async function editConfig(row:any){const v=prompt('新值:',row.configValue);if(v){await request.post('/admin/config',{...row,configValue:v});loadConfigs()}}
async function saveNotice(){await request.post('/admin/notice',noticeForm.value);ElMessage.success('已保存');noticeDialog.value=false;loadNotices()}
async function delNotice(id:number){await request.delete('/admin/notice/'+id);ElMessage.success('已删除');loadNotices()}
async function saveHomeConfig() {
  homeSaving.value = true
  try {
    const items = [
      { configKey: 'home_banner_text', configValue: homeForm.bannerText, description: '首页Banner文字' },
      { configKey: 'home_banner_link', configValue: homeForm.bannerLink, description: '首页Banner跳转链接' },
      { configKey: 'home_featured_product_ids', configValue: homeForm.featuredIds, description: '首页推荐商品ID(逗号分隔)' }
    ]
    // 保留已有ID以更新
    for (const item of items) {
      const existing = configs.value.find((c:any) => c.configKey === item.configKey)
      if (existing) item['id'] = existing.id
      await request.post('/admin/config', item)
    }
    ElMessage.success('首页配置已保存')
    loadConfigs()
  } finally { homeSaving.value = false }
}
onMounted(()=>{loadConfigs();loadNotices()})
</script>
