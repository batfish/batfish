package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix;

@ParametersAreNonnullByDefault
public class Ipv4Origin implements Serializable {

  private static final long serialVersionUID = 1L;

  private final @Nonnull Prefix _prefix;

  public Ipv4Origin(Prefix prefix) {
    _prefix = prefix;
  }

  public @Nonnull Prefix getPrefix() {
    return _prefix;
  }
}
