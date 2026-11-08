package com.yujian.lease.web.admin.service.impl;

import com.yujian.lease.common.constant.RedisConstant;
import com.yujian.lease.common.exception.LeaseException;
import com.yujian.lease.common.result.ResultCodeEnum;
import com.yujian.lease.common.utils.JwtUtil;
import com.yujian.lease.model.entity.SystemUser;
import com.yujian.lease.model.enums.BaseStatus;
import com.yujian.lease.web.admin.mapper.SystemUserMapper;
import com.yujian.lease.web.admin.service.LoginService;
import com.yujian.lease.web.admin.vo.login.CaptchaVo;
import com.yujian.lease.web.admin.vo.login.LoginVo;
import com.yujian.lease.web.admin.vo.system.user.SystemUserInfoVo;
import com.wf.captcha.SpecCaptcha;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class LoginServiceImpl implements LoginService {

    private static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private SystemUserMapper systemUserMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public CaptchaVo getCaptcha() {

        SpecCaptcha specCaptcha = new SpecCaptcha(130, 48, 4);
        String key = RedisConstant.ADMIN_LOGIN_PREFIX + UUID.randomUUID();
        String value = specCaptcha.text().toLowerCase();
        stringRedisTemplate.opsForValue().set(key, value, RedisConstant.ADMIN_LOGIN_CAPTCHA_TTL_SEC, TimeUnit.SECONDS);
        return new CaptchaVo(specCaptcha.toBase64(), key);
    }

    @Override
    public String login(LoginVo loginVo) {

        if (!StringUtils.hasLength(loginVo.getCaptchaCode())) {
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_NOT_FOUND);
        }

        String code = stringRedisTemplate.opsForValue().get(loginVo.getCaptchaKey());
        if (!StringUtils.hasLength(code)) {
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_EXPIRED);
        }

        if (!code.equals(loginVo.getCaptchaCode().toLowerCase())) {
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_ERROR);
        }

        SystemUser systemUser = systemUserMapper.selectOneByUsername(loginVo.getUsername());

        if (systemUser == null) {
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_NOT_EXIST_ERROR);
        }

        if (systemUser.getStatus() == BaseStatus.DISABLE) {
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_DISABLED_ERROR);
        }

        String failKey = RedisConstant.ADMIN_LOGIN_FAIL_PREFIX + loginVo.getUsername();
        String failCountStr = stringRedisTemplate.opsForValue().get(failKey);
        if (failCountStr != null && Integer.parseInt(failCountStr) >= RedisConstant.ADMIN_LOGIN_MAX_FAIL) {
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_LOCKED);
        }

        String storedPassword = systemUser.getPassword();
        boolean passwordMatches;
        if (storedPassword != null && storedPassword.startsWith("$2")) {
            // 新密码使用 BCrypt
            passwordMatches = PASSWORD_ENCODER.matches(loginVo.getPassword(), storedPassword);
        } else {
            // 兼容历史 MD5 密码，校验成功后自动升级为 BCrypt
            passwordMatches = storedPassword != null
                    && storedPassword.equalsIgnoreCase(DigestUtils.md5Hex(loginVo.getPassword()));
            if (passwordMatches) {
                systemUser.setPassword(PASSWORD_ENCODER.encode(loginVo.getPassword()));
                systemUserMapper.updateById(systemUser);
            }
        }
        if (!passwordMatches) {
            Long failCount = stringRedisTemplate.opsForValue().increment(failKey);
            if (failCount != null && failCount == 1L) {
                stringRedisTemplate.expire(failKey, RedisConstant.ADMIN_LOGIN_LOCK_SEC, TimeUnit.SECONDS);
            }
            if (failCount != null && failCount >= RedisConstant.ADMIN_LOGIN_MAX_FAIL) {
                throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_LOCKED);
            }
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_ERROR);
        }

        stringRedisTemplate.delete(failKey);
        return jwtUtil.createToken(systemUser.getId(), systemUser.getUsername());
    }

    @Override
    public SystemUserInfoVo getUserInfoById(Long userId) {

        SystemUser systemUser = systemUserMapper.selectById(userId);
        SystemUserInfoVo systemUserInfoVo = new SystemUserInfoVo();
        systemUserInfoVo.setName(systemUser.getName());
        systemUserInfoVo.setAvatarUrl(systemUser.getAvatarUrl());
        return systemUserInfoVo;
    }
}
