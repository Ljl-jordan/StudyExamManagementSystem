package com.ljl.studyexammanagementsystem.repository;

import com.ljl.studyexammanagementsystem.entity.SysConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SysConfigRepository extends JpaRepository<SysConfig, Long> {

    @Query("SELECT c FROM SysConfig c WHERE c.isDelete = 0 ORDER BY c.id")
    List<SysConfig> findAllActive();

    Optional<SysConfig> findByConfigKeyAndIsDelete(String configKey, Byte isDelete);

    // 添加一个直接按key查找的方法
   /* default SysConfig findByConfigKey(String configKey) {
        //根据key + isDelete=0 查找有效配置
        Optional<SysConfig> configOpt = findByConfigKeyAndIsDelete(configKey, (byte) 0);
        //有数据返回对象，查不到返回null
        return configOpt.orElse(null);
    }
} */
    //改进版：外部接着使用optional流处理结果，从根源规避空指针异常
    default Optional<SysConfig> findByConfigKey(String configKey) {
        return findByConfigKeyAndIsDelete(configKey, (byte) 0);
    }
}
//default Optional<SysConfig> findValidByConfigKey(String configKey) {
//    return findByConfigKeyAndIsDelete(configKey, (byte) 0);
//}