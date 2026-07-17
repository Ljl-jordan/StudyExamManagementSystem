package com.ljl.studyexammanagementsystem.service;

import com.ljl.studyexammanagementsystem.entity.LoginLog;
import com.ljl.studyexammanagementsystem.entity.OperateLog;
import com.ljl.studyexammanagementsystem.vo.Result;
import org.springframework.data.domain.Page;

public interface LogService {

    Result<Page<LoginLog>> loginLogPage(Integer pageNum, Integer pageSize, String keyword);

    Result<Page<OperateLog>> operLogPage(Integer pageNum, Integer pageSize, String keyword);
}
