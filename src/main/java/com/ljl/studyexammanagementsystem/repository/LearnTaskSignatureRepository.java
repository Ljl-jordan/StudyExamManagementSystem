package com.ljl.studyexammanagementsystem.repository;

import com.ljl.studyexammanagementsystem.entity.LearnTaskSignature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LearnTaskSignatureRepository extends JpaRepository<LearnTaskSignature, Long> {

    LearnTaskSignature findByTaskIdAndUserIdAndIsDelete(Long taskId, Long userId, Byte isDelete);
}
