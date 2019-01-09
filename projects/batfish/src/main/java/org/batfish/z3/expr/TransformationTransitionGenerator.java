package org.batfish.z3.expr;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.z3.expr.ExtractExpr.newExtractExpr;
import static org.batfish.z3.expr.HeaderSpaceMatchExpr.matchPrefix;
import static org.batfish.z3.expr.visitors.Simplifier.simplifyBooleanExpr;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import java.util.List;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.transformation.AssignIpAddressFromPool;
import org.batfish.datamodel.transformation.IpField;
import org.batfish.datamodel.transformation.Noop;
import org.batfish.datamodel.transformation.ShiftIpAddressIntoSubnet;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.TransformationStep;
import org.batfish.datamodel.transformation.TransformationStepVisitor;
import org.batfish.z3.AclLineMatchExprToBooleanExpr;
import org.batfish.z3.Field;

/**
 * Converts {@link Transformation Transformations} to transistions in the NOD reachability engine.
 */
public final class TransformationTransitionGenerator {
  private int _ctr;

  private final String _node1;
  private final String _iface1;

  private final String _node2;
  private final String _iface2;

  private final String _tag; // distinguish this transformation from others on the same interface
  private final AclLineMatchExprToBooleanExpr _aclLineMatchExprToBooleanExpr;
  private final StateExpr _outState;

  // The generated rule statements
  private ImmutableList.Builder<BasicRuleStatement> _statements;

  private TransformationTransitionGenerator(
      String node1,
      String iface1,
      String node2,
      String iface2,
      String tag,
      AclLineMatchExprToBooleanExpr aclLineMatchExprToBooleanExpr,
      StateExpr outState) {
    _ctr = 0;
    _statements = ImmutableList.builder();
    _node1 = node1;
    _iface1 = iface1;
    _node2 = node2;
    _iface2 = iface2;
    _tag = tag;
    _aclLineMatchExprToBooleanExpr = aclLineMatchExprToBooleanExpr;
    _outState = outState;
  }

  private class StepRuleVisitor implements TransformationStepVisitor<BasicRuleStatement> {
    private final StateExpr _preState;
    private final StateExpr _postState;

    StepRuleVisitor(StateExpr preState, StateExpr postState) {
      _preState = preState;
      _postState = postState;
    }

    private Field getField(IpField ipField) {
      switch (ipField) {
        case DESTINATION:
          return Field.DST_IP;
        case SOURCE:
          return Field.SRC_IP;
        default:
          throw new IllegalArgumentException("Unknown IpField: " + ipField);
      }
    }

    @Override
    public BasicRuleStatement visitAssignIpAddressFromPool(
        AssignIpAddressFromPool assignIpAddressFromPool) {
      return new BasicRuleStatement(
          assignFromPoolExpr(
              getField(assignIpAddressFromPool.getIpField()),
              assignIpAddressFromPool.getPoolStart(),
              assignIpAddressFromPool.getPoolEnd()),
          _preState,
          _postState);
    }

    @Override
    public BasicRuleStatement visitNoop(Noop noop) {
      return new BasicRuleStatement(_preState, _postState);
    }

    @Override
    public BasicRuleStatement visitShiftIpAddressIntoSubnet(
        ShiftIpAddressIntoSubnet shiftIpAddressIntoSubnet) {
      return new BasicRuleStatement(
          shiftIntoSubnetExpr(
              getField(shiftIpAddressIntoSubnet.getIpField()),
              shiftIpAddressIntoSubnet.getSubnet()),
          _preState,
          _postState);
    }
  }

  /** Generate a new StateExpr for this transformation, and transitions from it to the end state. */
  private StateExpr generateTransitions(Transformation transformation) {
    int id = _ctr++;
    StateExpr state = new TransformationExpr(_node1, _iface1, _node2, _iface2, _tag, id);

    BooleanExpr guardExpr =
        simplifyBooleanExpr(
            _aclLineMatchExprToBooleanExpr.toBooleanExpr(transformation.getGuard()));
    BooleanExpr notGuardExpr =
        guardExpr == TrueExpr.INSTANCE ? FalseExpr.INSTANCE : new NotExpr(guardExpr);

    StateExpr andThen =
        transformation.getAndThen() == null
            ? _outState
            : generateTransitions(transformation.getAndThen());
    StateExpr orElse =
        transformation.getOrElse() == null
            ? _outState
            : generateTransitions(transformation.getOrElse());

    StateExpr stepsExpr =
        generateStepTransitions(id, transformation.getTransformationSteps(), andThen);

    _statements.add(new BasicRuleStatement(guardExpr, state, stepsExpr));
    _statements.add(new BasicRuleStatement(notGuardExpr, state, orElse));
    return state;
  }

  private StateExpr generateStepTransitions(
      int transformationId, List<TransformationStep> transformationSteps, StateExpr andThen) {
    StateExpr next = andThen;
    int stepId = transformationSteps.size() - 1;
    for (TransformationStep step : Lists.reverse(transformationSteps)) {
      StateExpr stepExpr =
          new TransformationStepExpr(
              _node1, _iface1, _node2, _iface2, _tag, transformationId, stepId);
      _statements.add(step.accept(new StepRuleVisitor(stepExpr, next)));
      next = stepExpr;
      stepId--;
    }
    return next;
  }

  public static List<BasicRuleStatement> generateTransitions(
      String node1,
      String iface1,
      String node2,
      String iface2,
      String tag,
      AclLineMatchExprToBooleanExpr aclLineMatchExprToBooleanExpr,
      StateExpr preState,
      StateExpr postState,
      @Nullable Transformation transformation) {
    if (transformation == null) {
      return ImmutableList.of(new BasicRuleStatement(preState, postState));
    }

    TransformationTransitionGenerator gen =
        new TransformationTransitionGenerator(
            node1, iface1, node2, iface2, tag, aclLineMatchExprToBooleanExpr, postState);
    StateExpr stateExpr = gen.generateTransitions(transformation);
    gen._statements.add(new BasicRuleStatement(preState, stateExpr));
    return gen._statements.build();
  }

  @VisibleForTesting
  static BooleanExpr shiftIntoSubnetExpr(Field field, Prefix subnet) {
    checkArgument(
        subnet.getPrefixLength() < Prefix.MAX_PREFIX_LENGTH,
        "subnet prefix must be less than the maximum prefix length");

    TransformedVarIntExpr transformedField = new TransformedVarIntExpr(field);

    // The shifting constraint has two parts: the transformed field is in the subnet, and
    // all the bits that don't correspond to the subnet are preserved.
    BooleanExpr shiftExpr = matchPrefix(subnet, transformedField);

    int high = Prefix.MAX_PREFIX_LENGTH - subnet.getPrefixLength() - 1;
    BooleanExpr preservedExpr =
        new EqExpr(newExtractExpr(transformedField, 0, high), newExtractExpr(field, 0, high));

    return new AndExpr(ImmutableList.of(shiftExpr, preservedExpr));
  }

  @VisibleForTesting
  static BooleanExpr assignFromPoolExpr(Field field, Ip poolStart, Ip poolEnd) {
    return new RangeMatchExpr(
        new TransformedVarIntExpr(field),
        field.getSize(),
        ImmutableSet.of(Range.closed(poolStart.asLong(), poolEnd.asLong())));
  }
}
