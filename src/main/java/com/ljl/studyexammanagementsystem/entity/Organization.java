package com.ljl.studyexammanagementsystem.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@ApiModel(value = "Organization", description = "组织机构")
@Table(name = "sys_org")
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "组织机构ID")
    private Long id;

    @Column(name = "parent_id", nullable = false)
    @ApiModelProperty(value = "父组织机构ID")
    private Long parentId;

    @Column(name = "org_name", nullable = false, length = 50)
    @ApiModelProperty(value = "组织机构名称")
    private String orgName;

    @Column(name = "create_user")
    @ApiModelProperty(value = "创建用户ID")
    private Long createUser;

    @Column(name = "create_time", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @Column(name = "update_user")
    @ApiModelProperty(value = "更新用户ID")
    private Long updateUser;

    @Column(name = "update_time")
    @Temporal(TemporalType.TIMESTAMP)
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @Column(name = "is_delete", nullable = false)
    @ApiModelProperty(value = "删除标志")
    private Byte isDelete;


}
