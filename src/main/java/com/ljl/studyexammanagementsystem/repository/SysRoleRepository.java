package com.ljl.studyexammanagementsystem.repository;

import com.ljl.studyexammanagementsystem.entity.SysRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysRoleRepository extends JpaRepository<SysRole, Long> {

    @Query("SELECT r FROM SysRole r WHERE r.isDelete = 0")
    Page<SysRole> findAllActive(Pageable pageable);

    @Query("SELECT r FROM SysRole r WHERE r.isDelete = 0 AND r.roleType = 0")
    List<SysRole> findBuiltInRoles();

    @Query("SELECT r.dataScope FROM SysRole r INNER JOIN SysUserRole ur ON r.id = ur.roleId WHERE ur.userId = :userId AND r.isDelete = 0")
    List<Byte> findDataScopesByUserId(@Param("userId") Long userId);
}
