package com.ljl.studyexammanagementsystem.service.impl;

import com.ljl.studyexammanagementsystem.entity.LoginLog;
import com.ljl.studyexammanagementsystem.entity.OperateLog;
import com.ljl.studyexammanagementsystem.repository.LoginLogRepository;
import com.ljl.studyexammanagementsystem.repository.OperateLogRepository;
import com.ljl.studyexammanagementsystem.service.LogService;
import com.ljl.studyexammanagementsystem.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class LogServiceImpl implements LogService {

    @Autowired
    private LoginLogRepository loginLogRepository;

    @Autowired
    private OperateLogRepository operateLogRepository;

    @Override
    public Result<Page<LoginLog>> loginLogPage(Integer pageNum, Integer pageSize, String keyword) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        if (keyword != null && !keyword.trim().isEmpty()) {
            return Result.success(loginLogRepository.findByKeyword(keyword.trim(), pageable));
        }
        return Result.success(loginLogRepository.findAllActive(pageable));
    }

    @Override
    public Result<Page<OperateLog>> operLogPage(Integer pageNum, Integer pageSize, String keyword) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        if (keyword != null && !keyword.trim().isEmpty()) {
            return Result.success(operateLogRepository.findByKeyword(keyword.trim(), pageable));
        }
        return Result.success(operateLogRepository.findAllActive(pageable));
    }
}
