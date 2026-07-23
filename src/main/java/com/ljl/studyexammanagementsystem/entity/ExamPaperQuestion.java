package com.ljl.studyexammanagementsystem.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "exam_paper_question")
@ApiModel(description = "试卷题目关联表")
public class ExamPaperQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "ID", example = "1")
    private Long id;

    @Column(name = "paper_id", nullable = false)
    @ApiModelProperty(value = "试卷ID", example = "1")
    private Long paperId;

    @Column(name = "question_id", nullable = false)
    @ApiModelProperty(value = "题目ID", example = "1")
    private Long questionId;

    @Column(name = "question_order", nullable = false)
    @ApiModelProperty(value = "题目序号", example = "1")
    private Integer questionOrder;

    @Column(name = "score", nullable = false)
    @ApiModelProperty(value = "题目分值", example = "5.0")
    private Double score;

}
