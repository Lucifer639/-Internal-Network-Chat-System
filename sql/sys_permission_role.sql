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

 Date: 14/03/2024 14:34:11
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_permission_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_permission_role`;
CREATE TABLE `sys_permission_role`  (
  `id` bigint NOT NULL,
  `created_dt` bigint NULL DEFAULT NULL COMMENT '创建时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_dt` bigint NULL DEFAULT NULL COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `status` int NOT NULL COMMENT '逻辑删除字段',
  `version` int NULL DEFAULT NULL COMMENT '乐观锁版本',
  `permission_id` bigint NOT NULL COMMENT '权限id',
  `role_id` bigint NOT NULL COMMENT '角色id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_permission_role
-- ----------------------------
INSERT INTO `sys_permission_role` VALUES (1, NULL, NULL, NULL, NULL, 1, 1, 1, 1);
INSERT INTO `sys_permission_role` VALUES (2, NULL, NULL, NULL, NULL, 1, 1, 2, 2);
INSERT INTO `sys_permission_role` VALUES (3, NULL, NULL, NULL, NULL, 1, 1, 3, 2);
INSERT INTO `sys_permission_role` VALUES (4, NULL, NULL, NULL, NULL, 1, 1, 4, 2);
INSERT INTO `sys_permission_role` VALUES (5, NULL, NULL, NULL, NULL, 1, 1, 5, 2);
INSERT INTO `sys_permission_role` VALUES (6, NULL, NULL, NULL, NULL, 1, 1, 6, 2);
INSERT INTO `sys_permission_role` VALUES (7, NULL, NULL, NULL, NULL, 1, 1, 2, 3);
INSERT INTO `sys_permission_role` VALUES (8, NULL, NULL, NULL, NULL, 1, 1, 3, 3);
INSERT INTO `sys_permission_role` VALUES (9, NULL, NULL, NULL, NULL, 1, 1, 4, 3);
INSERT INTO `sys_permission_role` VALUES (10, NULL, NULL, NULL, NULL, 1, 1, 5, 3);

SET FOREIGN_KEY_CHECKS = 1;
