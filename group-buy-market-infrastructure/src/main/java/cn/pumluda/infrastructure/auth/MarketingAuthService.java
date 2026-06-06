package cn.pumluda.infrastructure.auth;

import cn.pumluda.infrastructure.dao.IMarketingUserDao;
import cn.pumluda.infrastructure.dao.po.MarketingUserPo;
import cn.pumluda.types.enums.ResponseEnum;
import cn.pumluda.types.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 营销后台认证服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarketingAuthService {

    private final IMarketingUserDao marketingUserDao;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private volatile boolean adminReady = false;

    /**
     * 应用完全启动后初始化管理员（此时 DB 连接池就绪）
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initAdminUser() {
        try {
            ensureAdmin();
        } catch (Exception e) {
            log.error("[营销认证] 初始化管理员失败，将在首次登录时重试", e);
        }
    }

    /**
     * 登录认证 — 每次登录时兜底确保管理员存在且密码正确
     */
    public String login(String username, String password) {
        // 兜底：如果管理员初始化曾失败，在此重试
        if (!adminReady) {
            try {
                ensureAdmin();
            } catch (Exception ignored) {
            }
        }

        MarketingUserPo user = marketingUserDao.findByUsername(username);
        if (user == null) {
            log.warn("[营销登录] 用户不存在 username={}", username);
            throw new AppException(ResponseEnum.ILLEGAL_PARAMETER.getCode(), "用户名或密码错误");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            log.warn("[营销登录] 密码错误 username={}", username);
            throw new AppException(ResponseEnum.ILLEGAL_PARAMETER.getCode(), "用户名或密码错误");
        }

        log.info("[营销登录] 登录成功 username={} role={}", username, user.getRole());
        return jwtUtil.generateToken(user.getUsername(), user.getRole());
    }

    // ---- private ----

    private void ensureAdmin() {
        MarketingUserPo admin = marketingUserDao.findByUsername("admin");
        String correctHash = passwordEncoder.encode("admin123");

        if (admin == null) {
            marketingUserDao.insert(MarketingUserPo.builder()
                                                   .username("admin").passwordHash(correctHash).role("ADMIN").build());
            log.info("[营销认证] 已创建管理员 admin / admin123");
        } else if (!passwordEncoder.matches("admin123", admin.getPasswordHash())) {
            marketingUserDao.updatePassword("admin", correctHash);
            log.info("[营销认证] 管理员密码已修复为 admin123");
        } else {
            log.info("[营销认证] 管理员账号正常");
        }
        adminReady = true;
    }

}
