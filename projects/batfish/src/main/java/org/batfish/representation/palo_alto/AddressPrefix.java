package org.batfish.representation.palo_alto;

import javax.annotation.Nullable;
import org.batfish.datamodel.Prefix;

/** Represents an address-prefix used in match policy rules */
public class AddressPrefix {
  @Nullable private Prefix _prefix;
  @Nullable private Boolean _exact;

  @Nullable
  public Prefix getPrefix() {
    return _prefix;
  }

  public void setPrefix(@Nullable Prefix prefix) {
    _prefix = prefix;
  }

  @Nullable
  public Boolean getExact() {
    return _exact;
  }

  public void setExact(@Nullable Boolean exact) {
    _exact = exact;
  }
}
