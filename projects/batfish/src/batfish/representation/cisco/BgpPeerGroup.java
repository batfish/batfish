package batfish.representation.cisco;

import java.util.LinkedHashSet;
import java.util.Set;

public class BgpPeerGroup {
   private Long _clusterId;
   private boolean _defaultOriginate;
   private String _defaultOriginateMap;
   private String _inboundRouteMap;
   private String _name;
   private Set<String> _neighborAddresses;
   private String _outboundRouteMap;
   private Integer _remoteAS;
   private boolean _routeReflectorClient;
   private boolean _sendCommunity;
   private String _updateSource;
   private String _inboundPrefixList;

   public BgpPeerGroup(String name) {
      _name = name;
      _neighborAddresses = new LinkedHashSet<String>();
      _clusterId = null;
      _routeReflectorClient = false;
      _inboundPrefixList = null;
      _inboundRouteMap = null;
      _outboundRouteMap = null;
      _defaultOriginate = false;
      _remoteAS = null;
   }

   public Long getClusterId() {
      return _clusterId;
   }

   public boolean getDefaultOriginate() {
      return _defaultOriginate;
   }

   public String getDefaultOriginateMap() {
      return _defaultOriginateMap;
   }

   public String getInboundRouteMap() {
      return _inboundRouteMap;
   }

   public String getInboundPrefixList() {
      return _inboundPrefixList;
   }

   public String getName() {
      return _name;
   }

   public Set<String> getNeighborAddresses() {
      return _neighborAddresses;
   }

   public String getOutboundRouteMap() {
      return _outboundRouteMap;
   }

   public Integer getRemoteAS() {
      return _remoteAS;
   }

   public boolean getRouteReflectorClient() {
      return _routeReflectorClient;
   }

   public boolean getSendCommunity() {
      return _sendCommunity;
   }

   public String getUpdateSource() {
      return _updateSource;
   }

   public void setClusterId(Long clusterId) {
      _clusterId = clusterId;
   }

   public void setDefaultOriginate(boolean b) {
      _defaultOriginate = true;
   }
   
   public void setDefaultOriginateMap(String routeMapName) {
      _defaultOriginateMap = routeMapName;
   }

   public void setInboundRouteMap(String name) {
      _inboundRouteMap = name;
   }

   public void addNeighborAddress(String neighborAddress) {
      _neighborAddresses.add(neighborAddress);
   }

   public void setOutboundRouteMap(String name) {
      _outboundRouteMap = name;
   }

   public void setRemoteAS(int remoteAS) {
      _remoteAS = remoteAS;
   }

   public void setRouteReflectorClient() {
      _routeReflectorClient = true;
   }

   public void setSendCommunity(boolean sendCommunity) {
      _sendCommunity = sendCommunity;
   }

   public void setUpdateSource(String updateSource) {
      _updateSource = updateSource;
   }

   public void setInboundPrefixList(String inboundPrefixList) {
      _inboundPrefixList = inboundPrefixList;
   }

}
