package org.batfish.representation.juniper;

import java.util.List;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.TcpFlags;
import org.batfish.common.Warnings;

public final class FwFromTcpFlags extends FwFrom {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private List<TcpFlags> _tcpFlags;

   public FwFromTcpFlags(List<TcpFlags> tcpFlags) {
      _tcpFlags = tcpFlags;
   }

   @Override
   public void applyTo(IpAccessListLine line, JuniperConfiguration jc,
         Warnings w, Configuration c) {
      line.getTcpFlags().addAll(_tcpFlags);
   }

}
