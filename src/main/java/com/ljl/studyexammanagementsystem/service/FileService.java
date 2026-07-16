// 文件路径: src/main/java/com/ljl/studyexammanagementsystem/service/FileService.java
package com.ljl.studyexammanagementsystem.service;

import com.ljl.studyexammanagementsystem.entity.SysFile;
import com.ljl.studyexammanagementsystem.vo.Result;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public interface FileService {

    Result<Map<String, Object>> upload(String originalFilename, byte[] fileBytes, long fileSize, String businessType, Long userId);

    Result<SysFile> getFileById(Long id);

    void download(Long id, HttpServletResponse response);

    Result<Void> delete(Long id, Long userId);
}
