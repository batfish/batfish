package batfish.representation.cisco;

import java.io.Serializable;

import batfish.representation.Ip;

public abstract class BgpPeerGroup implements Serializable {

   private static final long serialVersionUID = 1L;
   protected Ip _clusterId;
   protected Boolean _defaultOriginate;
   protected String _defaultOriginateMap;
   protected String _inboundPrefixList;
   protected String _inboundRouteMap;
   protected String _outboundPrefixList;
   protected String _outboundRouteMap;
   protected Integer _remoteAS;
   protected Boolean _routeReflectorClient;
   protected Boolean _sendCommunity;
   protected String _updateSource;

   public Ip getClusterId() {
      return _clusterId;
   }

   public boolean getDefaultOriginate() {
      return _defaultOriginate;
   }

   public String getDefaultOriginateMap() {
      return _defaultOriginateMap;
   }

   public String getInboundPrefixList() {
      return _inboundPrefixList;
   }

   public String getInboundRouteMap() {
      return _inboundRouteMap;
   }

   public abstract String getName();

   public String getOutboundPrefixList() {
      return _outboundPrefixList;
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

   public void inheritUnsetFields(BgpPeerGroup pg) {
      if (_clusterId == null) {
         _clusterId = pg.getClusterId();
      }
      if (_defaultOriginate == null) {
         _defaultOriginate = pg.getDefaultOriginate();
      }
      if (_defaultOriginateMap == null) {
         _defaultOriginateMap = pg.getDefaultOriginateMap();
      }
      if (_inboundPrefixList == null) {
         _inboundPrefixList = pg.getInboundPrefixList();
      }
      if (_inboundRouteMap == null) {
         _inboundRouteMap = pg.getInboundRouteMap();
      }
      if (_outboundPrefixList == null) {
         _outboundPrefixList = pg.getOutboundPrefixList();
      }
      if (_outboundRouteMap == null) {
         _outboundRouteMap = pg.getOutboundRouteMap();
      }
      if (_remoteAS == null) {
         _remoteAS = pg.getRemoteAS();
      }
      if (_routeReflectorClient == null) {
         _routeReflectorClient = pg.getRouteReflectorClient();
      }
      if (_sendCommunity == null) {
         _sendCommunity = pg.getSendCommunity();
      }
      if (_updateSource == null) {
         _updateSource = pg.getUpdateSource();
      }
   }

   public void setClusterId(Ip ip) {
      _clusterId = ip;
   }

   public void setDefaultOriginate(boolean b) {
      _defaultOriginate = true;
   }

   public void setDefaultOriginateMap(String routeMapName) {
      _defaultOriginateMap = routeMapName;
   }

   public void setInboundPrefixList(String inboundPrefixList) {
      _inboundPrefixList = inboundPrefixList;
   }

   public void setInboundRouteMap(String name) {
      _inboundRouteMap = name;
   }

   public void setOutboundPrefixList(String listName) {
      _outboundPrefixList = listName;
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

}
