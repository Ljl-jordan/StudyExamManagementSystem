package com.ljl.studyexammanagementsystem.repository;

import com.ljl.studyexammanagementsystem.entity.QbQuestionMultiple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QbQuestionMultipleRepository extends JpaRepository<QbQuestionMultiple, Long> {

    QbQuestionMultiple findByQuestionId(Long questionId);

    void deleteByQuestionId(Long questionId);
}
