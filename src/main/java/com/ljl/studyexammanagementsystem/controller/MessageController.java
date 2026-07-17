package com.ljl.studyexammanagementsystem.controller;

import com.ljl.studyexammanagementsystem.entity.SysMessage;
import com.ljl.studyexammanagementsystem.service.MessageService;
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
@RequestMapping("/api/message")
@Api(tags = "站内消息接口")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @GetMapping("/list")
    @ApiOperation(value = "消息分页列表")
    public Result<Page<SysMessage>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Byte readFlag,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return messageService.page(pageNum, pageSize, readFlag, userId);
    }

    @GetMapping("/unread/count")
    @ApiOperation(value = "查询未读消息数")
    public Result<Map<String, Object>> unreadCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return messageService.unreadCount(userId);
    }

    @PutMapping("/read/{id}")
    @ApiOperation(value = "标记消息已读")
    public Result<Void> readOne(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return messageService.readOne(id, userId);
    }

    @PutMapping("/read/all")
    @ApiOperation(value = "一键全部已读")
    public Result<Void> readAll(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return messageService.readAll(userId);
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "消息详情")
    public Result<SysMessage> detail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return messageService.detail(id, userId);
    }

    @DeleteMapping("/del/{id}")
    @ApiOperation(value = "删除消息")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return messageService.delete(id, userId);
    }

    @PostMapping("/manualPush")
    @ApiOperation(value = "手动批量推送消息")
    public Result<Void> manualPush(@RequestBody Map<String, Object> params) {
        List<Long> userIds = (List<Long>) params.get("userIds");
        String title = (String) params.get("title");
        String content = (String) params.get("content");
        return messageService.manualPush(userIds, title, content);
    }
}
