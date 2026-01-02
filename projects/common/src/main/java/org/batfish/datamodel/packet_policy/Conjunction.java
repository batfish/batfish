package org.batfish.datamodel.packet_policy;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** A logical AND of two {@link BoolExpr boolean expressions} */
public class Conjunction implements BoolExpr {

  private static final String PROP_CONJUNCTS = "conjuncts";

  private final @Nonnull List<BoolExpr> _conjuncts;

  private Conjunction(List<BoolExpr> conjuncts) {
    _conjuncts = ImmutableList.copyOf(conjuncts);
  }

  public static BoolExpr of(Iterable<BoolExpr> exprs) {
    List<BoolExpr> l = ImmutableList.copyOf(exprs);
    if (l.isEmpty()) {
      return TrueExpr.instance();
    }
    return new Conjunction(l);
  }

  public static BoolExpr of(BoolExpr... exprs) {
    return of(Arrays.asList(exprs));
  }

  @JsonProperty(PROP_CONJUNCTS)
  public @Nonnull List<BoolExpr> getConjuncts() {
    return _conjuncts;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Conjunction)) {
      return false;
    }
    Conjunction that = (Conjunction) o;
    return _conjuncts.equals(that._conjuncts);
  }

  @Override
  public int hashCode() {
    return _conjuncts.hashCode();
  }

  @Override
  public <T> T accept(BoolExprVisitor<T> tBoolExprVisitor) {
    return tBoolExprVisitor.visitConjunction(this);
  }

  @JsonCreator
  private static Conjunction create(
      @JsonProperty(PROP_CONJUNCTS) @Nullable List<BoolExpr> conjuncts) {
    checkArgument(conjuncts != null && !conjuncts.isEmpty(), "Missing %s", PROP_CONJUNCTS);
    return new Conjunction(conjuncts);
  }
}
