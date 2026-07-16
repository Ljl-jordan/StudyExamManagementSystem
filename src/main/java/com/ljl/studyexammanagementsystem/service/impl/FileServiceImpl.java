// 文件路径: src/main/java/com/ljl/studyexammanagementsystem/service/impl/FileServiceImpl.java
package com.ljl.studyexammanagementsystem.service.impl;

import com.ljl.studyexammanagementsystem.repository.SysFileRepository;
import com.ljl.studyexammanagementsystem.entity.SysFile;
import com.ljl.studyexammanagementsystem.service.FileService;
import com.ljl.studyexammanagementsystem.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;

@Service
public class FileServiceImpl implements FileService {

    @Value("${file.upload-path}")
    private String uploadPath;

    @Value("${file.allowed-extensions}")
    private String allowedExtensions;

    @Value("${file.max-size}")
    private Long maxSize;

    @Autowired
    private SysFileRepository sysFileRepository;

    // ==================== 1. 文件上传 ====================

    @Override
    public Result<Map<String, Object>> upload(String originalFilename, byte[] fileBytes, long fileSize, String businessType, Long userId) {
        if (originalFilename == null || originalFilename.isEmpty()) {
            return Result.paramError("文件不能为空");
        }

        String suffix = "";
        if (originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        }

        // 校验文件格式
        List<String> allowed = Arrays.asList(allowedExtensions.split(","));
        if (!allowed.contains(suffix)) {
            return Result.paramError("不支持的文件格式：" + suffix + "，允许格式：" + allowedExtensions);
        }

        // 校验文件大小
        if (fileSize > maxSize) {
            return Result.paramError("文件大小超过限制，最大允许" + (maxSize / 1024 / 1024) + "MB");
        }

        // 生成唯一文件名
        String newFileName = UUID.randomUUID().toString().replace("-", "") + "." + suffix;

        // 按日期创建子目录
        Calendar cal = Calendar.getInstance();
        String dateDir = cal.get(Calendar.YEAR) + "/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.DAY_OF_MONTH);
        File dir = new File(uploadPath + dateDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 写入磁盘
        File destFile = new File(dir, newFileName);
        try (FileOutputStream fos = new FileOutputStream(destFile)) {
            fos.write(fileBytes);
        } catch (IOException e) {
            return Result.serverError("文件上传失败：" + e.getMessage());
        }

        // 保存数据库记录
        SysFile sysFile = new SysFile();
        sysFile.setFileName(originalFilename);
        sysFile.setFileUrl(dateDir + "/" + newFileName);
        sysFile.setFileSize(fileSize);
        sysFile.setSuffix(suffix);
        sysFile.setBusinessType(businessType);
        sysFile.setUploadUserId(userId);
        sysFile.setUploadTime(new Date());
        sysFile.setIsDelete((byte) 0);
        sysFileRepository.save(sysFile);

        Map<String, Object> data = new HashMap<>();
        data.put("fileId", sysFile.getId());
        data.put("fileName", originalFilename);
        data.put("fileUrl", sysFile.getFileUrl());
        data.put("fileSize", fileSize);
        return Result.success("上传成功", data);
    }

    // ==================== 2. 查询文件 ====================

    @Override
    public Result<SysFile> getFileById(Long id) {
        SysFile file = sysFileRepository.findById(id).orElse(null);
        if (file == null || file.getIsDelete() == 1) {
            return Result.paramError("文件不存在");
        }
        return Result.success(file);
    }

    // ==================== 3. 文件下载/预览 ====================

    @Override
    public void download(Long id, HttpServletResponse response) {
        SysFile file = sysFileRepository.findById(id).orElse(null);
        if (file == null || file.getIsDelete() == 1) {
            response.setStatus(404);
            return;
        }

        File diskFile = new File(uploadPath + file.getFileUrl());
        if (!diskFile.exists()) {
            response.setStatus(404);
            return;
        }

        try (InputStream is = new FileInputStream(diskFile);
             OutputStream os = response.getOutputStream()) {
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode(file.getFileName(), "UTF-8"));
            byte[] buffer = new byte[4096];
            int len;
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            os.flush();
        } catch (IOException e) {
            response.setStatus(500);
        }
    }

    // ==================== 4. 文件删除（逻辑删除） ====================

    @Override
    public Result<Void> delete(Long id, Long userId) {
        SysFile file = sysFileRepository.findById(id).orElse(null);
        if (file == null || file.getIsDelete() == 1) {
            return Result.paramError("文件不存在");
        }
        file.setIsDelete((byte) 1);
        sysFileRepository.save(file);
        return Result.success("删除成功", null);
    }
}
