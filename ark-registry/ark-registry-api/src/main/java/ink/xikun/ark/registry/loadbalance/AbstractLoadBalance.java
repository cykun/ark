package ink.xikun.ark.registry.loadbalance;

import ink.xikun.ark.common.RpcRequest;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

public abstract class AbstractLoadBalance implements LoadBalance {

    protected abstract <T> T doSelect(List<T> serviceMetaList, RpcRequest request);

    @Override
    public <T> T select(List<T> serviceMetaList, RpcRequest request) {
        if (CollectionUtils.isEmpty(serviceMetaList)) {
            throw new IllegalArgumentException("error");
        }
        return doSelect(serviceMetaList, request);
    }
}
