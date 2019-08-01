package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.Set;

/** A match on the presence of any of a set of layer-3 ip-options. */
public class FwFromIpOptions implements Serializable {

  public FwFromIpOptions() {
    _options = EnumSet.noneOf(IpOptions.class);
  }

  public Set<IpOptions> getOptions() {
    return _options;
  }

  private final EnumSet<IpOptions> _options;
}
