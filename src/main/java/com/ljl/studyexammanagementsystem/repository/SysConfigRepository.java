package com.ljl.studyexammanagementsystem.repository;

import com.ljl.studyexammanagementsystem.entity.SysConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SysConfigRepository extends JpaRepository<SysConfig, Long> {

    @Query("SELECT c FROM SysConfig c WHERE c.isDelete = 0 ORDER BY c.id")
    List<SysConfig> findAllActive();

    Optional<SysConfig> findByConfigKeyAndIsDelete(String configKey, Byte isDelete);
}
