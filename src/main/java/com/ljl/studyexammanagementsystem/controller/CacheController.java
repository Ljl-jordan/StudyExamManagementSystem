package com.ljl.studyexammanagementsystem.controller;

import com.ljl.studyexammanagementsystem.cache.CacheService;
import com.ljl.studyexammanagementsystem.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "缓存管理")
@RestController
@RequestMapping("/api/cache")
public class CacheController {

    private final CacheService cacheService;

    public CacheController(CacheService cacheService) {

        this.cacheService = cacheService;
    }

    @ApiOperation("获取缓存统计")
    @GetMapping("/stats")
    public Result<CacheService.CacheStats> stats() {

        return Result.success(cacheService.stats());
    }
}