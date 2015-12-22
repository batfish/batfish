package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.Set;

public final class HostInboundSettings implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final Set<HostProtocol> _protocols;

   private final Set<HostSystemService> _services;

   public HostInboundSettings() {
      _protocols = EnumSet.noneOf(HostProtocol.class);
      _services = EnumSet.noneOf(HostSystemService.class);
   }

   public Set<HostProtocol> getProtocols() {
      return _protocols;
   }

   public Set<HostSystemService> getServices() {
      return _services;
   }

}
