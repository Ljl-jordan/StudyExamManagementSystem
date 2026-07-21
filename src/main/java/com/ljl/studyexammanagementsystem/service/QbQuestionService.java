package com.ljl.studyexammanagementsystem.service;

import com.ljl.studyexammanagementsystem.entity.QbQuestion;
import com.ljl.studyexammanagementsystem.vo.Result;
import org.springframework.data.domain.Page;

public interface QbQuestionService {

    /**
     * 试题分页列表
     */
    Result<Page<QbQuestion>> page(Integer pageNum, Integer pageSize, String keyword, Long categoryId, Long userId, Long orgId);

    /**
     * 试题详情
     */
    Result<QbQuestion> detail(Long id);

    /**
     * 新增试题
     */
    Result<Void> add(QbQuestion question, Object extraData, Long userId);

    /**
     * 编辑试题
     */
    Result<Void> update(Long id, QbQuestion question, Object extraData, Long userId);

    /**
     * 删除试题
     */
    Result<Void> delete(Long id);

    /**
     * 切换题型时清理旧题型附表数据
     */
    void cleanOldExtraData(Long questionId, Byte oldQuestionType);
}
