package org.batfish.dataplane.topology;

import static com.google.common.base.MoreObjects.toStringHelper;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IsisProcess;

public class IsisNode implements Comparable<IsisNode> {

  private final String _hostname;

  private final String _interfaceName;

  public IsisNode(@Nonnull String hostname, @Nonnull String interfaceName) {
    _hostname = hostname;
    _interfaceName = interfaceName;
  }

  @Override
  public int compareTo(IsisNode o) {
    return Comparator.comparing(IsisNode::getHostname)
        .thenComparing(IsisNode::getInterfaceName)
        .compare(this, o);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IsisNode)) {
      return false;
    }
    IsisNode rhs = (IsisNode) o;
    return _hostname.equals(rhs._hostname) && _interfaceName.equals(_interfaceName);
  }

  public @Nonnull String getHostname() {
    return _hostname;
  }

  public @Nullable Interface getInterface(@Nonnull Map<String, Configuration> configurations) {
    Configuration c = configurations.get(_hostname);
    if (c == null) {
      return null;
    }
    return c.getInterfaces().get(_interfaceName);
  }

  public @Nonnull String getInterfaceName() {
    return _interfaceName;
  }

  public @Nullable IsisProcess getIsisProcess(@Nonnull Map<String, Configuration> configurations) {
    Configuration c = configurations.get(_hostname);
    if (c == null) {
      return null;
    }
    Interface i = c.getInterfaces().get(_interfaceName);
    if (i == null) {
      return null;
    }
    return i.getVrf().getIsisProcess();
  }

  @Override
  public int hashCode() {
    return Objects.hash(_hostname, _interfaceName);
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .add("hostname", _hostname)
        .add("interfaceName", _interfaceName)
        .toString();
  }
}
