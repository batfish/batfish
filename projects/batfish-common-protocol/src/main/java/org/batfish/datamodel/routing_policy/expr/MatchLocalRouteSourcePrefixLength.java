package org.batfish.datamodel.routing_policy.expr;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import org.batfish.datamodel.LocalRoute;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

public class MatchLocalRouteSourcePrefixLength extends BooleanExpr {

  private static final String PROP_MATCH_LENGTH = "matchLength";

  private static final long serialVersionUID = 1L;

  private final SubRange _matchLength;

  @JsonCreator
  public MatchLocalRouteSourcePrefixLength(
      @JsonProperty(PROP_MATCH_LENGTH) @Nonnull SubRange matchLength) {
    _matchLength = matchLength;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    MatchLocalRouteSourcePrefixLength other = (MatchLocalRouteSourcePrefixLength) obj;
    return _matchLength.equals(other._matchLength);
  }

  @Override
  public Result evaluate(Environment environment) {
    LocalRoute localRoute = (LocalRoute) environment.getOriginalRoute();
    Result result = new Result();
    result.setBooleanValue(_matchLength.includes(localRoute.getSourcePrefixLength()));
    return result;
  }

  @JsonProperty(PROP_MATCH_LENGTH)
  public @Nonnull SubRange getMatchLength() {
    return _matchLength;
  }

  @Override
  public int hashCode() {
    return _matchLength.hashCode();
  }
}
