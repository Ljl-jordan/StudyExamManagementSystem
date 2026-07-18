package com.ljl.studyexammanagementsystem.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Data
@Entity
@ApiModel(value = "KbCategory", description = "知识库分类")
@Table(name = "kb_category")
@JsonInclude(JsonInclude.Include.ALWAYS)
public class KbCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "分类ID")
    private Long id;

    @Column(name = "parent_id", nullable = false)
    @ApiModelProperty(value = "上级分类ID，顶级为0")
    private Long parentId;

    @Column(name = "category_name", nullable = false, length = 50)
    @ApiModelProperty(value = "分类名称")
    private String categoryName;

    @Column(name = "sort_order")
    @ApiModelProperty(value = "排序序号")
    private Integer sortOrder;

    @Column(name = "create_user")
    @ApiModelProperty(value = "创建人用户ID")
    private Long createUser;

    @Column(name = "create_time", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @Column(name = "update_user")
    @ApiModelProperty(value = "更新人用户ID")
    private Long updateUser;

    @Column(name = "update_time")
    @Temporal(TemporalType.TIMESTAMP)
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @Column(name = "is_delete", nullable = false)
    @ApiModelProperty(value = "逻辑删除标志")
    private Byte isDelete;

    @Transient
    @ApiModelProperty(value = "子分类列表（树形查询时返回）")
    private List<KbCategory> children;
}
