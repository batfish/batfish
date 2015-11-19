package org.batfish.representation.juniper;

import org.batfish.representation.Ip;
import org.batfish.util.NamedStructure;

public class IkeGateway extends NamedStructure {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private Ip _address;

   private Interface _externalInterface;

   private String _ikePolicy;

   public IkeGateway(String name) {
      super(name);
   }

   public Ip getAddress() {
      return _address;
   }

   public Interface getExternalInterface() {
      return _externalInterface;
   }

   public String getIkePolicy() {
      return _ikePolicy;
   }

   public void setAddress(Ip address) {
      _address = address;
   }

   public void setExternalInterface(Interface externalInterface) {
      _externalInterface = externalInterface;
   }

   public void setIkePolicy(String ikePolicy) {
      _ikePolicy = ikePolicy;
   }

}
