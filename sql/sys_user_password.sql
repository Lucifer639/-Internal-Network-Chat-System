/*
 Navicat Premium Data Transfer

 Source Server         : local
 Source Server Type    : MySQL
 Source Server Version : 80032
 Source Host           : localhost:3306
 Source Schema         : pp

 Target Server Type    : MySQL
 Target Server Version : 80032
 File Encoding         : 65001

 Date: 14/03/2024 14:34:32
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_user_password
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_password`;
CREATE TABLE `sys_user_password`  (
  `id` bigint NOT NULL COMMENT '主键id',
  `created_dt` bigint NULL DEFAULT NULL COMMENT '创建时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_dt` bigint NULL DEFAULT NULL COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `status` int NOT NULL COMMENT '逻辑删除字段',
  `version` int NULL DEFAULT NULL COMMENT '乐观锁版本',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `password` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `last_time` bigint NULL DEFAULT NULL COMMENT '上次登录时间',
  `last_ip` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '上次登录ip',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
