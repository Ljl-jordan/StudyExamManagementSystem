package com.ljl.studyexammanagementsystem.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@ApiModel(value = "LearnStudyRecord", description = "学习记录")
@Table(name = "learn_study_record")
public class LearnStudyRecord {

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

    @Column(name = "material_id", nullable = false)
    @ApiModelProperty(value = "素材ID")
    private Long materialId;

    @Column(name = "accumulated_time", nullable = false)
    @ApiModelProperty(value = "累计学习时长（秒）")
    private Integer accumulatedTime;

    @Column(name = "last_report_time")
    @Temporal(TemporalType.TIMESTAMP)
    @ApiModelProperty(value = "最近上报时间")
    private Date lastReportTime;

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
