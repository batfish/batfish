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
import org.batfish.common.ip.EmptyIpSpace;
import org.batfish.common.ip.Ip;
import org.batfish.datamodel.IpRange;
import org.batfish.common.ip.IpSpace;
import org.batfish.common.ip.Prefix;

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
  @Nullable private Prefix _prefix;

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
      return _prefix.toIpSpace();
    } else if (_ipRange != null) {
      return IpRange.range(_ipRange.lowerEndpoint(), _ipRange.upperEndpoint());
    }
    return EmptyIpSpace.INSTANCE;
  }

  /** Returns all addresses owned by this address object as an IP {@link RangeSet}. */
  @Nonnull
  public RangeSet<Ip> getAddressAsRangeSet() {
    if (_ip != null) {
      return ImmutableRangeSet.of(Range.singleton(_ip));
    } else if (_prefix != null) {
      return ImmutableRangeSet.of(Range.closed(_prefix.getStartIp(), _prefix.getEndIp()));
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

  @Nullable
  public Prefix getPrefix() {
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

  public void setPrefix(@Nullable Prefix prefix) {
    _type = prefix == null ? null : Type.PREFIX;
    clearAddress();
    _prefix = prefix;
  }
}
