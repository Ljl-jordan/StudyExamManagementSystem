// 文件路径: src/main/java/com/ljl/studyexammanagementsystem/controller/ExportController.java
package com.ljl.studyexammanagementsystem.controller;

import com.ljl.studyexammanagementsystem.service.ExportService;
import com.ljl.studyexammanagementsystem.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/export")
@Api(tags = "异步导出管理")
public class ExportController {

    @Autowired
    private ExportService exportService;

    @PostMapping("/create")
    @ApiOperation(value = "创建导出任务", notes = "创建异步导出任务")
    public Result<Void> createTask(@RequestBody Map<String, String> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String exportParams = params.get("exportParams");
        return exportService.createTask(userId, exportParams);
    }

    @GetMapping("/list")
    @ApiOperation(value = "导出任务列表", notes = "分页查询当前用户的导出任务")
    public Result<Map<String, Object>> taskList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return exportService.taskList(userId, pageNum, pageSize);
    }
}
