// 文件路径: src/main/java/com/ljl/studyexammanagementsystem/service/impl/ExportServiceImpl.java
package com.ljl.studyexammanagementsystem.service.impl;

import com.ljl.studyexammanagementsystem.repository.AsyncExportRepository;
import com.ljl.studyexammanagementsystem.repository.SysFileRepository;
import com.ljl.studyexammanagementsystem.entity.AsyncExport;
import com.ljl.studyexammanagementsystem.entity.SysFile;
import com.ljl.studyexammanagementsystem.service.ExportService;
import com.ljl.studyexammanagementsystem.utils.ExcelUtil;
import com.ljl.studyexammanagementsystem.vo.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

@Service
public class ExportServiceImpl implements ExportService {

    private static final Logger log = LoggerFactory.getLogger(ExportServiceImpl.class);

    @Autowired
    private AsyncExportRepository asyncExportRepository;

    @Autowired
    private SysFileRepository sysFileRepository;

    @Value("${file.upload-path}")
    private String uploadPath;

    // ==================== 1. 创建导出任务 ====================

    @Override
    public Result<Void> createTask(Long userId, String exportParams) {
        AsyncExport task = new AsyncExport();
        task.setUserId(userId);
        task.setTaskStatus((byte) 0);
        task.setExportParams(exportParams);
        task.setCreateTime(new Date());
        task.setUpdateTime(new Date());
        task.setIsDelete((byte) 0);
        asyncExportRepository.save(task);

        processExportAsync(task.getId());
        return Result.success("导出任务已创建", null);
    }

    // ==================== 2. 查询任务列表 ====================

    @Override
    public Result<Map<String, Object>> taskList(Long userId, Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<AsyncExport> page = asyncExportRepository.findByUserIdAndIsDeleteOrderByCreateTimeDesc(userId, (byte) 0, pageable);

        Map<String, Object> data = new HashMap<>();
        data.put("list", page.getContent());
        data.put("total", page.getTotalElements());
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);
        return Result.success(data);
    }

    // ==================== 异步处理导出 ====================

    @Async
    public void processExportAsync(Long taskId) {
        try {
            AsyncExport task = asyncExportRepository.findById(taskId).orElse(null);
            if (task == null) return;

            // 模拟导出处理（实际业务中根据 exportParams 查询数据生成Excel）
            Thread.sleep(2000);

            // 生成示例Excel文件
            String dateDir = "export";
            File dir = new File(uploadPath + dateDir);
            if (!dir.exists()) dir.mkdirs();
            String fileName = "export_" + taskId + "_" + System.currentTimeMillis() + ".xlsx";
            String fullPath = dir.getAbsolutePath() + "/" + fileName;

            List<String> headers = Arrays.asList("ID", "名称", "状态");
            List<List<String>> dataList = new ArrayList<>();
            dataList.add(Arrays.asList("1", "示例数据", "正常"));
            ExcelUtil.export(fullPath, "导出数据", headers, dataList);

            // 保存文件记录
            SysFile sysFile = new SysFile();
            sysFile.setFileName(fileName);
            sysFile.setFileUrl(dateDir + "/" + fileName);
            sysFile.setFileSize(new File(fullPath).length());
            sysFile.setSuffix("xlsx");
            sysFile.setBusinessType("export");
            sysFile.setBusinessId(taskId);
            sysFile.setUploadUserId(task.getUserId());
            sysFile.setUploadTime(new Date());
            sysFile.setIsDelete((byte) 0);
            sysFileRepository.save(sysFile);

            // 更新任务状态为完成
            task.setFileId(sysFile.getId());
            task.setTaskStatus((byte) 1);
            task.setUpdateTime(new Date());
            asyncExportRepository.save(task);

            log.info("导出任务[{}]已完成，文件ID={}", taskId, sysFile.getId());
        } catch (Exception e) {
            log.error("导出任务[{}]处理失败: {}", taskId, e.getMessage());
            AsyncExport task = asyncExportRepository.findById(taskId).orElse(null);
            if (task != null) {
                task.setTaskStatus((byte) 2);
                task.setUpdateTime(new Date());
                asyncExportRepository.save(task);
            }
        }
    }
}
