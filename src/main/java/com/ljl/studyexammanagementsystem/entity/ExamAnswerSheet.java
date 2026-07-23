package com.ljl.studyexammanagementsystem.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@Table(name = "exam_answer_sheet")
@ApiModel(description = "答卷表")
public class ExamAnswerSheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "ID", example = "1")
    private Long id;

    @Column(name = "paper_id", nullable = false)
    @ApiModelProperty(value = "试卷ID", example = "1")
    private Long paperId;

    @Column(name = "user_id", nullable = false)
    @ApiModelProperty(value = "用户ID", example = "1")
    private Long userId;

    @Column(name = "user_name", length = 100)
    @ApiModelProperty(value = "用户名", example = "张三")
    private String userName;

    @Column(name = "organization_id")
    @ApiModelProperty(value = "组织ID", example = "1")
    private Long organizationId;

    @Column(name = "start_time")
    @ApiModelProperty(value = "开始答题时间", example = "2023-04-01 10:00:00")
    private Date startTime;

    @Column(name = "submit_time")
    @ApiModelProperty(value = "提交时间", example = "2023-04-01 11:00:00")
    private Date submitTime;

    @Column(name = "auto_score", precision = 10, scale = 2)
    @ApiModelProperty(value = "客观题得分", example = "60.00")
    private BigDecimal autoScore;

    @Column(name = "manual_score", precision = 10, scale = 2)
    @ApiModelProperty(value = "主观题得分", example = "30.00")
    private BigDecimal manualScore;

    @Column(name = "total_score", precision = 10, scale = 2)
    @ApiModelProperty(value = "总分", example = "90.00")
    private BigDecimal totalScore;

    @Column(name = "pass_flag")
    @ApiModelProperty(value = "是否及格", example = "1")
    private Byte passFlag; // 0-不及格，1-及格

    @Column(name = "status", nullable = false)
    @ApiModelProperty(value = "状态", example = "1")
    private Byte status; // 0-进行中，1-已提交，2-已阅卷

    @Column(name = "create_time")
    @ApiModelProperty(value = "创建时间", example = "2023-04-01 10:00:00")
    private Date createTime;

    @Column(name = "update_time")
    @ApiModelProperty(value = "更新时间", example = "2023-04-01 10:00:00")
    private Date updateTime;

    @Column(name = "is_delete", nullable = false)
    @ApiModelProperty(value = "是否删除", example = "0")
    private Byte isDelete;

}
