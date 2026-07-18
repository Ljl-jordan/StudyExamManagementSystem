package com.ljl.studyexammanagementsystem.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@ApiModel(value = "KbMaterial", description = "知识库素材")
@Table(name = "kb_material")
public class KbMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "素材ID")
    private Long id;

    @Column(name = "category_id", nullable = false)
    @ApiModelProperty(value = "所属分类ID")
    private Long categoryId;

    @Column(name = "material_name", nullable = false, length = 100)
    @ApiModelProperty(value = "素材名称")
    private String materialName;

    @Column(name = "material_type", nullable = false)
    @ApiModelProperty(value = "素材内容类型：1富文本 2附件 3外链")
    private Byte materialType;

    @Column(name = "rich_content", columnDefinition = "TEXT")
    @ApiModelProperty(value = "富文本内容")
    private String richContent;

    @Column(name = "link_url", length = 255)
    @ApiModelProperty(value = "外链地址")
    private String linkUrl;

    @Column(name = "cover_file_id")
    @ApiModelProperty(value = "封面文件ID")
    private Long coverFileId;

    @Column(name = "material_status", nullable = false)
    @ApiModelProperty(value = "素材状态：0草稿 1已发布")
    private Byte materialStatus;

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
}
