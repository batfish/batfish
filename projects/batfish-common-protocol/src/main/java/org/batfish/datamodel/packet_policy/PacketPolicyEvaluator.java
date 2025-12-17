package org.batfish.datamodel.packet_policy;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.FibForward;
import org.batfish.datamodel.FibNextVrf;
import org.batfish.datamodel.FibNullRoute;
import org.batfish.datamodel.FilterResult;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.acl.Evaluator;
import org.batfish.datamodel.flow.FilterStep;
import org.batfish.datamodel.flow.FilterStep.FilterType;
import org.batfish.datamodel.flow.Step;
import org.batfish.datamodel.flow.StepAction;
import org.batfish.datamodel.transformation.TransformationEvaluator;
import org.batfish.datamodel.transformation.TransformationStep;
import org.batfish.datamodel.visitors.FibActionVisitor;

/**
 * Evaluates a {@link PacketPolicy} against a given {@link Flow}.
 *
 * <p>To evaluate an entire policy, see {@link #evaluate(Flow, String, String, PacketPolicy, Map,
 * Map, Map)} which will return a {@link PacketPolicyResult}.
 */
public final class PacketPolicyEvaluator {

  private final @Nonnull Map<String, IpAccessList> _availableAcls;
  private final @Nonnull Map<String, IpSpace> _namedIpSpaces;

  // Start state
  private final @Nonnull String _srcInterface;
  private final @Nonnull String _srcInterfaceVrf;

  /** Vrf name to FIB mapping */
  private final @Nonnull Map<String, Fib> _fibs;

  private final @Nonnull ImmutableList.Builder<Step<?>> _traceSteps;

  // Modified state
  private @Nonnull Flow.Builder _currentFlow;

  // Expr and stmt visitors
  private final @Nonnull BoolExprEvaluator _boolExprEvaluator = new BoolExprEvaluator();
  private final @Nonnull StatementEvaluator _stmtEvaluator = new StatementEvaluator();
  private final @Nonnull VrfExprEvaluator _vrfExprEvaluator = new VrfExprEvaluator();

  private final class BoolExprEvaluator implements BoolExprVisitor<Boolean> {

    @Override
    public Boolean visitPacketMatchExpr(PacketMatchExpr expr) {
      return Evaluator.matches(
          expr.getExpr(), _currentFlow.build(), _srcInterface, _availableAcls, _namedIpSpaces);
    }

    @Override
    public Boolean visitTrueExpr(TrueExpr expr) {
      return true;
    }

    @Override
    public Boolean visitFalseExpr(FalseExpr expr) {
      return false;
    }

    @Override
    public Boolean visitFibLookupOutgoingInterfaceIsOneOf(FibLookupOutgoingInterfaceIsOneOf expr) {
      Fib fib = _fibs.get(_vrfExprEvaluator.visit(expr.getVrf()));
      if (fib == null) {
        return false;
      }

      FibActionVisitor<Boolean> actionVisitor =
          new FibActionVisitor<Boolean>() {
            @Override
            public Boolean visitFibForward(FibForward fibForward) {
              return expr.getInterfaceNames().contains(fibForward.getInterfaceName());
            }

            @Override
            public Boolean visitFibNextVrf(FibNextVrf fibNextVrf) {
              // Recurse and continue interface collection
              // Use override IP if present, otherwise use packet destination IP
              return _fibs
                  .get(fibNextVrf.getNextVrf())
                  .get(firstNonNull(fibNextVrf.getIp(), _currentFlow.getDstIp()))
                  .stream()
                  .anyMatch(entry -> entry.getAction().accept(this));
            }

            @Override
            public Boolean visitFibNullRoute(FibNullRoute fibNullRoute) {
              // nothing to do
              return false;
            }
          };

      return fib.get(_currentFlow.getDstIp()).stream()
          .anyMatch(entry -> entry.getAction().accept(actionVisitor));
    }

    @Override
    public Boolean visitConjunction(Conjunction expr) {
      return expr.getConjuncts().stream().allMatch(c -> c.accept(this));
    }
  }

  /**
   * Evaluates statements in a packet policy. Can return {@code null} if the statement did not
   * execute (e.g., an IF that did not match, or there was no action to return
   */
  private final class StatementEvaluator implements StatementVisitor<Action> {
    StatementEvaluator() {}

    @Override
    public @Nullable Action visitIf(If ifStmt) {
      if (ifStmt.getMatchCondition().accept(_boolExprEvaluator)) {
        return ifStmt.getTrueStatements().stream()
            .map(this::visit)
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
      }
      return null;
    }

    @Override
    public Action visitReturn(Return returnStmt) {
      return returnStmt.getAction();
    }

    @Override
    public Action visitApplyFilter(ApplyFilter applyFilter) {
      IpAccessList acl = _availableAcls.get(applyFilter.getFilter());
      Flow flow = _currentFlow.build();
      FilterResult filterResult = acl.filter(flow, _srcInterface, _availableAcls, _namedIpSpaces);
      boolean denied = filterResult.getAction() == LineAction.DENY;

      // Create filter step
      // TODO What if policy applies an ACL between transformations? Does that happen?
      FilterType filterType =
          _traceSteps.build().stream().anyMatch(TransformationStep.class::isInstance)
              ? FilterType.POST_TRANSFORMATION_INGRESS_FILTER
              : FilterType.INGRESS_FILTER;
      _traceSteps.add(
          new FilterStep(
              new FilterStep.FilterStepDetail(acl.getName(), filterType, _srcInterface, flow),
              denied ? StepAction.DENIED : StepAction.PERMITTED));

      return denied ? Drop.instance() : null;
    }

    @Override
    public Action visitApplyTransformation(ApplyTransformation transformation) {
      TransformationEvaluator.TransformationResult result =
          TransformationEvaluator.eval(
              transformation.getTransformation(),
              _currentFlow.build(),
              _srcInterface,
              _availableAcls,
              _namedIpSpaces);
      _currentFlow = result.getOutputFlow().toBuilder();
      _traceSteps.addAll(result.getTraceSteps());
      return null;
    }
  }

  private PacketPolicyEvaluator(
      Flow originalFlow,
      String srcInterface,
      String srcInterfaceVrf,
      Map<String, IpAccessList> availableAcls,
      Map<String, IpSpace> namedIpSpaces,
      Map<String, Fib> fibs) {
    _currentFlow = originalFlow.toBuilder();
    _srcInterface = srcInterface;
    _srcInterfaceVrf = srcInterfaceVrf;
    _availableAcls = availableAcls;
    _namedIpSpaces = namedIpSpaces;
    _fibs = fibs;
    _traceSteps = ImmutableList.builder();
  }

  private @Nonnull Flow getTransformedFlow() {
    return _currentFlow.build();
  }

  private PacketPolicyResult evaluate(PacketPolicy policy) {
    Action action =
        policy.getStatements().stream()
            .map(_stmtEvaluator::visit)
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(policy.getDefaultAction().getAction());
    return new PacketPolicyResult(getTransformedFlow(), action, _traceSteps.build());
  }

  public static PacketPolicyResult evaluate(
      Flow f,
      String srcInterface,
      String srcInterfaceVrf,
      PacketPolicy policy,
      Map<String, IpAccessList> availableAcls,
      Map<String, IpSpace> namedIpSpaces,
      Map<String, Fib> fibs) {
    return new PacketPolicyEvaluator(
            f, srcInterface, srcInterfaceVrf, availableAcls, namedIpSpaces, fibs)
        .evaluate(policy);
  }

  /**
   * Combination of final (possibly transformed) {@link Flow} and the action taken by the evaluated
   * {@link PacketPolicy}
   */
  public static final class PacketPolicyResult {
    private final @Nonnull List<Step<?>> _traceSteps;
    private final @Nonnull Flow _finalFlow;
    private final @Nonnull Action _action;

    PacketPolicyResult(Flow finalFlow, Action action, List<Step<?>> traceSteps) {
      _finalFlow = finalFlow;
      _action = action;
      _traceSteps = ImmutableList.copyOf(traceSteps);
    }

    public @Nonnull Flow getFinalFlow() {
      return _finalFlow;
    }

    public @Nonnull Action getAction() {
      return _action;
    }

    public @Nonnull List<Step<?>> getTraceSteps() {
      return _traceSteps;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      PacketPolicyResult that = (PacketPolicyResult) o;
      return _finalFlow.equals(that.getFinalFlow())
          && _action.equals(that.getAction())
          && _traceSteps.equals(that.getTraceSteps());
    }

    @Override
    public int hashCode() {
      return Objects.hash(_finalFlow, _action, _traceSteps);
    }
  }

  private final class VrfExprEvaluator implements VrfExprVisitor<String> {

    @Override
    public String visitLiteralVrfName(LiteralVrfName expr) {
      return expr.getVrfName();
    }

    @Override
    public String visitIngressInterfaceVrf(IngressInterfaceVrf expr) {
      return _srcInterfaceVrf;
    }
  }
}
