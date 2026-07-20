package com.ljl.studyexammanagementsystem.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@ApiModel(value = "LearnTaskSignature", description = "电子签名")
@Table(name = "learn_task_signature")
public class LearnTaskSignature {

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

    @Column(name = "file_id", nullable = false)
    @ApiModelProperty(value = "签名文件ID")
    private Long fileId;

    @Column(name = "sign_time")
    @Temporal(TemporalType.TIMESTAMP)
    @ApiModelProperty(value = "签名时间")
    private Date signTime;

    @Column(name = "create_time", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @Column(name = "is_delete", nullable = false)
    @ApiModelProperty(value = "逻辑删除标志")
    private Byte isDelete;
}
