package org.batfish.representation;

import java.util.Set;
import java.util.TreeSet;

import org.batfish.common.Pair;

public final class IpsecVpn extends Pair<Configuration, String> {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private Interface _bindInterface;

   private transient Set<IpsecVpn> _candidateRemoteIpsecVpns;

   private IkeGateway _gateway;

   private IpsecPolicy _ipsecPolicy;

   private transient IpsecVpn _remoteIpsecVpn;

   public IpsecVpn(String name, Configuration configuration) {
      super(configuration, name);
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

   public Set<IpsecVpn> getCandidateRemoteIpsecVpns() {
      return _candidateRemoteIpsecVpns;
   }

   public Configuration getConfiguration() {
      return _t1;
   }

   public IkeGateway getGateway() {
      return _gateway;
   }

   public IpsecPolicy getIpsecPolicy() {
      return _ipsecPolicy;
   }

   public String getName() {
      return _t2;
   }

   public IpsecVpn getRemoteIpsecVpn() {
      return _remoteIpsecVpn;
   }

   public void initCandidateRemoteVpns() {
      _candidateRemoteIpsecVpns = new TreeSet<IpsecVpn>();
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
