package com.ljl.studyexammanagementsystem.controller;

import com.ljl.studyexammanagementsystem.entity.QbCategory;
import com.ljl.studyexammanagementsystem.service.QbCategoryService;
import com.ljl.studyexammanagementsystem.utils.DataPermissionUtil;
import com.ljl.studyexammanagementsystem.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/qbCategory")
public class QbCategoryController {

    @Autowired
    private QbCategoryService qbCategoryService;

    @Autowired
    private DataPermissionUtil dataPermissionUtil;

    /**
     * 获取分类树形结构
     */
    @GetMapping("/tree")
    public Result<?> getTree() {
        Long userId = dataPermissionUtil.getCurrentUserId();
        Long orgId = dataPermissionUtil.getCurrentUserOrgId();
        return qbCategoryService.getTree(userId, orgId);
    }

    /**
     * 分类分页列表
     */
    @GetMapping("/page")
    public Result<?> page(@RequestParam(defaultValue = "1") Integer pageNum,
                          @RequestParam(defaultValue = "10") Integer pageSize,
                          @RequestParam(required = false) String keyword) {
        Long userId = dataPermissionUtil.getCurrentUserId();
        Long orgId = dataPermissionUtil.getCurrentUserOrgId();
        return qbCategoryService.page(pageNum, pageSize, keyword, userId, orgId);
    }

    /**
     * 新增分类
     */
    @PostMapping("/add")
    public Result<?> add(@RequestBody QbCategory category) {
        Long userId = dataPermissionUtil.getCurrentUserId();
        Long orgId = dataPermissionUtil.getCurrentUserOrgId();
        return qbCategoryService.add(category, userId, orgId);
    }

    /**
     * 编辑分类
     */
    @PutMapping("/update/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody QbCategory category) {
        Long userId = dataPermissionUtil.getCurrentUserId();
        return qbCategoryService.update(id, category, userId);
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Long id) {
        return qbCategoryService.delete(id);
    }
}
