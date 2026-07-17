package com.ljl.studyexammanagementsystem.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@ApiModel(description = "系统消息")
@Table(name = "sys_message")
public class SysMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "消息ID")
    private Long id;

    @Column(name = "receive_user_id", nullable = false)
    @ApiModelProperty(value = "接收用户ID")
    private Long receiveUserId;

    @Column(name = "business_type", nullable = false, length = 30)
    @ApiModelProperty(value = "业务类型：learnTask/examPaper/manual")
    private String businessType;

    @Column(name = "business_id")
    @ApiModelProperty(value = "关联业务ID")
    private Long businessId;

    @Column(name = "title", nullable = false, length = 100)
    @ApiModelProperty(value = "消息标题")
    private String title;

    @Column(name = "content", length = 500)
    @ApiModelProperty(value = "消息内容")
    private String content;

    @Column(name = "read_flag", nullable = false)
    @ApiModelProperty(value = "已读标记：0未读 1已读")
    private Byte readFlag;

    @Column(name = "create_time", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @Column(name = "is_delete", nullable = false)
    @ApiModelProperty(value = "逻辑删除")
    private Byte isDelete;
}
