package org.batfish.question.routes;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.Prefix;

/**
 * Class representing the primary key used for grouping {@link org.batfish.datamodel.AbstractRoute}s
 * and {@link BgpRoute}s
 */
@ParametersAreNonnullByDefault
public class RouteRowKey {
  private final @Nonnull String _hostName;

  private final @Nonnull String _vrfName;

  private final @Nonnull Prefix _prefix;

  public RouteRowKey(String hostName, String vrfName, Prefix prefix) {
    _hostName = hostName;
    _vrfName = vrfName;
    _prefix = prefix;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RouteRowKey that = (RouteRowKey) o;
    return Objects.equals(_hostName, that._hostName)
        && Objects.equals(_vrfName, that._vrfName)
        && Objects.equals(_prefix, that._prefix);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_hostName, _vrfName, _prefix);
  }

  public @Nonnull String getHostName() {
    return _hostName;
  }

  public @Nonnull String getVrfName() {
    return _vrfName;
  }

  public @Nonnull Prefix getPrefix() {
    return _prefix;
  }
}
