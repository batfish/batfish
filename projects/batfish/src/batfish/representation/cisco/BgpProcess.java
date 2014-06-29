package batfish.representation.cisco;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import batfish.representation.Ip;

public class BgpProcess {

   private Set<Ip> _activatedNeighbors;
   private Map<BgpNetwork, Boolean> _aggregateNetworks;
   private Ip _clusterId;
   private int _defaultMetric;
   private boolean _defaultNeighborActivate;
   private Set<BgpNetwork> _networks;
   private Map<String, BgpPeerGroup> _peerGroups;
   private int _pid;
   private boolean _redistributeStatic;
   private String _redistributeStaticMap;
   private Ip _routerId;
   private Set<String> _shutdownNeighbors;

   public BgpProcess(int procnum) {
      _pid = procnum;
      _peerGroups = new HashMap<String, BgpPeerGroup>();
      _networks = new LinkedHashSet<BgpNetwork>();
      _activatedNeighbors = new LinkedHashSet<Ip>();
      _defaultNeighborActivate = true;
      _aggregateNetworks = new HashMap<BgpNetwork, Boolean>();
      _shutdownNeighbors = new LinkedHashSet<String>();
      _clusterId = null;
   }

   public void addActivatedNeighbor(Ip address) {
      if (!(_shutdownNeighbors.contains(address))) {
         _activatedNeighbors.add(address);
      }
   }

   public void addActivatedNeighbors(List<Ip> addresses) {
      addresses.removeAll(_shutdownNeighbors);
      _activatedNeighbors.addAll(addresses);
   }

   public void addDefaultOriginateNeighbor(String neighbor, String routeMapName) {
      BgpPeerGroup pg = _peerGroups.get(neighbor);
      pg.setDefaultOriginate(true);
      pg.setDefaultOriginateMap(routeMapName);
   }

   public void addDefaultOriginateNeighbors(
         Map<String, String> defaultOriginateNeighbors) {
      for (String neighbor : defaultOriginateNeighbors.keySet()) {
         String routeMapName = defaultOriginateNeighbors.get(neighbor);
         addDefaultOriginateNeighbor(neighbor, routeMapName);
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

   public void addPeerGroupInboundPrefixList(String peerGroupName,
         String listName) {
      BgpPeerGroup pg = _peerGroups.get(peerGroupName);
      if (pg != null) {
         pg.setInboundPrefixList(listName);
      }
      else {
         throw new Error("Peer group: \"" + peerGroupName
               + "\" does not exist!");
      }
   }

   public void addPeerGroupInboundRouteMap(String peerGroupName, String mapName) {
      BgpPeerGroup pg = _peerGroups.get(peerGroupName);
      if (pg != null) {
         pg.setInboundRouteMap(mapName);
      }
      else {
         throw new Error("Peer group: \"" + peerGroupName
               + "\" does not exist!");
      }
   }

   public void addPeerGroupMember(Ip address, String namedPeerGroupName) {
      BgpPeerGroup namedPeerGroup = _peerGroups.get(namedPeerGroupName);
      if (namedPeerGroup != null) {
         namedPeerGroup.addNeighborAddress(address);
         if (_defaultNeighborActivate) {
            addActivatedNeighbor(address);
         }
         BgpPeerGroup unnamedPeerGroup = _peerGroups.get(address.toString());
         if (unnamedPeerGroup == null) {
            unnamedPeerGroup = new BgpPeerGroup(address.toString());
            unnamedPeerGroup.addNeighborAddress(address);
            addPeerGroup(unnamedPeerGroup);
         }
      }
      else {
         throw new Error("Peer group: \"" + namedPeerGroupName
               + "\" does not exist!");
      }
   }

   public void addPeerGroupOutboundRouteMap(String peerGroupName, String mapName) {
      BgpPeerGroup pg = _peerGroups.get(peerGroupName);
      if (pg != null) {
         pg.setOutboundRouteMap(mapName);
      }
      else {
         throw new Error("Peer group: \"" + peerGroupName
               + "\" does not exist!");
      }
   }

   public void addPeerGroupRouteReflectorClient(String peerGroupName) {
      BgpPeerGroup pg = _peerGroups.get(peerGroupName);
      if (pg != null) {
         pg.setRouteReflectorClient();
      }
      else {
         throw new Error("Peer group: \"" + peerGroupName
               + "\" does not exist!");
      }
   }

   public void addPeerGroupRouteReflectorClients(List<String> rrcPeerGroups) {
      for (String peerGroupName : rrcPeerGroups) {
         addPeerGroupRouteReflectorClient(peerGroupName);
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

   public Set<Ip> getActivatedNeighbors() {
      return _activatedNeighbors;
   }

   public Map<BgpNetwork, Boolean> getAggregateNetworks() {
      return _aggregateNetworks;
   }

   public Ip getClusterId() {
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

   public Ip getRouterId() {
      return _routerId;
   }

   public void setClusterId(Ip clusterId) {
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

   public void setRouterId(Ip routerId) {
      _routerId = routerId;
   }

   public void addPeerGroupOutboundPrefixList(String peerGroupName, String listName) {
      BgpPeerGroup pg = _peerGroups.get(peerGroupName);
      if (pg != null) {
         pg.setOutboundPrefixList(listName);
      }
      else {
         throw new Error("Peer group: \"" + peerGroupName
               + "\" does not exist!");
      }
   }

}
