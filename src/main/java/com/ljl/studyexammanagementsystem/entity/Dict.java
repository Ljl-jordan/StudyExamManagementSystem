// 文件路径: src/main/java/com/ljl/studyexammanagementsystem/entity/Dict.java
package com.ljl.studyexammanagementsystem.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "sys_dict")
@Data
@ApiModel(description = "字典表")
public class Dict {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "主键")
    private Long id;

    @Column(name = "dict_type", nullable = false, length = 50)
    @ApiModelProperty(value = "字典类型")
    private String dictType;

    @Column(name = "dict_label", nullable = false, length = 100)
    @ApiModelProperty(value = "字典标签")
    private String dictLabel;


    @Column(name = "dict_value", nullable = false, length = 100)
    @ApiModelProperty(value = "字典值")
    private String dictValue;

    @ApiModelProperty(value = "排序")
    @Column(name = "sort")
    private Integer sort;


    @Column(name = "create_user")
    @ApiModelProperty(value = "创建人")
    private Long createUser;


    @Column(name = "create_time", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新人")
    @Column(name = "update_user")
    private Long updateUser;

    @ApiModelProperty(value = "更新时间")
    @Column(name = "update_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;

    @ApiModelProperty(value = "是否删除")
    @Column(name = "is_delete", nullable = false)
    private Byte isDelete;


}
