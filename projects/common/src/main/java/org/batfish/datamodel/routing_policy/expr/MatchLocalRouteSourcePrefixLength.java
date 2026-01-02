package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.batfish.datamodel.LocalRoute;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

/**
 * Boolean expression that evaluates whether an {@link Environment} has a {@link LocalRoute} with a
 * source prefix length within a given range of lengths to match.
 */
public final class MatchLocalRouteSourcePrefixLength extends BooleanExpr {
  private static final String PROP_MATCH_LENGTH = "matchLength";

  private final SubRange _matchLength;

  @JsonCreator
  public MatchLocalRouteSourcePrefixLength(
      @JsonProperty(PROP_MATCH_LENGTH) @Nonnull SubRange matchLength) {
    _matchLength = matchLength;
  }

  @Override
  public <T, U> T accept(BooleanExprVisitor<T, U> visitor, U arg) {
    return visitor.visitMatchLocalRouteSourcePrefixLength(this, arg);
  }

  @Override
  public Result evaluate(Environment environment) {
    LocalRoute localRoute = (LocalRoute) environment.getOriginalRoute();
    return new Result(_matchLength.includes(localRoute.getSourcePrefixLength()));
  }

  @JsonProperty(PROP_MATCH_LENGTH)
  public @Nonnull SubRange getMatchLength() {
    return _matchLength;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof MatchLocalRouteSourcePrefixLength)) {
      return false;
    }
    MatchLocalRouteSourcePrefixLength other = (MatchLocalRouteSourcePrefixLength) obj;
    return Objects.equals(_matchLength, other._matchLength);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_matchLength);
  }
}
