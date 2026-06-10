package cn.pumluda.infrastructure.auth;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Project: group-buy-market-better <p>
 * File: MarketingAuthInterceptor <p>
 * Created by: 16374 <p>
 * Date: 2026/6/6 <p>
 * Description: 营销后台 JWT 鉴权拦截器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MarketingAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // 登录接口不需要鉴权
        if (request.getRequestURI().contains("/marketing/login")) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("[营销鉴权] 缺少 Authorization 头");
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":\"0401\",\"info\":\"未登录或Token已过期\"}");
            return false;
        }

        String token = authHeader.substring(7);
        Claims claims = jwtUtil.parseToken(token);
        if (claims == null) {
            log.warn("[营销鉴权] Token 无效或已过期");
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":\"0401\",\"info\":\"Token无效或已过期\"}");
            return false;
        }

        // 将用户信息放入 request attribute，方便 Controller 获取
        request.setAttribute("username", claims.get("username", String.class));
        request.setAttribute("role", claims.get("role", String.class));

        return true;
    }

}
