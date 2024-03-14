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

 Date: 14/03/2024 14:33:18
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for pp_applicant
-- ----------------------------
DROP TABLE IF EXISTS `pp_applicant`;
CREATE TABLE `pp_applicant`  (
  `id` bigint NOT NULL,
  `created_dt` bigint NULL DEFAULT NULL COMMENT '创建时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_dt` bigint NULL DEFAULT NULL COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `status` int NOT NULL COMMENT '逻辑删除字段',
  `version` int NULL DEFAULT NULL COMMENT '乐观锁版本',
  `applicant_id` bigint NOT NULL COMMENT '当type=0或1时，此字段为申请人id；当type=2时，此字段为群id',
  `user_id` bigint NULL DEFAULT NULL COMMENT '当type=2时，此字段有效，为邀请者id',
  `receive_id` bigint NOT NULL COMMENT '当type=0或2时，此字段为接收者id；但type=1时，此字段为群id',
  `type` int NOT NULL COMMENT '当type=0时，此行表示申请好友；当type=1，此行表示申请入群；当type=2，此行表示邀请入群',
  `agree` int NULL DEFAULT NULL COMMENT '为空表示未处理，为0表示拒绝，为1表示同意',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
