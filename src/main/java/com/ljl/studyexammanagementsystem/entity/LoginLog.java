// 文件路径: src/main/java/com/ljl/studyexammanagementsystem/entity/LoginLog.java
package com.ljl.studyexammanagementsystem.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "sys_login_log")
public class LoginLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "login_account", nullable = false, length = 50)
    private String loginAccount;

    @Column(name = "login_ip", length = 30)
    private String loginIp;

    @Column(name = "login_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date loginTime;

    @Column(name = "logout_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date logoutTime;

    @Column(name = "login_status", nullable = false)
    private Byte loginStatus;

    @Column(name = "is_delete", nullable = false)
    private Byte isDelete;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getLoginAccount() {
        return loginAccount;
    }

    public void setLoginAccount(String loginAccount) {
        this.loginAccount = loginAccount;
    }

    public String getLoginIp() {
        return loginIp;
    }

    public void setLoginIp(String loginIp) {
        this.loginIp = loginIp;
    }

    public Date getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Date loginTime) {
        this.loginTime = loginTime;
    }

    public Date getLogoutTime() {
        return logoutTime;
    }

    public void setLogoutTime(Date logoutTime) {
        this.logoutTime = logoutTime;
    }

    public Byte getLoginStatus() {
        return loginStatus;
    }

    public void setLoginStatus(Byte loginStatus) {
        this.loginStatus = loginStatus;
    }

    public Byte getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Byte isDelete) {
        this.isDelete = isDelete;
    }
}
