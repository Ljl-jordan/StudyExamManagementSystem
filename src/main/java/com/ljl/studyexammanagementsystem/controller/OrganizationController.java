package com.ljl.studyexammanagementsystem.controller;

import com.ljl.studyexammanagementsystem.entity.Organization;
import com.ljl.studyexammanagementsystem.service.OrganizationService;
import com.ljl.studyexammanagementsystem.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organization")
public class OrganizationController {

    @Autowired
    private OrganizationService organizationService;

    @GetMapping("/list")
    public Result<List<Organization>> list() {
        List<Organization> organizations = organizationService.findAll();
        return Result.success(organizations);
    }

    @GetMapping("/tree/{parentId}")
    public Result<List<Organization>> tree(@PathVariable Long parentId) {
        List<Organization> organizations = organizationService.findByParentId(parentId);
        return Result.success(organizations);
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody Organization organization) {
        organizationService.save(organization);
        return Result.success();
    }

    @PutMapping("/update/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Organization organization) {
        organizationService.update(id, organization);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        organizationService.deleteById(id);
        return Result.success();
    }
}
