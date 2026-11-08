-- KEYS[1] 限流 key
-- ARGV[1] 每秒允许的最大请求数
-- 返回 1 允许，0 拒绝

local key = KEYS[1]
local limit = tonumber(ARGV[1])

local count = redis.call('incr', key)
if count == 1 then
    redis.call('expire', key, 1)
end
if count > limit then
    return 0
end
return 1
