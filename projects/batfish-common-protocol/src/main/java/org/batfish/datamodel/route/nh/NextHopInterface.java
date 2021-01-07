package org.batfish.datamodel.route.nh;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.MoreObjects;
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

  /** The interface name to which the traffic should be routed */
  @Nonnull
  public String getInterfaceName() {
    return _interfaceName;
  }

  /** Optional next hop/ARP IP to use */
  @Nullable
  public Ip getIp() {
    return _ip;
  }

  /**
   * Create a new interface next hop without a specific ARP IP
   *
   * @param interfaceName name of the interface
   * @throws IllegalArgumentException if the interface name is not valid, e.g., {@link
   *     Interface#NULL_INTERFACE_NAME}
   */
  @Nonnull
  public static NextHopInterface of(String interfaceName) {
    return new NextHopInterface(interfaceName, null);
  }

  /**
   * Create a new interface next hop with a specific ARP IP
   *
   * @param interfaceName name of the interface
   * @param ip the IP to ARP for
   * @throws IllegalArgumentException if the interface name is not valid, e.g., {@link
   *     Interface#NULL_INTERFACE_NAME} or the IP is invalid, e.g., {@link Ip#AUTO}
   */
  @Nonnull
  public static NextHopInterface of(String interfaceName, Ip ip) {
    return new NextHopInterface(interfaceName, ip);
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

  @Nonnull private final String _interfaceName;
  @Nullable private final Ip _ip;

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
}
