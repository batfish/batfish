package org.batfish.representation.palo_alto;

import com.google.common.collect.BoundType;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.EmptyIp6Space;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.Ip6Space;
import org.batfish.datamodel.IpRange;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.Prefix;

/** Represents a Palo Alto address object */
@ParametersAreNonnullByDefault
public final class AddressObject implements Serializable {

  public enum Type {
    IP,
    IP_RANGE,
    PREFIX
  }

  private @Nullable String _description;
  private final @Nonnull String _name;
  private final @Nonnull Set<String> _tags;
  private @Nullable Type _type;

  // Only one can be set
  private @Nullable Ip _ip;
  private @Nullable Ip6 _ip6;
  private @Nullable Range<Ip> _ipRange;
  private @Nullable Range<Ip6> _ipRange6;
  private @Nullable IpPrefix _prefix;
  private @Nullable Ip6Prefix _prefix6;

  public AddressObject(String name) {
    _name = name;
    _tags = new HashSet<>();
  }

  private void clearAddress() {
    _ip = null;
    _ip6 = null;
    _ipRange = null;
    _ipRange6 = null;
    _prefix = null;
    _prefix6 = null;
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public @Nonnull IpSpace getIpSpace() {
    if (_ip != null) {
      return _ip.toIpSpace();
    } else if (_prefix != null) {
      return _prefix.getPrefix().toIpSpace();
    } else if (_ipRange != null) {
      assert _ipRange.lowerBoundType() == BoundType.CLOSED
          && _ipRange.upperBoundType() == BoundType.CLOSED;
      return IpRange.range(_ipRange.lowerEndpoint(), _ipRange.upperEndpoint());
    }
    return EmptyIpSpace.INSTANCE;
  }

  public @Nonnull Ip6Space getIp6Space() {
    if (_ip6 != null) {
      return _ip6.toPrefix6().toIp6Space();
    } else if (_prefix6 != null) {
      return _prefix6.getPrefix().toIp6Space();
    }
    // Note: Ip6Range is not yet supported as Ip6Space implementation in datamodel
    return EmptyIp6Space.INSTANCE;
  }

  /**
   * Convert this address object into a {@link ConcreteInterfaceAddress} if possible. For some types
   * of address objects this is not possible and returns {@code null} instead.
   */
  public @Nullable ConcreteInterfaceAddress toConcreteInterfaceAddress(Warnings w) {
    if (_ip != null) {
      return ConcreteInterfaceAddress.create(_ip, Prefix.MAX_PREFIX_LENGTH);
    } else if (_prefix != null) {
      return ConcreteInterfaceAddress.create(
          _prefix.getIp(), _prefix.getPrefix().getPrefixLength());
    }
    // Cannot convert ambiguous address objects like ip-range objects to concrete iface address
    w.redFlagf(
        "Could not convert %s AddressObject '%s' to ConcreteInterfaceAddress.", _type, _name);
    return null;
  }

  /** Returns all addresses owned by this address object as an IP {@link RangeSet}. */
  public @Nonnull RangeSet<Ip> getAddressAsRangeSet() {
    if (_ip != null) {
      return ImmutableRangeSet.of(Range.singleton(_ip));
    } else if (_prefix != null) {
      return ImmutableRangeSet.of(
          Range.closed(_prefix.getPrefix().getStartIp(), _prefix.getPrefix().getEndIp()));
    } else if (_ipRange != null) {
      return ImmutableRangeSet.of(_ipRange);
    }
    return ImmutableRangeSet.of();
  }

  public @Nullable Ip getIp() {
    return _ip;
  }

  public @Nullable Range<Ip> getIpRange() {
    return _ipRange;
  }

  /**
   * Get {@link IpPrefix} for this address, if it exists. This can be used for specifying an
   * interface address, so it preserves the initial (not canonical) base ip address.
   */
  public @Nullable IpPrefix getIpPrefix() {
    return _prefix;
  }

  public @Nullable Ip6 getIp6() {
    return _ip6;
  }

  public @Nullable Ip6Prefix getPrefix6() {
    return _prefix6;
  }

  public @Nullable Range<Ip6> getIpRange6() {
    return _ipRange6;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull Set<String> getTags() {
    return _tags;
  }

  public @Nullable Type getType() {
    return _type;
  }

  public void setDescription(String description) {
    _description = description;
  }

  public void setIp(@Nullable Ip ip) {
    _type = ip == null ? null : Type.IP;
    clearAddress();
    _ip = ip;
  }

  public void setIpRange(@Nullable Range<Ip> ipRange) {
    _type = ipRange == null ? null : Type.IP_RANGE;
    clearAddress();
    _ipRange = ipRange;
  }

  public void setIpRange6(@Nullable Range<Ip6> ipRange6) {
    _type = ipRange6 == null ? null : Type.IP_RANGE;
    clearAddress();
    _ipRange6 = ipRange6;
  }

  public void setPrefix(@Nullable IpPrefix prefix) {
    _type = prefix == null ? null : Type.PREFIX;
    clearAddress();
    _prefix = prefix;
  }

  public void setIp(@Nullable Ip6 ip) {
    _type = ip == null ? null : Type.IP;
    clearAddress();
    _ip6 = ip;
  }

  public void setPrefix(@Nullable Ip6Prefix prefix) {
    _type = prefix == null ? null : Type.PREFIX;
    clearAddress();
    _prefix6 = prefix;
  }
}
