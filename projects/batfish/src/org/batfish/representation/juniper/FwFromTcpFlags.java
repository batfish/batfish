package org.batfish.representation.juniper;

import java.util.List;

import org.batfish.main.Warnings;
import org.batfish.representation.Configuration;
import org.batfish.representation.IpAccessListLine;
import org.batfish.representation.TcpFlags;

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
