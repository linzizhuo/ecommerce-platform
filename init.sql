-- =============================================
-- CloudMall 数据库初始化脚本
-- 分支: vue-springboot, microservices
-- 数据库: ecommerce
-- =============================================

CREATE DATABASE IF NOT EXISTS ecommerce DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ecommerce;

-- ==================== 用户与权限 ====================

DROP TABLE IF EXISTS t_user;
CREATE TABLE t_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    password VARCHAR(200) NOT NULL COMMENT '密码(BCrypt)',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar VARCHAR(500) COMMENT '头像',
    status INT DEFAULT 1 COMMENT '0禁用 1正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_phone (phone)
) COMMENT '用户';

DROP TABLE IF EXISTS t_user_address;
CREATE TABLE t_user_address (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    receiver VARCHAR(50) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    province VARCHAR(50),
    city VARCHAR(50),
    district VARCHAR(50),
    detail VARCHAR(200),
    is_default INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
) COMMENT '收货地址';

DROP TABLE IF EXISTS t_merchant;
CREATE TABLE t_merchant (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT COMMENT '关联用户ID',
    shop_name VARCHAR(100) NOT NULL,
    logo VARCHAR(500),
    status INT DEFAULT 0 COMMENT '0待审 1通过 2驳回',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '商家';

DROP TABLE IF EXISTS t_admin_user;
CREATE TABLE t_admin_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(200) NOT NULL,
    status INT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_username (username)
) COMMENT '管理员';

DROP TABLE IF EXISTS t_role;
CREATE TABLE t_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL,
    role_code VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    UNIQUE INDEX idx_code (role_code)
) COMMENT '角色';

DROP TABLE IF EXISTS t_permission;
CREATE TABLE t_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    perm_name VARCHAR(50) NOT NULL,
    perm_code VARCHAR(100) NOT NULL,
    parent_id BIGINT DEFAULT 0,
    type INT DEFAULT 1 COMMENT '1菜单 2按钮',
    sort INT DEFAULT 0
) COMMENT '权限';

DROP TABLE IF EXISTS t_role_permission;
CREATE TABLE t_role_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    perm_id BIGINT NOT NULL
) COMMENT '角色-权限';

DROP TABLE IF EXISTS t_user_role;
CREATE TABLE t_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL
) COMMENT '用户-角色';

-- ==================== 商品 ====================

DROP TABLE IF EXISTS t_category;
CREATE TABLE t_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    parent_id BIGINT DEFAULT 0,
    level INT DEFAULT 1,
    sort INT DEFAULT 0,
    INDEX idx_parent_id (parent_id)
) COMMENT '商品类目';

DROP TABLE IF EXISTS t_product;
CREATE TABLE t_product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    merchant_id BIGINT COMMENT '商家ID',
    category_id BIGINT,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    brand VARCHAR(100),
    status INT DEFAULT 0 COMMENT '0草稿 1待审 2已上架 3已下架',
    main_image VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_category (category_id),
    INDEX idx_merchant (merchant_id),
    INDEX idx_status (status)
) COMMENT '商品SPU';

DROP TABLE IF EXISTS t_sku;
CREATE TABLE t_sku (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    spec_info VARCHAR(200) COMMENT '规格JSON',
    price INT NOT NULL COMMENT '价格(分)',
    original_price INT COMMENT '原价(分)',
    image VARCHAR(500),
    stock INT DEFAULT 0,
    INDEX idx_product_id (product_id)
) COMMENT 'SKU';

DROP TABLE IF EXISTS t_product_image;
CREATE TABLE t_product_image (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    url VARCHAR(500) NOT NULL,
    sort INT DEFAULT 0,
    type INT DEFAULT 1 COMMENT '1主图 2详情图',
    INDEX idx_product_id (product_id)
) COMMENT '商品图片';

DROP TABLE IF EXISTS t_review;
CREATE TABLE t_review (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    order_id BIGINT,
    rating INT DEFAULT 5,
    content TEXT,
    images VARCHAR(1000) COMMENT '图片JSON',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_product_id (product_id)
) COMMENT '商品评价';

-- ==================== 订单与交易 ====================

DROP TABLE IF EXISTS t_order;
CREATE TABLE t_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(30) NOT NULL COMMENT '订单号',
    user_id BIGINT NOT NULL,
    merchant_id BIGINT,
    total_amount INT NOT NULL COMMENT '总金额(分)',
    discount_amount INT DEFAULT 0 COMMENT '优惠金额(分)',
    pay_amount INT NOT NULL COMMENT '实付(分)',
    status INT DEFAULT 0 COMMENT '0待付 1已付/待发 2已发货 3已完成 4已取消 5退款中 6已退款',
    coupon_id BIGINT,
    address_snapshot VARCHAR(1000) COMMENT '地址快照JSON',
    pay_time DATETIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_order_no (order_no),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) COMMENT '订单';

DROP TABLE IF EXISTS t_order_item;
CREATE TABLE t_order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    product_name VARCHAR(200),
    spec_info VARCHAR(200),
    price INT NOT NULL,
    quantity INT NOT NULL,
    product_image VARCHAR(500),
    INDEX idx_order_id (order_id)
) COMMENT '订单明细';

DROP TABLE IF EXISTS t_payment;
CREATE TABLE t_payment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    pay_no VARCHAR(50) NOT NULL,
    amount INT NOT NULL,
    pay_method INT DEFAULT 1 COMMENT '1支付宝 2微信',
    status INT DEFAULT 0 COMMENT '0待支付 1已支付',
    pay_time DATETIME,
    UNIQUE INDEX idx_pay_no (pay_no),
    INDEX idx_order_id (order_id)
) COMMENT '支付记录';

DROP TABLE IF EXISTS t_after_sale;
CREATE TABLE t_after_sale (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    type INT NOT NULL COMMENT '1退款 2退货退款 3换货',
    reason VARCHAR(500),
    amount INT,
    status INT DEFAULT 0 COMMENT '0待审 1通过 2驳回 3完成',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order_id (order_id)
) COMMENT '售后';

-- ==================== 营销 ====================

DROP TABLE IF EXISTS t_coupon_template;
CREATE TABLE t_coupon_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    merchant_id BIGINT,
    name VARCHAR(100) NOT NULL COMMENT '优惠券名称',
    type INT DEFAULT 1 COMMENT '1满减券 2折扣券',
    threshold INT DEFAULT 0 COMMENT '使用门槛(分)',
    value INT NOT NULL COMMENT '优惠值(分)',
    total_count INT DEFAULT 0,
    received_count INT DEFAULT 0,
    start_time DATETIME,
    end_time DATETIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '优惠券模板';

DROP TABLE IF EXISTS t_user_coupon;
CREATE TABLE t_user_coupon (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    template_id BIGINT NOT NULL,
    status INT DEFAULT 0 COMMENT '0未使用 1已使用 2已过期',
    expire_time DATETIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
) COMMENT '用户优惠券';

DROP TABLE IF EXISTS t_seckill_session;
CREATE TABLE t_seckill_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku_id BIGINT NOT NULL,
    seckill_price INT NOT NULL COMMENT '秒杀价(分)',
    stock INT NOT NULL COMMENT '秒杀库存',
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    status INT DEFAULT 0 COMMENT '0未开始 1进行中 2已结束',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '秒杀场次(阶段二使用)';

-- ==================== 示例数据 ====================

-- 类目
INSERT INTO t_category (name, parent_id, level, sort) VALUES
('手机数码', 0, 1, 1),
('电脑办公', 0, 1, 2),
('家用电器', 0, 1, 3),
('服饰鞋包', 0, 1, 4),
('手机', 1, 2, 1),
('平板', 1, 2, 2),
('笔记本', 2, 2, 1),
('台式机', 2, 2, 2),
('空调', 3, 2, 1),
('洗衣机', 3, 2, 2),
('男装', 4, 2, 1),
('女装', 4, 2, 2);

-- 商品
INSERT INTO t_product (merchant_id, category_id, name, description, brand, status, main_image) VALUES
(1, 5, 'CloudPhone X1 5G智能手机', '6.7英寸AMOLED | 5000mAh | 1亿像素 | 骁龙8Gen3', 'CloudTech', 2, ''),
(1, 5, 'CloudPhone SE 青春版', '6.5英寸LCD | 4500mAh | 4800万像素 | 性价比', 'CloudTech', 2, ''),
(1, 5, 'SpeedMate 旗舰手机', '6.8英寸2K曲面屏 | 120W快充 | 徕卡影像', 'SpeedMate', 2, ''),
(1, 7, 'ThinkBook Pro 16 轻薄本', '16英寸2.5K | i7-13700H | 16GB | 1TB', 'ThinkBook', 2, ''),
(1, 7, 'MateBook 14 办公本', '14英寸触控 | i5-13500H | 16GB | 512GB', 'MateBook', 2, ''),
(1, 9, 'CoolAir 变频空调 1.5匹', '一级能效 | 变频冷暖 | 自清洁', 'CoolAir', 2, ''),
(1, 11, '商务休闲夹克 春秋款', '优质面料 | 修身版型 | 多色可选', 'MensWear', 2, ''),
(1, 12, '法式复古连衣裙', '气质收腰 | A字裙摆 | 优雅显瘦', 'LadyStyle', 2, '');

-- SKU
INSERT INTO t_sku (product_id, spec_info, price, original_price, stock) VALUES
(1, '{"颜色":"曜石黑","容量":"256GB"}', 399900, 429900, 100),
(1, '{"颜色":"曜石黑","容量":"512GB"}', 449900, 479900, 80),
(1, '{"颜色":"星云蓝","容量":"256GB"}', 399900, 429900, 60),
(1, '{"颜色":"星云蓝","容量":"512GB"}', 449900, 479900, 50),
(2, '{"颜色":"薄荷绿","容量":"128GB"}', 199900, 219900, 200),
(2, '{"颜色":"深空灰","容量":"128GB"}', 199900, 219900, 150),
(3, '{"颜色":"陶瓷白","容量":"256GB"}', 499900, 529900, 50),
(4, '{"颜色":"深空灰","配置":"16GB+1TB"}', 699900, 749900, 40),
(4, '{"颜色":"银色","配置":"32GB+1TB"}', 799900, 849900, 25),
(5, '{"颜色":"皓月银","配置":"16GB+512GB"}', 549900, 589900, 60),
(6, '{"匹数":"1.5匹","颜色":"白色"}', 299900, 349900, 80),
(7, '{"颜色":"深蓝色","尺码":"L"}', 39900, 59900, 150),
(7, '{"颜色":"卡其色","尺码":"L"}', 39900, 59900, 100),
(8, '{"颜色":"碎花","尺码":"M"}', 29900, 45900, 200);

-- 管理员 (admin/admin123)
INSERT INTO t_admin_user (username, password) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi');

-- 角色
INSERT INTO t_role (role_name, role_code) VALUES
('超级管理员', 'ROLE_SUPER_ADMIN'),
('商家', 'ROLE_MERCHANT'),
('普通用户', 'ROLE_USER');
