package com.ljl.studyexammanagementsystem.service.impl;

import com.ljl.studyexammanagementsystem.entity.LearnTask;
import com.ljl.studyexammanagementsystem.entity.LearnTaskMaterial;
import com.ljl.studyexammanagementsystem.entity.LearnTaskUser;
import com.ljl.studyexammanagementsystem.repository.LearnTaskMaterialRepository;
import com.ljl.studyexammanagementsystem.repository.LearnTaskRepository;
import com.ljl.studyexammanagementsystem.repository.LearnTaskUserRepository;
import com.ljl.studyexammanagementsystem.service.LearnTaskService;
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
public class LearnTaskServiceImpl implements LearnTaskService {

    @Autowired
    private LearnTaskRepository learnTaskRepository;

    @Autowired
    private LearnTaskMaterialRepository learnTaskMaterialRepository;

    @Autowired
    private LearnTaskUserRepository learnTaskUserRepository;

    @Autowired
    private DataPermissionUtil dataPermissionUtil;

    /**
     * 任务分页列表（带数据权限）
     */
    @Override
    public Result<Page<LearnTask>> page(Integer pageNum, Integer pageSize, String keyword, Long userId, Long orgId) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Byte dataScope = dataPermissionUtil.getDataScope(userId);

        Specification<LearnTask> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("isDelete"), (byte) 0));
            if (keyword != null && !keyword.trim().isEmpty()) {
                predicates.add(cb.like(root.get("taskName"), "%" + keyword.trim() + "%"));
            }
            if (dataScope == 2) {
                predicates.add(cb.equal(root.get("createUser"), userId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<LearnTask> page = learnTaskRepository.findAll(spec, pageable);
        return Result.success(page);
    }

    /**
     * 任务详情（回显已绑定素材、分配人员）
     */
    @Override
    public Result<LearnTask> detail(Long id, Long userId, Long orgId) {
        LearnTask task = learnTaskRepository.findById(id).orElse(null);
        if (task == null || task.getIsDelete() == 1) {
            return Result.paramError("任务不存在");
        }
        return Result.success(task);
    }

    /**
     * 新建草稿任务
     */
    @Override
    @Transactional
    public Result<Void> addDraft(LearnTask task, List<Long> materialIds, List<Long> userIds, Long createUserId) {
        // 参数校验
        if (task.getTaskName() == null || task.getTaskName().trim().isEmpty()) {
            return Result.paramError("任务名称不能为空");
        }
        // 起止时间校验
        if (task.getStartTime() != null && task.getEndTime() != null) {
            if (task.getEndTime().before(task.getStartTime())) {
                return Result.paramError("结束时间不能早于开始时间");
            }
        }
        // 素材绑定去重校验
        if (materialIds != null && hasDuplicate(materialIds)) {
            return Result.paramError("绑定素材存在重复");
        }

        task.setTaskStatus((byte) 0); // 草稿
        task.setCreateUser(createUserId);
        task.setCreateTime(new Date());
        task.setIsDelete((byte) 0);
        learnTaskRepository.save(task);

        // 绑定素材
        if (materialIds != null && !materialIds.isEmpty()) {
            saveTaskMaterials(task.getId(), materialIds);
        }
        // 绑定分配人员（草稿状态不生成消息）
        if (userIds != null && !userIds.isEmpty()) {
            saveTaskUsers(task.getId(), userIds);
        }
        return Result.success("新增成功", null);
    }

    /**
     * 编辑任务（仅草稿可编辑）
     */
    @Override
    @Transactional
    public Result<Void> update(Long id, LearnTask task, List<Long> materialIds, List<Long> userIds, Long updateUserId) {
        LearnTask existing = learnTaskRepository.findById(id).orElse(null);
        if (existing == null || existing.getIsDelete() == 1) {
            return Result.paramError("任务不存在");
        }
        // 仅草稿支持编辑
        if (existing.getTaskStatus() != 0) {
            return Result.businessBlock("已下发任务无法编辑");
        }
        // 起止时间校验
        if (task.getStartTime() != null && task.getEndTime() != null) {
            if (task.getEndTime().before(task.getStartTime())) {
                return Result.paramError("结束时间不能早于开始时间");
            }
        }
        // 素材去重校验
        if (materialIds != null && hasDuplicate(materialIds)) {
            return Result.paramError("绑定素材存在重复");
        }

        if (task.getTaskName() != null && !task.getTaskName().trim().isEmpty()) {
            existing.setTaskName(task.getTaskName().trim());
        }
        if (task.getTaskDesc() != null) {
            existing.setTaskDesc(task.getTaskDesc());
        }
        if (task.getStartTime() != null) {
            existing.setStartTime(task.getStartTime());
        }
        if (task.getEndTime() != null) {
            existing.setEndTime(task.getEndTime());
        }
        existing.setUpdateUser(updateUserId);
        existing.setUpdateTime(new Date());
        learnTaskRepository.save(existing);

        // 更新素材绑定（先删后增）
        if (materialIds != null) {
            List<LearnTaskMaterial> oldMaterials = learnTaskMaterialRepository.findByTaskIdAndIsDelete(id, (byte) 0);
            for (LearnTaskMaterial ltm : oldMaterials) {
                ltm.setIsDelete((byte) 1);
            }
            learnTaskMaterialRepository.saveAll(oldMaterials);
            saveTaskMaterials(id, materialIds);
        }
        // 更新分配人员
        if (userIds != null) {
            List<LearnTaskUser> oldUsers = learnTaskUserRepository.findByTaskIdAndIsDelete(id, (byte) 0);
            for (LearnTaskUser ltu : oldUsers) {
                ltu.setIsDelete((byte) 1);
            }
            learnTaskUserRepository.saveAll(oldUsers);
            saveTaskUsers(id, userIds);
        }
        return Result.success("修改成功", null);
    }

    /**
     * 删除任务（仅草稿可删除）
     */
    @Override
    @Transactional
    public Result<Void> delete(Long id) {
        LearnTask existing = learnTaskRepository.findById(id).orElse(null);
        if (existing == null || existing.getIsDelete() == 1) {
            return Result.paramError("任务不存在");
        }
        // 仅草稿支持删除
        if (existing.getTaskStatus() != 0) {
            return Result.businessBlock("已下发任务无法删除");
        }
        existing.setIsDelete((byte) 1);
        existing.setUpdateTime(new Date());
        learnTaskRepository.save(existing);

        // 同步删除关联素材和人员
        List<LearnTaskMaterial> materials = learnTaskMaterialRepository.findByTaskIdAndIsDelete(id, (byte) 0);
        for (LearnTaskMaterial ltm : materials) {
            ltm.setIsDelete((byte) 1);
        }
        learnTaskMaterialRepository.saveAll(materials);

        List<LearnTaskUser> users = learnTaskUserRepository.findByTaskIdAndIsDelete(id, (byte) 0);
        for (LearnTaskUser ltu : users) {
            ltu.setIsDelete((byte) 1);
        }
        learnTaskUserRepository.saveAll(users);

        return Result.success("删除成功", null);
    }

    /**
     * 保存任务素材关联（去重）
     */
    private void saveTaskMaterials(Long taskId, List<Long> materialIds) {
        Set<Long> added = new HashSet<>();
        int sort = 0;
        for (Long materialId : materialIds) {
            if (added.contains(materialId)) continue;
            added.add(materialId);
            LearnTaskMaterial ltm = new LearnTaskMaterial();
            ltm.setTaskId(taskId);
            ltm.setMaterialId(materialId);
            ltm.setSortOrder(sort++);
            ltm.setCreateTime(new Date());
            ltm.setIsDelete((byte) 0);
            learnTaskMaterialRepository.save(ltm);
        }
    }

    /**
     * 保存任务用户关联
     */
    private void saveTaskUsers(Long taskId, List<Long> userIds) {
        for (Long userId : userIds) {
            LearnTaskUser ltu = new LearnTaskUser();
            ltu.setTaskId(taskId);
            ltu.setUserId(userId);
            ltu.setLearnStatus((byte) 0);
            ltu.setCreateTime(new Date());
            ltu.setIsDelete((byte) 0);
            learnTaskUserRepository.save(ltu);
        }
    }

    /**
     * 检查列表中是否有重复元素
     */
    private boolean hasDuplicate(List<Long> list) {
        Set<Long> set = new HashSet<>(list);
        return set.size() != list.size();
    }
}
