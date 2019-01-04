package org.batfish.datamodel.transformation;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.acl.AclLineMatchExprs.TRUE;
import static org.batfish.datamodel.transformation.Transformation.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.DestinationNat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.SourceNat;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.PermittedByAcl;

/** Utility methods related to {@link Transformation}. */
public final class TransformationUtil {
  private TransformationUtil() {}

  public static Transformation fromDestinationNats(List<DestinationNat> destinationNats) {
    return destinationNats == null
        ? null
        : Lists.reverse(destinationNats)
            .stream()
            .reduce(
                null,
                (orElse, nat) ->
                    when(matchCondition(nat.getAcl()))
                        .apply(
                            assignFromPoolOrDoNothing(
                                IpField.DESTINATION, nat.getPoolIpFirst(), nat.getPoolIpLast()))
                        .setOrElse(orElse)
                        .build(),
                // combiner only used for parallel streams
                TransformationUtil::illegalCombiner);
  }

  public static Transformation fromSourceNats(List<SourceNat> sourceNats) {
    return sourceNats == null
        ? null
        : Lists.reverse(sourceNats)
            .stream()
            .reduce(
                null,
                (orElse, nat) ->
                    when(matchCondition(nat.getAcl()))
                        .apply(
                            assignFromPoolOrDoNothing(
                                IpField.SOURCE, nat.getPoolIpFirst(), nat.getPoolIpLast()))
                        .setOrElse(orElse)
                        .build(),
                // combiner only used for parallel streams
                TransformationUtil::illegalCombiner);
  }

  private static List<TransformationStep> assignFromPoolOrDoNothing(
      IpField ipField, Ip poolFirst, Ip poolLast) {
    return poolFirst == null || poolLast == null
        ? ImmutableList.of()
        : ImmutableList.of(new AssignIpAddressFromPool(ipField, poolFirst, poolLast));
  }

  @SuppressWarnings("PMD")
  private static Transformation illegalCombiner(Transformation t1, Transformation t2) {
    throw new BatfishException("Cannot convert NATs to Transformations in parallel");
  }

  private static AclLineMatchExpr matchCondition(IpAccessList acl) {
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
