package org.batfish.vendor.a10.representation;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.MultipathEquivalentAsPathMatchMode.EXACT_PATH;
import static org.batfish.datamodel.Names.generatedBgpCommonExportPolicyName;
import static org.batfish.datamodel.Names.generatedBgpPeerExportPolicyName;
import static org.batfish.datamodel.Names.generatedBgpRedistributionPolicyName;
import static org.batfish.datamodel.transformation.TransformationStep.assignDestinationIp;
import static org.batfish.datamodel.transformation.TransformationStep.assignDestinationPort;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourceIp;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourcePort;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpTieBreaker;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.KernelRoute;
import org.batfish.datamodel.NamedPort;
import org.batfish.datamodel.Names;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.SubRange;
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
import org.batfish.datamodel.transformation.ApplyAll;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.TransformationStep;
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
          VirtualServerPort.Type.HTTP,
          VirtualServerPort.Type.HTTPS,
          VirtualServerPort.Type.TCP,
          VirtualServerPort.Type.TCP_PROXY);

  /** Set of {@link VirtualServerPort.Type}s that use {@code udp} protocol */
  static final Set<VirtualServerPort.Type> VIRTUAL_UDP_PORT_TYPES =
      ImmutableSet.of(VirtualServerPort.Type.RADIUS, VirtualServerPort.Type.UDP);

  /** Returns the {@link IntegerSpace} representing the specified {@link ServerPort}'s ports. */
  @VisibleForTesting
  @Nonnull
  static IntegerSpace toIntegerSpace(ServerPort port) {
    return IntegerSpace.of(
            new SubRange(
                port.getNumber(),
                port.getNumber() + Optional.ofNullable(port.getRange()).orElse(0)))
        .intersection(IntegerSpace.PORTS);
  }

  /**
   * Returns the {@link IntegerSpace} representing the specified {@link VirtualServerPort}'s ports.
   */
  @VisibleForTesting
  @Nonnull
  static IntegerSpace toIntegerSpace(VirtualServerPort port) {
    return IntegerSpace.of(
            new SubRange(
                port.getNumber(),
                port.getNumber() + Optional.ofNullable(port.getRange()).orElse(0)))
        .intersection(IntegerSpace.PORTS);
  }

  /** Returns the {@link IpProtocol} corresponding to the specified virtual-server port. */
  @VisibleForTesting
  @Nonnull
  static Optional<IpProtocol> toProtocol(VirtualServerPort port) {
    VirtualServerPort.Type type = port.getType();
    if (VIRTUAL_TCP_PORT_TYPES.contains(type)) {
      return Optional.of(IpProtocol.TCP);
    }
    assert VIRTUAL_UDP_PORT_TYPES.contains(type);
    return Optional.of(IpProtocol.UDP);
  }

  /**
   * Returns the match condition, when a flow is destined for the specified virtual-server member
   * (e.g. when NAT should apply for the member).
   */
  @VisibleForTesting
  @Nonnull
  static AclLineMatchExpr toMatchCondition(
      VirtualServerTarget target,
      VirtualServerPort port,
      VirtualServerTargetToIpSpace virtualServerTargetToIpSpace) {
    ImmutableList.Builder<AclLineMatchExpr> exprs =
        ImmutableList.<AclLineMatchExpr>builder()
            .add(AclLineMatchExprs.matchDst(virtualServerTargetToIpSpace.visit(target)))
            .add(AclLineMatchExprs.matchDstPort(toIntegerSpace(port)));
    toProtocol(port).ifPresent(protocol -> exprs.add(AclLineMatchExprs.matchIpProtocol(protocol)));
    return AclLineMatchExprs.and(exprs.build());
  }

  /** Returns the source transformation step for the specified NAT pool. */
  @Nonnull
  static TransformationStep toSnatTransformationStep(NatPool pool) {
    return new ApplyAll(
        assignSourceIp(pool.getStart(), pool.getEnd()),
        assignSourcePort(SNAT_PORT_POOL_START, SNAT_PORT_POOL_END));
  }

  /**
   * Returns the destination transformation steps for the specified service-group. Each step
   * corresponds to the transformation for a single member of the service-group.
   */
  @VisibleForTesting
  @Nonnull
  static List<TransformationStep> toDstTransformationSteps(
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
  @Nonnull
  private static TransformationStep toDstIpTransformationStep(Server server) {
    return assignDestinationIp(ServerTargetToIp.INSTANCE.visit(server.getTarget()));
  }

  /** Returns the destination port transformation step for the specified server. */
  @Nonnull
  private static TransformationStep toDstPortTransformationStep(ServerPort serverPort) {
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

  static boolean isVirtualServerEnabled(VirtualServer virtualServer) {
    return firstNonNull(virtualServer.getEnable(), true)
        && virtualServer.getPorts().values().stream()
            .anyMatch(A10Conversion::isVirtualServerPortEnabled);
  }

  static boolean isVirtualServerPortEnabled(VirtualServerPort port) {
    return firstNonNull(port.getEnable(), true);
  }

  static boolean isVrrpAEnabled(@Nullable VrrpA vrrpA) {
    return Optional.ofNullable(vrrpA)
        .map(VrrpA::getCommon)
        .map(VrrpACommon::getEnable)
        .orElse(false);
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
        .filter(A10Conversion::isVirtualServerEnabled)
        .filter(vs -> vrid == getVirtualServerVrid(vs))
        .map(VirtualServerTargetVirtualAddressExtractor::extractIp);
  }

  /**
   * Get all the IPs (not the subnets) of all enabled {@code slb virtual-server}s assigned to the
   * given ha-group.
   */
  static @Nonnull Stream<Ip> getVirtualServerIpsByHaGroup(
      Collection<VirtualServer> virtualServers, int haGroup) {
    return virtualServers.stream()
        .filter(A10Conversion::isVirtualServerEnabled)
        .filter(vs -> haGroup == getVirtualServerHaGroup(vs))
        .map(VirtualServerTargetVirtualAddressExtractor::extractIp);
  }

  private static int getVirtualServerHaGroup(VirtualServer virtualServer) {
    return firstNonNull(virtualServer.getHaGroup(), DEFAULT_HA_GROUP);
  }

  /** Get all the IPs (not the subnets) of all enabled {@code slb virtual-server}s. */
  static @Nonnull Stream<Ip> getVirtualServerIpsForAllVrids(
      Collection<VirtualServer> virtualServers) {
    return virtualServers.stream()
        .filter(A10Conversion::isVirtualServerEnabled)
        .map(VirtualServerTargetVirtualAddressExtractor::extractIp);
  }

  /** Get all of the the kernel routes generated for all enabled {@code slb virtual-server}s. */
  static @Nonnull Stream<KernelRoute> getVirtualServerKernelRoutes(
      Collection<VirtualServer> virtualServers) {
    return virtualServers.stream()
        .filter(A10Conversion::isVirtualServerEnabled)
        .map(A10Conversion::toKernelRoute);
  }

  @VisibleForTesting
  static @Nonnull KernelRoute toKernelRoute(VirtualServer virtualServer) {
    Ip requiredOwnedIp = VirtualServerTargetVirtualAddressExtractor.extractIp(virtualServer);
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
        .setNetwork(Prefix.create(floatingIp, Prefix.MAX_PREFIX_LENGTH))
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

  /** Extracts the virtual {@link Ip} of a {@link VirtualServerTarget} that the device may own. */
  static final class VirtualServerTargetVirtualAddressExtractor
      implements VirtualServerTargetVisitor<Ip> {
    // TODO: this may need to return a set of IPs; or a prefix or an IP.
    static final VirtualServerTargetVirtualAddressExtractor INSTANCE =
        new VirtualServerTargetVirtualAddressExtractor();

    private static @Nonnull Ip extractIp(VirtualServer virtualServer) {
      return virtualServer.getTarget().accept(INSTANCE);
    }

    @Override
    public Ip visitAddress(VirtualServerTargetAddress address) {
      return address.getAddress();
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
    public Prefix visitAddress(VirtualServerTargetAddress address) {
      return Prefix.create(address.getAddress(), Prefix.MAX_PREFIX_LENGTH);
    }
  }

  private static int getVirtualServerVrid(VirtualServer virtualServer) {
    return Optional.ofNullable(virtualServer.getVrid()).orElse(0);
  }

  /**
   * Create a {@link VrrpGroup.Builder} from the configuration for a {@code vrrp-a vrid} and a set
   * of virtual addresses.
   */
  static @Nonnull VrrpGroup.Builder toVrrpGroupBuilder(
      @Nullable VrrpAVrid vridConfig, Iterable<Ip> virtualAddresses) {
    if (vridConfig == null) {
      return VrrpGroup.builder()
          .setPreempt(DEFAULT_VRRP_A_PREEMPT)
          .setPriority(DEFAULT_VRRP_A_PRIORITY)
          .setVirtualAddresses(virtualAddresses);
    } else {
      return VrrpGroup.builder()
          .setPreempt(getVrrpAVridPreempt(vridConfig))
          .setPriority(getVrrpAVridPriority(vridConfig))
          .setVirtualAddresses(virtualAddresses);
    }
  }

  /**
   * Create a {@link VrrpGroup.Builder} from the configuration for a particular {@code ha group}
   * from the ha configuration and a set of virtual addresses.
   */
  static @Nonnull VrrpGroup.Builder toVrrpGroupBuilder(
      int haGroupId, Ha ha, Iterable<Ip> virtualAddresses) {
    return VrrpGroup.builder()
        .setPreempt(getHaPreemptionEnable(ha))
        .setPriority(getHaGroupPriority(ha.getGroups().get(haGroupId)))
        .setVirtualAddresses(virtualAddresses);
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
   * Convert a map of {@link VrrpGroup.Builder}s to a map of {@link VrrpGroup}s by assigning the
   * primary {@link ConcreteInterfaceAddress} of the given interface as the source-address.
   */
  static @Nonnull SortedMap<Integer, VrrpGroup> toVrrpGroups(
      org.batfish.datamodel.Interface iface, Map<Integer, VrrpGroup.Builder> vrrpGroupBuilders) {
    ConcreteInterfaceAddress sourceAddress = iface.getConcreteAddress();
    assert sourceAddress != null;
    ImmutableSortedMap.Builder<Integer, VrrpGroup> builder = ImmutableSortedMap.naturalOrder();
    vrrpGroupBuilders.forEach(
        (vrid, vrrpGroupBuilder) ->
            builder.put(vrid, vrrpGroupBuilder.setSourceAddress(sourceAddress).build()));
    return builder.build();
  }

  /**
   * Returns a boolean indicating if the specified VI interface should have VRRP configuration
   * associated with it when vrrp-a is enabled.
   */
  static boolean vrrpAEnabledAppliesToInterface(
      org.batfish.datamodel.Interface iface, Set<Ip> peerIps) {
    if (iface.getInterfaceType() == InterfaceType.LOOPBACK) {
      return false;
    }
    return iface.getAllAddresses().stream()
        .filter(ConcreteInterfaceAddress.class::isInstance)
        .map(ConcreteInterfaceAddress.class::cast)
        .map(ConcreteInterfaceAddress::getPrefix)
        .anyMatch(prefix -> peerIps.stream().anyMatch(prefix::containsIp));
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
   * Returns a boolean indicating if the specified VI interface should have VRRP configuration
   * associated with it when ha is enabled.
   */
  static boolean haAppliesToInterface(org.batfish.datamodel.Interface iface, Ip connMirrorIp) {
    if (iface.getInterfaceType() == InterfaceType.LOOPBACK) {
      return false;
    }
    return iface.getAllAddresses().stream()
        .filter(ConcreteInterfaceAddress.class::isInstance)
        .map(ConcreteInterfaceAddress.class::cast)
        .map(ConcreteInterfaceAddress::getPrefix)
        .anyMatch(prefix -> prefix.containsIp(connMirrorIp));
  }

  /** Convert the BGP process and associated routing policies, and attach them to the config. */
  static void createBgpProcess(BgpProcess bgpProcess, Configuration c, Warnings w) {
    Ip routerId = bgpProcess.getRouterId();
    if (routerId == null) {
      w.redFlag("Converting a BgpProcess without an explicit router-id is currently unsupported");
      return;
    }
    org.batfish.datamodel.BgpProcess newBgpProcess =
        org.batfish.datamodel.BgpProcess.builder()
            .setRouterId(routerId)
            .setEbgpAdminCost(DEFAULT_EBGP_ADMIN_COST)
            .setIbgpAdminCost(DEFAULT_IBGP_ADMIN_COST)
            .setLocalAdminCost(DEFAULT_LOCAL_ADMIN_COST)
            .setVrf(c.getDefaultVrf())
            .build();

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
    warnings.redFlag(String.format("BGP neighbor %s: could not determine update source", remoteIp));
    return null;
  }
}
