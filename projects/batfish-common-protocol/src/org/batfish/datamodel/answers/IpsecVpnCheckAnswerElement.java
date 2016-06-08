package org.batfish.datamodel.answers;

import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.batfish.common.Pair;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpsecVpn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class IpsecVpnCheckAnswerElement implements AnswerElement {

   public static class IpsecVpnPair extends
         Pair<Pair<String, String>, Pair<String, String>> {

      private static final String HOSTNAME1_VAR = "hostname1";

      private static final String HOSTNAME2_VAR = "hostname2";

      private static final String IPSEC_VPN1_VAR = "ipsecVpn1";

      private static final String IPSEC_VPN2_VAR = "ipsecVpn2";

      /**
       *
       */
      private static final long serialVersionUID = 1L;

      @JsonCreator
      public IpsecVpnPair(@JsonProperty(HOSTNAME1_VAR) String hostname1,
            @JsonProperty(IPSEC_VPN1_VAR) String ipsecVpn1,
            @JsonProperty(HOSTNAME2_VAR) String hostname2,
            @JsonProperty(IPSEC_VPN2_VAR) String ipsecVpn2) {
         super(new Pair<String, String>(hostname1, ipsecVpn1),
               new Pair<String, String>(hostname2, ipsecVpn2));
      }

      @JsonProperty(HOSTNAME1_VAR)
      public String getHostname1() {
         return _first.getFirst();
      }

      @JsonProperty(HOSTNAME2_VAR)
      public String getHostname2() {
         return _second.getFirst();
      }

      @JsonProperty(IPSEC_VPN1_VAR)
      public String getIpsecVpn1() {
         return _first.getSecond();
      }

      @JsonProperty(IPSEC_VPN2_VAR)
      public String getIpsecVpn2() {
         return _second.getSecond();
      }

   }

   private SortedMap<String, SortedSet<IpsecVpnPair>> _incompatibleIkeProposals;

   private SortedMap<String, SortedSet<IpsecVpnPair>> _incompatibleIpsecProposals;

   private SortedMap<String, SortedSet<String>> _missingEndpoint;

   private SortedMap<String, SortedSet<IpsecVpnPair>> _nonUniqueEndpoint;

   private SortedMap<String, SortedSet<IpsecVpnPair>> _preSharedKeyMismatch;

   public IpsecVpnCheckAnswerElement() {
      _incompatibleIkeProposals = new TreeMap<String, SortedSet<IpsecVpnPair>>();
      _incompatibleIpsecProposals = new TreeMap<String, SortedSet<IpsecVpnPair>>();
      _missingEndpoint = new TreeMap<String, SortedSet<String>>();
      _nonUniqueEndpoint = new TreeMap<String, SortedSet<IpsecVpnPair>>();
      _preSharedKeyMismatch = new TreeMap<String, SortedSet<IpsecVpnPair>>();
   }

   public void addIpsecVpn(SortedMap<String, SortedSet<String>> ipsecVpnMap,
         Configuration c, IpsecVpn ipsecVpn) {
      String hostname = c.getHostname();
      SortedSet<String> ipsecVpnsByHostname = ipsecVpnMap.get(hostname);
      if (ipsecVpnsByHostname == null) {
         ipsecVpnsByHostname = new TreeSet<String>();
         ipsecVpnMap.put(hostname, ipsecVpnsByHostname);
      }
      String ipsecVpnName = ipsecVpn.getName();
      ipsecVpnsByHostname.add(ipsecVpnName);
   }

   public void addIpsecVpnPair(
         SortedMap<String, SortedSet<IpsecVpnPair>> ipsecVpnPairMap,
         Configuration c, IpsecVpn ipsecVpn, IpsecVpn remoteIpsecVpn) {
      String hostname = c.getHostname();
      SortedSet<IpsecVpnPair> ipsecVpnPairsByHostname = ipsecVpnPairMap
            .get(hostname);
      if (ipsecVpnPairsByHostname == null) {
         ipsecVpnPairsByHostname = new TreeSet<IpsecVpnPair>();
         ipsecVpnPairMap.put(hostname, ipsecVpnPairsByHostname);
      }
      String ipsecVpnName = ipsecVpn.getName();
      String remoteHostname = ipsecVpn.getRemoteIpsecVpn().getOwner()
            .getHostname();
      String remoteIpsecVpnName = remoteIpsecVpn.getName();
      ipsecVpnPairsByHostname.add(new IpsecVpnPair(hostname, ipsecVpnName,
            remoteHostname, remoteIpsecVpnName));
   }

   public SortedMap<String, SortedSet<IpsecVpnPair>> getIncompatibleIkeProposals() {
      return _incompatibleIkeProposals;
   }

   public SortedMap<String, SortedSet<IpsecVpnPair>> getIncompatibleIpsecProposals() {
      return _incompatibleIpsecProposals;
   }

   public SortedMap<String, SortedSet<String>> getMissingEndpoint() {
      return _missingEndpoint;
   }

   public SortedMap<String, SortedSet<IpsecVpnPair>> getNonUniqueEndpoint() {
      return _nonUniqueEndpoint;
   }

   public SortedMap<String, SortedSet<IpsecVpnPair>> getPreSharedKeyMismatch() {
      return _preSharedKeyMismatch;
   }

   public void setIncompatibleIkeProposals(
         SortedMap<String, SortedSet<IpsecVpnPair>> incompatibleIkeProposals) {
      _incompatibleIkeProposals = incompatibleIkeProposals;
   }

   public void setIncompatibleIpsecProposals(
         SortedMap<String, SortedSet<IpsecVpnPair>> incompatibleIpsecProposals) {
      _incompatibleIpsecProposals = incompatibleIpsecProposals;
   }

   public void setMissingEndpoint(
         SortedMap<String, SortedSet<String>> missingEndpoint) {
      _missingEndpoint = missingEndpoint;
   }

   public void setNonUniqueEndpoint(
         SortedMap<String, SortedSet<IpsecVpnPair>> nonUniqueEndpoint) {
      _nonUniqueEndpoint = nonUniqueEndpoint;
   }

   public void setPreSharedKeyMismatch(
         SortedMap<String, SortedSet<IpsecVpnPair>> preSharedKeyMismatch) {
      _preSharedKeyMismatch = preSharedKeyMismatch;
   }

}
