package com.ljl.studyexammanagementsystem.service;

import com.ljl.studyexammanagementsystem.entity.KbCategory;
import com.ljl.studyexammanagementsystem.vo.Result;
import org.springframework.data.domain.Page;

import java.util.List;

public interface KbCategoryService {

    Result<List<KbCategory>> getTree(Long userId, Long orgId);

    Result<Page<KbCategory>> page(Integer pageNum, Integer pageSize, String keyword, Long userId, Long orgId);

    Result<Void> add(KbCategory category, Long userId, Long orgId);

    Result<Void> update(Long id, KbCategory category, Long userId);

    Result<Void> delete(Long id);
}
