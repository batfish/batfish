package org.batfish.datamodel.packet_policy;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A {@link PacketPolicy} If statement. Executes inner statements only if the {@link
 * #getMatchCondition()} is true.
 */
public final class If implements Statement {
  private static final String PROP_ACTIONS = "actions";
  private static final String PROP_TRUE_STATEMENTS = "trueStatements";

  private final @Nonnull BoolExpr _matchCondition;
  private final @Nonnull List<Statement> _trueStatements;

  public If(BoolExpr matchCondition, List<Statement> trueStatements) {
    _matchCondition = matchCondition;
    _trueStatements = ImmutableList.copyOf(trueStatements);
  }

  @JsonCreator
  private static @Nonnull If jsonCreator(
      @JsonProperty(PROP_ACTIONS) @Nullable List<Statement> actions,
      @JsonProperty(PROP_TRUE_STATEMENTS) @Nullable BoolExpr matchCondition) {
    checkArgument(matchCondition != null, "Missing %s", PROP_TRUE_STATEMENTS);
    return new If(matchCondition, firstNonNull(actions, ImmutableList.of()));
  }

  @JsonProperty(PROP_ACTIONS)
  public @Nonnull List<Statement> getTrueStatements() {
    return _trueStatements;
  }

  @JsonProperty(PROP_TRUE_STATEMENTS)
  public @Nonnull BoolExpr getMatchCondition() {
    return _matchCondition;
  }

  @Override
  public <T> T accept(StatementVisitor<T> visitor) {
    return visitor.visitIf(this);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    If anIf = (If) o;
    return Objects.equals(getMatchCondition(), anIf.getMatchCondition())
        && Objects.equals(getTrueStatements(), anIf.getTrueStatements());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getMatchCondition(), getTrueStatements());
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).toString();
  }
}
