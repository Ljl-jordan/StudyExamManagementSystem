package com.ljl.studyexammanagementsystem.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@ApiModel(description = "角色")
@Table(name = "sys_role")
public class SysRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "角色ID")
    private Long id;

    @Column(name = "role_name", nullable = false, length = 50)
    @ApiModelProperty(value = "角色名称")
    private String roleName;

    @Column(name = "role_type", nullable = false)
    @ApiModelProperty(value = "角色类型：0内置 1自定义")
    private Byte roleType;

    @Column(name = "data_scope")
    @ApiModelProperty(value = "数据权限：0全部 1本组织及下级 2仅本人")
    private Byte dataScope;

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
    @ApiModelProperty(value = "是否删除：0否 1是")
    private Byte isDelete;
}
