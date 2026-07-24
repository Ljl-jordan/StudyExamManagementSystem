package com.ljl.studyexammanagementsystem.service.impl;

import com.ljl.studyexammanagementsystem.entity.LearnTask;
import com.ljl.studyexammanagementsystem.entity.LearnTaskUser;
import com.ljl.studyexammanagementsystem.repository.LearnTaskRepository;
import com.ljl.studyexammanagementsystem.repository.LearnTaskUserRepository;
import com.ljl.studyexammanagementsystem.service.LearnTaskUserService;
import com.ljl.studyexammanagementsystem.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class LearnTaskUserServiceImpl implements LearnTaskUserService {

    @Autowired
    private LearnTaskUserRepository learnTaskUserRepository;

    @Autowired
    private LearnTaskRepository learnTaskRepository;

    @Override
    @Transactional
    public Result<Void> startLearning(Long taskId, Long userId) {
        // 检查任务是否存在
        LearnTask task = learnTaskRepository.findById(taskId).orElse(null);
        if (task == null || task.getIsDelete() == 1) {
            return Result.paramError("学习任务不存在");
        }

        // 检查任务是否已归档（状态为2）
        if (task.getTaskStatus() == 2) {
            return Result.businessBlock("该学习任务已归档，无法开始学习");
        }

        // 检查用户是否被分配到该任务
        LearnTaskUser taskUser = learnTaskUserRepository.findByTaskIdAndUserIdAndIsDelete(taskId, userId, (byte) 0);
        if (taskUser == null) {
            return Result.businessBlock("您未被分配到该学习任务");
        }

        // 检查任务是否在有效期内
        Date now = new Date();
        if (task.getStartTime() != null && now.before(task.getStartTime())) {
            return Result.businessBlock("学习任务尚未开始");
        }
        if (task.getEndTime() != null && now.after(task.getEndTime())) {
            return Result.businessBlock("学习任务已结束");
        }

        // 更新学习状态为"学习中"
        taskUser.setLearnStatus((byte) 1); // 1-学习中
        taskUser.setUpdateTime(new Date());
        learnTaskUserRepository.save(taskUser);

        return Result.success("开始学习成功", null);
    }

    @Override
    public Result<Void> completeTask(Long taskId, Long userId) {
        // 检查任务是否存在
        LearnTask task = learnTaskRepository.findById(taskId).orElse(null);
        if (task == null || task.getIsDelete() == 1) {
            return Result.paramError("学习任务不存在");
        }

        // 检查任务是否已归档（状态为2）
        if (task.getTaskStatus() == 2) {
            return Result.businessBlock("该学习任务已归档，无法完成操作");
        }

        // 检查用户是否被分配到该任务
        LearnTaskUser taskUser = learnTaskUserRepository.findByTaskIdAndUserIdAndIsDelete(taskId, userId, (byte) 0);
        if (taskUser == null) {
            return Result.businessBlock("您未被分配到该学习任务");
        }

        // 更新学习状态为"已完成"
        taskUser.setLearnStatus((byte) 2); // 2-已完成
        taskUser.setUpdateTime(new Date());
        learnTaskUserRepository.save(taskUser);

        return Result.success("完成学习任务成功", null);
    }

    @Override
    public Result<List<LearnTaskUser>> getUserTasks(Long userId) {
        List<LearnTaskUser> taskUsers = learnTaskUserRepository.findByUserIdAndIsDelete(userId, (byte) 0);
        return Result.success(taskUsers);
    }
}
