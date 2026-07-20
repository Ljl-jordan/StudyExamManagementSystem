package com.ljl.studyexammanagementsystem.controller;

import com.ljl.studyexammanagementsystem.entity.LearnTask;
import com.ljl.studyexammanagementsystem.service.LearnTaskService;
import com.ljl.studyexammanagementsystem.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/learn/task")
@Api(tags = "学习任务管理接口")
public class LearnTaskController {

    @Autowired
    private LearnTaskService learnTaskService;

    @GetMapping("/list")
    @ApiOperation(value = "任务分页列表")
    public Result<Page<LearnTask>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long taskStatus,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long orgId = (Long) request.getAttribute("orgId");
        return learnTaskService.page(pageNum, pageSize, keyword, taskStatus, userId, orgId);
    }

    @GetMapping("/detail/{id}")
    @ApiOperation(value = "任务详情")
    public Result<LearnTask> detail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long orgId = (Long) request.getAttribute("orgId");
        return learnTaskService.detail(id, userId, orgId);
    }

    @PostMapping("/add")
    @ApiOperation(value = "新建草稿任务")
    public Result<Void> add(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        LearnTask task = buildTaskFromParams(params);
        List<Long> materialIds = (List<Long>) params.get("materialIds");
        List<Long> userIds = (List<Long>) params.get("userIds");
        return learnTaskService.addDraft(task, materialIds, userIds, userId);
    }

    @PutMapping("/edit")
    @ApiOperation(value = "编辑任务（仅草稿）")
    public Result<Void> edit(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long id = params.get("id") != null ? Long.valueOf(params.get("id").toString()) : null;
        if (id == null) {
            return Result.paramError("任务ID不能为空");
        }
        LearnTask task = buildTaskFromParams(params);
        List<Long> materialIds = (List<Long>) params.get("materialIds");
        List<Long> userIds = (List<Long>) params.get("userIds");
        return learnTaskService.update(id, task, materialIds, userIds, userId);
    }

    @DeleteMapping("/del/{id}")
    @ApiOperation(value = "删除任务（仅草稿）")
    public Result<Void> delete(@PathVariable Long id) {
        return learnTaskService.delete(id);
    }

    @PutMapping("/publish/{id}")
    @ApiOperation(value = "任务下发")
    public Result<Void> publish(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return learnTaskService.publish(id, userId);
    }

    @PostMapping("/assignByOrg")
    @ApiOperation(value = "批量按组织分配学员")
    public Result<Void> assignByOrgs(@RequestBody Map<String, Object> params) {
        Long taskId = params.get("taskId") != null ? Long.valueOf(params.get("taskId").toString()) : null;
        if (taskId == null) {
            return Result.paramError("任务ID不能为空");
        }
        List<Long> orgIds = (List<Long>) params.get("orgIds");
        return learnTaskService.assignByOrgs(taskId, orgIds);
    }

    @PostMapping("/assignUsers")
    @ApiOperation(value = "单独分配学员")
    public Result<Void> assignUsers(@RequestBody Map<String, Object> params) {
        Long taskId = params.get("taskId") != null ? Long.valueOf(params.get("taskId").toString()) : null;
        if (taskId == null) {
            return Result.paramError("任务ID不能为空");
        }
        List<Long> userIds = (List<Long>) params.get("userIds");
        return learnTaskService.assignUsers(taskId, userIds);
    }

    @PutMapping("/archive/{id}")
    @ApiOperation(value = "任务归档")
    public Result<Void> archive(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return learnTaskService.archive(id, userId);
    }

    private LearnTask buildTaskFromParams(Map<String, Object> params) {
        LearnTask task = new LearnTask();
        if (params.get("id") != null) {
            task.setId(Long.valueOf(params.get("id").toString()));
        }
        if (params.get("taskName") != null) {
            task.setTaskName(params.get("taskName").toString());
        }
        if (params.get("taskDesc") != null) {
            task.setTaskDesc(params.get("taskDesc").toString());
        }
        return task;
    }
}
