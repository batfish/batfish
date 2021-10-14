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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.NamedPort;
import org.batfish.datamodel.SubRange;
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
}
