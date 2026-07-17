package com.ljl.studyexammanagementsystem.service.impl;

import com.ljl.studyexammanagementsystem.entity.SysMessage;
import com.ljl.studyexammanagementsystem.repository.MessageRepository;
import com.ljl.studyexammanagementsystem.service.MessageService;
import com.ljl.studyexammanagementsystem.utils.MessageUtil;
import com.ljl.studyexammanagementsystem.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MessageUtil messageUtil;

    @Override
    public Result<Page<SysMessage>> page(Integer pageNum, Integer pageSize, Byte readFlag, Long userId) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        if (readFlag != null && readFlag == 0) {
            return Result.success(messageRepository.findUnreadByUserId(userId, pageable));
        }
        return Result.success(messageRepository.findByUserId(userId, pageable));
    }

    @Override
    public Result<Map<String, Object>> unreadCount(Long userId) {
        long count = messageRepository.countUnreadByUserId(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("count", count);
        return Result.success(data);
    }

    @Override
    @Transactional
    public Result<Void> readOne(Long id, Long userId) {
        SysMessage msg = messageRepository.findById(id).orElse(null);
        if (msg == null || msg.getIsDelete() == 1) {
            return Result.paramError("消息不存在");
        }
        if (!msg.getReceiveUserId().equals(userId)) {
            return Result.forbidden("无权操作他人消息");
        }
        msg.setReadFlag((byte) 1);
        messageRepository.save(msg);
        return Result.success("已标记已读", null);
    }

    @Override
    @Transactional
    public Result<Void> readAll(Long userId) {
        List<SysMessage> unreadList = messageRepository.findAllUnreadByUserId(userId);
        for (SysMessage msg : unreadList) {
            msg.setReadFlag((byte) 1);
            messageRepository.save(msg);
        }
        return Result.success("全部已读", null);
    }

    @Override
    @Transactional
    public Result<SysMessage> detail(Long id, Long userId) {
        SysMessage msg = messageRepository.findById(id).orElse(null);
        if (msg == null || msg.getIsDelete() == 1) {
            return Result.paramError("消息不存在");
        }
        if (!msg.getReceiveUserId().equals(userId)) {
            return Result.forbidden("无权查看他人消息");
        }
        if (msg.getReadFlag() == 0) {
            msg.setReadFlag((byte) 1);
            messageRepository.save(msg);
        }
        return Result.success(msg);
    }

    @Override
    @Transactional
    public Result<Void> delete(Long id, Long userId) {
        SysMessage msg = messageRepository.findById(id).orElse(null);
        if (msg == null || msg.getIsDelete() == 1) {
            return Result.paramError("消息不存在");
        }
        if (!msg.getReceiveUserId().equals(userId)) {
            return Result.forbidden("无权删除他人消息");
        }
        msg.setIsDelete((byte) 1);
        messageRepository.save(msg);
        return Result.success("删除成功", null);
    }

    @Override
    public Result<Void> manualPush(List<Long> userIds, String title, String content) {
        if (userIds == null || userIds.isEmpty()) {
            return Result.paramError("请选择接收用户");
        }
        if (title == null || title.trim().isEmpty()) {
            return Result.paramError("消息标题不能为空");
        }
        messageUtil.pushBatch(userIds, "manual", 0L, title, content);
        return Result.success("推送成功", null);
    }
}
