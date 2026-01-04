SHOW DATABASES;
CREATE DATABASE inventory_system;
USE inventory_system;
CREATE TABLE products (
                          id VARCHAR(20) PRIMARY KEY,
                          name VARCHAR(50) NOT NULL,
                          category VARCHAR(30) NOT NULL,
                          buy_price DECIMAL(10,2) NOT NULL,
                          sell_price DECIMAL(10,2) NOT NULL,
                          stock INT NOT NULL DEFAULT 0,
                          created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                          updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
CREATE TABLE operation_logs (
                                id VARCHAR(50) PRIMARY KEY,
                                product_id VARCHAR(20),
                                product_name VARCHAR(50),
                                action VARCHAR(20) NOT NULL COMMENT '入库/出库/上架',
                                quantity INT NOT NULL,
                                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,

    -- 外键约束（可选）
                                FOREIGN KEY (product_id) REFERENCES products(id)
);

ALTER TABLE products
    ADD COLUMN shelf_life_days INT DEFAULT 365 COMMENT '保质期天数';

ALTER TABLE products
    ADD COLUMN last_restock_date DATETIME NULL COMMENT '最后入库时间';

CREATE INDEX idx_action_time ON operation_logs(action, timestamp);
CREATE INDEX idx_product_id ON operation_logs(product_id);

INSERT INTO products (id, name, category, buy_price, sell_price, stock)
VALUES ('SP0001', '矿泉水', '饮料', 1.5, 2.0, 100);

INSERT INTO products (id, name, category, buy_price, sell_price, stock) VALUES
                                                                            ('SP0021', '矿泉水', '饮料', 1.50, 2.00, 150),
                                                                            ('SP0022', '可乐', '饮料', 2.80, 4.00, 120),
                                                                            ('SP0023', '绿茶', '饮料', 3.00, 5.00, 90),
                                                                            ('SP0024', '薯片', '零食', 3.50, 7.00, 80),
                                                                            ('SP0025', '巧克力', '零食', 8.00, 15.00, 60),
                                                                            ('SP0026', '方便面', '速食', 4.50, 6.00, 200),
                                                                            ('SP0027', '牙膏', '日用品', 12.00, 18.00, 40),
                                                                            ('SP0028', '纸巾', '日用品', 10.00, 14.00, 70),
                                                                            ('SP0029', '冰淇淋', '冷饮', 5.00, 9.00, 30),
                                                                            ('SP0030', '口香糖', '零食', 1.00, 2.50, 200);

-- 删除旧日志（清空测试环境）
DELETE FROM operation_logs
WHERE timestamp < DATE_SUB(CURDATE(), INTERVAL 30 DAY);  -- 删除30天前的数据

-- 设置变量方便批量插入
SET @start_date = DATE_SUB(CURDATE(), INTERVAL 30 DAY);

-- 矿泉水 SP0001：每天出库 10~15 件
INSERT INTO operation_logs (id, product_id, product_name, action, quantity, timestamp)
SELECT UUID(), 'SP0001', '矿泉水', '出库', FLOOR(RAND() * 6) + 10, DATE_ADD(@start_date, INTERVAL seq HOUR)
FROM (
         SELECT a.N + b.N*10 + c.N*100 AS seq
         FROM (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) a,
              (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) b,
              (SELECT 0 AS N UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) c
     ) seqs
WHERE seq < 720; -- 30天 × 24小时 = 720 小时

-- 入库记录（每周一次大补货）
INSERT INTO operation_logs (id, product_id, product_name, action, quantity, timestamp) VALUES
                                                                                           (UUID(), 'SP0001', '矿泉水', '入库', 100, '2025-12-05'),
                                                                                           (UUID(), 'SP0001', '矿泉水', '入库', 100, '2025-12-12'),
                                                                                           (UUID(), 'SP0001', '矿泉水', '入库', 100, '2025-12-19'),
                                                                                           (UUID(), 'SP0001', '矿泉水', '入库', 100, '2025-12-26');

-- 巧克力 SP0005：每周出库 3~5 次，每次 1~2 件
INSERT INTO operation_logs (id, product_id, product_name, action, quantity, timestamp)
SELECT UUID(), 'SP0005', '巧克力', '出库', FLOOR(RAND() * 2) + 1, DATE_ADD(@start_date, INTERVAL FLOOR(RAND() * 720) HOUR)
FROM (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5) t1,
     (SELECT 1 UNION SELECT 2 UNION SELECT 3) t2;

-- 牙膏 SP0007：每月出库 10 次左右
INSERT INTO operation_logs (id, product_id, product_name, action, quantity, timestamp)
SELECT UUID(), 'SP0007', '牙膏', '出库', 1, DATE_ADD(@start_date, INTERVAL FLOOR(RAND() * 720) HOUR)
FROM (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) t;

-- 冰淇淋 SP0009：仅在周末有少量购买
INSERT INTO operation_logs (id, product_id, product_name, action, quantity, timestamp) VALUES
                                                                                           (UUID(), 'SP0009', '冰淇淋', '出库', 2, '2025-12-06'),
                                                                                           (UUID(), 'SP0009', '冰淇淋', '出库', 1, '2025-12-13'),
                                                                                           (UUID(), 'SP0009', '冰淇淋', '出库', 3, '2025-12-20'),
                                                                                           (UUID(), 'SP0009', '冰淇淋', '出库', 2, '2025-12-27');

-- 最近一次进货是在月初，之后没再补
INSERT INTO operation_logs (id, product_id, product_name, action, quantity, timestamp)
VALUES (UUID(), 'SP0009', '冰淇淋', '入库', 50, '2025-12-01');

INSERT INTO operation_logs (id, product_id, product_name, action, quantity, timestamp)
SELECT UUID(), id, name, '上架', stock, DATE_SUB(CURDATE(), INTERVAL 31 DAY)
FROM products;

ALTER TABLE operation_logs DROP FOREIGN KEY fk_product_id;

ALTER TABLE operation_logs
    ADD CONSTRAINT fk_product_id
        FOREIGN KEY (product_id) REFERENCES products(id)
            ON DELETE SET NULL;

ALTER TABLE operation_logs
    MODIFY COLUMN product_id VARCHAR(20) NULL;

SELECT
    product_id,
    product_name,
    SUM(quantity) AS total_sold
FROM operation_logs
WHERE action = '出库'
  AND timestamp >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
GROUP BY product_id, product_name;

INSERT INTO products (id, name, category, buy_price, sell_price, stock, created_at, updated_at, shelf_life_days, last_restock_date) VALUES
                                                                                                                                        ('SP0070', '矿泉水', '饮料', 1.50, 3.00, 108, '2025-12-30 18:27:08', '2025-12-30 18:27:08', 365, '2025-12-31 09:02:59'),
                                                                                                                                        ('SP0071', '可乐', '饮料', 2.50, 4.00, 56, '2025-12-28 10:15:22', '2025-12-29 14:33:11', 180, '2025-12-30 16:45:33'),
                                                                                                                                        ('SP0072', '苹果', '水果', 4.00, 6.50, 89, '2025-12-25 08:30:45', '2025-12-31 10:12:33', 30, '2025-12-31 08:20:15'),
                                                                                                                                        ('SP0073', '香蕉', '水果', 3.50, 5.50, 67, '2025-12-26 14:22:18', '2025-12-30 17:45:22', 21, '2025-12-30 12:30:45'),
                                                                                                                                        ('SP0074', '牛奶', '蛋奶', 5.00, 8.00, 45, '2025-12-29 09:11:33', '2025-12-31 08:55:27', 7, '2025-12-31 07:30:22'),
                                                                                                                                        ('SP0075', '酸奶', '蛋奶', 3.00, 6.00, 120, '2025-12-27 16:40:11', '2025-12-30 11:22:44', 14, '2025-12-29 15:20:33'),
                                                                                                                                        ('SP0076', '牙膏', '日用品', 8.00, 15.00, 234, '2025-12-20 10:30:55', '2025-12-28 14:22:33', 730, '2025-12-25 09:15:44'),
                                                                                                                                        ('SP0077', '牙刷', '日用品', 2.00, 4.00, 189, '2025-12-18 11:22:44', '2025-12-29 16:33:22', 365, '2025-12-24 10:45:12'),
                                                                                                                                        ('SP0078', '洗发水', '日用品', 18.00, 28.00, 67, '2025-12-15 09:15:33', '2025-12-30 18:22:11', 730, '2025-12-28 14:30:55'),
                                                                                                                                        ('SP0079', '牛肉', '肉类', 35.00, 48.00, 34, '2025-12-22 13:44:22', '2025-12-31 09:12:33', 14, '2025-12-31 06:20:44');

INSERT INTO operation_logs (id, product_id, product_name, action, quantity, timestamp) VALUES
                                                                                           ('a60c1db5-e551-11f0-b1b4-005056c00001', 'SP0070', '矿泉水', '出库', 5, '2025-12-30 10:00:00'),
                                                                                           ('a60c1db6-e551-11f0-b1b4-005056c00002', 'SP0070', '矿泉水', '出库', 3, '2025-12-29 14:00:00'),
                                                                                           ('a60c1db7-e551-11f0-b1b4-005056c00003', 'SP0070', '矿泉水', '出库', 4, '2025-12-28 09:00:00'),
                                                                                           ('a60c1db8-e551-11f0-b1b4-005056c00004', 'SP0070', '矿泉水', '出库', 6, '2025-12-27 11:00:00'),
                                                                                           ('a60c1db9-e551-11f0-b1b4-005056c00005', 'SP0070', '矿泉水', '出库', 2, '2025-12-26 15:00:00'),
                                                                                           ('a60c1dba-e551-11f0-b1b4-005056c00006', 'SP0070', '矿泉水', '出库', 7, '2025-12-25 10:00:00'),
                                                                                           ('a60c1dbb-e551-11f0-b1b4-005056c00007', 'SP0070', '矿泉水', '出库', 4, '2025-12-24 14:00:00'),
                                                                                           ('a60c1dbc-e551-11f0-b1b4-005056c00008', 'SP0070', '矿泉水', '出库', 5, '2025-12-23 09:00:00'),
                                                                                           ('a60c1dbd-e551-11f0-b1b4-005056c00009', 'SP0071', '可乐', '出库', 2, '2025-12-30 11:00:00'),
                                                                                           ('a60c1dbe-e551-11f0-b1b4-005056c0000a', 'SP0071', '可乐', '出库', 3, '2025-12-29 15:00:00'),
                                                                                           ('a60c1dbf-e551-11f0-b1b4-005056c0000b', 'SP0071', '可乐', '出库', 2, '2025-12-28 10:00:00'),
                                                                                           ('a60c1dc0-e551-11f0-b1b4-005056c0000c', 'SP0071', '可乐', '出库', 1, '2025-12-27 16:00:00'),
                                                                                           ('a60c1dc1-e551-11f0-b1b4-005056c0000d', 'SP0071', '可乐', '出库', 3, '2025-12-26 12:00:00'),
                                                                                           ('a60c1dc2-e551-11f0-b1b4-005056c0000e', 'SP0072', '苹果', '出库', 8, '2025-12-31 08:00:00'),
                                                                                           ('a60c1dc3-e551-11f0-b1b4-005056c0000f', 'SP0072', '苹果', '出库', 6, '2025-12-30 10:00:00'),
                                                                                           ('a60c1dc4-e551-11f0-b1b4-005056c00010', 'SP0072', '苹果', '出库', 9, '2025-12-29 09:00:00'),
                                                                                           ('a60c1dc5-e551-11f0-b1b4-005056c00011', 'SP0072', '苹果', '出库', 7, '2025-12-28 11:00:00'),
                                                                                           ('a60c1dc6-e551-11f0-b1b4-005056c00012', 'SP0072', '苹果', '出库', 5, '2025-12-27 14:00:00'),
                                                                                           ('a60c1dc7-e551-11f0-b1b4-005056c00013', 'SP0072', '苹果', '出库', 8, '2025-12-26 10:00:00'),
                                                                                           ('a60c1dc8-e551-11f0-b1b4-005056c00014', 'SP0073', '香蕉', '出库', 6, '2025-12-30 14:00:00'),
                                                                                           ('a60c1dc9-e551-11f0-b1b4-005056c00015', 'SP0073', '香蕉', '出库', 7, '2025-12-29 13:00:00'),
                                                                                           ('a60c1dca-e551-11f0-b1b4-005056c00016', 'SP0073', '香蕉', '出库', 5, '2025-12-28 15:00:00'),
                                                                                           ('a60c1dcb-e551-11f0-b1b4-005056c00017', 'SP0073', '香蕉', '出库', 8, '2025-12-27 12:00:00'),
                                                                                           ('a60c1dcc-e551-11f0-b1b4-005056c00018', 'SP0074', '牛奶', '出库', 5, '2025-12-31 07:00:00'),
                                                                                           ('a60c1dcd-e551-11f0-b1b4-005056c00019', 'SP0074', '牛奶', '出库', 4, '2025-12-30 09:00:00'),
                                                                                           ('a60c1dce-e551-11f0-b1b4-005056c0001a', 'SP0074', '牛奶', '出库', 6, '2025-12-29 08:00:00'),
                                                                                           ('a60c1dcf-e551-11f0-b1b4-005056c0001b', 'SP0074', '牛奶', '出库', 3, '2025-12-28 10:00:00'),
                                                                                           ('a60c1dd0-e551-11f0-b1b4-005056c0001c', 'SP0074', '牛奶', '出库', 5, '2025-12-27 08:00:00'),
                                                                                           ('a60c1dd1-e551-11f0-b1b4-005056c0001d', 'SP0075', '酸奶', '出库', 3, '2025-12-30 16:00:00'),
                                                                                           ('a60c1dd2-e551-11f0-b1b4-005056c0001e', 'SP0075', '酸奶', '出库', 4, '2025-12-29 14:00:00'),
                                                                                           ('a60c1dd3-e551-11f0-b1b4-005056c0001f', 'SP0075', '酸奶', '出库', 2, '2025-12-28 15:00:00'),
                                                                                           ('a60c1dd4-e551-11f0-b1b4-005056c00020', 'SP0076', '牙膏', '出库', 1, '2025-12-28 10:00:00'),
                                                                                           ('a60c1dd5-e551-11f0-b1b4-005056c00021', 'SP0076', '牙膏', '出库', 1, '2025-12-25 11:00:00'),
                                                                                           ('a60c1dd6-e551-11f0-b1b4-005056c00022', 'SP0076', '牙膏', '出库', 1, '2025-12-22 13:00:00'),
                                                                                           ('a60c1dd7-e551-11f0-b1b4-005056c00023', 'SP0077', '牙刷', '出库', 1, '2025-12-29 09:00:00'),
                                                                                           ('a60c1dd8-e551-11f0-b1b4-005056c00024', 'SP0077', '牙刷', '出库', 1, '2025-12-26 10:00:00'),
                                                                                           ('a60c1dd9-e551-11f0-b1b4-005056c00025', 'SP0078', '洗发水', '出库', 2, '2025-12-28 14:00:00'),
                                                                                           ('a60c1dda-e551-11f0-b1b4-005056c00026', 'SP0078', '洗发水', '出库', 1, '2025-12-25 16:00:00'),
                                                                                           ('a60c1ddb-e551-11f0-b1b4-005056c00027', 'SP0079', '牛肉', '出库', 2, '2025-12-30 12:00:00'),
                                                                                           ('a60c1ddc-e551-11f0-b1b4-005056c00028', 'SP0079', '牛肉', '出库', 3, '2025-12-29 11:00:00'),
                                                                                           ('a60c1ddd-e551-11f0-b1b4-005056c00029', 'SP0079', '牛肉', '出库', 1, '2025-12-28 13:00:00');

