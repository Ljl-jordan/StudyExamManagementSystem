package com.ljl.studyexammanagementsystem.service;

import com.ljl.studyexammanagementsystem.vo.Result;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface LearnStudyRecordService {

    Result<Void> reportStudyTime(Long taskId, Long userId, Long materialId, Integer studySeconds);

    Result<List<Map<String, Object>>> getProgress(Long taskId, Long userId);

    Result<Void> submitSignature(Long taskId, Long userId, Long fileId);

    /** 学习明细分页查询 */
    Result<Map<String, Object>> detailPage(Integer pageNum, Integer pageSize, Long taskId, Long orgId, Long userId,
                                           String startTime, String endTime, Long currentUserId, Long currentOrgId);

    /** 同步导出学习台账Excel（5000条上限） */
    void exportSyncExcel(Long taskId, Long orgId, Long userId, String startTime, String endTime,
                         Long currentUserId, Long currentOrgId, HttpServletResponse response);

    /** 异步导出学习台账Excel */
    Result<Void> exportAsyncExcel(Long taskId, Long orgId, Long userId, String startTime, String endTime,
                                  Long currentUserId, Long currentOrgId);
}
