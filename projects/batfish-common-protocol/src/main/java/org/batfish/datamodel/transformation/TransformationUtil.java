package org.batfish.datamodel.transformation;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.acl.AclLineMatchExprs.TRUE;
import static org.batfish.datamodel.flow.TransformationStep.TransformationType.DEST_NAT;
import static org.batfish.datamodel.flow.TransformationStep.TransformationType.SOURCE_NAT;
import static org.batfish.datamodel.transformation.IpField.DESTINATION;
import static org.batfish.datamodel.transformation.IpField.SOURCE;
import static org.batfish.datamodel.transformation.Noop.NOOP_DEST_NAT;
import static org.batfish.datamodel.transformation.Noop.NOOP_SOURCE_NAT;
import static org.batfish.datamodel.transformation.Transformation.always;
import static org.batfish.datamodel.transformation.Transformation.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import java.util.List;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.DestinationNat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.SourceNat;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.flow.TransformationStep.TransformationType;

/** Utility methods related to {@link Transformation}. */
@ParametersAreNonnullByDefault
public final class TransformationUtil {
  private TransformationUtil() {}

  private static final TransformationStepVisitor<Boolean> IS_SOURCE_NAT =
      new TransformationStepVisitor<Boolean>() {
        @Override
        public Boolean visitAssignIpAddressFromPool(
            AssignIpAddressFromPool assignIpAddressFromPool) {
          return assignIpAddressFromPool.getType() == SOURCE_NAT;
        }

        @Override
        public Boolean visitNoop(Noop noop) {
          return false;
        }

        @Override
        public Boolean visitShiftIpAddressIntoSubnet(
            ShiftIpAddressIntoSubnet shiftIpAddressIntoSubnet) {
          return shiftIpAddressIntoSubnet.getType() == SOURCE_NAT;
        }
      };

  private static final TransformationStepVisitor<Stream<Ip>> SOURCE_NAT_POOL_IPS =
      new TransformationStepVisitor<Stream<Ip>>() {
        @Override
        public Stream<Ip> visitAssignIpAddressFromPool(
            AssignIpAddressFromPool assignIpAddressFromPool) {
          return assignIpAddressFromPool.getType() != SOURCE_NAT
              ? Stream.of()
              : LongStream.range(
                      assignIpAddressFromPool.getPoolStart().asLong(),
                      assignIpAddressFromPool.getPoolEnd().asLong() + 1)
                  .mapToObj(Ip::create);
        }

        @Override
        public Stream<Ip> visitNoop(Noop noop) {
          return Stream.of();
        }

        @Override
        public Stream<Ip> visitShiftIpAddressIntoSubnet(
            ShiftIpAddressIntoSubnet shiftIpAddressIntoSubnet) {
          // not a pool
          return Stream.of();
        }
      };

  public static Transformation fromDestinationNats(@Nullable List<DestinationNat> destinationNats) {
    if (destinationNats == null || destinationNats.isEmpty()) {
      return null;
    }

    // Always include in the trace that we went through the NAT.
    Transformation transformation = always().apply(NOOP_DEST_NAT).build();
    for (DestinationNat nat : Lists.reverse(destinationNats)) {
      transformation =
          when(matchCondition(nat.getAcl()))
              .apply(
                  assignFromPoolOrNoop(
                      DEST_NAT, DESTINATION, nat.getPoolIpFirst(), nat.getPoolIpLast()))
              .setOrElse(transformation)
              .build();
    }
    return transformation;
  }

  public static Transformation fromSourceNats(@Nullable List<SourceNat> sourceNats) {
    if (sourceNats == null || sourceNats.isEmpty()) {
      return null;
    }

    // Always include in the trace that we went through the NAT.
    Transformation transformation = always().apply(NOOP_SOURCE_NAT).build();
    for (SourceNat nat : Lists.reverse(sourceNats)) {
      transformation =
          when(matchCondition(nat.getAcl()))
              .apply(
                  assignFromPoolOrNoop(
                      SOURCE_NAT, SOURCE, nat.getPoolIpFirst(), nat.getPoolIpLast()))
              .setOrElse(transformation)
              .build();
    }
    return transformation;
  }

  private static List<TransformationStep> assignFromPoolOrNoop(
      TransformationType type, IpField ipField, @Nullable Ip poolFirst, @Nullable Ip poolLast) {
    return poolFirst == null || poolLast == null
        ? ImmutableList.of(new Noop(type))
        : ImmutableList.of(new AssignIpAddressFromPool(type, ipField, poolFirst, poolLast));
  }

  public static boolean hasSourceNat(@Nullable Transformation transformation) {
    if (transformation == null) {
      return false;
    }

    return transformation.getTransformationSteps().stream().anyMatch(IS_SOURCE_NAT::visit)
        || hasSourceNat(transformation.getAndThen())
        || hasSourceNat(transformation.getOrElse());
  }

  private static AclLineMatchExpr matchCondition(@Nullable IpAccessList acl) {
    checkArgument(acl == null || acl.getName() != null, "NAT ACLs must be named");
    return acl == null ? TRUE : new PermittedByAcl(acl.getName());
  }

  public static Stream<Ip> sourceNatPoolIps(@Nullable Transformation transformation) {
    return transformation == null
        ? Stream.of()
        : Streams.concat(
            transformation.getTransformationSteps().stream().flatMap(SOURCE_NAT_POOL_IPS::visit),
            sourceNatPoolIps(transformation.getAndThen()),
            sourceNatPoolIps(transformation.getOrElse()));
  }

  /**
   * Visit all transformation steps in a (nested) {@link Transformation}. First visit the steps of
   * the root transformation, then recursively visit the andThen transformation, then visit the
   * orElse transformation.
   */
  public static void visitTransformationSteps(
      @Nullable Transformation transformation, TransformationStepVisitor<Void> stepVisitor) {
    if (transformation == null) {
      return;
    }

    transformation.getTransformationSteps().forEach(stepVisitor::visit);
    visitTransformationSteps(transformation.getAndThen(), stepVisitor);
    visitTransformationSteps(transformation.getOrElse(), stepVisitor);
  }
}
