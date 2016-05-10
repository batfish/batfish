package org.batfish.representation;

import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.Ip;

public class IkeGateway extends ComparableStructure<String> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private Ip _address;

   private Interface _externalInterface;

   private IkePolicy _ikePolicy;

   private Ip _localAddress;

   private String _localId;

   private String _remoteId;

   public IkeGateway(String name) {
      super(name);
   }

   public Ip getAddress() {
      return _address;
   }

   public Interface getExternalInterface() {
      return _externalInterface;
   }

   public IkePolicy getIkePolicy() {
      return _ikePolicy;
   }

   public Ip getLocalAddress() {
      return _localAddress;
   }

   public String getLocalId() {
      return _localId;
   }

   public String getRemoteId() {
      return _remoteId;
   }

   public void setAddress(Ip address) {
      _address = address;
   }

   public void setExternalInterface(Interface externalInterface) {
      _externalInterface = externalInterface;
   }

   public void setIkePolicy(IkePolicy ikePolicy) {
      _ikePolicy = ikePolicy;
   }

   public void setLocalAddress(Ip localAddress) {
      _localAddress = localAddress;
   }

   public void setLocalId(String localId) {
      _localId = localId;
   }

   public void setRemoteId(String remoteId) {
      _remoteId = remoteId;
   }

}
