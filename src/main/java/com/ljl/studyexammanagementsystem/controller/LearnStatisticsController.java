
package com.ljl.studyexammanagementsystem.controller;

import com.ljl.studyexammanagementsystem.service.LearnStatisticsService;
import com.ljl.studyexammanagementsystem.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/learn/statistics")
@Api(tags = "学习统计接口")
public class LearnStatisticsController {

    @Autowired
    private LearnStatisticsService learnStatisticsService;

    @GetMapping("/personal")
    @ApiOperation(value = "个人学习统计", notes = "按用户维度聚合：完成人数、未完成人数、平均学时、已签名人数；支持按任务、时间范围筛选；基于数据权限自动过滤")
    public Result<Map<String, Object>> personal(
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long orgId = (Long) request.getAttribute("orgId");
        return learnStatisticsService.personalStatistics(taskId, startTime, endTime, userId, orgId);
    }

    @GetMapping("/org")
    @ApiOperation(value = "组织维度学习统计", notes = "按组织聚合统计；超级管理员查看全组织，普通管理员仅查看下级组织；支持按任务、时间、组织筛选")
    public Result<Map<String, Object>> org(
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long currentOrgId = (Long) request.getAttribute("orgId");
        return learnStatisticsService.orgStatistics(taskId, orgId, startTime, endTime, userId, currentOrgId);
    }
}
