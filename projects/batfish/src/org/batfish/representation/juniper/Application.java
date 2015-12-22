package org.batfish.representation.juniper;

import java.util.List;

import org.batfish.main.Warnings;
import org.batfish.representation.IpAccessListLine;

public interface Application {

   void applyTo(IpAccessListLine srcLine, List<IpAccessListLine> lines,
         Warnings w);

}
