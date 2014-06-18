package batfish.representation;

import java.util.LinkedHashSet;
import java.util.Set;

import batfish.util.Util;

public class BgpNeighbor {
   private Ip _address;
   private Long _clusterId;
   private Integer _defaultMetric;
   private Set<GeneratedRoute> _generatedRoutes;
   private String _groupName;
   private Set<PolicyMap> _inboundPolicyMaps;
   private Integer _localAs;
   private Set<PolicyMap> _originationPolicies;
   private Set<PolicyMap> _outboundPolicyMaps;
   private Integer _remoteAs;
   private Boolean _sendCommunity;
   private String _updateSource;

   public BgpNeighbor(Ip address) {
      _address = address;
      _clusterId = null;
      _outboundPolicyMaps = new LinkedHashSet<PolicyMap>();
      _inboundPolicyMaps = new LinkedHashSet<PolicyMap>();
      _groupName = null;
      _originationPolicies = new LinkedHashSet<PolicyMap>();
      _generatedRoutes = new LinkedHashSet<GeneratedRoute>();
      _remoteAs = null;
      _localAs = null;
   }

   public void addInboundPolicyMap(PolicyMap map) {
      _inboundPolicyMaps.add(map);
   }

   public void addOutboundPolicyMap(PolicyMap map) {
      _outboundPolicyMaps.add(map);
   }

   public Ip getAddress() {
      return _address;
   }

   public Long getClusterId() {
      return _clusterId;
   }

   public Integer getDefaultMetric() {
      return _defaultMetric;
   }

   public Set<GeneratedRoute> getGeneratedRoutes() {
      return _generatedRoutes;
   }

   public String getGroupName() {
      return _groupName;
   }

   public String getIFString(int indentLevel) {
	   
	   String retString = Util.getIndentString(indentLevel) + "BgpNeighbor";
	   
	   retString += String.format("\n%s%s %s", Util.getIndentString(indentLevel+1), "Ip", _address);
	   retString += String.format("\n%s%s %s", Util.getIndentString(indentLevel+1), "ClusterId", _clusterId);
	   retString += String.format("\n%s%s %s", Util.getIndentString(indentLevel+1), "DefaultMetric", _defaultMetric);
	   retString += String.format("\n%s%s %s", Util.getIndentString(indentLevel+1), "GroupName", _groupName);
	   retString += String.format("\n%s%s %s", Util.getIndentString(indentLevel+1), "LocalAs", _localAs);
	   retString += String.format("\n%s%s %s", Util.getIndentString(indentLevel+1), "RemoteAs", _remoteAs);
	   retString += String.format("\n%s%s %s", Util.getIndentString(indentLevel+1), "SendCommunity", _sendCommunity);
	   retString += String.format("\n%s%s %s", Util.getIndentString(indentLevel+1), "UpdateSource", _updateSource);

	   //generated routes
	   for (GeneratedRoute gr : _generatedRoutes) {
		   retString += "\n" + gr.getIFString(indentLevel + 1);
	   }

	   //inbound policy maps
	   if (_inboundPolicyMaps.size() > 0) {
		   retString += "\n" + Util.getIndentString(indentLevel + 1) + "InboundPolicyMaps";

		   for (PolicyMap pm : _inboundPolicyMaps) {
			   //ARICHECK: can the mapname have a space in it?
			   //          are all policymap declared elsewhere?
			   retString += " " + pm.getMapName();
		   }
	   }

	   //outbound policy maps
	   if (_outboundPolicyMaps.size() > 0) {
		   retString += "\n" + Util.getIndentString(indentLevel + 1) + "OutboundPolicyMaps";

		   for (PolicyMap pm : _outboundPolicyMaps) {
			   //ARICHECK: can the mapname have a space in it?
			   //          are all policymap declared elsewhere?
			   retString += " " + pm.getMapName();
		   }
	   }

	   //origination policies
	   if (_originationPolicies.size() > 0) {
		   retString += "\n" + Util.getIndentString(indentLevel + 1) + "OriginationPolicies";

		   for (PolicyMap pm : _originationPolicies) {
			   //ARICHECK: can the mapname have a space in it?
			   //          are all policymap declared elsewhere?
			   retString += " " + pm.getMapName();
		   }
	   }
	   
	   return retString;	     
   }

   public Set<PolicyMap> getInboundPolicyMaps() {
      return _inboundPolicyMaps;
   }

   public Integer getLocalAs() {
      return _localAs;
   }

   public Set<PolicyMap> getOriginationPolicies() {
      return _originationPolicies;
   }

   public Set<PolicyMap> getOutboundPolicyMaps() {
      return _outboundPolicyMaps;
   }

   public Integer getRemoteAs() {
      return _remoteAs;
   }

   public Boolean getSendCommunity() {
      return _sendCommunity;
   }

   public String getUpdateSource() {
      return _updateSource;
   }

   public void setClusterId(Long clusterId) {
      _clusterId = clusterId;
   }

   public void setDefaultMetric(Integer defaultMetric) {
      _defaultMetric = defaultMetric;
   }

   public void setGroupName(String name) {
      _groupName = name;
   }

   public void setLocalAs(Integer localAs) {
      _localAs = localAs;
   }

   public void setRemoteAs(Integer remoteAs) {
      _remoteAs = remoteAs;
   }

   public void setSendCommunity(Boolean sendCommunity) {
      _sendCommunity = sendCommunity;
   }

   public void setUpdateSource(String updateSource) {
      _updateSource = updateSource;
   }

   public boolean sameParseTree(BgpNeighbor neighbor, String prefix) {
      boolean res = _address.equals(neighbor._address);
      boolean finalRes = res;
      
      if(_clusterId != null){
         res =  (_clusterId.equals(neighbor._clusterId));
      }else{
         res = (neighbor._clusterId == null);
      }
      if(res == false){
         System.out.println("BGPNeighbor:ClusterId "+prefix);
         finalRes = res;
      }
      
      if(_defaultMetric != null){
         res =  (_defaultMetric.equals(neighbor._defaultMetric));
      }else{
         res = (neighbor._defaultMetric == null);
      }
      if(res == false){
         System.out.println("BGPNeighbor:DefaultMatric "+prefix);
         finalRes = res;
      }
      
      /*
      if(_groupName != null){
         res = res && (_groupName.equals(neighbor._groupName));
      }else{
         res = res && (neighbor._groupName == null);
      }
      if(res == false){
         System.out.print("BGPNeighbor:GroupName ");
         return res;
      }
      */
      
      if(_localAs != null){
         res =  (_localAs.equals(neighbor._localAs));
      }else{
         res = (neighbor._localAs == null);
      }
      if(res == false){
         System.out.println("BGPNeighbor:LocalAS "+prefix);
         finalRes = res;
      }
      
      if(_remoteAs != null){
         res =  (_remoteAs.equals(neighbor._remoteAs));
      }else{
         res =  (neighbor._remoteAs == null);
      }
      if(res == false){
         System.out.println("BGPNeighbor:RemoteAS "+prefix);
         finalRes = res;
      }
      
      if(_sendCommunity != null){
         res =  (_sendCommunity.equals(neighbor._sendCommunity));
      }else{
         res =  (neighbor._sendCommunity == null);
      }
      if(res == false){
         System.out.println("BGPNeighbor:SendComm "+prefix);
         finalRes = res;
      }
      
      if(_updateSource != null){
         res =  (_updateSource.equals(neighbor._updateSource));
      }else{
         res =  (neighbor._updateSource == null);
      }

      if(res == false){
         System.out.println("BGPNeighbor:UpdateSource "+prefix);
         finalRes = res;
      }
      
      if(_generatedRoutes.size() != neighbor._generatedRoutes.size()){
         System.out.println("BGPNeighbor:GenRoute:Size "+prefix);
         finalRes = false;
      }else{
      for (GeneratedRoute lhs: _generatedRoutes){
         boolean found = false;
         for (GeneratedRoute rhs: neighbor._generatedRoutes){        
            if (lhs.equals(rhs)){
               res =  lhs.sameParseTree(rhs, "BGPNeighbor:GenRoute "+prefix);
               found = true;
               if(res == false){                  
                  finalRes = res;
               }
               break;
            }            
         }
         if (found == false){
            System.out.println("BGPNeighbor:GenRoute:NotFound "+prefix);
            finalRes = false;
         }
      }
      }
      
      if(_inboundPolicyMaps.size() != neighbor._inboundPolicyMaps.size()){
         System.out.println("BGPNeighbor:inboundPoliMap:Size "+prefix);
         finalRes = false;
      }else{
      for (PolicyMap lhs: _inboundPolicyMaps){
         boolean found = false;
         for (PolicyMap rhs: neighbor._inboundPolicyMaps){        
            if (lhs.getMapName().equals(rhs.getMapName())){
               res =  lhs.sameParseTree(rhs, "BGPNeighbor:inboundPoliMap "+prefix);
               found = true;
               if(res == false){
                  finalRes = res;
               }
               break;
            }            
         }
         if (found == false){
            System.out.println("BGPNeighbor:inboundPoliMap:NotFound "+prefix);
            finalRes = false;
         }
      }
      }
      
      if(_originationPolicies.size() != neighbor._originationPolicies.size()){
         System.out.println("BGPNeighbor:origPoli:Size "+prefix);
         finalRes = false;
      }else{
      for (PolicyMap lhs: _originationPolicies){
         boolean found = false;
         for (PolicyMap rhs: neighbor._originationPolicies){        
            if (lhs.getMapName().equals(rhs.getMapName())){
               res =  lhs.sameParseTree(rhs, "BGPNeighbor:origPoli "+prefix);
               found = true;
               if(res == false){
                  finalRes = res;
               }
               break;
            }            
         }
         if (found == false){
            System.out.println("BGPNeighbor:origPoli:NotFound "+prefix);
            finalRes = false;
         }
      }
      }
      
      if(_outboundPolicyMaps.size() != neighbor._outboundPolicyMaps.size()){
         System.out.println("BGPNeighbor:outboundPoliMap:Size "+prefix);
         finalRes = false;
      }else{
      for (PolicyMap lhs: _outboundPolicyMaps){
         boolean found = false;
         for (PolicyMap rhs: neighbor._outboundPolicyMaps){        
            if (lhs.getMapName().equals(rhs.getMapName())){
               res = lhs.sameParseTree(rhs, "BGPNeighbor:outboundPoliMap "+prefix);
               found = true;
               if(res == false){
                  finalRes = res;
               }
               break;
            }            
         }
         if ( found == false){
            System.out.println("BGPNeighbor:outboundPoliMap:NotFound "+prefix);
            finalRes = false;
         }
      }
      }
      
      return finalRes;
   }

}
