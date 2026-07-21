package com.ljl.studyexammanagementsystem.repository;

import com.ljl.studyexammanagementsystem.entity.QbQuestionSingle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QbQuestionSingleRepository extends JpaRepository<QbQuestionSingle, Long> {

    QbQuestionSingle findByQuestionId(Long questionId);

    void deleteByQuestionId(Long questionId);
}
