package org.batfish.vendor.check_point_management;

import static com.google.common.base.MoreObjects.toStringHelper;

import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.vendor.VendorSupplementalInformation;

/** Configuration encompassing settings for all CheckPoint management servers in a snapshot. */
public final class CheckpointManagementConfiguration implements VendorSupplementalInformation {

  public CheckpointManagementConfiguration(Map<String, ManagementServer> servers) {
    _servers = servers;
  }

  public @Nonnull Map<String, ManagementServer> getServers() {
    return _servers;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CheckpointManagementConfiguration)) {
      return false;
    }
    CheckpointManagementConfiguration that = (CheckpointManagementConfiguration) o;
    return _servers.equals(that._servers);
  }

  @Override
  public int hashCode() {
    return _servers.hashCode();
  }

  @Override
  public String toString() {
    return toStringHelper(this).add("_servers", _servers).toString();
  }

  private final @Nonnull Map<String, ManagementServer> _servers;
}
