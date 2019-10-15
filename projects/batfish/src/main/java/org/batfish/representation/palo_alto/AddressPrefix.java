package org.batfish.representation.palo_alto;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Prefix;

/** Represents an address-prefix used in match policy rules */
public class AddressPrefix implements Serializable {
  @Nonnull private Prefix _prefix;
  private boolean _exact;

  public AddressPrefix(Prefix prefix, boolean exact) {
    _prefix = prefix;
    _exact = exact;
  }

  @Nonnull
  public Prefix getPrefix() {
    return _prefix;
  }

  public boolean getExact() {
    return _exact;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AddressPrefix)) {
      return false;
    }
    AddressPrefix that = (AddressPrefix) o;
    return _exact == that._exact && _prefix.equals(that._prefix);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_prefix, _exact);
  }
}
