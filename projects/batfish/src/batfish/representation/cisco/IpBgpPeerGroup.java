package batfish.representation.cisco;

import batfish.representation.Ip;

public class IpBgpPeerGroup extends BgpPeerGroup {

   private String _groupName;
   private Ip _ip;

   public IpBgpPeerGroup(Ip ip) {
      _ip = ip;
   }

   public String getGroupName() {
      if (_groupName == null) {
         return _ip.toString();
      }
      else {
         return _groupName;
      }
   }

   public Ip getIp() {
      return _ip;
   }

   @Override
   public String getName() {
      return _ip.toString();
   }

   public void setGroupName(String name) {
      _groupName = name;
   }

   public void inheritUnsetFields(NamedBgpPeerGroup npg) {
      if (_clusterId == null) {
         _clusterId = npg.getClusterId();
      }
      if (_defaultOriginate == null) {
         _defaultOriginate = npg.getDefaultOriginate();
      }
      if (_defaultOriginateMap == null) {
         _defaultOriginateMap = npg.getDefaultOriginateMap();
      }
      if (_inboundPrefixList == null) {
         _inboundPrefixList = npg.getInboundPrefixList();
      }
      if (_inboundRouteMap == null) {
         _inboundRouteMap = npg.getInboundRouteMap();
      }
      if (_outboundPrefixList == null) {
         _outboundPrefixList = npg.getOutboundPrefixList();
      }
      if (_outboundRouteMap == null) {
         _outboundRouteMap = npg.getOutboundRouteMap();
      }
      if (_remoteAS == null) {
         _remoteAS = npg.getRemoteAS();
      }
      if (_routeReflectorClient == null) {
         _routeReflectorClient = npg.getRouteReflectorClient();
      }
      if (_sendCommunity == null) {
         _sendCommunity = npg.getSendCommunity();
      }
      if (_updateSource == null) {
         _updateSource = npg.getUpdateSource();
      }
   }

}
