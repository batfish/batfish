package org.batfish.datamodel.isis;

import static com.google.common.base.MoreObjects.toStringHelper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Comparator;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.NetworkConfigurations;

public final class IsisNode implements Comparable<IsisNode> {

  private static final String PROP_NODE = "node";

  private static final String PROP_INTERFACE = "interface";

  private final String _hostname;

  private final String _interfaceName;

  public IsisNode(@Nonnull String hostname, @Nonnull String interfaceName) {
    _hostname = hostname;
    _interfaceName = interfaceName;
  }

  @JsonCreator
  private static @Nonnull IsisNode create(
      @JsonProperty(PROP_NODE) String hostname,
      @JsonProperty(PROP_INTERFACE) String interfaceName) {
    return new IsisNode(hostname, interfaceName);
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
    return _hostname.equals(rhs._hostname) && _interfaceName.equals(rhs._interfaceName);
  }

  @JsonProperty(PROP_NODE)
  public @Nonnull String getHostname() {
    return _hostname;
  }

  public @Nullable Interface getInterface(@Nonnull NetworkConfigurations nc) {
    return nc.getInterface(_hostname, _interfaceName);
  }

  @JsonProperty(PROP_INTERFACE)
  public @Nonnull String getInterfaceName() {
    return _interfaceName;
  }

  public @Nullable IsisProcess getIsisProcess(@Nonnull NetworkConfigurations nc) {
    Interface i = getInterface(nc);
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
