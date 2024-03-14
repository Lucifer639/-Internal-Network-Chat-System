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

 Date: 14/03/2024 14:33:57
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for pp_group_member
-- ----------------------------
DROP TABLE IF EXISTS `pp_group_member`;
CREATE TABLE `pp_group_member`  (
  `id` bigint NOT NULL,
  `created_dt` bigint NULL DEFAULT NULL COMMENT '创建时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_dt` bigint NULL DEFAULT NULL COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `status` int NOT NULL COMMENT '逻辑删除字段',
  `version` int NULL DEFAULT NULL COMMENT '乐观锁版本',
  `group_id` bigint NULL DEFAULT NULL,
  `user_id` bigint NULL DEFAULT NULL COMMENT '群成员id',
  `level` int NULL DEFAULT NULL COMMENT '0:群主，1:管理员，2:普通成员',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
