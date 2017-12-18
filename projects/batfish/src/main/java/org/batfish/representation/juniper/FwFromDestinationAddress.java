package org.batfish.representation.juniper;

import com.google.common.collect.Iterables;
import java.util.Collections;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;

public final class FwFromDestinationAddress extends FwFrom {

  /** */
  private static final long serialVersionUID = 1L;

  private final Prefix _prefix;

  public FwFromDestinationAddress(Prefix prefix) {
    _prefix = prefix;
  }

  @Override
  public void applyTo(IpAccessListLine line, JuniperConfiguration jc, Warnings w, Configuration c) {
    line.setDstIps(
        Iterables.concat(line.getDstIps(), Collections.singleton(new IpWildcard(_prefix))));
  }

  public Prefix getPrefix() {
    return _prefix;
  }
}
