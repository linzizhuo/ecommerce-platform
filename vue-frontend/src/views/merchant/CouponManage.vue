<template>
  <div class="page"><el-container>
    <el-header class="mh"><span>CloudMall 商家后台</span><div><router-link to="/merchant/dashboard">看板</router-link><router-link to="/merchant/products">商品</router-link><router-link to="/merchant/orders">订单</router-link><router-link to="/merchant/coupons">优惠券</router-link></div></el-header>
    <el-main><h3>优惠券管理</h3><el-button type="primary">新建优惠券</el-button><el-empty description="暂无优惠券" style="margin-top:40px" /></el-main>
  </el-container></div>
</template>
<style scoped>.page{min-height:100vh;background:#f5f5f5}.mh{background:#fff;box-shadow:0 2px 8px rgba(0,0,0,.08);display:flex;align-items:center;justify-content:space-between;font-size:20px;font-weight:bold;color:#1890ff;padding:0 20px}.mh a{margin:0 10px;color:#666;text-decoration:none;font-size:14px;font-weight:normal}.el-main{max-width:1200px;margin:20px auto}</style>
