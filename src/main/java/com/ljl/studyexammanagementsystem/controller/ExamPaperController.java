package com.ljl.studyexammanagementsystem.controller;

import com.ljl.studyexammanagementsystem.entity.ExamPaper;
import com.ljl.studyexammanagementsystem.service.ExamPaperService;
import com.ljl.studyexammanagementsystem.utils.DataPermissionUtil;
import com.ljl.studyexammanagementsystem.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Api(tags = "试卷管理")
@RestController
@RequestMapping("/api/examPaper")
public class ExamPaperController {

    @Autowired
    private ExamPaperService examPaperService;

    @Autowired
    private DataPermissionUtil dataPermissionUtil;

    /**
     * 试卷草稿分页列表
     */
    @ApiOperation("试卷分页列表")
    @GetMapping("/page")
    public Result<?> page(@RequestParam(defaultValue = "1") Integer pageNum,
                          @RequestParam(defaultValue = "10") Integer pageSize,
                          @RequestParam(required = false) String keyword,
                          HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long orgId = (Long) request.getAttribute("orgId");
        return examPaperService.page(pageNum, pageSize, keyword, userId, orgId);
    }

    /**
     * 试卷详情
     */
    @ApiOperation("试卷详情")
    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id) {
        return examPaperService.detail(id);
    }

    /**
     * 新增试卷
     */
    @ApiOperation("新增试卷")
    @PostMapping("/add")
    public Result<?> add(@RequestBody ExamPaper paper, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return examPaperService.add(paper, userId);
    }

    /**
     * 编辑试卷
     */
    @ApiOperation("编辑试卷")
    @PutMapping("/update/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody ExamPaper paper, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return examPaperService.update(id, paper, userId);
    }

    /**
     * 删除试卷
     */
    @ApiOperation("删除试卷")
    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Long id) {
        return examPaperService.delete(id);
    }

    // Day 17 新增：手动选题组卷
    @ApiOperation("手动选题组卷")
    @PostMapping("/assembly/manual")
    public Result<?> manualAssembly(@RequestParam Long paperId, @RequestBody String questionIdsJson, HttpServletRequest request) {
        return examPaperService.manualAssembly(paperId, questionIdsJson);
    }

    // Day 17 新增：固定抽题组卷
    @ApiOperation("固定抽题组卷")
    @PostMapping("/assembly/fixed")
    public Result<?> fixedAssembly(@RequestParam Long paperId, @RequestBody String assemblyConfigJson, HttpServletRequest request) {
        return examPaperService.fixedAssembly(paperId, assemblyConfigJson);
    }

    // Day 17 新增：动态抽题组卷
    @ApiOperation("动态抽题组卷")
    @PostMapping("/assembly/dynamic")
    public Result<?> dynamicAssembly(@RequestParam Long paperId, @RequestBody String assemblyConfigJson, HttpServletRequest request) {
        return examPaperService.dynamicAssembly(paperId, assemblyConfigJson);
    }

    // Day 17 新增：Excel导入试题组卷
    @ApiOperation("Excel导入试题组卷")
    @PostMapping("/assembly/import")
    public Result<?> importAssembly(@RequestParam Long paperId, @RequestBody String questionIdsJson, HttpServletRequest request) {
        return examPaperService.importAssembly(paperId, questionIdsJson);
    }

    // Day 18 新增：发布试卷
    @ApiOperation("发布试卷")
    @PostMapping("/publish/{id}")
    public Result<?> publish(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return examPaperService.publish(id, userId);
    }

    // Day 18 新增：取消发布试卷
    @ApiOperation("取消发布试卷")
    @PostMapping("/unpublish/{id}")
    public Result<?> unpublish(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return examPaperService.unpublish(id, userId);
    }

    // Day 18 新增：归档试卷
    @ApiOperation("归档试卷")
    @PostMapping("/archive/{id}")
    public Result<?> archive(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return examPaperService.archive(id, userId);
    }
}
