-- 1. 组织表 sys_org
CREATE TABLE `sys_org` (
                           `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键自增ID',
                           `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '上级组织ID，顶级组织默认0',
                           `org_name` varchar(50) NOT NULL COMMENT '组织名称，同级唯一',
                           `create_user` bigint DEFAULT NULL COMMENT '创建人用户ID',
                           `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `update_user` bigint DEFAULT NULL COMMENT '更新人用户ID',
                           `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常、1已删除',
                           PRIMARY KEY (`id`),
                           KEY idx_parent_id (`parent_id`),
                           KEY idx_is_delete (`is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组织表';

create table sys_org(

)
-- 2. 用户表 sys_user
CREATE TABLE `sys_user` (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键自增ID',
                            `login_account` varchar(50) NOT NULL COMMENT '登录账号，全局唯一',
                            `password` varchar(100) NOT NULL COMMENT 'MD5加盐加密密码，不存明文',
                            `phone` varchar(11) DEFAULT NULL COMMENT '手机号，用于密码重置接收验证码',
                            `user_name` varchar(30) NOT NULL COMMENT '用户姓名',
                            `org_id` bigint NOT NULL COMMENT '所属组织ID，关联sys_org.id',
                            `user_status` tinyint NOT NULL DEFAULT 0 COMMENT '账号状态：0正常、1禁用',
                            `lock_time` datetime DEFAULT NULL COMMENT '账号锁定到期时间，5次输错锁定',
                            `create_user` bigint DEFAULT NULL COMMENT '创建人用户ID',
                            `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_user` bigint DEFAULT NULL COMMENT '更新人用户ID',
                            `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常、1已删除',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY uk_login_account (`login_account`),
                            KEY idx_org_id (`org_id`),
                            KEY idx_is_delete (`is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 3. 角色表 sys_role
CREATE TABLE `sys_role` (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                            `role_name` varchar(50) NOT NULL COMMENT '角色名称',
                            `role_type` tinyint NOT NULL DEFAULT 1 COMMENT '角色类型：0内置不可删、1自定义',
                            `menu_ids` varchar(500) DEFAULT NULL COMMENT '绑定菜单ID集合',
                            `data_scope` tinyint DEFAULT NULL COMMENT '数据权限范围标识',
                            `create_user` bigint DEFAULT NULL COMMENT '创建人ID',
                            `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_user` bigint DEFAULT NULL COMMENT '更新人ID',
                            `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                            PRIMARY KEY (`id`),
                            KEY idx_is_delete (`is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 4. 用户角色关联表 sys_user_role
CREATE TABLE `sys_user_role` (
                                 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                 `user_id` bigint NOT NULL COMMENT '用户ID，关联sys_user.id',
                                 `role_id` bigint NOT NULL COMMENT '角色ID，关联sys_role.id',
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY uk_user_role (`user_id`,`role_id`),
                                 KEY idx_role_id (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 5. 菜单权限表 sys_menu
CREATE TABLE `sys_menu` (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                            `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父菜单ID，顶级为0',
                            `menu_name` varchar(50) NOT NULL COMMENT '菜单名称',
                            `route` varchar(100) DEFAULT NULL COMMENT '前端路由标识',
                            `button_perms` varchar(500) DEFAULT NULL COMMENT '按钮权限标识集合',
                            `sort` int DEFAULT 0 COMMENT '菜单排序序号',
                            `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                            PRIMARY KEY (`id`),
                            KEY idx_parent_id (`parent_id`),
                            KEY idx_is_delete (`is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

-- 6. 文件资源表 sys_file
CREATE TABLE `sys_file` (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                            `business_type` varchar(30) DEFAULT NULL COMMENT '业务类型（素材/签名/试题等）',
                            `business_id` bigint DEFAULT NULL COMMENT '关联业务数据ID',
                            `file_name` varchar(100) NOT NULL COMMENT '文件原始名称',
                            `file_url` varchar(255) NOT NULL COMMENT '文件存储访问地址',
                            `file_size` bigint DEFAULT 0 COMMENT '文件大小（字节）',
                            `suffix` varchar(20) DEFAULT NULL COMMENT '文件后缀格式',
                            `upload_user_id` bigint DEFAULT NULL COMMENT '上传人用户ID',
                            `upload_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
                            `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                            PRIMARY KEY (`id`),
                            KEY idx_business (`business_type`,`business_id`),
                            KEY idx_is_delete (`is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件资源表';

-- 7. 系统配置表 sys_config
CREATE TABLE `sys_config` (
                              `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                              `config_key` varchar(50) NOT NULL COMMENT '配置唯一标识Key',
                              `config_value` varchar(500) DEFAULT NULL COMMENT '配置对应值',
                              `config_desc` varchar(100) DEFAULT NULL COMMENT '配置功能说明',
                              `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                              `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                              PRIMARY KEY (`id`),
                              UNIQUE KEY uk_config_key (`config_key`),
                              KEY idx_is_delete (`is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 8. 登录日志表 sys_login_log
CREATE TABLE `sys_login_log` (
                                 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                 `user_id` bigint DEFAULT NULL COMMENT '登录用户ID',
                                 `login_account` varchar(50) NOT NULL COMMENT '登录账号',
                                 `login_ip` varchar(30) DEFAULT NULL COMMENT '登录设备IP',
                                 `login_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
                                 `logout_time` datetime DEFAULT NULL COMMENT '退出登录时间',
                                 `login_status` tinyint NOT NULL COMMENT '登录状态：0失败、1成功',
                                 `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                                 PRIMARY KEY (`id`),
                                 KEY idx_user_id (`user_id`),
                                 KEY idx_login_time (`login_time`),
                                 KEY idx_is_delete (`is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

-- 9. 操作日志表 sys_oper_log
CREATE TABLE `sys_oper_log` (
                                `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                `oper_user_id` bigint DEFAULT NULL COMMENT '操作人用户ID',
                                `oper_user_name` varchar(30) DEFAULT NULL COMMENT '操作人姓名',
                                `oper_org_id` bigint DEFAULT NULL COMMENT '操作人所属组织ID',
                                `module` varchar(50) DEFAULT NULL COMMENT '操作所属模块',
                                `oper_type` varchar(30) DEFAULT NULL COMMENT '操作类型（新增/编辑/删除/导入/导出）',
                                `oper_detail` text DEFAULT NULL COMMENT '操作详细描述',
                                `oper_ip` varchar(30) DEFAULT NULL COMMENT '操作设备IP',
                                `oper_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
                                `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                                PRIMARY KEY (`id`),
                                KEY idx_oper_user_id (`oper_user_id`),
                                KEY idx_oper_time (`oper_time`),
                                KEY idx_is_delete (`is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';



CREATE TABLE `sys_role_menu` (
                                 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                 `role_id` bigint NOT NULL COMMENT '角色ID，关联sys_role.id',
                                 `menu_id` bigint NOT NULL COMMENT '菜单ID，关联sys_menu.id',
                                 `create_user` bigint DEFAULT NULL COMMENT '创建人ID',
                                 `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `update_user` bigint DEFAULT NULL COMMENT '更新人ID',
                                 `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常，1删除',
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY uk_role_menu (`role_id`,`menu_id`) COMMENT '防止同一角色重复绑定同一个菜单',
                                 KEY idx_role_id (`role_id`),
                                 KEY idx_menu_id (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';


CREATE TABLE `sys_dict` (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                            `dict_type` varchar(50) NOT NULL COMMENT '字典类型编码',
                            `dict_label` varchar(100) NOT NULL COMMENT '前端展示名称',
                            `dict_value` varchar(100) NOT NULL COMMENT '后端存储值',
                            `sort` int DEFAULT 0 COMMENT '排序序号',
                            `create_user` bigint DEFAULT NULL COMMENT '创建人ID',
                            `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_user` bigint DEFAULT NULL COMMENT '更新人ID',
                            `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常、1已删除',
                            PRIMARY KEY (`id`),
                            KEY idx_dict_type (`dict_type`),
                            KEY idx_is_delete (`is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统字典表';

-- 测试初始化数据（示例：账号状态字典）
INSERT INTO sys_dict (dict_type,dict_label,dict_value,sort,create_user,create_time,update_user,update_time,is_delete)
VALUES ('user_status','正常','0',1,1,NOW(),1,NOW(),0),
       ('user_status','禁用','1',2,1,NOW(),1,NOW(),0);


CREATE TABLE `sys_async_export` (
                                    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                    `user_id` bigint NOT NULL COMMENT '操作人ID，关联sys_user.id',
                                    `file_id` bigint DEFAULT NULL COMMENT '生成文件ID，关联sys_file.id',
                                    `task_status` tinyint NOT NULL DEFAULT 0 COMMENT '任务状态：0处理中 1完成 2失败',
                                    `export_params` text DEFAULT NULL COMMENT '导出筛选参数(JSON字符串)',
                                    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                    `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常、1已删除',
                                    PRIMARY KEY (`id`),
                                    KEY idx_user_id (`user_id`),
                                    KEY idx_file_id (`file_id`),
                                    KEY idx_task_status (`task_status`),
                                    KEY idx_is_delete (`is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='异步导出任务表';

CREATE TABLE `sys_message` (
                               `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                               `receive_user_id` bigint NOT NULL COMMENT '接收消息用户ID',
                               `business_type` varchar(30) NOT NULL COMMENT '业务类型：learnTask/examPaper/manual',
                               `business_id` bigint DEFAULT NULL COMMENT '关联业务ID',
                               `title` varchar(100) NOT NULL COMMENT '消息标题',
                               `content` varchar(500) DEFAULT NULL COMMENT '消息内容',
                               `read_flag` tinyint NOT NULL DEFAULT 0 COMMENT '已读标记：0未读 1已读',
                               `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                               PRIMARY KEY (`id`),
                               KEY idx_receive_user (`receive_user_id`),
                               KEY idx_read_flag (`read_flag`),
                               KEY idx_is_delete (`is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内消息表';

-- ... existing code ...

-- ====================== 知识库分类表 kb_category ======================
CREATE TABLE `kb_category` (
                               `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键自增ID',
                               `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '上级分类ID，顶级分类默认0',
                               `category_name` varchar(50) NOT NULL COMMENT '分类名称，同级唯一',
                               `sort_order` int DEFAULT 0 COMMENT '排序序号',
                               `create_user` bigint DEFAULT NULL COMMENT '创建人用户ID',
                               `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `update_user` bigint DEFAULT NULL COMMENT '更新人用户ID',
                               `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常、1已删除',
                               PRIMARY KEY (`id`),
                               KEY idx_parent_id (`parent_id`),
                               KEY idx_is_delete (`is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库分类表';

-- ====================== 知识库素材表 kb_material ======================
CREATE TABLE `kb_material` (
                               `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键自增ID',
                               `category_id` bigint NOT NULL COMMENT '所属分类ID，关联kb_category.id',
                               `material_name` varchar(100) NOT NULL COMMENT '素材名称',
                               `material_type` tinyint NOT NULL COMMENT '素材内容类型：1富文本 2附件 3外链',
                               `rich_content` text DEFAULT NULL COMMENT '富文本内容（material_type=1时使用）',
                               `link_url` varchar(255) DEFAULT NULL COMMENT '外链地址（material_type=3时使用）',
                               `cover_file_id` bigint DEFAULT NULL COMMENT '封面文件ID，关联sys_file.id',
                               `material_status` tinyint NOT NULL DEFAULT 0 COMMENT '素材状态：0草稿 1已发布',
                               `create_user` bigint DEFAULT NULL COMMENT '创建人用户ID',
                               `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `update_user` bigint DEFAULT NULL COMMENT '更新人用户ID',
                               `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常、1已删除',
                               PRIMARY KEY (`id`),
                               KEY idx_category_id (`category_id`),
                               KEY idx_material_status (`material_status`),
                               KEY idx_is_delete (`is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库素材表';

-- ====================== 素材附件关联表 kb_material_file ======================
CREATE TABLE `kb_material_file` (
                                    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键自增ID',
                                    `material_id` bigint NOT NULL COMMENT '素材ID，关联kb_material.id',
                                    `file_id` bigint NOT NULL COMMENT '文件ID，关联sys_file.id',
                                    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常、1已删除',
                                    PRIMARY KEY (`id`),
                                    KEY idx_material_id (`material_id`),
                                    KEY idx_file_id (`file_id`),
                                    KEY idx_is_delete (`is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='素材附件关联表';

-- ====================== 学习任务表 learn_task ======================
CREATE TABLE `learn_task` (
                              `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键自增ID',
                              `task_name` varchar(100) NOT NULL COMMENT '任务名称',
                              `task_desc` varchar(500) DEFAULT NULL COMMENT '任务描述',
                              `start_time` datetime DEFAULT NULL COMMENT '任务开始时间',
                              `end_time` datetime DEFAULT NULL COMMENT '任务结束时间',
                              `task_status` tinyint NOT NULL DEFAULT 0 COMMENT '任务状态：0草稿 1已下发 2已结束',
                              `create_user` bigint DEFAULT NULL COMMENT '创建人用户ID',
                              `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              `update_user` bigint DEFAULT NULL COMMENT '更新人用户ID',
                              `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                              `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常、1已删除',
                              PRIMARY KEY (`id`),
                              KEY idx_task_status (`task_status`),
                              KEY idx_is_delete (`is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习任务表';

-- ====================== 学习任务素材关联表 learn_task_material ======================
CREATE TABLE `learn_task_material` (
                                       `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键自增ID',
                                       `task_id` bigint NOT NULL COMMENT '任务ID，关联learn_task.id',
                                       `material_id` bigint NOT NULL COMMENT '素材ID，关联kb_material.id',
                                       `sort_order` int DEFAULT 0 COMMENT '素材排序序号',
                                       `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常、1已删除',
                                       PRIMARY KEY (`id`),
                                       KEY idx_task_id (`task_id`),
                                       KEY idx_material_id (`material_id`),
                                       KEY idx_is_delete (`is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习任务素材关联表';

-- ====================== 学习任务用户关联表 learn_task_user ======================
CREATE TABLE `learn_task_user` (
                                   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键自增ID',
                                   `task_id` bigint NOT NULL COMMENT '任务ID，关联learn_task.id',
                                   `user_id` bigint NOT NULL COMMENT '用户ID，关联sys_user.id',
                                   `learn_status` tinyint NOT NULL DEFAULT 0 COMMENT '学习状态：0未开始 1学习中 2已完成',
                                   `learn_score` decimal(5,2) DEFAULT NULL COMMENT '学习得分',
                                   `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   `is_delete` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常、1已删除',
                                   PRIMARY KEY (`id`),
                                   KEY idx_task_id (`task_id`),
                                   KEY idx_user_id (`user_id`),
                                   KEY idx_is_delete (`is_delete`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习任务用户关联表';



-- ====================== 菜单权限表 sys_menu（完整初始数据） ======================
INSERT INTO sys_menu (id, parent_id, menu_name, route, button_perms, sort, create_time, update_time, is_delete) VALUES
-- 一级菜单
(1, 0, '系统管理', 'system', NULL, 1, NOW(), NOW(), 0),
(2, 0, '组织管理', 'organization', NULL, 2, NOW(), NOW(), 0),
(3, 0, '用户管理', 'user', NULL, 3, NOW(), NOW(), 0),
(4, 0, '角色管理', 'role', NULL, 4, NOW(), NOW(), 0),
(5, 0, '日志管理', 'log', NULL, 5, NOW(), NOW(), 0),
(6, 0, '消息中心', 'message', NULL, 6, NOW(), NOW(), 0),
(7, 0, '系统配置', 'config', NULL, 7, NOW(), NOW(), 0),

-- 组织管理子菜单
(10, 2, '组织列表', 'org:list', 'org:list,org:search', 1, NOW(), NOW(), 0),
(11, 2, '新增组织', 'org:add', 'org:add', 2, NOW(), NOW(), 0),
(12, 2, '编辑组织', 'org:edit', 'org:edit', 3, NOW(), NOW(), 0),
(13, 2, '删除组织', 'org:delete', 'org:delete', 4, NOW(), NOW(), 0),

-- 用户管理子菜单
(20, 3, '用户列表', 'user:list', 'user:list,user:selectUser', 1, NOW(), NOW(), 0),
(21, 3, '新增用户', 'user:add', 'user:add', 2, NOW(), NOW(), 0),
(22, 3, '编辑用户', 'user:edit', 'user:edit', 3, NOW(), NOW(), 0),
(23, 3, '删除用户', 'user:delete', 'user:delete', 4, NOW(), NOW(), 0),
(24, 3, '导入用户', 'user:import', 'user:import', 5, NOW(), NOW(), 0),
(25, 3, '重置密码', 'user:resetPwd', 'user:resetPwd', 6, NOW(), NOW(), 0),
(26, 3, '批量调整组织', 'user:batchUpdateOrg', 'user:batchUpdateOrg', 7, NOW(), NOW(), 0),
(27, 3, '批量禁用', 'user:batchDisable', 'user:batchDisable', 8, NOW(), NOW(), 0),

-- 角色管理子菜单
(30, 4, '角色列表', 'role:list', 'role:list', 1, NOW(), NOW(), 0),
(31, 4, '新增角色', 'role:add', 'role:add', 2, NOW(), NOW(), 0),
(32, 4, '编辑角色', 'role:edit', 'role:edit', 3, NOW(), NOW(), 0),
(33, 4, '删除角色', 'role:delete', 'role:delete', 4, NOW(), NOW(), 0),
(34, 4, '复制角色', 'role:copy', 'role:copy', 5, NOW(), NOW(), 0),
(35, 4, '分配菜单', 'role:allotMenu', 'role:allotMenu', 6, NOW(), NOW(), 0),
(36, 4, '分配用户', 'role:allotUser', 'role:allotUser', 7, NOW(), NOW(), 0),

-- 日志管理子菜单
(40, 5, '登录日志', 'loginLog:list', 'loginLog:list', 1, NOW(), NOW(), 0),
(41, 5, '操作日志', 'operLog:list', 'operLog:list', 2, NOW(), NOW(), 0),

-- 消息中心子菜单
(50, 6, '消息列表', 'message:list', 'message:list,message:read,message:delete', 1, NOW(), NOW(), 0),
(51, 6, '手动推送', 'message:push', 'message:push', 2, NOW(), NOW(), 0),

-- 系统配置子菜单
(60, 7, '配置列表', 'config:list', 'config:list', 1, NOW(), NOW(), 0),
(61, 7, '修改配置', 'config:edit', 'config:edit', 2, NOW(), NOW(), 0);

-- ====================== 角色菜单关联（超级管理员角色id=1 绑定全部菜单） ======================
INSERT INTO sys_role_menu (role_id, menu_id, create_user, create_time, update_user, update_time, is_delete)
SELECT 1, id, NULL, NOW(), NULL, NOW(), 0 FROM sys_menu WHERE is_delete = 0;

-- ====================== 系统配置初始数据（补充） ======================
INSERT INTO sys_config (config_key, config_value, config_desc, update_time, is_delete) VALUES
                                                                                           ('system:passwordDefault', '123456', '用户初始默认密码', NOW(), 0),
                                                                                           ('system:loginFailLimit', '5', '登录失败锁定次数', NOW(), 0),
                                                                                           ('system:lockMinutes', '30', '账号锁定时长（分钟）', NOW(), 0),
                                                                                           ('system:exportLimit', '5000', '同步导出上限条数', NOW(), 0),
                                                                                           ('system:messageExpireDays', '90', '消息自动清理天数', NOW(), 0),
                                                                                           ('system:logArchiveDays', '180', '日志归档天数', NOW(), 0);

-- ====================== 补充：创建示例子组织 ======================
INSERT INTO sys_org (parent_id, org_name, create_user, create_time, update_user, update_time, is_delete) VALUES
                                                                                                             (1, '技术部', 1, NOW(), 1, NOW(), 0),
                                                                                                             (1, '财务部', 1, NOW(), 1, NOW(), 0),
                                                                                                             (1, '运营部', 1, NOW(), 1, NOW(), 0),
                                                                                                             (2, '前端组', 1, NOW(), 1, NOW(), 0),
                                                                                                             (2, '后端组', 1, NOW(), 1, NOW(), 0);

-- ====================== 补充：创建示例用户（密码均为123456） ======================
INSERT INTO sys_user (login_account, password, phone, user_name, org_id, user_status, create_user, create_time, update_user, update_time, is_delete) VALUES
                                                                                                                                                         ('zhangsan', 'e10adc3949ba59abbe56e057f20f883e', '13900001111', '张三', 4, 0, 1, NOW(), 1, NOW(), 0),
                                                                                                                                                         ('lisi',     'e10adc3949ba59abbe56e057f20f883e', '13900002222', '李四', 5, 0, 1, NOW(), 1, NOW(), 0),
                                                                                                                                                         ('wangwu',   'e10adc3949ba59abbe56e057f20f883e', '13900003333', '王五', 6, 0, 1, NOW(), 1, NOW(), 0);


-- 测试初始化数据
INSERT INTO sys_async_export (user_id,file_id,task_status,export_params,create_time,update_time,is_delete)
VALUES (1,1,1,'{\"orgId\":1}',NOW(),NOW(),0);




-- ====================== 1. 组织表 sys_org 顶级组织 ======================
INSERT INTO sys_org (id,parent_id,org_name,create_user,create_time,update_user,update_time,is_delete)
VALUES (1,0,'总公司',NULL,NOW(),NULL,NOW(),0);



-- ====================== 2. 用户表 sys_user (所属组织id=1) ======================
-- 账号:admin 密码123456（示例密文，根据项目加密规则自行替换）
INSERT INTO sys_user (id,login_account,password,phone,user_name,org_id,user_status,lock_time,create_user,create_time,update_user,update_time,is_delete)
VALUES (1,'admin','e10adc3949ba59abbe56e057f20f883e','13800138000','系统管理员',1,0,NULL,NULL,NOW(),NULL,NOW(),0);

-- ====================== 3. 角色表 sys_role ======================
INSERT INTO sys_role (id,role_name,role_type,menu_ids,data_scope,create_user,create_time,update_user,update_time,is_delete)
VALUES (1,'超级管理员',0,'1,2,3',1,NULL,NOW(),NULL,NOW(),0);

-- ====================== 4. 用户角色关联 sys_user_role 用户1绑定角色1 ======================
INSERT INTO sys_user_role (id,user_id,role_id)
VALUES (1,1,1);

-- ====================== 5. 菜单权限表 sys_menu 顶级菜单 ======================
INSERT INTO sys_menu (id,parent_id,menu_name,route,button_perms,sort,create_time,update_time,is_delete)
VALUES (1,0,'系统管理','system','sys:list,sys:add,sys:edit,sys:delete',1,NOW(),NOW(),0);

-- ====================== 6. 文件资源表 sys_file 测试附件记录 ======================
INSERT INTO sys_file (id,business_type,business_id,file_name,file_url,file_size,suffix,upload_user_id,upload_time,is_delete)
VALUES (1,'template',1,'导入模板.xlsx','/upload/template/import.xlsx',2048,'xlsx',1,NOW(),0);

-- ====================== 7. 系统配置表 sys_config ======================
INSERT INTO sys_config (id,config_key,config_value,config_desc,update_time,is_delete)
VALUES (1,'system:siteName','煤矿应急管理平台','系统站点名称',NOW(),0);

-- ====================== 8. 登录日志表 sys_login_log（admin登录记录） ======================
INSERT INTO sys_login_log (id,user_id,login_account,login_ip,login_time,logout_time,login_status,is_delete)
VALUES (1,1,'admin','127.0.0.1','2026-07-15 09:20:00',NULL,1,0);

-- ====================== 9. 操作日志表 sys_oper_log（管理员操作记录） ======================
INSERT INTO sys_oper_log (id,oper_user_id,oper_user_name,oper_org_id,module,oper_type,oper_detail,oper_ip,oper_time,is_delete)
VALUES (1,1,'系统管理员',1,'预案管理','新增','新增一条应急演练预案','127.0.0.1',NOW(),0);

-- 角色id=1【超级管理员】绑定第二条菜单（假设菜单id=2）
INSERT INTO sys_role_menu (role_id,menu_id,create_user,create_time,update_user,update_time,is_delete)
VALUES (1,2,NULL,NOW(),NULL,NOW(),0);

-- --DDL
-- -- ① 创建表（你写的init.sql建表语句，典型DDL）
-- CREATE TABLE `sys_user` (
--                             `id` bigint AUTO_INCREMENT PRIMARY KEY
-- );
--
-- -- ② 修改表：新增字段
-- ALTER TABLE sys_user ADD age INT COMMENT '年龄';
--
-- -- ③ 删除表
-- DROP TABLE IF EXISTS sys_test;
--
-- -- ④ 清空表所有数据（重置自增）
-- TRUNCATE TABLE sys_login_log;
--
-- --DQL
-- -- 根据账号查询用户（登录第一步查询）
-- SELECT * FROM sys_user WHERE login_account = 'admin' AND is_delete = 0;
--
-- -- 查询用户对应的角色ID
-- SELECT role_id FROM sys_user_role WHERE user_id = 1;
--
-- -- 分页查询所有用户
-- SELECT * FROM sys_user WHERE is_delete = 0 LIMIT 0,10;
--
--
-- create table sys_user(
--     id bigint auto_increment primary key
-- );

