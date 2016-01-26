package org.batfish.representation.vyos;

import java.util.Map.Entry;
import java.util.Set;

import org.batfish.common.BatfishException;
import org.batfish.main.ConfigurationFormat;
import org.batfish.main.Warnings;
import org.batfish.representation.Configuration;
import org.batfish.representation.IkeGateway;
import org.batfish.representation.IkePolicy;
import org.batfish.representation.Ip;
import org.batfish.representation.IpsecPolicy;
import org.batfish.representation.IpsecProposal;
import org.batfish.representation.IpsecProtocol;
import org.batfish.representation.IpsecVpn;
import org.batfish.representation.LineAction;
import org.batfish.representation.PolicyMap;
import org.batfish.representation.PolicyMapAction;
import org.batfish.representation.PolicyMapClause;
import org.batfish.representation.Prefix;
import org.batfish.representation.RouteFilterLine;
import org.batfish.representation.RouteFilterList;
import org.batfish.representation.VendorConfiguration;
import org.batfish.representation.VendorConversionException;

public class VyosVendorConfiguration extends VyosConfiguration implements
      VendorConfiguration {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private Configuration _c;

   private ConfigurationFormat _format;

   private Set<String> _unimplementedFeatures;

   private Warnings _w;

   private void convertInterfaces() {
      for (Entry<String, Interface> e : _interfaces.entrySet()) {
         String name = e.getKey();
         Interface iface = e.getValue();
         org.batfish.representation.Interface newIface = toInterface(iface);
         _c.getInterfaces().put(name, newIface);
      }
   }

   private void convertPrefixLists() {
      for (Entry<String, PrefixList> e : _prefixLists.entrySet()) {
         String name = e.getKey();
         PrefixList prefixList = e.getValue();
         RouteFilterList routeFilterList = toRouteFilterList(prefixList);
         _c.getRouteFilterLists().put(name, routeFilterList);
      }
   }

   private void convertRouteMaps() {
      for (Entry<String, RouteMap> e : _routeMaps.entrySet()) {
         String name = e.getKey();
         RouteMap routeMap = e.getValue();
         PolicyMap policyMap = toPolicyMap(routeMap);
         _c.getPolicyMaps().put(name, policyMap);
      }
   }

   private void convertVpns() {
      for (Entry<Ip, IpsecPeer> ipsecPeerEntry : _ipsecPeers.entrySet()) {
         // create ipsecvpn and ikegateway to correspond roughly to vyos ipsec
         // site-to-site peer
         Ip peerAddress = ipsecPeerEntry.getKey();
         IpsecPeer ipsecPeer = ipsecPeerEntry.getValue();
         String newIpsecVpnName = peerAddress.toString();
         String newIkeGatewayName = newIpsecVpnName;
         IpsecVpn newIpsecVpn = new IpsecVpn(newIpsecVpnName, _c);
         _c.getIpsecVpns().put(newIpsecVpnName, newIpsecVpn);
         IkeGateway newIkeGateway = new IkeGateway(newIkeGatewayName);
         _c.getIkeGateways().put(newIkeGatewayName, newIkeGateway);
         newIpsecVpn.setGateway(newIkeGateway);
         newIkeGateway.setLocalId(ipsecPeer.getAuthenticationId());
         newIkeGateway.setRemoteId(ipsecPeer.getAuthenticationRemoteId());

         // convert the referenced ike group
         String ikeGroupName = ipsecPeer.getIkeGroup();
         IkeGroup ikeGroup = _ikeGroups.get(ikeGroupName);
         if (ikeGroup == null) {
            _w.redFlag("Reference to undefined ike-group: \"" + ikeGroupName
                  + "\"");
         }
         else {
            ikeGroup.getReferers().put(
                  ipsecPeer,
                  "ike group for site-to-site peer: \""
                        + peerAddress.toString() + "\"");
            IkePolicy newIkePolicy = new IkePolicy(ikeGroupName);
            _c.getIkePolicies().put(ikeGroupName, newIkePolicy);
            newIkePolicy.setPreSharedKeyHash(ipsecPeer
                  .getAuthenticationPreSharedSecretHash());

            // convert contained ike proposals
            for (Entry<Integer, IkeProposal> ikeProposalEntry : ikeGroup
                  .getProposals().entrySet()) {
               String newIkeProposalName = ikeGroupName + ":"
                     + Integer.toString(ikeProposalEntry.getKey());
               IkeProposal ikeProposal = ikeProposalEntry.getValue();
               org.batfish.representation.IkeProposal newIkeProposal = new org.batfish.representation.IkeProposal(
                     newIkeProposalName);
               _c.getIkeProposals().put(newIkeProposalName, newIkeProposal);
               newIkePolicy.getProposals().put(newIkeProposalName,
                     newIkeProposal);
               newIkeProposal.setDiffieHellmanGroup(ikeProposal.getDhGroup());
               newIkeProposal.setEncryptionAlgorithm(ikeProposal
                     .getEncryptionAlgorithm());
               newIkeProposal.setLifetimeSeconds(ikeGroup.getLifetimeSeconds());
               newIkeProposal.setAuthenticationAlgorithm(ikeProposal
                     .getHashAlgorithm().toIkeAuthenticationAlgorithm());
               newIkeProposal.setAuthenticationMethod(ipsecPeer
                     .getAuthenticationMode());
            }
         }

         // convert the referenced esp group
         String espGroupName = ipsecPeer.getEspGroup();
         EspGroup espGroup = _espGroups.get(espGroupName);
         if (espGroup == null) {
            _w.redFlag("Reference to undefined esp-group: \"" + espGroupName
                  + "\"");
         }
         else {
            espGroup.getReferers().put(
                  ipsecPeer,
                  "esp-group for ipsec site-to-site peer: \""
                        + peerAddress.toString() + "\"");
            IpsecPolicy newIpsecPolicy = new IpsecPolicy(espGroupName);
            if (espGroup.getPfsSource() == null) {
               espGroup.setPfsSource(PfsSource.IKE_GROUP);
            }
            switch (espGroup.getPfsSource()) {
            case DISABLED:
               break;

            case ESP_GROUP:
               newIpsecPolicy.setPfsKeyGroup(espGroup.getPfsDhGroup());
               break;

            case IKE_GROUP:
               newIpsecPolicy.setPfsKeyGroupDynamicIke(true);
               break;

            default:
               throw new BatfishException("Invalid pfs source");
            }

            // convert contained esp proposals
            for (Entry<Integer, EspProposal> espProposalEntry : espGroup
                  .getProposals().entrySet()) {
               String newIpsecProposalName = espGroupName + ":"
                     + Integer.toString(espProposalEntry.getKey());
               EspProposal espProposal = espProposalEntry.getValue();
               IpsecProposal newIpsecProposal = new IpsecProposal(
                     newIpsecProposalName);
               _c.getIpsecProposals().put(newIpsecProposalName,
                     newIpsecProposal);
               newIpsecPolicy.getProposals().put(newIpsecProposalName,
                     newIpsecProposal);
               newIpsecProposal.setAuthenticationAlgorithm(espProposal
                     .getHashAlgorithm().toIpsecAuthenticationAlgorithm());
               newIpsecProposal.setEncryptionAlgorithm(espProposal
                     .getEncryptionAlgorithm());
               newIpsecProposal.setLifetimeSeconds(espGroup
                     .getLifetimeSeconds());
               newIpsecProposal.setProtocol(IpsecProtocol.ESP);
            }
         }
      }

   }

   @Override
   public Set<String> getUnimplementedFeatures() {
      return _unimplementedFeatures;
   }

   @Override
   public Warnings getWarnings() {
      return _w;
   }

   @Override
   public void setVendor(ConfigurationFormat format) {
      _format = format;
   }

   private org.batfish.representation.Interface toInterface(Interface iface) {
      String name = iface.getName();
      org.batfish.representation.Interface newIface = new org.batfish.representation.Interface(
            name, _c);
      newIface.setActive(true); // TODO: may have to change
      newIface.setBandwidth(iface.getBandwidth());
      newIface.setDescription(iface.getDescription());
      Prefix prefix = iface.getPrefix();
      if (prefix != null) {
         newIface.setPrefix(iface.getPrefix());
         newIface.getAllPrefixes().add(iface.getPrefix());
      }
      return newIface;
   }

   private PolicyMap toPolicyMap(RouteMap routeMap) {
      String name = routeMap.getName();
      PolicyMap policyMap = new PolicyMap(name);
      for (Entry<Integer, RouteMapRule> e : routeMap.getRules().entrySet()) {
         String ruleName = Integer.toString(e.getKey());
         RouteMapRule rule = e.getValue();
         PolicyMapClause clause = new PolicyMapClause();
         clause.setName(ruleName);
         clause.setAction(PolicyMapAction.fromLineAction(rule.getAction()));
         policyMap.getClauses().add(clause);
         for (RouteMapMatch match : rule.getMatches()) {
            match.applyTo(_c, policyMap, clause, _w);
         }
      }
      return policyMap;
   }

   private RouteFilterList toRouteFilterList(PrefixList prefixList) {
      String name = prefixList.getName();
      RouteFilterList newList = new RouteFilterList(name);
      for (PrefixListRule rule : prefixList.getRules().values()) {
         RouteFilterLine newLine = new RouteFilterLine(rule.getAction(),
               rule.getPrefix(), rule.getLengthRange());
         newList.getLines().add(newLine);
      }
      return newList;
   }

   @Override
   public Configuration toVendorIndependentConfiguration(Warnings warnings)
         throws VendorConversionException {
      _c = new Configuration(_hostname);
      _c.setVendor(_format);
      _c.setDefaultCrossZoneAction(LineAction.ACCEPT);
      _c.setDefaultInboundAction(LineAction.ACCEPT);

      convertPrefixLists();
      convertRouteMaps();
      convertInterfaces();
      convertVpns();

      return _c;
   }

}