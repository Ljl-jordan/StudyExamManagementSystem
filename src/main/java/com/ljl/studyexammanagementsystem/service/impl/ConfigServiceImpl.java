package com.ljl.studyexammanagementsystem.service.impl;

import com.ljl.studyexammanagementsystem.entity.SysConfig;
import com.ljl.studyexammanagementsystem.repository.SysConfigRepository;
import com.ljl.studyexammanagementsystem.service.ConfigService;
import com.ljl.studyexammanagementsystem.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class ConfigServiceImpl implements ConfigService {

    @Autowired
    private SysConfigRepository sysConfigRepository;

    @Override
    public Result<List<SysConfig>> list() {
        return Result.success(sysConfigRepository.findAllActive());
    }

    @Override
    @Transactional
    public Result<Void> edit(String configKey, String configValue) {
        if (configKey == null || configKey.trim().isEmpty()) {
            return Result.paramError("配置Key不能为空");
        }
        SysConfig config = sysConfigRepository.findByConfigKeyAndIsDelete(configKey.trim(), (byte) 0).orElse(null);
        if (config == null) {
            return Result.paramError("配置项不存在");
        }
        config.setConfigValue(configValue);
        config.setUpdateTime(new Date());
        sysConfigRepository.save(config);
        return Result.success("修改成功", null);
    }
}
