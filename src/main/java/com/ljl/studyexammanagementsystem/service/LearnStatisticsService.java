
package com.ljl.studyexammanagementsystem.service;

import com.ljl.studyexammanagementsystem.vo.Result;

import java.util.Map;

public interface LearnStatisticsService {

    /**
     * 个人学习统计：按用户维度聚合统计
     * @param taskId     任务ID（可选）
     * @param startTime  开始时间（可选，格式 yyyy-MM-dd HH:mm:ss）
     * @param endTime    结束时间（可选，格式 yyyy-MM-dd HH:mm:ss）
     * @param userId     当前登录用户ID
     * @param orgId      当前登录用户组织ID
     */
    Result<Map<String, Object>> personalStatistics(Long taskId, String startTime, String endTime, Long userId, Long orgId);

    /**
     * 组织维度学习统计：按组织聚合统计
     * @param taskId     任务ID（可选）
     * @param orgId      指定组织ID（可选，不传则按数据权限查全部可见组织）
     * @param startTime  开始时间（可选）
     * @param endTime    结束时间（可选）
     * @param userId     当前登录用户ID
     * @param currentOrgId 当前登录用户组织ID
     */
    Result<Map<String, Object>> orgStatistics(Long taskId, Long orgId, String startTime, String endTime, Long userId, Long currentOrgId);
}
