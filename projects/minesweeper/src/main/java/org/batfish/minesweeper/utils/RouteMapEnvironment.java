package org.batfish.minesweeper.utils;

import static com.google.common.base.MoreObjects.firstNonNull;

import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.Ip;

/**
 * Information about the environment that may be needed to evaluate a route map. Ultimately this can
 * include anything that a route map's behavior may depend upon, other than the route being
 * processed.
 */
public class RouteMapEnvironment {

  private final @Nonnull Predicate<String> _successfulTracks;

  private final @Nullable String _sourceVrf;

  private final @Nonnull BgpSessionProperties _sessionProperties;

  public RouteMapEnvironment(
      @Nullable Predicate<String> successfulTracks,
      @Nullable String sourceVrf,
      @Nonnull Ip remoteIp) {
    _successfulTracks = firstNonNull(successfulTracks, s -> false);
    _sourceVrf = sourceVrf;
    // dummy values for required fields
    _sessionProperties =
        BgpSessionProperties.builder()
            .setLocalAs(1)
            .setLocalIp(Ip.parse("1.1.1.1"))
            .setRemoteAs(2)
            .setRemoteIp(remoteIp)
            .build();
  }

  public @Nonnull BgpSessionProperties getSessionProperties() {
    return _sessionProperties;
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
    return Objects.equals(_sessionProperties, other._sessionProperties)
        && Objects.equals(_successfulTracks, other._successfulTracks)
        && Objects.equals(_sourceVrf, other._sourceVrf);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_sessionProperties, _successfulTracks, _sourceVrf);
  }
}
