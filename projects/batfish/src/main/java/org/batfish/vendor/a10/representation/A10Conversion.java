package org.batfish.vendor.a10.representation;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.datamodel.transformation.TransformationStep.assignDestinationIp;
import static org.batfish.datamodel.transformation.TransformationStep.assignDestinationPort;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourceIp;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourcePort;
import static org.batfish.vendor.a10.representation.VirtualServerPort.Type.UDP;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.KernelRoute;
import org.batfish.datamodel.NamedPort;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.VrrpGroup;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.transformation.ApplyAll;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.TransformationStep;

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
  @VisibleForTesting public static final long KERNEL_ROUTE_TAG_NAT_POOL = 1L;
  @VisibleForTesting public static final long KERNEL_ROUTE_TAG_VIRTUAL_SERVER_FLAGGED = 2L;
  @VisibleForTesting public static final long KERNEL_ROUTE_TAG_VIRTUAL_SERVER_UNFLAGGED = 3L;

  /** Set of {@link VirtualServerPort.Type}s that use {@code tcp} protocol */
  static final Set<VirtualServerPort.Type> VIRTUAL_TCP_PORT_TYPES =
      ImmutableSet.of(
          VirtualServerPort.Type.HTTP,
          VirtualServerPort.Type.HTTPS,
          VirtualServerPort.Type.TCP,
          VirtualServerPort.Type.TCP_PROXY);

  /** Set of {@link VirtualServerPort.Type}s that use {@code udp} protocol */
  static final Set<VirtualServerPort.Type> VIRTUAL_UDP_PORT_TYPES =
      ImmutableSet.of(VirtualServerPort.Type.UDP);

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
    assert type == UDP;
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
    return firstNonNull(virtualServer.getEnable(), true);
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
        .filter(
            vs ->
                vs.getPorts().values().stream()
                    .anyMatch(A10Conversion::getVirtualServerPortEnabled))
        .map(A10Conversion::toKernelRoute);
  }

  private static boolean getVirtualServerPortEnabled(VirtualServerPort port) {
    return firstNonNull(port.getEnable(), Boolean.TRUE);
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

  private static boolean getRedistributionFlagged(VirtualServer virtualServer) {
    return firstNonNull(virtualServer.getRedistributionFlagged(), Boolean.FALSE);
  }

  /**
   * Extracts the virtual {@link Ip} of a {@link VirtualServerTarget} - if any - that the device may
   * own.
   */
  private static final class VirtualServerTargetVirtualAddressExtractor
      implements VirtualServerTargetVisitor<Ip> {
    // TODO: this may need to return a set of IPs; or a prefix or an IP.
    private static final VirtualServerTargetVirtualAddressExtractor INSTANCE =
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
}
