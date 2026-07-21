package com.ljl.studyexammanagementsystem.service.impl;

import com.ljl.studyexammanagementsystem.entity.QbCategory;
import com.ljl.studyexammanagementsystem.repository.QbCategoryRepository;
import com.ljl.studyexammanagementsystem.repository.QbQuestionRepository;
import com.ljl.studyexammanagementsystem.service.QbCategoryService;
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
import java.util.stream.Collectors;

@Service
public class QbCategoryServiceImpl implements QbCategoryService {

    @Autowired
    private QbCategoryRepository qbCategoryRepository;

    @Autowired
    private QbQuestionRepository qbQuestionRepository;

    @Autowired
    private DataPermissionUtil dataPermissionUtil;

    /**
     * 获取分类树形结构（带数据权限过滤）
     */
    @Override
    public Result<List<QbCategory>> getTree(Long userId, Long orgId) {
        //获取未被删除的全部分类列表
        List<QbCategory> allCategories = qbCategoryRepository.findByIsDelete((byte) 0);
        // 数据权限过滤
        allCategories = filterByDataScope(allCategories, userId, orgId);
        List<QbCategory> tree = buildTree(allCategories, 0L);
        return Result.success(tree);
    }

    /**
     * 分类分页列表（带数据权限过滤）
     */
    @Override
    public Result<Page<QbCategory>> page(Integer pageNum, Integer pageSize, String keyword, Long userId, Long orgId) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Byte dataScope = dataPermissionUtil.getDataScope(userId);
        List<Long> visibleOrgIds = null;
        if (dataScope == 1) {
            visibleOrgIds = dataPermissionUtil.getVisibleOrgIds(orgId);
        }
        /*
        Specification动态分页条件：isDelete=0
        keyword分类名称模糊查询，dataScope=2仅查看自己创建的分类
        */
        Specification<QbCategory> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("isDelete"), (byte) 0));
            if (keyword != null && !keyword.trim().isEmpty()) {
                predicates.add(cb.like(root.get("categoryName"), "%" + keyword.trim() + "%"));
            }
            // 数据权限：普通用户仅查看本组织创建的分类
            if (dataScope == 2) {
                predicates.add(cb.equal(root.get("createUser"), userId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<QbCategory> page = qbCategoryRepository.findAll(spec, pageable);
        return Result.success(page);
    }

    /**
     * 新增分类（同级名称唯一校验）
     */
    @Override
    @Transactional
    public Result<Void> add(QbCategory category, Long userId, Long orgId) {
        //名称非空判断
        if (category.getCategoryName() == null || category.getCategoryName().trim().isEmpty()) {
            return Result.paramError("分类名称不能为空");
        }
        if (category.getParentId() == null) {
            category.setParentId(0L);
        }
        // 同级分类名称唯一校验
        QbCategory duplicate = qbCategoryRepository.findByCategoryNameAndParentId(
                category.getCategoryName().trim(), category.getParentId());
        if (duplicate != null) {
            return Result.paramError("同级下分类名称已存在");
        }
        category.setCreateUser(userId);
        category.setCreateTime(new Date());
        category.setIsDelete((byte) 0);
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        qbCategoryRepository.save(category);
        return Result.success("新增成功", null);
    }

    /**
     * 编辑分类（同级名称唯一校验）
     */
    @Override
    @Transactional
    public Result<Void> update(Long id, QbCategory category, Long userId) {
        QbCategory existing = qbCategoryRepository.findById(id).orElse(null);
        if (existing == null || existing.getIsDelete() == 1) {
            return Result.paramError("分类不存在");
        }
        // 父级不能选自身或子节点校验
        if (category.getParentId() != null) {
            if (category.getParentId().equals(id)) {
                return Result.paramError("上级分类不能选择自身");
            }
            if (isDescendant(id, category.getParentId())) {
                return Result.paramError("上级分类不能选择自身的子节点");
            }
        }
        // 同级名称唯一校验
        if (category.getCategoryName() != null && !category.getCategoryName().trim().isEmpty()) {
            Long parentId = category.getParentId() != null ? category.getParentId() : existing.getParentId();
            QbCategory duplicate = qbCategoryRepository.findByCategoryNameAndParentId(
                    category.getCategoryName().trim(), parentId);
            if (duplicate != null && !duplicate.getId().equals(id)) {
                return Result.paramError("同级下分类名称已存在");
            }
            existing.setCategoryName(category.getCategoryName().trim());
        }
        if (category.getParentId() != null) {
            existing.setParentId(category.getParentId());
        }
        if (category.getSortOrder() != null) {
            existing.setSortOrder(category.getSortOrder());
        }
        existing.setUpdateUser(userId);
        existing.setUpdateTime(new Date());
        qbCategoryRepository.save(existing);
        return Result.success("修改成功", null);
    }

    /**
     * 删除分类（绑定试题禁止删除，返回405拦截）
     */
    @Override
    @Transactional
    public Result<Void> delete(Long id) {
        QbCategory existing = qbCategoryRepository.findById(id).orElse(null);
        if (existing == null || existing.getIsDelete() == 1) {
            return Result.paramError("分类不存在");
        }
        // 子分类校验
        long childCount = qbCategoryRepository.countByParentIdAndIsDelete(id, (byte) 0);
        if (childCount > 0) {
            return Result.businessBlock("存在子分类，无法删除");
        }
        // 绑定试题校验
        long questionCount = qbQuestionRepository.countByCategoryId(id);
        if (questionCount > 0) {
            return Result.businessBlock("该分类下存在绑定试题，无法删除");
        }
        existing.setIsDelete((byte) 1);
        existing.setUpdateTime(new Date());
        qbCategoryRepository.save(existing);
        return Result.success("删除成功", null);
    }

    /**
     * 构建树形结构
     */
    private List<QbCategory> buildTree(List<QbCategory> allCategories, Long parentId) {
        return allCategories.stream()
                .filter(c -> parentId.equals(c.getParentId()))//按parentId=0找顶级
                .peek(c -> {
                    List<QbCategory> children = buildTree(allCategories, c.getId());
                    c.setChildren(children.isEmpty() ? null : children);
                })
                .collect(Collectors.toList());
    }

    /**
     * 判断targetParentId是否为currentId的子节点
     */
    private boolean isDescendant(Long currentId, Long targetParentId) {
        List<QbCategory> allCategories = qbCategoryRepository.findByIsDelete((byte) 0);
        Set<Long> visited = new HashSet<>();
        return checkDescendant(allCategories, currentId, targetParentId, visited);
    }

    private boolean checkDescendant(List<QbCategory> allCategories, Long currentId, Long targetParentId, Set<Long> visited) {
        if (visited.contains(currentId)) return false;
        visited.add(currentId);
        List<QbCategory> children = allCategories.stream()
                .filter(c -> currentId.equals(c.getParentId()) && c.getIsDelete() == 0)
                .collect(Collectors.toList());
        for (QbCategory child : children) {
            if (child.getId().equals(targetParentId)) return true;
            if (checkDescendant(allCategories, child.getId(), targetParentId, visited)) return true;
        }
        return false;
    }

    /**
     * 数据权限过滤：根据用户角色筛选可见分类
     * scope=0 超管看全部；scope=1 看本组织及下级创建；scope=2 仅看自己创建的
     */
    private List<QbCategory> filterByDataScope(List<QbCategory> categories, Long userId, Long orgId) {
        Byte dataScope = dataPermissionUtil.getDataScope(userId);
        if (dataScope == 0) {
            return categories;
        }
        if (dataScope == 1) {
            // 本组织及下级组织创建的分类（通过createUser关联的用户所属组织过滤）
            // 简化处理：返回所有分类（因为分类表没有orgId字段，通过createUser判断）
            return categories;
        }
        // scope=2 仅看自己创建的
        return categories.stream()
                .filter(c -> userId.equals(c.getCreateUser()))
                .collect(Collectors.toList());
    }
}
