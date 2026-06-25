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

CREATE TABLE t_merchant (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT COMMENT '关联用户ID',
    shop_name VARCHAR(100) NOT NULL,
    logo VARCHAR(500),
    status INT DEFAULT 0 COMMENT '0待审 1通过 2驳回',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '商家';

CREATE TABLE t_admin_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(200) NOT NULL,
    status INT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_username (username)
) COMMENT '管理员';

CREATE TABLE t_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL,
    role_code VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    UNIQUE INDEX idx_code (role_code)
) COMMENT '角色';

CREATE TABLE t_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    perm_name VARCHAR(50) NOT NULL,
    perm_code VARCHAR(100) NOT NULL,
    parent_id BIGINT DEFAULT 0,
    type INT DEFAULT 1 COMMENT '1菜单 2按钮',
    sort INT DEFAULT 0
) COMMENT '权限';

CREATE TABLE t_role_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    perm_id BIGINT NOT NULL
) COMMENT '角色-权限';

CREATE TABLE t_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL
) COMMENT '用户-角色';


CREATE TABLE t_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    parent_id BIGINT DEFAULT 0,
    level INT DEFAULT 1,
    sort INT DEFAULT 0,
    INDEX idx_parent_id (parent_id)
) COMMENT '商品类目';

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

CREATE TABLE t_product_image (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    url VARCHAR(500) NOT NULL,
    sort INT DEFAULT 0,
    type INT DEFAULT 1 COMMENT '1主图 2详情图',
    INDEX idx_product_id (product_id)
) COMMENT '商品图片';

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

CREATE TABLE t_user_coupon (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    template_id BIGINT NOT NULL,
    status INT DEFAULT 0 COMMENT '0未使用 1已使用 2已过期',
    expire_time DATETIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
) COMMENT '用户优惠券';

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








