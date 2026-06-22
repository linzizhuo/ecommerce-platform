import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    // ====== C端 ======
    { path: '/', name: 'home', component: () => import('@/views/portal/HomeView.vue') },
    { path: '/login', name: 'login', component: () => import('@/views/portal/LoginView.vue') },
    { path: '/register', name: 'register', component: () => import('@/views/portal/RegisterView.vue') },
    { path: '/product/list', name: 'productList', component: () => import('@/views/portal/ProductList.vue') },
    { path: '/product/:id', name: 'productDetail', component: () => import('@/views/portal/ProductDetail.vue') },
    { path: '/cart', name: 'cart', component: () => import('@/views/portal/CartView.vue') },
    { path: '/checkout', name: 'checkout', component: () => import('@/views/portal/CheckoutView.vue') },
    { path: '/orders', name: 'orders', component: () => import('@/views/portal/OrderList.vue') },
    { path: '/user/center', name: 'userCenter', component: () => import('@/views/portal/UserCenter.vue') },

    // ====== 商家端 ======
    { path: '/merchant', redirect: '/merchant/dashboard' },
    { path: '/merchant/login', name: 'merchantLogin', component: () => import('@/views/merchant/LoginView.vue') },
    { path: '/merchant/dashboard', name: 'merchantDashboard', component: () => import('@/views/merchant/Dashboard.vue') },
    { path: '/merchant/products', name: 'merchantProducts', component: () => import('@/views/merchant/ProductManage.vue') },
    { path: '/merchant/product/edit/:id?', name: 'merchantProductEdit', component: () => import('@/views/merchant/ProductEdit.vue') },
    { path: '/merchant/orders', name: 'merchantOrders', component: () => import('@/views/merchant/OrderManage.vue') },
    { path: '/merchant/coupons', name: 'merchantCoupons', component: () => import('@/views/merchant/CouponManage.vue') },

    // ====== 平台端 ======
    { path: '/admin', redirect: '/admin/dashboard' },
    { path: '/admin/login', name: 'adminLogin', component: () => import('@/views/admin/LoginView.vue') },
    { path: '/admin/dashboard', name: 'adminDashboard', component: () => import('@/views/admin/Dashboard.vue') },
    { path: '/admin/users', name: 'adminUsers', component: () => import('@/views/admin/UserManage.vue') },
    { path: '/admin/merchants', name: 'adminMerchants', component: () => import('@/views/admin/MerchantManage.vue') },
    { path: '/admin/products', name: 'adminProducts', component: () => import('@/views/admin/ProductAudit.vue') },
    { path: '/admin/roles', name: 'adminRoles', component: () => import('@/views/admin/RoleManage.vue') },
  ]
})

export default router
