package com.tandian.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tandian.dto.Result;
import com.tandian.dto.UserDTO;
import com.tandian.entity.Follow;
import com.tandian.mapper.FollowMapper;
import com.tandian.service.IFollowService;
import com.tandian.utils.RedisConstants;
import com.tandian.utils.UserHolder;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 关注关系：数据库持久化 + Redis Set 维护关注/粉丝关系，用于共同关注计算。
 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private UserServiceImpl userService;

    @Override
    public Result follow(Long followUserId, Boolean isFollow) {
        UserDTO current = UserHolder.getUser();
        if (current == null) {
            return Result.fail("请先登录");
        }
        Long userId = current.getId();
        if (userId.equals(followUserId)) {
            return Result.fail("不能关注自己");
        }
        String key = RedisConstants.FOLLOW_KEY + userId;

        if (Boolean.TRUE.equals(isFollow)) {
            Long count = query().eq("user_id", userId).eq("follow_user_id", followUserId).count();
            if (count == 0) {
                Follow follow = new Follow();
                follow.setUserId(userId);
                follow.setFollowUserId(followUserId);
                if (save(follow)) {
                    stringRedisTemplate.opsForSet().add(key, followUserId.toString());
                }
            } else {
                // 数据已经存在时补齐 Redis，保证缓存与数据库一致
                stringRedisTemplate.opsForSet().add(key, followUserId.toString());
            }
        } else {
            boolean removed = remove(new QueryWrapper<Follow>()
                    .eq("user_id", userId)
                    .eq("follow_user_id", followUserId));
            if (removed) {
                stringRedisTemplate.opsForSet().remove(key, followUserId.toString());
            }
        }
        return Result.ok();
    }

    @Override
    public Result isFollow(Long followUserId) {
        UserDTO current = UserHolder.getUser();
        if (current == null) {
            return Result.fail("请先登录");
        }
        Long count = query()
                .eq("follow_user_id", followUserId)
                .eq("user_id", current.getId())
                .count();
        return Result.ok(count > 0);
    }

    @Override
    public Result commonFollow(Long id) {
        UserDTO current = UserHolder.getUser();
        if (current == null) {
            return Result.fail("请先登录");
        }
        Set<String> intersect = stringRedisTemplate.opsForSet().intersect(
                RedisConstants.FOLLOW_KEY + current.getId(),
                RedisConstants.FOLLOW_KEY + id);
        if (intersect == null || intersect.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }
        List<Long> ids = intersect.stream().map(Long::valueOf).collect(Collectors.toList());
        List<UserDTO> users = userService.listByIds(ids)
                .stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .collect(Collectors.toList());
        return Result.ok(users);
    }
}
