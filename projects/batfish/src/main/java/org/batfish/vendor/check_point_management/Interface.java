package org.batfish.vendor.check_point_management;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** An interface as shown in show-gateways-and-servers */
public final class Interface implements Serializable {
  @JsonCreator
  private static @Nonnull Interface create(
      @JsonProperty(PROP_NAME) @Nullable String name,
      @JsonProperty(PROP_TOPOLOGY) @Nullable InterfaceTopology topology) {
    checkArgument(name != null, "Missing %s", PROP_NAME);
    checkArgument(topology != null, "Missing %s", PROP_TOPOLOGY);
    return new Interface(name, topology);
  }

  @VisibleForTesting
  Interface(String name, InterfaceTopology topology) {
    _name = name;
    _topology = topology;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull InterfaceTopology getTopology() {
    return _topology;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof Interface)) {
      return false;
    }
    Interface that = (Interface) o;
    return _name.equals(that._name) && _topology.equals(that._topology);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name, _topology);
  }

  private static final String PROP_NAME = "interface-name";
  private static final String PROP_TOPOLOGY = "topology";
  private final @Nonnull String _name;
  private final @Nonnull InterfaceTopology _topology;
}
