package com.ljl.studyexammanagementsystem.service;

import com.ljl.studyexammanagementsystem.entity.LearnTask;
import com.ljl.studyexammanagementsystem.vo.Result;
import org.springframework.data.domain.Page;

import java.util.List;

public interface LearnTaskService {

    Result<Page<LearnTask>> page(Integer pageNum, Integer pageSize, String keyword, Long userId, Long orgId);

    Result<LearnTask> detail(Long id, Long userId, Long orgId);

    Result<Void> addDraft(LearnTask task, List<Long> materialIds, List<Long> userIds, Long createUserId);

    Result<Void> update(Long id, LearnTask task, List<Long> materialIds, List<Long> userIds, Long updateUserId);

    Result<Void> delete(Long id);
}
