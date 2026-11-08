package com.tandian.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tandian.dto.LoginFormDTO;
import com.tandian.dto.Result;
import com.tandian.entity.User;
import com.tandian.mapper.UserMapper;
import com.tandian.service.IUserService;
import com.tandian.utils.RedisConstants;
import com.tandian.utils.RegexUtils;
import com.tandian.utils.UserHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.tandian.utils.RedisConstants.LOGIN_CODE_KEY;
import static com.tandian.utils.RedisConstants.LOGIN_CODE_LIMIT_KEY;
import static com.tandian.utils.RedisConstants.LOGIN_USER_KEY;
import static com.tandian.utils.SystemConstants.USER_NICK_NAME_PREFIX;

/**
 * 用户服务：验证码登录、登出、每日签到。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private static final DateTimeFormatter SIGN_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public Result sendCode(String phone) {
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号格式错误");
        }

        // 同一手机号 60 秒内只允许发送一次，防止短信接口被刷
        String limitKey = LOGIN_CODE_LIMIT_KEY + phone;
        Boolean allowed = stringRedisTemplate.opsForValue()
                .setIfAbsent(limitKey, "1", RedisConstants.LOGIN_CODE_LIMIT_SECONDS, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(allowed)) {
            return Result.fail("验证码发送过于频繁，请稍后再试");
        }

        String code = RandomUtil.randomNumbers(6);
        stringRedisTemplate.opsForValue()
                .set(LOGIN_CODE_KEY + phone, code, RedisConstants.LOGIN_CODE_TTL_MINUTES, TimeUnit.MINUTES);

        // 本地开发没有接入短信通道，验证码打印在日志里；生产环境替换为短信服务实现
        log.info("登录验证码已生成，phone={}, code={}", phone, code);
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm) {
        if (loginForm == null) {
            return Result.fail("登录参数不能为空");
        }
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号格式错误");
        }
        String code = loginForm.getCode();
        if (RegexUtils.isCodeInvalid(code)) {
            return Result.fail("验证码格式错误");
        }

        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        if (cacheCode == null || !cacheCode.equals(code)) {
            return Result.fail("验证码错误或已过期");
        }

        User user = query().eq("phone", phone).one();
        if (user == null) {
            user = createUserWithPhone(phone);
        }

        String token = UUID.randomUUID().toString().replace("-", "");
        Map<String, Object> userMap = new HashMap<>(4);
        userMap.put("id", user.getId().toString());
        userMap.put("nickName", user.getNickName());
        userMap.put("icon", user.getIcon() == null ? "" : user.getIcon());

        stringRedisTemplate.opsForHash().putAll(LOGIN_USER_KEY + token, userMap);
        stringRedisTemplate.expire(LOGIN_USER_KEY + token, RedisConstants.LOGIN_USER_TTL_MINUTES, TimeUnit.MINUTES);

        // 验证码一次性使用
        stringRedisTemplate.delete(LOGIN_CODE_KEY + phone);
        return Result.ok(token);
    }

    @Override
    public Result logout(String token) {
        if (token != null) {
            stringRedisTemplate.delete(LOGIN_USER_KEY + token);
        }
        UserHolder.removeUser();
        return Result.ok();
    }

    @Override
    public Result sign() {
        Long userId = currentUserId();
        LocalDateTime now = LocalDateTime.now();
        String key = RedisConstants.USER_SIGN_KEY + userId + ":" + now.format(SIGN_MONTH_FORMATTER);
        stringRedisTemplate.opsForValue().setBit(key, now.getDayOfMonth() - 1, true);
        return Result.ok();
    }

    @Override
    public Result signCount() {
        Long userId = currentUserId();
        LocalDateTime now = LocalDateTime.now();
        String key = RedisConstants.USER_SIGN_KEY + userId + ":" + now.format(SIGN_MONTH_FORMATTER);

        List<Long> result = stringRedisTemplate.opsForValue().bitField(
                key,
                BitFieldSubCommands.create()
                        .get(BitFieldSubCommands.BitFieldType.unsigned(now.getDayOfMonth()))
                        .valueAt(0)
        );
        if (result == null || result.isEmpty() || result.get(0) == null) {
            return Result.ok(0);
        }

        int count = 0;
        long num = result.get(0);
        while ((num & 1) == 1) {
            count++;
            num >>>= 1;
        }
        return Result.ok(count);
    }

    private Long currentUserId() {
        if (UserHolder.getUser() == null) {
            throw new IllegalStateException("用户未登录");
        }
        return UserHolder.getUser().getId();
    }

    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone)
                .setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomString(10))
                .setIcon("");
        save(user);
        return user;
    }
}
