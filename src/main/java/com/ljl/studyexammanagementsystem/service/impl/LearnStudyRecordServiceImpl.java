package com.ljl.studyexammanagementsystem.service.impl;

import com.ljl.studyexammanagementsystem.entity.LearnStudyRecord;
import com.ljl.studyexammanagementsystem.entity.LearnTaskMaterial;
import com.ljl.studyexammanagementsystem.entity.LearnTaskSignature;
import com.ljl.studyexammanagementsystem.entity.LearnTaskUser;
import com.ljl.studyexammanagementsystem.repository.LearnStudyRecordRepository;
import com.ljl.studyexammanagementsystem.repository.LearnTaskMaterialRepository;
import com.ljl.studyexammanagementsystem.repository.LearnTaskSignatureRepository;
import com.ljl.studyexammanagementsystem.repository.LearnTaskUserRepository;
import com.ljl.studyexammanagementsystem.service.LearnStudyRecordService;
import com.ljl.studyexammanagementsystem.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LearnStudyRecordServiceImpl implements LearnStudyRecordService {

    /** 防刷间隔：30秒内不允许重复上报 */
    private static final long ANTI_CHEAT_INTERVAL_MS = 30 * 1000L;
    /** 单素材达标最低学习时长（秒） */
    private static final int MATERIAL_PASS_SECONDS = 60;

    @Autowired
    private LearnStudyRecordRepository learnStudyRecordRepository;

    @Autowired
    private LearnTaskMaterialRepository learnTaskMaterialRepository;

    @Autowired
    private LearnTaskUserRepository learnTaskUserRepository;

    @Autowired
    private LearnTaskSignatureRepository learnTaskSignatureRepository;

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
            // 覆盖原有签名
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
}
