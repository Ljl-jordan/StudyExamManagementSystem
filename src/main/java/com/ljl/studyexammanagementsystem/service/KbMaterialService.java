package com.ljl.studyexammanagementsystem.service;

import com.ljl.studyexammanagementsystem.entity.KbMaterial;
import com.ljl.studyexammanagementsystem.vo.Result;
import org.springframework.data.domain.Page;

import java.util.List;

public interface KbMaterialService {

    Result<Page<KbMaterial>> page(Integer pageNum, Integer pageSize, String keyword, Long categoryId, Long userId, Long orgId);

    Result<KbMaterial> detail(Long id, Long userId, Long orgId);

    Result<Void> addDraft(KbMaterial material, List<Long> fileIds, Long userId);

    Result<Void> update(Long id, KbMaterial material, List<Long> fileIds, Long userId);

    Result<Void> publish(Long id, Long userId);

    Result<Void> delete(Long id);

    Result<Void> batchMoveCategory(List<Long> materialIds, Long targetCategoryId, Long userId);

    Result<Page<KbMaterial>> publishedSelect(Integer pageNum, Integer pageSize, String keyword);
}
