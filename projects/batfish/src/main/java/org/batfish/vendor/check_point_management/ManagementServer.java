package org.batfish.vendor.check_point_management;

import static com.google.common.base.MoreObjects.toStringHelper;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;

/** A CheckPoint management server. */
public final class ManagementServer implements Serializable {

  public ManagementServer(Map<String, ManagementDomain> domains, String name) {
    _domains = domains;
    _name = name;
  }

  public @Nonnull Map<String, ManagementDomain> getDomains() {
    return _domains;
  }

  public @Nonnull String getName() {
    return _name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof ManagementServer)) {
      return false;
    }
    ManagementServer that = (ManagementServer) o;
    return _domains.equals(that._domains) && _name.equals(that._name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_domains, _name);
  }

  @Override
  public String toString() {
    return toStringHelper(this).add("_domains", _domains).add("_name", _name).toString();
  }

  private final @Nonnull Map<String, ManagementDomain> _domains;
  private final @Nonnull String _name;
}
