package org.batfish.datamodel.vendor_family.f5_bigip;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;

/** Configuration for a pool member. */
@ParametersAreNonnullByDefault
public final class PoolMember implements Serializable {

  public PoolMember(
      @Nullable Ip address,
      @Nullable Ip6 address6,
      @Nullable String description,
      String name,
      String node,
      int port) {
    _address = address;
    _address6 = address6;
    _description = description;
    _name = name;
    _node = node;
    _port = port;
  }

  @JsonProperty(PROP_ADDRESS)
  public @Nullable Ip getAddress() {
    return _address;
  }

  @JsonProperty(PROP_ADDRESS6)
  public @Nullable Ip6 getAddress6() {
    return _address6;
  }

  @JsonProperty(PROP_DESCRIPTION)
  public @Nullable String getDescription() {
    return _description;
  }

  @JsonProperty(PROP_NAME)
  public @Nonnull String getName() {
    return _name;
  }

  @JsonProperty(PROP_NODE)
  public @Nonnull String getNode() {
    return _node;
  }

  @JsonProperty(PROP_PORT)
  public int getPort() {
    return _port;
  }

  private static final String PROP_ADDRESS = "address";
  private static final String PROP_ADDRESS6 = "address6";
  private static final String PROP_DESCRIPTION = "description";
  private static final String PROP_NAME = "name";
  private static final String PROP_NODE = "node";
  private static final String PROP_PORT = "port";

  @JsonCreator
  private static @Nonnull PoolMember create(
      @JsonProperty(PROP_ADDRESS) @Nullable Ip address,
      @JsonProperty(PROP_ADDRESS6) @Nullable Ip6 address6,
      @JsonProperty(PROP_DESCRIPTION) @Nullable String description,
      @JsonProperty(PROP_NAME) @Nullable String name,
      @JsonProperty(PROP_NODE) @Nullable String node,
      @JsonProperty(PROP_PORT) @Nullable Integer port) {
    checkArgument(name != null, "Missing %s", PROP_NAME);
    checkArgument(node != null, "Missing %s", PROP_NODE);
    checkArgument(port != null, "Missing %s", PROP_PORT);
    return new PoolMember(address, address6, description, name, node, port);
  }

  private final @Nullable Ip _address;
  private final @Nullable Ip6 _address6;
  private final @Nullable String _description;
  private final @Nonnull String _name;
  private final @Nonnull String _node;
  private final int _port;
}
