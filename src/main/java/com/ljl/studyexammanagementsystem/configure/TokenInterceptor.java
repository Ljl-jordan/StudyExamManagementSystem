package com.ljl.studyexammanagementsystem.configure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ljl.studyexammanagementsystem.entity.SysUser;
import com.ljl.studyexammanagementsystem.repository.SysUserRepository;
import com.ljl.studyexammanagementsystem.utils.JwtUtil;
import com.ljl.studyexammanagementsystem.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SysUserRepository sysUserRepository;

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

        String token = authHeader.substring(7);//去掉“Bearer”前缀

        if (jwtUtil.isTokenExpired(token)) {
            writeResponse(response, "Token已过期，请重新登录");
            return false;
        }

        if (!jwtUtil.validateToken(token)) {
            writeResponse(response, "Token无效");
            return false;
        }
//注入用户信息
        Long userId = jwtUtil.getUserIdFromToken(token);
        request.setAttribute("userId", userId); //将用户ID注入到请求中
        request.setAttribute("loginAccount", jwtUtil.getLoginAccountFromToken(token));  //将登录账号注入到请求中
//根据用户id查询用户信息，注入到请求中，用于后续权限控制
        SysUser user = sysUserRepository.findActiveById(userId).orElse(null);
        if (user != null) {
            request.setAttribute("orgId", user.getOrgId());
        }

        return true;
    }

    //统一返回格式写入响应
    private void writeResponse(HttpServletResponse response, String msg) throws Exception {
        response.setContentType("application/json;charset=UTF-8");//设置响应内容类型为application/json
        response.setStatus(200);//设置响应状态码为200
        //将错误信息通过Result.unauthorized(msg)封装为统一响应格式，再用objectMapper序列化为JSON字符串写入响应体
        response.getWriter().write(objectMapper.writeValueAsString(Result.unauthorized(msg)));
    }
}
