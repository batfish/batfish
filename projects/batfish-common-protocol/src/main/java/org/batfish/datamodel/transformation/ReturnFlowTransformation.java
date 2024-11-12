package org.batfish.datamodel.transformation;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.datamodel.acl.AclLineMatchExprs.FALSE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.TRUE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.or;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.Objects;
import java.util.Optional;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.acl.DeniedByAcl;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.GenericAclLineMatchExprVisitor;
import org.batfish.datamodel.acl.MatchDestinationIp;
import org.batfish.datamodel.acl.MatchDestinationPort;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.MatchIpProtocol;
import org.batfish.datamodel.acl.MatchSourceIp;
import org.batfish.datamodel.acl.MatchSourcePort;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.acl.NotMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.datamodel.acl.OriginatingFromDevice;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.acl.TrueExpr;

/**
 * Converts a {@link Transformation} to one in which transformation all src/dst fields have been
 * flipped (i.e. in guard expressions and transformation steps).
 */
public final class ReturnFlowTransformation {
  private ReturnFlowTransformation() {}

  // StepVisitor switches the Transformation to the opposite IpField.
  private static final class StepVisitor implements TransformationStepVisitor<TransformationStep> {
    @Override
    public TransformationStep visitAssignIpAddressFromPool(AssignIpAddressFromPool step) {
      return new AssignIpAddressFromPool(
          step.getType(), step.getIpField().opposite(), step.getIpRanges());
    }

    @Override
    public TransformationStep visitNoop(Noop noop) {
      return noop;
    }

    @Override
    public TransformationStep visitShiftIpAddressIntoSubnet(ShiftIpAddressIntoSubnet step) {
      return new ShiftIpAddressIntoSubnet(
          step.getType(), step.getIpField().opposite(), step.getSubnet());
    }

    @Override
    public TransformationStep visitAssignPortFromPool(AssignPortFromPool step) {
      return new AssignPortFromPool(
          step.getType(), step.getPortField().opposite(), step.getPoolStart(), step.getPoolEnd());
    }

    @Override
    public TransformationStep visitApplyAll(ApplyAll applyAll) {
      return new ApplyAll(
          applyAll.getSteps().stream()
              .map(step -> step.accept(this))
              .collect(ImmutableList.toImmutableList()));
    }

    @Override
    public TransformationStep visitApplyAny(ApplyAny applyAny) {
      return new ApplyAny(
          applyAny.getSteps().stream()
              .map(step -> step.accept(this))
              .collect(ImmutableList.toImmutableList()));
    }
  }

  /* GuardVisitor does two things:
   * 1. Swaps src/dst ip/port fields in an AclLineMatchExpr.
   * 2. Removes any source constraints (OriginatingFromDevice, MatchSrcInterface).
   *
   */
  private static final class GuardVisitor
      implements GenericAclLineMatchExprVisitor<AclLineMatchExpr> {
    @Override
    public AclLineMatchExpr visitAndMatchExpr(AndMatchExpr andMatchExpr) {
      if (andMatchExpr.getConjuncts().isEmpty()) {
        return TRUE;
      }
      ImmutableList<AclLineMatchExpr> conjuncts =
          andMatchExpr.getConjuncts().stream()
              .map(this::visit)
              .filter(Objects::nonNull)
              .collect(ImmutableList.toImmutableList());
      return conjuncts.isEmpty() ? null : and(conjuncts);
    }

    @Override
    public AclLineMatchExpr visitDeniedByAcl(DeniedByAcl deniedByAcl) {
      /* We can't handle this currently, since we'd need to create the return flow ACL for the
       * referenced ACL.
       */
      throw new IllegalArgumentException("DeniedByAcl is not allowed in a Transformation guard");
    }

    @Override
    public AclLineMatchExpr visitFalseExpr(FalseExpr falseExpr) {
      return falseExpr;
    }

    @Override
    public AclLineMatchExpr visitMatchDestinationIp(MatchDestinationIp matchDestinationIp) {
      return AclLineMatchExprs.matchSrc(matchDestinationIp.getIps());
    }

    @Override
    public AclLineMatchExpr visitMatchDestinationPort(MatchDestinationPort matchDestinationPort) {
      return AclLineMatchExprs.matchSrcPort(matchDestinationPort.getPorts());
    }

    @Override
    public AclLineMatchExpr visitMatchHeaderSpace(MatchHeaderSpace matchHeaderSpace) {
      HeaderSpace forwardHeaderSpace = matchHeaderSpace.getHeaderspace();
      return new MatchHeaderSpace(
          forwardHeaderSpace.toBuilder()
              .setSrcIps(forwardHeaderSpace.getDstIps())
              .setDstIps(forwardHeaderSpace.getSrcIps())
              .setSrcPorts(forwardHeaderSpace.getDstPorts())
              .setDstPorts(forwardHeaderSpace.getSrcPorts())
              .setNotSrcIps(forwardHeaderSpace.getNotDstIps())
              .setNotDstIps(forwardHeaderSpace.getNotSrcIps())
              .setNotSrcPorts(forwardHeaderSpace.getNotDstPorts())
              .setNotDstPorts(forwardHeaderSpace.getNotSrcPorts())
              .build());
    }

    @Override
    public AclLineMatchExpr visitMatchIpProtocol(MatchIpProtocol matchIpProtocol) {
      return matchIpProtocol;
    }

    @Override
    public AclLineMatchExpr visitMatchSourceIp(MatchSourceIp matchSourceIp) {
      return AclLineMatchExprs.matchDst(matchSourceIp.getIps());
    }

    @Override
    public AclLineMatchExpr visitMatchSourcePort(MatchSourcePort matchSourcePort) {
      return AclLineMatchExprs.matchDstPort(matchSourcePort.getPorts());
    }

    @Override
    public AclLineMatchExpr visitMatchSrcInterface(MatchSrcInterface matchSrcInterface) {
      /* This is unexpected to occur in a Transformation guard expression, but we'll handle it
       * anyway. The return flow is not expected to enter the same source interface(s) as the
       * forward flow, so just remove this constraint by replacing it with null.
       */
      return null;
    }

    @Override
    public AclLineMatchExpr visitNotMatchExpr(NotMatchExpr notMatchExpr) {
      return Optional.ofNullable(visit(notMatchExpr.getOperand()))
          .map(AclLineMatchExprs::not)
          .orElse(null);
    }

    @Override
    public AclLineMatchExpr visitOriginatingFromDevice(
        OriginatingFromDevice originatingFromDevice) {
      /* This is unexpected to occur in a Transformation guard expression, but we'll handle it
       * anyway. The return flow is not expected to enter the same source interface(s) as the
       * forward flow, so just remove this constraint by replacing it with null.
       */
      return null;
    }

    @Override
    public AclLineMatchExpr visitOrMatchExpr(OrMatchExpr orMatchExpr) {
      if (orMatchExpr.getDisjuncts().isEmpty()) {
        return FALSE;
      }
      ImmutableList<AclLineMatchExpr> disjuncts =
          orMatchExpr.getDisjuncts().stream()
              .map(this::visit)
              .filter(Objects::nonNull)
              .collect(ImmutableList.toImmutableList());
      return disjuncts.isEmpty() ? null : or(disjuncts);
    }

    @Override
    public AclLineMatchExpr visitPermittedByAcl(PermittedByAcl permittedByAcl) {
      /* We can't handle this currently, since we'd need to create the return flow ACL for the
       * referenced ACL.
       */
      throw new IllegalArgumentException("PermittedByAcl is not allowed in a Transformation guard");
    }

    @Override
    public AclLineMatchExpr visitTrueExpr(TrueExpr trueExpr) {
      return trueExpr;
    }
  }

  @VisibleForTesting static final GuardVisitor GUARD_VISITOR = new GuardVisitor();

  @VisibleForTesting static final StepVisitor STEP_VISITOR = new StepVisitor();

  public static Transformation returnFlowTransformation(Transformation forwardFlowTransformation) {
    return forwardFlowTransformation == null
        ? null
        : Transformation.when(
                firstNonNull(forwardFlowTransformation.getGuard().accept(GUARD_VISITOR), TRUE))
            .apply(
                forwardFlowTransformation.getTransformationSteps().stream()
                    .map(STEP_VISITOR::visit)
                    .collect(ImmutableList.toImmutableList()))
            .setAndThen(returnFlowTransformation(forwardFlowTransformation.getAndThen()))
            .setOrElse(returnFlowTransformation(forwardFlowTransformation.getOrElse()))
            .build();
  }
}
