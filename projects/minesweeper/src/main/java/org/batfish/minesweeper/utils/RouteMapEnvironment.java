package org.batfish.minesweeper.utils;

import static com.google.common.base.MoreObjects.firstNonNull;

import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Information about the environment that is needed to evaluate a route map. Ultimately this can
 * include anything that a route map's behavior may depend upon, other than the route being
 * processed.
 */
public class RouteMapEnvironment {

  private final @Nonnull Predicate<String> _successfulTracks;

  private final @Nullable String _sourceVrf;

  public RouteMapEnvironment(
      @Nullable Predicate<String> successfulTracks, @Nullable String sourceVrf) {
    _successfulTracks = firstNonNull(successfulTracks, s -> false);
    _sourceVrf = sourceVrf;
  }

  public @Nonnull Predicate<String> getSuccessfulTracks() {
    return _successfulTracks;
  }

  public @Nullable String getSourceVrf() {
    return _sourceVrf;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof RouteMapEnvironment)) {
      return false;
    }
    RouteMapEnvironment other = (RouteMapEnvironment) o;
    return Objects.equals(_successfulTracks, other._successfulTracks)
        && Objects.equals(_sourceVrf, other._sourceVrf);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_successfulTracks, _sourceVrf);
  }
}
