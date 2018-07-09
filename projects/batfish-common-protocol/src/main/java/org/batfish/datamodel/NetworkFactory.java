package org.batfish.datamodel;

import java.util.HashMap;
import java.util.Map;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.ospf.OspfArea;
import org.batfish.datamodel.ospf.OspfProcess;
import org.batfish.datamodel.routing_policy.RoutingPolicy;

public class NetworkFactory {

  public abstract static class NetworkFactoryBuilder<T> {

    private final NetworkFactory _networkFactory;

    private final Class<T> _outputClass;

    protected NetworkFactoryBuilder(NetworkFactory networkFactory, Class<T> outputClass) {
      _networkFactory = networkFactory;
      _outputClass = outputClass;
    }

    public abstract T build();

    private void checkNetworkFactory(String toGenerate) {
      if (_networkFactory == null) {
        throw new BatfishException(
            String.format(
                "Cannot generate %s for %s not created via %s",
                toGenerate, getClass().getCanonicalName(), NetworkFactory.class.getSimpleName()));
      }
    }

    protected long generateLong() {
      checkNetworkFactory("long");
      return _networkFactory.generateLong(_outputClass);
    }

    protected String generateName() {
      checkNetworkFactory("name");
      return _networkFactory.generateName(_outputClass);
    }
  }

  private Map<Class<?>, Long> _generatedLongCounters;

  private Map<Class<?>, Integer> _generatedNameCounters;

  public NetworkFactory() {
    _generatedNameCounters = new HashMap<>();
    _generatedLongCounters = new HashMap<>();
  }

  public IpAccessList.Builder aclBuilder() {
    return new IpAccessList.Builder(this);
  }

  public BgpActivePeerConfig.Builder bgpNeighborBuilder() {
    return BgpActivePeerConfig.builder();
  }

  public BgpPassivePeerConfig.Builder bgpDynamicNeighborBuilder() {
    return BgpPassivePeerConfig.builder();
  }

  public BgpProcess.Builder bgpProcessBuilder() {
    return new BgpProcess.Builder(this);
  }

  public Configuration.Builder configurationBuilder() {
    return new Configuration.Builder(this);
  }

  private long generateLong(Class<?> forClass) {
    return _generatedLongCounters.compute(forClass, (n, v) -> v == null ? 0L : v + 1L);
  }

  private String generateName(Class<?> forClass) {
    String className = forClass.getSimpleName();
    return "~"
        + className
        + "_"
        + _generatedNameCounters.compute(forClass, (n, v) -> v == null ? 0 : v + 1)
        + "~";
  }

  public Interface.Builder interfaceBuilder() {
    return new Interface.Builder(this);
  }

  public OspfArea.Builder ospfAreaBuilder() {
    return OspfArea.builder(this);
  }

  public OspfProcess.Builder ospfProcessBuilder() {
    return OspfProcess.builder(this);
  }

  public RipProcess.Builder ripProcessBuilder() {
    return new RipProcess.Builder(this);
  }

  public RoutingPolicy.Builder routingPolicyBuilder() {
    return new RoutingPolicy.Builder(this);
  }

  public Vrf.Builder vrfBuilder() {
    return new Vrf.Builder(this);
  }
}
