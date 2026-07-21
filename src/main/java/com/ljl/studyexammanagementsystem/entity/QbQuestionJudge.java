package com.ljl.studyexammanagementsystem.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@ApiModel(value = "QbQuestionJudge", description = "判断题附表")
@Table(name = "qb_question_judge")
public class QbQuestionJudge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "主键ID")
    private Long id;

    @Column(name = "question_id", nullable = false)
    @ApiModelProperty(value = "关联试题ID")
    private Long questionId;

    @Column(name = "correct_answer", nullable = false)
    @ApiModelProperty(value = "正确答案(1正确/0错误)")
    private Boolean correctAnswer;
}
