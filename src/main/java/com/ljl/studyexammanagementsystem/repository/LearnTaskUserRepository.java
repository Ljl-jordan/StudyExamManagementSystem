package com.ljl.studyexammanagementsystem.repository;

import com.ljl.studyexammanagementsystem.entity.LearnTaskUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearnTaskUserRepository extends JpaRepository<LearnTaskUser, Long> {

    List<LearnTaskUser> findByTaskIdAndIsDelete(Long taskId, Byte isDelete);

    long countByUserIdAndIsDelete(Long userId, Byte isDelete);
}
