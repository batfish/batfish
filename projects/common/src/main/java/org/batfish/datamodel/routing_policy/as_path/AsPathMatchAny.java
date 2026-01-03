package org.batfish.datamodel.routing_policy.as_path;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** A disjunction of {@link AsPathMatchExpr}s. */
public final class AsPathMatchAny extends AsPathMatchExpr {

  public static AsPathMatchAny of(Iterable<AsPathMatchExpr> disjuncts) {
    return new AsPathMatchAny(ImmutableSet.copyOf(disjuncts));
  }

  @Override
  public <T, U> T accept(AsPathMatchExprVisitor<T, U> visitor, U arg) {
    return visitor.visitAsPathMatchAny(this, arg);
  }

  @JsonIgnore
  public @Nonnull Set<AsPathMatchExpr> getDisjuncts() {
    return _disjuncts;
  }

  @JsonProperty(PROP_DISJUNCTS)
  private @Nonnull List<AsPathMatchExpr> getDisjunctsSorted() {
    // ordered for refs
    return ImmutableList.copyOf(_disjuncts);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof AsPathMatchAny)) {
      return false;
    }
    AsPathMatchAny that = (AsPathMatchAny) obj;
    return _disjuncts.equals(that._disjuncts);
  }

  @Override
  public int hashCode() {
    return _disjuncts.hashCode();
  }

  private static final String PROP_DISJUNCTS = "disjuncts";

  @JsonCreator
  private static @Nonnull AsPathMatchAny create(
      @JsonProperty(PROP_DISJUNCTS) @Nullable Iterable<AsPathMatchExpr> disjuncts) {
    return of(ImmutableSet.copyOf(firstNonNull(disjuncts, ImmutableSet.of())));
  }

  private final @Nonnull Set<AsPathMatchExpr> _disjuncts;

  private AsPathMatchAny(Set<AsPathMatchExpr> disjuncts) {
    _disjuncts = disjuncts;
  }
}
