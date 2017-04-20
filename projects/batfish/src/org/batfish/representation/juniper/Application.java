package org.batfish.representation.juniper;

import java.util.List;

import org.batfish.datamodel.IpAccessListLine;
import org.batfish.common.Warnings;

public interface Application {

   void applyTo(IpAccessListLine srcLine, List<IpAccessListLine> lines,
         Warnings w);

   boolean getIpv6();

}
