package org.batfish.datamodel.packet_policy;

import com.google.common.collect.ImmutableMap;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.acl.Evaluator;

/**
 * Evaluates a {@link PacketPolicy} against a given {@link Flow}.
 *
 * <p>To evaluate an entire policy, see {@link #evaluate(Flow, String, PacketPolicy)} which will
 * return a {@link FlowResult}.
 */
@ParametersAreNonnullByDefault
public final class FlowEvaluator {
  // Start state
  @Nonnull private final String _srcInterface;

  // Modified state
  @Nonnull private Flow.Builder _currentFlow;

  // Expr and stmt visitors
  @Nonnull private BoolExprEvaluator _boolExprEvaluator = new BoolExprEvaluator();
  @Nonnull private StatementEvaluator _stmtEvaluator = new StatementEvaluator();

  private final class BoolExprEvaluator implements BoolExprVisitor<Boolean> {

    @Override
    public Boolean visitPacketMatchExpr(PacketMatchExpr expr) {
      return Evaluator.matches(
          expr.getExpr(),
          _currentFlow.build(),
          _srcInterface,
          ImmutableMap.of(),
          ImmutableMap.of());
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
  }

  private FlowEvaluator(Flow originalFlow, String srcInterface) {
    _currentFlow = originalFlow.toBuilder();
    _srcInterface = srcInterface;
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

  public static FlowResult evaluate(Flow f, String srcInterface, PacketPolicy policy) {
    return new FlowEvaluator(f, srcInterface).evaluate(policy);
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
}
