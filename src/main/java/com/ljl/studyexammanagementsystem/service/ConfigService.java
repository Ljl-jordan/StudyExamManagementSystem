package com.ljl.studyexammanagementsystem.service;

import com.ljl.studyexammanagementsystem.entity.SysConfig;
import com.ljl.studyexammanagementsystem.vo.Result;

import java.util.List;

public interface ConfigService {

    Result<List<SysConfig>> list();

    Result<Void> edit(String configKey, String configValue);
}
