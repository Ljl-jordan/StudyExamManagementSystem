package com.ljl.studyexammanagementsystem.service.impl;

import com.ljl.studyexammanagementsystem.cache.CacheKeys;
import com.ljl.studyexammanagementsystem.cache.CacheService;
import com.ljl.studyexammanagementsystem.entity.KbMaterial;
import com.ljl.studyexammanagementsystem.entity.KbMaterialFile;
import com.ljl.studyexammanagementsystem.repository.KbMaterialFileRepository;
import com.ljl.studyexammanagementsystem.repository.KbMaterialRepository;
import com.ljl.studyexammanagementsystem.repository.LearnTaskMaterialRepository;
import com.ljl.studyexammanagementsystem.service.KbMaterialService;
import com.ljl.studyexammanagementsystem.utils.DataPermissionUtil;
import com.ljl.studyexammanagementsystem.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.util.*;

@Service
public class KbMaterialServiceImpl implements KbMaterialService {

    @Autowired
    private KbMaterialRepository kbMaterialRepository;

    @Autowired
    private KbMaterialFileRepository kbMaterialFileRepository;

    @Autowired
    private LearnTaskMaterialRepository learnTaskMaterialRepository;

    @Autowired
    private DataPermissionUtil dataPermissionUtil;

    @Autowired
    private CacheService cacheService;

    /**
     * 素材分页列表（带数据权限）
     */
    @Override
    public Result<Page<KbMaterial>> page(Integer pageNum, Integer pageSize, String keyword, Long categoryId, Long userId, Long orgId) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Byte dataScope = dataPermissionUtil.getDataScope(userId);
//Specification动态分页条件：isDelete=0  keyword 素材名称模糊查询 categoryId 分类id dataScope=2仅查看自己创建的素材
        Specification<KbMaterial> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("isDelete"), (byte) 0));
            if (keyword != null && !keyword.trim().isEmpty()) {
                predicates.add(cb.like(root.get("materialName"), "%" + keyword.trim() + "%"));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("categoryId"), categoryId));
            }
            if (dataScope == 2) {
                predicates.add(cb.equal(root.get("createUser"), userId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<KbMaterial> page = kbMaterialRepository.findAll(spec, pageable);
        return Result.success(page);
    }

    /**
     * 素材详情
     */
    @Override
    public Result<KbMaterial> detail(Long id, Long userId, Long orgId) {
        KbMaterial material = cacheService.getOrLoad(
                CacheKeys.materialDetail(id),
                KbMaterial.class,
                30 * 60L,
                () -> {
                    KbMaterial cached = kbMaterialRepository.findById(id).orElse(null);
                    return cached == null || cached.getIsDelete() == 1 ? null : cached;
                });
        if (material == null) {
            return Result.paramError("素材不存在");
        }
        return Result.success(material);
    }

    /**
     * 新增草稿素材（富文本/附件/外链互斥校验）
     */
    @Override
    @Transactional
    public Result<Void> addDraft(KbMaterial material, List<Long> fileIds, Long userId) {
        // 参数校验
        if (material.getMaterialName() == null || material.getMaterialName().trim().isEmpty()) {
            return Result.paramError("素材名称不能为空");
        }
        if (material.getMaterialType() == null) {
            return Result.paramError("素材内容类型不能为空");
        }
        if (material.getCategoryId() == null) {
            return Result.paramError("所属分类不能为空");
        }
        // 互斥校验：根据类型清空冗余字段
        Result<Void> mutexCheck = checkMaterialMutex(material);
        if (mutexCheck != null) return mutexCheck;

        material.setMaterialStatus((byte) 0); // 默认草稿
        material.setCreateUser(userId);
        material.setCreateTime(new Date());
        material.setIsDelete((byte) 0);
        kbMaterialRepository.save(material);

        // 保存附件关联
        if (fileIds != null && !fileIds.isEmpty() && material.getMaterialType() == 2) {
            saveMaterialFiles(material.getId(), fileIds);
        }
        return Result.success("新增成功", null);
    }

    /**
     * 编辑素材（已发布不可修改分类和类型）
     */
    @Override
    @Transactional
    public Result<Void> update(Long id, KbMaterial material, List<Long> fileIds, Long userId) {
        KbMaterial existing = kbMaterialRepository.findById(id).orElse(null);
        if (existing == null || existing.getIsDelete() == 1) {
            return Result.paramError("素材不存在");
        }
        // 已发布素材不可修改分类、素材类型
        if (existing.getMaterialStatus() == 1) {
            if (material.getCategoryId() != null && !material.getCategoryId().equals(existing.getCategoryId())) {
                return Result.paramError("已发布素材不可修改分类");
            }
            if (material.getMaterialType() != null && !material.getMaterialType().equals(existing.getMaterialType())) {
                return Result.paramError("已发布素材不可修改素材类型");
            }
        }
        // 更新字段
        if (material.getMaterialName() != null && !material.getMaterialName().trim().isEmpty()) {
            existing.setMaterialName(material.getMaterialName().trim());
        }
        if (material.getCategoryId() != null) {
            existing.setCategoryId(material.getCategoryId());
        }
        if (material.getMaterialType() != null) {
            existing.setMaterialType(material.getMaterialType());
        }
        // 互斥校验
        Result<Void> mutexCheck = checkMaterialMutex(material);
        if (mutexCheck != null) return mutexCheck;

        if (material.getRichContent() != null) {
            existing.setRichContent(material.getRichContent());
        }
        if (material.getLinkUrl() != null) {
            existing.setLinkUrl(material.getLinkUrl());
        }
        if (material.getCoverFileId() != null) {
            existing.setCoverFileId(material.getCoverFileId());
        }
        existing.setUpdateUser(userId);
        existing.setUpdateTime(new Date());
        kbMaterialRepository.save(existing);
        cacheService.evict(CacheKeys.materialDetail(id));

        // 更新附件关联（先删后增）
        if (fileIds != null && material.getMaterialType() != null && material.getMaterialType() == 2) {
            List<KbMaterialFile> oldFiles = kbMaterialFileRepository.findByMaterialIdAndIsDelete(id, (byte) 0);
            for (KbMaterialFile mf : oldFiles) {
                mf.setIsDelete((byte) 1);
            }
            kbMaterialFileRepository.saveAll(oldFiles);
            saveMaterialFiles(id, fileIds);
        }
        return Result.success("修改成功", null);
    }

    /**
     * 发布素材
     */
    @Override
    @Transactional
    public Result<Void> publish(Long id, Long userId) {
        KbMaterial existing = kbMaterialRepository.findById(id).orElse(null);
        if (existing == null || existing.getIsDelete() == 1) {
            return Result.paramError("素材不存在");
        }
        if (existing.getMaterialStatus() == 1) {
            return Result.paramError("素材已发布，不可重复发布");
        }
        existing.setMaterialStatus((byte) 1);
        existing.setUpdateUser(userId);
        existing.setUpdateTime(new Date());
        kbMaterialRepository.save(existing);
        cacheService.evict(CacheKeys.materialDetail(id));
        return Result.success("发布成功", null);
    }

    /**
     * 删除素材（双重拦截：绑定任务、存在学时记录）
     */
    @Override
    @Transactional
    public Result<Void> delete(Long id) {
        KbMaterial existing = kbMaterialRepository.findById(id).orElse(null);
        if (existing == null || existing.getIsDelete() == 1) {
            return Result.paramError("素材不存在");
        }
        // 拦截1：绑定任务
        long taskBindCount = learnTaskMaterialRepository.countByMaterialIdAndIsDelete(id, (byte) 0);
        if (taskBindCount > 0) {
            return Result.businessBlock("该素材已绑定学习任务，无法删除");
        }
        // 逻辑删除
        existing.setIsDelete((byte) 1);
        existing.setUpdateTime(new Date());
        kbMaterialRepository.save(existing);
        cacheService.evict(CacheKeys.materialDetail(id));
        // 同步删除附件关联
        List<KbMaterialFile> files = kbMaterialFileRepository.findByMaterialIdAndIsDelete(id, (byte) 0);
        for (KbMaterialFile mf : files) {
            mf.setIsDelete((byte) 1);
        }
        kbMaterialFileRepository.saveAll(files);
        return Result.success("删除成功", null);
    }

    /**
     * 批量迁移分类
     */
    @Override
    @Transactional
    public Result<Void> batchMoveCategory(List<Long> materialIds, Long targetCategoryId, Long userId) {
        if (materialIds == null || materialIds.isEmpty()) {
            return Result.paramError("素材ID列表不能为空");
        }
        if (targetCategoryId == null) {
            return Result.paramError("目标分类ID不能为空");
        }
        for (Long materialId : materialIds) {
            KbMaterial material = kbMaterialRepository.findById(materialId).orElse(null);
            if (material != null && material.getIsDelete() == 0) {
                // 已发布素材不可修改分类
                if (material.getMaterialStatus() == 1) {
                    continue;
                }
                material.setCategoryId(targetCategoryId);
                material.setUpdateUser(userId);
                material.setUpdateTime(new Date());
                kbMaterialRepository.save(material);
                cacheService.evict(CacheKeys.materialDetail(materialId));
            }
        }
        return Result.success("批量迁移成功", null);
    }

    /**
     * 素材下拉筛选：仅展示已发布素材并分页
     */
    @Override
    public Result<Page<KbMaterial>> publishedSelect(Integer pageNum, Integer pageSize, String keyword) {
        //仅查status=1 + 分页
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<KbMaterial> page;
        if (keyword != null && !keyword.trim().isEmpty()) {
            page = kbMaterialRepository.findPublishedByKeyword(keyword.trim(), pageable);
        } else {
            page = kbMaterialRepository.findPublishedPage(pageable);
        }
        return Result.success(page);
    }

    /**
     * 素材内容类型互斥校验
     */
    private Result<Void> checkMaterialMutex(KbMaterial material) {
        if (material.getMaterialType() == null) return null;
        switch (material.getMaterialType()) {
            case 1: // 富文本
                material.setLinkUrl(null);
                break;
            case 2: // 附件
                material.setRichContent(null);
                material.setLinkUrl(null);
                break;
            case 3: // 外链
                material.setRichContent(null);
                if (material.getLinkUrl() == null || material.getLinkUrl().trim().isEmpty()) {
                    return Result.paramError("外链地址不能为空");
                }
                break;
            default:
                return Result.paramError("素材内容类型不合法");
        }
        return null;
    }

    /**
     * 保存素材附件关联
     */
    private void saveMaterialFiles(Long materialId, List<Long> fileIds) {
        for (Long fileId : fileIds) {
            KbMaterialFile mf = new KbMaterialFile();
            mf.setMaterialId(materialId);
            mf.setFileId(fileId);
            mf.setCreateTime(new Date());
            mf.setIsDelete((byte) 0);
            kbMaterialFileRepository.save(mf);
        }
    }
}
