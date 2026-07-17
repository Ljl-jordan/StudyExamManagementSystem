package com.ljl.studyexammanagementsystem.controller;

import com.ljl.studyexammanagementsystem.entity.Dict;
import com.ljl.studyexammanagementsystem.service.DictService;
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
@RequestMapping("/api/dict")
@Api(tags = "字典管理")
public class DictController {

    @Autowired
    private DictService dictService;

    @GetMapping("/page")
    @ApiOperation(value = "分页查询字典列表", notes = "分页查询字典列表")
    public Result<Page<Dict>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String dictType) {
        return dictService.page(pageNum, pageSize, dictType);
    }

    @GetMapping("/dropdown")
    @ApiOperation(value = "下拉选择字典列表", notes = "下拉选择字典列表")
    public Result<List<Dict>> dropdown(@RequestParam String dictType) {
        return dictService.dropdown(dictType);
    }

    @PostMapping("/add")
    @ApiOperation(value = "添加字典", notes = "添加字典")
    public Result<Void> add(@RequestBody Dict dict, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return dictService.add(dict, userId);
    }

    @PostMapping("/update")
    @ApiOperation(value = "修改字典", notes = "修改字典")
    public Result<Void> update(@RequestParam Long id, @RequestBody Dict dict, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return dictService.update(id, dict, userId);
    }

    @PostMapping("/delete")
    @ApiOperation(value = "删除字典", notes = "删除字典")
    public Result<Void> delete(@RequestParam Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return dictService.delete(id, userId);
    }
}
