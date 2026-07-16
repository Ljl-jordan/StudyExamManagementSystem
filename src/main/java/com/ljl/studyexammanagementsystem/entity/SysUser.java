// 文件路径: src/main/java/com/ljl/studyexammanagementsystem/entity/SysUser.java
package com.ljl.studyexammanagementsystem.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "sys_user")
public class SysUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login_account", nullable = false, length = 50)
    private String loginAccount;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(length = 11)
    private String phone;

    @Column(name = "user_name", nullable = false, length = 30)
    private String userName;

    @Column(name = "org_id", nullable = false)
    private Long orgId;

    @Column(name = "user_status", nullable = false)
    private Byte userStatus;

    @Column(name = "lock_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lockTime;

    @Column(name = "create_user")
    private Long createUser;

    @Column(name = "create_time", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    @Column(name = "update_user")
    private Long updateUser;

    @Column(name = "update_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;

    @Column(name = "is_delete", nullable = false)
    private Byte isDelete;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLoginAccount() { return loginAccount; }
    public void setLoginAccount(String loginAccount) { this.loginAccount = loginAccount; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public Long getOrgId() { return orgId; }
    public void setOrgId(Long orgId) { this.orgId = orgId; }

    public Byte getUserStatus() { return userStatus; }
    public void setUserStatus(Byte userStatus) { this.userStatus = userStatus; }

    public Date getLockTime() { return lockTime; }
    public void setLockTime(Date lockTime) { this.lockTime = lockTime; }

    public Long getCreateUser() { return createUser; }
    public void setCreateUser(Long createUser) { this.createUser = createUser; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public Long getUpdateUser() { return updateUser; }
    public void setUpdateUser(Long updateUser) { this.updateUser = updateUser; }

    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }

    public Byte getIsDelete() { return isDelete; }
    public void setIsDelete(Byte isDelete) { this.isDelete = isDelete; }
}
