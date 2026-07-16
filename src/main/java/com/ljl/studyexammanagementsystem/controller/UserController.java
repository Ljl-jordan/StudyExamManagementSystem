package com.ljl.studyexammanagementsystem.controller;

import com.ljl.studyexammanagementsystem.dao.repository.SysUserRepository;
import com.ljl.studyexammanagementsystem.entity.SysUser;
import com.ljl.studyexammanagementsystem.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private SysUserRepository sysUserRepository;

    @GetMapping("/list")
    public Result<List<SysUser>> list() {
        List<SysUser> users = sysUserRepository.findByIsDelete((byte) 0);
        return Result.success(users);
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody SysUser user) {
        sysUserRepository.save(user);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysUserRepository.deleteById(id);
        return Result.success();
    }

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
