package com.ljl.studyexammanagementsystem.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@ApiModel(value = "LearnTaskMaterial", description = "学习任务素材关联")
@Table(name = "learn_task_material")
public class LearnTaskMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "主键ID")
    private Long id;

    @Column(name = "task_id", nullable = false)
    @ApiModelProperty(value = "任务ID")
    private Long taskId;

    @Column(name = "material_id", nullable = false)
    @ApiModelProperty(value = "素材ID")
    private Long materialId;

    @Column(name = "sort_order")
    @ApiModelProperty(value = "排序序号")
    private Integer sortOrder;

    @Column(name = "create_time", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @Column(name = "is_delete", nullable = false)
    @ApiModelProperty(value = "逻辑删除标志")
    private Byte isDelete;
}
