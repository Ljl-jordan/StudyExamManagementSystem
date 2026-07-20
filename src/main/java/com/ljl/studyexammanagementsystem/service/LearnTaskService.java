package com.ljl.studyexammanagementsystem.service;

import com.ljl.studyexammanagementsystem.entity.LearnTask;
import com.ljl.studyexammanagementsystem.vo.Result;
import org.springframework.data.domain.Page;

import java.util.List;

public interface LearnTaskService {

    Result<Page<LearnTask>> page(Integer pageNum, Integer pageSize, String keyword, Long taskStatus, Long userId, Long orgId);

    Result<LearnTask> detail(Long id, Long userId, Long orgId);

    Result<Long> addDraft(LearnTask task, List<Long> materialIds, List<Long> userIds, Long createUserId);

    Result<Void> update(Long id, LearnTask task, List<Long> materialIds, List<Long> userIds, Long updateUserId);

    Result<Void> delete(Long id);

    Result<Void> publish(Long id, Long userId);

    Result<Void> assignByOrgs(Long taskId, List<Long> orgIds);

    Result<Void> assignUsers(Long taskId, List<Long> userIds);

    Result<Void> archive(Long id, Long userId);
}
