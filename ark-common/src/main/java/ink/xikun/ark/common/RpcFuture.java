package ink.xikun.ark.common;

import io.netty.util.concurrent.Promise;

public record RpcFuture<T>(Promise<T> promise, long timeout) {

}
