package com.ljl.studyexammanagementsystem.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@ApiModel(value = "LearnTask", description = "学习任务")
@Table(name = "learn_task")
public class LearnTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "任务ID")
    private Long id;

    @Column(name = "task_name", nullable = false, length = 100)
    @ApiModelProperty(value = "任务名称")
    private String taskName;

    @Column(name = "task_desc", length = 500)
    @ApiModelProperty(value = "任务描述")
    private String taskDesc;

    @Column(name = "start_time")
    @Temporal(TemporalType.TIMESTAMP)
    @ApiModelProperty(value = "任务开始时间")
    private Date startTime;

    @Column(name = "end_time")
    @Temporal(TemporalType.TIMESTAMP)
    @ApiModelProperty(value = "任务结束时间")
    private Date endTime;

    @Column(name = "task_status", nullable = false)
    @ApiModelProperty(value = "任务状态：0草稿 1已下发 2已结束")
    private Byte taskStatus;

    @Column(name = "create_user")
    @ApiModelProperty(value = "创建人用户ID")
    private Long createUser;

    @Column(name = "create_time", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @Column(name = "update_user")
    @ApiModelProperty(value = "更新人用户ID")
    private Long updateUser;

    @Column(name = "update_time")
    @Temporal(TemporalType.TIMESTAMP)
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @Column(name = "is_delete", nullable = false)
    @ApiModelProperty(value = "逻辑删除标志")
    private Byte isDelete;
}
