package org.batfish.datamodel;

import java.util.Set;
import java.util.TreeSet;

import org.batfish.common.Pair;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
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
      IkeProposal activeIkeProposal = getActiveIkeProposal(remoteIpsecVpn);
      return activeIkeProposal != null;
   }

   public Boolean compatibleIpsecProposals(IpsecVpn remoteIpsecVpn) {
      // handle dynamic pfs key group
      if (_ipsecPolicy.getPfsKeyGroupDynamicIke()) {
         IkeProposal activeProposal = getActiveIkeProposal(remoteIpsecVpn);
         if (activeProposal == null) {
            return false;
         }
         if (!remoteIpsecVpn.getIpsecPolicy().getPfsKeyGroupDynamicIke()) {
            // remote vpn uses static pfs key group.
            if (!activeProposal.getDiffieHellmanGroup().equals(
                  remoteIpsecVpn.getIpsecPolicy().getPfsKeyGroup())) {
               return false;
            }
         }
         // else remote vpn also uses dynamic pfs key group. They must agree as
         // long as a compatible proposal is selected, which has already
         // happened.
      }
      else if (_ipsecPolicy.getPfsKeyGroup() != remoteIpsecVpn.getIpsecPolicy()
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

   private IkeProposal getActiveIkeProposal(IpsecVpn remoteIpsecVpn) {
      for (IkeProposal lhs : _gateway.getIkePolicy().getProposals().values()) {
         for (IkeProposal rhs : remoteIpsecVpn.getGateway().getIkePolicy()
               .getProposals().values()) {
            if (lhs.compatibleWith(rhs)) {
               return lhs;
            }
         }
      }
      return null;
   }

   public Interface getBindInterface() {
      return _bindInterface;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Set<IpsecVpn> getCandidateRemoteIpsecVpns() {
      return _candidateRemoteIpsecVpns;
   }

   public Configuration getConfiguration() {
      return _first;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public IkeGateway getGateway() {
      return _gateway;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public IpsecPolicy getIpsecPolicy() {
      return _ipsecPolicy;
   }

   public String getName() {
      return _second;
   }

   @JsonIdentityReference(alwaysAsId = true)
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
