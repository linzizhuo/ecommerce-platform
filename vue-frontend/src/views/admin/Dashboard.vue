<template>
  <div class="page"><el-container>
    <el-aside width="200px" style="background:#304156;min-height:100vh;color:#fff;padding:20px 0">
      <h3 style="text-align:center;color:#fff;margin-bottom:30px">CloudMall</h3>
      <el-menu background-color="#304156" text-color="#bfcbd9" active-text-color="#409EFF" router>
        <el-menu-item index="/admin/dashboard"><el-icon><DataAnalysis /></el-icon>数据大屏</el-menu-item>
        <el-menu-item index="/admin/users"><el-icon><User /></el-icon>用户管理</el-menu-item>
        <el-menu-item index="/admin/merchants"><el-icon>< Shop /></el-icon>商家管理</el-menu-item>
        <el-menu-item index="/admin/products"><el-icon><Goods /></el-icon>商品审核</el-menu-item>
        <el-menu-item index="/admin/roles"><el-icon><Setting /></el-icon>角色权限</el-menu-item>
      </el-menu>
    </el-aside>
    <el-main style="background:#f0f2f5;min-height:100vh">
      <h2>数据大屏</h2>
      <p style="color:#999">平台运营数据概览</p>
      <el-card style="margin-top:20px"><el-empty description="功能开发中，课程演示版本" /></el-card>
    </el-main>
  </el-container></div>
</template>
<style scoped>.page{min-height:100vh}</style>
