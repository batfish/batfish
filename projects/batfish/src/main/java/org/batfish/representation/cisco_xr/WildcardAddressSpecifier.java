package org.batfish.representation.cisco_xr;

import java.util.Objects;
import javax.annotation.Nonnull;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;

public class WildcardAddressSpecifier implements AccessListAddressSpecifier {

  private final IpWildcard _ipWildcard;

  public IpWildcard getIpWildcard() {
    return _ipWildcard;
  }

  public WildcardAddressSpecifier(IpWildcard ipWildcard) {
    _ipWildcard = ipWildcard;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof WildcardAddressSpecifier)) {
      return false;
    }
    WildcardAddressSpecifier that = (WildcardAddressSpecifier) o;
    return Objects.equals(_ipWildcard, that._ipWildcard);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_ipWildcard);
  }

  @Override
  @Nonnull
  public IpSpace toIpSpace() {
    return _ipWildcard.toIpSpace();
  }
}
