package com.ljl.studyexammanagementsystem.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "exam_answer_record")
@ApiModel(description = "答题记录表")
public class ExamAnswerRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "ID", example = "1")
    private Long id;

    @Column(name = "answer_sheet_id", nullable = false)
    @ApiModelProperty(value = "答卷ID", example = "1")
    private Long answerSheetId;

    @Column(name = "question_id", nullable = false)
    @ApiModelProperty(value = "题目ID", example = "1")
    private Long questionId;

    @Column(name = "question_type", nullable = false)
    @ApiModelProperty(value = "题目类型", example = "1")
    private Byte questionType; // 1-单选，2-多选，3-判断，4-填空，5-简答

    @Column(name = "user_answer", length = 2000)
    @ApiModelProperty(value = "用户答案", example = "A")
    private String userAnswer;

    @Column(name = "correct_answer", length = 2000)
    @ApiModelProperty(value = "正确答案", example = "A")
    private String correctAnswer;

    @Column(name = "score", nullable = false)
    @ApiModelProperty(value = "题目分值", example = "5.00")
    private Double score;

    @Column(name = "auto_score")
    @ApiModelProperty(value = "自动判分得分", example = "5.00")
    private Double autoScore;

    @Column(name = "is_right")
    @ApiModelProperty(value = "是否正确", example = "1")
    private Byte isRight; // 0-错误，1-正确

    @Column(name = "manual_score")
    @ApiModelProperty(value = "人工判分得分", example = "4.00")
    private Double manualScore;

    @Column(name = "create_time")
    @ApiModelProperty(value = "创建时间", example = "2023-04-01 10:00:00")
    private Date createTime;

    @Column(name = "update_time")
    @ApiModelProperty(value = "更新时间", example = "2023-04-01 10:00:00")
    private Date updateTime;

}
