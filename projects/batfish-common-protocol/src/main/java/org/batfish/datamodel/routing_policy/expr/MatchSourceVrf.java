package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

/**
 * Boolean expression that evaluates whether an {@link Environment} has a route that came from a
 * given source VRF.
 */
@ParametersAreNonnullByDefault
public final class MatchSourceVrf extends BooleanExpr {

  private static final long serialVersionUID = 1L;

  private static final String PROP_SOURCE_VRF = "sourceVrf";

  private final String _sourceVrf;

  @JsonCreator
  private static MatchSourceVrf create(@Nullable @JsonProperty(PROP_SOURCE_VRF) String sourceVrf) {
    checkArgument(sourceVrf != null, "%s must be provided", PROP_SOURCE_VRF);
    return new MatchSourceVrf(sourceVrf);
  }

  public MatchSourceVrf(String sourceVrf) {
    _sourceVrf = sourceVrf;
  }

  @Override
  public Result evaluate(Environment environment) {
    throw new BatfishException("No implementation for MatchSourceVrf.evaluate()");
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof MatchSourceVrf)) {
      return false;
    }
    return _sourceVrf.equals(((MatchSourceVrf) obj)._sourceVrf);
  }

  @Override
  public int hashCode() {
    return _sourceVrf.hashCode();
  }

  @Override
  public String toString() {
    return toStringHelper().add(PROP_SOURCE_VRF, _sourceVrf).toString();
  }
}
