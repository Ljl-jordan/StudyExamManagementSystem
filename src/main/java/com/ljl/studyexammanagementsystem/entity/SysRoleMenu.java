package com.ljl.studyexammanagementsystem.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "sys_role_menu")
@ApiModel(description = "角色菜单")

public class SysRoleMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "角色菜单ID")
    private Long id;

    @Column(name = "role_id", nullable = false)
    @ApiModelProperty(value = "角色ID")
    private Long roleId;

    @Column(name = "menu_id", nullable = false)
    @ApiModelProperty(value = "菜单ID")
    private Long menuId;

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
