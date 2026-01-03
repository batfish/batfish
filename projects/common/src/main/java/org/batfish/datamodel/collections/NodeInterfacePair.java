package org.batfish.datamodel.collections;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.Names.escapeNameIfNeeded;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.io.ObjectStreamException;
import java.io.Serial;
import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.util.InterfaceNameComparator;
import org.batfish.datamodel.Interface;

/** Combination of node name and interface name */
@ParametersAreNonnullByDefault
public final class NodeInterfacePair implements Serializable, Comparable<NodeInterfacePair> {
  // Soft values: let it be garbage collected in times of pressure.
  // Maximum size 2^18: Just some upper bound on cache size, well less than GiB.
  //   (~40 bytes reasonable entry size: 12+12+pointers, would be 10 MiB total).
  private static final LoadingCache<NodeInterfacePair, NodeInterfacePair> CACHE =
      CacheBuilder.newBuilder().softValues().maximumSize(1 << 18).build(CacheLoader.from(x -> x));

  private static final String PROP_HOSTNAME = "hostname";
  private static final String PROP_INTERFACE = "interface";

  private final @Nonnull String _hostname;
  private final @Nonnull String _interfaceName;

  @JsonCreator
  private static NodeInterfacePair jsonCreator(
      @JsonProperty(PROP_HOSTNAME) @Nullable String node,
      @JsonProperty(PROP_INTERFACE) @Nullable String iface) {
    checkArgument(node != null, "NodeInterfacePair missing %s", PROP_HOSTNAME);
    checkArgument(iface != null, "NodeInterfacePair missing %s", PROP_INTERFACE);
    return of(node, iface);
  }

  public static NodeInterfacePair of(String hostname, String interfaceName) {
    return CACHE.getUnchecked(new NodeInterfacePair(hostname, interfaceName));
  }

  public static NodeInterfacePair of(Interface iface) {
    return of(iface.getOwner().getHostname(), iface.getName());
  }

  private NodeInterfacePair(String hostname, String interfaceName) {
    _hostname = hostname.toLowerCase();
    _interfaceName = interfaceName;
  }

  /** Return node name */
  @JsonProperty(PROP_HOSTNAME)
  public @Nonnull String getHostname() {
    return _hostname;
  }

  /** Return node interface name */
  @JsonProperty(PROP_INTERFACE)
  public @Nonnull String getInterface() {
    return _interfaceName;
  }

  @Override
  public String toString() {
    return escapeNameIfNeeded(_hostname) + "[" + escapeNameIfNeeded(_interfaceName) + "]";
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof NodeInterfacePair)) {
      return false;
    }
    NodeInterfacePair that = (NodeInterfacePair) o;
    return _hostname.equals(that._hostname) && _interfaceName.equals(that._interfaceName);
  }

  @Override
  public int hashCode() {
    return 31 * _hostname.hashCode() + _interfaceName.hashCode();
  }

  @Override
  public int compareTo(NodeInterfacePair other) {
    int hostCmp = _hostname.compareTo(other._hostname);
    return hostCmp != 0
        ? hostCmp
        : InterfaceNameComparator.instance().compare(_interfaceName, other._interfaceName);
  }

  /** Re-intern after deserialization. */
  @Serial
  private Object readResolve() throws ObjectStreamException {
    return CACHE.getUnchecked(this);
  }
}
