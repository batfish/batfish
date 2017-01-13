package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

import org.batfish.datamodel.Ip;

public abstract class BgpPeerGroup implements Serializable {

   private static final long serialVersionUID = 1L;

   protected Boolean _active;

   protected Boolean _advertiseInactive;

   private Map<String, String> _afGroups;

   protected Boolean _allowAsIn;

   protected Ip _clusterId;

   protected Integer _defaultMetric;

   protected Boolean _defaultOriginate;

   protected String _defaultOriginateMap;

   protected String _description;

   protected Boolean _disablePeerAsCheck;

   protected Boolean _ebgpMultihop;

   private String _groupName;

   protected String _inboundPrefixList;

   private String _inboundRoute6Map;

   protected String _inboundRouteMap;

   private transient boolean _inherited;

   protected String _outboundPrefixList;

   private String _outboundRoute6Map;

   protected String _outboundRouteMap;

   private String _peerSession;

   protected Integer _remoteAs;

   protected Boolean _removePrivateAs;

   protected Boolean _routeReflectorClient;

   protected Boolean _sendCommunity;

   private boolean _sendExtendedCommunity;

   protected Boolean _shutdown;

   protected String _updateSource;

   public BgpPeerGroup() {
      _afGroups = new TreeMap<>();
   }

   public Boolean getActive() {
      return _active;
   }

   public Boolean getAdvertiseInactive() {
      return _advertiseInactive;
   }

   public Map<String, String> getAfGroups() {
      return _afGroups;
   }

   public Boolean getAllowAsIn() {
      return _allowAsIn;
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

   public String getDescription() {
      return _description;
   }

   public Boolean getDisablePeerAsCheck() {
      return _disablePeerAsCheck;
   }

   public Boolean getEbgpMultihop() {
      return _ebgpMultihop;
   }

   public String getGroupName() {
      return _groupName;
   }

   public String getInboundPrefixList() {
      return _inboundPrefixList;
   }

   public String getInboundRoute6Map() {
      return _inboundRoute6Map;
   }

   public String getInboundRouteMap() {
      return _inboundRouteMap;
   }

   public boolean getInherited() {
      return _inherited;
   }

   public abstract String getName();

   public String getOutboundPrefixList() {
      return _outboundPrefixList;
   }

   public String getOutboundRoute6Map() {
      return _outboundRoute6Map;
   }

   public String getOutboundRouteMap() {
      return _outboundRouteMap;
   }

   protected final BgpPeerGroup getParentGroup(BgpProcess proc,
         CiscoConfiguration cv) {
      BgpPeerGroup parent = null;
      if (_groupName != null) {
         parent = proc.getNamedPeerGroups().get(_groupName);
         BgpProcess defaultProc = cv.getDefaultVrf().getBgpProcess();
         if (parent == null) {
            parent = defaultProc.getNamedPeerGroups().get(_groupName);
         }
         if (parent == null) {
            cv.undefined(
                  "Reference to undefined peer-group: '" + _groupName + "'",
                  CiscoConfiguration.BGP_PEER_GROUP, _groupName);
         }
      }
      return parent;
   }

   protected final BgpPeerGroup getParentSession(BgpProcess proc,
         CiscoConfiguration cv) {
      BgpPeerGroup parent = null;
      if (_peerSession != null) {
         parent = proc.getPeerSessions().get(_peerSession);
         BgpProcess defaultProc = cv.getDefaultVrf().getBgpProcess();
         if (parent == null) {
            parent = defaultProc.getPeerSessions().get(_peerSession);
         }
         if (parent == null) {
            cv.undefined(
                  "Reference to undefined peer-session: '" + _peerSession + "'",
                  CiscoConfiguration.BGP_PEER_GROUP, _peerSession);
         }
      }
      if (parent == null) {
         parent = proc.getMasterBgpPeerGroup();
      }
      return parent;
   }

   public String getPeerSession() {
      return _peerSession;
   }

   public Integer getRemoteAs() {
      return _remoteAs;
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

   public boolean getSendExtendedCommunity() {
      return _sendExtendedCommunity;
   }

   public Boolean getShutdown() {
      return _shutdown;
   }

   public String getUpdateSource() {
      return _updateSource;
   }

   private void inheritUnsetFields(BgpPeerGroup pg) {
      if (_active == null) {
         _active = pg.getActive();
      }
      if (_advertiseInactive == null) {
         _advertiseInactive = pg.getAdvertiseInactive();
      }
      if (_allowAsIn == null) {
         _allowAsIn = pg.getAllowAsIn();
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
      if (_description == null) {
         _description = pg.getDescription();
      }
      if (_disablePeerAsCheck == null) {
         _disablePeerAsCheck = pg.getDisablePeerAsCheck();
      }
      if (_ebgpMultihop == null) {
         _ebgpMultihop = pg.getEbgpMultihop();
      }
      if (_inboundPrefixList == null) {
         _inboundPrefixList = pg.getInboundPrefixList();
      }
      if (_inboundRouteMap == null) {
         _inboundRouteMap = pg.getInboundRouteMap();
      }
      if (_inboundRoute6Map == null) {
         _inboundRoute6Map = pg.getInboundRoute6Map();
      }
      if (_outboundPrefixList == null) {
         _outboundPrefixList = pg.getOutboundPrefixList();
      }
      if (_outboundRouteMap == null) {
         _outboundRouteMap = pg.getOutboundRouteMap();
      }
      if (_outboundRoute6Map == null) {
         _outboundRoute6Map = pg.getOutboundRoute6Map();
      }
      if (_remoteAs == null) {
         _remoteAs = pg.getRemoteAs();
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

   public void inheritUnsetFields(BgpProcess proc, CiscoConfiguration cv) {
      if (_inherited) {
         return;
      }
      _inherited = true;
      BgpPeerGroup aa = getParentGroup(proc, cv);
      if (aa != null) {
         aa.inheritUnsetFields(proc, cv);
         inheritUnsetFields(aa);
      }
      BgpPeerGroup pg = getParentSession(proc, cv);
      if (pg != null) {
         pg.inheritUnsetFields(proc, cv);
         inheritUnsetFields(pg);
      }
   }

   public void setActive(boolean active) {
      _active = active;
   }

   public void setAdvertiseInactive(boolean advertiseInactive) {
      _advertiseInactive = advertiseInactive;
   }

   public void setAllowAsIn(boolean allowAsIn) {
      _allowAsIn = allowAsIn;
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

   public void setDescription(String description) {
      _description = description;
   }

   public void setDisablePeerAsCheck(boolean disablePeerAsCheck) {
      _disablePeerAsCheck = disablePeerAsCheck;
   }

   public void setEbgpMultihop(boolean ebgpMultihop) {
      _ebgpMultihop = ebgpMultihop;
   }

   public void setGroupName(String groupName) {
      _groupName = groupName;
   }

   public void setInboundPrefixList(String inboundPrefixList) {
      _inboundPrefixList = inboundPrefixList;
   }

   public void setInboundRoute6Map(String inboundRoute6Map) {
      _inboundRoute6Map = inboundRoute6Map;
   }

   public void setInboundRouteMap(String name) {
      _inboundRouteMap = name;
   }

   public void setOutboundPrefixList(String listName) {
      _outboundPrefixList = listName;
   }

   public void setOutboundRoute6Map(String outboundRoute6Map) {
      _outboundRoute6Map = outboundRoute6Map;
   }

   public void setOutboundRouteMap(String name) {
      _outboundRouteMap = name;
   }

   public void setPeerSession(String peerSession) {
      _peerSession = peerSession;
   }

   public void setRemoteAs(int remoteAS) {
      _remoteAs = remoteAS;
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

   public void setSendExtendedCommunity(boolean sendExtendedCommunity) {
      _sendExtendedCommunity = sendExtendedCommunity;
   }

   public void setShutdown(boolean shutdown) {
      _shutdown = shutdown;
   }

   public void setUpdateSource(String updateSource) {
      _updateSource = updateSource;
   }

}
