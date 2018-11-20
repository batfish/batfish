package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkState;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.batfish.datamodel.ospf.OspfArea;
import org.batfish.datamodel.ospf.OspfProcess;
import org.batfish.datamodel.routing_policy.RoutingPolicy;

/**
 * Network Factory -- helpful in creating test networks, as it allows child builders to
 * automatically generate names/ID for objects they create.
 *
 * <p>Some builders pre-populate required fields with a default (usually based on Cisco IOS, if
 * vendor-dependent).
 */
public class NetworkFactory {

  /** Base class for all network factory builders */
  public abstract static class NetworkFactoryBuilder<T> {

    @Nullable private final NetworkFactory _networkFactory;

    private final Class<T> _outputClass;

    protected NetworkFactoryBuilder(@Nullable NetworkFactory networkFactory, Class<T> outputClass) {
      _networkFactory = networkFactory;
      _outputClass = outputClass;
    }

    public abstract T build();

    protected long generateLong() {
      checkState(_networkFactory != null, "Cannot generate a long value without a network factory");
      return _networkFactory.generateLong(_outputClass);
    }

    protected String generateName() {
      checkState(_networkFactory != null, "Cannot generate a name without a network factory");
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

  /** Return an OSPF builder. Pre-defines required fields (e.g., reference bandwidth) */
  public OspfProcess.Builder ospfProcessBuilder() {
    return OspfProcess.builder(this).setReferenceBandwidth(1e8);
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
