package ink.xikun.ark.registry.loadbalance;

import ink.xikun.ark.common.RpcRequest;

import java.util.List;

public interface LoadBalance {

    <T> T select(List<T> serviceMetaList, RpcRequest request);
}
