package batfish.representation;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

public class BgpNeighbor implements Serializable {

   private static final long serialVersionUID = 1L;

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

}
