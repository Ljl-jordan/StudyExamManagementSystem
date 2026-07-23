package com.ljl.studyexammanagementsystem.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@Table(name = "exam_paper")
@ApiModel(description = "ExamPaper")
public class ExamPaper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "ID", example = "1")
    private Long id;

    @Column(name = "paper_title", nullable = false, length = 200)
    @ApiModelProperty(value = "试卷标题", example = "考试试卷1")
    private String paperTitle;

    @Column(name = "paper_type", nullable = false)
    @ApiModelProperty(value = "试卷类型", example = "1")
    private Byte paperType; // 1-练习卷，2-正式考试

    @Column(name = "total_score", precision = 10, scale = 2)
    @ApiModelProperty(value = "总分", example = "100.00")
    private BigDecimal totalScore;

    @Column(name = "passing_score", precision = 10, scale = 2)
    @ApiModelProperty(value = "及格分", example = "60.00")
    private BigDecimal passingScore;

    @Column(name = "duration_minutes")
    @ApiModelProperty(value = "时长（分钟）", example = "60")
    private Integer durationMinutes;

    @Column(name = "start_time")
    @ApiModelProperty(value = "开始时间", example = "2023-04-01 10:00:00")
    private Date startTime;

    @Column(name = "end_time")
    @ApiModelProperty(value = "结束时间", example = "2023-04-01 12:00:00")
    private Date endTime;

    @Column(name = "paper_status", nullable = false)
    @ApiModelProperty(value = "试卷状态", example = "1")
    private Byte paperStatus; // 0-草稿，1-已发布，2-已归档

    @Column(name = "publish_time")
    @ApiModelProperty(value = "发布时间", example = "2023-04-01 10:00:00")
    private Date publishTime;

    @Column(name = "create_user")
    @ApiModelProperty(value = "创建用户", example = "1")
    private Long createUser;

    @Column(name = "create_time")
    @ApiModelProperty(value = "创建时间", example = "2023-04-01 10:00:00")
    private Date createTime;

    @Column(name = "update_time")
    @ApiModelProperty(value = "更新时间", example = "2023-04-01 10:00:00")
    private Date updateTime;

    @Column(name = "is_delete", nullable = false)
    @ApiModelProperty(value = "是否删除", example = "0")
    private Byte isDelete;

    @Column(name = "exam_description", length = 1000)
    @ApiModelProperty(value = "考试描述", example = "这是一次重要的考试")
    private String examDescription;


}
