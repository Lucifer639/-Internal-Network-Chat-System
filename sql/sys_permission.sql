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

 Date: 14/03/2024 14:34:04
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission`  (
  `id` bigint NOT NULL,
  `created_dt` bigint NULL DEFAULT NULL COMMENT '创建时间',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_dt` bigint NULL DEFAULT NULL COMMENT '更新时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `status` int NOT NULL COMMENT '逻辑删除字段',
  `version` int NULL DEFAULT NULL COMMENT '乐观锁版本',
  `permission_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限码',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '权限描述',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_permission
-- ----------------------------
INSERT INTO `sys_permission` VALUES (1, NULL, NULL, NULL, NULL, 1, 1, 'ALL_PERMISSION', '所有权限');
INSERT INTO `sys_permission` VALUES (2, NULL, NULL, NULL, NULL, 1, 1, 'ADD_GROUP_MEMBER', '添加群成员');
INSERT INTO `sys_permission` VALUES (3, NULL, NULL, NULL, NULL, 1, 1, 'DELETE_GROUP_MEMBER', '删除群成员');
INSERT INTO `sys_permission` VALUES (4, NULL, NULL, NULL, NULL, 1, 1, 'UPDATE_GROUP_MEMBER', '修改群成员');
INSERT INTO `sys_permission` VALUES (5, NULL, NULL, NULL, NULL, 1, 1, 'GROUP_CHAT_SILENCE', '群聊禁言');
INSERT INTO `sys_permission` VALUES (6, NULL, NULL, NULL, NULL, 1, 1, 'DISBAND_GROUP', '解散群');

SET FOREIGN_KEY_CHECKS = 1;
