package org.batfish.vendor.check_point_management;

import static com.google.common.base.MoreObjects.toStringHelper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.vendor.VendorSupplementalInformation;

/** Configuration encompassing settings for all CheckPoint management servers in a snapshot. */
public final class CheckpointManagementConfiguration implements VendorSupplementalInformation {

  public CheckpointManagementConfiguration(Map<String, ManagementServer> servers) {
    _servers = servers;
  }

  public @Nonnull Map<String, ManagementServer> getServers() {
    return _servers;
  }

  /**
   * Get actual hostname for cluster member name in CheckPoint management config, or {@link
   * Optional#empty} if information is absent.
   *
   * <p>Only available after conversion until serialization of converted configurations.
   */
  public @Nonnull Optional<String> getHostnameForGateway(String clusterMemberName) {
    // Need to handle null map, since only non-null if at least one mapping is recorded.
    return Optional.ofNullable(_clusterMemberToHostname).map(map -> map.get(clusterMemberName));
  }

  /**
   * Record a mapping from cluster member name in CheckPoint management config to actual hostname in
   * gateway config.
   *
   * <p>Should only be called during conversion of a CheckPoint gateway.
   */
  public synchronized void recordClusterMemberNameToHostname(
      String clusterMemberName, String hostname) {
    if (_clusterMemberToHostname == null) {
      _clusterMemberToHostname = new HashMap<>();
    }
    _clusterMemberToHostname.put(clusterMemberName, hostname);
  }

  /**
   * Whether the named gateway has a Sync interface.
   *
   * <p>Only available after conversion until serialization of converted configurations.
   */
  public boolean hasSyncInterface(String hostname) {
    // Need to handle null set, since only non-null if at least one item is recorded.
    return Optional.ofNullable(_gatewaysWithSyncInterface)
        .map(set -> set.contains(hostname))
        .orElse(false);
  }

  /**
   * Record that the named gateway has a Sync interface.
   *
   * <p>Should only be called during conversion of a CheckPoint gateway.
   */
  public synchronized void recordSyncInterface(String hostname) {
    if (_gatewaysWithSyncInterface == null) {
      _gatewaysWithSyncInterface = new HashSet<>();
    }
    _gatewaysWithSyncInterface.add(hostname);
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

  private transient @Nullable Map<String, String> _clusterMemberToHostname;
  private transient @Nullable Set<String> _gatewaysWithSyncInterface;
}
