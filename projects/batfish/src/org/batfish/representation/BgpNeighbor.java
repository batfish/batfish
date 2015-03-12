package org.batfish.representation;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Represents a peering with a single router (by ip address) acting as a bgp
 * peer to the router whose configuration's BGP process contains this object
 */
public class BgpNeighbor implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   /**
    * The ip address of this peer for the purpose of this peering
    */
   private Ip _address;

   /**
    * The cluster id associated with this peer to be used in route reflection
    */
   private Long _clusterId;

   /**
    * The default metric associated with routes sent to this peer
    */
   private Integer _defaultMetric;

   /**
    * The set of generated and/or aggregate routes to be potentially sent to
    * this peer before outbound policies are taken into account
    */
   private Set<GeneratedRoute> _generatedRoutes;

   /**
    * The group name associated with this peer in the vendor-specific
    * configuration from which the containing configuration is derived. This
    * field is OPTIONAL and should not impact the subsequent data plane
    * computation.
    */
   private String _groupName;

   /**
    * The set of policies applied to inbound routes. Each policy in this set is
    * applied independently of the others.
    */
   private Set<PolicyMap> _inboundPolicyMaps;

   /**
    * The autonomous system number of the containing BGP process as reported to
    * this peer
    */
   private Integer _localAs;

   /**
    * The set of policies governing routes that may be originated (i.e. routes
    * not received through BGP) from the containing BGP process. Each policy in
    * this set is applied independently of the others. These policies are
    * applied before outbound policies are considered.
    */
   private Set<PolicyMap> _originationPolicies;

   /**
    * The set of policies applied to outbound routes. Each policy in this set is
    * applied independently of the others.
    */
   private Set<PolicyMap> _outboundPolicyMaps;

   /**
    * Ip range from which to accept dynamic BGP peering sessions.
    */
   private Prefix _prefix;

   /**
    * The autonomous system number that the containing BGP process considers
    * this peer to have.
    */
   private Integer _remoteAs;

   /**
    * Flag governing whether to include community numbers in outgoing route
    * advertisements to this peer
    */
   private Boolean _sendCommunity;

   /**
    * TODO: figure out semantics of this field
    */
   private String _updateSource;

   private BgpNeighbor() {
      _clusterId = null;
      _outboundPolicyMaps = new LinkedHashSet<PolicyMap>();
      _inboundPolicyMaps = new LinkedHashSet<PolicyMap>();
      _groupName = null;
      _originationPolicies = new LinkedHashSet<PolicyMap>();
      _generatedRoutes = new LinkedHashSet<GeneratedRoute>();
      _remoteAs = null;
      _localAs = null;
   }

   /**
    * Constructs a BgpNeighbor with the given peer ip address for
    * {@link #_address}
    *
    * @param address
    */
   public BgpNeighbor(Ip address) {
      this();
      _address = address;
   }

   /**
    * Constructs a BgpNeighbor with the given peer dynamic ip range for
    * {@link #_prefix}
    *
    * @param prefix
    */
   public BgpNeighbor(Prefix prefix) {
      this();
      _prefix = prefix;
   }

   /**
    * Adds a policy to the set of inbound policies for this peer
    *
    * @param map
    *           The policy to add
    */
   public void addInboundPolicyMap(PolicyMap map) {
      _inboundPolicyMaps.add(map);
   }

   /**
    * Adds a policy to the set of outbound policies for this peer
    *
    * @param map
    *           The policy to add
    */
   public void addOutboundPolicyMap(PolicyMap map) {
      _outboundPolicyMaps.add(map);
   }

   /**
    * @return {@link #_address}
    */
   public Ip getAddress() {
      return _address;
   }

   /**
    * @return {@link #_clusterId}
    */
   public Long getClusterId() {
      return _clusterId;
   }

   /**
    * @return {@link #_defaultMetric}
    */
   public Integer getDefaultMetric() {
      return _defaultMetric;
   }

   /**
    * @return {@link #_generatedRoutes}
    */
   public Set<GeneratedRoute> getGeneratedRoutes() {
      return _generatedRoutes;
   }

   /**
    * @return {@link #_groupName}
    */
   public String getGroupName() {
      return _groupName;
   }

   /**
    * @return {@link #_inboundPolicyMaps}
    */
   public Set<PolicyMap> getInboundPolicyMaps() {
      return _inboundPolicyMaps;
   }

   /**
    * @return {@link #_localAs}
    */
   public Integer getLocalAs() {
      return _localAs;
   }

   /**
    * @return {@link #_originationPolicies}
    */
   public Set<PolicyMap> getOriginationPolicies() {
      return _originationPolicies;
   }

   /**
    * @return {@link #_outboundPolicyMaps}
    */
   public Set<PolicyMap> getOutboundPolicyMaps() {
      return _outboundPolicyMaps;
   }

   /**
    * @return {@link #_prefix} if non-null, else /32 prefix of {@link #_address}
    */
   public Prefix getPrefix() {
      if (_prefix != null) {
         return _prefix;
      }
      else {
         return new Prefix(_address, 32);
      }
   }

   /**
    * @return {@link #_remoteAs}
    */
   public Integer getRemoteAs() {
      return _remoteAs;
   }

   /**
    * @return {@link #_sendCommunity}
    */
   public Boolean getSendCommunity() {
      return _sendCommunity;
   }

   /**
    * @return {@link #_updateSource}
    */
   public String getUpdateSource() {
      return _updateSource;
   }

   /**
    * Sets {@link #_clusterId}
    *
    * @param clusterId
    */
   public void setClusterId(Long clusterId) {
      _clusterId = clusterId;
   }

   /**
    * Sets {@link #_defaultMetric}
    *
    * @param defaultMetric
    */
   public void setDefaultMetric(Integer defaultMetric) {
      _defaultMetric = defaultMetric;
   }

   /**
    * Sets {@link #_groupName}
    *
    * @param name
    */
   public void setGroupName(String name) {
      _groupName = name;
   }

   /**
    * Sets {@link #_localAs}
    *
    * @param localAs
    */
   public void setLocalAs(Integer localAs) {
      _localAs = localAs;
   }

   /**
    * Sets {@link #_remoteAs}
    *
    * @param remoteAs
    */
   public void setRemoteAs(Integer remoteAs) {
      _remoteAs = remoteAs;
   }

   /**
    * Sets {@link #_sendCommunity}
    *
    * @param sendCommunity
    */
   public void setSendCommunity(Boolean sendCommunity) {
      _sendCommunity = sendCommunity;
   }

   /**
    * Sets {@link #_updateSource}
    *
    * @param updateSource
    */
   public void setUpdateSource(String updateSource) {
      _updateSource = updateSource;
   }

}
