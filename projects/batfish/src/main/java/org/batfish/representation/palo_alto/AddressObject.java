package org.batfish.representation.palo_alto;

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
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Ip;
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

  @Nullable private String _description;
  @Nonnull private final String _name;
  @Nonnull private final Set<String> _tags;
  @Nullable private Type _type;

  // Only one can be set
  @Nullable private Ip _ip;
  @Nullable private Range<Ip> _ipRange;
  @Nullable private IpPrefix _prefix;

  public AddressObject(String name) {
    _name = name;
    _tags = new HashSet<>();
  }

  private void clearAddress() {
    _ip = null;
    _ipRange = null;
    _prefix = null;
  }

  @Nullable
  public String getDescription() {
    return _description;
  }

  @Nonnull
  public IpSpace getIpSpace() {
    if (_ip != null) {
      return _ip.toIpSpace();
    } else if (_prefix != null) {
      return _prefix.getPrefix().toIpSpace();
    } else if (_ipRange != null) {
      return IpRange.range(_ipRange.lowerEndpoint(), _ipRange.upperEndpoint());
    }
    return EmptyIpSpace.INSTANCE;
  }

  /**
   * Convert this address object into a {@link ConcreteInterfaceAddress} if possible. For some types
   * of address objects this is not possible and returns {@code null} instead.
   */
  @Nullable
  public ConcreteInterfaceAddress toConcreteInterfaceAddress(Warnings w) {
    if (_ip != null) {
      return ConcreteInterfaceAddress.create(_ip, Prefix.MAX_PREFIX_LENGTH);
    } else if (_prefix != null) {
      return ConcreteInterfaceAddress.create(
          _prefix.getIp(), _prefix.getPrefix().getPrefixLength());
    }
    // Cannot convert ambiguous address objects like ip-range objects to concrete iface address
    w.redFlag(
        String.format(
            "Could not convert %s AddressObject '%s' to ConcreteInterfaceAddress.", _type, _name));
    return null;
  }

  /** Returns all addresses owned by this address object as an IP {@link RangeSet}. */
  @Nonnull
  public RangeSet<Ip> getAddressAsRangeSet() {
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

  @Nullable
  public Ip getIp() {
    return _ip;
  }

  @Nullable
  public Range<Ip> getIpRange() {
    return _ipRange;
  }

  /**
   * Get {@link IpPrefix} for this address, if it exists. This can be used for specifying an
   * interface address, so it preserves the initial (not canonical) base ip address.
   */
  @Nullable
  public IpPrefix getIpPrefix() {
    return _prefix;
  }

  @Nonnull
  public String getName() {
    return _name;
  }

  @Nonnull
  public Set<String> getTags() {
    return _tags;
  }

  @Nullable
  public Type getType() {
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

  public void setPrefix(@Nullable IpPrefix prefix) {
    _type = prefix == null ? null : Type.PREFIX;
    clearAddress();
    _prefix = prefix;
  }
}
