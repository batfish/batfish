package org.batfish.representation.juniper;

import com.google.common.collect.Iterables;
import java.util.Collections;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpProtocol;

public final class FwFromProtocol extends FwFrom {

  /** */
  private static final long serialVersionUID = 1L;

  private final IpProtocol _protocol;

  public FwFromProtocol(IpProtocol protocol) {
    _protocol = protocol;
  }

  @Override
  public void applyTo(IpAccessListLine line, JuniperConfiguration jc, Warnings w, Configuration c) {
    line.setIpProtocols(Iterables.concat(line.getIpProtocols(), Collections.singleton(_protocol)));
  }

  public IpProtocol getProtocol() {
    return _protocol;
  }
}
