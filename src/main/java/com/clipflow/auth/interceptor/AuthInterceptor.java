package com.clipflow.auth.interceptor;

import com.clipflow.auth.context.UserContext;
import com.clipflow.auth.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public AuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        String authorization = request.getHeader("Authorization");

        if (authorization == null ||
                !authorization.startsWith("Bearer ")) {
            writeUnauthorized(response);
            return false;
        }

        String token = authorization.substring(7);

        try {
            Long userId = jwtUtil.parseUserId(token);
            UserContext.setUserId(userId);
            return true;
        } catch (Exception exception) {
            writeUnauthorized(response);
            return false;
        }
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception exception) {

        UserContext.clear();
    }

    private void writeUnauthorized(HttpServletResponse response)
            throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        response.getWriter().write(
                """
                {"code":401,"message":"未登录或Token无效","data":null}
                """
        );
    }
}