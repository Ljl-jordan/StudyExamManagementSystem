package com.ljl.studyexammanagementsystem.service;

import com.ljl.studyexammanagementsystem.entity.LearnTaskUser;
import com.ljl.studyexammanagementsystem.vo.Result;

import java.util.List;

public interface LearnTaskUserService {
    Result<Void> startLearning(Long taskId, Long userId);
    Result<Void> completeTask(Long taskId, Long userId);
    Result<List<LearnTaskUser>> getUserTasks(Long userId);
}
