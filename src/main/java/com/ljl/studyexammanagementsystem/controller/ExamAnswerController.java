package com.ljl.studyexammanagementsystem.controller;

import com.ljl.studyexammanagementsystem.service.ExamAnswerService;
import com.ljl.studyexammanagementsystem.utils.DataPermissionUtil;
import com.ljl.studyexammanagementsystem.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Api(tags = "考试答题管理")
@RestController
@RequestMapping("/api/examAnswer")
public class ExamAnswerController {

    @Autowired
    private ExamAnswerService examAnswerService;

    @Autowired
    private DataPermissionUtil dataPermissionUtil;

    // Day19 考生答题相关接口

    /**
     * 考生获取试卷题目（校验考试起止时间）
     */
    @ApiOperation("考生获取试卷题目")
    @GetMapping("/getQuestions/{paperId}")
    public Result<?> getExamQuestions(@PathVariable Long paperId, HttpServletRequest request) {
        return examAnswerService.getExamQuestions(paperId, request);
    }

    /**
     * 实时保存答题答案
     */
    @ApiOperation("实时保存答题答案")
    @PostMapping("/saveAnswer")
    public Result<String> saveAnswer(@RequestParam Long answerSheetId,
                                     @RequestParam Long questionId,
                                     @RequestParam String userAnswer,
                                     HttpServletRequest request) {
        return examAnswerService.saveAnswer(answerSheetId, questionId, userAnswer, request);
    }

    /**
     * 手动交卷
     */
    @ApiOperation("手动交卷")
    @PostMapping("/submitPaper/{answerSheetId}")
    public Result<String> submitPaper(@PathVariable Long answerSheetId, HttpServletRequest request) {
        return examAnswerService.submitPaper(answerSheetId, request);
    }

    /**
     * 创建答卷
     */
    @ApiOperation("创建答卷")
    @PostMapping("/createAnswerSheet")
    public Result<String> createAnswerSheet(@RequestParam Long paperId, HttpServletRequest request) {
        return examAnswerService.createAnswerSheet(paperId, request);
    }

    // Day20 阅卷相关接口

    /**
     * 答卷详情查询
     */
    @ApiOperation("答卷详情查询")
    @GetMapping("/detail/{answerSheetId}")
    public Result<?> getAnswerSheetDetail(@PathVariable Long answerSheetId, HttpServletRequest request) {
        return examAnswerService.getAnswerSheetDetail(answerSheetId, request);
    }

    /**
     * 简答题单人人工打分
     */
    @ApiOperation("简答题单人人工打分")
    @PutMapping("/gradeEssayQuestion")
    public Result<String> gradeEssayQuestion(@RequestParam Long answerSheetId,
                                             @RequestParam Long questionId,
                                             @RequestParam Double score,
                                             HttpServletRequest request) {
        return examAnswerService.gradeEssayQuestion(answerSheetId, questionId, score, request);
    }

    /**
     * 批量阅卷打分
     */
    @ApiOperation("批量阅卷打分")
    @PostMapping("/batchGradeEssayQuestions")
    public Result<String> batchGradeEssayQuestions(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        return examAnswerService.batchGradeEssayQuestions(params, request);
    }

    // Day21 成绩相关接口

    /**
     * 成绩分页列表
     */
    @ApiOperation("成绩分页列表")
    @GetMapping("/scoreList")
    public Result<?> getScoreList(@RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize,
                                  @RequestParam(required = false) Long paperId,
                                  @RequestParam(required = false) Long userId,
                                  @RequestParam(required = false) String startTimeStr,
                                  @RequestParam(required = false) String endTimeStr,
                                  @RequestParam(required = false) Byte status,
                                  HttpServletRequest request) {

        // 将字符串格式的时间转换为Date对象
        Date startTime = null;
        Date endTime = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            if (startTimeStr != null && !startTimeStr.isEmpty()) {
                startTime = sdf.parse(startTimeStr);
            }
            if (endTimeStr != null && !endTimeStr.isEmpty()) {
                endTime = sdf.parse(endTimeStr);
            }
        } catch (Exception e) {
            return Result.error("时间格式错误，请使用 yyyy-MM-dd HH:mm:ss 格式");
        }

        Map<String, Object> filters = new HashMap<>();
        filters.put("paperId", paperId);
        filters.put("userId", userId);
        filters.put("startTime", startTime);
        filters.put("endTime", endTime);
        filters.put("status", status);

        return examAnswerService.getScoreList(pageNum, pageSize, filters, request);
    }

    /**
     * 同步成绩导出
     */
    @ApiOperation("同步成绩导出")
    @PostMapping("/exportScores/sync")
    public Result<String> exportScoresSync(@RequestBody Map<String, Object> filters, HttpServletRequest request) {
        return examAnswerService.exportScoresSync(filters, request);
    }

    /**
     * 异步成绩导出
     */
    @ApiOperation("异步成绩导出")
    @PostMapping("/exportScores/async")
    public Result<String> exportScoresAsync(@RequestBody Map<String, Object> filters, HttpServletRequest request) {
        return examAnswerService.exportScoresAsync(filters, request);
    }

}
