/*
 杨不易呀: https://yby6.com

 Date: 21/05/2023 22:28:20
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for t_order_info
-- ----------------------------
DROP TABLE IF EXISTS `t_order_info`;
CREATE TABLE `t_order_info` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '订单id',
  `title` varchar(256) DEFAULT NULL COMMENT '订单标题',
  `order_no` varchar(50) DEFAULT NULL COMMENT '商户订单编号',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户id',
  `product_id` bigint(20) DEFAULT NULL COMMENT '支付产品id',
  `total_fee` int(11) DEFAULT NULL COMMENT '订单金额(分)',
  `code_url` varchar(50) DEFAULT NULL COMMENT '订单二维码连接',
  `order_status` varchar(10) DEFAULT NULL COMMENT '订单状态',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=172 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for t_payment_info
-- ----------------------------
DROP TABLE IF EXISTS `t_payment_info`;
CREATE TABLE `t_payment_info` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '支付记录id',
  `order_no` varchar(50) DEFAULT NULL COMMENT '商户订单编号',
  `transaction_id` varchar(50) DEFAULT NULL COMMENT '支付系统交易编号',
  `payment_type` varchar(20) DEFAULT NULL COMMENT '支付类型',
  `trade_type` varchar(20) DEFAULT NULL COMMENT '交易类型',
  `trade_state` varchar(50) DEFAULT NULL COMMENT '交易状态',
  `payer_total` int(11) DEFAULT NULL COMMENT '支付金额(分)',
  `content` text COMMENT '通知参数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=62 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for t_product
-- ----------------------------
DROP TABLE IF EXISTS `t_product`;
CREATE TABLE `t_product` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '商Bid',
  `title` varchar(20) DEFAULT NULL COMMENT '商品名称',
  `price` int(11) DEFAULT NULL COMMENT '价格(分)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Table structure for t_refund_info
-- ----------------------------
DROP TABLE IF EXISTS `t_refund_info`;
CREATE TABLE `t_refund_info` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '款单id',
  `order_no` varchar(50) DEFAULT NULL COMMENT '商户订单编号',
  `refund_no` varchar(50) DEFAULT NULL COMMENT '商户退款单编号',
  `refund_id` varchar(50) DEFAULT NULL COMMENT '支付系统退款单号',
  `total_fee` int(11) DEFAULT NULL COMMENT '原订单金额(分)',
  `refund` int(11) DEFAULT NULL COMMENT '退款金额(分)',
  `reason` varchar(50) DEFAULT NULL COMMENT '退款原因',
  `refund_status` varchar(10) DEFAULT NULL COMMENT '退款状态',
  `content_return` text COMMENT '申请退款返回参数',
  `content_notify` text COMMENT '退款结果通知参数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;

use `payment_demo`.`t_product`;

INSERT INTO `payment_demo`.`t_product` (`id`, `title`, `price`, `create_time`, `update_time`) VALUES (1, 'Java课程', 1, '2022-11-13 14:31:00', '2023-05-19 10:52:49');
INSERT INTO `payment_demo`.`t_product` (`id`, `title`, `price`, `create_time`, `update_time`) VALUES (2, '大数据课程', 1, '2022-11-13 14:31:00', '2023-05-19 10:52:49');
INSERT INTO `payment_demo`.`t_product` (`id`, `title`, `price`, `create_time`, `update_time`) VALUES (3, '前端课程', 1, '2022-11-13 14:31:00', '2023-05-19 10:52:49');
INSERT INTO `payment_demo`.`t_product` (`id`, `title`, `price`, `create_time`, `update_time`) VALUES (4, 'UI程', 1, '2022-11-13 14:31:00', '2023-05-19 10:52:49');
INSERT INTO `payment_demo`.`t_product` (`id`, `title`, `price`, `create_time`, `update_time`) VALUES (5, '随机课程', 1, '2022-11-17 04:22:35', '2023-05-19 10:52:49');
INSERT INTO `payment_demo`.`t_product` (`id`, `title`, `price`, `create_time`, `update_time`) VALUES (6, '自定义课程', 1, '2022-11-17 04:23:54', '2023-05-19 10:52:49');
INSERT INTO `payment_demo`.`t_product` (`id`, `title`, `price`, `create_time`, `update_time`) VALUES (7, '全网课程', 1, '2022-11-18 02:52:30', '2023-05-19 10:52:49');
INSERT INTO `payment_demo`.`t_product` (`id`, `title`, `price`, `create_time`, `update_time`) VALUES (8, '测试支付', 1, '2023-05-19 10:52:05', '2023-05-19 10:52:05');
INSERT INTO `payment_demo`.`t_product` (`id`, `title`, `price`, `create_time`, `update_time`) VALUES (9, 'Java课程', 10, '2023-05-19 10:52:56', '2023-05-19 10:53:09');
INSERT INTO `payment_demo`.`t_product` (`id`, `title`, `price`, `create_time`, `update_time`) VALUES (10, '大数据课程', 20, '2023-05-19 10:52:56', '2023-05-19 10:53:11');