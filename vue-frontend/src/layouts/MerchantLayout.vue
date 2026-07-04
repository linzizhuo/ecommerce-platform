<template>
  <el-container style="min-height:100vh">
    <el-header class="merchant-header">
      <span class="merchant-logo">CloudMall 商家后台</span>
      <div class="merchant-nav">
        <router-link to="/merchant/dashboard">看板</router-link>
        <router-link to="/merchant/products">商品</router-link>
        <router-link to="/merchant/orders">订单</router-link>
        <router-link to="/merchant/coupons">优惠券</router-link>
        <router-link to="/merchant/seckill">秒杀</router-link>
        <router-link to="/merchant/groupbuy">拼团</router-link>
        <router-link to="/merchant/presale">预售</router-link>
        <router-link to="/merchant/distribution">分销</router-link>
        <router-link to="/merchant/combo">套餐</router-link>
        <router-link to="/merchant/redenvelope">红包</router-link>
        <router-link to="/merchant/activities">活动</router-link>
        <router-link to="/merchant/reconciliation">对账</router-link>
        <router-link to="/merchant/stock-report">库存</router-link>
        <router-link to="/" class="nav-home">回前台</router-link>
      </div>
    </el-header>
    <el-main class="merchant-main">
      <slot />
    </el-main>
  </el-container>
</template>

<style scoped>
.merchant-header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
}
.merchant-logo {
  font-size: 20px;
  font-weight: 700;
  color: #1890ff;
}
.merchant-nav a {
  margin: 0 10px;
  color: #666;
  text-decoration: none;
  font-size: 14px;
}
.merchant-nav a.router-link-active {
  color: #1890ff;
}
.merchant-nav a.nav-home {
  color: #ff4d4f;
}
.merchant-main {
  max-width: 1200px;
  margin: 20px auto;
  padding: 20px;
  width: 100%;
}
</style>
