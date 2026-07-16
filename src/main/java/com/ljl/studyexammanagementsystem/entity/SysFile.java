// 文件路径: src/main/java/com/ljl/studyexammanagementsystem/entity/SysFile.java
package com.ljl.studyexammanagementsystem.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "sys_file")
@Data
@ApiModel
public class SysFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @ApiModelProperty(value = "文件ID")
    private Long id;

    @Column(name = "business_type", length = 30)
    @ApiModelProperty(value = "业务类型")
    private String businessType;

    @Column(name = "business_id")
    @ApiModelProperty(value = "业务ID")
    private Long businessId;

    @Column(name = "file_name", nullable = false, length = 100)
    @ApiModelProperty(value = "文件名")
    private String fileName;

    @Column(name = "file_url", nullable = false, length = 255)
    @ApiModelProperty(value = "文件URL")
    private String fileUrl;

    @Column(name = "file_size")
    @ApiModelProperty(value = "文件大小")
    private Long fileSize;

    @Column(name = "suffix", length = 20)
    @ApiModelProperty(value = "文件后缀")
    private String suffix;

    @Column(name = "upload_user_id")
    @ApiModelProperty(value = "上传用户ID")
    private Long uploadUserId;

    @Column(name = "upload_time")
    @Temporal(TemporalType.TIMESTAMP)
    @ApiModelProperty(value = "上传时间")
    private Date uploadTime;

    @Column(name = "is_delete", nullable = false)
    @ApiModelProperty(value = "是否删除")
    private Byte isDelete;

}
