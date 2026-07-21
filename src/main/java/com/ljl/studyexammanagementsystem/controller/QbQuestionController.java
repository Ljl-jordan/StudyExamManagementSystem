package com.ljl.studyexammanagementsystem.controller;

import com.ljl.studyexammanagementsystem.entity.QbQuestion;
import com.ljl.studyexammanagementsystem.service.QbQuestionService;
import com.ljl.studyexammanagementsystem.utils.DataPermissionUtil;
import com.ljl.studyexammanagementsystem.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

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
    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id) {
        return qbQuestionService.detail(id);
    }

    /**
     * 新增试题
     */
    @PostMapping("/add")
    public Result<?> add(@RequestBody QbQuestion question, @RequestBody(required = false) Object extraData, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return qbQuestionService.add(question, extraData, userId);
    }

    /**
     * 编辑试题
     */
    @PutMapping("/update/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody QbQuestion question, @RequestBody(required = false) Object extraData, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return qbQuestionService.update(id, question, extraData, userId);
    }

    /**
     * 删除试题
     */
    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Long id) {
        return qbQuestionService.delete(id);
    }
}
