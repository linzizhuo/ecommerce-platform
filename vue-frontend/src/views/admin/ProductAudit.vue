<template>
  <div class="page">
    <el-container>
      <el-aside width="200px" style="background:#304156;min-height:100vh;color:#fff;padding:20px 0">
        <h3 style="text-align:center;color:#fff">CloudMall 管理后台</h3>
        <el-menu background-color="#304156" text-color="#fff" active-text-color="#ffd04b" router>
          <el-menu-item index="/admin/dashboard">数据大屏</el-menu-item>
          <el-menu-item index="/admin/users">用户管理</el-menu-item>
          <el-menu-item index="/admin/merchants">商家管理</el-menu-item>
          <el-menu-item index="/admin/products">商品审核</el-menu-item>
          <el-menu-item index="/admin/roles">角色权限</el-menu-item>
        </el-menu>
      </el-aside>
      <el-main><slot>管理后台</slot></el-main>
    </el-container>
  </div>
</template>
<style scoped>.page { min-height: 100vh; background: #f0f2f5; }
.el-aside { min-height: 100vh; }</style>
