-- =============================================
-- CloudMall MV架构 - 数据库初始化脚本
-- 分支: mv-architecture
-- 数据库: ecommerce (单库)
-- =============================================

CREATE DATABASE IF NOT EXISTS ecommerce DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ecommerce;

-- ---------- 类目表 ----------
DROP TABLE IF EXISTS t_category;
CREATE TABLE t_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT '类目名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父类目ID',
    level INT DEFAULT 1 COMMENT '层级',
    sort INT DEFAULT 0 COMMENT '排序',
    INDEX idx_parent_id (parent_id)
) COMMENT '商品类目';

-- ---------- 商品表(SPU) ----------
DROP TABLE IF EXISTS t_product;
CREATE TABLE t_product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT COMMENT '类目ID',
    name VARCHAR(200) NOT NULL COMMENT '商品名称',
    description TEXT COMMENT '商品描述',
    brand VARCHAR(100) COMMENT '品牌',
    status INT DEFAULT 1 COMMENT '状态 0下架 1上架',
    main_image VARCHAR(500) COMMENT '主图URL',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_category_id (category_id),
    INDEX idx_status (status)
) COMMENT '商品SPU';

-- ---------- SKU表 ----------
DROP TABLE IF EXISTS t_sku;
CREATE TABLE t_sku (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL COMMENT '商品ID',
    spec_info VARCHAR(200) COMMENT '规格信息JSON',
    price INT NOT NULL COMMENT '价格(分)',
    original_price INT COMMENT '原价(分)',
    image VARCHAR(500) COMMENT 'SKU图片',
    stock INT DEFAULT 0 COMMENT '库存',
    INDEX idx_product_id (product_id)
) COMMENT 'SKU';

-- ---------- 用户表 ----------
DROP TABLE IF EXISTS t_user;
CREATE TABLE t_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    password VARCHAR(200) NOT NULL COMMENT '密码',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar VARCHAR(500) COMMENT '头像',
    status INT DEFAULT 1 COMMENT '状态 0禁用 1正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_phone (phone)
) COMMENT '用户';

-- ---------- 收货地址表 (MVC新增) ----------
DROP TABLE IF EXISTS t_user_address;
CREATE TABLE t_user_address (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    receiver VARCHAR(50) NOT NULL COMMENT '收货人',
    phone VARCHAR(20) NOT NULL COMMENT '联系电话',
    province VARCHAR(50) COMMENT '省',
    city VARCHAR(50) COMMENT '市',
    district VARCHAR(50) COMMENT '区',
    detail VARCHAR(200) COMMENT '详细地址',
    is_default INT DEFAULT 0 COMMENT '是否默认',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
) COMMENT '收货地址';

-- ========== 示例数据 ==========

-- 商品类目
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

-- 商品 SPU
INSERT INTO t_product (category_id, name, description, brand, status, main_image) VALUES
(5, 'CloudPhone X1 5G智能手机', '6.7英寸AMOLED屏幕 | 5000mAh大电池 | 1亿像素主摄 | 骁龙8Gen3处理器', 'CloudTech', 1, ''),
(5, 'CloudPhone SE 青春版', '6.5英寸LCD屏幕 | 4500mAh电池 | 4800万像素 | 性价比之选', 'CloudTech', 1, ''),
(5, 'SpeedMate 旗舰手机', '6.8英寸2K曲面屏 | 120W快充 | 徕卡影像', 'SpeedMate', 1, ''),
(7, 'ThinkBook Pro 16 轻薄本', '16英寸2.5K屏 | i7-13700H | 16GB内存 | 1TB固态', 'ThinkBook', 1, ''),
(7, 'MateBook 14 办公本', '14英寸触控屏 | i5-13500H | 16GB | 512GB', 'MateBook', 1, ''),
(9, 'CoolAir 变频空调 1.5匹', '一级能效 | 变频冷暖 | 自清洁 | 低噪音', 'CoolAir', 1, ''),
(11, '商务休闲夹克 春秋款', '优质面料 | 修身版型 | 多色可选 | 商务休闲两不误', 'MensWear', 1, ''),
(12, '法式复古连衣裙', '气质收腰 | A字裙摆 | 优雅显瘦 | 适合春夏', 'LadyStyle', 1, '');

-- 商品 SKU
INSERT INTO t_sku (product_id, spec_info, price, original_price, stock) VALUES
-- CloudPhone X1: 颜色+容量组合
(1, '{"颜色":"曜石黑","容量":"256GB"}', 399900, 429900, 100),
(1, '{"颜色":"曜石黑","容量":"512GB"}', 449900, 479900, 80),
(1, '{"颜色":"星云蓝","容量":"256GB"}', 399900, 429900, 60),
(1, '{"颜色":"星云蓝","容量":"512GB"}', 449900, 479900, 50),
-- CloudPhone SE
(2, '{"颜色":"薄荷绿","容量":"128GB"}', 199900, 219900, 200),
(2, '{"颜色":"深空灰","容量":"128GB"}', 199900, 219900, 150),
(2, '{"颜色":"深空灰","容量":"256GB"}', 229900, 249900, 120),
-- SpeedMate 旗舰
(3, '{"颜色":"陶瓷白","容量":"256GB"}', 499900, 529900, 50),
(3, '{"颜色":"陶瓷白","容量":"512GB"}', 549900, 579900, 30),
-- ThinkBook Pro 16
(4, '{"颜色":"深空灰","配置":"16GB+1TB"}', 699900, 749900, 40),
(4, '{"颜色":"银色","配置":"32GB+1TB"}', 799900, 849900, 25),
-- MateBook 14
(5, '{"颜色":"皓月银","配置":"16GB+512GB"}', 549900, 589900, 60),
(5, '{"颜色":"深空灰","配置":"16GB+1TB"}', 599900, 639900, 35),
-- CoolAir 空调
(6, '{"匹数":"1.5匹","颜色":"白色"}', 299900, 349900, 80),
-- 夹克
(7, '{"颜色":"深蓝色","尺码":"L"}', 39900, 59900, 150),
(7, '{"颜色":"深蓝色","尺码":"XL"}', 39900, 59900, 120),
(7, '{"颜色":"卡其色","尺码":"L"}', 39900, 59900, 100),
-- 连衣裙
(8, '{"颜色":"碎花","尺码":"M"}', 29900, 45900, 200),
(8, '{"颜色":"碎花","尺码":"L"}', 29900, 45900, 180),
(8, '{"颜色":"纯色黑","尺码":"M"}', 25900, 39900, 160);
