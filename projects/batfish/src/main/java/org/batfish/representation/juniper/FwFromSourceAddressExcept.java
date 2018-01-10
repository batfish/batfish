package org.batfish.representation.juniper;

import com.google.common.collect.Iterables;
import java.util.Collections;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;

public final class FwFromSourceAddressExcept extends FwFrom {

  /** */
  private static final long serialVersionUID = 1L;

  private final Prefix _prefix;

  public FwFromSourceAddressExcept(Prefix prefix) {
    _prefix = prefix;
  }

  @Override
  public void applyTo(IpAccessListLine line, JuniperConfiguration jc, Warnings w, Configuration c) {
    IpWildcard wildcard = new IpWildcard(_prefix);
    line.setNotSrcIps(Iterables.concat(line.getNotSrcIps(), Collections.singleton(wildcard)));
  }

  public Prefix getPrefix() {
    return _prefix;
  }
}
