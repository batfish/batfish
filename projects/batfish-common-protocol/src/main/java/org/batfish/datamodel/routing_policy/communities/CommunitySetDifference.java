package org.batfish.datamodel.routing_policy.communities;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A {@link CommunitySetExpr} representing the union of the sets represented by its constituent
 * expressions.
 */
public final class CommunitySetDifference extends CommunitySetExpr {

  public CommunitySetDifference(CommunitySetExpr initial, CommunityMatchExpr removalCriterion) {
    _initial = initial;
    _removalCriterion = removalCriterion;
  }

  @Override
  public <T, U> T accept(CommunitySetExprVisitor<T, U> visitor, U arg) {
    return visitor.visitCommunitySetDifference(this, arg);
  }

  @JsonProperty(PROP_INITIAL)
  public @Nonnull CommunitySetExpr getInitial() {
    return _initial;
  }

  @JsonProperty(PROP_REMOVAL_CRITERION)
  public @Nonnull CommunityMatchExpr getRemovalCriterion() {
    return _removalCriterion;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CommunitySetDifference)) {
      return false;
    }
    CommunitySetDifference rhs = (CommunitySetDifference) obj;
    return _initial.equals(rhs._initial) && _removalCriterion.equals(rhs._removalCriterion);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_initial, _removalCriterion);
  }

  private static final String PROP_INITIAL = "initial";
  private static final String PROP_REMOVAL_CRITERION = "removalCriterion";

  @JsonCreator
  private static @Nonnull CommunitySetDifference create(
      @JsonProperty(PROP_INITIAL) @Nullable CommunitySetExpr initial,
      @JsonProperty(PROP_REMOVAL_CRITERION) @Nullable CommunityMatchExpr removalCriterion) {
    checkArgument(initial != null, "missing %s", PROP_INITIAL);
    checkArgument(removalCriterion != null, "missing %s", PROP_REMOVAL_CRITERION);
    return new CommunitySetDifference(initial, removalCriterion);
  }

  private final @Nonnull CommunitySetExpr _initial;
  private final @Nonnull CommunityMatchExpr _removalCriterion;
}
