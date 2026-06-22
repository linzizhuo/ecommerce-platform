<template>
  <div class="page">
    <el-container>
      <el-header style="background:#fff;box-shadow:0 2px 8px rgba(0,0,0,0.08);display:flex;align-items:center;justify-content:space-between">
        <span style="font-size:20px;font-weight:bold;color:#1890ff">CloudMall 商家后台</span>
        <div>
          <router-link to="/merchant/dashboard" style="margin:0 10px">看板</router-link>
          <router-link to="/merchant/products" style="margin:0 10px">商品</router-link>
          <router-link to="/merchant/orders" style="margin:0 10px">订单</router-link>
          <router-link to="/merchant/coupons" style="margin:0 10px">优惠券</router-link>
          <el-button size="small" @click="$router.push('/')">回前台</el-button>
        </div>
      </el-header>
      <el-main style="max-width:1200px;margin:20px auto">
        <slot>商家后台</slot>
      </el-main>
    </el-container>
  </div>
</template>
<style scoped>.page { min-height: 100vh; background: #f5f5f5; }</style>
