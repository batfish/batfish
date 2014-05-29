package batfish.representation.juniper;

import java.util.ArrayList;
import java.util.List;

public class BGPGroup {
   private Long _clusterId;
   private String _name;
   private Integer _localAS;
   private boolean _routeReflectorClient;
   private List<BGPNeighbor> _neighbors;
   private boolean _isExternal;

   // not used at this moment
   private Integer _remoteAS;
   private List<String> _inboundPolicyStatementName;
   private List<String> _outboundPolicyStatementName;
   private String _updateSource;

   public BGPGroup(String name) {
      _name = name;
      _routeReflectorClient = false;
      _inboundPolicyStatementName = null;
      _outboundPolicyStatementName = null;
      _clusterId = null;
      _updateSource = null;
      _neighbors = new ArrayList<BGPNeighbor>();
      _isExternal = true;
   }

   public boolean getRouteReflectorClient() {
      return _routeReflectorClient;
   }

   public void setRemoteAS(int remoteAS) {
      _remoteAS = remoteAS;
   }
   
   public void setLocalAS(int localAS) {
      _localAS = localAS;
   }

   public void addNeighbor(BGPNeighbor n) {
      _neighbors.add(n);
   }
   
   public void setIsExternal(boolean e){
      _isExternal = e;
   }

   public void setInboundPolicyStatement(List<String> name) {
      _inboundPolicyStatementName = name;
   }

   public void setOutboundPolicyStatement(List<String> name) {
      _outboundPolicyStatementName = name;
   }

   public Long getClusterId() {
      return _clusterId;
   }

   public boolean getIsExternal(){
      return _isExternal;
   }
   
   public Integer getRemoteAS() {
      return _remoteAS;
   }
   
   public Integer getLocalAS() {
      return _localAS;
   }

   public String getName() {
      return _name;
   }

   public List<String> getInboundPolicyStatement() {
      return _inboundPolicyStatementName;
   }

   public List<String> getOutboundPolicyStatement() {
      return _outboundPolicyStatementName;
   }

   public String getUpdateSource() {
      return _updateSource;
   }

   public List<BGPNeighbor> getNeighbors() {
      return _neighbors;
   }

   public void setRouteReflectorClient() {
      _routeReflectorClient = true;
   }

   public void setClusterId(Long clusterId) {
      _clusterId = clusterId;
   }

   public void setUpdateSource(String updateSource) {
      _updateSource = updateSource;
   }

}
