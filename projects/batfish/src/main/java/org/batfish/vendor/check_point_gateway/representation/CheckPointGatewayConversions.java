package org.batfish.vendor.check_point_gateway.representation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpRange;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.vendor.check_point_management.AddressRange;
import org.batfish.vendor.check_point_management.Network;

/** Utility class for Checkpoint conversion methods */
public final class CheckPointGatewayConversions {
  /**
   * Converts the given {@link AddressRange} into an {@link IpSpace}, or returns {@code null} if the
   * address range does not represent an IPv4 space.
   */
  static @Nullable IpSpace toIpSpace(AddressRange addressRange) {
    // TODO Convert IPv6 address ranges
    if (addressRange.getIpv4AddressFirst() == null || addressRange.getIpv4AddressLast() == null) {
      return null;
    }
    return IpRange.range(addressRange.getIpv4AddressFirst(), addressRange.getIpv4AddressLast());
  }

  /** Converts the given {@link Network} into an {@link IpSpace}. */
  static @Nonnull IpSpace toIpSpace(Network network) {
    // TODO Network objects also have a "mask-length4" that we don't currently extract.
    //  If network objects always represent valid Prefixes, it may be simpler to extract
    //  that instead of subnet-mask and convert the network to a PrefixIpSpace.
    // In Network, the mask has bits that matter set, but IpWildcard interprets set mask bits as
    // "don't care". Flip mask to convert to IpWildcard.
    long flippedMask = network.getSubnetMask().asLong() ^ Ip.MAX.asLong();
    return IpWildcard.ipWithWildcardMask(network.getSubnet4(), flippedMask).toIpSpace();
  }

  private CheckPointGatewayConversions() {}
}
