package com.ljl.studyexammanagementsystem.controller;

import com.ljl.studyexammanagementsystem.entity.Organization;
import com.ljl.studyexammanagementsystem.service.OrganizationService;
import com.ljl.studyexammanagementsystem.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/org")
@Api(tags = "组织管理接口")
public class OrganizationController {

    @Autowired
    private OrganizationService organizationService;

    @GetMapping("/tree")
    @ApiOperation(value = "获取组织树形结构")
    public Result<List<Organization>> tree() {
        return organizationService.getTree();
    }

    @GetMapping("/list")
    @ApiOperation(value = "组织分页列表")
    public Result<Page<Organization>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        return organizationService.page(pageNum, pageSize, keyword);
    }

    @GetMapping("/search")
    @ApiOperation(value = "组织名称模糊搜索")
    public Result<Page<Organization>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return organizationService.page(pageNum, pageSize, keyword);
    }

    @PostMapping("/add")
    @ApiOperation(value = "新增组织")
    public Result<Void> add(@RequestBody Organization organization, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return organizationService.add(organization, userId);
    }

    @PutMapping("/edit")
    @ApiOperation(value = "编辑组织")
    public Result<Void> edit(@RequestBody Organization organization, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long id = organization.getId();
        if (id == null) {
            return Result.paramError("组织ID不能为空");
        }
        return organizationService.update(id, organization, userId);
    }

    @DeleteMapping("/del/{id}")
    @ApiOperation(value = "删除组织")
    public Result<Void> delete(@PathVariable Long id) {
        return organizationService.delete(id);
    }
}
