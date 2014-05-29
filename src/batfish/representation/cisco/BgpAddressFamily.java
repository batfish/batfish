package batfish.representation.cisco;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import batfish.grammar.cisco.bgp.AFType;

public class BgpAddressFamily {
   private Map<BgpNetwork, Boolean> _aggregateNetworks;
   private int _defaultMetric;
   private Map<String, String> _defaultOriginateNeighbors;
   private Map<String, String> _inboundRouteMaps;
   private List<String> _neighbors;
   private List<BgpNetwork> _networks;
   private Map<String, String> _outboundRouteMaps;
   private boolean _redistributeStatic;
   private String _redistributeStaticMap;
   private List<String> _rrcPeerGroups;
   private List<String> _scPeerGroups;
   private AFType _type;
   private Map<String, String> _inboundPrefixLists;
   private Map<String, String> _peerGroupMembership;

   public BgpAddressFamily(AFType type) {
      _type = type;
      _networks = new ArrayList<BgpNetwork>();
      _neighbors = new ArrayList<String>();
      _rrcPeerGroups = new ArrayList<String>();
      _inboundRouteMaps = new HashMap<String, String>();
      _outboundRouteMaps = new HashMap<String, String>();
      _scPeerGroups = new ArrayList<String>();
      _defaultOriginateNeighbors = new HashMap<String, String>();
      _aggregateNetworks = new HashMap<BgpNetwork, Boolean>();
      _defaultMetric = 0;
      _redistributeStatic = false;
      _redistributeStaticMap = null;
      _inboundPrefixLists = new HashMap<String, String>();
      _peerGroupMembership = new HashMap<String,String>();
   }

   public Map<BgpNetwork, Boolean> getAggregateNetworks() {
      return _aggregateNetworks;
   }

   public int getDefaultMetric() {
      return _defaultMetric;
   }
   
   public Map<String, String> getDefaultOriginateNeighbors() {
      return _defaultOriginateNeighbors;
   }
   
   public Map<String, String> getInboundRouteMaps() {
      return _inboundRouteMaps;
   }

   public List<String> getNeighbors() {
      return _neighbors;
   }

   public List<BgpNetwork> getNetworks() {
      return _networks;
   }

   public Map<String, String> getOutboundRouteMaps() {
      return _outboundRouteMaps;
   }

   public boolean getRedistributeStatic() {
      return _redistributeStatic;
   }

   public String getRedistributeStaticMap() {
      return _redistributeStaticMap;
   }

   public List<String> getRRCPeerGroups() {
      return _rrcPeerGroups;
   }

   public List<String> getSCPeerGroups() {
      return _scPeerGroups;
   }

   public AFType getType() {
      return _type;
   }

   public void setDefaultMetric(int metric) {
      _defaultMetric = metric;
   }
   
   public void setRedistributeStatic(boolean b) {
      _redistributeStatic = true;
   }
   
   public void setRedistributeStaticMap(String routeMapName) {
      _redistributeStaticMap = routeMapName;
   }

   public Map<String, String> getInboundPrefixLists() {
      return _inboundPrefixLists;
   }
   
   public Map<String,String> getPeerGroupMembership(){
      return _peerGroupMembership;
   }
}
