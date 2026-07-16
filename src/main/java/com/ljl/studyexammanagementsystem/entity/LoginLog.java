// 文件路径: src/main/java/com/ljl/studyexammanagementsystem/entity/LoginLog.java
package com.ljl.studyexammanagementsystem.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;
@Data
@Entity
@ApiModel("登录日志")
@Table(name = "sys_login_log")
public class LoginLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @ApiModelProperty("主键ID")
    private Long id;

    @Column(name = "user_id")
    @ApiModelProperty("用户ID")
    private Long userId;

    @Column(name = "login_account", nullable = false, length = 50)
    @ApiModelProperty("登录账号")
    private String loginAccount;

    @Column(name = "login_ip", length = 30)
    @ApiModelProperty("登录IP")
    private String loginIp;

    @Column(name = "login_time")
    @ApiModelProperty("登录时间")
    @Temporal(TemporalType.TIMESTAMP)
    private Date loginTime;

    @Column(name = "logout_time")
    @ApiModelProperty("登出时间")
    @Temporal(TemporalType.TIMESTAMP)
    private Date logoutTime;

    @Column(name = "login_status", nullable = false)
    @ApiModelProperty("登录状态")
    private Byte loginStatus;

    @Column(name = "is_delete", nullable = false)
    @ApiModelProperty("是否删除")
    private Byte isDelete;

}
