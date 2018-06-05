package org.chris.demo.redis.lock;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientRequest {

    private final ProductStore store;

    private ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);

    public ClientRequest(ProductStore store) {
        this.store = store;
    }

    // 模拟秒杀请求
    public void execute() {

        // 产生100个请求，用10个线程响应这100个请求去秒杀商品
        IntStream.range(0, 100).forEach((i) -> {
            String userId = String.valueOf(i + 1);
            pool.execute(() -> store.purchase(userId));
        });

        pool.shutdown();

        while (!pool.isTerminated()) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {

            }
        }

        Set<String> users = store.getPurchasedUsers();

        log.info("{}个用户秒杀到了商品", users.size());
    }
}
