package com.ljl.studyexammanagementsystem.service.impl;

import com.ljl.studyexammanagementsystem.entity.*;
import com.ljl.studyexammanagementsystem.repository.*;
import com.ljl.studyexammanagementsystem.service.LearnStudyRecordService;
import com.ljl.studyexammanagementsystem.utils.DataPermissionUtil;
import com.ljl.studyexammanagementsystem.utils.ExcelUtil;
import com.ljl.studyexammanagementsystem.utils.MessageUtil;
import com.ljl.studyexammanagementsystem.vo.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LearnStudyRecordServiceImpl implements LearnStudyRecordService {

    private static final Logger log = LoggerFactory.getLogger(LearnStudyRecordServiceImpl.class);

    /** 防刷间隔：30秒内不允许重复上报 */
    private static final long ANTI_CHEAT_INTERVAL_MS = 30 * 1000L;
    /** 单素材达标最低学习时长（秒） */
    private static final int MATERIAL_PASS_SECONDS = 60;
    /** 同步导出最大条数限制 */
    private static final int SYNC_EXPORT_MAX_COUNT = 5000;

    @Autowired
    private LearnStudyRecordRepository learnStudyRecordRepository;

    @Autowired
    private LearnTaskMaterialRepository learnTaskMaterialRepository;

    @Autowired
    private LearnTaskUserRepository learnTaskUserRepository;

    @Autowired
    private LearnTaskSignatureRepository learnTaskSignatureRepository;

    @Autowired
    private SysUserRepository sysUserRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private LearnTaskRepository learnTaskRepository;

    @Autowired
    private KbMaterialRepository kbMaterialRepository;

    @Autowired
    private AsyncExportRepository asyncExportRepository;

    @Autowired
    private SysFileRepository sysFileRepository;

    @Autowired
    private DataPermissionUtil dataPermissionUtil;

    @Autowired
    private MessageUtil messageUtil;

    @Value("${file.upload-path}")
    private String uploadPath;

    // ==================== 学时上报 ====================

    /**
     * 学时实时上报（含防刷拦截 + 累计时长累加）
     */
    @Override
    @Transactional
    public Result<Void> reportStudyTime(Long taskId, Long userId, Long materialId, Integer studySeconds) {
        if (studySeconds == null || studySeconds <= 0) {
            return Result.paramError("学习时长必须大于0");
        }

        LearnStudyRecord record = learnStudyRecordRepository.findActiveRecord(taskId, userId, materialId);

        if (record != null) {
            // 防刷校验：短时间内重复提交拦截
            if (record.getLastReportTime() != null) {
                long interval = System.currentTimeMillis() - record.getLastReportTime().getTime();
                if (interval < ANTI_CHEAT_INTERVAL_MS) {
                    return Result.paramError("提交过于频繁，请稍后再试");
                }
            }
            // 累计时长累加
            record.setAccumulatedTime(record.getAccumulatedTime() + studySeconds);
            record.setLastReportTime(new Date());
            record.setUpdateTime(new Date());
        } else {
            // 首次上报，新建记录
            record = new LearnStudyRecord();
            record.setTaskId(taskId);
            record.setUserId(userId);
            record.setMaterialId(materialId);
            record.setAccumulatedTime(studySeconds);
            record.setLastReportTime(new Date());
            record.setCreateTime(new Date());
            record.setIsDelete((byte) 0);
        }
        learnStudyRecordRepository.save(record);

        // 检查该任务下全部素材是否均已达标，自动更新学员完成状态
        checkAndUpdateTaskProgress(taskId, userId);

        return Result.success("上报成功", null);
    }

    // ==================== 进度统计 ====================

    /**
     * 学员任务进度统计（按素材维度聚合）
     */
    @Override
    public Result<List<Map<String, Object>>> getProgress(Long taskId, Long userId) {
        List<LearnTaskMaterial> taskMaterials = learnTaskMaterialRepository.findByTaskIdAndIsDelete(taskId, (byte) 0);
        List<LearnStudyRecord> records = learnStudyRecordRepository.findByTaskIdAndUserIdAndIsDelete(taskId, userId, (byte) 0);

        Map<Long, LearnStudyRecord> recordMap = records.stream()
                .collect(Collectors.toMap(LearnStudyRecord::getMaterialId, r -> r));

        List<Map<String, Object>> progressList = new ArrayList<>();
        for (LearnTaskMaterial tm : taskMaterials) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("materialId", tm.getMaterialId());
            item.put("sortOrder", tm.getSortOrder());
            LearnStudyRecord record = recordMap.get(tm.getMaterialId());
            item.put("accumulatedTime", record != null ? record.getAccumulatedTime() : 0);
            item.put("completed", record != null && record.getAccumulatedTime() >= MATERIAL_PASS_SECONDS);
            progressList.add(item);
        }
        return Result.success(progressList);
    }

    // ==================== 电子签名 ====================

    /**
     * 提交电子签名（校验全部素材达标 + 支持重复覆盖）
     */
    @Override
    @Transactional
    public Result<Void> submitSignature(Long taskId, Long userId, Long fileId) {
        if (fileId == null) {
            return Result.paramError("签名文件ID不能为空");
        }

        // 校验全部素材学习时长是否达标
        List<LearnTaskMaterial> taskMaterials = learnTaskMaterialRepository.findByTaskIdAndIsDelete(taskId, (byte) 0);
        if (taskMaterials.isEmpty()) {
            return Result.paramError("该任务未绑定素材");
        }
        List<LearnStudyRecord> records = learnStudyRecordRepository.findByTaskIdAndUserIdAndIsDelete(taskId, userId, (byte) 0);
        Map<Long, LearnStudyRecord> recordMap = records.stream()
                .collect(Collectors.toMap(LearnStudyRecord::getMaterialId, r -> r));

        for (LearnTaskMaterial tm : taskMaterials) {
            LearnStudyRecord record = recordMap.get(tm.getMaterialId());
            if (record == null || record.getAccumulatedTime() < MATERIAL_PASS_SECONDS) {
                return Result.businessBlock("存在未学完的素材，无法提交签名");
            }
        }

        // 查找已有签名（支持重复覆盖）
        LearnTaskSignature signature = learnTaskSignatureRepository.findByTaskIdAndUserIdAndIsDelete(taskId, userId, (byte) 0);
        if (signature != null) {
            signature.setFileId(fileId);
            signature.setSignTime(new Date());
        } else {
            signature = new LearnTaskSignature();
            signature.setTaskId(taskId);
            signature.setUserId(userId);
            signature.setFileId(fileId);
            signature.setSignTime(new Date());
            signature.setCreateTime(new Date());
            signature.setIsDelete((byte) 0);
        }
        learnTaskSignatureRepository.save(signature);

        // 同步更新 LearnTaskUser 的签名信息
        LearnTaskUser taskUser = learnTaskUserRepository.findByTaskIdAndUserIdAndIsDelete(taskId, userId, (byte) 0);
        if (taskUser != null) {
            taskUser.setSignatureFileId(fileId);
            taskUser.setSignTime(new Date());
            taskUser.setUpdateTime(new Date());
            learnTaskUserRepository.save(taskUser);
        }

        return Result.success("签名提交成功", null);
    }

    // ==================== 学习明细分页查询 ====================

    /**
     * 学习明细分页查询
     * 逻辑：
     * 1. 根据数据权限确定可见用户范围
     * 2. 以 LearnTaskUser 为主表进行分页，关联查询组织名、用户名、任务名、素材名、学时、签名状态
     * 3. 支持按任务ID、组织ID、用户ID、时间范围筛选
     */
    @Override
    public Result<Map<String, Object>> detailPage(Integer pageNum, Integer pageSize, Long taskId, Long orgId,
                                                  Long filterUserId, String startTime, String endTime,
                                                  Long currentUserId, Long currentOrgId) {
        // 1. 数据权限过滤
        Byte dataScope = dataPermissionUtil.getDataScope(currentUserId);
        List<Long> visibleOrgIds = getVisibleOrgIds(dataScope, currentOrgId);

        // 如果指定了组织ID，校验是否越权
        if (orgId != null) {
            if (!visibleOrgIds.contains(orgId)) {
                return Result.forbidden("无权查看该组织数据");
            }
            visibleOrgIds = Collections.singletonList(orgId);
        }
        final List<Long> finalVisibleOrgIds = visibleOrgIds;
        // 2. 查询可见组织下全部用户
        List<SysUser> visibleUsers = sysUserRepository.findByIsDelete((byte) 0).stream()
                .filter(u -> finalVisibleOrgIds.contains(u.getOrgId()))
                .collect(Collectors.toList());
        if (dataScope == 2) {
            visibleUsers = visibleUsers.stream().filter(u -> u.getId().equals(currentUserId)).collect(Collectors.toList());
        }
        if (filterUserId != null) {
            visibleUsers = visibleUsers.stream().filter(u -> u.getId().equals(filterUserId)).collect(Collectors.toList());
        }
        if (visibleUsers.isEmpty()) {
            return Result.success(buildEmptyPage(pageNum, pageSize));
        }

        List<Long> visibleUserIds = visibleUsers.stream().map(SysUser::getId).collect(Collectors.toList());
        // 构建用户ID→用户信息映射
        Map<Long, SysUser> userMap = visibleUsers.stream().collect(Collectors.toMap(SysUser::getId, u -> u));

        // 3. 查询 LearnTaskUser 记录（按条件过滤）
        List<LearnTaskUser> allTaskUsers;
        if (taskId != null) {
            allTaskUsers = learnTaskUserRepository.findByTaskIdAndUserIds(taskId, visibleUserIds);
        } else {
            allTaskUsers = learnTaskUserRepository.findByOrgIds(visibleOrgIds);
            if (dataScope == 2) {
                allTaskUsers = allTaskUsers.stream().filter(tu -> tu.getUserId().equals(currentUserId)).collect(Collectors.toList());
            }
            if (filterUserId != null) {
                allTaskUsers = allTaskUsers.stream().filter(tu -> tu.getUserId().equals(filterUserId)).collect(Collectors.toList());
            }
        }

        // 时间范围过滤（基于 updateTime 或 createTime）
        allTaskUsers = filterTaskUsersByTime(allTaskUsers, startTime, endTime);

        // 4. 手动分页
        int total = allTaskUsers.size();
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<LearnTaskUser> pageData = fromIndex < total ? allTaskUsers.subList(fromIndex, toIndex) : new ArrayList<>();

        // 5. 组装明细数据
        List<Map<String, Object>> detailList = new ArrayList<>();
        for (LearnTaskUser tu : pageData) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", tu.getId());
            item.put("taskId", tu.getTaskId());
            item.put("userId", tu.getUserId());

            // 用户名、组织名
            SysUser user = userMap.get(tu.getUserId());
            if (user != null) {
                item.put("userName", user.getUserName());
                item.put("orgId", user.getOrgId());
                Organization org = organizationRepository.findById(user.getOrgId()).orElse(null);
                item.put("orgName", org != null ? org.getOrgName() : "");
            } else {
                item.put("userName", "");
                item.put("orgName", "");
            }

            // 任务名
            LearnTask task = learnTaskRepository.findById(tu.getTaskId()).orElse(null);
            item.put("taskName", task != null ? task.getTaskName() : "");

            // 学习状态
            item.put("learnStatus", tu.getLearnStatus());
            String statusText = "未开始";
            if (tu.getLearnStatus() != null) {
                if (tu.getLearnStatus() == 1) statusText = "学习中";
                else if (tu.getLearnStatus() == 2) statusText = "已完成";
            }
            item.put("learnStatusText", statusText);

            // 累计学时（该任务下全部素材学时之和）
            List<LearnStudyRecord> studyRecords = learnStudyRecordRepository.findByTaskIdAndUserIdAndIsDelete(tu.getTaskId(), tu.getUserId(), (byte) 0);
            int totalSeconds = studyRecords.stream().mapToInt(LearnStudyRecord::getAccumulatedTime).sum();
            BigDecimal hours = BigDecimal.valueOf(totalSeconds).divide(BigDecimal.valueOf(3600), 2, RoundingMode.HALF_UP);
            item.put("totalStudySeconds", totalSeconds);
            item.put("totalStudyHours", hours);

            // 素材学习明细
            List<Map<String, Object>> materialDetails = new ArrayList<>();
            List<LearnTaskMaterial> taskMaterials = learnTaskMaterialRepository.findByTaskIdAndIsDelete(tu.getTaskId(), (byte) 0);
            Map<Long, LearnStudyRecord> recordMap = studyRecords.stream().collect(Collectors.toMap(LearnStudyRecord::getMaterialId, r -> r));
            for (LearnTaskMaterial tm : taskMaterials) {
                Map<String, Object> md = new LinkedHashMap<>();
                KbMaterial material = kbMaterialRepository.findById(tm.getMaterialId()).orElse(null);
                md.put("materialName", material != null ? material.getMaterialName() : "");
                LearnStudyRecord sr = recordMap.get(tm.getMaterialId());
                int accTime = sr != null ? sr.getAccumulatedTime() : 0;
                md.put("accumulatedSeconds", accTime);
                md.put("accumulatedHours", BigDecimal.valueOf(accTime).divide(BigDecimal.valueOf(3600), 2, RoundingMode.HALF_UP));
                md.put("completed", accTime >= MATERIAL_PASS_SECONDS);
                materialDetails.add(md);
            }
            item.put("materialDetails", materialDetails);

            // 签名状态
            item.put("signed", tu.getSignatureFileId() != null);
            item.put("signTime", tu.getSignTime());

            detailList.add(item);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", detailList);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return Result.success(result);
    }

    // ==================== 同步导出 ====================

    /**
     * 同步导出学习台账Excel
     * 逻辑：
     * 1. 查询全部符合条件的学习明细数据
     * 2. 超过5000条返回405提示
     * 3. 使用 ExcelUtil 生成 xlsx 文件并写入 response 输出流
     */
    @Override
    public void exportSyncExcel(Long taskId, Long orgId, Long filterUserId, String startTime, String endTime,
                                Long currentUserId, Long currentOrgId, HttpServletResponse response) {
        // 1. 数据权限过滤
        Byte dataScope = dataPermissionUtil.getDataScope(currentUserId);
        List<Long> visibleOrgIds = getVisibleOrgIds(dataScope, currentOrgId);

        if (orgId != null) {
            if (!visibleOrgIds.contains(orgId)) {
                writeErrorResponse(response, 403, "无权查看该组织数据");
                return;
            }
            visibleOrgIds = Collections.singletonList(orgId);
        }
        final List<Long> finalVisibleOrgIds = visibleOrgIds;

        // 2. 查询可见范围用户
        List<SysUser> visibleUsers = sysUserRepository.findByIsDelete((byte) 0).stream()
                .filter(u -> finalVisibleOrgIds.contains(u.getOrgId()))
                .collect(Collectors.toList());
        if (dataScope == 2) {
            visibleUsers = visibleUsers.stream().filter(u -> u.getId().equals(currentUserId)).collect(Collectors.toList());
        }
        if (filterUserId != null) {
            visibleUsers = visibleUsers.stream().filter(u -> u.getId().equals(filterUserId)).collect(Collectors.toList());
        }

        if (visibleUsers.isEmpty()) {
            writeErrorResponse(response, 400, "无可导出的数据");
            return;
        }

        List<Long> visibleUserIds = visibleUsers.stream().map(SysUser::getId).collect(Collectors.toList());
        Map<Long, SysUser> userMap = visibleUsers.stream().collect(Collectors.toMap(SysUser::getId, u -> u));

        // 3. 查询全部学习明细
        List<LearnTaskUser> allTaskUsers;
        if (taskId != null) {
            allTaskUsers = learnTaskUserRepository.findByTaskIdAndUserIds(taskId, visibleUserIds);
        } else {
            allTaskUsers = learnTaskUserRepository.findByOrgIds(visibleOrgIds);
            if (dataScope == 2) {
                allTaskUsers = allTaskUsers.stream().filter(tu -> tu.getUserId().equals(currentUserId)).collect(Collectors.toList());
            }
            if (filterUserId != null) {
                allTaskUsers = allTaskUsers.stream().filter(tu -> tu.getUserId().equals(filterUserId)).collect(Collectors.toList());
            }
        }
        allTaskUsers = filterTaskUsersByTime(allTaskUsers, startTime, endTime);

        // 4. 5000条拦截
        if (allTaskUsers.size() > SYNC_EXPORT_MAX_COUNT) {
            writeErrorResponse(response, 405, "导出数据超过5000条，请使用异步导出");
            return;
        }

        // 5. 构建Excel数据
        List<String> headers = Arrays.asList("组织名称", "用户姓名", "任务名称", "素材名称", "学习学时", "签名状态");
        List<List<String>> dataList = new ArrayList<>();

        for (LearnTaskUser tu : allTaskUsers) {
            SysUser user = userMap.get(tu.getUserId());
            String orgName = "";
            String userName = "";
            if (user != null) {
                userName = user.getUserName();
                Organization org = organizationRepository.findById(user.getOrgId()).orElse(null);
                orgName = org != null ? org.getOrgName() : "";
            }

            LearnTask task = learnTaskRepository.findById(tu.getTaskId()).orElse(null);
            String taskName = task != null ? task.getTaskName() : "";

            // 查询该用户在该任务下的学习记录
            List<LearnStudyRecord> studyRecords = learnStudyRecordRepository.findByTaskIdAndUserIdAndIsDelete(tu.getTaskId(), tu.getUserId(), (byte) 0);
            List<LearnTaskMaterial> taskMaterials = learnTaskMaterialRepository.findByTaskIdAndIsDelete(tu.getTaskId(), (byte) 0);
            Map<Long, LearnStudyRecord> recordMap = studyRecords.stream().collect(Collectors.toMap(LearnStudyRecord::getMaterialId, r -> r));

            String signedText = tu.getSignatureFileId() != null ? "已签名" : "未签名";

            // 每个素材一行
            if (taskMaterials.isEmpty()) {
                dataList.add(Arrays.asList(orgName, userName, taskName, "", "0", signedText));
            } else {
                for (LearnTaskMaterial tm : taskMaterials) {
                    KbMaterial material = kbMaterialRepository.findById(tm.getMaterialId()).orElse(null);
                    String materialName = material != null ? material.getMaterialName() : "";
                    LearnStudyRecord sr = recordMap.get(tm.getMaterialId());
                    int accTime = sr != null ? sr.getAccumulatedTime() : 0;
                    String hours = BigDecimal.valueOf(accTime).divide(BigDecimal.valueOf(3600), 2, RoundingMode.HALF_UP).toString();
                    dataList.add(Arrays.asList(orgName, userName, taskName, materialName, hours, signedText));
                }
            }
        }

        // 6. 写入响应流
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("学习台账_" + System.currentTimeMillis() + ".xlsx", "UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

            // 使用临时文件方式
            String tempPath = System.getProperty("java.io.tmpdir") + "/" + fileName;
            ExcelUtil.export(tempPath, "学习台账", headers, dataList);

            try (FileInputStream fis = new FileInputStream(tempPath);
                 OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int len;
                while ((len = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
                os.flush();
            }
            // 清理临时文件
            new File(tempPath).delete();
        } catch (Exception e) {
            log.error("同步导出学习台账失败: {}", e.getMessage());
            writeErrorResponse(response, 500, "导出失败，请稍后重试");
        }
    }

    // ==================== 异步导出 ====================

    /**
     * 异步导出学习台账Excel
     * 逻辑：
     * 1. 创建异步导出任务记录
     * 2. 异步线程中查询数据、生成Excel、保存文件记录
     * 3. 完成后通过站内消息通知用户下载
     */
    @Override
    public Result<Void> exportAsyncExcel(Long taskId, Long orgId, Long filterUserId, String startTime, String endTime,
                                         Long currentUserId, Long currentOrgId) {
        // 将筛选参数序列化为JSON存储
        StringBuilder paramsJson = new StringBuilder("{");
        if (taskId != null) paramsJson.append("\"taskId\":").append(taskId).append(",");
        if (orgId != null) paramsJson.append("\"orgId\":").append(orgId).append(",");
        if (filterUserId != null) paramsJson.append("\"userId\":").append(filterUserId).append(",");
        if (startTime != null) paramsJson.append("\"startTime\":\"").append(startTime).append("\",");
        if (endTime != null) paramsJson.append("\"endTime\":\"").append(endTime).append("\",");
        paramsJson.append("\"type\":\"learnDetail\"}");

        AsyncExport exportTask = new AsyncExport();
        exportTask.setUserId(currentUserId);
        exportTask.setTaskStatus((byte) 0);
        exportTask.setExportParams(paramsJson.toString());
        exportTask.setCreateTime(new Date());
        exportTask.setUpdateTime(new Date());
        exportTask.setIsDelete((byte) 0);
        asyncExportRepository.save(exportTask);

        // 异步处理导出
        processLearnExportAsync(exportTask.getId(), taskId, orgId, filterUserId, startTime, endTime, currentUserId, currentOrgId);

        return Result.success("导出任务已创建，完成后将通知您下载", null);
    }

    /**
     * 异步处理学习台账导出
     * 在独立线程中执行：查询数据 → 生成Excel → 保存文件 → 更新任务状态 → 推送消息通知
     */
    @Async
    public void processLearnExportAsync(Long exportTaskId, Long taskId, Long orgId, Long filterUserId,
                                        String startTime, String endTime, Long currentUserId, Long currentOrgId) {
        try {
            // 1. 数据权限过滤（与同步导出逻辑一致）
            Byte dataScope = dataPermissionUtil.getDataScope(currentUserId);
            List<Long> visibleOrgIds = getVisibleOrgIds(dataScope, currentOrgId);

            if (orgId != null && visibleOrgIds.contains(orgId)) {
                visibleOrgIds = Collections.singletonList(orgId);
            }
            final List<Long> finalVisibleOrgIds = visibleOrgIds;
            List<SysUser> visibleUsers = sysUserRepository.findByIsDelete((byte) 0).stream()
                    .filter(u -> finalVisibleOrgIds.contains(u.getOrgId()))
                    .collect(Collectors.toList());
            if (dataScope == 2) {
                visibleUsers = visibleUsers.stream().filter(u -> u.getId().equals(currentUserId)).collect(Collectors.toList());
            }
            if (filterUserId != null) {
                visibleUsers = visibleUsers.stream().filter(u -> u.getId().equals(filterUserId)).collect(Collectors.toList());
            }

            Map<Long, SysUser> userMap = visibleUsers.stream().collect(Collectors.toMap(SysUser::getId, u -> u, (a, b) -> a));
            List<Long> visibleUserIds = visibleUsers.stream().map(SysUser::getId).collect(Collectors.toList());

            // 2. 查询数据
            List<LearnTaskUser> allTaskUsers;
            if (taskId != null) {
                allTaskUsers = learnTaskUserRepository.findByTaskIdAndUserIds(taskId, visibleUserIds);
            } else {
                allTaskUsers = learnTaskUserRepository.findByOrgIds(visibleOrgIds);
            }
            allTaskUsers = filterTaskUsersByTime(allTaskUsers, startTime, endTime);

            // 3. 构建Excel数据
            List<String> headers = Arrays.asList("组织名称", "用户姓名", "任务名称", "素材名称", "学习学时", "签名状态");
            List<List<String>> dataList = new ArrayList<>();

            for (LearnTaskUser tu : allTaskUsers) {
                SysUser user = userMap.get(tu.getUserId());
                String orgName = "";
                String userName = "";
                if (user != null) {
                    userName = user.getUserName();
                    Organization org = organizationRepository.findById(user.getOrgId()).orElse(null);
                    orgName = org != null ? org.getOrgName() : "";
                }
                LearnTask task = learnTaskRepository.findById(tu.getTaskId()).orElse(null);
                String taskName = task != null ? task.getTaskName() : "";
                String signedText = tu.getSignatureFileId() != null ? "已签名" : "未签名";

                List<LearnStudyRecord> studyRecords = learnStudyRecordRepository.findByTaskIdAndUserIdAndIsDelete(tu.getTaskId(), tu.getUserId(), (byte) 0);
                List<LearnTaskMaterial> taskMaterials = learnTaskMaterialRepository.findByTaskIdAndIsDelete(tu.getTaskId(), (byte) 0);
                Map<Long, LearnStudyRecord> recordMap = studyRecords.stream().collect(Collectors.toMap(LearnStudyRecord::getMaterialId, r -> r));

                if (taskMaterials.isEmpty()) {
                    dataList.add(Arrays.asList(orgName, userName, taskName, "", "0", signedText));
                } else {
                    for (LearnTaskMaterial tm : taskMaterials) {
                        KbMaterial material = kbMaterialRepository.findById(tm.getMaterialId()).orElse(null);
                        String materialName = material != null ? material.getMaterialName() : "";
                        LearnStudyRecord sr = recordMap.get(tm.getMaterialId());
                        int accTime = sr != null ? sr.getAccumulatedTime() : 0;
                        String hours = BigDecimal.valueOf(accTime).divide(BigDecimal.valueOf(3600), 2, RoundingMode.HALF_UP).toString();
                        dataList.add(Arrays.asList(orgName, userName, taskName, materialName, hours, signedText));
                    }
                }
            }

            // 4. 生成Excel文件
            String dateDir = "export";
            File dir = new File(uploadPath + dateDir);
            if (!dir.exists()) dir.mkdirs();
            String fileName = "learn_detail_" + exportTaskId + "_" + System.currentTimeMillis() + ".xlsx";
            String fullPath = dir.getAbsolutePath() + "/" + fileName;
            ExcelUtil.export(fullPath, "学习台账", headers, dataList);

            // 5. 保存文件记录
            SysFile sysFile = new SysFile();
            sysFile.setFileName(fileName);
            sysFile.setFileUrl(dateDir + "/" + fileName);
            sysFile.setFileSize(new File(fullPath).length());
            sysFile.setSuffix("xlsx");
            sysFile.setBusinessType("learnExport");
            sysFile.setBusinessId(exportTaskId);
            sysFile.setUploadUserId(currentUserId);
            sysFile.setUploadTime(new Date());
            sysFile.setIsDelete((byte) 0);
            sysFileRepository.save(sysFile);

            // 6. 更新导出任务状态为完成
            AsyncExport exportTask = asyncExportRepository.findById(exportTaskId).orElse(null);
            if (exportTask != null) {
                exportTask.setFileId(sysFile.getId());
                exportTask.setTaskStatus((byte) 1);
                exportTask.setUpdateTime(new Date());
                asyncExportRepository.save(exportTask);
            }

            // 7. 推送站内消息通知用户下载
            messageUtil.pushSingle(currentUserId, "learnExport", exportTaskId,
                    "学习台账导出完成", "您的学习台账已导出完成，共" + dataList.size() + "条数据，请及时下载。");

            log.info("学习台账异步导出任务[{}]已完成，共{}条数据，文件ID={}", exportTaskId, dataList.size(), sysFile.getId());
        } catch (Exception e) {
            log.error("学习台账异步导出任务[{}]处理失败: {}", exportTaskId, e.getMessage());
            AsyncExport exportTask = asyncExportRepository.findById(exportTaskId).orElse(null);
            if (exportTask != null) {
                exportTask.setTaskStatus((byte) 2);
                exportTask.setUpdateTime(new Date());
                asyncExportRepository.save(exportTask);
            }
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 检查并更新任务完成状态
     * 当学员所有素材累计时长均达标时，自动标记任务为"已完成"
     */
    private void checkAndUpdateTaskProgress(Long taskId, Long userId) {
        List<LearnTaskMaterial> taskMaterials = learnTaskMaterialRepository.findByTaskIdAndIsDelete(taskId, (byte) 0);
        List<LearnStudyRecord> records = learnStudyRecordRepository.findByTaskIdAndUserIdAndIsDelete(taskId, userId, (byte) 0);
        Map<Long, LearnStudyRecord> recordMap = records.stream()
                .collect(Collectors.toMap(LearnStudyRecord::getMaterialId, r -> r));

        boolean allCompleted = true;
        for (LearnTaskMaterial tm : taskMaterials) {
            LearnStudyRecord record = recordMap.get(tm.getMaterialId());
            if (record == null || record.getAccumulatedTime() < MATERIAL_PASS_SECONDS) {
                allCompleted = false;
                break;
            }
        }

        if (allCompleted) {
            LearnTaskUser taskUser = learnTaskUserRepository.findByTaskIdAndUserIdAndIsDelete(taskId, userId, (byte) 0);
            if (taskUser != null && taskUser.getLearnStatus() != 2) {
                taskUser.setLearnStatus((byte) 2);
                taskUser.setUpdateTime(new Date());
                learnTaskUserRepository.save(taskUser);
            }
        } else {
            // 有学习记录但未全部完成 → 标记为"学习中"
            LearnTaskUser taskUser = learnTaskUserRepository.findByTaskIdAndUserIdAndIsDelete(taskId, userId, (byte) 0);
            if (taskUser != null && taskUser.getLearnStatus() == 0) {
                taskUser.setLearnStatus((byte) 1);
                taskUser.setUpdateTime(new Date());
                learnTaskUserRepository.save(taskUser);
            }
        }
    }

    /**
     * 根据数据权限获取可见组织ID列表
     */
    private List<Long> getVisibleOrgIds(Byte dataScope, Long orgId) {
        if (dataScope == 0) {
            List<Organization> allOrgs = organizationRepository.findByIsDelete((byte) 0);
            return allOrgs.stream().map(Organization::getId).collect(Collectors.toList());
        }
        return dataPermissionUtil.getVisibleOrgIds(orgId);
    }

    /**
     * 按时间范围过滤 LearnTaskUser 记录（基于 updateTime）
     */
    private List<LearnTaskUser> filterTaskUsersByTime(List<LearnTaskUser> taskUsers, String startTime, String endTime) {
        if (startTime == null && endTime == null) {
            return taskUsers;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date start = startTime != null ? sdf.parse(startTime) : null;
            Date end = endTime != null ? sdf.parse(endTime) : null;
            return taskUsers.stream().filter(tu -> {
                Date refTime = tu.getUpdateTime() != null ? tu.getUpdateTime() : tu.getCreateTime();
                if (refTime == null) return false;
                if (start != null && refTime.before(start)) return false;
                if (end != null && refTime.after(end)) return false;
                return true;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            return taskUsers;
        }
    }

    /** 构建空分页结果 */
    private Map<String, Object> buildEmptyPage(Integer pageNum, Integer pageSize) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", new ArrayList<>());
        result.put("total", 0);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        return result;
    }

    /** 向 response 写入错误响应（用于同步导出异常场景） */
    private void writeErrorResponse(HttpServletResponse response, int code, String msg) {
        try {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(code == 405 ? 200 : code);
            response.getWriter().write("{\"code\":" + code + ",\"msg\":\"" + msg + "\",\"data\":null}");
            response.getWriter().flush();
        } catch (IOException e) {
            log.error("写入错误响应失败: {}", e.getMessage());
        }
    }
}
