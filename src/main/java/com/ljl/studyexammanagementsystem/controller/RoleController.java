package com.ljl.studyexammanagementsystem.controller;

import com.ljl.studyexammanagementsystem.service.RoleService;
import com.ljl.studyexammanagementsystem.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/role")
@Api(tags = "角色管理接口")
public class RoleController {

    @Autowired
    private RoleService roleService;

    /**
     * 角色分页列表
     */
    @GetMapping("/list")
    @ApiOperation(value = "角色分页列表")
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return roleService.page(pageNum, pageSize);
    }

    /**
     * 新增角色
     */
    @PostMapping("/add")
    @ApiOperation(value = "新增角色")
    public Result<Void> add(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return roleService.add(params, userId);
    }

    /**
     * 编辑角色
     */
    @PutMapping("/edit")
    @ApiOperation(value = "编辑角色")
    public Result<Void> edit(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long id = params.get("id") != null ? Long.valueOf(params.get("id").toString()) : null;
        if (id == null) {
            return Result.paramError("角色ID不能为空");
        }
        return roleService.update(id, params, userId);
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/del/{id}")
    @ApiOperation(value = "删除角色")
    public Result<Void> delete(@PathVariable Long id) {
        return roleService.delete(id);
    }

    /**
     * 复制角色（复制菜单权限与数据范围）
     */
    @PostMapping("/copy/{id}")
    @ApiOperation(value = "复制角色")
    public Result<Void> copy(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return roleService.copyRole(id, userId);
    }

    /**
     * 为角色批量分配菜单权限
     */
    @PostMapping("/allotMenu")
    @ApiOperation(value = "为角色批量分配菜单权限")
    public Result<Void> allotMenu(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long roleId = params.get("roleId") != null ? Long.valueOf(params.get("roleId").toString()) : null;
        if (roleId == null) {
            return Result.paramError("角色ID不能为空");
        }
        List<Long> menuIds = (List<Long>) params.get("menuIds");
        return roleService.allotMenu(roleId, menuIds, userId);
    }

    /**
     * 为用户分配角色
     */
    @PostMapping("/allotUser")
    @ApiOperation(value = "为用户分配角色")
    public Result<Void> allotUser(@RequestBody Map<String, Object> params) {
        List<Long> userIds = (List<Long>) params.get("userIds");
        Long roleId = params.get("roleId") != null ? Long.valueOf(params.get("roleId").toString()) : null;
        if (roleId == null) {
            return Result.paramError("角色ID不能为空");
        }
        return roleService.allotUser(userIds, roleId);
    }
}
