package com.healthy.system.auth;

import com.healthy.system.common.ApiResponse;
import io.jsonwebtoken.Claims;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public AuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // 放行浏览器的 CORS 预检请求，否则会导致前端看到 OPTIONS 401
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeUnauthorized(response, "未登录");
            return false;
        }
        String token = authHeader.substring(7);
        try {
            Claims claims = jwtUtil.parseToken(token);
            Long uid = claims.get("uid", Long.class);
            String uname = claims.get("uname", String.class);
            String role = claims.get("role", String.class);
            UserContext.set(new LoginUser(uid, uname, role));
            return true;
        } catch (Exception e) {
            writeUnauthorized(response, "登录已失效，请重新登录");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse<Void> body = ApiResponse.error(401, message);
        String json = "{\"code\":" + body.getCode() + ",\"message\":\"" + message + "\",\"data\":null}";
        response.getWriter().write(json);
    }
}

