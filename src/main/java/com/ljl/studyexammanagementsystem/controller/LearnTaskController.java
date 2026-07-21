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
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @GetMapping("/{id}")
    @ApiOperation(value = "任务详情")
    public Result<LearnTask> detail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long orgId = (Long) request.getAttribute("orgId");
        return learnTaskService.detail(id, userId, orgId);
    }

    @PostMapping("/add")
    @ApiOperation(value = "新建草稿任务")
    public Result<Map<String, Object>> add(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        LearnTask task = buildTaskFromParams(params);
        List<Long> materialIds = toLongList(params.get("materialIds"));
        List<Long> userIds = toLongList(params.get("userIds"));
        Result<Long> result = learnTaskService.addDraft(task, materialIds, userIds, userId);
        Map<String, Object> data = new java.util.LinkedHashMap<>();
        data.put("taskId", result.getData());
        return Result.success(result.getMsg(),data);
    }

    @PutMapping("/edit/{id}")  // 修改路径：添加{id}路径参数
    @ApiOperation(value = "编辑任务（仅草稿）")
    public Result<Void> edit(@PathVariable Long id, @RequestBody Map<String, Object> params, HttpServletRequest request) {  // 添加@PathVariable Long id
        Long userId = (Long) request.getAttribute("userId");
        // 移除从params获取id的逻辑，直接使用路径参数id
        LearnTask task = buildTaskFromParams(params);
        List<Long> materialIds = toLongList(params.get("materialIds"));
        List<Long> userIds = toLongList(params.get("userIds"));
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

    @PostMapping("/allotOrg")  // 修改路径：assignByOrg → allotOrg
    @ApiOperation(value = "批量按组织分配学员")
    public Result<Void> assignByOrgs(@RequestBody Map<String, Object> params) {  // 方法名可保留，内部逻辑不变
        Long taskId = params.get("taskId") != null ? Long.valueOf(params.get("taskId").toString()) : null;
        if (taskId == null) {
            return Result.paramError("任务ID不能为空");
        }
        List<Long> orgIds = toLongList(params.get("orgIds"));
        return learnTaskService.assignByOrgs(taskId, orgIds);
    }

    @PostMapping("/allotUser")  // 修改路径：assignUsers → allotUser
    @ApiOperation(value = "单独分配学员")
    public Result<Void> assignUsers(@RequestBody Map<String, Object> params) {  // 方法名可保留，内部逻辑不变
        Long taskId = params.get("taskId") != null ? Long.valueOf(params.get("taskId").toString()) : null;
        if (taskId == null) {
            return Result.paramError("任务ID不能为空");
        }
        List<Long> userIds = toLongList(params.get("userIds"));
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            if (params.get("startTime") != null) {
                task.setStartTime(sdf.parse(params.get("startTime").toString()));
            }
            if (params.get("endTime") != null) {
                task.setEndTime(sdf.parse(params.get("endTime").toString()));
            }
        } catch (Exception e) {
        }
        return task;
    }
    //新增toLongList方法，将List<Number>转换为List<Long>

    //将前端传来的 Object 类型 ID 集合统一转换成 Long 类型 ID 列表，用于批量操作接口处理批量 ID 参数。
    private List<Long> toLongList(Object obj) {
        if (obj == null) return null;
        List<?> list = (List<?>) obj;//强转为泛型未知类型
        return list.stream().map(o -> Long.valueOf(o.toString())).collect(Collectors.toList());
    }
}
