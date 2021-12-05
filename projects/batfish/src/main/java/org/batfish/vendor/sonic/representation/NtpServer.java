package org.batfish.vendor.sonic.representation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Represents NTP_SERVER: https://github.com/Azure/SONiC/wiki/Configuration#ntp-and-syslog-servers
 */
@ParametersAreNonnullByDefault
public class NtpServer implements ConfigDbObject {

  // a server is either an IP or name
  private @Nonnull final Set<String> _servers;

  public NtpServer(Set<String> servers) {
    _servers = ImmutableSet.copyOf(servers);
  }

  @JsonCreator
  private static NtpServer create(@Nullable Map<String, Object> servers) {
    return new NtpServer(servers == null ? ImmutableSet.of() : servers.keySet());
  }

  @Nonnull
  public Set<String> getServers() {
    return _servers;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NtpServer)) {
      return false;
    }
    NtpServer that = (NtpServer) o;
    return Objects.equals(_servers, that._servers);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_servers);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("servers", _servers).toString();
  }
}
