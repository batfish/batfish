package org.batfish.z3.state;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.batfish.z3.HeaderField;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.expr.Expr;
import org.batfish.z3.expr.VarIntExpr;
import org.batfish.z3.expr.visitors.BooleanExprVisitor;
import org.batfish.z3.expr.visitors.ExprVisitor;
import org.batfish.z3.state.visitors.StateVisitor;

public abstract class State<T extends State<T, ?>, P extends StateParameterization<T>> {

  public static class StateExpr<T extends State<T, ?>, P extends StateParameterization<T>>
      extends BooleanExpr {

    public static final List<Expr> VARIABLES =
        Arrays.stream(HeaderField.values())
            .map(VarIntExpr::new)
            .collect(ImmutableList.toImmutableList());

    private final String _baseName;

    private final P _parameterization;

    private StateExpr(String baseName, P parameterization) {
      _baseName = baseName;
      _parameterization = parameterization;
    }

    @Override
    public void accept(BooleanExprVisitor visitor) {
      visitor.visitStateExpr(this);
    }

    @Override
    public void accept(ExprVisitor visitor) {
      visitor.visitStateExpr(this);
    }

    public String getBaseName() {
      return _baseName;
    }

    public P getParameterization() {
      return _parameterization;
    }
  }

  private final String _baseName;

  public State(String baseName) {
    _baseName = baseName;
  }

  public abstract void accept(StateVisitor visitor);

  protected final StateExpr<T, P> buildStateExpr(P parameterization) {
    return new StateExpr<>(_baseName, parameterization);
  }

  public String getBaseName() {
    return _baseName;
  }

  protected abstract Set<Transition<T>> getDefaultTransitions();

  public final Set<Transition<T>> getEnabledTransitions(
      Set<Class<? extends Transition<?>>> disabledTransitions) {
    if (disabledTransitions == null) {
      return getDefaultTransitions();
    } else {
      return ImmutableSet.copyOf(Sets.difference(getDefaultTransitions(), disabledTransitions));
    }
  }
}
