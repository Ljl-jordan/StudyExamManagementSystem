package com.ljl.studyexammanagementsystem.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@ApiModel(value = "QbQuestionSingle", description = "单选题附表")
@Table(name = "qb_question_single")
public class QbQuestionSingle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "主键ID")
    private Long id;

    @Column(name = "question_id", nullable = false)
    @ApiModelProperty(value = "关联试题ID")
    private Long questionId;

    @Column(name = "option_a", length = 500)
    @ApiModelProperty(value = "选项A")
    private String optionA;

    @Column(name = "option_b", length = 500)
    @ApiModelProperty(value = "选项B")
    private String optionB;

    @Column(name = "option_c", length = 500)
    @ApiModelProperty(value = "选项C")
    private String optionC;

    @Column(name = "option_d", length = 500)
    @ApiModelProperty(value = "选项D")
    private String optionD;

    @Column(name = "correct_answer", length = 10, nullable = false)
    @ApiModelProperty(value = "正确答案(A/B/C/D)")
    private String correctAnswer;
}
