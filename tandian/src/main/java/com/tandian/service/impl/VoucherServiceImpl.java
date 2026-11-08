package com.tandian.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tandian.dto.Result;
import com.tandian.entity.SeckillVoucher;
import com.tandian.entity.Voucher;
import com.tandian.mapper.VoucherMapper;
import com.tandian.service.ISeckillVoucherService;
import com.tandian.service.IVoucherService;
import com.tandian.utils.RedisConstants;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements IVoucherService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryVoucherOfShop(Long shopId) {
        List<Voucher> vouchers = getBaseMapper().queryVoucherOfShop(shopId);
        return Result.ok(vouchers);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addSeckillVoucher(Voucher voucher) {
        if (voucher == null) {
            throw new IllegalArgumentException("优惠券信息不能为空");
        }
        if (voucher.getStock() == null || voucher.getStock() < 0) {
            throw new IllegalArgumentException("秒杀库存不能为空且不能小于 0");
        }
        if (voucher.getBeginTime() == null || voucher.getEndTime() == null) {
            throw new IllegalArgumentException("秒杀开始/结束时间不能为空");
        }
        if (!voucher.getBeginTime().isBefore(voucher.getEndTime())) {
            throw new IllegalArgumentException("秒杀开始时间必须早于结束时间");
        }

        save(voucher);

        SeckillVoucher seckillVoucher = new SeckillVoucher();
        seckillVoucher.setVoucherId(voucher.getId());
        seckillVoucher.setStock(voucher.getStock());
        seckillVoucher.setBeginTime(voucher.getBeginTime());
        seckillVoucher.setEndTime(voucher.getEndTime());
        seckillVoucherService.save(seckillVoucher);

        // 新增秒杀券后立即同步 Redis 库存，否则 Lua 脚本会读到 0 而提示库存不足
        stringRedisTemplate.opsForValue().set(
                RedisConstants.SECKILL_STOCK_KEY + voucher.getId(),
                voucher.getStock().toString());
    }
}
