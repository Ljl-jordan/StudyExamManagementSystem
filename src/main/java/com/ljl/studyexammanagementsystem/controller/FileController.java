// 文件路径: src/main/java/com/ljl/studyexammanagementsystem/controller/FileController.java
package com.ljl.studyexammanagementsystem.controller;

import com.ljl.studyexammanagementsystem.entity.SysFile;
import com.ljl.studyexammanagementsystem.service.FileService;
import com.ljl.studyexammanagementsystem.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
@RequestMapping("/api/file")
@Api(tags = "文件管理")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    @ApiOperation(value = "文件上传", notes = "上传文件并返回文件信息")
    public Result<Map<String, Object>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "businessType", required = false) String businessType,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            return fileService.upload(file.getOriginalFilename(), file.getBytes(), file.getSize(), businessType, userId);
        } catch (Exception e) {
            return Result.serverError("文件读取失败");
        }
    }

    @GetMapping("/preview/{id}")
    @ApiOperation(value = "文件预览", notes = "预览文件")
    public void preview(@PathVariable Long id, HttpServletResponse response) {
        fileService.download(id, response);
    }

    @GetMapping("/download/{id}")
    @ApiOperation(value = "文件下载", notes = "下载文件")
    public void download(@PathVariable Long id, HttpServletResponse response) {
        fileService.download(id, response);
    }

    @PostMapping("/delete")
    @ApiOperation(value = "文件删除", notes = "删除文件")
    public Result<Void> delete(@RequestParam Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return fileService.delete(id, userId);
    }
}
