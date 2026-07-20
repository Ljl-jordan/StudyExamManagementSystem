package com.ljl.studyexammanagementsystem.service;

import com.ljl.studyexammanagementsystem.vo.Result;

import java.util.List;
import java.util.Map;

public interface LearnStudyRecordService {

    Result<Void> reportStudyTime(Long taskId, Long userId, Long materialId, Integer studySeconds);

    Result<List<Map<String, Object>>> getProgress(Long taskId, Long userId);

    Result<Void> submitSignature(Long taskId, Long userId, Long fileId);
}
