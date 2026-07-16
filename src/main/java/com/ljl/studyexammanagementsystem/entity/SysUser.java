// 文件路径: src/main/java/com/ljl/studyexammanagementsystem/entity/SysUser.java
package com.ljl.studyexammanagementsystem.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@ApiModel
@Table(name = "sys_user")
public class SysUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @ApiModelProperty(value = "用户ID")
    private Long id;

    @Column(name = "login_account", nullable = false, length = 50)
    @ApiModelProperty(value = "登录账号")
    private String loginAccount;

    @Column(nullable = false, length = 100)
    @ApiModelProperty(value = "密码")
    private String password;

    @Column(length = 11)
    @ApiModelProperty(value = "手机号")
    private String phone;

    @Column(name = "user_name", nullable = false, length = 30)
    @ApiModelProperty(value = "用户名")
    private String userName;

    @Column(name = "org_id", nullable = false)
    @ApiModelProperty(value = "组织ID")
    private Long orgId;

    @Column(name = "user_status", nullable = false)
    @ApiModelProperty(value = "用户状态")
    private Byte userStatus;

    @Column(name = "lock_time")
    @Temporal(TemporalType.TIMESTAMP)
    @ApiModelProperty(value = "锁定时间")
    private Date lockTime;

    @Column(name = "create_user")
    @ApiModelProperty(value = "创建人")
    private Long createUser;

    @Column(name = "create_time", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @Column(name = "update_user")
    @ApiModelProperty(value = "更新人")
    private Long updateUser;

    @Column(name = "update_time")
    @Temporal(TemporalType.TIMESTAMP)
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @Column(name = "is_delete", nullable = false)
    @ApiModelProperty(value = "是否删除")
    private Byte isDelete;


}
