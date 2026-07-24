package com.ljl.studyexammanagementsystem.repository;

import com.ljl.studyexammanagementsystem.entity.LearnTaskUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearnTaskUserRepository extends JpaRepository<LearnTaskUser, Long> {

    List<LearnTaskUser> findByTaskIdAndIsDelete(Long taskId, Byte isDelete);

    long countByUserIdAndIsDelete(Long userId, Byte isDelete);

    LearnTaskUser findByTaskIdAndUserIdAndIsDelete(Long taskId, Long userId, Byte isDelete);

    List<LearnTaskUser> findByTaskIdAndUserIdInAndIsDelete(Long taskId, List<Long> userIds, Byte isDelete);

    // 添加查找未完成学习任务的用户的方法
    List<LearnTaskUser> findByTaskIdAndLearnStatusNotAndIsDelete(Long taskId, Byte learnStatus, Byte isDelete);

    /** 统计指定任务下已完成人数（learn_status=2） */
    @Query("SELECT COUNT(ltu) FROM LearnTaskUser ltu WHERE ltu.isDelete = 0 AND ltu.taskId = :taskId AND ltu.userId IN :userIds AND ltu.learnStatus = 2")
    long countCompletedByTaskIdAndUserIds(@Param("taskId") Long taskId, @Param("userIds") List<Long> userIds);

    /** 统计指定任务下已签名人数 */
    @Query("SELECT COUNT(ltu) FROM LearnTaskUser ltu WHERE ltu.isDelete = 0 AND ltu.taskId = :taskId AND ltu.userId IN :userIds AND ltu.signatureFileId IS NOT NULL")
    long countSignedByTaskIdAndUserIds(@Param("taskId") Long taskId, @Param("userIds") List<Long> userIds);

    /** 查询指定任务和组织范围下的学员分配记录 */
    @Query("SELECT ltu FROM LearnTaskUser ltu WHERE ltu.isDelete = 0 AND ltu.taskId = :taskId AND ltu.userId IN (SELECT u.id FROM SysUser u WHERE u.isDelete = 0 AND u.orgId IN :orgIds)")
    List<LearnTaskUser> findByTaskIdAndOrgIds(@Param("taskId") Long taskId, @Param("orgIds") List<Long> orgIds);

    /** 查询指定组织范围下全部学员分配记录（不限任务） */
    @Query("SELECT ltu FROM LearnTaskUser ltu WHERE ltu.isDelete = 0 AND ltu.userId IN (SELECT u.id FROM SysUser u WHERE u.isDelete = 0 AND u.orgId IN :orgIds)")
    List<LearnTaskUser> findByOrgIds(@Param("orgIds") List<Long> orgIds);

    /** 查询指定任务下全部学员分配记录 */
    @Query("SELECT ltu FROM LearnTaskUser ltu WHERE ltu.isDelete = 0 AND ltu.taskId = :taskId")
    List<LearnTaskUser> findAllByTaskId(@Param("taskId") Long taskId);

    /** 查询指定任务下指定用户列表的学员分配记录 */
    @Query("SELECT ltu FROM LearnTaskUser ltu WHERE ltu.isDelete = 0 AND ltu.taskId = :taskId AND ltu.userId IN :userIds")
    List<LearnTaskUser> findByTaskIdAndUserIds(@Param("taskId") Long taskId, @Param("userIds") List<Long> userIds);

    List<LearnTaskUser> findByTaskIdAndLearnStatusAndIsDelete(Long taskId, Byte learnStatus, Byte isDelete);

    List<LearnTaskUser> findByUserIdAndIsDelete(Long userId, Byte isDelete);
}
