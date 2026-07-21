package com.ljl.studyexammanagementsystem.service;

import com.ljl.studyexammanagementsystem.entity.QbCategory;
import com.ljl.studyexammanagementsystem.vo.Result;
import org.springframework.data.domain.Page;

import java.util.List;

public interface QbCategoryService {

    /**
     * 获取分类树形结构
     */
    Result<List<QbCategory>> getTree(Long userId, Long orgId);

    /**
     * 分类分页列表
     */
    Result<Page<QbCategory>> page(Integer pageNum, Integer pageSize, String keyword, Long userId, Long orgId);

    /**
     * 新增分类
     */
    Result<Void> add(QbCategory category, Long userId, Long orgId);

    /**
     * 编辑分类
     */
    Result<Void> update(Long id, QbCategory category, Long userId);

    /**
     * 删除分类（绑定试题禁止删除）
     */
    Result<Void> delete(Long id);
}
