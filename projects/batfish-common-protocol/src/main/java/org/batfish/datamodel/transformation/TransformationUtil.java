package org.batfish.datamodel.transformation;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.acl.AclLineMatchExprs.TRUE;
import static org.batfish.datamodel.transformation.Transformation.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.DestinationNat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.SourceNat;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.PermittedByAcl;

/** Utility methods related to {@link Transformation}. */
@ParametersAreNonnullByDefault
public final class TransformationUtil {
  private TransformationUtil() {}

  public static Transformation fromDestinationNats(@Nullable List<DestinationNat> destinationNats) {
    if (destinationNats == null) {
      return null;
    }

    Transformation transformation = null;
    for (DestinationNat nat : Lists.reverse(destinationNats)) {
      transformation =
          when(matchCondition(nat.getAcl()))
              .apply(
                  assignFromPoolOrDoNothing(
                      IpField.DESTINATION, nat.getPoolIpFirst(), nat.getPoolIpLast()))
              .setOrElse(transformation)
              .build();
    }
    return transformation;
  }

  public static Transformation fromSourceNats(@Nullable List<SourceNat> sourceNats) {
    if (sourceNats == null) {
      return null;
    }

    Transformation transformation = null;
    for (SourceNat nat : Lists.reverse(sourceNats)) {
      transformation =
          when(matchCondition(nat.getAcl()))
              .apply(
                  assignFromPoolOrDoNothing(
                      IpField.SOURCE, nat.getPoolIpFirst(), nat.getPoolIpLast()))
              .setOrElse(transformation)
              .build();
    }
    return transformation;
  }

  private static List<TransformationStep> assignFromPoolOrDoNothing(
      IpField ipField, @Nullable Ip poolFirst, @Nullable Ip poolLast) {
    return poolFirst == null || poolLast == null
        ? ImmutableList.of()
        : ImmutableList.of(new AssignIpAddressFromPool(ipField, poolFirst, poolLast));
  }

  private static AclLineMatchExpr matchCondition(@Nullable IpAccessList acl) {
    checkArgument(acl == null || acl.getName() != null, "NAT ACLs must be named");
    return acl == null ? TRUE : new PermittedByAcl(acl.getName());
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
