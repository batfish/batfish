package org.batfish.z3.expr;

import com.google.common.base.Suppliers;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import org.batfish.z3.state.StateParameter;
import org.batfish.z3.state.visitors.GenericStateExprVisitor;
import org.batfish.z3.state.visitors.Parameterizer;

/** An expression representing parameterized state. */
public abstract class StateExpr {
  private final Supplier<Integer> _hashCode = Suppliers.memoize(this::computeHashCode);
  private final Supplier<List<StateParameter>> _params = Suppliers.memoize(this::computeParameters);

  public abstract <R> R accept(GenericStateExprVisitor<R> visitor);

  private int computeHashCode() {
    return Objects.hash(getClass().getCanonicalName(), _params.get());
  }

  private List<StateParameter> computeParameters() {
    return Parameterizer.getParameters(this);
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (o == null) {
      return false;
    } else if (!getClass().equals(o.getClass())) {
      return false;
    }
    return _params.get().equals(((StateExpr) o)._params.get());
  }

  @Override
  public int hashCode() {
    return _hashCode.get();
  }
}
