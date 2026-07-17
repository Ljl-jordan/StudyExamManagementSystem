package com.ljl.studyexammanagementsystem.service;

import com.ljl.studyexammanagementsystem.entity.SysMenu;
import com.ljl.studyexammanagementsystem.vo.Result;

import java.util.List;
import java.util.Map;

public interface MenuService {

    Result<List<SysMenu>> getTree();

    Result<Map<String, Object>> getUserPermissions(Long userId);
}
