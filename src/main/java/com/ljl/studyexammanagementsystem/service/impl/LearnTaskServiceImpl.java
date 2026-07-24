package com.ljl.studyexammanagementsystem.service.impl;

import com.ljl.studyexammanagementsystem.entity.LearnTask;
import com.ljl.studyexammanagementsystem.entity.LearnTaskMaterial;
import com.ljl.studyexammanagementsystem.entity.LearnTaskUser;
import com.ljl.studyexammanagementsystem.entity.SysUser;
import com.ljl.studyexammanagementsystem.repository.LearnTaskMaterialRepository;
import com.ljl.studyexammanagementsystem.repository.LearnTaskRepository;
import com.ljl.studyexammanagementsystem.repository.LearnTaskUserRepository;
import com.ljl.studyexammanagementsystem.repository.SysUserRepository;
import com.ljl.studyexammanagementsystem.service.LearnTaskService;
import com.ljl.studyexammanagementsystem.utils.DataPermissionUtil;
import com.ljl.studyexammanagementsystem.utils.MessageUtil;
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
public class LearnTaskServiceImpl implements LearnTaskService {

    @Autowired
    private LearnTaskRepository learnTaskRepository;

    @Autowired
    private LearnTaskMaterialRepository learnTaskMaterialRepository;

    @Autowired
    private LearnTaskUserRepository learnTaskUserRepository;

    @Autowired
    private SysUserRepository sysUserRepository;

    @Autowired
    private DataPermissionUtil dataPermissionUtil;

    @Autowired
    private MessageUtil messageUtil;

    /**
     * 任务分页列表（支持按状态筛选 + 数据权限）
     */
    @Override
    public Result<Page<LearnTask>> page(Integer pageNum, Integer pageSize, String keyword, Long taskStatus, Long userId, Long orgId) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Byte dataScope = dataPermissionUtil.getDataScope(userId);

        Specification<LearnTask> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("isDelete"), (byte) 0));

            // 如果指定了特定状态，按指定状态查询
            // 如果未指定状态，则包含所有状态（包括已归档）
            if (taskStatus != null) {
                predicates.add(cb.equal(root.get("taskStatus"), taskStatus.byteValue()));
            }

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
     * 任务详情
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
    public Result<Long> addDraft(LearnTask task, List<Long> materialIds, List<Long> userIds, Long createUserId) {
        if (task.getTaskName() == null || task.getTaskName().trim().isEmpty()) {
            return Result.paramError("任务名称不能为空");
        }
        if (task.getStartTime() != null && task.getEndTime() != null) {
            if (task.getEndTime().before(task.getStartTime())) {
                return Result.paramError("结束时间不能早于开始时间");
            }
        }
        if (materialIds != null && hasDuplicate(materialIds)) {
            return Result.paramError("绑定素材存在重复");
        }

        task.setTaskStatus((byte) 0);
        task.setCreateUser(createUserId);
        task.setCreateTime(new Date());
        task.setIsDelete((byte) 0);
        learnTaskRepository.save(task);

        if (materialIds != null && !materialIds.isEmpty()) {
            saveTaskMaterials(task.getId(), materialIds);
        }
        if (userIds != null && !userIds.isEmpty()) {
            saveTaskUsers(task.getId(), userIds);
        }
        return Result.success("新增成功", task.getId());
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
        if (existing.getTaskStatus() != 0) {
            return Result.businessBlock("已下发任务无法编辑");
        }
        if (task.getStartTime() != null && task.getEndTime() != null) {
            if (task.getEndTime().before(task.getStartTime())) {
                return Result.paramError("结束时间不能早于开始时间");
            }
        }
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

        if (materialIds != null) {
            List<LearnTaskMaterial> oldMaterials = learnTaskMaterialRepository.findByTaskIdAndIsDelete(id, (byte) 0);
            for (LearnTaskMaterial ltm : oldMaterials) {
                ltm.setIsDelete((byte) 1);
            }
            learnTaskMaterialRepository.saveAll(oldMaterials);
            saveTaskMaterials(id, materialIds);
        }
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
        if (existing.getTaskStatus() != 0) {
            return Result.businessBlock("已下发任务无法删除");
        }
        existing.setIsDelete((byte) 1);
        existing.setUpdateTime(new Date());
        learnTaskRepository.save(existing);

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
     * 任务下发（事务控制 + 自动推送消息）
     * 状态流转：草稿(0) → 已下发(1)
     * 下发后锁定编辑和删除
     */
    @Override
    @Transactional
    public Result<Void> publish(Long id, Long userId) {
        LearnTask existing = learnTaskRepository.findById(id).orElse(null);
        if (existing == null || existing.getIsDelete() == 1) {
            return Result.paramError("任务不存在");
        }
        if (existing.getTaskStatus() != 0) {
            return Result.businessBlock("仅草稿状态可下发");
        }
        // 校验必须有分配人员
        List<LearnTaskUser> taskUsers = learnTaskUserRepository.findByTaskIdAndIsDelete(id, (byte) 0);
        if (taskUsers.isEmpty()) {
            return Result.paramError("请先分配学员后再下发");
        }

        // 状态变更为已下发
        existing.setTaskStatus((byte) 1);
        existing.setUpdateUser(userId);
        existing.setUpdateTime(new Date());
        learnTaskRepository.save(existing);

        // 事务内批量生成消息（任意异常则整体回滚）
        List<Long> receiveUserIds = taskUsers.stream()
                .map(LearnTaskUser::getUserId)
                .collect(Collectors.toList());
        messageUtil.pushBatch(receiveUserIds, "learnTask", id,
                "学习任务下发通知", "您有新的学习任务：" + existing.getTaskName() + "，请及时完成。");

        return Result.success("下发成功", null);
    }

    /**
     * 批量按组织分配学员
     * 查询组织下全部学员，去重后新增分配关系
     */
    @Override
    @Transactional
    public Result<Void> assignByOrgs(Long taskId, List<Long> orgIds) {
        LearnTask existing = learnTaskRepository.findById(taskId).orElse(null);
        if (existing == null || existing.getIsDelete() == 1) {
            return Result.paramError("任务不存在");
        }
        if (existing.getTaskStatus() != 0 && existing.getTaskStatus() != 1) {
            return Result.businessBlock("已归档任务无法分配");
        }
        if (orgIds == null || orgIds.isEmpty()) {
            return Result.paramError("组织ID列表不能为空");
        }

        // 查询指定组织下所有学员
        List<SysUser> allUsers = new ArrayList<>();
        for (Long orgId : orgIds) {
            allUsers.addAll(sysUserRepository.findByOrgIdAndIsDelete(orgId, (byte) 0));
        }
        if (allUsers.isEmpty()) {
            return Result.paramError("所选组织下无可用学员");
        }

        // 查询已分配学员，去重
        List<LearnTaskUser> alreadyAssigned = learnTaskUserRepository.findByTaskIdAndIsDelete(taskId, (byte) 0);
        Set<Long> assignedUserIds = alreadyAssigned.stream()
                .map(LearnTaskUser::getUserId)
                .collect(Collectors.toSet());

        List<Long> newUserIds = allUsers.stream()
                .map(u -> u.getId())
                .filter(uid -> !assignedUserIds.contains(uid))
                .collect(Collectors.toList());

        if (!newUserIds.isEmpty()) {
            saveTaskUsers(taskId, newUserIds);
        }
        return Result.success("分配成功，共分配" + newUserIds.size() + "名学员", null);
    }

    /**
     * 单独分配学员
     * 去重后新增分配关系
     */
    @Override
    @Transactional
    public Result<Void> assignUsers(Long taskId, List<Long> userIds) {
        LearnTask existing = learnTaskRepository.findById(taskId).orElse(null);
        if (existing == null || existing.getIsDelete() == 1) {
            return Result.paramError("任务不存在");
        }
        if (existing.getTaskStatus() != 0 && existing.getTaskStatus() != 1) {
            return Result.businessBlock("已归档任务无法分配");
        }
        if (userIds == null || userIds.isEmpty()) {
            return Result.paramError("用户ID列表不能为空");
        }

        List<LearnTaskUser> alreadyAssigned = learnTaskUserRepository.findByTaskIdAndUserIdInAndIsDelete(taskId, userIds, (byte) 0);
        Set<Long> assignedUserIds = alreadyAssigned.stream()
                .map(LearnTaskUser::getUserId)
                .collect(Collectors.toSet());

        List<Long> newUserIds = userIds.stream()
                .filter(uid -> !assignedUserIds.contains(uid))
                .collect(Collectors.toList());

        if (!newUserIds.isEmpty()) {
            saveTaskUsers(taskId, newUserIds);
        }
        return Result.success("分配成功，共分配" + newUserIds.size() + "名学员", null);
    }

    /**
     * 任务归档（已下发 → 已结束）
     */
    @Override
    @Transactional
    public Result<Void> archive(Long id, Long userId) {
        LearnTask existing = learnTaskRepository.findById(id).orElse(null);
        if (existing == null || existing.getIsDelete() == 1) {
            return Result.paramError("任务不存在");
        }
        if (existing.getTaskStatus() != 1) {
            return Result.businessBlock("仅已下发状态可归档");
        }

        // 检查是否有学员正在学习此任务（即学习状态为"学习中"）
        List<LearnTaskUser> learningUsers = learnTaskUserRepository.findByTaskIdAndLearnStatusAndIsDelete(
                id, (byte) 1, (byte) 0); // 1表示"学习中"
        if (!learningUsers.isEmpty()) {
            return Result.businessBlock("该任务有学员正在学习中，无法归档，请等待学员完成后再操作");
        }

        existing.setTaskStatus((byte) 2);
        existing.setUpdateUser(userId);
        existing.setUpdateTime(new Date());
        learnTaskRepository.save(existing);
        return Result.success("归档成功", null);
    }

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

    private boolean hasDuplicate(List<Long> list) {
        Set<Long> set = new HashSet<>(list);
        return set.size() != list.size();
    }
}
