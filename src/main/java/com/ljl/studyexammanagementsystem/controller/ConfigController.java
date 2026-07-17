package com.ljl.studyexammanagementsystem.controller;

import com.ljl.studyexammanagementsystem.entity.SysConfig;
import com.ljl.studyexammanagementsystem.service.ConfigService;
import com.ljl.studyexammanagementsystem.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/config")
@Api(tags = "系统配置接口")
public class ConfigController {

    @Autowired
    private ConfigService configService;

    @GetMapping("/list")
    @ApiOperation(value = "查询系统配置")
    public Result<List<SysConfig>> list() {
        return configService.list();
    }

    @PutMapping("/edit")
    @ApiOperation(value = "修改系统配置")
    public Result<Void> edit(@RequestBody Map<String, String> params) {
        return configService.edit(params.get("configKey"), params.get("configValue"));
    }
}
