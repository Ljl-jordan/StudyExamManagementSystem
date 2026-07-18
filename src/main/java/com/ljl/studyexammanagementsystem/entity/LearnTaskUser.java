package com.ljl.studyexammanagementsystem.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@ApiModel(value = "LearnTaskUser", description = "学习任务用户关联")
@Table(name = "learn_task_user")
public class LearnTaskUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "主键ID")
    private Long id;

    @Column(name = "task_id", nullable = false)
    @ApiModelProperty(value = "任务ID")
    private Long taskId;

    @Column(name = "user_id", nullable = false)
    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @Column(name = "learn_status", nullable = false)
    @ApiModelProperty(value = "学习状态：0未开始 1学习中 2已完成")
    private Byte learnStatus;

    @Column(name = "learn_score", precision = 5, scale = 2)
    @ApiModelProperty(value = "学习得分")
    private BigDecimal learnScore;

    @Column(name = "create_time", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @Column(name = "update_time")
    @Temporal(TemporalType.TIMESTAMP)
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @Column(name = "is_delete", nullable = false)
    @ApiModelProperty(value = "逻辑删除标志")
    private Byte isDelete;
}
