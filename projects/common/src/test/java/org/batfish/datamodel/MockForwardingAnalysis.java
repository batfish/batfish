package org.batfish.datamodel;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.Nonnull;

public class MockForwardingAnalysis implements ForwardingAnalysis {
  private final Map<String, Map<String, VrfForwardingBehavior>> _vrfForwardingBehavior;
  private final Map<String, Map<String, IpSpace>> _arpReplies;

  private MockForwardingAnalysis(
      Map<String, Map<String, IpSpace>> arpReplies,
      Map<String, Map<String, VrfForwardingBehavior>> vrfForwardingBehavior) {
    _arpReplies = ImmutableMap.copyOf(arpReplies);
    _vrfForwardingBehavior = ImmutableMap.copyOf(vrfForwardingBehavior);
  }

  @Override
  public Map<String, Map<String, IpSpace>> getArpReplies() {
    return _arpReplies;
  }

  @Override
  public @Nonnull Map<String, Map<String, VrfForwardingBehavior>> getVrfForwardingBehavior() {
    return _vrfForwardingBehavior;
  }

  /** Helper when we only need VrfForwardinBehavior at a single vrf */
  public static MockForwardingAnalysis withVrfForwardingBehavior(
      String node, String vrf, VrfForwardingBehavior vrfForwardingBehavior) {
    return MockForwardingAnalysis.builder()
        .setVrfForwardingBehavior(
            ImmutableMap.of(node, ImmutableMap.of(vrf, vrfForwardingBehavior)))
        .build();
  }

  /** Helper when we only need accepted IPs at a single interface */
  public static MockForwardingAnalysis withAcceptedIps(
      String node, String vrf, String iface, IpSpace ips) {
    return withInterfaceForwardingBehavior(
        node, vrf, iface, InterfaceForwardingBehavior.withAcceptedIps(ips));
  }

  /** Helper when we only need accepted IPs at a single interface */
  public static MockForwardingAnalysis withDeliveredToSubnetIps(
      String node, String vrf, String iface, IpSpace ips) {
    return withInterfaceForwardingBehavior(
        node, vrf, iface, InterfaceForwardingBehavior.withDeliveredToSubnet(ips));
  }

  /** Helper when we only {@link InterfaceForwardingBehavior} for a single interface. */
  public static MockForwardingAnalysis withInterfaceForwardingBehavior(
      String node, String vrf, String iface, InterfaceForwardingBehavior ifb) {
    ImmutableMap<String, Map<String, VrfForwardingBehavior>> vrfForwardingBehavior =
        ImmutableMap.of(
            node,
            ImmutableMap.of(
                vrf,
                VrfForwardingBehavior.withInterfaceForwardingBehavior(
                    ImmutableMap.of(iface, ifb))));
    return MockForwardingAnalysis.builder().setVrfForwardingBehavior(vrfForwardingBehavior).build();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private Map<String, Map<String, VrfForwardingBehavior>> _vrfForwardingBehavior =
        ImmutableMap.of();
    private Map<String, Map<String, IpSpace>> _arpReplies = ImmutableMap.of();

    public Builder setVrfForwardingBehavior(
        Map<String, Map<String, VrfForwardingBehavior>> vrfForwardingBehavior) {
      _vrfForwardingBehavior = vrfForwardingBehavior;
      return this;
    }

    public Builder setArpReplies(Map<String, Map<String, IpSpace>> arpReplies) {
      _arpReplies = arpReplies;
      return this;
    }

    public MockForwardingAnalysis build() {
      return new MockForwardingAnalysis(_arpReplies, _vrfForwardingBehavior);
    }
  }
}
