package com.ljl.studyexammanagementsystem.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@ApiModel(description = "系统配置")
@Table(name = "sys_config")
public class SysConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "配置ID")
    private Long id;

    @Column(name = "config_key", nullable = false, length = 50)
    @ApiModelProperty(value = "配置Key")
    private String configKey;

    @Column(name = "config_value", length = 500)
    @ApiModelProperty(value = "配置值")
    private String configValue;

    @Column(name = "config_desc", length = 100)
    @ApiModelProperty(value = "配置说明")
    private String configDesc;

    @Column(name = "update_time")
    @Temporal(TemporalType.TIMESTAMP)
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @Column(name = "is_delete", nullable = false)
    @ApiModelProperty(value = "是否删除：0否 1是")
    private Byte isDelete;
}
