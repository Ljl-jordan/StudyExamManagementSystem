package com.ljl.studyexammanagementsystem.repository;

import com.ljl.studyexammanagementsystem.entity.SysRoleMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface SysRoleMenuRepository extends JpaRepository<SysRoleMenu, Long> {

    List<SysRoleMenu> findByRoleId(Long roleId);

    @Modifying
    @Transactional
    @Query("DELETE FROM SysRoleMenu rm WHERE rm.roleId = :roleId")
    void deleteByRoleId(@Param("roleId") Long roleId);
}
