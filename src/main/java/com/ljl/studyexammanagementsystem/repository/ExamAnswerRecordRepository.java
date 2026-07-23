package com.ljl.studyexammanagementsystem.repository;

import com.ljl.studyexammanagementsystem.entity.ExamAnswerRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamAnswerRecordRepository extends JpaRepository<ExamAnswerRecord, Long> {

    List<ExamAnswerRecord> findByAnswerSheetId(Long answerSheetId);

    ExamAnswerRecord findByAnswerSheetIdAndQuestionId(Long answerSheetId, Long questionId);

    List<ExamAnswerRecord> findByAnswerSheetIdIn(List<Long> answerSheetIds);

}
