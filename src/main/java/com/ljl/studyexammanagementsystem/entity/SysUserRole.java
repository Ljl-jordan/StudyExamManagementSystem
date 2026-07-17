package com.ljl.studyexammanagementsystem.entity;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "sys_user_role")
@ApiModel(description = "用户角色")
public class SysUserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "用户角色ID")
    private Long id;

    @Column(name = "user_id", nullable = false)
    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @Column(name = "role_id", nullable = false)
    @ApiModelProperty(value = "角色ID")
    private Long roleId;
}
