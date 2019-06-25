package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix6;

@ParametersAreNonnullByDefault
public class Ipv6Origin implements Serializable {

  private final @Nonnull Prefix6 _prefix6;

  public Ipv6Origin(Prefix6 prefix6) {
    _prefix6 = prefix6;
  }

  public @Nonnull Prefix6 getPrefix() {
    return _prefix6;
  }
}
