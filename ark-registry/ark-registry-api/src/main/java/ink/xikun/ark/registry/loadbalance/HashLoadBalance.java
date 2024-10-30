package ink.xikun.ark.registry.loadbalance;

import ink.xikun.ark.common.RpcRequest;

import java.util.List;

public class HashLoadBalance extends AbstractLoadBalance {

    @Override
    protected <T> T doSelect(List<T> serviceMetaList, RpcRequest request) {
        return serviceMetaList.get(request.hashCode());
    }
}
