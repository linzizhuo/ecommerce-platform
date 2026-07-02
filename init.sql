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

-- ==================== 外键约束 ====================

ALTER TABLE t_user_address ADD CONSTRAINT fk_addr_user FOREIGN KEY (user_id) REFERENCES t_user(id);
ALTER TABLE t_merchant ADD CONSTRAINT fk_merchant_user FOREIGN KEY (user_id) REFERENCES t_user(id);
ALTER TABLE t_product ADD CONSTRAINT fk_product_merchant FOREIGN KEY (merchant_id) REFERENCES t_merchant(id);
ALTER TABLE t_product ADD CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES t_category(id);
ALTER TABLE t_sku ADD CONSTRAINT fk_sku_product FOREIGN KEY (product_id) REFERENCES t_product(id);
ALTER TABLE t_review ADD CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES t_user(id);
ALTER TABLE t_review ADD CONSTRAINT fk_review_product FOREIGN KEY (product_id) REFERENCES t_product(id);
ALTER TABLE t_review ADD CONSTRAINT fk_review_order FOREIGN KEY (order_id) REFERENCES t_order(id);
ALTER TABLE t_order ADD CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES t_user(id);
ALTER TABLE t_order_item ADD CONSTRAINT fk_oi_order FOREIGN KEY (order_id) REFERENCES t_order(id);
ALTER TABLE t_order_item ADD CONSTRAINT fk_oi_sku FOREIGN KEY (sku_id) REFERENCES t_sku(id);
ALTER TABLE t_payment ADD CONSTRAINT fk_pay_order FOREIGN KEY (order_id) REFERENCES t_order(id);
ALTER TABLE t_after_sale ADD CONSTRAINT fk_as_order FOREIGN KEY (order_id) REFERENCES t_order(id);
ALTER TABLE t_after_sale ADD CONSTRAINT fk_as_user FOREIGN KEY (user_id) REFERENCES t_user(id);
ALTER TABLE t_coupon_template ADD CONSTRAINT fk_ct_merchant FOREIGN KEY (merchant_id) REFERENCES t_merchant(id);
ALTER TABLE t_user_coupon ADD CONSTRAINT fk_uc_user FOREIGN KEY (user_id) REFERENCES t_user(id);
ALTER TABLE t_user_coupon ADD CONSTRAINT fk_uc_template FOREIGN KEY (template_id) REFERENCES t_coupon_template(id);
ALTER TABLE t_seckill_session ADD CONSTRAINT fk_ss_sku FOREIGN KEY (sku_id) REFERENCES t_sku(id);

-- ==================== 示例数据 ====================

-- 测试用户 (密码都是123456的BCrypt)
INSERT INTO t_user (phone, password, nickname) VALUES
('13800001111', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '测试用户');

-- 商家 (关联用户id=1)
INSERT INTO t_merchant (user_id, shop_name, status) VALUES
(1, 'CloudTech官方旗舰店', 1);

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

-- ==================== 用户收藏 ====================
DROP TABLE IF EXISTS t_user_favorite;
CREATE TABLE t_user_favorite (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX uk_user_product (user_id, product_id)
) COMMENT '用户收藏';

-- ==================== 积分与等级 ====================
DROP TABLE IF EXISTS t_user_point;
CREATE TABLE t_user_point (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    total_point INT DEFAULT 0 COMMENT '总积分',
    available_point INT DEFAULT 0 COMMENT '可用积分',
    INDEX idx_user_id (user_id)
) COMMENT '用户积分';

DROP TABLE IF EXISTS t_point_log;
CREATE TABLE t_point_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    point INT NOT NULL COMMENT '变动积分(正=获得,负=消费)',
    type INT NOT NULL COMMENT '1下单 2签到 3兑换 4过期',
    description VARCHAR(200),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_time (user_id, create_time)
) COMMENT '积分流水';

DROP TABLE IF EXISTS t_member_level;
CREATE TABLE t_member_level (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    level_name VARCHAR(50) NOT NULL COMMENT '等级名称',
    level_code INT NOT NULL UNIQUE COMMENT '等级编码(0普通/1银/2金/3钻石)',
    min_amount INT NOT NULL COMMENT '最低消费额(分)',
    discount_rate INT DEFAULT 100 COMMENT '折扣率(百分制,95=9.5折)',
    free_shipping INT DEFAULT 0 COMMENT '是否免运费',
    description VARCHAR(200)
) COMMENT '会员等级';

-- ==================== 消息通知 ====================
DROP TABLE IF EXISTS t_message;
CREATE TABLE t_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    type INT DEFAULT 1 COMMENT '1系统 2订单 3促销 4售后',
    is_read INT DEFAULT 0 COMMENT '0未读 1已读',
    order_id BIGINT COMMENT '关联订单ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_read (user_id, is_read)
) COMMENT '消息/站内信';

-- ==================== 物流 ====================
DROP TABLE IF EXISTS t_logistics;
CREATE TABLE t_logistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    company VARCHAR(50) COMMENT '快递公司',
    tracking_no VARCHAR(50) COMMENT '快递单号',
    trace_data TEXT COMMENT '物流轨迹JSON',
    status INT DEFAULT 0 COMMENT '0待揽收 1运输中 2派送中 3已签收',
    ship_time DATETIME,
    sign_time DATETIME,
    INDEX idx_order_id (order_id),
    INDEX idx_tracking_no (tracking_no)
) COMMENT '物流信息';

-- ==================== 订单日志 ====================
DROP TABLE IF EXISTS t_order_log;
CREATE TABLE t_order_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    operator_id BIGINT COMMENT '操作人ID',
    operator_type INT COMMENT '1用户 2商家 3系统',
    action VARCHAR(100) NOT NULL COMMENT '操作描述',
    old_status INT,
    new_status INT,
    remark VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order_id (order_id)
) COMMENT '订单操作日志';

-- ==================== 库存流水 ====================
DROP TABLE IF EXISTS t_stock_log;
CREATE TABLE t_stock_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku_id BIGINT NOT NULL,
    order_id BIGINT,
    before_stock INT NOT NULL,
    change_count INT NOT NULL COMMENT '变动数量(负=扣减)',
    after_stock INT NOT NULL,
    type INT COMMENT '1下单锁库存 2支付扣减 3取消回滚 4商家修改',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_sku_id (sku_id)
) COMMENT '库存流水';

-- ==================== 搜索热词 ====================
DROP TABLE IF EXISTS t_search_hot_word;
CREATE TABLE t_search_hot_word (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    keyword VARCHAR(100) NOT NULL UNIQUE,
    search_count INT DEFAULT 0 COMMENT '搜索次数',
    is_manual INT DEFAULT 0 COMMENT '0自动 1人工设置',
    sort INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '搜索热词';

-- ==================== 活动 ====================
DROP TABLE IF EXISTS t_activity;
CREATE TABLE t_activity (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    type INT NOT NULL COMMENT '1限时折扣 2拼团 3秒杀 4预售 5满减',
    rules TEXT COMMENT '活动规则JSON',
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    status INT DEFAULT 0 COMMENT '0草稿 1待审 2进行中 3已结束',
    merchant_id BIGINT COMMENT '商家ID(平台活动为NULL)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_time (start_time, end_time),
    INDEX idx_status (status)
) COMMENT '营销活动';

DROP TABLE IF EXISTS t_activity_product;
CREATE TABLE t_activity_product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    activity_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    sku_id BIGINT,
    activity_price INT COMMENT '活动价(分)',
    stock INT COMMENT '活动库存',
    INDEX idx_activity (activity_id)
) COMMENT '活动商品关联';

-- ==================== 拼团 ====================
DROP TABLE IF EXISTS t_group_buy;
CREATE TABLE t_group_buy (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    activity_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    leader_id BIGINT NOT NULL COMMENT '团长用户ID',
    required_count INT DEFAULT 2 COMMENT '成团人数',
    current_count INT DEFAULT 1 COMMENT '当前人数',
    group_price INT NOT NULL COMMENT '拼团价(分)',
    status INT DEFAULT 0 COMMENT '0拼团中 1已成团 2失败',
    expire_time DATETIME COMMENT '过期时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_activity (activity_id)
) COMMENT '拼团';

DROP TABLE IF EXISTS t_group_buy_member;
CREATE TABLE t_group_buy_member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    order_id BIGINT COMMENT '关联订单',
    is_leader INT DEFAULT 0 COMMENT '0团员 1团长',
    join_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_group (group_id)
) COMMENT '拼团成员';

-- ==================== 预售 ====================
DROP TABLE IF EXISTS t_presale;
CREATE TABLE t_presale (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    activity_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    deposit INT NOT NULL COMMENT '定金(分)',
    final_amount INT NOT NULL COMMENT '尾款(分)',
    deposit_start DATETIME COMMENT '定金开始时间',
    deposit_end DATETIME COMMENT '定金结束时间',
    final_start DATETIME COMMENT '尾款开始时间',
    final_end DATETIME COMMENT '尾款结束时间',
    status INT DEFAULT 0 COMMENT '0未开始 1定金期 2尾款期 3结束',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '预售';

-- ==================== 组合套餐 ====================
DROP TABLE IF EXISTS t_combo_package;
CREATE TABLE t_combo_package (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    total_price INT NOT NULL COMMENT '套餐总价(分)',
    original_price INT COMMENT '原价合计(分)',
    merchant_id BIGINT,
    status INT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '组合套餐';

DROP TABLE IF EXISTS t_combo_item;
CREATE TABLE t_combo_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    combo_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    quantity INT DEFAULT 1,
    INDEX idx_combo (combo_id)
) COMMENT '套餐商品明细';

-- ==================== 分销 ====================
DROP TABLE IF EXISTS t_distribution;
CREATE TABLE t_distribution (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE COMMENT '分销员用户ID',
    parent_id BIGINT COMMENT '上级分销员',
    total_commission INT DEFAULT 0 COMMENT '累计佣金(分)',
    available_commission INT DEFAULT 0,
    level INT DEFAULT 0 COMMENT '分销等级',
    status INT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '分销员';

-- ==================== 公告与配置 ====================
DROP TABLE IF EXISTS t_notice;
CREATE TABLE t_notice (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    type INT DEFAULT 1 COMMENT '1系统公告 2活动公告 3弹窗',
    is_top INT DEFAULT 0 COMMENT '是否置顶',
    status INT DEFAULT 1 COMMENT '0隐藏 1显示',
    start_time DATETIME,
    end_time DATETIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '公告';

DROP TABLE IF EXISTS t_system_config;
CREATE TABLE t_system_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    description VARCHAR(200) COMMENT '说明',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '系统配置';

-- ==================== 统计表 ====================
DROP TABLE IF EXISTS t_stat_daily;
CREATE TABLE t_stat_daily (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stat_date DATE NOT NULL,
    merchant_id BIGINT,
    order_count INT DEFAULT 0 COMMENT '订单数',
    order_amount BIGINT DEFAULT 0 COMMENT '订单金额(分)',
    pay_count INT DEFAULT 0 COMMENT '支付笔数',
    pay_amount BIGINT DEFAULT 0 COMMENT '支付金额(分)',
    new_user_count INT DEFAULT 0 COMMENT '新增用户',
    visit_count INT DEFAULT 0 COMMENT '访问量PV',
    visit_user_count INT DEFAULT 0 COMMENT '访客数UV',
    UNIQUE INDEX uk_date_merchant (stat_date, merchant_id)
) COMMENT '每日统计';

-- ==================== 红包 ====================
DROP TABLE IF EXISTS t_red_envelope;
CREATE TABLE t_red_envelope (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT NOT NULL COMMENT '发送人用户ID',
    receiver_id BIGINT COMMENT '接收人用户ID',
    order_id BIGINT COMMENT '关联订单ID',
    amount INT NOT NULL COMMENT '金额(分)',
    type INT DEFAULT 1 COMMENT '1普通红包 2订单返利红包',
    status INT DEFAULT 0 COMMENT '0未领取 1已领取 2已过期',
    message VARCHAR(200) COMMENT '祝福语',
    expire_time DATETIME COMMENT '过期时间(默认24小时)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    receive_time DATETIME,
    INDEX idx_sender (sender_id),
    INDEX idx_receiver (receiver_id)
) COMMENT '红包';

-- ==================== 数据字典 ====================
DROP TABLE IF EXISTS t_dict_type;
CREATE TABLE t_dict_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dict_name VARCHAR(100) NOT NULL COMMENT '字典名称',
    dict_code VARCHAR(100) NOT NULL UNIQUE COMMENT '字典编码',
    description VARCHAR(200) COMMENT '描述',
    status INT DEFAULT 1 COMMENT '1启用 0停用',
    INDEX idx_code (dict_code)
) COMMENT '字典类型';

DROP TABLE IF EXISTS t_dict_item;
CREATE TABLE t_dict_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dict_type_id BIGINT NOT NULL COMMENT '字典类型ID',
    label VARCHAR(100) NOT NULL COMMENT '字典标签',
    value VARCHAR(100) NOT NULL COMMENT '字典值',
    sort INT DEFAULT 0 COMMENT '排序',
    css_class VARCHAR(50) COMMENT 'CSS样式类',
    status INT DEFAULT 1 COMMENT '1启用 0停用',
    INDEX idx_type (dict_type_id)
) COMMENT '字典项';

-- ==================== 违规处罚 ====================
DROP TABLE IF EXISTS t_violation;
CREATE TABLE t_violation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    merchant_id BIGINT NOT NULL COMMENT '商家ID',
    type INT NOT NULL COMMENT '1商品违规 2虚假发货 3欺诈',
    reason VARCHAR(500) COMMENT '违规原因',
    penalty_type INT COMMENT '1警告 2罚款 3下架商品 4封店',
    penalty_amount INT DEFAULT 0 COMMENT '罚金(分)',
    status INT DEFAULT 0 COMMENT '0待执行 1已执行 2已申诉',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_merchant (merchant_id)
) COMMENT '违规处罚';

-- ==================== 会员等级初始数据 ====================
INSERT INTO t_member_level (level_name, level_code, min_amount, discount_rate, free_shipping) VALUES
('普通会员', 0, 0, 100, 0),
('银卡会员', 1, 50000, 98, 0),
('金卡会员', 2, 200000, 95, 1),
('钻石会员', 3, 500000, 90, 1);

-- 系统配置初始数据
INSERT INTO t_system_config (config_key, config_value, description) VALUES
('order_auto_cancel_minutes', '30', '未支付订单自动取消时间(分钟)'),
('point_rate', '100', '消费积分比例(消费多少分得1积分)'),
('free_shipping_threshold', '9900', '免运费门槛(分)'),
('seckill_default_qps', '100', '秒杀默认QPS限制');

-- ==================== 活动会场 ====================
DROP TABLE IF EXISTS t_venue;
CREATE TABLE t_venue (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '会场名称',
    description VARCHAR(500) COMMENT '会场描述',
    page_config TEXT COMMENT '页面配置JSON: banner/product_ids/coupon_ids/background等',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    status INT DEFAULT 0 COMMENT '0草稿 1已发布 2已下线',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '活动会场';

-- ==================== 用户标签/画像 ====================
DROP TABLE IF EXISTS t_user_tag;
CREATE TABLE t_user_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    tag_name VARCHAR(50) COMMENT '标签名: spending_tier/fav_category/avg_order/recent_freq',
    tag_value VARCHAR(100) COMMENT '标签值',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id)
) COMMENT '用户标签画像';
