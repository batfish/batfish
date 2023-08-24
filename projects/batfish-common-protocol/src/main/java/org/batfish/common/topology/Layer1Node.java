package org.batfish.common.topology;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Comparator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.util.InterfaceNameComparator;
import org.batfish.datamodel.collections.NodeInterfacePair;

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

  public @Nonnull NodeInterfacePair asNodeInterfacePair() {
    return NodeInterfacePair.of(_hostname, _interfaceName);
  }

  @Override
  public int compareTo(Layer1Node o) {
    return Comparator.comparing(Layer1Node::getHostname)
        .thenComparing(Layer1Node::getInterfaceName, InterfaceNameComparator.instance())
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

  /* Cache the hashcode */
  private transient int _hashCode = 0;

  @Override
  public int hashCode() {
    int h = _hashCode;
    if (h == 0) {
      h = _hostname.hashCode() * 31 + _interfaceName.hashCode();
      _hashCode = h;
    }
    return h;
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .add(PROP_HOSTNAME, _hostname)
        .add(PROP_INTERFACE_NAME, _interfaceName)
        .toString();
  }
}
