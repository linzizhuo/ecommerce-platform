<template>
  <AdminLayout title="⚙️ 系统配置">
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
            <el-form-item label="Banner文字"><el-input v-model="homeForm.bannerText" placeholder="例如：⚡ 限时秒杀火热进行中 · 手慢无 · 点击抢购 >"/></el-form-item>
            <el-form-item label="Banner跳转链接"><el-input v-model="homeForm.bannerLink" placeholder="例如：/seckill"/></el-form-item>
            <el-form-item label="推荐商品ID"><el-input v-model="homeForm.featuredIds" placeholder="多个ID用逗号分隔，例如：1,2,3"/><span style="color:#999;font-size:12px">仅显示已上架商品，无效ID自动忽略</span></el-form-item>
            <el-form-item><el-button type="primary" @click="saveHomeConfig" :loading="homeSaving">保存首页配置</el-button></el-form-item>
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
      <el-tab-pane label="弹窗管理" name="popup">
        <el-button type="primary" @click="popupDialog=true;popupForm={type:3,status:1}" style="margin-bottom:12px">+ 新弹窗</el-button>
        <el-table :data="popups">
          <el-table-column prop="title" label="弹窗标题"/>
          <el-table-column prop="content" label="内容/链接"/>
          <el-table-column label="状态" width="100"><template #default="{row}">{{row.status?'启用':'禁用'}}</template></el-table-column>
          <el-table-column prop="startTime" label="开始时间" width="160"/>
          <el-table-column prop="endTime" label="结束时间" width="160"/>
          <el-table-column label="操作" width="140">
            <template #default="{row}">
              <el-button size="small" @click="popupForm={...row};popupDialog=true">编辑</el-button>
              <el-button size="small" type="danger" @click="delNotice(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="渠道配置" name="channel">
        <el-button type="primary" @click="addChannel()" style="margin-bottom:12px">+ 添加渠道</el-button>
        <el-table :data="channels">
          <el-table-column prop="configKey" label="渠道编码" width="180"/>
          <el-table-column prop="configValue" label="渠道名称"/>
          <el-table-column prop="description" label="说明"/>
          <el-table-column label="操作" width="120"><template #default="{row}"><el-button size="small" @click="editConfig(row)">编辑</el-button><el-button size="small" type="danger" @click="delChannel(row)">删除</el-button></template></el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
    <!-- 公告/弹窗编辑 -->
    <el-dialog v-model="noticeDialog" title="编辑公告" width="500px"><el-form :model="noticeForm" label-width="80px">
      <el-form-item label="标题"><el-input v-model="noticeForm.title"/></el-form-item>
      <el-form-item label="类型"><el-select v-model="noticeForm.type"><el-option label="系统" :value="1"/><el-option label="活动" :value="2"/><el-option label="弹窗" :value="3"/></el-select></el-form-item>
      <el-form-item label="内容"><el-input v-model="noticeForm.content" type="textarea"/></el-form-item>
      <el-form-item label="开始时间"><el-date-picker v-model="noticeForm.startTime" type="datetime" placeholder="选填" style="width:100%"/></el-form-item>
      <el-form-item label="结束时间"><el-date-picker v-model="noticeForm.endTime" type="datetime" placeholder="选填" style="width:100%"/></el-form-item>
    </el-form><template #footer><el-button @click="noticeDialog=false">取消</el-button><el-button type="danger" @click="saveNotice">保存</el-button></template></el-dialog>
    <!-- 弹窗编辑 -->
    <el-dialog v-model="popupDialog" title="编辑弹窗" width="500px"><el-form :model="popupForm" label-width="80px">
      <el-form-item label="标题"><el-input v-model="popupForm.title"/></el-form-item>
      <el-form-item label="内容(图片URL或文案)"><el-input v-model="popupForm.content" type="textarea"/></el-form-item>
      <el-form-item label="状态"><el-switch v-model="popupForm.status" :active-value="1" :inactive-value="0"/></el-form-item>
      <el-form-item label="开始时间"><el-date-picker v-model="popupForm.startTime" type="datetime" placeholder="选填" style="width:100%"/></el-form-item>
      <el-form-item label="结束时间"><el-date-picker v-model="popupForm.endTime" type="datetime" placeholder="选填" style="width:100%"/></el-form-item>
    </el-form><template #footer><el-button @click="popupDialog=false">取消</el-button><el-button type="primary" @click="savePopup">保存</el-button></template></el-dialog>
  </AdminLayout>
</template>
<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import AdminLayout from '@/layouts/AdminLayout.vue'
const activeTab = ref('config')
const configs = ref<any[]>([])
const notices = ref<any[]>([])
const noticeDialog = ref(false)
const popupDialog = ref(false)
const noticeForm = ref<any>({})
const popupForm = ref<any>({type:3,status:1})
const homeSaving = ref(false)
const homeForm = reactive({ bannerText: '', bannerLink: '', featuredIds: '' })

const popups = computed(() => notices.value.filter(n => n.type === 3))
const channels = computed(() => configs.value.filter(c => (c.configKey || '').startsWith('channel_')))
const allChannels = computed(() => configs.value.filter(c => (c.configKey || '').startsWith('channel_')))

async function loadConfigs(){const r:any=await request.get('/admin/config/list');configs.value=r.data||[]
  const bc=configs.value.find((c:any)=>c.configKey==='home_banner_text')
  const bl=configs.value.find((c:any)=>c.configKey==='home_banner_link')
  const fp=configs.value.find((c:any)=>c.configKey==='home_featured_product_ids')
  if(bc)homeForm.bannerText=bc.configValue||'';if(bl)homeForm.bannerLink=bl.configValue||'';if(fp)homeForm.featuredIds=fp.configValue||''
}
async function loadNotices(){const r:any=await request.get('/admin/notice/list');notices.value=r.data||[]}
async function editConfig(row:any){const v=prompt('新值:',row.configValue);if(v){await request.post('/admin/config',{...row,configValue:v});loadConfigs()}}
async function saveNotice(){await request.post('/admin/notice',noticeForm.value);ElMessage.success('已保存');noticeDialog.value=false;loadNotices()}
async function delNotice(id:number){await request.delete('/admin/notice/'+id);ElMessage.success('已删除');loadNotices()}
async function savePopup(){
  const data = { ...popupForm.value, type: 3 }
  await request.post('/admin/notice', data)
  ElMessage.success('弹窗已保存'); popupDialog.value = false; loadNotices()
}
async function saveHomeConfig() {
  homeSaving.value=true
  try{
    const items=[{configKey:'home_banner_text',configValue:homeForm.bannerText,description:'首页Banner文字'},{configKey:'home_banner_link',configValue:homeForm.bannerLink,description:'首页Banner跳转链接'},{configKey:'home_featured_product_ids',configValue:homeForm.featuredIds,description:'首页推荐商品ID(逗号分隔)'}]
    for(const item of items){const existing=configs.value.find((c:any)=>c.configKey===item.configKey);if(existing)item['id']=existing.id;await request.post('/admin/config',item)}
    ElMessage.success('首页配置已保存');loadConfigs()
  }finally{homeSaving.value=false}
}
function addChannel() {
  const code = prompt('渠道编码(如: channel_app):')
  if (!code) return
  const name = prompt('渠道名称(如: APP):')
  if (!name) return
  editConfig({ configKey: code, configValue: name, description: '渠道配置' })
  loadConfigs()
}
async function delChannel(row: any) {
  await request.delete('/admin/config/' + row.id)
  ElMessage.success('已删除'); loadConfigs()
}
onMounted(()=>{loadConfigs();loadNotices()})
</script>
