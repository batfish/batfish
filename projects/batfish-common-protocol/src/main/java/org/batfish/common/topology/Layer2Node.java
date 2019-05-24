package org.batfish.common.topology;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class Layer2Node implements Comparable<Layer2Node>, Serializable {
  private static final String PROP_HOSTNAME = "hostname";
  private static final String PROP_INTERFACE_NAME = "interfaceName";
  private static final String PROP_SWITCHPORT_VLAN_ID = "switchportVlanId";
  private static final long serialVersionUID = 1L;

  @JsonCreator
  private static @Nonnull Layer2Node create(
      @JsonProperty(PROP_HOSTNAME) String hostname,
      @JsonProperty(PROP_INTERFACE_NAME) String interfaceName,
      @JsonProperty(PROP_SWITCHPORT_VLAN_ID) Integer switchportVlanId) {
    return new Layer2Node(
        requireNonNull(hostname), requireNonNull(interfaceName), switchportVlanId);
  }

  private final @Nonnull String _hostname;

  private final @Nonnull String _interfaceName;

  private final @Nullable Integer _switchportVlanId;

  public Layer2Node(@Nonnull Layer1Node layer1Node, @Nullable Integer vlanId) {
    _hostname = layer1Node.getHostname();
    _interfaceName = layer1Node.getInterfaceName();
    _switchportVlanId = vlanId;
  }

  public Layer2Node(
      @Nonnull String hostname, @Nonnull String interfaceName, @Nullable Integer vlanId) {
    _hostname = hostname;
    _interfaceName = interfaceName;
    _switchportVlanId = vlanId;
  }

  @Override
  public int compareTo(@Nonnull Layer2Node o) {
    // compareTo is hot for some networks, so inlining the comparator is worth it
    if (this == o) {
      return 0;
    }
    int r = _hostname.compareTo(o._hostname);
    if (r != 0) {
      return r;
    }
    r = _interfaceName.compareTo(o._interfaceName);
    if (r != 0) {
      return r;
    }
    if (_switchportVlanId == null && o._switchportVlanId == null) {
      return 0;
    }
    if (_switchportVlanId == null) {
      return -1;
    }
    if (o._switchportVlanId == null) {
      return 1;
    }
    return _switchportVlanId.compareTo(o._switchportVlanId);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Layer2Node)) {
      return false;
    }
    Layer2Node rhs = (Layer2Node) obj;
    return _hostname.equals(rhs._hostname)
        && _interfaceName.equals(rhs._interfaceName)
        && Objects.equals(_switchportVlanId, rhs._switchportVlanId);
  }

  @JsonProperty(PROP_HOSTNAME)
  public @Nonnull String getHostname() {
    return _hostname;
  }

  @JsonProperty(PROP_INTERFACE_NAME)
  public @Nonnull String getInterfaceName() {
    return _interfaceName;
  }

  @JsonProperty(PROP_SWITCHPORT_VLAN_ID)
  public @Nullable Integer getSwitchportVlanId() {
    return _switchportVlanId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_hostname, _interfaceName, _switchportVlanId);
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .omitNullValues()
        .add(PROP_HOSTNAME, _hostname)
        .add(PROP_INTERFACE_NAME, _interfaceName)
        .add(PROP_SWITCHPORT_VLAN_ID, _switchportVlanId)
        .toString();
  }
}
