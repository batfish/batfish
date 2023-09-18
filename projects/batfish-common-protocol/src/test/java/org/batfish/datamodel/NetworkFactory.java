package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.VisibleForTesting;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
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

    private final @Nullable NetworkFactory _networkFactory;

    private final Class<T> _outputClass;

    protected NetworkFactoryBuilder(@Nullable NetworkFactory networkFactory, Class<T> outputClass) {
      _networkFactory = networkFactory;
      _outputClass = outputClass;
    }

    public abstract T build();

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
    return IpAccessList.builder(() -> generateName(IpAccessList.class));
  }

  public BgpActivePeerConfig.Builder bgpNeighborBuilder() {
    return BgpActivePeerConfig.builder()
        .setIpv4UnicastAddressFamily(Ipv4UnicastAddressFamily.builder().build());
  }

  public BgpPassivePeerConfig.Builder bgpDynamicNeighborBuilder() {
    return BgpPassivePeerConfig.builder()
        .setIpv4UnicastAddressFamily(Ipv4UnicastAddressFamily.builder().build());
  }

  public BgpProcess.Builder bgpProcessBuilder() {
    return BgpProcess.builder();
  }

  public Configuration.Builder configurationBuilder() {
    return Configuration.builder(() -> generateName(Configuration.class));
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

  /** Generates an interface with a generated name and {@link InterfaceType#PHYSICAL} type. */
  public Interface.Builder interfaceBuilder() {
    return Interface.builder(() -> generateName(Interface.class)).setType(InterfaceType.PHYSICAL);
  }

  public OspfArea.Builder ospfAreaBuilder() {
    return OspfArea.builder(() -> generateLong(OspfArea.class));
  }

  /** Return an OSPF builder. Pre-defines required fields (e.g., reference bandwidth) */
  public OspfProcess.Builder ospfProcessBuilder() {
    return OspfProcess.builder(() -> generateName(OspfProcess.class)).setReferenceBandwidth(1e8);
  }

  public RipProcess.Builder ripProcessBuilder() {
    return RipProcess.builder();
  }

  @VisibleForTesting
  public RoutingPolicy.Builder routingPolicyBuilder() {
    return RoutingPolicy.builder(() -> generateName(RoutingPolicy.class));
  }

  public Vrf.Builder vrfBuilder() {
    return Vrf.builder(() -> generateName(Vrf.class));
  }
}
