package org.batfish.vendor.check_point_management;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** An interface as shown in show-gateways-and-servers */
public final class Interface implements Serializable {
  @JsonCreator
  private static @Nonnull Interface create(
      @JsonProperty(PROP_NAME) @Nullable String name,
      @JsonProperty(PROP_TOPOLOGY) @Nullable InterfaceTopology topology,
      @JsonProperty(PROP_IPV4_ADDRESS) @Nullable Ip ipv4Address,
      @JsonProperty(PROP_IPV4_MASK_LENGTH) @Nullable Integer ipv4MaskLength) {
    checkArgument(name != null, "Missing %s", PROP_NAME);
    checkArgument(topology != null, "Missing %s", PROP_TOPOLOGY);
    checkArgument(ipv4Address != null, "Missing %s", PROP_IPV4_ADDRESS);
    checkArgument(ipv4MaskLength != null, "Missing %s", PROP_IPV4_MASK_LENGTH);
    return new Interface(name, topology, ipv4Address, ipv4MaskLength);
  }

  @VisibleForTesting
  public Interface(String name, InterfaceTopology topology, Ip ipv4Address, int ipv4MaskLength) {
    _name = name;
    _topology = topology;
    _ipv4Address = ipv4Address;
    _ipv4MaskLength = ipv4MaskLength;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull InterfaceTopology getTopology() {
    return _topology;
  }

  public @Nonnull Ip getIpv4Address() {
    return _ipv4Address;
  }

  public int getIpv4MaskLength() {
    return _ipv4MaskLength;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof Interface)) {
      return false;
    }
    Interface that = (Interface) o;
    return _ipv4MaskLength == that._ipv4MaskLength
        && _name.equals(that._name)
        && _topology.equals(that._topology)
        && _ipv4Address.equals(that._ipv4Address);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name, _topology, _ipv4Address, _ipv4MaskLength);
  }

  private static final String PROP_NAME = "interface-name";
  private static final String PROP_TOPOLOGY = "topology";
  private static final String PROP_IPV4_ADDRESS = "ipv4-address";
  private static final String PROP_IPV4_MASK_LENGTH = "ipv4-mask-length";
  private final @Nonnull String _name;
  private final @Nonnull Ip _ipv4Address;
  private final int _ipv4MaskLength;
  private final @Nonnull InterfaceTopology _topology;
}
