package cn.pumluda.types.utils.juc;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * Project: group-buy-market-better <p>
 * File: CompletableFutureUtils <p>
 * Created by: 16374 <p>
 * Date: 2026/5/27 <p>
 * Time: 17:33 <p>
 * Description: 异步执行编排工具类
 */
@Slf4j
public class CompletableFutureUtils {
    private final ExecutorService executor;

    public CompletableFutureUtils(ExecutorService executor) {
        this.executor = executor;
    }

    /**
     * 并行执行多个无返回值的任务
     * 任意任务失败则立马取消其他任务并抛出异常
     *
     * @param tasks 无返回值的异步执行任务
     */
    public void runParallel(Runnable... tasks) {
        if (tasks == null || tasks.length == 0) return;

        CompletableFuture<?>[] futures = new CompletableFuture[tasks.length];
        for (int i = 0; i < tasks.length; i++) {
            futures[i] = CompletableFuture.runAsync(tasks[i], executor);
        }
        awaitAllAndThrowIfAnyError(futures);
    }

    /**
     * 并行执行多个有返回值的任务
     * 任意任务失败则立马取消其他任务并抛出异常
     *
     * @param suppliers 带返回值的异步执行任务
     * @param <T>       结果类型
     * @return 异步执行结果列表
     */
    public <T> List<T> supplyParallel(Supplier<T>... suppliers) {
        if (suppliers == null || suppliers.length == 0) return List.of();

        CompletableFuture<T>[] futures = new CompletableFuture[suppliers.length];
        for (int i = 0; i < futures.length; i++) {
            futures[i] = CompletableFuture.supplyAsync(suppliers[i], executor);
        }
        awaitAllAndThrowIfAnyError(futures);
        return collectResults(futures);
    }

    /**
     * 带超时的并行执行多个有返回值的任务
     *
     * @param timeout   超时时间
     * @param unit      时间单位
     * @param suppliers 带返回值的异步执行任务
     * @param <T>       结果类型
     * @return 异步执行结果列表
     * @throws TimeoutException 超时异常
     */
    public <T> List<T> supplyParallelWithTimeout(long timeout, TimeUnit unit, Supplier<T>... suppliers) throws TimeoutException {
        if (suppliers == null || suppliers.length == 0) return List.of();

        CompletableFuture<T>[] futures = new CompletableFuture[suppliers.length];
        for (int i = 0; i < suppliers.length; i++) {
            futures[i] = CompletableFuture.supplyAsync(suppliers[i], executor);
        }
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures);
        try {
            allOf.get(timeout, unit);
        } catch (TimeoutException e) {
            cancelAll(futures);
            throw e;
        } catch (InterruptedException | ExecutionException e) {
            cancelAll(futures);
            throw new RuntimeException(e.getCause() != null ? e.getCause() : e);
        }
        return collectResults(futures);
    }

    /* 并发兜底处理机制 */
    private void awaitAllAndThrowIfAnyError(CompletableFuture<?>[] futures) {
        try {
            CompletableFuture.allOf(futures).join();
        } catch (CompletionException e) {
            cancelAll(futures);

            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new RuntimeException(cause);
        }
    }

    private void cancelAll(CompletableFuture<?>[] futures) {
        for (CompletableFuture<?> f : futures) {
            if (f != null) {
                f.cancel(true);
            }
        }
    }

    /* 任务结果收集 */
    private <T> List<T> collectResults(CompletableFuture<T>[] futures) {
        List<T> results = new ArrayList<>(futures.length);
        for (CompletableFuture<T> f : futures) {
            results.add(f.join());
        }
        return results;
    }
}
