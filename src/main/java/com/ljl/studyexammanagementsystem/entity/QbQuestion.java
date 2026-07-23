package com.ljl.studyexammanagementsystem.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@ApiModel(value = "QbQuestion", description = "试题主表")
@Table(name = "qb_question")
public class QbQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "试题ID")
    private Long id;

    @Column(name = "question_title", nullable = false, length = 500)
    @ApiModelProperty(value = "题干")
    private String questionTitle;

    @Column(name = "question_type", nullable = false)
    @ApiModelProperty(value = "题型：1单选题 2多选题 3判断题 4填空题 5简答题")
    private Byte questionType;

    @Column(name = "difficulty_level", nullable = false)
    @ApiModelProperty(value = "难度等级：1简单 2一般 3困难")
    private Byte difficultyLevel;

    @Column(name = "score", nullable = false)
    @ApiModelProperty(value = "题目分值")
    private Double score;

    @Column(name = "category_id", nullable = false)
    @ApiModelProperty(value = "所属分类ID")
    private Long categoryId;

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


    @Column(name = "analysis", columnDefinition = "TEXT")
    @ApiModelProperty(value = "试题解析")
    private String analysis;
}
