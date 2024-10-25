package ink.xikun.ark.handler;

import java.util.concurrent.ThreadPoolExecutor;

public final class RpcRequestProcessor {

    private static final class ThreadPoolExecutorHolder {
        private static final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(16, 16, 600L,
                java.util.concurrent.TimeUnit.SECONDS, new java.util.concurrent.LinkedBlockingQueue<>());
    }

    public static void submitRequest(Runnable task) {
        ThreadPoolExecutorHolder.threadPoolExecutor.submit(task);
    }
}
