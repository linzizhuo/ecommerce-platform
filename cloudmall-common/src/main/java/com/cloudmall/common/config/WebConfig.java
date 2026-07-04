package com.cloudmall.common.config;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置 — 注册JWT拦截器
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Resource
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")           // API接口需要登录
                .excludePathPatterns(
                        "/api/user/login", "/api/user/register",
                        "/api/admin/login",           // 管理员登录公开
                        "/api/merchant/login",        // 商家登录公开
                        "/api/product/**",           // 商品浏览公开
                        "/api/category/**",          // 类目公开
                        "/api/review/product/**",   // 评价查看公开（提交评价需登录）
                        "/api/search/**",            // 搜索公开
                        "/api/notice/active",        // 公告公开
                        "/api/seckill/sessions",     // 秒杀场次公开
                        "/api/presale/list",         // 预售列表公开
                        "/api/combo/**",             // 套餐浏览公开
                        "/api/point/levels",         // 等级公开
                        "/api/config/**"             // 配置公开
                );
    }
}
