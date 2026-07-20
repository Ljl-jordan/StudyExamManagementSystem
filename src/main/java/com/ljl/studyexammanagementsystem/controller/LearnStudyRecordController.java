package com.ljl.studyexammanagementsystem.controller;

import com.ljl.studyexammanagementsystem.service.LearnStudyRecordService;
import com.ljl.studyexammanagementsystem.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    @GetMapping("/detail")
    @ApiOperation(value = "学习明细分页查询", notes = "支持按任务、组织、用户、时间范围筛选；基于数据权限自动过滤；返回组织名、用户名、任务名、素材名、学时、签名状态")
    public Result<Map<String, Object>> detail(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        Long currentOrgId = (Long) request.getAttribute("orgId");
        return learnStudyRecordService.detailPage(pageNum, pageSize, taskId, orgId, userId, startTime, endTime, currentUserId, currentOrgId);
    }

    @GetMapping("/export/sync")
    @ApiOperation(value = "同步导出学习台账Excel", notes = "最多导出5000条，超过返回405；导出字段：组织、用户、任务、素材、学时、签名状态")
    public void exportSync(
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            HttpServletRequest request,
            HttpServletResponse response) {
        Long currentUserId = (Long) request.getAttribute("userId");
        Long currentOrgId = (Long) request.getAttribute("orgId");
        learnStudyRecordService.exportSyncExcel(taskId, orgId, userId, startTime, endTime, currentUserId, currentOrgId, response);
    }

    @PostMapping("/export/async")
    @ApiOperation(value = "异步导出学习台账Excel", notes = "任务入库后异步处理，完成后推送消息通知下载")
    public Result<Void> exportAsync(
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        Long currentOrgId = (Long) request.getAttribute("orgId");
        return learnStudyRecordService.exportAsyncExcel(taskId, orgId, userId, startTime, endTime, currentUserId, currentOrgId);
    }
}
