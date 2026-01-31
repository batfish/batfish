package org.batfish.vendor.arista.representation;

import javax.annotation.Nonnull;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;

public class WildcardAddressSpecifier implements AccessListAddressSpecifier {

  private final @Nonnull IpWildcard _ipWildcard;

  public @Nonnull IpWildcard getIpWildcard() {
    return _ipWildcard;
  }

  public WildcardAddressSpecifier(@Nonnull IpWildcard ipWildcard) {
    _ipWildcard = ipWildcard;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof WildcardAddressSpecifier)) {
      return false;
    }
    WildcardAddressSpecifier that = (WildcardAddressSpecifier) o;
    return _ipWildcard.equals(that._ipWildcard);
  }

  @Override
  public int hashCode() {
    return _ipWildcard.hashCode();
  }

  @Override
  public @Nonnull IpSpace toIpSpace() {
    return _ipWildcard.toIpSpace();
  }
}
