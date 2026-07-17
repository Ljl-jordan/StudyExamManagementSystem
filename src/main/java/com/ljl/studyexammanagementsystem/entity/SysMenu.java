package com.ljl.studyexammanagementsystem.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@ApiModel(description = "菜单")
@Table(name = "sys_menu")
public class SysMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "菜单ID")
    private Long id;

    @Column(name = "parent_id", nullable = false)
    @ApiModelProperty(value = "父菜单ID，顶级为0")
    private Long parentId;

    @Column(name = "menu_name", nullable = false, length = 50)
    @ApiModelProperty(value = "菜单名称")
    private String menuName;

    @Column(name = "route", length = 100)
    @ApiModelProperty(value = "前端路由标识")
    private String route;

    @Column(name = "button_perms", length = 500)
    @ApiModelProperty(value = "按钮权限标识集合")
    private String buttonPerms;

    @Column(name = "sort")
    @ApiModelProperty(value = "排序序号")
    private Integer sort;

    @Column(name = "create_time", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @Column(name = "update_time")
    @Temporal(TemporalType.TIMESTAMP)
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @Column(name = "is_delete", nullable = false)
    @ApiModelProperty(value = "是否删除：0否 1是")
    private Byte isDelete;
}
