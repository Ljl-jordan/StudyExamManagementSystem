
package com.ljl.studyexammanagementsystem.service.impl;

import com.ljl.studyexammanagementsystem.entity.LearnStudyRecord;
import com.ljl.studyexammanagementsystem.entity.LearnTaskUser;
import com.ljl.studyexammanagementsystem.entity.Organization;
import com.ljl.studyexammanagementsystem.entity.SysUser;
import com.ljl.studyexammanagementsystem.repository.*;
import com.ljl.studyexammanagementsystem.service.LearnStatisticsService;
import com.ljl.studyexammanagementsystem.utils.DataPermissionUtil;
import com.ljl.studyexammanagementsystem.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LearnStatisticsServiceImpl implements LearnStatisticsService {

    /** 单素材达标最低学习时长（秒），与 LearnStudyRecordServiceImpl 保持一致 */
    private static final int MATERIAL_PASS_SECONDS = 60;

    @Autowired
    private LearnTaskUserRepository learnTaskUserRepository;

    @Autowired
    private LearnStudyRecordRepository learnStudyRecordRepository;

    @Autowired
    private SysUserRepository sysUserRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private DataPermissionUtil dataPermissionUtil;

    /**
     * 个人学习统计
     * 逻辑：
     * 1. 根据数据权限确定可见用户范围（超级管理员看全部，普通管理员看本组织及下级，data_scope=2仅看自己）
     * 2. 按任务/时间范围筛选学习记录
     * 3. 聚合统计：完成人数、未完成人数、平均学时、已签名人数
     */
    @Override
    public Result<Map<String, Object>> personalStatistics(Long taskId, String startTime, String endTime, Long userId, Long orgId) {
        // 1. 获取数据权限范围，计算可见组织列表
        Byte dataScope = dataPermissionUtil.getDataScope(userId);
        List<Long> visibleOrgIds = getVisibleOrgIds(dataScope, orgId);

        // 2. 查询可见组织下全部用户
        List<SysUser> visibleUsers = sysUserRepository.findByIsDelete((byte) 0).stream()
                .filter(u -> visibleOrgIds.contains(u.getOrgId()))
                .collect(Collectors.toList());

        // data_scope=2 仅看本人
        if (dataScope == 2) {
            visibleUsers = visibleUsers.stream()
                    .filter(u -> u.getId().equals(userId))
                    .collect(Collectors.toList());
        }

        if (visibleUsers.isEmpty()) {
            return Result.success(buildEmptyStatistics());
        }

        List<Long> visibleUserIds = visibleUsers.stream().map(SysUser::getId).collect(Collectors.toList());

        // 3. 查询学员任务分配记录
        List<LearnTaskUser> taskUsers;
        if (taskId != null) {
            taskUsers = learnTaskUserRepository.findByTaskIdAndUserIds(taskId, visibleUserIds);
        } else {
            taskUsers = learnTaskUserRepository.findByOrgIds(visibleOrgIds);
            if (dataScope == 2) {
                taskUsers = taskUsers.stream().filter(tu -> tu.getUserId().equals(userId)).collect(Collectors.toList());
            }
        }

        if (taskUsers.isEmpty()) {
            return Result.success(buildEmptyStatistics());
        }

        // 4. 提取实际被分配的用户ID
        Set<Long> assignedUserIds = taskUsers.stream().map(LearnTaskUser::getUserId).collect(Collectors.toSet());

        // 5. 统计完成人数（learn_status=2）和已签名人数
        long completedCount = taskUsers.stream().filter(tu -> tu.getLearnStatus() != null && tu.getLearnStatus() == 2).count();
        long uncompletedCount = assignedUserIds.size() - completedCount;
        long signedCount = taskUsers.stream().filter(tu -> tu.getSignatureFileId() != null).count();

        // 6. 计算平均学时（基于学习记录累计时长）
        List<LearnStudyRecord> records;
        if (taskId != null) {
            records = learnStudyRecordRepository.findByTaskIdAndUserIds(taskId, new ArrayList<>(assignedUserIds));
        } else {
            records = learnStudyRecordRepository.findByUserIds(new ArrayList<>(assignedUserIds));
        }

        // 时间范围过滤（按最后上报时间）
        records = filterRecordsByTime(records, startTime, endTime);

        // 平均学时 = 全部学习记录累计时长之和 / 已分配用户数（单位：小时，保留2位小数）
        int totalSeconds = records.stream().mapToInt(LearnStudyRecord::getAccumulatedTime).sum();
        BigDecimal avgHours = BigDecimal.ZERO;
        if (assignedUserIds.size() > 0) {
            avgHours = BigDecimal.valueOf(totalSeconds)
                    .divide(BigDecimal.valueOf(assignedUserIds.size()), 2, RoundingMode.HALF_UP)
                    .divide(BigDecimal.valueOf(3600), 2, RoundingMode.HALF_UP);
        }

        // 7. 组装返回结果
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalUsers", assignedUserIds.size());
        result.put("completedCount", completedCount);
        result.put("uncompletedCount", uncompletedCount < 0 ? 0 : uncompletedCount);
        result.put("avgStudyHours", avgHours);
        result.put("signedCount", signedCount);
        result.put("totalStudySeconds", totalSeconds);

        return Result.success(result);
    }

    /**
     * 组织维度学习统计
     * 逻辑：
     * 1. 根据数据权限确定可见组织范围
     * 2. 逐个组织聚合统计完成人数、未完成人数、平均学时、已签名人数
     * 3. 超级管理员查看全部组织，普通管理员仅查看本组织及下级
     */
    @Override
    public Result<Map<String, Object>> orgStatistics(Long taskId, Long orgId, String startTime, String endTime, Long userId, Long currentOrgId) {
        Byte dataScope = dataPermissionUtil.getDataScope(userId);
        List<Long> visibleOrgIds = getVisibleOrgIds(dataScope, currentOrgId);

        // 如果指定了组织ID，校验是否在可见范围内（防止越权）
        if (orgId != null) {
            if (!visibleOrgIds.contains(orgId)) {
                return Result.forbidden("无权查看该组织数据");
            }
            visibleOrgIds = Collections.singletonList(orgId);
        }

        // 查询可见组织信息（用于名称映射）
        Map<Long, String> orgNameMap = new LinkedHashMap<>();
        for (Long oid : visibleOrgIds) {
            Organization org = organizationRepository.findById(oid).orElse(null);
            if (org != null) {
                orgNameMap.put(oid, org.getOrgName());
            }
        }

        // 按组织逐个聚合统计
        List<Map<String, Object>> orgStatsList = new ArrayList<>();
        for (Long oid : visibleOrgIds) {
            List<SysUser> orgUsers = sysUserRepository.findByOrgIdAndIsDelete(oid, (byte) 0);
            if (orgUsers.isEmpty()) {
                continue;
            }
            List<Long> orgUserIds = orgUsers.stream().map(SysUser::getId).collect(Collectors.toList());

            // 查询该组织学员的任务分配记录
            List<LearnTaskUser> taskUsers;
            if (taskId != null) {
                taskUsers = learnTaskUserRepository.findByTaskIdAndUserIds(taskId, orgUserIds);
            } else {
                taskUsers = learnTaskUserRepository.findByOrgIds(Collections.singletonList(oid));
            }

            if (taskUsers.isEmpty()) {
                continue;
            }

            Set<Long> assignedUids = taskUsers.stream().map(LearnTaskUser::getUserId).collect(Collectors.toSet());
            long completedCount = taskUsers.stream().filter(tu -> tu.getLearnStatus() != null && tu.getLearnStatus() == 2).count();
            long signedCount = taskUsers.stream().filter(tu -> tu.getSignatureFileId() != null).count();

            // 计算该组织平均学时
            List<LearnStudyRecord> records;
            if (taskId != null) {
                records = learnStudyRecordRepository.findByTaskIdAndUserIds(taskId, new ArrayList<>(assignedUids));
            } else {
                records = learnStudyRecordRepository.findByUserIds(new ArrayList<>(assignedUids));
            }
            records = filterRecordsByTime(records, startTime, endTime);

            int totalSeconds = records.stream().mapToInt(LearnStudyRecord::getAccumulatedTime).sum();
            BigDecimal avgHours = BigDecimal.ZERO;
            if (assignedUids.size() > 0) {
                avgHours = BigDecimal.valueOf(totalSeconds)
                        .divide(BigDecimal.valueOf(assignedUids.size()), 2, RoundingMode.HALF_UP)
                        .divide(BigDecimal.valueOf(3600), 2, RoundingMode.HALF_UP);
            }

            Map<String, Object> orgStat = new LinkedHashMap<>();
            orgStat.put("orgId", oid);
            orgStat.put("orgName", orgNameMap.getOrDefault(oid, ""));
            orgStat.put("totalUsers", assignedUids.size());
            orgStat.put("completedCount", completedCount);
            orgStat.put("uncompletedCount", assignedUids.size() - completedCount < 0 ? 0 : assignedUids.size() - completedCount);
            orgStat.put("avgStudyHours", avgHours);
            orgStat.put("signedCount", signedCount);
            orgStat.put("totalStudySeconds", totalSeconds);
            orgStatsList.add(orgStat);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", orgStatsList);
        result.put("orgCount", orgStatsList.size());
        return Result.success(result);
    }

    /**
     * 根据数据权限获取可见组织ID列表
     * data_scope=0：超级管理员，查看全部组织
     * data_scope=1：本组织及下级
     * data_scope=2：仅本人（此方法中返回当前组织）
     */
    private List<Long> getVisibleOrgIds(Byte dataScope, Long orgId) {
        if (dataScope == 0) {
            // 超级管理员：获取全部组织ID
            List<Organization> allOrgs = organizationRepository.findByIsDelete((byte) 0);
            return allOrgs.stream().map(Organization::getId).collect(Collectors.toList());
        }
        // data_scope=1 或 data_scope=2：返回当前组织及下级
        return dataPermissionUtil.getVisibleOrgIds(orgId);
    }

    /**
     * 按时间范围过滤学习记录（基于 lastReportTime）
     */
    private List<LearnStudyRecord> filterRecordsByTime(List<LearnStudyRecord> records, String startTime, String endTime) {
        if (startTime == null && endTime == null) {
            return records;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date start = startTime != null ? sdf.parse(startTime) : null;
            Date end = endTime != null ? sdf.parse(endTime) : null;
            return records.stream().filter(r -> {
                if (r.getLastReportTime() == null) return false;
                if (start != null && r.getLastReportTime().before(start)) return false;
                if (end != null && r.getLastReportTime().after(end)) return false;
                return true;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            return records;
        }
    }

    /** 构建空统计结果 */
    private Map<String, Object> buildEmptyStatistics() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalUsers", 0);
        result.put("completedCount", 0);
        result.put("uncompletedCount", 0);
        result.put("avgStudyHours", BigDecimal.ZERO);
        result.put("signedCount", 0);
        result.put("totalStudySeconds", 0);
        return result;
    }
}
