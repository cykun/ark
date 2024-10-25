package ink.xikun.ark.registry.loadbalancer;

import java.util.List;

public interface ServiceLoadBalancer<T> {

    T select(List<T> serviceMetaList, int hashCode);
}
