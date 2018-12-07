package org.batfish.question.routes;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix;

@ParametersAreNonnullByDefault
public class RouteRowKey {
  @Nonnull private String _hostName;

  @Nonnull private String _vrfName;

  @Nonnull private Prefix _prefix;

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

  @Nonnull
  public String getHostName() {
    return _hostName;
  }

  @Nonnull
  public String getVrfName() {
    return _vrfName;
  }

  @Nonnull
  public Prefix getPrefix() {
    return _prefix;
  }
}
