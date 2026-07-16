package com.ljl.studyexammanagementsystem.service;

import com.ljl.studyexammanagementsystem.vo.LoginVO;
import com.ljl.studyexammanagementsystem.vo.Result;

import java.util.Map;

public interface AuthService {

    Result<Map<String, Object>> login(LoginVO loginVO, String loginIp);

    Result<Void> logout(Long userId);

    Result<Map<String, Object>> refreshToken(String token);

    Result<Void> resetPassword(Long userId, String oldPassword, String newPassword);

    Result<Map<String, Object>> getCurrentUser(Long userId);

    Result<Void> unlockAccount(String loginAccount);
}
