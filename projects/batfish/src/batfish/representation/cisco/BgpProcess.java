package batfish.representation.cisco;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import batfish.representation.Ip;

public class BgpProcess {

   private Set<String> _activatedNeighbors;
   private Map<BgpNetwork, Boolean> _aggregateNetworks;
   private Ip _clusterId;
   private int _defaultMetric;
   private boolean _defaultNeighborActivate;
   private Set<BgpNetwork> _networks;
   private Map<String, BgpPeerGroup> _peerGroups;
   private int _pid;
   private boolean _redistributeStatic;
   private String _redistributeStaticMap;
   private String _routerId;
   private Set<String> _shutdownNeighbors;

   public BgpProcess(int procnum) {
      _pid = procnum;
      _peerGroups = new HashMap<String, BgpPeerGroup>();
      _networks = new LinkedHashSet<BgpNetwork>();
      _activatedNeighbors = new LinkedHashSet<String>();
      _defaultNeighborActivate = true;
      _aggregateNetworks = new HashMap<BgpNetwork, Boolean>();
      _shutdownNeighbors = new LinkedHashSet<String>();
      _clusterId = null;
   }

   public void addActivatedNeighbor(String address) {
      if (!(_shutdownNeighbors.contains(address))) {
         _activatedNeighbors.add(address);
      }
   }

   public void addActivatedNeighbors(List<String> addresses) {
      addresses.removeAll(_shutdownNeighbors);
      _activatedNeighbors.addAll(addresses);
   }

   public void addDefaultOriginateNeighbors(
         Map<String, String> defaultOriginateNeighbors) {
      for (String neighbor : defaultOriginateNeighbors.keySet()) {
         BgpPeerGroup pg = _peerGroups.get(neighbor);
         String routeMapName = defaultOriginateNeighbors.get(neighbor);
         pg.setDefaultOriginate(true);
         pg.setDefaultOriginateMap(routeMapName);
      }
   }

   public void addNetwork(BgpNetwork network) {
      _networks.add(network);
   }

   public void addNetworks(List<BgpNetwork> networks) {
      _networks.addAll(networks);
   }

   public void addPeerGroup(BgpPeerGroup peerGroup) {
      _peerGroups.put(peerGroup.getName(), peerGroup);
   }

   public void addPeerGroupInboundPrefixLists(
         Map<String, String> inboundPrefixLists) {
      for (String peerGroupName : inboundPrefixLists.keySet()) {
         BgpPeerGroup pg = _peerGroups.get(peerGroupName);
         if (pg != null) {
            pg.setInboundPrefixList(inboundPrefixLists.get(peerGroupName));
         }
         else {
           throw new Error("Peer group: \"" + peerGroupName
                 + "\" does not exist!");
         }
      }
   }

   public void addPeerGroupInboundRouteMaps(Map<String, String> inboundRouteMaps) {
      for (String peerGroupName : inboundRouteMaps.keySet()) {
         BgpPeerGroup pg = _peerGroups.get(peerGroupName);
         if (pg != null) {
            pg.setInboundRouteMap(inboundRouteMaps.get(peerGroupName));
         }
         else {
            throw new Error("Peer group: \"" + peerGroupName
                  + "\" does not exist!");
         }
      }
   }

   public void addPeerGroupMembership(Map<String, String> peerGroupMembership) {
      for (String address : peerGroupMembership.keySet()) {
         String namedPeerGroupName = peerGroupMembership.get(address);
         BgpPeerGroup namedPeerGroup = _peerGroups.get(namedPeerGroupName);
         if (namedPeerGroup != null) {
            namedPeerGroup.addNeighborAddress(address);
            if (_defaultNeighborActivate) {
               addActivatedNeighbor(address);
            }
            BgpPeerGroup unnamedPeerGroup = _peerGroups.get(address);
            if (unnamedPeerGroup == null) {
               unnamedPeerGroup = new BgpPeerGroup(address);
               unnamedPeerGroup.addNeighborAddress(address);
               addPeerGroup(unnamedPeerGroup);
            }
         }
         else {
            throw new Error("Peer group: \"" + namedPeerGroupName
                  + "\" does not exist!");
         }
      }
   }

   public void addPeerGroupOutboundRouteMaps(
         Map<String, String> outboundRouteMaps) {
      for (String peerGroupName : outboundRouteMaps.keySet()) {
         BgpPeerGroup pg = _peerGroups.get(peerGroupName);
         if (pg != null) {
            pg.setOutboundRouteMap(outboundRouteMaps.get(peerGroupName));
         }
         else {
            throw new Error("Peer group: \"" + peerGroupName
                  + "\" does not exist!");
         }
      }
   }

   public void addPeerGroupRouteReflectorClients(List<String> rrcPeerGroups) {
      for (String peerGroupName : rrcPeerGroups) {
         BgpPeerGroup pg = _peerGroups.get(peerGroupName);
         if (pg != null) {
            pg.setRouteReflectorClient();
         }
         else {
            throw new Error("Peer group: \"" + peerGroupName
                  + "\" does not exist!");
         }
      }
   }

   public void addSendCommunityPeerGroups(List<String> sendCommunityPeerGroups) {
      for (String peerGroupName : sendCommunityPeerGroups) {
         BgpPeerGroup pg = _peerGroups.get(peerGroupName);
         if (pg != null) {
            pg.setSendCommunity(true);
         }
         else {
            throw new Error("Peer group: \"" + peerGroupName
                  + "\" does not exist!");
         }
      }
   }

   public void addShutDownNeighbor(String address) {
      _shutdownNeighbors.add(address);
   }

   public Set<String> getActivatedNeighbors() {
      return _activatedNeighbors;
   }

   public Map<BgpNetwork, Boolean> getAggregateNetworks() {
      return _aggregateNetworks;
   }

   public Ip getClusterId(){
      return _clusterId;
   }

   public int getDefaultMetric() {
      return _defaultMetric;
   }

   public boolean getDefaultNeighborActivate() {
      return _defaultNeighborActivate;
   }

   public Set<BgpNetwork> getNetworks() {
      return _networks;
   }

   public BgpPeerGroup getPeerGroup(String name) {
      return _peerGroups.get(name);
   }

   public Map<String, BgpPeerGroup> getPeerGroups() {
      return _peerGroups;
   }

   public int getPid() {
      return _pid;
   }
   
   public boolean getRedistributeStatic() {
      return _redistributeStatic;
   }

   public String getRedistributeStaticMap() {
      return _redistributeStaticMap;
   }

   public String getRouterId() {
      return _routerId;
   }

   public void setClusterId(Ip clusterId){
      _clusterId = clusterId;
   }

   public void setDefaultMetric(int defaultMetric) {
      _defaultMetric = defaultMetric;
   }
   
   public void setPeerGroupUpdateSource(String address, String source) {
      BgpPeerGroup pg = _peerGroups.get(address);
      if (pg != null) {
         pg.setUpdateSource(source);
      }
      else {
        throw new Error("Peer group: \"" + address + "\" does not exist!");
      }
   }

   public void setRedistributeStatic(boolean redistributeStatic) {
      _redistributeStatic = redistributeStatic;
   }

   public void setRedistributeStaticMap(String redistributeStaticMap) {
      _redistributeStaticMap = redistributeStaticMap;
   }

   public void setRouterId(String id) {
      _routerId = id;
   }

}
