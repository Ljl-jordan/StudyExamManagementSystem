package com.ljl.studyexammanagementsystem.controller;

import com.ljl.studyexammanagementsystem.entity.KbMaterial;
import com.ljl.studyexammanagementsystem.service.KbMaterialService;
import com.ljl.studyexammanagementsystem.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/kb/material")
@Api(tags = "知识库素材管理接口")
public class KbMaterialController {

    @Autowired
    private KbMaterialService kbMaterialService;

    @GetMapping("/list")
    @ApiOperation(value = "素材分页列表")
    public Result<Page<KbMaterial>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long orgId = (Long) request.getAttribute("orgId");
        return kbMaterialService.page(pageNum, pageSize, keyword, categoryId, userId, orgId);
    }

    @GetMapping("/detail/{id}")
    @ApiOperation(value = "素材详情")
    public Result<KbMaterial> detail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long orgId = (Long) request.getAttribute("orgId");
        return kbMaterialService.detail(id, userId, orgId);
    }

    @PostMapping("/add")
    @ApiOperation(value = "新增草稿素材")
    public Result<Void> add(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        KbMaterial material = buildMaterialFromParams(params);
        List<Long> fileIds = (List<Long>) params.get("fileIds");
        return kbMaterialService.addDraft(material, fileIds, userId);
    }

    @PutMapping("/edit")
    @ApiOperation(value = "编辑素材")
    public Result<Void> edit(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long id = params.get("id") != null ? Long.valueOf(params.get("id").toString()) : null;
        if (id == null) {
            return Result.paramError("素材ID不能为空");
        }
        KbMaterial material = buildMaterialFromParams(params);
        List<Long> fileIds = (List<Long>) params.get("fileIds");
        return kbMaterialService.update(id, material, fileIds, userId);
    }

    @PutMapping("/publish/{id}")
    @ApiOperation(value = "发布素材")
    public Result<Void> publish(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return kbMaterialService.publish(id, userId);
    }

    @DeleteMapping("/del/{id}")
    @ApiOperation(value = "删除素材")
    public Result<Void> delete(@PathVariable Long id) {
        return kbMaterialService.delete(id);
    }

    @PostMapping("/batchMove")
    @ApiOperation(value = "批量迁移分类")
    public Result<Void> batchMove(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<Long> materialIds = (List<Long>) params.get("materialIds");
        Long targetCategoryId = params.get("targetCategoryId") != null ? Long.valueOf(params.get("targetCategoryId").toString()) : null;
        return kbMaterialService.batchMoveCategory(materialIds, targetCategoryId, userId);
    }

    @GetMapping("/select")
    @ApiOperation(value = "素材下拉筛选（仅已发布）")
    public Result<Page<KbMaterial>> select(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        return kbMaterialService.publishedSelect(pageNum, pageSize, keyword);
    }

    /**
     * 从请求参数构建素材实体
     */
    private KbMaterial buildMaterialFromParams(Map<String, Object> params) {
        KbMaterial material = new KbMaterial();
        if (params.get("id") != null) {
            material.setId(Long.valueOf(params.get("id").toString()));
        }
        if (params.get("materialName") != null) {
            material.setMaterialName(params.get("materialName").toString());
        }
        if (params.get("categoryId") != null) {
            material.setCategoryId(Long.valueOf(params.get("categoryId").toString()));
        }
        if (params.get("materialType") != null) {
            material.setMaterialType(Byte.valueOf(params.get("materialType").toString()));
        }
        if (params.get("richContent") != null) {
            material.setRichContent(params.get("richContent").toString());
        }
        if (params.get("linkUrl") != null) {
            material.setLinkUrl(params.get("linkUrl").toString());
        }
        if (params.get("coverFileId") != null) {
            material.setCoverFileId(Long.valueOf(params.get("coverFileId").toString()));
        }
        return material;
    }
}
