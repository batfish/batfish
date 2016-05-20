package org.batfish.datamodel.answers;

import java.util.EnumSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.collections.RoleSet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class NodesAnswerElement implements AnswerElement {

   @JsonInclude(Include.NON_NULL)
   public static class NodeSummary {

      private Set<String> _asPathAccessLists;

      private Set<String> _communityLists;

      private ConfigurationFormat _configurationFormat;

      private Set<String> _ikeGateways;

      private Set<String> _ikePolicies;

      private Set<String> _ikeProposals;

      private Set<String> _interfaces;

      private Set<String> _ipAccessLists;

      private Set<String> _ipsecPolicies;

      private Set<String> _ipsecProposals;

      private Set<String> _ipsecVpns;

      private Set<String> _policyMaps;

      private RoleSet _roles;

      private Set<String> _routeFilterLists;

      private Set<RoutingProtocol> _routingProtocols;

      private Set<String> _zones;

      public NodeSummary(Configuration node) {
         if (!node.getAsPathAccessLists().isEmpty()) {
            _asPathAccessLists = node.getAsPathAccessLists().keySet();
         }
         if (!node.getCommunityLists().isEmpty()) {
            _communityLists = node.getCommunityLists().keySet();
         }
         _configurationFormat = node.getVendor();
         if (!node.getInterfaces().isEmpty()) {
            _interfaces = node.getInterfaces().keySet();
         }
         if (!node.getIkeGateways().isEmpty()) {
            _ikeGateways = node.getIkeGateways().keySet();
         }
         if (!node.getIkePolicies().isEmpty()) {
            _ikePolicies = node.getIkePolicies().keySet();
         }
         if (!node.getIkeProposals().isEmpty()) {
            _ikeProposals = node.getIkeProposals().keySet();
         }
         if (!node.getIpAccessLists().isEmpty()) {
            _ipAccessLists = node.getIpAccessLists().keySet();
         }
         if (!node.getIpsecPolicies().isEmpty()) {
            _ipsecPolicies = node.getIpsecPolicies().keySet();
         }
         if (!node.getIpsecProposals().isEmpty()) {
            _ipsecProposals = node.getIpsecProposals().keySet();
         }
         if (!node.getIpsecVpns().isEmpty()) {
            _ipsecVpns = node.getIpsecVpns().keySet();
         }
         if (!node.getPolicyMaps().isEmpty()) {
            _policyMaps = node.getPolicyMaps().keySet();
         }
         if (!node.getRoles().isEmpty()) {
            _roles = node.getRoles();
         }
         if (!node.getRouteFilterLists().isEmpty()) {
            _routeFilterLists = node.getRouteFilterLists().keySet();
         }
         _routingProtocols = EnumSet.noneOf(RoutingProtocol.class);
         if (node.getBgpProcess() != null) {
            _routingProtocols.add(RoutingProtocol.BGP);
         }
         if (node.getOspfProcess() != null) {
            _routingProtocols.add(RoutingProtocol.OSPF);
         }
         if (node.getIsisProcess() != null) {
            _routingProtocols.add(RoutingProtocol.ISIS);
         }
         if (!node.getStaticRoutes().isEmpty()) {
            _routingProtocols.add(RoutingProtocol.STATIC);
         }
         if (!node.getGeneratedRoutes().isEmpty()) {
            _routingProtocols.add(RoutingProtocol.AGGREGATE);
         }
         if (!node.getZones().isEmpty()) {
            _zones = node.getZones().keySet();
         }
      }

      public Set<String> getAsPathAccessLists() {
         return _asPathAccessLists;
      }

      public Set<String> getCommunityLists() {
         return _communityLists;
      }

      public ConfigurationFormat getConfigurationFormat() {
         return _configurationFormat;
      }

      public Set<String> getIkeGateways() {
         return _ikeGateways;
      }

      public Set<String> getIkePolicies() {
         return _ikePolicies;
      }

      public Set<String> getIkeProposals() {
         return _ikeProposals;
      }

      public Set<String> getInterfaces() {
         return _interfaces;
      }

      public Set<String> getIpAccessLists() {
         return _ipAccessLists;
      }

      public Set<String> getIpsecPolicies() {
         return _ipsecPolicies;
      }

      public Set<String> getIpsecProposals() {
         return _ipsecProposals;
      }

      public Set<String> getIpsecVpns() {
         return _ipsecVpns;
      }

      public Set<String> getPolicyMaps() {
         return _policyMaps;
      }

      public RoleSet getRoles() {
         return _roles;
      }

      public Set<String> getRouteFilterLists() {
         return _routeFilterLists;
      }

      public Set<RoutingProtocol> getRoutingProtocols() {
         return _routingProtocols;
      }

      public Set<String> getZones() {
         return _zones;
      }

      public void setAsPathAccessLists(Set<String> asPathAccessLists) {
         _asPathAccessLists = asPathAccessLists;
      }

      public void setCommunityLists(Set<String> communityLists) {
         _communityLists = communityLists;
      }

      public void setConfigurationFormat(ConfigurationFormat configurationFormat) {
         _configurationFormat = configurationFormat;
      }

      public void setIkeGateways(Set<String> ikeGateways) {
         _ikeGateways = ikeGateways;
      }

      public void setIkePolicies(Set<String> ikePolicies) {
         _ikePolicies = ikePolicies;
      }

      public void setIkeProposals(Set<String> ikeProposals) {
         _ikeProposals = ikeProposals;
      }

      public void setInterfaces(Set<String> interfaces) {
         _interfaces = interfaces;
      }

      public void setIpAccessLists(Set<String> ipAccessLists) {
         _ipAccessLists = ipAccessLists;
      }

      public void setIpsecPolicies(Set<String> ipsecPolicies) {
         _ipsecPolicies = ipsecPolicies;
      }

      public void setIpsecProposals(Set<String> ipsecProposals) {
         _ipsecProposals = ipsecProposals;
      }

      public void setIpsecVpns(Set<String> ipsecVpns) {
         _ipsecVpns = ipsecVpns;
      }

      public void setPolicyMaps(Set<String> policyMaps) {
         _policyMaps = policyMaps;
      }

      public void setRoles(RoleSet roles) {
         _roles = roles;
      }

      public void setRouteFilterLists(Set<String> routeFilterLists) {
         _routeFilterLists = routeFilterLists;
      }

      public void setRoutingProtocols(Set<RoutingProtocol> routingProtocols) {
         _routingProtocols = routingProtocols;
      }

      public void setZones(Set<String> zones) {
         _zones = zones;
      }

   }

   private Map<String, Configuration> _nodes;

   private Map<String, NodeSummary> _summary;

   public NodesAnswerElement(Map<String, Configuration> nodes, boolean summary) {
      if (summary) {
         _summary = new TreeMap<String, NodeSummary>();
         for (Entry<String, Configuration> e : nodes.entrySet()) {
            String hostname = e.getKey();
            Configuration node = e.getValue();
            _summary.put(hostname, new NodeSummary(node));
         }
      }
      else {
         _nodes = nodes;
      }
   }

   public Map<String, Configuration> getAnswer() {
      return _nodes;
   }

   public Map<String, NodeSummary> getSummary() {
      return _summary;
   }

   public void setSummary(Map<String, NodeSummary> summary) {
      _summary = summary;
   }

}
