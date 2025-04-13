package org.batfish.vendor.a10.representation;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.MultipathEquivalentAsPathMatchMode.EXACT_PATH;
import static org.batfish.datamodel.Names.generatedBgpCommonExportPolicyName;
import static org.batfish.datamodel.Names.generatedBgpPeerExportPolicyName;
import static org.batfish.datamodel.Names.generatedBgpRedistributionPolicyName;
import static org.batfish.datamodel.Prefix.MAX_PREFIX_LENGTH;
import static org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker.NO_PREFERENCE;
import static org.batfish.datamodel.bgp.NextHopIpTieBreaker.LOWEST_NEXT_HOP_IP;
import static org.batfish.datamodel.transformation.TransformationStep.assignDestinationIp;
import static org.batfish.datamodel.transformation.TransformationStep.assignDestinationPort;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourceIp;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourcePort;
import static org.batfish.vendor.a10.representation.TraceElements.traceElementForAccessList;
import static org.batfish.vendor.a10.representation.TraceElements.traceElementForVirtualServer;
import static org.batfish.vendor.a10.representation.TraceElements.traceElementForVirtualServerPort;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpTieBreaker;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.KernelRoute;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.NamedPort;
import org.batfish.datamodel.Names;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.VrrpGroup;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.bgp.AddressFamilyCapabilities;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.CallExpr;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.expr.MatchTag;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetWeight;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.datamodel.tracking.TrackAction;
import org.batfish.datamodel.transformation.ApplyAll;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.TransformationStep;
import org.batfish.vendor.VendorStructureId;
import org.batfish.vendor.a10.representation.BgpNeighbor.SendCommunity;

/** Conversion helpers for converting VS model {@link A10Configuration} to the VI model. */
public class A10Conversion {
  /**
   * Start port was determined via lab testing. The port also appears to increment for each
   * subsequent connection.
   */
  public static final int SNAT_PORT_POOL_START = 2048;

  public static final int SNAT_PORT_POOL_END = NamedPort.EPHEMERAL_HIGHEST.number();
  static final boolean DEFAULT_VRRP_A_PREEMPT = true;
  @VisibleForTesting public static final int DEFAULT_VRRP_A_PRIORITY = 150;

  // TODO: confirm on ACOSv2 device
  @VisibleForTesting public static final int DEFAULT_HA_PRIORITY = 150;

  @VisibleForTesting public static final long KERNEL_ROUTE_TAG_NAT_POOL = 1L;
  @VisibleForTesting public static final long KERNEL_ROUTE_TAG_VIRTUAL_SERVER_FLAGGED = 2L;
  @VisibleForTesting public static final long KERNEL_ROUTE_TAG_VIRTUAL_SERVER_UNFLAGGED = 3L;
  @VisibleForTesting public static final long KERNEL_ROUTE_TAG_FLOATING_IP = 4L;
  @VisibleForTesting static final int DEFAULT_EBGP_ADMIN_COST = 20;
  @VisibleForTesting static final int DEFAULT_IBGP_ADMIN_COST = 200;
  @VisibleForTesting static final int DEFAULT_LOCAL_ADMIN_COST = 200;
  @VisibleForTesting static final int DEFAULT_LOCAL_BGP_WEIGHT = 32768;

  // TODO: confirm on ACOSv2 device
  @VisibleForTesting static final int DEFAULT_HA_GROUP = 0;

  /** Set of {@link VirtualServerPort.Type}s that use {@code tcp} protocol */
  static final Set<VirtualServerPort.Type> VIRTUAL_TCP_PORT_TYPES =
      ImmutableSet.of(
          VirtualServerPort.Type.DIAMETER,
          VirtualServerPort.Type.HTTP,
          VirtualServerPort.Type.HTTPS,
          VirtualServerPort.Type.SMTP,
          VirtualServerPort.Type.SSL_PROXY,
          VirtualServerPort.Type.TCP,
          VirtualServerPort.Type.TCP_PROXY);

  /** Set of {@link VirtualServerPort.Type}s that use {@code udp} protocol */
  static final Set<VirtualServerPort.Type> VIRTUAL_UDP_PORT_TYPES =
      ImmutableSet.of(
          VirtualServerPort.Type.RADIUS, VirtualServerPort.Type.SIP, VirtualServerPort.Type.UDP);

  /** Returns the {@link IntegerSpace} representing the specified {@link ServerPort}'s ports. */
  @VisibleForTesting
  static @Nonnull IntegerSpace toIntegerSpace(ServerPort port) {
    return IntegerSpace.of(new SubRange(port.getNumber(), getEndPort(port)))
        .intersection(IntegerSpace.PORTS);
  }

  /**
   * Returns the {@link IntegerSpace} representing the specified {@link VirtualServerPort}'s ports.
   */
  @VisibleForTesting
  static @Nonnull IntegerSpace toIntegerSpace(VirtualServerPort port) {
    return IntegerSpace.of(toSubRange(port)).intersection(IntegerSpace.PORTS);
  }

  /** Determine the last/highest port represented by the specified {@link ServerPort}. */
  public static int getEndPort(ServerPort port) {
    return port.getNumber() + Optional.ofNullable(port.getRange()).orElse(0);
  }

  /** Determine the last/highest port represented by the specified {@link VirtualServerPort}. */
  public static int getEndPort(VirtualServerPort port) {
    return port.getNumber() + Optional.ofNullable(port.getRange()).orElse(0);
  }

  private static @Nonnull SubRange toSubRange(VirtualServerPort port) {
    return new SubRange(port.getNumber(), getEndPort(port));
  }

  /** Returns the {@link IpProtocol} corresponding to the specified virtual-server port. */
  @VisibleForTesting
  static @Nonnull Optional<IpProtocol> toProtocol(VirtualServerPort port) {
    VirtualServerPort.Type type = port.getType();
    if (VIRTUAL_TCP_PORT_TYPES.contains(type)) {
      return Optional.of(IpProtocol.TCP);
    }
    assert VIRTUAL_UDP_PORT_TYPES.contains(type);
    return Optional.of(IpProtocol.UDP);
  }

  /** Returns the source transformation step for the specified NAT pool. */
  static @Nonnull TransformationStep toSnatTransformationStep(NatPool pool) {
    return new ApplyAll(
        assignSourceIp(pool.getStart(), pool.getEnd()),
        assignSourcePort(SNAT_PORT_POOL_START, SNAT_PORT_POOL_END));
  }

  /**
   * Returns the destination transformation steps for the specified service-group. Each step
   * corresponds to the transformation for a single member of the service-group.
   */
  @VisibleForTesting
  static @Nonnull List<TransformationStep> toDstTransformationSteps(
      ServiceGroup serviceGroup, Map<String, Server> servers) {
    return serviceGroup.getMembers().values().stream()
        .map(
            m -> {
              Server server = servers.get(m.getName());
              ServerPort.ServerPortAndType serverPortKey =
                  new ServerPort.ServerPortAndType(m.getPort(), serviceGroup.getType());

              // Guaranteed by extraction
              assert server != null;
              ServerPort serverPort = server.getPorts().get(serverPortKey);
              assert serverPort != null;

              if (!firstNonNull(server.getEnable(), true)
                  || !firstNonNull(serverPort.getEnable(), true)) {
                return null;
              }

              return new ApplyAll(
                  toDstPortTransformationStep(serverPort), toDstIpTransformationStep(server));
            })
        .filter(Objects::nonNull)
        .collect(ImmutableList.toImmutableList());
  }

  /** Returns the destination IP transformation step for the specified server. */
  private static @Nonnull TransformationStep toDstIpTransformationStep(Server server) {
    return assignDestinationIp(ServerTargetToIp.INSTANCE.visit(server.getTarget()));
  }

  /** Returns the destination port transformation step for the specified server. */
  private static @Nonnull TransformationStep toDstPortTransformationStep(ServerPort serverPort) {
    IntegerSpace portRange = toIntegerSpace(serverPort);
    return assignDestinationPort(portRange.least(), portRange.greatest());
  }

  /**
   * Returns a {@link Transformation} that chains the provided {@link SimpleTransformation}s
   * orElse-wise. Returns {@link Optional#empty()} if there are no transformations.
   *
   * <p>This function does not make any guarantees about ordering of the resulting {@link
   * Transformation}.
   */
  public static @Nonnull Optional<Transformation> orElseChain(
      Iterable<SimpleTransformation> simpleTransformations) {
    Transformation current = null;
    for (SimpleTransformation earlier : simpleTransformations) {
      current =
          Transformation.when(earlier.getGuard())
              .apply(earlier.getStep())
              .setOrElse(current)
              .build();
    }
    return Optional.ofNullable(current);
  }

  public static boolean isVirtualServerEnabled(VirtualServer virtualServer) {
    return firstNonNull(virtualServer.getEnable(), true);
  }

  static boolean isAnyVirtualServerPortEnabled(VirtualServer virtualServer) {
    return isVirtualServerEnabled(virtualServer)
        && virtualServer.getPorts().values().stream()
            .anyMatch(A10Conversion::isVirtualServerPortEnabled);
  }

  public static boolean isVirtualServerPortEnabled(VirtualServerPort port) {
    return firstNonNull(port.getEnable(), true);
  }

  static boolean isVrrpAEnabled(@Nullable VrrpA vrrpA) {
    return Optional.ofNullable(vrrpA)
        .map(VrrpA::getCommon)
        .map(VrrpACommon::getEnable)
        .orElse(false);
  }

  @VisibleForTesting
  public static @Nonnull String generatedServerTrackMethodName(Ip gatewayIp) {
    return String.format("~gateway~%s~", gatewayIp);
  }

  @VisibleForTesting
  public static @Nonnull String generatedFailedTrackMethodName(String trackName) {
    return String.format("~FAILED~%s~", trackName);
  }

  /** Precondition: vrrpA.getCommon() is not null. */
  static @Nonnull Stream<Integer> getEnabledVrids(VrrpA vrrpA) {
    assert vrrpA.getCommon() != null;
    return Stream.concat(
        vrrpA.getCommon().getDisableDefaultVrid() ? Stream.of() : Stream.of(0),
        vrrpA.getVrids().keySet().stream().filter(vrid -> vrid != 0));
  }

  /** Get all the IPs (not the subnets) of all {@code ip nat pool}s assigned to the given vrid. */
  static @Nonnull Stream<Ip> getNatPoolIps(Collection<NatPool> natPools, int vrid) {
    return natPools.stream()
        .filter(pool -> vrid == getNatPoolVrid(pool))
        .flatMap(pool -> enumerateIps(pool.getStart(), pool.getEnd()));
  }

  /**
   * Get all the IPs (not the subnets) of all {@code ip nat pool}s assigned to the given ha-group.
   */
  static @Nonnull Stream<Ip> getNatPoolIpsByHaGroup(Collection<NatPool> natPools, int haGroup) {
    return natPools.stream()
        .filter(pool -> haGroup == getNatPoolHaGroup(pool))
        .flatMap(pool -> enumerateIps(pool.getStart(), pool.getEnd()));
  }

  private static int getNatPoolHaGroup(NatPool natPool) {
    return firstNonNull(natPool.getHaGroupId(), DEFAULT_HA_GROUP);
  }

  /** Get all the IPs (not the subnets) of all {@code ip nat pool}s. */
  static @Nonnull Stream<Ip> getNatPoolIpsForAllVrids(Collection<NatPool> natPools) {
    return natPools.stream().flatMap(pool -> enumerateIps(pool.getStart(), pool.getEnd()));
  }

  /** Get all of the the kernel routes generated for all {@code ip nat pool}s. */
  static @Nonnull Stream<KernelRoute> getNatPoolKernelRoutes(Collection<NatPool> natPools) {
    return natPools.stream().map(A10Conversion::toKernelRoute);
  }

  /** Create kernel route for an {@code ip nat pool} network. */
  @VisibleForTesting
  static @Nonnull KernelRoute toKernelRoute(NatPool natPool) {
    Ip requiredOwnedIp = natPool.getStart();
    Prefix network = Prefix.create(requiredOwnedIp, natPool.getNetmask());
    return KernelRoute.builder()
        .setNetwork(network)
        .setRequiredOwnedIp(requiredOwnedIp)
        .setTag(KERNEL_ROUTE_TAG_NAT_POOL)
        .build();
  }

  private static int getNatPoolVrid(NatPool natPool) {
    return Optional.ofNullable(natPool.getVrid()).orElse(0);
  }

  private static @Nonnull Stream<Ip> enumerateIps(Ip start, Ip end) {
    long startAsLong = start.asLong();
    long endAsLong = end.asLong();
    assert startAsLong <= endAsLong;
    return LongStream.range(startAsLong, endAsLong + 1L).mapToObj(Ip::create);
  }

  /**
   * Get all the IPs (not the subnets) of all enabled {@code slb virtual-server}s assigned to the
   * given vrid.
   */
  static @Nonnull Stream<Ip> getVirtualServerIps(
      Collection<VirtualServer> virtualServers, int vrid) {
    return virtualServers.stream()
        .filter(A10Conversion::isIpv4VirtualServer)
        .filter(A10Conversion::isAnyVirtualServerPortEnabled)
        .filter(vs -> vrid == getVirtualServerVrid(vs))
        .map(A10Conversion::getTargetIp);
  }

  /**
   * Get all the IPs (not the subnets) of all enabled {@code slb virtual-server}s assigned to the
   * given ha-group.
   */
  static @Nonnull Stream<Ip> getVirtualServerIpsByHaGroup(
      Collection<VirtualServer> virtualServers, int haGroup) {
    return virtualServers.stream()
        .filter(A10Conversion::isIpv4VirtualServer)
        .filter(A10Conversion::isAnyVirtualServerPortEnabled)
        .filter(vs -> haGroup == getVirtualServerHaGroup(vs))
        .map(A10Conversion::getTargetIp);
  }

  private static int getVirtualServerHaGroup(VirtualServer virtualServer) {
    return firstNonNull(virtualServer.getHaGroup(), DEFAULT_HA_GROUP);
  }

  /** Get all the IPs (not the subnets) of all enabled {@code slb virtual-server}s. */
  static @Nonnull Stream<Ip> getVirtualServerIpsForAllVrids(
      Collection<VirtualServer> virtualServers) {
    return virtualServers.stream()
        .filter(A10Conversion::isIpv4VirtualServer)
        .filter(A10Conversion::isAnyVirtualServerPortEnabled)
        .map(A10Conversion::getTargetIp);
  }

  /** Get all of the the kernel routes generated for all enabled {@code slb virtual-server}s. */
  static @Nonnull Stream<KernelRoute> getVirtualServerKernelRoutes(
      Collection<VirtualServer> virtualServers) {
    return virtualServers.stream()
        .filter(A10Conversion::isIpv4VirtualServer)
        .filter(A10Conversion::isAnyVirtualServerPortEnabled)
        .map(A10Conversion::toKernelRoute);
  }

  /**
   * Returns {@code true} if the given {@link VirtualServer} has a {@link VirtualServerTarget
   * target} that uses IPv4 addressing.
   */
  public static boolean isIpv4VirtualServer(VirtualServer virtualServer) {
    return IS_IPV4_VIRTUAL_SERVER_TARGET.visit(virtualServer.getTarget());
  }

  private static final VirtualServerTargetVisitor<Boolean> IS_IPV4_VIRTUAL_SERVER_TARGET =
      new VirtualServerTargetVisitor<Boolean>() {
        @Override
        public Boolean visitVirtualServerTargetAddress(
            VirtualServerTargetAddress virtualServerTargetAddress) {
          return true;
        }

        @Override
        public Boolean visitVirtualServerTargetAddress6(
            VirtualServerTargetAddress6 virtualServerTargetAddress6) {
          return false;
        }
      };

  @VisibleForTesting
  static @Nonnull KernelRoute toKernelRoute(VirtualServer virtualServer) {
    Ip requiredOwnedIp = getTargetIp(virtualServer);
    Prefix network = VirtualServerTargetKernelRouteNetworkExtractor.extractNetwork(virtualServer);
    long tag =
        getRedistributionFlagged(virtualServer)
            ? KERNEL_ROUTE_TAG_VIRTUAL_SERVER_FLAGGED
            : KERNEL_ROUTE_TAG_VIRTUAL_SERVER_UNFLAGGED;
    return KernelRoute.builder()
        .setNetwork(network)
        .setRequiredOwnedIp(requiredOwnedIp)
        .setTag(tag)
        .build();
  }

  /** Convert ACOSv2 {@code floating-ip}s to {@link KernelRoute}s */
  static @Nonnull Stream<KernelRoute> getFloatingIpKernelRoutes(Set<Ip> floatingIps) {
    return floatingIps.stream().map(A10Conversion::toKernelRoute);
  }

  static @Nonnull Stream<KernelRoute> getFloatingIpKernelRoutes(VrrpA vrrpA) {
    return getFloatingIpsForAllVrids(vrrpA).map(A10Conversion::toKernelRoute);
  }

  static @Nonnull KernelRoute toKernelRoute(Ip floatingIp) {
    return KernelRoute.builder()
        .setNetwork(Prefix.create(floatingIp, MAX_PREFIX_LENGTH))
        .setTag(KERNEL_ROUTE_TAG_FLOATING_IP)
        .setRequiredOwnedIp(floatingIp)
        .build();
  }

  static @Nonnull Stream<Ip> getFloatingIps(VrrpA vrrpA, int vrid) {
    if (vrid == 0 && !vrrpA.getVrids().containsKey(0)) {
      return Stream.of();
    }
    return vrrpA.getVrids().get(vrid).getFloatingIps().stream();
  }

  static @Nonnull Stream<Ip> getFloatingIpsByHaGroup(Map<Ip, FloatingIp> floatingIps, int haGroup) {
    return floatingIps.entrySet().stream()
        .filter(e -> getFloatingIpHaGroup(e.getValue()) == haGroup)
        .map(Entry::getKey);
  }

  private static int getFloatingIpHaGroup(FloatingIp floatingIp) {
    return firstNonNull(floatingIp.getHaGroup(), DEFAULT_HA_GROUP);
  }

  static @Nonnull Stream<Ip> getFloatingIpsForAllVrids(VrrpA vrrpA) {
    return vrrpA.getVrids().values().stream()
        .map(VrrpAVrid::getFloatingIps)
        .flatMap(Collection::stream);
  }

  private static boolean getRedistributionFlagged(VirtualServer virtualServer) {
    return firstNonNull(virtualServer.getRedistributionFlagged(), Boolean.FALSE);
  }

  public static @Nonnull Ip getTargetIp(VirtualServer virtualServer) {
    return virtualServer.getTarget().accept(VirtualServerTargetVirtualAddressExtractor.INSTANCE);
  }

  /** Extracts the virtual {@link Ip} of a {@link VirtualServerTarget} that the device may own. */
  public static final class VirtualServerTargetVirtualAddressExtractor
      implements VirtualServerTargetVisitor<Ip> {
    // TODO: this may need to return a set of IPs; or a prefix or an IP.
    static final VirtualServerTargetVirtualAddressExtractor INSTANCE =
        new VirtualServerTargetVirtualAddressExtractor();

    @Override
    public Ip visitVirtualServerTargetAddress(
        VirtualServerTargetAddress virtualServerTargetAddress) {
      return virtualServerTargetAddress.getAddress();
    }

    @Override
    public Ip visitVirtualServerTargetAddress6(
        VirtualServerTargetAddress6 virtualServerTargetAddress6) {
      throw new IllegalArgumentException("Cannot convert IPv6 target");
    }
  }

  /**
   * Extracts the network of the {@link KernelRoute} corresponding to a {@link VirtualServerTarget}.
   */
  private static final class VirtualServerTargetKernelRouteNetworkExtractor
      implements VirtualServerTargetVisitor<Prefix> {
    // TODO: this may need to return a set of IPs; or a prefix or an IP.
    private static final VirtualServerTargetKernelRouteNetworkExtractor INSTANCE =
        new VirtualServerTargetKernelRouteNetworkExtractor();

    private static @Nonnull Prefix extractNetwork(VirtualServer virtualServer) {
      return virtualServer.getTarget().accept(INSTANCE);
    }

    @Override
    public Prefix visitVirtualServerTargetAddress(
        VirtualServerTargetAddress virtualServerTargetAddress) {
      return Prefix.create(virtualServerTargetAddress.getAddress(), MAX_PREFIX_LENGTH);
    }

    @Override
    public Prefix visitVirtualServerTargetAddress6(
        VirtualServerTargetAddress6 virtualServerTargetAddress6) {
      throw new IllegalArgumentException("Cannot extract network from IPv6 target");
    }
  }

  private static int getVirtualServerVrid(VirtualServer virtualServer) {
    return Optional.ofNullable(virtualServer.getVrid()).orElse(0);
  }

  /**
   * Create a {@link VrrpGroup} from the configuration for a {@code vrrp-a vrid}, the source
   * address, the virtual addresses to assign, and the interfaces on which all virtual addresses
   * should be assigned.
   */
  static @Nonnull VrrpGroup toVrrpGroup(
      @Nullable VrrpAVrid vridConfig,
      ConcreteInterfaceAddress sourceAddress,
      Iterable<Ip> virtualAddresses,
      Collection<String> ipOwnerInterfaces,
      // template name -> generated track method name -> action
      Map<String, Map<String, TrackAction>> failOverPolicyTemplateActions) {
    Map<String, Set<Ip>> virtualAddressesMap =
        ipOwnerInterfaces.stream()
            .collect(
                ImmutableMap.toImmutableMap(
                    Function.identity(), n -> ImmutableSet.copyOf(virtualAddresses)));
    if (vridConfig == null) {
      return VrrpGroup.builder()
          .setPreempt(DEFAULT_VRRP_A_PREEMPT)
          .setPriority(DEFAULT_VRRP_A_PRIORITY)
          .setSourceAddress(sourceAddress)
          .setVirtualAddresses(virtualAddressesMap)
          .build();
    } else {
      Map<String, TrackAction> trackActions =
          Optional.ofNullable(vridConfig.getBladeParameters())
              .map(VrrpaVridBladeParameters::getFailOverPolicyTemplate)
              // already warned in extraction if absent
              .map(failOverPolicyTemplateActions::get)
              .orElse(ImmutableMap.of());
      return VrrpGroup.builder()
          .setPreempt(getVrrpAVridPreempt(vridConfig))
          .setPriority(getVrrpAVridPriority(vridConfig))
          .setSourceAddress(sourceAddress)
          .setVirtualAddresses(virtualAddressesMap)
          .setTrackActions(trackActions)
          .build();
    }
  }

  /**
   * Create a {@link VrrpGroup} from the configuration for a particular {@code ha group} from the ha
   * configuration, the source address, the virtual addresses to assign, and the interfaces on which
   * all virtual addresses should be assigned.
   */
  static @Nonnull VrrpGroup toVrrpGroup(
      int haGroupId,
      Ha ha,
      ConcreteInterfaceAddress sourceAddress,
      Iterable<Ip> virtualAddresses,
      Collection<String> ipOwnerInterfaces,
      Map<String, TrackAction> trackActions) {
    Map<String, Set<Ip>> virtualAddressesMap =
        ipOwnerInterfaces.stream()
            .collect(
                ImmutableMap.toImmutableMap(
                    Function.identity(), n -> ImmutableSet.copyOf(virtualAddresses)));
    return VrrpGroup.builder()
        .setPreempt(getHaPreemptionEnable(ha))
        .setPriority(getHaGroupPriority(ha.getGroups().get(haGroupId)))
        .setVirtualAddresses(virtualAddressesMap)
        .setSourceAddress(sourceAddress)
        .setTrackActions(trackActions)
        .build();
  }

  private static int getHaGroupPriority(HaGroup haGroup) {
    return firstNonNull(haGroup.getPriority(), DEFAULT_HA_PRIORITY);
  }

  static boolean getHaPreemptionEnable(Ha ha) {
    return firstNonNull(ha.getPreemptionEnable(), Boolean.FALSE);
  }

  private static boolean getVrrpAVridPreempt(VrrpAVrid vridConfig) {
    return !firstNonNull(vridConfig.getPreemptModeDisable(), false);
  }

  private static int getVrrpAVridPriority(VrrpAVrid vridConfig) {
    return Optional.ofNullable(vridConfig.getBladeParameters())
        .map(VrrpaVridBladeParameters::getPriority)
        .orElse(DEFAULT_VRRP_A_PRIORITY);
  }

  /**
   * Returns the source IP to be used for vrrp-a peerings if the specified VI interface should have
   * VRRP configuration associated with it when vrrp-a is enabled, or else {@link Optional#empty()}.
   */
  static @Nonnull Optional<ConcreteInterfaceAddress> findVrrpAEnabledSourceAddress(
      org.batfish.datamodel.Interface iface, Set<Ip> peerIps) {
    if (iface.getInterfaceType() == InterfaceType.LOOPBACK) {
      return Optional.empty();
    }
    return iface.getAllConcreteAddresses().stream()
        .filter(address -> peerIps.stream().anyMatch(address.getPrefix()::containsIp))
        .findFirst();
  }

  /**
   * Returns a boolean indicating if the specified VI interface should have VRRP configuration
   * associated with it when vrrp-a is disabled.
   */
  static boolean vrrpADisabledAppliesToInterface(org.batfish.datamodel.Interface iface) {
    if (iface.getInterfaceType() == InterfaceType.LOOPBACK) {
      return false;
    }
    return iface.getConcreteAddress() != null;
  }

  /**
   * Returns the source IP to be used for HA if the specified VI interface should have VRRP
   * configuration associated with it when ha is enabled, or else {@link Optional#empty()}.
   */
  static @Nonnull Optional<ConcreteInterfaceAddress> findHaSourceAddress(
      org.batfish.datamodel.Interface iface, Ip connMirrorIp) {
    if (iface.getInterfaceType() == InterfaceType.LOOPBACK) {
      return Optional.empty();
    }
    return iface.getAllConcreteAddresses().stream()
        .filter(address -> address.getPrefix().containsIp(connMirrorIp))
        .findFirst();
  }

  /** Convert the BGP process and associated routing policies, and attach them to the config. */
  static void createBgpProcess(BgpProcess bgpProcess, Configuration c, Warnings w) {
    Ip routerId = bgpProcess.getRouterId();
    if (routerId == null) {
      w.redFlag("Converting a BgpProcess without an explicit router-id is currently unsupported");
      return;
    }
    org.batfish.datamodel.BgpProcess newBgpProcess =
        bgpProcessBuilder().setRouterId(routerId).setVrf(c.getDefaultVrf()).build();

    boolean multipath = firstNonNull(bgpProcess.getMaximumPaths(), 1) > 1;
    newBgpProcess.setMultipathEbgp(multipath);
    newBgpProcess.setMultipathIbgp(multipath);
    // TODO: verify: https://github.com/batfish/batfish/issues/7567
    newBgpProcess.setMultipathEquivalentAsPathMatchMode(EXACT_PATH);
    // TODO: verify: https://github.com/batfish/batfish/issues/7567
    newBgpProcess.setTieBreaker(BgpTieBreaker.ROUTER_ID);
    long defaultLocalAs = bgpProcess.getAsn();

    // TODO: support alternate default local-preference

    // Next we build up the BGP common export policy.
    RoutingPolicy.Builder bgpCommonExportPolicy =
        RoutingPolicy.builder()
            .setOwner(c)
            .setName(Names.generatedBgpCommonExportPolicyName(DEFAULT_VRF_NAME));

    // Finalize common export policy
    bgpCommonExportPolicy.addStatement(Statements.ReturnTrue.toStaticStatement()).build();

    // Create BGP redistribution policy to import main RIB routes into BGP RIB
    String redistPolicyName = generatedBgpRedistributionPolicyName(DEFAULT_VRF_NAME);
    RoutingPolicy.Builder redistributionPolicy =
        RoutingPolicy.builder().setOwner(c).setName(redistPolicyName);
    redistributionPolicy.addStatement(new SetWeight(new LiteralInt(DEFAULT_LOCAL_BGP_WEIGHT)));

    // Redistribute connected
    if (bgpProcess.isRedistributeConnected()) {
      BooleanExpr guard = new MatchProtocol(RoutingProtocol.CONNECTED);
      guard.setComment("Redistribute connected routes into BGP");
      redistributionPolicy.addStatement(
          new If(guard, ImmutableList.of(Statements.ExitAccept.toStaticStatement())));
    }

    // Redistribute floating-ip
    if (bgpProcess.isRedistributeFloatingIp()) {
      ImmutableList.Builder<BooleanExpr> conditions = ImmutableList.builder();
      conditions.add(new MatchProtocol(RoutingProtocol.KERNEL));
      conditions.add(new MatchTag(IntComparator.EQ, new LiteralLong(KERNEL_ROUTE_TAG_FLOATING_IP)));
      Conjunction guard = new Conjunction(conditions.build());
      guard.setComment("Redistribute floating-ip routes into BGP");
      redistributionPolicy.addStatement(
          new If(guard, ImmutableList.of(Statements.ExitAccept.toStaticStatement())));
    }

    // Redistribute ip-nat
    if (bgpProcess.isRedistributeIpNat()) {
      ImmutableList.Builder<BooleanExpr> conditions = ImmutableList.builder();
      conditions.add(new MatchProtocol(RoutingProtocol.KERNEL));
      conditions.add(new MatchTag(IntComparator.EQ, new LiteralLong(KERNEL_ROUTE_TAG_NAT_POOL)));
      Conjunction guard = new Conjunction(conditions.build());
      guard.setComment("Redistribute ip nat pool routes into BGP");
      redistributionPolicy.addStatement(
          new If(guard, ImmutableList.of(Statements.ExitAccept.toStaticStatement())));
    }

    // Redistribute vip only-flagged
    if (bgpProcess.isRedistributeVipOnlyFlagged()) {
      ImmutableList.Builder<BooleanExpr> conditions = ImmutableList.builder();
      conditions.add(new MatchProtocol(RoutingProtocol.KERNEL));
      conditions.add(
          new MatchTag(IntComparator.EQ, new LiteralLong(KERNEL_ROUTE_TAG_VIRTUAL_SERVER_FLAGGED)));
      Conjunction guard = new Conjunction(conditions.build());
      guard.setComment("Redistribute redistribution-flagged slb virtual-server routes into BGP");
      redistributionPolicy.addStatement(
          new If(guard, ImmutableList.of(Statements.ExitAccept.toStaticStatement())));
    }

    // Redistribute vip not-flagged
    if (bgpProcess.isRedistributeVipOnlyNotFlagged()) {
      ImmutableList.Builder<BooleanExpr> conditions = ImmutableList.builder();
      conditions.add(new MatchProtocol(RoutingProtocol.KERNEL));
      conditions.add(
          new MatchTag(
              IntComparator.EQ, new LiteralLong(KERNEL_ROUTE_TAG_VIRTUAL_SERVER_UNFLAGGED)));
      Conjunction guard = new Conjunction(conditions.build());
      guard.setComment(
          "Redistribute non-redistribution-flagged slb virtual-server routes into BGP");
      redistributionPolicy.addStatement(
          new If(guard, ImmutableList.of(Statements.ExitAccept.toStaticStatement())));
    }

    // Finalize redistribution policy and attach to process
    redistributionPolicy.addStatement(Statements.ExitReject.toStaticStatement()).build();
    newBgpProcess.setRedistributionPolicy(redistPolicyName);

    // Create and attach neighbors
    // TODO: support deactivated neighbor
    bgpProcess
        .getNeighbors()
        .forEach(
            (bgpNeighborId, bgpNeighbor) -> {
              createAndAttachBgpNeighbor(
                  bgpNeighborId, defaultLocalAs, bgpNeighbor, newBgpProcess, c, w);
            });
  }

  private static @Nonnull org.batfish.datamodel.BgpProcess.Builder bgpProcessBuilder() {
    return org.batfish.datamodel.BgpProcess.builder()
        .setEbgpAdminCost(DEFAULT_EBGP_ADMIN_COST)
        .setIbgpAdminCost(DEFAULT_IBGP_ADMIN_COST)
        .setLocalAdminCost(DEFAULT_LOCAL_ADMIN_COST)
        .setLocalOriginationTypeTieBreaker(NO_PREFERENCE)
        .setNetworkNextHopIpTieBreaker(LOWEST_NEXT_HOP_IP)
        .setRedistributeNextHopIpTieBreaker(LOWEST_NEXT_HOP_IP);
  }

  /**
   * Create a {@link BgpActivePeerConfig} for a bgp neighbor, and attach it to the new BGP process.
   */
  @VisibleForTesting
  static void createAndAttachBgpNeighbor(
      BgpNeighborId bgpNeighborId,
      long defaultLocalAs,
      BgpNeighbor bgpNeighbor,
      org.batfish.datamodel.BgpProcess newBgpProcess,
      Configuration c,
      Warnings w) {
    // TODO: use visitor, support peer-groups
    assert bgpNeighborId instanceof BgpNeighborIdAddress;
    Ip remoteIp = ((BgpNeighborIdAddress) bgpNeighborId).getAddress();
    Long remoteAs = bgpNeighbor.getRemoteAs();
    assert remoteAs != null;
    SendCommunity sendCommunity = bgpNeighbor.getSendCommunity();
    boolean sendStandard =
        sendCommunity == SendCommunity.STANDARD || sendCommunity == SendCommunity.BOTH;
    boolean sendExtended =
        sendCommunity == SendCommunity.EXTENDED || sendCommunity == SendCommunity.BOTH;

    BgpActivePeerConfig.builder()
        .setBgpProcess(newBgpProcess)
        .setLocalIp(
            computeUpdateSource(
                c.getAllInterfaces(DEFAULT_VRF_NAME), remoteIp, bgpNeighbor.getUpdateSource(), w))
        .setPeerAddress(remoteIp)
        .setLocalAs(defaultLocalAs)
        .setRemoteAs(remoteAs)
        .setDescription(bgpNeighbor.getDescription())
        .setIpv4UnicastAddressFamily(
            Ipv4UnicastAddressFamily.builder()
                // TODO: support route-maps
                .setExportPolicy(computeExportPolicy(bgpNeighborId, c))
                .setAddressFamilyCapabilities(
                    AddressFamilyCapabilities.builder()
                        .setSendCommunity(sendStandard)
                        .setSendExtendedCommunity(sendExtended)
                        .build())
                .build())
        // build and attach neighbor
        .build();
  }

  /** Create export policy for a bgp neighbor, populate it in the config, and returns its name. */
  private static @Nonnull String computeExportPolicy(BgpNeighborId bgpNeighborId, Configuration c) {
    // TODO: support non-IP neighbor-id
    assert bgpNeighborId instanceof BgpNeighborIdAddress;
    Conjunction peerExportGuard = new Conjunction();
    List<BooleanExpr> peerExportConditions = peerExportGuard.getConjuncts();
    List<Statement> exportStatements = new LinkedList<>();
    exportStatements.add(
        new If(
            "peer-export policy main conditional: exitAccept if true / exitReject if false",
            peerExportGuard,
            ImmutableList.of(Statements.ExitAccept.toStaticStatement()),
            ImmutableList.of(Statements.ExitReject.toStaticStatement())));
    peerExportConditions.add(new CallExpr(generatedBgpCommonExportPolicyName(DEFAULT_VRF_NAME)));
    RoutingPolicy exportPolicy =
        new RoutingPolicy(
            generatedBgpPeerExportPolicyName(
                DEFAULT_VRF_NAME, ((BgpNeighborIdAddress) bgpNeighborId).getAddress().toString()),
            c);
    exportPolicy.setStatements(exportStatements);
    c.getRoutingPolicies().put(exportPolicy.getName(), exportPolicy);
    return exportPolicy.getName();
  }

  /**
   * Compute the update source IP for a bgp neighbor. Use explicit update source IP if avaialble.
   * Otherwise, use interface address in subnet containing the peer IP. If one cannot be found,
   * return {@code null}.
   */
  @VisibleForTesting
  static @Nullable Ip computeUpdateSource(
      Map<String, org.batfish.datamodel.Interface> interfaces,
      Ip remoteIp,
      @Nullable BgpNeighborUpdateSource updateSource,
      Warnings warnings) {
    if (updateSource != null) {
      // TODO: support interface update-source
      assert updateSource instanceof BgpNeighborUpdateSourceAddress;
      return ((BgpNeighborUpdateSourceAddress) updateSource).getAddress();
    }
    Optional<Ip> firstMatchingInterfaceAddress =
        interfaces.values().stream()
            .flatMap(i -> i.getAllConcreteAddresses().stream())
            .filter(ia -> ia != null && ia.getPrefix().containsIp(remoteIp))
            .map(ConcreteInterfaceAddress::getIp)
            .findFirst();
    if (firstMatchingInterfaceAddress.isPresent()) {
      return firstMatchingInterfaceAddress.get();
    }
    return null;
  }

  /**
   * Returns {@code true} if the specified interface is effectively enabled. Resolves default values
   * using the supplied {@code acosMajorVersion}, since defaults are version-dependent.
   */
  public static boolean getInterfaceEnabledEffective(
      Interface iface, @Nullable Integer acosMajorVersion) {
    Boolean enabled = iface.getEnabled();
    if (enabled != null) {
      return enabled;
    }
    return switch (iface.getType()) {
      case ETHERNET ->
          // Err on the side of enabling too many interfaces, if version num is unknown
          acosMajorVersion == null || acosMajorVersion <= 2;
      case LOOPBACK, TRUNK, VE -> true;
    };
  }

  /**
   * Convert the specified {@link AccessList} into a VI {@link IpAccessList} and attach to the
   * specified VI {@link Configuration}.
   */
  static void convertAccessList(AccessList acl, Configuration c, String filename) {
    IpAccessList.builder()
        .setLines(toAclLines(acl, filename))
        .setOwner(c)
        .setName(computeAclName(acl.getName()))
        .build();
  }

  /** Convert the specified {@link AccessList} to a list of {@link AclLine}s. */
  private static @Nonnull List<AclLine> toAclLines(AccessList acl, String filename) {
    VendorStructureId vsid =
        new VendorStructureId(
            filename, A10StructureType.ACCESS_LIST.getDescription(), acl.getName());
    ImmutableList.Builder<AclLine> lines = ImmutableList.builder();
    acl.getRules()
        .forEach(
            rule ->
                lines.add(
                    new ExprAclLine(
                        toLineAction(rule),
                        AccessListRuleToMatchExpr.INSTANCE.visit(rule),
                        rule.getLineText(),
                        traceElementForAccessList(
                            acl.getName(),
                            filename,
                            rule.getAction() == AccessListRule.Action.PERMIT),
                        vsid)));
    return lines.build();
  }

  /** Compute the VI {@link IpAccessList} name for the specified {@link AccessList}. */
  @VisibleForTesting
  public static String computeAclName(String aclName) {
    return String.format("IP_ACCESS_LIST~%s", aclName);
  }

  /** Convert a {@link VirtualServerTarget} to its corresponding {@link AclLineMatchExpr}. */
  static final class VirtualServerTargetToMatchExpr
      implements VirtualServerTargetVisitor<AclLineMatchExpr> {
    static final VirtualServerTargetToMatchExpr INSTANCE = new VirtualServerTargetToMatchExpr();

    @Override
    public AclLineMatchExpr visitVirtualServerTargetAddress(
        VirtualServerTargetAddress virtualServerTargetAddress) {
      return AclLineMatchExprs.matchDst(
          virtualServerTargetAddress.getAddress().toIpSpace(),
          TraceElement.builder()
              .add(
                  String.format(
                      "Matched virtual-server target address %s",
                      virtualServerTargetAddress.getAddress()))
              .build());
    }

    @Override
    public AclLineMatchExpr visitVirtualServerTargetAddress6(
        VirtualServerTargetAddress6 virtualServerTargetAddress6) {
      throw new IllegalArgumentException("Cannot convert IPv6 target");
    }
  }

  @VisibleForTesting
  public static @Nonnull AclLineMatchExpr toMatchExpr(VirtualServer server, String filename) {
    return AclLineMatchExprs.and(
        traceElementForVirtualServer(server, filename),
        VirtualServerTargetToMatchExpr.INSTANCE.visit(server.getTarget()));
  }

  @VisibleForTesting
  public static @Nonnull AclLineMatchExpr toMatchExpr(VirtualServerPort port) {
    Optional<IpProtocol> protocol = toProtocol(port);
    assert protocol.isPresent();
    return AclLineMatchExprs.and(
        traceElementForVirtualServerPort(port),
        AclLineMatchExprs.matchIpProtocol(protocol.get()),
        AclLineMatchExprs.matchDstPort(toIntegerSpace(port)));
  }

  /** Get the {@link LineAction} for the specified {@link AccessList}. */
  private static @Nonnull LineAction toLineAction(AccessListRule rule) {
    if (rule.getAction() == AccessListRule.Action.PERMIT) {
      return LineAction.PERMIT;
    }
    assert rule.getAction() == AccessListRule.Action.DENY;
    return LineAction.DENY;
  }
}
