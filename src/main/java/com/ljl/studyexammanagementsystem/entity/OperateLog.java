package com.ljl.studyexammanagementsystem.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@ApiModel(description = "操作日志")
@Table(name = "sys_oper_log")
public class OperateLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "日志ID")
    private Long id;

    @Column(name = "oper_user_id")
    @ApiModelProperty(value = "操作人ID")
    private Long operUserId;

    @Column(name = "oper_user_name", length = 30)
    @ApiModelProperty(value = "操作人姓名")
    private String operUserName;

    @Column(name = "oper_org_id")
    @ApiModelProperty(value = "操作人所属组织ID")
    private Long operOrgId;

    @Column(name = "module", length = 50)
    @ApiModelProperty(value = "操作模块")
    private String module;

    @Column(name = "oper_type", length = 30)
    @ApiModelProperty(value = "操作类型")
    private String operType;

    @Column(name = "oper_detail", columnDefinition = "TEXT")
    @ApiModelProperty(value = "操作详情")
    private String operDetail;

    @Column(name = "oper_ip", length = 30)
    @ApiModelProperty(value = "操作IP")
    private String operIp;

    @Column(name = "oper_time")
    @Temporal(TemporalType.TIMESTAMP)
    @ApiModelProperty(value = "操作时间")
    private Date operTime;

    @Column(name = "is_delete", nullable = false)
    @ApiModelProperty(value = "是否删除：0否 1是")
    private Byte isDelete;
}
