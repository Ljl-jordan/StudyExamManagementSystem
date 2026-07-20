package com.ljl.studyexammanagementsystem.controller;

import com.ljl.studyexammanagementsystem.service.LearnStudyRecordService;
import com.ljl.studyexammanagementsystem.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/learn/study")
@Api(tags = "学习记录与签名接口")
public class LearnStudyRecordController {

    @Autowired
    private LearnStudyRecordService learnStudyRecordService;

    @PostMapping("/report")
    @ApiOperation(value = "学时实时上报")
    public Result<Void> report(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long taskId = params.get("taskId") != null ? Long.valueOf(params.get("taskId").toString()) : null;
        Long materialId = params.get("materialId") != null ? Long.valueOf(params.get("materialId").toString()) : null;
        Integer studySeconds = params.get("studySeconds") != null ? Integer.valueOf(params.get("studySeconds").toString()) : null;
        if (taskId == null || materialId == null) {
            return Result.paramError("参数不完整");
        }
        return learnStudyRecordService.reportStudyTime(taskId, userId, materialId, studySeconds);
    }

    @GetMapping("/progress")
    @ApiOperation(value = "学员任务进度统计")
    public Result<List<Map<String, Object>>> progress(
            @RequestParam Long taskId,
            @RequestParam Long userId) {
        return learnStudyRecordService.getProgress(taskId, userId);
    }

    @PostMapping("/signature")
    @ApiOperation(value = "提交电子签名")
    public Result<Void> signature(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long taskId = params.get("taskId") != null ? Long.valueOf(params.get("taskId").toString()) : null;
        Long fileId = params.get("fileId") != null ? Long.valueOf(params.get("fileId").toString()) : null;
        if (taskId == null) {
            return Result.paramError("任务ID不能为空");
        }
        return learnStudyRecordService.submitSignature(taskId, userId, fileId);
    }
}
