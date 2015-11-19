package org.batfish.representation;

import org.batfish.util.NamedStructure;

public final class IpsecVpn extends NamedStructure {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private Interface _bindInterface;

   private IkeGateway _gateway;

   private IpsecPolicy _ipsecPolicy;

   public IpsecVpn(String name) {
      super(name);
   }

   public Interface getBindInterface() {
      return _bindInterface;
   }

   public IkeGateway getGateway() {
      return _gateway;
   }

   public IpsecPolicy getIpsecPolicy() {
      return _ipsecPolicy;
   }

   public void setBindInterface(Interface iface) {
      _bindInterface = iface;
   }

   public void setGateway(IkeGateway gateway) {
      _gateway = gateway;
   }

   public void setIpsecPolicy(IpsecPolicy ipsecPolicy) {
      _ipsecPolicy = ipsecPolicy;
   }

}
