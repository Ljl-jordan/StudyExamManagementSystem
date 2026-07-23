package com.ljl.studyexammanagementsystem.controller;

import com.ljl.studyexammanagementsystem.entity.QbQuestion;
import com.ljl.studyexammanagementsystem.service.QbQuestionService;
import com.ljl.studyexammanagementsystem.utils.DataPermissionUtil;
import com.ljl.studyexammanagementsystem.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@Api(tags = "试题管理")
@RestController
@RequestMapping("/api/qbQuestion")
public class QbQuestionController {

    @Autowired
    private QbQuestionService qbQuestionService;

    @Autowired
    private DataPermissionUtil dataPermissionUtil;

    /**
     * 试题分页列表
     */
    @ApiOperation("试题分页列表")
    @GetMapping("/page")
    public Result<?> page(@RequestParam(defaultValue = "1") Integer pageNum,
                          @RequestParam(defaultValue = "10") Integer pageSize,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(required = false) Long categoryId,
                          HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long orgId = (Long) request.getAttribute("orgId");
        return qbQuestionService.page(pageNum, pageSize, keyword, categoryId, userId, orgId);
    }

    /**
     * 试题详情
     */
    @ApiOperation("试题详情")
    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id) {
        return qbQuestionService.detail(id);
    }

    /**
     * 新增试题
     */
    @ApiOperation("新增试题")
    @PostMapping("/add")
    public Result<?> add(@RequestBody Map<String, Object> requestData, HttpServletRequest request) {
        // 从requestData中提取question和extraData
        Map<String, Object> questionMap = (Map<String, Object>) requestData.get("question");
        Object extraData = requestData.get("extraData");

        // 转换为对应的对象并调用服务
        QbQuestion question = mapToQuestion(questionMap);
        Long userId = (Long) request.getAttribute("userId");
        return qbQuestionService.add(question, extraData, userId);
    }

    /**
     * 编辑试题
     */
    @ApiOperation("编辑试题")
    @PutMapping("/update/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody QbQuestion question, @RequestBody(required = false) Object extraData, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return qbQuestionService.update(id, question, extraData, userId);
    }

    /**
     * 删除试题
     */
    @ApiOperation("删除试题")
    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Long id) {
        return qbQuestionService.delete(id);
    }

    // Day 16 新增：批量导入试题
    @ApiOperation("批量导入试题")
    @PostMapping("/batch/import")
    public Result<?> batchImport(@RequestParam MultipartFile file, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return qbQuestionService.batchImport(file, userId);
    }

    // Day 16 新增：批量导出试题
    @ApiOperation("批量导出试题")
    @PostMapping("/batch/export")
    public void batchExport(@RequestBody List<Long> ids, HttpServletResponse response) {
        qbQuestionService.batchExport(ids, response);
    }

    // Day 16 新增：批量迁移试题分类
    @ApiOperation("批量迁移试题分类")
    @PutMapping("/batch/transferCategory")
    public Result<?> batchTransferCategory(@RequestParam List<Long> ids, @RequestParam Long targetCategoryId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return qbQuestionService.batchTransferCategory(ids, targetCategoryId, userId);
    }
    /**
     * 将Map映射为QbQuestion对象
     */
    private QbQuestion mapToQuestion(Map<String, Object> questionMap) {
        QbQuestion question = new QbQuestion();

        if (questionMap.get("id") != null) {
            question.setId(Long.valueOf(questionMap.get("id").toString()));
        }
        if (questionMap.get("questionTitle") != null) {
            question.setQuestionTitle(questionMap.get("questionTitle").toString());
        }
        if (questionMap.get("questionType") != null) {
            question.setQuestionType(Byte.valueOf(questionMap.get("questionType").toString()));
        }
        if (questionMap.get("difficultyLevel") != null) {
            question.setDifficultyLevel(Byte.valueOf(questionMap.get("difficultyLevel").toString()));
        }
        if (questionMap.get("score") != null) {
            question.setScore(Double.valueOf(questionMap.get("score").toString()));
        }
        if (questionMap.get("categoryId") != null) {
            question.setCategoryId(Long.valueOf(questionMap.get("categoryId").toString()));
        }
        if (questionMap.get("analysis") != null) {
            question.setAnalysis(questionMap.get("analysis").toString());
        }
        if (questionMap.get("createUser") != null) {
            question.setCreateUser(Long.valueOf(questionMap.get("createUser").toString()));
        }

        return question;
    }
}
