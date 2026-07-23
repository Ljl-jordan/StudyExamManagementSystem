package com.ljl.studyexammanagementsystem.service;

import com.ljl.studyexammanagementsystem.entity.ExamPaper;
import com.ljl.studyexammanagementsystem.vo.Result;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

public interface ExamPaperService {

    Result<Page<ExamPaper>> page(Integer pageNum, Integer pageSize, String keyword, Long userId, Long orgId);

    Result<ExamPaper> detail(Long id);

    Result<Void> add(ExamPaper paper, Long userId);

    Result<Void> update(Long id, ExamPaper paper, Long userId);

    Result<String> delete(Long id);

    // Day 17 新增：四种组卷接口
    Result<String> manualAssembly(Long paperId, String questionIdsJson); // 手动选题

    Result<String> fixedAssembly(Long paperId, String assemblyConfigJson); // 固定抽题

    Result<String> dynamicAssembly(Long paperId, String assemblyConfigJson); // 动态抽题

    Result<String> importAssembly(Long paperId, String questionIdsJson); // Excel导入试题

    @Transactional
    Result<String> publish(Long id, Long userId);

    // Day 18 新增：取消发布试卷
    @Transactional
    Result<String> unpublish(Long id, Long userId);

    // Day 18 新增：归档试卷
    @Transactional
    Result<String> archive(Long id, Long userId);
}
