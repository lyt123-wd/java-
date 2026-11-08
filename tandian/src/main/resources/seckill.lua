-- KEYS: 无
-- ARGV[1] voucherId
-- ARGV[2] userId
-- ARGV[3] orderId
-- ARGV[4] streamKey
-- ARGV[5] streamMaxLen

local voucherId = ARGV[1]
local userId = ARGV[2]
local orderId = ARGV[3]
local streamKey = ARGV[4]
local streamMaxLen = tonumber(ARGV[5])

local stockKey = 'seckill:stock:' .. voucherId
local orderKey = 'seckill:order:' .. voucherId

-- 1. 库存校验
local stock = tonumber(redis.call('get', stockKey) or '0')
if stock <= 0 then
    return 1
end

-- 2. 一人一单校验
if redis.call('sismember', orderKey, userId) == 1 then
    return 2
end

-- 3. 扣减库存并标记用户
redis.call('incrby', stockKey, -1)
redis.call('sadd', orderKey, userId)

-- 4. 发送异步下单消息，限制 Stream 长度，防止无限增长
redis.call('xadd', streamKey, 'MAXLEN', '~', streamMaxLen, '*',
        'userId', userId, 'voucherId', voucherId, 'id', orderId)

return 0
