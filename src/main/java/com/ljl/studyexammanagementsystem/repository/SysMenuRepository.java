package com.ljl.studyexammanagementsystem.repository;

import com.ljl.studyexammanagementsystem.entity.SysMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysMenuRepository extends JpaRepository<SysMenu, Long> {

    @Query("SELECT m FROM SysMenu m WHERE m.isDelete = 0 ORDER BY m.sort ASC")
    List<SysMenu> findAllActive();

    List<SysMenu> findByParentIdAndIsDelete(Long parentId, Byte isDelete);

    @Query("SELECT m FROM SysMenu m INNER JOIN SysRoleMenu rm ON m.id = rm.menuId WHERE rm.roleId IN :roleIds AND m.isDelete = 0 AND rm.isDelete = 0 ORDER BY m.sort ASC")
    List<SysMenu> findByRoleIds(@Param("roleIds") List<Long> roleIds);

    @Query("SELECT DISTINCT m.buttonPerms FROM SysMenu m INNER JOIN SysRoleMenu rm ON m.id = rm.menuId WHERE rm.roleId IN :roleIds AND m.isDelete = 0 AND rm.isDelete = 0 AND m.buttonPerms IS NOT NULL AND m.buttonPerms <> ''")
    List<String> findButtonPermsByRoleIds(@Param("roleIds") List<Long> roleIds);
}
