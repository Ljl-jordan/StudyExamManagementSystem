package com.ljl.studyexammanagementsystem.service;

import com.ljl.studyexammanagementsystem.vo.Result;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface ExamAnswerService {

    // Day19 考生答题相关接口
    Result<?> getExamQuestions(Long paperId, HttpServletRequest request);

    Result<String> saveAnswer(Long answerSheetId, Long questionId, String userAnswer, HttpServletRequest request);

    Result<String> submitPaper(Long answerSheetId, HttpServletRequest request);

    Result<String> createAnswerSheet(Long paperId, HttpServletRequest request);

    // 自动交卷和判分
    void autoSubmitExpiredPapers();

    void autoGradeObjectiveQuestions();

    // Day20 阅卷相关接口
    Result<?> getAnswerSheetDetail(Long answerSheetId, HttpServletRequest request);

    Result<String> gradeEssayQuestion(Long answerSheetId, Long questionId, Double score, HttpServletRequest request);

    Result<String> batchGradeEssayQuestions(Map<String, Object> params, HttpServletRequest request);

    // Day21 成绩相关接口
    Result<String> getScoreList(Integer pageNum, Integer pageSize, Map<String, Object> filters, HttpServletRequest request);

    Result<String> exportScoresSync(Map<String, Object> filters, HttpServletRequest request);

    Result<String> exportScoresAsync(Map<String, Object> filters, HttpServletRequest request);

    //Result<?> error(String message);
}