package org.chris.demo.redis.lock;

import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProductStore {

    private final int productCounts;

    private final RedisTemplate<String, String> redisTemplate;

    private static final String PRODUCT_COUNTS = "product_counts";
    private static final String LUCKY_USERS = "lucky_users";

    public ProductStore(RedisTemplate<String, String> redisTemplate, int productCounts) {
        this.redisTemplate = redisTemplate;
        this.productCounts = productCounts;
    }

    @PostConstruct
    public void init() {
        this.redisTemplate.delete(LUCKY_USERS);
        log.info("清空成用户列表");
        this.redisTemplate.opsForValue().set(PRODUCT_COUNTS, String.valueOf(productCounts));
        log.info("初始化{}个商品", productCounts);
    }

    public boolean purchase(String userId) {

        return redisTemplate.execute(new SessionCallback<Boolean>() {
            public Boolean execute(@SuppressWarnings("rawtypes") RedisOperations operations)
                throws DataAccessException {

                StringRedisTemplate template = StringRedisTemplate.class.cast(operations);

                while (true) {
                    try {
                        template.watch(PRODUCT_COUNTS);
                        int inventory = Integer.valueOf(template.opsForValue().get(PRODUCT_COUNTS));
                        if (inventory == 0) {
                            // 库存已经为0，此用户秒杀失败
                            log.info("库存已经为0，用户[{}]秒杀失败 :(", userId);
                            return false;
                        }
                        template.multi();
                        template.opsForValue().set(PRODUCT_COUNTS, String.valueOf(inventory - 1));
                        template.opsForSet().add(LUCKY_USERS, userId);

                        if (!template.exec().isEmpty()) {
                            log.info("用户[{}]秒杀成功 yeah!!!", userId);
                            return true;
                        }

                    } catch (Exception e) {
                        template.discard();
                        return false;
                    }
                }

            }
        });

    }

    public Set<String> getPurchasedUsers() {
        return this.redisTemplate.opsForSet().members(LUCKY_USERS);
    }

}
