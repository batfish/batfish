package org.batfish.common.topology;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.NetworkConfigurations;

@ParametersAreNonnullByDefault
public final class Layer1Node implements Comparable<Layer1Node> {
  private static final String PROP_HOSTNAME = "hostname";
  private static final String PROP_INTERFACE_NAME = "interfaceName";

  @JsonCreator
  private static @Nonnull Layer1Node create(
      @JsonProperty(PROP_HOSTNAME) @Nullable String hostname,
      @JsonProperty(PROP_INTERFACE_NAME) @Nullable String interfaceName) {
    checkArgument(hostname != null, "Missing %s", PROP_HOSTNAME);
    checkArgument(interfaceName != null, "Missing %s", PROP_INTERFACE_NAME);
    return new Layer1Node(hostname, interfaceName);
  }

  private final String _hostname;

  private final String _interfaceName;

  public Layer1Node(String hostname, String interfaceName) {
    // Guarantee hostname is canonical (lowercase)
    _hostname = hostname.toLowerCase();
    _interfaceName = interfaceName;
  }

  @Override
  public int compareTo(Layer1Node o) {
    return Comparator.comparing(Layer1Node::getHostname)
        .thenComparing(Layer1Node::getInterfaceName)
        .compare(this, o);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Layer1Node)) {
      return false;
    }
    Layer1Node rhs = (Layer1Node) obj;
    return _hostname.equals(rhs._hostname) && _interfaceName.equals(rhs._interfaceName);
  }

  @JsonProperty(PROP_HOSTNAME)
  public @Nonnull String getHostname() {
    return _hostname;
  }

  @JsonProperty(PROP_INTERFACE_NAME)
  public @Nonnull String getInterfaceName() {
    return _interfaceName;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_hostname, _interfaceName);
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .add(PROP_HOSTNAME, _hostname)
        .add(PROP_INTERFACE_NAME, _interfaceName)
        .toString();
  }

  /**
   * Maps a layer-1 physical node to a layer-1 logical node. If a physical node is a member of an
   * aggregate interface, returns the node for the aggregate interface. If that aggregate interface
   * is missing or off, returns {@code null}. If physical node is not member of an aggregate
   * interface, the physical node is treated as a logical node and returned.
   */
  public @Nullable Layer1Node toLogicalNode(NetworkConfigurations networkConfigurations) {
    Optional<Interface> optIface = networkConfigurations.getInterface(_hostname, _interfaceName);
    checkArgument(
        optIface.isPresent(),
        "Unable to create logical node for missing interface %s[%s]",
        _hostname,
        _interfaceName);
    Interface iface = optIface.get();
    if (iface.getChannelGroup() == null) {
      return this;
    }
    return networkConfigurations
        .getInterface(_hostname, iface.getChannelGroup())
        .filter(Interface::getActive)
        .map(c -> new Layer1Node(_hostname, c.getName()))
        .orElse(null);
  }
}
