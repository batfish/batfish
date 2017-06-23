package org.batfish.datamodel;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

import org.batfish.common.BatfishException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * Instances of this class represent hypothetical BGP advertisements used for
 * testing, or where the config of an advertising border router is unavailable
 *
 * @author arifogel
 *
 */

@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
public class BgpAdvertisement
      implements Comparable<BgpAdvertisement>, Serializable {

   public enum BgpAdvertisementType {
      EBGP_ORIGINATED,
      EBGP_RECEIVED,
      EBGP_SENT,
      IBGP_ORIGINATED,
      IBGP_RECEIVED,
      IBGP_SENT;

      private final static Map<String, BgpAdvertisementType> _map = buildMap();

      private static Map<String, BgpAdvertisementType> buildMap() {
         Map<String, BgpAdvertisementType> map = new HashMap<>();
         for (BgpAdvertisementType bgpAdvertisementType : BgpAdvertisementType
               .values()) {
            String name = bgpAdvertisementType.toString().toLowerCase();
            map.put(name, bgpAdvertisementType);
         }
         return Collections.unmodifiableMap(map);
      }

      @JsonCreator
      public static BgpAdvertisementType fromName(String name) {
         String lName = name.toLowerCase();
         BgpAdvertisementType bgpAdvertisementType = _map.get(lName);
         if (bgpAdvertisementType == null) {
            throw new BatfishException("Invalid name: \"" + name + "\"");
         }
         return bgpAdvertisementType;
      }

      @JsonValue
      public String getName() {
         return name().toLowerCase();
      }

   }

   private static final String AS_PATH_VAR = "asPath";

   private static final String CLUSTER_LIST_VAR = "clusterList";

   private static final String COMMUNITIES_VAR = "communities";

   private static final String DST_IP_VAR = "dstIp";

   private static final String DST_NODE_VAR = "dstNode";

   private static final String DST_VRF_VAR = "dstVrf";

   private static final String LOCAL_PREFERENCE_VAR = "localPreference";

   private static final String MED_VAR = "med";

   private static final String NETWORK_VAR = "network";

   private static final String NEXT_HOP_IP_VAR = "nextHopIp";

   private static final String ORIGIN_TYPE_VAR = "originType";

   private static final String ORIGINATOR_IP_VAR = "originatorIp";

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private static final String SRC_IP_VAR = "srcIp";

   private static final String SRC_NODE_VAR = "srcNode";

   private static final String SRC_PROTOCOL_VAR = "srcProtocol";

   private static final String SRC_VRF_VAR = "srcVrf";

   private static final String TYPE_VAR = "type";

   public static final int UNSET_LOCAL_PREFERENCE = 0;

   public static final Ip UNSET_ORIGINATOR_IP = new Ip(-1l);

   public static final int UNSET_WEIGHT = 0;

   private static final String WEIGHT_VAR = "weight";

   private final AsPath _asPath;

   private final SortedSet<Long> _clusterList;

   private final SortedSet<Long> _communities;

   private final Ip _dstIp;

   private final String _dstNode;

   private final String _dstVrf;

   private final int _localPreference;

   private final int _med;

   private final Prefix _network;

   private final Ip _nextHopIp;

   private final Ip _originatorIp;

   private final OriginType _originType;

   private final Ip _srcIp;

   private final String _srcNode;

   private final RoutingProtocol _srcProtocol;

   private final String _srcVrf;

   private final BgpAdvertisementType _type;

   private final int _weight;

   @JsonCreator
   public BgpAdvertisement(@JsonProperty(TYPE_VAR) BgpAdvertisementType type,
         @JsonProperty(NETWORK_VAR) Prefix network,
         @JsonProperty(NEXT_HOP_IP_VAR) Ip nextHopIp,
         @JsonProperty(SRC_NODE_VAR) String srcNode,
         @JsonProperty(SRC_VRF_VAR) String srcVrf,
         @JsonProperty(SRC_IP_VAR) Ip srcIp,
         @JsonProperty(DST_NODE_VAR) String dstNode,
         @JsonProperty(DST_VRF_VAR) String dstVrf,
         @JsonProperty(DST_IP_VAR) Ip dstIp,
         @JsonProperty(SRC_PROTOCOL_VAR) RoutingProtocol srcProtocol,
         @JsonProperty(ORIGIN_TYPE_VAR) OriginType originType,
         @JsonProperty(LOCAL_PREFERENCE_VAR) int localPreference,
         @JsonProperty(MED_VAR) int med,
         @JsonProperty(ORIGINATOR_IP_VAR) Ip originatorIp,
         @JsonProperty(AS_PATH_VAR) AsPath asPath,
         @JsonProperty(COMMUNITIES_VAR) SortedSet<Long> communities,
         @JsonProperty(CLUSTER_LIST_VAR) SortedSet<Long> clusterList,
         @JsonProperty(WEIGHT_VAR) int weight) {
      _type = type;
      _network = network;
      _nextHopIp = nextHopIp;
      _srcNode = srcNode;
      _srcVrf = srcVrf;
      _srcIp = srcIp;
      _dstNode = dstNode;
      _dstVrf = dstVrf;
      _dstIp = dstIp;
      _srcProtocol = srcProtocol;
      _originType = originType;
      _localPreference = localPreference;
      _med = med;
      _originatorIp = originatorIp;
      _asPath = asPath;
      _communities = communities;
      _clusterList = clusterList;
      _weight = weight;
   }

   @Override
   public int compareTo(BgpAdvertisement rhs) {
      int ret;
      ret = _type.compareTo(rhs._type);
      if (ret != 0) {
         return ret;
      }
      ret = _srcNode.compareTo(rhs._srcNode);
      if (ret != 0) {
         return ret;
      }
      ret = _srcVrf.compareTo(rhs._srcVrf);
      if (ret != 0) {
         return ret;
      }
      ret = _dstNode.compareTo(rhs._dstNode);
      if (ret != 0) {
         return ret;
      }
      if (_dstVrf == null) {
         if (rhs._dstVrf != null) {
            ret = -1;
         }
         else {
            ret = 0;
         }
      }
      else if (rhs._dstVrf == null) {
         ret = 1;
      }
      else {
         ret = _dstVrf.compareTo(rhs._dstVrf);
      }
      if (ret != 0) {
         return ret;
      }
      ret = _dstNode.compareTo(rhs._dstNode);
      if (ret != 0) {
         return ret;
      }
      ret = _network.compareTo(rhs._network);
      if (ret != 0) {
         return ret;
      }
      ret = Integer.compare(_localPreference, rhs._localPreference);
      if (ret != 0) {
         return ret;
      }
      ret = Integer.compare(_med, rhs._med);
      if (ret != 0) {
         return ret;
      }
      ret = _nextHopIp.compareTo(rhs._nextHopIp);
      if (ret != 0) {
         return ret;
      }
      ret = _originatorIp.compareTo(rhs._originatorIp);
      if (ret != 0) {
         return ret;
      }
      ret = _originType.compareTo(rhs._originType);
      if (ret != 0) {
         return ret;
      }
      ret = _srcProtocol.compareTo(rhs._srcProtocol);
      if (ret != 0) {
         return ret;
      }
      ret = _asPath.toString().compareTo(rhs._asPath.toString());
      if (ret != 0) {
         return ret;
      }
      ret = _communities.toString().compareTo(rhs._communities.toString());
      if (ret != 0) {
         return ret;
      }
      ret = _clusterList.toString().compareTo(rhs._clusterList.toString());
      if (ret != 0) {
         return ret;
      }
      ret = Integer.compare(_weight, rhs._weight);
      if (ret != 0) {
         return ret;
      }
      return 0;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      BgpAdvertisement other = (BgpAdvertisement) obj;
      if (!_network.equals(other._network)) {
         return false;
      }
      if (!_asPath.toString().equals(other._asPath.toString())) {
         return false;
      }
      if (!_clusterList.toString().equals(other._clusterList.toString())) {
         return false;
      }
      if (!_communities.toString().equals(other._communities.toString())) {
         return false;
      }
      if (!_dstIp.equals(other._dstIp)) {
         return false;
      }
      if (!_dstNode.equals(other._dstNode)) {
         return false;
      }
      if (_dstVrf == null) {
         if (other._dstVrf != null) {
            return false;
         }
      }
      else if (!_dstVrf.equals(other._dstVrf)) {
         return false;
      }
      if (_localPreference != other._localPreference) {
         return false;
      }
      if (_med != other._med) {
         return false;
      }
      if (!_nextHopIp.equals(other._nextHopIp)) {
         return false;
      }
      if (_originType != other._originType) {
         return false;
      }
      if (!_originatorIp.equals(other._originatorIp)) {
         return false;
      }
      if (!_srcIp.equals(other._srcIp)) {
         return false;
      }
      if (!_srcNode.equals(other._srcNode)) {
         return false;
      }
      if (_srcProtocol != other._srcProtocol) {
         return false;
      }
      if (!_srcVrf.equals(other._srcVrf)) {
         return false;
      }
      if (!_type.equals(other._type)) {
         return false;
      }
      if (_weight != other._weight) {
         return false;
      }
      return true;
   }

   @JsonProperty(AS_PATH_VAR)
   public AsPath getAsPath() {
      return _asPath;
   }

   @JsonProperty(CLUSTER_LIST_VAR)
   public SortedSet<Long> getClusterList() {
      return Collections.unmodifiableSortedSet(_clusterList);
   }

   @JsonProperty(COMMUNITIES_VAR)
   public SortedSet<Long> getCommunities() {
      return Collections.unmodifiableSortedSet(_communities);
   }

   @JsonProperty(DST_IP_VAR)
   public Ip getDstIp() {
      return _dstIp;
   }

   @JsonProperty(DST_NODE_VAR)
   public String getDstNode() {
      return _dstNode;
   }

   @JsonProperty(DST_VRF_VAR)
   public String getDstVrf() {
      return _dstVrf;
   }

   @JsonProperty(LOCAL_PREFERENCE_VAR)
   public int getLocalPreference() {
      return _localPreference;
   }

   @JsonProperty(MED_VAR)
   public int getMed() {
      return _med;
   }

   @JsonProperty(NETWORK_VAR)
   public Prefix getNetwork() {
      return _network;
   }

   @JsonProperty(NEXT_HOP_IP_VAR)
   public Ip getNextHopIp() {
      return _nextHopIp;
   }

   @JsonProperty(ORIGINATOR_IP_VAR)
   public Ip getOriginatorIp() {
      return _originatorIp;
   }

   @JsonProperty(ORIGIN_TYPE_VAR)
   public OriginType getOriginType() {
      return _originType;
   }

   @JsonProperty(SRC_IP_VAR)
   public Ip getSrcIp() {
      return _srcIp;
   }

   @JsonProperty(SRC_NODE_VAR)
   public String getSrcNode() {
      return _srcNode;
   }

   @JsonProperty(SRC_PROTOCOL_VAR)
   public RoutingProtocol getSrcProtocol() {
      return _srcProtocol;
   }

   @JsonProperty(SRC_VRF_VAR)
   public String getSrcVrf() {
      return _srcVrf;
   }

   @JsonProperty(TYPE_VAR)
   public BgpAdvertisementType getType() {
      return _type;
   }

   @JsonProperty(WEIGHT_VAR)
   public int getWeight() {
      return _weight;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + _asPath.hashCode();
      result = prime * result + _clusterList.hashCode();
      result = prime * result + _communities.hashCode();
      result = prime * result + _dstIp.hashCode();
      result = prime * result + _dstNode.hashCode();
      result = prime * result + (_dstVrf != null ? _dstVrf.hashCode() : 0);
      result = prime * result + _localPreference;
      result = prime * result + _med;
      result = prime * result + _network.hashCode();
      result = prime * result + _nextHopIp.hashCode();
      result = prime * result + _originType.ordinal();
      result = prime * result + _originatorIp.hashCode();
      result = prime * result + _srcIp.hashCode();
      result = prime * result + _srcNode.hashCode();
      result = prime * result + _srcProtocol.ordinal();
      result = prime * result + _srcVrf.hashCode();
      result = prime * result + _type.ordinal();
      result = prime * result + _weight;
      return result;
   }

   public String prettyPrint(String diffSymbol) {
      String net = getNetwork().toString();
      String prot = _srcProtocol.protocolName();
      String diffStr = diffSymbol != null ? diffSymbol + " " : "";
      String routeStr = String.format(
            "%s%s dstNode:%s dstVrf:%s dstIp:%s srcNode:%s srcVrf:%s srcIp:%s net:%s nhip:%s origin:%s lp:%s med:%s weight:%s asPath:%s communities:%s orIp:%s clst:%s srcProt:%s\n",
            diffStr, _type, _dstNode, _dstVrf, _dstIp, _srcNode, _srcVrf,
            _srcIp, net, _nextHopIp, _originType, _localPreference, _med,
            _weight, _asPath, _communities, _originatorIp, _clusterList, prot);
      return routeStr;
   }

   @Override
   public String toString() {
      String originatorIp = _originatorIp.equals(UNSET_ORIGINATOR_IP) ? "N/A"
            : _originatorIp.toString();
      return "BgpAdvert<" + _type + ", " + _network + ", " + _nextHopIp + ", "
            + _srcIp + ", " + _dstIp + ", " + _srcProtocol + ", " + _srcNode
            + ", " + _srcVrf + ", " + _dstNode + ", " + _dstVrf + ", "
            + _localPreference + ", " + _med + ", " + originatorIp + ", "
            + _originType + ">";
   }

}
