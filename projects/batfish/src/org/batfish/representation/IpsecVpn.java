package org.batfish.representation;

import org.batfish.util.NamedStructure;

public final class IpsecVpn extends NamedStructure {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private Interface _bindInterface;

   private Configuration _configuration;

   private IkeGateway _gateway;

   private IpsecPolicy _ipsecPolicy;

   private transient IpsecVpn _remoteIpsecVpn;

   public IpsecVpn(String name, Configuration configuration) {
      super(name);
      _configuration = configuration;
   }

   public Boolean compatibleIkeProposals(IpsecVpn remoteIpsecVpn) {
      for (IkeProposal lhs : _gateway.getIkePolicy().getProposals().values()) {
         for (IkeProposal rhs : remoteIpsecVpn.getGateway().getIkePolicy()
               .getProposals().values()) {
            if (lhs.compatibleWith(rhs)) {
               return true;
            }
         }
      }
      return false;
   }

   public Boolean compatibleIpsecProposals(IpsecVpn remoteIpsecVpn) {
      if (_ipsecPolicy.getPfsKeyGroup() != remoteIpsecVpn.getIpsecPolicy()
            .getPfsKeyGroup()) {
         return false;
      }
      for (IpsecProposal lhs : _ipsecPolicy.getProposals().values()) {
         for (IpsecProposal rhs : remoteIpsecVpn.getIpsecPolicy()
               .getProposals().values()) {
            if (lhs.compatibleWith(rhs)) {
               return true;
            }
         }
      }
      return false;
   }

   public Interface getBindInterface() {
      return _bindInterface;
   }

   public Configuration getConfiguration() {
      return _configuration;
   }

   public IkeGateway getGateway() {
      return _gateway;
   }

   public IpsecPolicy getIpsecPolicy() {
      return _ipsecPolicy;
   }

   public IpsecVpn getRemoteIpsecVpn() {
      return _remoteIpsecVpn;
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

   public void setRemoteIpsecVpn(IpsecVpn remoteIpsecVpn) {
      _remoteIpsecVpn = remoteIpsecVpn;
   }

}
