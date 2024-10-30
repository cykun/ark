package ink.xikun.ark.registry.loadbalance;

import ink.xikun.ark.common.RpcRequest;

import java.util.List;

public class RandomLoadBalancer extends AbstractLoadBalance {

    @Override
    protected <T> T doSelect(List<T> serviceMetaList, RpcRequest request) {
        return serviceMetaList.get((int) (Math.random() * serviceMetaList.size()));
    }
}
