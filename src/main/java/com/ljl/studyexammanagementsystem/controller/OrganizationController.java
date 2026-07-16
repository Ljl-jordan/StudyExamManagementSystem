package com.ljl.studyexammanagementsystem.controller;

import com.ljl.studyexammanagementsystem.entity.Organization;
import com.ljl.studyexammanagementsystem.service.OrganizationService;
import com.ljl.studyexammanagementsystem.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organization")
@Api(tags = "OrganizationController")
public class OrganizationController {

    @Autowired
    private OrganizationService organizationService;

    @GetMapping("/list")
    @ApiOperation(value = "获取组织机构列表", notes = "获取所有组织机构列表")
    public Result<List<Organization>> list() {
        List<Organization> organizations = organizationService.findAll();
        return Result.success(organizations);
    }

    @GetMapping("/tree/{parentId}")
    @ApiOperation(value = "获取组织机构树", notes = "根据父级ID获取组织机构树")
    public Result<List<Organization>> tree(@PathVariable Long parentId) {
        List<Organization> organizations = organizationService.findByParentId(parentId);
        return Result.success(organizations);
    }

    @PostMapping("/add")
    @ApiOperation(value = "添加组织机构", notes = "添加新的组织机构")
    public Result<Void> add(@RequestBody Organization organization) {
        organizationService.save(organization);
        return Result.success();
    }

    @PutMapping("/update/{id}")
    @ApiOperation(value = "更新组织机构", notes = "更新指定ID的组织机构")
    public Result<Void> update(@PathVariable Long id, @RequestBody Organization organization) {
        organizationService.update(id, organization);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "删除组织机构", notes = "删除指定ID的组织机构")
    public Result<Void> delete(@PathVariable Long id) {
        organizationService.deleteById(id);
        return Result.success();
    }
}
