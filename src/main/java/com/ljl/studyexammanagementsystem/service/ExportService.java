// 文件路径: src/main/java/com/ljl/studyexammanagementsystem/service/ExportService.java
package com.ljl.studyexammanagementsystem.service;

import com.ljl.studyexammanagementsystem.vo.Result;

import java.util.Map;

public interface ExportService {

    Result<Void> createTask(Long userId, String exportParams);

    Result<Map<String, Object>> taskList(Long userId, Integer pageNum, Integer pageSize);
}
