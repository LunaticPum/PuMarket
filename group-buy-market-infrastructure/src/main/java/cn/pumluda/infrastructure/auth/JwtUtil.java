package cn.pumluda.infrastructure.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Project: group-buy-market-better <p>
 * File: JwtUtil <p>
 * Created by: 16374 <p>
 * Date: 2026/6/6 <p>
 * Description: JWT 工具类 —— 营销后台 Token 签发与校验
 */
@Component
public class JwtUtil {

    private static final String SECRET = "GroupBuyMarketMarketingSecretKey2026!@#$%";
    private static final long EXPIRE_HOURS = 24;

    /**
     * 签发 JWT Token
     *
     * @param username 用户名
     * @param role     角色
     * @return JWT token 字符串
     */
    public String generateToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE_HOURS * 3600 * 1000))
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }

    /**
     * 解析并校验 Token
     *
     * @param token JWT token
     * @return Claims 或 null（无效/过期）
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(SECRET)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 判断 Token 是否有效
     */
    public boolean isValid(String token) {
        Claims claims = parseToken(token);
        return claims != null && !claims.getExpiration().before(new Date());
    }

}
