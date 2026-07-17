package com.ljl.studyexammanagementsystem.controller;

import com.ljl.studyexammanagementsystem.entity.LoginLog;
import com.ljl.studyexammanagementsystem.entity.OperateLog;
import com.ljl.studyexammanagementsystem.service.LogService;
import com.ljl.studyexammanagementsystem.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Api(tags = "日志管理接口")
public class LogController {

    @Autowired
    private LogService logService;

    @GetMapping("/loginLog/list")
    @ApiOperation(value = "登录日志分页")
    public Result<Page<LoginLog>> loginLogList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        return logService.loginLogPage(pageNum, pageSize, keyword);
    }

    @GetMapping("/operLog/list")
    @ApiOperation(value = "操作日志分页")
    public Result<Page<OperateLog>> operLogList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        return logService.operLogPage(pageNum, pageSize, keyword);
    }
}
