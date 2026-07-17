package com.ljl.studyexammanagementsystem.service;

import com.ljl.studyexammanagementsystem.entity.SysMessage;
import com.ljl.studyexammanagementsystem.vo.Result;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface MessageService {

    Result<Page<SysMessage>> page(Integer pageNum, Integer pageSize, Byte readFlag, Long userId);

    Result<Map<String, Object>> unreadCount(Long userId);

    Result<Void> readOne(Long id, Long userId);

    Result<Void> readAll(Long userId);

    Result<SysMessage> detail(Long id, Long userId);

    Result<Void> delete(Long id, Long userId);

    Result<Void> manualPush(List<Long> userIds, String title, String content);
}
