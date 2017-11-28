package org.batfish.datamodel;

import java.util.HashMap;
import java.util.Map;
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

    protected String generateName() {
      return _networkFactory.generateName(_outputClass);
    }

    long generateLong() {
      return _networkFactory.generateLong(_outputClass);
    }
  }

  private Map<Class<?>, Integer> _generatedNameCounters;

  private Map<Class<?>, Long> _generatedLongCounters;

  public NetworkFactory() {
    _generatedNameCounters = new HashMap<>();
    _generatedLongCounters = new HashMap<>();
  }

  public Configuration.Builder configurationBuilder() {
    return new Configuration.Builder(this);
  }

  private String generateName(Class<?> forClass) {
    String className = forClass.getSimpleName();
    return "~"
        + className
        + "_"
        + _generatedNameCounters.compute(forClass, (n, v) -> v == null ? 0 : v + 1)
        + "~";
  }

  private long generateLong(Class<?> forClass) {
    return _generatedLongCounters.compute(forClass, (n, v) -> v == null ? 0L : v + 1L);
  }

  public Interface.Builder interfaceBuilder() {
    return new Interface.Builder(this);
  }

  public OspfProcess.Builder ospfProcessBuilder() {
    return new OspfProcess.Builder(this);
  }

  public Vrf.Builder vrfBuilder() {
    return new Vrf.Builder(this);
  }

  public OspfArea.Builder ospfAreaBuilder() {
    return new OspfArea.Builder(this);
  }

  public RoutingPolicy.Builder routingPolicyBuilder() {
    return new RoutingPolicy.Builder(this);
  }
}
