package org.batfish.engine;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A device's configured facts in neutral form: what the device is configured to do, in the shape of
 * Batfish's model, without Batfish's generated internals. This is what NOCFlow stores in the twin's
 * configured layer; the compiled model stays inside the engine.
 */
public record DeviceFacts(
    @Nonnull String node,
    @Nonnull String format,
    @Nonnull List<InterfaceFacts> interfaces,
    @Nonnull List<VrfFacts> vrfs,
    @Nonnull List<AclFacts> acls,
    @Nonnull List<PolicyFacts> policies,
    @Nonnull List<SetFacts> sets,
    @JsonInclude(JsonInclude.Include.ALWAYS) @Nonnull List<VniFacts> vnis) {

  /**
   * Configured VXLAN settings. Source VRF is the underlay; vrf is the model container (tenant domain
   * for L3).
   */
  public record VniFacts(
      int vni,
      @Nonnull String layer,
      @Nonnull String vrf,
      @Nonnull String sourceVrf,
      @Nullable String sourceAddress,
      int udpPort,
      @Nullable Integer vlan,
      @Nullable String bumTransportMethod,
      @Nonnull List<String> bumTransportIps) {}

  public record InterfaceFacts(
      @Nonnull String name,
      @Nonnull String type,
      @Nonnull String vrf,
      boolean adminUp,
      boolean active,
      @Nonnull List<String> addresses,
      int mtu,
      @Nullable Double bandwidth,
      @Nonnull String switchportMode,
      @Nullable Integer accessVlan,
      @Nullable String allowedVlans,
      @Nullable Integer nativeVlan,
      @Nullable Integer vlan,
      @Nullable String channelGroup,
      @Nullable OspfInterfaceFacts ospf,
      @Nullable String incomingFilter,
      @Nullable String outgoingFilter,
      boolean nat) {}

  public record OspfInterfaceFacts(
      @Nullable String process,
      @Nullable Long area,
      @Nullable Integer cost,
      @Nullable String networkType,
      boolean passive,
      int helloInterval,
      int deadInterval) {}

  public record VrfFacts(
      @Nonnull String name,
      @Nonnull List<StaticRouteFacts> staticRoutes,
      @Nullable BgpFacts bgp,
      @Nonnull List<OspfFacts> ospf) {}

  public record StaticRouteFacts(
      @Nonnull String prefix,
      @Nullable String nextHopIp,
      @Nullable String nextHopInterface,
      long adminDistance,
      long tag) {}

  public record BgpFacts(@Nonnull String routerId, @Nonnull List<BgpPeerFacts> peers) {}

  /**
   * One configured BGP peer: an address, a passive prefix, or an interface (unnumbered). The
   * policies are the IPv4 unicast ones as Batfish names them; names in {@code ~...~} are Batfish's
   * generated wrappers around the device's own policies, which the policy view lists under {@code
   * usedBy}.
   */
  public record BgpPeerFacts(
      @Nonnull String peer,
      @Nonnull String kind,
      @Nullable Long localAs,
      @Nonnull String remoteAsns,
      @Nullable String localIp,
      @Nonnull List<String> families,
      @Nullable String importPolicy,
      @Nullable String exportPolicy,
      @Nullable String description) {}

  public record OspfFacts(
      @Nonnull String processId,
      @Nonnull String routerId,
      double referenceBandwidth,
      @Nonnull List<OspfAreaFacts> areas) {}

  public record OspfAreaFacts(long area, @Nonnull String type, @Nonnull List<String> interfaces) {}

  public record AclFacts(@Nonnull String name, @Nonnull List<AclLineFacts> lines) {}

  public record AclLineFacts(@Nullable String text, @Nullable String action) {}

  /**
   * A routing policy as the customer wrote it: the device's own lines, split into clauses, with the
   * sets each clause references. Readable and diffable; never computed from.
   */
  public record PolicyFacts(
      @Nonnull String type,
      @Nonnull String name,
      @Nonnull List<String> lines,
      @Nonnull List<ClauseFacts> clauses,
      @Nonnull List<String> usedBy,
      boolean compiled) {}

  public record ClauseFacts(
      @Nonnull String name, @Nonnull List<String> lines, @Nonnull List<SetRef> references) {}

  public record SetRef(@Nonnull String type, @Nonnull String name) {}

  /** A prefix, community or AS-path set, as written. */
  public record SetFacts(@Nonnull String type, @Nonnull String name, @Nonnull List<String> lines) {}
}
