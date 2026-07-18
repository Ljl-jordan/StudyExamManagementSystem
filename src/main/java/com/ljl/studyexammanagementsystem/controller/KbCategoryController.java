package com.ljl.studyexammanagementsystem.controller;

import com.ljl.studyexammanagementsystem.entity.KbCategory;
import com.ljl.studyexammanagementsystem.service.KbCategoryService;
import com.ljl.studyexammanagementsystem.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/kb/category")
@Api(tags = "知识库分类管理接口")
public class KbCategoryController {

    @Autowired
    private KbCategoryService kbCategoryService;

    @GetMapping("/tree")
    @ApiOperation(value = "获取分类树形结构")
    public Result<List<KbCategory>> tree(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long orgId = (Long) request.getAttribute("orgId");
        return kbCategoryService.getTree(userId, orgId);
    }

    @GetMapping("/list")
    @ApiOperation(value = "分类分页列表")
    public Result<Page<KbCategory>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long orgId = (Long) request.getAttribute("orgId");
        return kbCategoryService.page(pageNum, pageSize, keyword, userId, orgId);
    }

    @PostMapping("/add")
    @ApiOperation(value = "新增分类")
    public Result<Void> add(@RequestBody KbCategory category, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long orgId = (Long) request.getAttribute("orgId");
        return kbCategoryService.add(category, userId, orgId);
    }

    @PutMapping("/edit")
    @ApiOperation(value = "编辑分类")
    public Result<Void> edit(@RequestBody KbCategory category, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long id = category.getId();
        if (id == null) {
            return Result.paramError("分类ID不能为空");
        }
        return kbCategoryService.update(id, category, userId);
    }

    @DeleteMapping("/del/{id}")
    @ApiOperation(value = "删除分类")
    public Result<Void> delete(@PathVariable Long id) {
        return kbCategoryService.delete(id);
    }
}
