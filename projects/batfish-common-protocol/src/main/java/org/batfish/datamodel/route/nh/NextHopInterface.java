package org.batfish.datamodel.route.nh;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.io.ObjectStreamException;
import java.io.Serial;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Route;

/**
 * Route next hop indicating that a route must go out a particular interface, with an
 * <em>optional</em> next hop IP that would be ARP'ed for.
 *
 * <p>This is not to be confused with the interface resolution done by the FIB. This next hop type
 * is used for interface routes, such as: connected, local, and static interface routes.
 */
public final class NextHopInterface implements NextHop {

  // Soft values: let it be garbage collected in times of pressure.
  // Maximum size 2^20: Just some upper bound on cache size, well less than GiB.
  //   (8 bytes seems smallest possible entry (long), would be 8 MiB total).
  private static final LoadingCache<NextHopInterface, NextHopInterface> CACHE =
      CacheBuilder.newBuilder().softValues().maximumSize(1 << 20).build(CacheLoader.from(x -> x));

  /** The interface name to which the traffic should be routed */
  @JsonProperty(PROP_INTERFACE)
  public @Nonnull String getInterfaceName() {
    return _interfaceName;
  }

  /** Optional next hop/ARP IP to use */
  @JsonProperty(PROP_IP)
  public @Nullable Ip getIp() {
    return _ip;
  }

  /**
   * Create a new interface next hop without a specific ARP IP
   *
   * @param interfaceName name of the interface
   * @throws IllegalArgumentException if the interface name is not valid, e.g., {@link
   *     Interface#NULL_INTERFACE_NAME}
   */
  public static @Nonnull NextHopInterface of(String interfaceName) {
    return CACHE.getUnchecked(new NextHopInterface(interfaceName, null));
  }

  /**
   * Create a new interface next hop with a specific ARP IP
   *
   * @param interfaceName name of the interface
   * @param ip the IP to ARP for
   * @throws IllegalArgumentException if the interface name is not valid, e.g., {@link
   *     Interface#NULL_INTERFACE_NAME} or the IP is invalid, e.g., {@link Ip#AUTO}
   */
  public static @Nonnull NextHopInterface of(String interfaceName, Ip ip) {
    return CACHE.getUnchecked(new NextHopInterface(interfaceName, ip));
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NextHopInterface)) {
      return false;
    }
    NextHopInterface that = (NextHopInterface) o;
    return _interfaceName.equals(that._interfaceName) && Objects.equals(_ip, that._ip);
  }

  @Override
  public int hashCode() {
    // This hashcode could be hot, so compute manually instead of Objects.hash(...)
    return _interfaceName.hashCode() + (_ip == null ? 0 : 31 * _ip.hashCode());
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(NextHopInterface.class)
        .omitNullValues()
        .add("interfaceName", _interfaceName)
        .add("ip", _ip)
        .toString();
  }

  @Override
  public <T> T accept(NextHopVisitor<T> visitor) {
    return visitor.visitNextHopInterface(this);
  }

  private static final String PROP_INTERFACE = "interface";
  private static final String PROP_IP = "ip";

  private final @Nonnull String _interfaceName;
  private final @Nullable Ip _ip;

  @JsonCreator
  private static @Nonnull NextHopInterface create(
      @JsonProperty(PROP_INTERFACE) @Nullable String iface,
      @JsonProperty(PROP_IP) @Nullable Ip ip) {
    checkArgument(iface != null, "Missing %s", PROP_INTERFACE);
    return of(iface, ip);
  }

  private NextHopInterface(String interfaceName, @Nullable Ip ip) {
    checkArgument(
        !Interface.NULL_INTERFACE_NAME.equals(interfaceName),
        "NULL interface cannot be used as NextHopInterface, use NextHopDiscard");
    checkArgument(
        !Route.UNSET_NEXT_HOP_INTERFACE.equals(interfaceName),
        "NextHopInterface cannot have unset interface. Received %s",
        interfaceName);
    checkArgument(
        ip == null || (!ip.equals(Ip.AUTO) && !ip.equals(Ip.ZERO) && !ip.equals(Ip.MAX)),
        "NextHopIp must be a valid concrete IP address. Received %s",
        ip);
    _interfaceName = interfaceName;
    _ip = ip;
  }

  /** Re-intern after deserialization. */
  @Serial
  private Object readResolve() throws ObjectStreamException {
    return CACHE.getUnchecked(this);
  }
}
