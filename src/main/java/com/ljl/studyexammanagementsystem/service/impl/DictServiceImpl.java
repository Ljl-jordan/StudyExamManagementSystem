// 文件路径: src/main/java/com/ljl/studyexammanagementsystem/service/impl/DictServiceImpl.java
package com.ljl.studyexammanagementsystem.service.impl;

import com.ljl.studyexammanagementsystem.repository.DictRepository;
import com.ljl.studyexammanagementsystem.entity.Dict;
import com.ljl.studyexammanagementsystem.service.DictService;
import com.ljl.studyexammanagementsystem.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DictServiceImpl implements DictService {

    @Autowired
    private DictRepository dictRepository;

    // ==================== 1. 分页查询 ====================

    @Override
    public Result<Map<String, Object>> page(Integer pageNum, Integer pageSize, String dictType) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.ASC, "sort"));
        Page<Dict> page;
        if (dictType != null && !dictType.trim().isEmpty()) {
            page = dictRepository.findByDictTypeAndIsDelete(dictType, (byte) 0, pageable);
        } else {
            page = dictRepository.findByIsDelete((byte) 0, pageable);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("list", page.getContent());
        data.put("total", page.getTotalElements());
        data.put("pageNum", pageNum);
        data.put("pageSize", pageSize);
        return Result.success(data);
    }

    // ==================== 2. 下拉列表 ====================

    @Override
    public Result<List<Dict>> dropdown(String dictType) {
        List<Dict> list = dictRepository.findByDictTypeAndIsDeleteOrderBySortAsc(dictType, (byte) 0);
        return Result.success(list);
    }

    // ==================== 3. 新增字典 ====================

    @Override
    public Result<Void> add(Dict dict, Long userId) {
        dict.setCreateUser(userId);
        dict.setCreateTime(new Date());
        dict.setIsDelete((byte) 0);
        if (dict.getSort() == null) {
            dict.setSort(0);
        }
        dictRepository.save(dict);
        return Result.success("新增成功", null);
    }

    // ==================== 4. 修改字典 ====================

    @Override
    public Result<Void> update(Long id, Dict dict, Long userId) {
        Dict existing = dictRepository.findById(id).orElse(null);
        if (existing == null || existing.getIsDelete() == 1) {
            return Result.paramError("字典不存在");
        }
        existing.setDictType(dict.getDictType());
        existing.setDictLabel(dict.getDictLabel());
        existing.setDictValue(dict.getDictValue());
        existing.setSort(dict.getSort());
        existing.setUpdateUser(userId);
        existing.setUpdateTime(new Date());
        dictRepository.save(existing);
        return Result.success("修改成功", null);
    }

    // ==================== 5. 删除字典 ====================

    @Override
    public Result<Void> delete(Long id, Long userId) {
        Dict dict = dictRepository.findById(id).orElse(null);
        if (dict == null || dict.getIsDelete() == 1) {
            return Result.paramError("字典不存在");
        }
        dict.setIsDelete((byte) 1);
        dict.setUpdateUser(userId);
        dict.setUpdateTime(new Date());
        dictRepository.save(dict);
        return Result.success("删除成功", null);
    }
}
