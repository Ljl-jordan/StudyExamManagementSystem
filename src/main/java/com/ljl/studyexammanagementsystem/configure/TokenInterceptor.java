// 文件路径: src/main/java/com/ljl/studyexammanagementsystem/configure/TokenInterceptor.java
package com.ljl.studyexammanagementsystem.configure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ljl.studyexammanagementsystem.utils.JwtUtil;
import com.ljl.studyexammanagementsystem.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Token拦截器
 * 拦截所有 /api/** 请求，验证JWT有效性
 * 登录接口 /api/auth/login 在WebMvcConfig中配置放行
 */
@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeResponse(response, "未登录或Token缺失");
            return false;
        }

        String token = authHeader.substring(7);

        if (jwtUtil.isTokenExpired(token)) {
            writeResponse(response, "Token已过期，请重新登录");
            return false;
        }

        if (!jwtUtil.validateToken(token)) {
            writeResponse(response, "Token无效");
            return false;
        }

        // 将用户信息存入request属性，后续Controller直接取用
        request.setAttribute("userId", jwtUtil.getUserIdFromToken(token));
        request.setAttribute("loginAccount", jwtUtil.getLoginAccountFromToken(token));

        return true;
    }

    private void writeResponse(HttpServletResponse response, String msg) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(200);
        response.getWriter().write(objectMapper.writeValueAsString(Result.unauthorized(msg)));
    }
}
