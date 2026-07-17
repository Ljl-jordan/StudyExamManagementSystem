package com.ljl.studyexammanagementsystem.repository;

import com.ljl.studyexammanagementsystem.entity.SysUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SysUserRepository extends JpaRepository<SysUser, Long> {

    Optional<SysUser> findByLoginAccountAndIsDelete(String loginAccount, Byte isDelete);

    List<SysUser> findByIsDelete(Byte isDelete);

    List<SysUser> findByOrgIdAndIsDelete(Long orgId, Byte isDelete);

    long countByOrgIdAndIsDelete(Long orgId, Byte isDelete);

    boolean existsByLoginAccountAndIsDelete(String loginAccount, Byte isDelete);

    @Query("SELECT u FROM SysUser u WHERE u.isDelete = 0 AND u.orgId IN :orgIds")
    Page<SysUser> findByOrgIdIn(@Param("orgIds") List<Long> orgIds, Pageable pageable);

    @Query("SELECT u FROM SysUser u WHERE u.isDelete = 0 AND u.orgId IN :orgIds AND (u.userName LIKE %:keyword% OR u.loginAccount LIKE %:keyword%)")
    Page<SysUser> findByOrgIdInAndKeyword(@Param("orgIds") List<Long> orgIds, @Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT u FROM SysUser u WHERE u.isDelete = 0 AND (u.userName LIKE %:keyword% OR u.loginAccount LIKE %:keyword%)")
    Page<SysUser> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT u FROM SysUser u WHERE u.isDelete = 0")
    Page<SysUser> findAllActive(Pageable pageable);

    @Query("SELECT u FROM SysUser u WHERE u.isDelete = 0 AND u.id = :userId")
    Optional<SysUser> findActiveById(@Param("userId") Long userId);
}
