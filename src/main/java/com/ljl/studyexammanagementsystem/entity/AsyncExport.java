// 文件路径: src/main/java/com/ljl/studyexammanagementsystem/entity/AsyncExport.java
package com.ljl.studyexammanagementsystem.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "sys_async_export")
@Data
@ApiModel(description = "异步导出任务表")
public class AsyncExport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "主键ID")
    private Long id;

    @Column(name = "user_id", nullable = false)
    @ApiModelProperty(value = "操作人ID")
    private Long userId;

    @Column(name = "file_id")
    @ApiModelProperty(value = "生成文件ID")
    private Long fileId;

    @Column(name = "task_status", nullable = false)
    @ApiModelProperty(value = "任务状态：0处理中 1完成 2失败")
    private Byte taskStatus;

    @Column(name = "export_params", columnDefinition = "TEXT")
    @ApiModelProperty(value = "导出筛选参数(JSON字符串)")
    private String exportParams;

    @Column(name = "create_time")
    @Temporal(TemporalType.TIMESTAMP)
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @Column(name = "update_time")
    @Temporal(TemporalType.TIMESTAMP)
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @Column(name = "is_delete", nullable = false)
    @ApiModelProperty(value = "是否删除")
    private Byte isDelete;
}
