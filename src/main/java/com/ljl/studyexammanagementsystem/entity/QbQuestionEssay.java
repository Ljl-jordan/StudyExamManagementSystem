package com.ljl.studyexammanagementsystem.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@ApiModel(value = "QbQuestionEssay", description = "简答题附表")
@Table(name = "qb_question_essay")
public class QbQuestionEssay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "主键ID")
    private Long id;

    @Column(name = "question_id", nullable = false)
    @ApiModelProperty(value = "关联试题ID")
    private Long questionId;

    @Column(name = "reference_answer", columnDefinition = "TEXT", nullable = false)
    @ApiModelProperty(value = "参考答案")
    private String referenceAnswer;
}
