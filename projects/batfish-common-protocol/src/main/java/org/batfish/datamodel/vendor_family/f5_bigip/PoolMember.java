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
  private static final String PROP_ADDRESS = "address";
  private static final String PROP_ADDRESS6 = "address6";
  private static final String PROP_DESCRIPTION = "description";
  private static final String PROP_NAME = "name";
  private static final String PROP_NODE = "node";
  private static final String PROP_PORT = "port";
  private static final long serialVersionUID = 1L;

  @JsonCreator
  private static @Nonnull PoolMember create(
      @JsonProperty(PROP_NAME) @Nullable String name,
      @JsonProperty(PROP_NODE) @Nullable String node,
      @JsonProperty(PROP_PORT) @Nullable Integer port) {
    checkArgument(name != null, "Missing %s", PROP_NAME);
    checkArgument(node != null, "Missing %s", PROP_NODE);
    checkArgument(port != null, "Missing %s", PROP_PORT);
    return new PoolMember(name, node, port);
  }

  private @Nullable Ip _address;
  private @Nullable Ip6 _address6;
  private @Nullable String _description;
  private final @Nonnull String _name;
  private final @Nonnull String _node;
  private final int _port;

  public PoolMember(String name, String node, int port) {
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

  @JsonProperty(PROP_ADDRESS)
  public void setAddress(@Nullable Ip address) {
    _address = address;
  }

  @JsonProperty(PROP_ADDRESS6)
  public void setAddress6(@Nullable Ip6 address6) {
    _address6 = address6;
  }

  @JsonProperty(PROP_DESCRIPTION)
  public void setDescription(@Nullable String description) {
    _description = description;
  }
}
