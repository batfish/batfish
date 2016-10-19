package org.batfish.datamodel;

import java.util.LinkedHashSet;
import java.util.Set;

import org.batfish.common.util.CommonUtil;
import org.batfish.common.util.ComparableStructure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a peering with a single router (by ip address) acting as a bgp
 * peer to the router whose configuration's BGP process contains this object
 */
public final class BgpNeighbor extends ComparableStructure<Prefix> {

   public static final class BgpNeighborSummary
         extends ComparableStructure<String> {

      private static final String DESCRIPTION_VAR = "description";

      private static final String GROUP_VAR = "group";

      private static final String LOCAL_AS_VAR = "localAs";

      private static final String LOCAL_IP_VAR = "localIp";

      private static final String REMOTE_AS_VAR = "remoteAs";

      private static final String REMOTE_IP_VAR = "remoteIp";

      private static final String REMOTE_PREFIX_VAR = "dynamicRemotePrefix";

      /**
       *
       */
      private static final long serialVersionUID = 1L;

      private final String _description;

      private final String _group;

      private final int _localAs;

      private final Ip _localIp;

      private final int _remoteAs;

      private final Ip _remoteIp;

      private final Prefix _remotePrefix;

      public BgpNeighborSummary(BgpNeighbor bgpNeighbor) {
         super(bgpNeighbor.getOwner().getName() + ":"
               + (bgpNeighbor.getDynamic() ? bgpNeighbor.getPrefix().toString()
                     : bgpNeighbor.getAddress().toString()));
         _description = bgpNeighbor._description;
         _group = bgpNeighbor._group;
         _localAs = bgpNeighbor._localAs;
         _localIp = bgpNeighbor._localIp;
         _remoteAs = bgpNeighbor._remoteAs;
         _remoteIp = bgpNeighbor.getAddress();
         _remotePrefix = bgpNeighbor._key;
      }

      @JsonCreator
      public BgpNeighborSummary(@JsonProperty(NAME_VAR) String name,
            @JsonProperty(DESCRIPTION_VAR) String description,
            @JsonProperty(GROUP_VAR) String group,
            @JsonProperty(LOCAL_AS_VAR) int localAs,
            @JsonProperty(LOCAL_IP_VAR) Ip localIp,
            @JsonProperty(REMOTE_AS_VAR) int remoteAs,
            @JsonProperty(REMOTE_IP_VAR) Ip remoteIp,
            @JsonProperty(REMOTE_PREFIX_VAR) Prefix remotePrefix) {
         super(name);
         _description = description;
         _group = group;
         _localAs = localAs;
         _localIp = localIp;
         _remoteAs = remoteAs;
         _remoteIp = remoteIp;
         _remotePrefix = remotePrefix;
      }

      @JsonProperty(DESCRIPTION_VAR)
      public String getDescription() {
         return _description;
      }

      @JsonProperty(GROUP_VAR)
      public String getGroup() {
         return _group;
      }

      @JsonProperty(LOCAL_AS_VAR)
      public int getLocalAs() {
         return _localAs;
      }

      @JsonProperty(LOCAL_IP_VAR)
      public Ip getLocalIp() {
         return _localIp;
      }

      @JsonIgnore
      public Prefix getPrefix() {
         if (_remotePrefix == null) {
            return new Prefix(_remoteIp, 32);
         }
         else {
            return _remotePrefix;
         }
      }

      @JsonProperty(REMOTE_AS_VAR)
      public int getRemoteAs() {
         return _remoteAs;
      }

      @JsonProperty(REMOTE_IP_VAR)
      public Ip getRemoteIp() {
         return _remoteIp;
      }

      @JsonProperty(REMOTE_PREFIX_VAR)
      public Prefix getRemotePrefix() {
         return _remotePrefix;
      }

   }

   private static final String ADDRESS_VAR = "address";

   private static final String ADVERTISE_EXTERNAL_VAR = "advertiseExternal";

   private static final String ADVERTISE_INACTIVE_VAR = "advertiseInactive";

   private static final String ALLOW_LOCAL_AS_IN_VAR = "allowLocalAsIn";

   private static final String ALLOW_REMOTE_AS_OUT_VAR = "allowRemoteAsOut";

   private static final String CLUSTER_ID_VAR = "clusterId";

   private static final String DEFAULT_METRIC_VAR = "defaultMetric";

   private static final String DESCRIPTION_VAR = "description";

   private static final String DYNAMIC_VAR = "dynamic";

   private static final String EBGP_MULTIHOP_VAR = "ebgpMultihop";

   private static final String GENERATED_ROUTES_VAR = "generatedRoutes";

   private static final String GROUP_VAR = "group";

   private static final String LOCAL_AS_VAR = "localAs";

   private static final String LOCAL_IP_VAR = "localIp";

   private static final String NAME_VAR = "name";

   private static final String OWNER_VAR = "owner";

   private static final String REMOTE_AS_VAR = "remoteAs";

   private static final String REMOTE_PREFIX_VAR = "remotePrefix";

   private static final String SEND_COMMUNITY_VAR = "sendCommunity";

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private boolean _advertiseExternal;

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
   private int _defaultMetric;

   private String _description;

   private boolean _ebgpMultihop;

   private String _exportPolicy;

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
   private String _group;

   private String _importPolicy;

   /**
    * The autonomous system number of the containing BGP process as reported to
    * this peer
    */
   private Integer _localAs;

   /**
    * The ip address of the containing router as reported to this peer
    */
   private Ip _localIp;

   private Configuration _owner;

   /**
    * The autonomous system number that the containing BGP process considers
    * this peer to have.
    */
   private Integer _remoteAs;

   private transient BgpNeighbor _remoteBgpNeighbor;

   private boolean _routeReflectorClient;

   /**
    * Flag governing whether to include community numbers in outgoing route
    * advertisements to this peer
    */
   private boolean _sendCommunity;

   @SuppressWarnings("unused")
   private BgpNeighbor() {
      this(null);
   }

   /**
    * Constructs a BgpNeighbor with the given peer ip address for
    * {@link #_address}
    *
    * @param address
    */
   public BgpNeighbor(Ip address, Configuration owner) {
      this(new Prefix(address, 32), owner);
   }

   @JsonCreator
   public BgpNeighbor(@JsonProperty(NAME_VAR) Prefix prefix) {
      super(prefix);
      _generatedRoutes = new LinkedHashSet<>();
   }

   /**
    * Constructs a BgpNeighbor with the given peer dynamic ip range for
    * {@link #_network}
    *
    * @param prefix
    */
   public BgpNeighbor(Prefix prefix, Configuration owner) {
      this(prefix);
      _owner = owner;
   }
   
   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }
      BgpNeighbor other = (BgpNeighbor) o;
      if (this._advertiseExternal != other._advertiseExternal) {
         return false;
      }
      if (this._advertiseInactive != other._advertiseInactive) {
         return false;
      }
      if (this._allowLocalAsIn != other._allowLocalAsIn) {
         return false;
      }
      if (this._allowRemoteAsOut != other._allowRemoteAsOut) {
         return false;
      }
      if (!CommonUtil.bothNullOrEqual(this._candidateRemoteBgpNeighbors, other._candidateRemoteBgpNeighbors)) {
         return false;
      }
      if (!this._clusterId.equals(other._clusterId)) {
         return false;
      }
      if (this._defaultMetric!= other._defaultMetric) {
         return false;
      }
      // we will skip description
      if (this._ebgpMultihop != other._ebgpMultihop) {
         return false;
      }
      if (!this._exportPolicy.equals(other._exportPolicy)) {
         return false;
      }
      // we will skip generated routes. 
      if (!CommonUtil.bothNullOrEqual(this._group, other._group)) {
         return false;
      }
      if (!CommonUtil.bothNullOrEqual(this._importPolicy, other._importPolicy)) {
         return false;
      }
      if (!this._localAs.equals(other._localAs)) {
         return false;
      }
      if (!CommonUtil.bothNullOrEqual(this._localIp, other._localIp)) {
         return false;
      }
      //we will skip owner.
      if (!this._remoteAs.equals(other._remoteAs)) {
         return false;
      }
      if (!CommonUtil.bothNullOrEqual(this._remoteBgpNeighbor, other._remoteBgpNeighbor)) {
         return false;
      }
      if (this._routeReflectorClient != other._routeReflectorClient) {
         return false;
      }
      if (this._sendCommunity != other._sendCommunity) {
         return false;
      }
      return true;
   }

   /**
    * @return {@link #_address}
    */
   @JsonProperty(ADDRESS_VAR)
   public Ip getAddress() {
      if (_key != null && _key.getPrefixLength() == 32) {
         return _key.getAddress();
      }
      else {
         return null;
      }
   }

   @JsonProperty(ADVERTISE_EXTERNAL_VAR)
   public boolean getAdvertiseExternal() {
      return _advertiseExternal;
   }

   @JsonProperty(ADVERTISE_INACTIVE_VAR)
   public boolean getAdvertiseInactive() {
      return _advertiseInactive;
   }

   @JsonProperty(ALLOW_LOCAL_AS_IN_VAR)
   public boolean getAllowLocalAsIn() {
      return _allowLocalAsIn;
   }

   @JsonProperty(ALLOW_REMOTE_AS_OUT_VAR)
   public boolean getAllowRemoteAsOut() {
      return _allowRemoteAsOut;
   }

   @JsonIgnore
   public Set<BgpNeighbor> getCandidateRemoteBgpNeighbors() {
      return _candidateRemoteBgpNeighbors;
   }

   /**
    * @return {@link #_clusterId}
    */
   @JsonProperty(CLUSTER_ID_VAR)
   public Long getClusterId() {
      return _clusterId;
   }

   /**
    * @return {@link #_defaultMetric}
    */
   @JsonProperty(DEFAULT_METRIC_VAR)
   public int getDefaultMetric() {
      return _defaultMetric;
   }

   @JsonProperty(DESCRIPTION_VAR)
   public String getDescription() {
      return _description;
   }

   @JsonProperty(DYNAMIC_VAR)
   public boolean getDynamic() {
      return _key != null && _key.getPrefixLength() < 32;
   }

   @JsonProperty(EBGP_MULTIHOP_VAR)
   public boolean getEbgpMultihop() {
      return _ebgpMultihop;
   }

   public String getExportPolicy() {
      return _exportPolicy;
   }

   /**
    * @return {@link #_generatedRoutes}
    */
   @JsonProperty(GENERATED_ROUTES_VAR)
   public Set<GeneratedRoute> getGeneratedRoutes() {
      return _generatedRoutes;
   }

   /**
    * @return {@link #_group}
    */
   @JsonProperty(GROUP_VAR)
   public String getGroup() {
      return _group;
   }

   public String getImportPolicy() {
      return _importPolicy;
   }

   /**
    * @return {@link #_localAs}
    */
   @JsonProperty(LOCAL_AS_VAR)
   public Integer getLocalAs() {
      return _localAs;
   }

   /**
    * @return {@link #_localIp}
    */
   @JsonProperty(LOCAL_IP_VAR)
   public Ip getLocalIp() {
      return _localIp;
   }

   @JsonIdentityReference(alwaysAsId = true)
   @JsonProperty(OWNER_VAR)
   public Configuration getOwner() {
      return _owner;
   }

   /**
    * @return {@link #_network}
    */
   @JsonProperty(REMOTE_PREFIX_VAR)
   public Prefix getPrefix() {
      return _key;
   }

   /**
    * @return {@link #_remoteAs}
    */
   @JsonProperty(REMOTE_AS_VAR)
   public Integer getRemoteAs() {
      return _remoteAs;
   }

   @JsonIgnore
   public BgpNeighbor getRemoteBgpNeighbor() {
      return _remoteBgpNeighbor;
   }

   public boolean getRouteReflectorClient() {
      return _routeReflectorClient;
   }

   /**
    * @return {@link #_sendCommunity}
    */
   @JsonProperty(SEND_COMMUNITY_VAR)
   public boolean getSendCommunity() {
      return _sendCommunity;
   }

   public void initCandidateRemoteBgpNeighbors() {
      _candidateRemoteBgpNeighbors = new LinkedHashSet<>();
   }

   @JsonProperty(ADDRESS_VAR)
   public void setAddress(Ip address) {
      // Intentionally empty
   }

   @JsonProperty(ADVERTISE_EXTERNAL_VAR)
   public void setAdvertiseExternal(boolean advertiseExternal) {
      _advertiseExternal = advertiseExternal;
   }

   @JsonProperty(ADVERTISE_INACTIVE_VAR)
   public void setAdvertiseInactive(boolean advertiseInactive) {
      _advertiseInactive = advertiseInactive;
   }

   @JsonProperty(ALLOW_LOCAL_AS_IN_VAR)
   public void setAllowLocalAsIn(boolean allowLocalAsIn) {
      _allowLocalAsIn = allowLocalAsIn;
   }

   @JsonProperty(ALLOW_REMOTE_AS_OUT_VAR)
   public void setAllowRemoteAsOut(boolean allowRemoteAsOut) {
      _allowRemoteAsOut = allowRemoteAsOut;
   }

   /**
    * Sets {@link #_clusterId}
    *
    * @param clusterId
    */
   @JsonProperty(CLUSTER_ID_VAR)
   public void setClusterId(Long clusterId) {
      _clusterId = clusterId;
   }

   /**
    * Sets {@link #_defaultMetric}
    *
    * @param defaultMetric
    */
   @JsonProperty(DEFAULT_METRIC_VAR)
   public void setDefaultMetric(int defaultMetric) {
      _defaultMetric = defaultMetric;
   }

   @JsonProperty(DESCRIPTION_VAR)
   public void setDescription(String description) {
      _description = description;
   }

   @JsonProperty(DYNAMIC_VAR)
   public void setDynamic(boolean dynamic) {
      // Intentionally empty
   }

   @JsonProperty(EBGP_MULTIHOP_VAR)
   public void setEbgpMultihop(boolean ebgpMultihop) {
      _ebgpMultihop = ebgpMultihop;
   }

   public void setExportPolicy(String originationPolicyName) {
      _exportPolicy = originationPolicyName;
   }

   @JsonProperty(GENERATED_ROUTES_VAR)
   public void setGeneratedRoutes(Set<GeneratedRoute> generatedRoutes) {
      _generatedRoutes = generatedRoutes;
   }

   /**
    * Sets {@link #_group}
    *
    * @param name
    */
   @JsonProperty(GROUP_VAR)
   public void setGroup(String name) {
      _group = name;
   }

   public void setImportPolicy(String importPolicy) {
      _importPolicy = importPolicy;
   }

   /**
    * Sets {@link #_localAs}
    *
    * @param localAs
    */
   @JsonProperty(LOCAL_AS_VAR)
   public void setLocalAs(Integer localAs) {
      _localAs = localAs;
   }

   /**
    * Sets {@link #_localIp}
    *
    * @param localIp
    */
   @JsonProperty(LOCAL_IP_VAR)
   public void setLocalIp(Ip localIp) {
      _localIp = localIp;
   }

   @JsonProperty(OWNER_VAR)
   public void setOwner(Configuration owner) {
      _owner = owner;
   }

   /**
    * Sets {@link #_remoteAs}
    *
    * @param remoteAs
    */
   @JsonProperty(REMOTE_AS_VAR)
   public void setRemoteAs(Integer remoteAs) {
      _remoteAs = remoteAs;
   }

   public void setRemoteBgpNeighbor(BgpNeighbor remoteBgpNeighbor) {
      _remoteBgpNeighbor = remoteBgpNeighbor;
   }

   @JsonProperty(REMOTE_PREFIX_VAR)
   public void setRemotePrefix(Prefix remotePrefix) {
      // Intentionally empty
   }

   public void setRouteReflectorClient(boolean routeReflectorClient) {
      _routeReflectorClient = routeReflectorClient;
   }

   /**
    * Sets {@link #_sendCommunity}
    *
    * @param sendCommunity
    */
   @JsonProperty(SEND_COMMUNITY_VAR)
   public void setSendCommunity(boolean sendCommunity) {
      _sendCommunity = sendCommunity;
   }

   @Override
   public String toString() {
      return "BgpNeighbor<Prefix:" + _key + ", AS:" + _remoteAs + ">";
   }

}
