<template>
  <div class="page"><el-container>
    <el-header class="mh"><span>CloudMall 商家后台</span><div><router-link to="/merchant/dashboard">看板</router-link><router-link to="/merchant/products">商品</router-link><router-link to="/merchant/orders">订单</router-link><router-link to="/merchant/coupons">优惠券</router-link></div></el-header>
    <el-main>
      <h3>数据看板</h3>
      <el-row :gutter="16">
        <el-col :span="6"><el-card><div class="stat"><div class="num">--</div><div class="label">今日订单</div></div></el-card></el-col>
        <el-col :span="6"><el-card><div class="stat"><div class="num">--</div><div class="label">今日销售额</div></div></el-card></el-col>
        <el-col :span="6"><el-card><div class="stat"><div class="num">--</div><div class="label">在售商品</div></div></el-card></el-col>
        <el-col :span="6"><el-card><div class="stat"><div class="num">--</div><div class="label">访客数</div></div></el-card></el-col>
      </el-row>
    </el-main>
  </el-container></div>
</template>
<style scoped>.page{min-height:100vh;background:#f5f5f5}.mh{background:#fff;box-shadow:0 2px 8px rgba(0,0,0,.08);display:flex;align-items:center;justify-content:space-between;font-size:20px;font-weight:bold;color:#1890ff;padding:0 20px}.mh a{margin:0 10px;color:#666;text-decoration:none;font-size:14px;font-weight:normal}.el-main{max-width:1200px;margin:20px auto}.stat{text-align:center;padding:20px}.num{font-size:36px;color:#1890ff;font-weight:bold}.label{color:#999;margin-top:8px}</style>
