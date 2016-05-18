package org.batfish.datamodel.answers;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.batfish.common.Pair;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpsecVpn;

public class IpsecVpnCheckAnswerElement implements AnswerElement {

   public static class IpsecVpnPair extends
         Pair<Pair<String, String>, Pair<String, String>> {

      /**
       *
       */
      private static final long serialVersionUID = 1L;

      public IpsecVpnPair(String hostname1, String ipsecVpn1, String hostname2,
            String ipsecVpn2) {
         super(new Pair<String, String>(hostname1, ipsecVpn1),
               new Pair<String, String>(hostname2, ipsecVpn2));
      }

      public String getHostname1() {
         return _first.getFirst();
      }

      public String getHostname2() {
         return _second.getFirst();
      }

      public String getIpsecVpn1() {
         return _first.getSecond();
      }

      public String getIpsecVpn2() {
         return _second.getSecond();
      }

   }

   private Map<String, Set<IpsecVpnPair>> _incompatibleIkeProposals;

   private Map<String, Set<IpsecVpnPair>> _incompatibleIpsecProposals;

   private Map<String, Set<String>> _missingEndpoint;

   private Map<String, Set<IpsecVpnPair>> _nonUniqueEndpoint;

   private Map<String, Set<IpsecVpnPair>> _preSharedKeyMismatch;

   public IpsecVpnCheckAnswerElement() {
      _incompatibleIkeProposals = new TreeMap<String, Set<IpsecVpnPair>>();
      _incompatibleIpsecProposals = new TreeMap<String, Set<IpsecVpnPair>>();
      _missingEndpoint = new TreeMap<String, Set<String>>();
      _nonUniqueEndpoint = new TreeMap<String, Set<IpsecVpnPair>>();
      _preSharedKeyMismatch = new TreeMap<String, Set<IpsecVpnPair>>();
   }

   public void addIpsecVpn(Map<String, Set<String>> ipsecVpnMap,
         Configuration c, IpsecVpn ipsecVpn) {
      String hostname = c.getHostname();
      Set<String> ipsecVpnsByHostname = ipsecVpnMap.get(hostname);
      if (ipsecVpnsByHostname == null) {
         ipsecVpnsByHostname = new TreeSet<String>();
         ipsecVpnMap.put(hostname, ipsecVpnsByHostname);
      }
      String ipsecVpnName = ipsecVpn.getName();
      ipsecVpnsByHostname.add(ipsecVpnName);
   }

   public void addIpsecVpnPair(Map<String, Set<IpsecVpnPair>> ipsecVpnPairMap,
         Configuration c, IpsecVpn ipsecVpn, IpsecVpn remoteIpsecVpn) {
      String hostname = c.getHostname();
      Set<IpsecVpnPair> ipsecVpnPairsByHostname = ipsecVpnPairMap.get(hostname);
      if (ipsecVpnPairsByHostname == null) {
         ipsecVpnPairsByHostname = new TreeSet<IpsecVpnPair>();
         ipsecVpnPairMap.put(hostname, ipsecVpnPairsByHostname);
      }
      String ipsecVpnName = ipsecVpn.getName();
      String remoteHostname = ipsecVpn.getRemoteIpsecVpn().getConfiguration()
            .getHostname();
      String remoteIpsecVpnName = remoteIpsecVpn.getName();
      ipsecVpnPairsByHostname.add(new IpsecVpnPair(hostname, ipsecVpnName,
            remoteHostname, remoteIpsecVpnName));
   }

   public Map<String, Set<IpsecVpnPair>> getIncompatibleIkeProposals() {
      return _incompatibleIkeProposals;
   }

   public Map<String, Set<IpsecVpnPair>> getIncompatibleIpsecProposals() {
      return _incompatibleIpsecProposals;
   }

   public Map<String, Set<String>> getMissingEndpoint() {
      return _missingEndpoint;
   }

   public Map<String, Set<IpsecVpnPair>> getNonUniqueEndpoint() {
      return _nonUniqueEndpoint;
   }

   public Map<String, Set<IpsecVpnPair>> getPreSharedKeyMismatch() {
      return _preSharedKeyMismatch;
   }

   public void setIncompatibleIkeProposals(
         Map<String, Set<IpsecVpnPair>> incompatibleIkeProposals) {
      _incompatibleIkeProposals = incompatibleIkeProposals;
   }

   public void setIncompatibleIpsecProposals(
         Map<String, Set<IpsecVpnPair>> incompatibleIpsecProposals) {
      _incompatibleIpsecProposals = incompatibleIpsecProposals;
   }

   public void setMissingEndpoint(Map<String, Set<String>> missingEndpoint) {
      _missingEndpoint = missingEndpoint;
   }

   public void setNonUniqueEndpoint(
         Map<String, Set<IpsecVpnPair>> nonUniqueEndpoint) {
      _nonUniqueEndpoint = nonUniqueEndpoint;
   }

   public void setPreSharedKeyMismatch(
         Map<String, Set<IpsecVpnPair>> preSharedKeyMismatch) {
      _preSharedKeyMismatch = preSharedKeyMismatch;
   }

}
