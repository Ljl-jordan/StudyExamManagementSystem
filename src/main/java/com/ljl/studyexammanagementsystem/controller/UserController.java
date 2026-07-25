package com.ljl.studyexammanagementsystem.controller;

import com.ljl.studyexammanagementsystem.entity.SysUser;
import com.ljl.studyexammanagementsystem.service.UserService;
import com.ljl.studyexammanagementsystem.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@Api(tags = "用户管理接口")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    @ApiOperation(value = "用户分页列表", notes = "支持数据权限过滤、关键字搜索、组织筛选")
        public Result<Page<SysUser>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long orgId,
            HttpServletRequest request) {
        return userService.page(pageNum, pageSize, keyword, orgId, request);
    }

    @PostMapping("/add")
    @ApiOperation(value = "新增用户")
    public Result<Void> add(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.add(params, userId);
    }

    @PutMapping("/edit")
    @ApiOperation(value = "编辑用户")
    public Result<Void> edit(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long id = params.get("id") != null ? Long.valueOf(params.get("id").toString()) : null;
        if (id == null) {
            return Result.paramError("用户ID不能为空");
        }
        return userService.update(id, params, userId);
    }

    @DeleteMapping("/del/{id}")
    @ApiOperation(value = "删除用户")
    public Result<Void> delete(@PathVariable Long id) {
        return userService.delete(id);
    }

    @PostMapping("/resetUserPwd")
    @ApiOperation(value = "重置用户密码")
    public Result<Void> resetPassword(@RequestBody Map<String, String> params, HttpServletRequest request) {
        Long operatorId = (Long) request.getAttribute("userId");
        Long targetUserId = params.get("userId") != null ? Long.valueOf(params.get("userId")) : null;
        String newPassword = params.get("newPassword");
        if (targetUserId == null) {
            return Result.paramError("用户ID不能为空");
        }
        return userService.resetPassword(targetUserId, newPassword, operatorId);
    }

    @PostMapping("/import")
    @ApiOperation(value = "Excel批量导入用户")
    public Result<List<Map<String, Object>>> importUsers(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.importUsers(file, userId);
    }

    @PostMapping("/batchUpdateOrg")
    @ApiOperation(value = "批量调整用户组织")
    public Result<Void> batchUpdateOrg(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long operatorId = (Long) request.getAttribute("userId");
        List<Long> userIds = (List<Long>) params.get("userIds");
        Long orgId = params.get("orgId") != null ? Long.valueOf(params.get("orgId").toString()) : null;
        return userService.batchUpdateOrg(userIds, orgId, operatorId);
    }

    @PostMapping("/batchDisable")
    @ApiOperation(value = "批量禁用用户")
    public Result<Void> batchDisable(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long operatorId = (Long) request.getAttribute("userId");
        List<Long> userIds = (List<Long>) params.get("userIds");
        return userService.batchDisable(userIds, operatorId);
    }

    @PostMapping("/batchEnable")
    @ApiOperation(value = "批量启用用户")
    public Result<Void> batchEnable(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long operatorId = (Long) request.getAttribute("userId");
        List<Long> userIds = (List<Long>) params.get("userIds");
        return userService.batchEnable(userIds, operatorId);
    }
    @GetMapping("/selectUser")
    @ApiOperation(value = "用户下拉选择（分页）")
    public Result<Map<String, Object>> selectUsers(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        return userService.selectUsers(pageNum, pageSize, keyword);
    }
}
