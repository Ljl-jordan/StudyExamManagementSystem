package com.ljl.studyexammanagementsystem.controller;

import com.ljl.studyexammanagementsystem.repository.SysUserRepository;
import com.ljl.studyexammanagementsystem.entity.SysUser;
import com.ljl.studyexammanagementsystem.vo.Result;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@Api(tags = "UserController")
public class UserController {

    @Autowired
    private SysUserRepository sysUserRepository;

    @ApiOperation(value = "获取用户列表", notes = "获取所有用户列表")
    @GetMapping("/list")
    public Result<List<SysUser>> list() {
        List<SysUser> users = sysUserRepository.findByIsDelete((byte) 0);
        return Result.success(users);
    }

    @ApiOperation(value = "添加用户", notes = "添加新的用户")
    @PostMapping("/add")
    public Result<Void> add(@RequestBody SysUser user) {
        sysUserRepository.save(user);
        return Result.success();
    }

    @ApiOperation(value = "删除用户", notes = "删除指定ID的用户")
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysUserRepository.deleteById(id);
        return Result.success();
    }

    @ApiOperation(value = "更新用户", notes = "更新指定ID的用户")
    @PutMapping("/update/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysUser user) {
        SysUser existing = sysUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        existing.setLoginAccount(user.getLoginAccount());
        existing.setUserName(user.getUserName());
        existing.setPhone(user.getPhone());
        existing.setOrgId(user.getOrgId());
        existing.setUserStatus(user.getUserStatus());
        sysUserRepository.save(existing);
        return Result.success();
    }
}
