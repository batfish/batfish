package org.batfish.vendor.cisco_nxos.representation;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip6;

/** Describes the configuration for an IP address assigned to an {@link Interface}. */
@ParametersAreNonnullByDefault
public final class InterfaceIpv6AddressWithAttributes implements Serializable {

  public InterfaceIpv6AddressWithAttributes(Ip6 address6, int prefixLength) {
    _address6 = address6;
    _prefixLength = prefixLength;
  }

  public static @Nonnull InterfaceIpv6AddressWithAttributes parse(String address) {
    String[] parts = address.split("/");
    checkArgument(parts.length == 2, "Invalid Prefix6 string '%s'", address);
    Ip6 address6 = Ip6.parse(parts[0]);
    int prefixLength = Integer.parseInt(parts[1]);
    return new InterfaceIpv6AddressWithAttributes(address6, prefixLength);
  }

  public @Nonnull Ip6 getAddress6() {
    return _address6;
  }

  public long getTag() {
    return _tag;
  }

  public void setTag(@Nullable Long tag) {
    _tag = firstNonNull(tag, 0L);
  }

  //////////////////////////////////////////////////
  ///// Private implementation details         /////
  //////////////////////////////////////////////////

  private final @Nonnull Ip6 _address6;
  private final int _prefixLength;
  private long _tag;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof InterfaceIpv6AddressWithAttributes)) {
      return false;
    }
    InterfaceIpv6AddressWithAttributes that = (InterfaceIpv6AddressWithAttributes) o;
    return _address6.equals(that._address6)
        && _prefixLength == that._prefixLength
        && _tag == that._tag;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_address6, _prefixLength, _tag);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .omitNullValues()
        .add("address6", _address6)
        .add("prefixLength", _prefixLength)
        .add("tag", _tag)
        .toString();
  }
}
