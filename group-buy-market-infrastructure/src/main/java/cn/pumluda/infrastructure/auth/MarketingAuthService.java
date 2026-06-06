package cn.pumluda.infrastructure.auth;

import cn.pumluda.infrastructure.dao.IMarketingUserDao;
import cn.pumluda.infrastructure.dao.po.MarketingUserPo;
import cn.pumluda.types.enums.ResponseEnum;
import cn.pumluda.types.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Project: group-buy-market-better <p>
 * File: MarketingAuthService <p>
 * Created by: 16374 <p>
 * Date: 2026/6/6 <p>
 * Description: 营销后台认证服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarketingAuthService {

    private final IMarketingUserDao marketingUserDao;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 应用启动时确保默认管理员存在且密码哈希正确
     */
    @PostConstruct
    public void initAdminUser() {
        MarketingUserPo admin = marketingUserDao.findByUsername("admin");
        String correctHash = passwordEncoder.encode("admin123");

        if (admin == null) {
            log.info("[营销认证] 首次启动，创建默认管理员 admin");
            marketingUserDao.insert(MarketingUserPo.builder()
                    .username("admin").passwordHash(correctHash).role("ADMIN").build());
        } else if (!passwordEncoder.matches("admin123", admin.getPasswordHash())) {
            log.warn("[营销认证] 密码哈希不匹配，自动修复中...");
            marketingUserDao.updatePassword("admin", correctHash);
            log.info("[营销认证] 管理员密码已修复，用户名: admin 密码: admin123");
        } else {
            log.info("[营销认证] 管理员账号正常");
        }
    }

    /**
     * 登录认证
     *
     * @param username 用户名
     * @param password 明文密码
     * @return JWT token
     */
    public String login(String username, String password) {
        MarketingUserPo user = marketingUserDao.findByUsername(username);
        if (user == null) {
            log.warn("[营销登录] 用户不存在 username={}", username);
            throw new AppException(
                    ResponseEnum.ILLEGAL_PARAMETER.getCode(),
                    "用户名或密码错误"
            );
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            log.warn("[营销登录] 密码错误 username={}", username);
            throw new AppException(
                    ResponseEnum.ILLEGAL_PARAMETER.getCode(),
                    "用户名或密码错误"
            );
        }

        log.info("[营销登录] 登录成功 username={} role={}", username, user.getRole());
        return jwtUtil.generateToken(user.getUsername(), user.getRole());
    }

}
