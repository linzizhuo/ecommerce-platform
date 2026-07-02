import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    // ====== C端 (Portal) ======
    { path: '/', name: 'home', component: () => import('@/views/portal/HomeView.vue') },
    { path: '/login', name: 'login', component: () => import('@/views/portal/LoginView.vue') },
    { path: '/register', name: 'register', component: () => import('@/views/portal/RegisterView.vue') },
    { path: '/product/list', name: 'productList', component: () => import('@/views/portal/ProductList.vue') },
    { path: '/product/:id', name: 'productDetail', component: () => import('@/views/portal/ProductDetail.vue') },
    { path: '/cart', name: 'cart', component: () => import('@/views/portal/CartView.vue') },
    { path: '/checkout', name: 'checkout', component: () => import('@/views/portal/CheckoutView.vue') },
    { path: '/orders', name: 'orders', component: () => import('@/views/portal/OrderList.vue') },
    { path: '/order/:id', name: 'orderDetail', component: () => import('@/views/portal/OrderDetail.vue') },
    { path: '/user/center', name: 'userCenter', component: () => import('@/views/portal/UserCenter.vue') },
    // C端高并发新增
    { path: '/seckill', name: 'seckill', component: () => import('@/views/portal/SeckillView.vue') },
    { path: '/search', name: 'search', component: () => import('@/views/portal/SearchView.vue') },
    { path: '/favorites', name: 'favorites', component: () => import('@/views/portal/FavoriteView.vue') },
    { path: '/messages', name: 'messages', component: () => import('@/views/portal/MessageView.vue') },
    { path: '/aftersale', name: 'aftersale', component: () => import('@/views/portal/AfterSaleView.vue') },
    { path: '/logistics/:orderId', name: 'logistics', component: () => import('@/views/portal/LogisticsView.vue') },
    { path: '/points', name: 'points', component: () => import('@/views/portal/PointView.vue') },
    { path: '/groupbuy/:id', name: 'groupbuy', component: () => import('@/views/portal/GroupBuyView.vue') },
    // C端新增
    { path: '/presale', name: 'presale', component: () => import('@/views/portal/PresaleView.vue') },
    { path: '/distribution', name: 'distribution', component: () => import('@/views/portal/DistributionView.vue') },
    { path: '/combo', name: 'combo', component: () => import('@/views/portal/ComboView.vue') },
    { path: '/red-envelope', name: 'redEnvelope', component: () => import('@/views/portal/RedEnvelopeView.vue') },

    // ====== 商家端 (Merchant) ======
    { path: '/merchant', redirect: '/merchant/dashboard' },
    { path: '/merchant/login', name: 'merchantLogin', component: () => import('@/views/merchant/LoginView.vue') },
    { path: '/merchant/dashboard', name: 'merchantDashboard', component: () => import('@/views/merchant/Dashboard.vue') },
    { path: '/merchant/products', name: 'merchantProducts', component: () => import('@/views/merchant/ProductManage.vue') },
    { path: '/merchant/product/edit/:id?', name: 'merchantProductEdit', component: () => import('@/views/merchant/ProductEdit.vue') },
    { path: '/merchant/orders', name: 'merchantOrders', component: () => import('@/views/merchant/OrderManage.vue') },
    { path: '/merchant/coupons', name: 'merchantCoupons', component: () => import('@/views/merchant/CouponManage.vue') },
    { path: '/merchant/seckill', name: 'merchantSeckill', component: () => import('@/views/merchant/SeckillManage.vue') },
    { path: '/merchant/groupbuy', name: 'merchantGroupbuy', component: () => import('@/views/merchant/GroupBuyManage.vue') },
    // 商家端新增
    { path: '/merchant/presale', name: 'merchantPresale', component: () => import('@/views/merchant/PresaleManage.vue') },
    { path: '/merchant/distribution', name: 'merchantDistribution', component: () => import('@/views/merchant/DistributionManage.vue') },
    { path: '/merchant/combo', name: 'merchantCombo', component: () => import('@/views/merchant/ComboManage.vue') },
    { path: '/merchant/redenvelope', name: 'merchantRedEnvelope', component: () => import('@/views/merchant/RedEnvelopeManage.vue') },

    // ====== 平台端 (Admin) ======
    { path: '/admin', redirect: '/admin/dashboard' },
    { path: '/admin/login', name: 'adminLogin', component: () => import('@/views/admin/LoginView.vue') },
    { path: '/admin/dashboard', name: 'adminDashboard', component: () => import('@/views/admin/Dashboard.vue') },
    { path: '/admin/statistics', name: 'adminStatistics', component: () => import('@/views/admin/StatisticsView.vue') },
    { path: '/admin/users', name: 'adminUsers', component: () => import('@/views/admin/UserManage.vue') },
    { path: '/admin/merchants', name: 'adminMerchants', component: () => import('@/views/admin/MerchantManage.vue') },
    { path: '/admin/products', name: 'adminProducts', component: () => import('@/views/admin/ProductAudit.vue') },
    { path: '/admin/roles', name: 'adminRoles', component: () => import('@/views/admin/RoleManage.vue') },
    { path: '/admin/config', name: 'adminConfig', component: () => import('@/views/admin/ConfigView.vue') },
    // 平台端新增
    { path: '/admin/activities', name: 'adminActivities', component: () => import('@/views/admin/ActivityManage.vue') },
    { path: '/admin/dict', name: 'adminDict', component: () => import('@/views/admin/DictManage.vue') },
    { path: '/admin/violations', name: 'adminViolations', component: () => import('@/views/admin/ViolationManage.vue') },
  ]
})

export default router
