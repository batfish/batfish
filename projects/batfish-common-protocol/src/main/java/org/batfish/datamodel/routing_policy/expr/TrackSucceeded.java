package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

/**
 * A {@link BooleanExpr} that evaluates to true iff the success state for a track provided by name
 * is present in the evaluation {@link Environment}.
 */
@ParametersAreNonnullByDefault
public final class TrackSucceeded extends BooleanExpr {

  @Override
  public <T, U> T accept(BooleanExprVisitor<T, U> visitor, U arg) {
    return visitor.visitTrackSucceeded(this, arg);
  }

  @Override
  public Result evaluate(Environment environment) {
    // RoutingPolicy caller is responsible for providing required track states in Environment
    return new Result(environment.isTrackSuccessful(_trackName));
  }

  public TrackSucceeded(String trackName) {
    _trackName = trackName;
  }

  @JsonCreator
  private static @Nonnull TrackSucceeded create(
      @JsonProperty(PROP_TRACK_NAME) @Nullable String trackName) {
    checkArgument(trackName != null, "Missing %s", PROP_TRACK_NAME);
    return new TrackSucceeded(trackName);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof TrackSucceeded)) {
      return false;
    }
    return _trackName.equals(((TrackSucceeded) obj)._trackName);
  }

  @Override
  public int hashCode() {
    return _trackName.hashCode();
  }

  @JsonProperty(PROP_TRACK_NAME)
  public @Nonnull String getTrackName() {
    return _trackName;
  }

  private static final String PROP_TRACK_NAME = "trackName";

  private final @Nonnull String _trackName;
}
