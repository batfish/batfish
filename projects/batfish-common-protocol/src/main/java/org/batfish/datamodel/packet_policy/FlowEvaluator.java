package org.batfish.datamodel.packet_policy;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.FibEntry;
import org.batfish.datamodel.FibForward;
import org.batfish.datamodel.FibNextVrf;
import org.batfish.datamodel.FibNullRoute;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.acl.Evaluator;
import org.batfish.datamodel.transformation.TransformationEvaluator;
import org.batfish.datamodel.visitors.FibActionVisitor;

/**
 * Evaluates a {@link PacketPolicy} against a given {@link Flow}.
 *
 * <p>To evaluate an entire policy, see {@link #evaluate(Flow, String, String, PacketPolicy, Map,
 * Map, Map)} which will return a {@link FlowResult}.
 */
public final class FlowEvaluator {

  private final @Nonnull Map<String, IpAccessList> _availableAcls;
  private final @Nonnull Map<String, IpSpace> _namedIpSpaces;

  // Start state
  @Nonnull private final String _srcInterface;
  @Nonnull private final String _srcInterfaceVrf;
  /** Vrf name to FIB mapping */
  @Nonnull private final Map<String, Fib> _fibs;

  // Modified state
  @Nonnull private Flow.Builder _currentFlow;

  // Expr and stmt visitors
  @Nonnull private BoolExprEvaluator _boolExprEvaluator = new BoolExprEvaluator();
  @Nonnull private StatementEvaluator _stmtEvaluator = new StatementEvaluator();
  @Nonnull private VrfExprEvaluator _vrfExprEvaluator = new VrfExprEvaluator();

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
    public Boolean visitFibLookupOutgoingInterfaceIsOneOf(
        FibLookupOutgoingInterfaceIsOneOf expr) {
      String vrf = _vrfExprEvaluator.visit(expr.getVrf());
      Fib fib = _fibs.get(vrf);
      if (fib == null) {
        return false;
      }

      // Collect entries
      Set<FibEntry> entries = fib.get(_currentFlow.getDstIp());
      // Set of all interfaces the lookup resolves to
      ImmutableSet.Builder<String> outgoingInterfaces = ImmutableSet.builder();

      FibActionVisitor<Void> actionVisitor =
          new FibActionVisitor<Void>() {
            @Override
            public Void visitFibForward(FibForward fibForward) {
              outgoingInterfaces.add(fibForward.getInterfaceName());
              return null;
            }

            @Override
            public Void visitFibNextVrf(FibNextVrf fibNextVrf) {
              Fib innerFib = _fibs.get(fibNextVrf.getNextVrf());
              Set<FibEntry> innerEntries = innerFib.get(_currentFlow.getDstIp());
              // Recurse and continue interface collection
              innerEntries.forEach(entry -> entry.getAction().accept(this));
              return null;
            }

            @Override
            public Void visitFibNullRoute(FibNullRoute fibNullRoute) {
              // nothing to do
              return null;
            }
          };

      entries.forEach(entry -> entry.getAction().accept(actionVisitor));
      return !Sets.intersection(outgoingInterfaces.build(), expr.getInterfaceNames()).isEmpty();
    }
  }

  /**
   * Evaluates statements in a packet policy. Can return {@code null} if the statement did not
   * execute (e.g., an IF that did not match, or there was no action to return
   */
  private final class StatementEvaluator implements StatementVisitor<Action> {
    StatementEvaluator() {}

    @Override
    @Nullable
    public Action visitIf(If ifStmt) {
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
    public Action visitApplyTransformation(ApplyTransformation transformation) {
      _currentFlow =
          TransformationEvaluator.eval(
                  transformation.getTransformation(),
                  _currentFlow.build(),
                  _srcInterface,
                  _availableAcls,
                  _namedIpSpaces)
              .getOutputFlow()
              .toBuilder();
      return null;
    }
  }

  private FlowEvaluator(
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
  }

  @Nonnull
  private Flow getTransformedFlow() {
    return _currentFlow.build();
  }

  private FlowResult evaluate(PacketPolicy policy) {
    Action action =
        policy.getStatements().stream()
            .map(_stmtEvaluator::visit)
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(policy.getDefaultAction().getAction());
    return new FlowResult(getTransformedFlow(), action);
  }

  public static FlowResult evaluate(
      Flow f,
      String srcInterface,
      String srcInterfaceVrf,
      PacketPolicy policy,
      Map<String, IpAccessList> availableAcls,
      Map<String, IpSpace> namedIpSpaces,
      Map<String, Fib> fibs) {
    return new FlowEvaluator(f, srcInterface, srcInterfaceVrf, availableAcls, namedIpSpaces, fibs)
        .evaluate(policy);
  }

  /** Combination of final (possibly transformed) {@link Flow} and the action taken */
  public static final class FlowResult {
    private final Flow _finalFlow;
    private final Action _action;

    FlowResult(Flow finalFlow, Action action) {
      _finalFlow = finalFlow;
      _action = action;
    }

    public Flow getFinalFlow() {
      return _finalFlow;
    }

    public Action getAction() {
      return _action;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      FlowResult that = (FlowResult) o;
      return Objects.equals(getFinalFlow(), that.getFinalFlow())
          && Objects.equals(getAction(), that.getAction());
    }

    @Override
    public int hashCode() {
      return Objects.hash(getFinalFlow(), getAction());
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
