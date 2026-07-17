// 文件路径: src/main/java/com/ljl/studyexammanagementsystem/service/DictService.java
package com.ljl.studyexammanagementsystem.service;

import com.ljl.studyexammanagementsystem.entity.Dict;
import com.ljl.studyexammanagementsystem.vo.Result;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface DictService {

    Result<Page<Dict>> page(Integer pageNum, Integer pageSize, String dictType);

    Result<List<Dict>> dropdown(String dictType);

    Result<Void> add(Dict dict, Long userId);

    Result<Void> update(Long id, Dict dict, Long userId);

    Result<Void> delete(Long id, Long userId);
}
