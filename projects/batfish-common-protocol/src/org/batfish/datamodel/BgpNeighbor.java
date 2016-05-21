package org.batfish.datamodel;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * Represents a peering with a single router (by ip address) acting as a bgp
 * peer to the router whose configuration's BGP process contains this object
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
@JsonInclude(Include.NON_NULL)
public final class BgpNeighbor implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   /**
    * The ip address of this peer for the purpose of this peering
    */
   private Ip _address;

   private boolean _advertiseInactive;

   private boolean _allowLocalAsIn;

   private boolean _allowRemoteAsOut;

   private transient Set<BgpNeighbor> _candidateRemoteBgpNeighbors;

   /**
    * The cluster id associated with this peer to be used in route reflection
    */
   private Long _clusterId;

   /**
    * The default metric associated with routes sent to this peer
    */
   private Integer _defaultMetric;

   private String _description;

   private boolean _ebgpMultihop;

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
    * The ip address of the containing router as reported to this peer
    */
   private Ip _localIp;

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

   private final Configuration _owner;

   /**
    * Ip range from which to accept dynamic BGP peering sessions.
    */
   private Prefix _prefix;

   /**
    * The autonomous system number that the containing BGP process considers
    * this peer to have.
    */
   private Integer _remoteAs;

   private transient BgpNeighbor _remoteBgpNeighbor;

   /**
    * Flag governing whether to include community numbers in outgoing route
    * advertisements to this peer
    */
   private Boolean _sendCommunity;

   private BgpNeighbor(Configuration owner) {
      _outboundPolicyMaps = new LinkedHashSet<PolicyMap>();
      _inboundPolicyMaps = new LinkedHashSet<PolicyMap>();
      _originationPolicies = new LinkedHashSet<PolicyMap>();
      _generatedRoutes = new LinkedHashSet<GeneratedRoute>();
      _owner = owner;
   }

   /**
    * Constructs a BgpNeighbor with the given peer ip address for
    * {@link #_address}
    *
    * @param address
    */
   public BgpNeighbor(Ip address, Configuration owner) {
      this(owner);
      _address = address;
   }

   /**
    * Constructs a BgpNeighbor with the given peer dynamic ip range for
    * {@link #_prefix}
    *
    * @param prefix
    */
   public BgpNeighbor(Prefix prefix, Configuration owner) {
      this(owner);
      _prefix = prefix;
   }

   /**
    * @return {@link #_address}
    */
   public Ip getAddress() {
      return _address;
   }

   public boolean getAdvertiseInactive() {
      return _advertiseInactive;
   }

   public boolean getAllowLocalAsIn() {
      return _allowLocalAsIn;
   }

   public boolean getAllowRemoteAsOut() {
      return _allowRemoteAsOut;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Set<BgpNeighbor> getCandidateRemoteBgpNeighbors() {
      return _candidateRemoteBgpNeighbors;
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

   public String getDescription() {
      return _description;
   }

   public boolean getEbgpMultihop() {
      return _ebgpMultihop;
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
   @JsonIdentityReference(alwaysAsId = true)
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
    * @return {@link #_localIp}
    */
   public Ip getLocalIp() {
      return _localIp;
   }

   /**
    * @return {@link #_originationPolicies}
    */
   @JsonIdentityReference(alwaysAsId = true)
   public Set<PolicyMap> getOriginationPolicies() {
      return _originationPolicies;
   }

   /**
    * @return {@link #_outboundPolicyMaps}
    */
   @JsonIdentityReference(alwaysAsId = true)
   public Set<PolicyMap> getOutboundPolicyMaps() {
      return _outboundPolicyMaps;
   }

   @JsonIdentityReference(alwaysAsId = true)
   public Configuration getOwner() {
      return _owner;
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

   @JsonIdentityReference(alwaysAsId = true)
   public BgpNeighbor getRemoteBgpNeighbor() {
      return _remoteBgpNeighbor;
   }

   /**
    * @return {@link #_sendCommunity}
    */
   public Boolean getSendCommunity() {
      return _sendCommunity;
   }

   public void initCandidateRemoteBgpNeighbors() {
      _candidateRemoteBgpNeighbors = new LinkedHashSet<BgpNeighbor>();
   }

   public void setAdvertiseInactive(boolean advertiseInactive) {
      _advertiseInactive = advertiseInactive;
   }

   public void setAllowLocalAsIn(boolean allowLocalAsIn) {
      _allowLocalAsIn = allowLocalAsIn;
   }

   public void setAllowRemoteAsOut(boolean allowRemoteAsOut) {
      _allowRemoteAsOut = allowRemoteAsOut;
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

   public void setDescription(String description) {
      _description = description;
   }

   public void setEbgpMultihop(boolean ebgpMultihop) {
      _ebgpMultihop = ebgpMultihop;
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
    * Sets {@link #_localIp}
    *
    * @param updateSource
    */
   public void setLocalIp(Ip updateSource) {
      _localIp = updateSource;
   }

   /**
    * Sets {@link #_remoteAs}
    *
    * @param remoteAs
    */
   public void setRemoteAs(Integer remoteAs) {
      _remoteAs = remoteAs;
   }

   public void setRemoteBgpNeighbor(BgpNeighbor remoteBgpNeighbor) {
      _remoteBgpNeighbor = remoteBgpNeighbor;
   }

   /**
    * Sets {@link #_sendCommunity}
    *
    * @param sendCommunity
    */
   public void setSendCommunity(Boolean sendCommunity) {
      _sendCommunity = sendCommunity;
   }

   @Override
   public String toString() {
      return "BgpNeighbor<Ip:" + _address + ", AS:" + _remoteAs + ">";
   }

}
