package com.ljl.studyexammanagementsystem.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@ApiModel(value = "QbQuestionBlank", description = "填空题附表")
@Table(name = "qb_question_blank")
public class QbQuestionBlank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "主键ID")
    private Long id;

    @Column(name = "question_id", nullable = false)
    @ApiModelProperty(value = "关联试题ID")
    private Long questionId;

    @Column(name = "blank_count", nullable = false)
    @ApiModelProperty(value = "填空数量")
    private Integer blankCount;

    @Column(name = "answer_json", columnDefinition = "TEXT", nullable = false)
    @ApiModelProperty(value = "答案JSON数组")
    private String answerJson;
}
