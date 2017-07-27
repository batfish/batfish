package org.batfish.representation.juniper;

import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.IpAccessListLine;

public interface Application {

  void applyTo(IpAccessListLine srcLine, List<IpAccessListLine> lines, Warnings w);

  boolean getIpv6();
}
