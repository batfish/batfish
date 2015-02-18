package org.batfish.representation.cisco;

import java.io.Serializable;

import org.batfish.representation.Ip;

public abstract class BgpPeerGroup implements Serializable {

   private static final long serialVersionUID = 1L;
   protected Boolean _active;
   protected Boolean _allowAsIn;
   protected Ip _clusterId;
   protected Integer _defaultMetric;
   protected Boolean _defaultOriginate;
   protected String _defaultOriginateMap;
   protected String _inboundPrefixList;
   protected String _inboundRouteMap;
   protected String _outboundPrefixList;
   protected String _outboundRouteMap;
   protected Integer _remoteAS;
   private Boolean _removePrivateAs;
   protected Boolean _routeReflectorClient;
   protected Boolean _sendCommunity;
   protected Boolean _shutdown;
   protected String _updateSource;

   public Boolean allowAsIn() {
      return _allowAsIn;
   }

   public Boolean getActive() {
      return _active;
   }

   public Ip getClusterId() {
      return _clusterId;
   }

   public Integer getDefaultMetric() {
      return _defaultMetric;
   }

   public Boolean getDefaultOriginate() {
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

   public Boolean getRemovePrivateAs() {
      return _removePrivateAs;
   }

   public Boolean getRouteReflectorClient() {
      return _routeReflectorClient;
   }

   public Boolean getSendCommunity() {
      return _sendCommunity;
   }

   public Boolean getShutdown() {
      return _shutdown;
   }

   public String getUpdateSource() {
      return _updateSource;
   }

   public void inheritUnsetFields(BgpPeerGroup pg) {
      if (_active == null) {
         _active = pg.getActive();
      }
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
      if (_shutdown == null) {
         _shutdown = pg.getShutdown();
      }
      if (_updateSource == null) {
         _updateSource = pg.getUpdateSource();
      }
   }

   public void setActive(boolean active) {
      _active = active;
   }

   public void SetAllowAsIn(boolean b) {
      _allowAsIn = b;
   }

   public void setClusterId(Ip ip) {
      _clusterId = ip;
   }

   public void setDefaultMetric(int defaultMetric) {
      _defaultMetric = defaultMetric;
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

   public void setRemovePrivateAs(boolean removePrivateAs) {
      _removePrivateAs = removePrivateAs;
   }

   public void setRouteReflectorClient(boolean b) {
      _routeReflectorClient = b;
   }

   public void setSendCommunity(boolean sendCommunity) {
      _sendCommunity = sendCommunity;
   }

   public void setShutdown(boolean shutdown) {
      _shutdown = shutdown;
   }

   public void setUpdateSource(String updateSource) {
      _updateSource = updateSource;
   }

}
