package org.batfish.datamodel;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.batfish.common.BatfishException;
import org.batfish.common.BfConsts;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.collections.CommunitySet;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * Instances of this class represent hypothetical BGP advertisements used for
 * testing, or where the config of an advertising border router is unavailable
 *
 * @author arifogel
 *
 */

@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
public class BgpAdvertisement implements Comparable<BgpAdvertisement>,
      Serializable {

   public enum BgpAdvertisementType {
      EBGP_ORIGINATED("bgp"),
      EBGP_RECEIVED("bgp_ti"),
      EBGP_SENT("bgp_to"),
      IBGP_ORIGINATED("ibgp"),
      IBGP_RECEIVED("ibgp_ti"),
      IBGP_SENT("ibgp_to");

      private final static Map<String, BgpAdvertisementType> _map = buildMap();

      private static Map<String, BgpAdvertisementType> buildMap() {
         Map<String, BgpAdvertisementType> map = new HashMap<String, BgpAdvertisementType>();
         for (BgpAdvertisementType bgpAdvertisementType : BgpAdvertisementType
               .values()) {
            String nxtnetTypeName = bgpAdvertisementType._nxtnetTypeName;
            map.put(nxtnetTypeName, bgpAdvertisementType);
         }
         return Collections.unmodifiableMap(map);
      }

      public static BgpAdvertisementType fromNxtnetTypeName(
            String nxtnetTypeName) {
         BgpAdvertisementType bgpAdvertisementType = _map.get(nxtnetTypeName);
         if (bgpAdvertisementType == null) {
            throw new BatfishException("Invalid nxtnetTypeName: \""
                  + nxtnetTypeName + "\"");
         }
         return bgpAdvertisementType;
      }

      private String _nxtnetTypeName;

      private BgpAdvertisementType(String nxtnetTypeName) {
         _nxtnetTypeName = nxtnetTypeName;
      }

      public String getNxtnetTypeName() {
         return _nxtnetTypeName;
      }

   }

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private static final Ip UNSET_ORIGINATOR_IP = new Ip(-1l);

   private final AsPath _asPath;

   private final CommunitySet _communities;

   private final Ip _dstIp;

   private final String _dstNode;

   private final int _localPreference;

   private final int _med;

   private final Prefix _network;

   private final Ip _nextHopIp;

   private final Ip _originatorIp;

   private final OriginType _originType;

   private final Ip _srcIp;

   private final String _srcNode;

   private final RoutingProtocol _srcProtocol;

   private final String _type;

   public BgpAdvertisement(JSONObject announcement) throws JSONException {
      _type = announcement.getString(BfConsts.KEY_BGP_ANNOUNCEMENT_TYPE);
      _network = new Prefix(
            announcement.getString(BfConsts.KEY_BGP_ANNOUNCEMENT_PREFIX));
      _nextHopIp = new Ip(
            announcement.getString(BfConsts.KEY_BGP_ANNOUNCEMENT_NEXT_HOP_IP));
      _srcNode = announcement.getString(BfConsts.KEY_BGP_ANNOUNCEMENT_SRC_NODE);
      _srcIp = new Ip(
            announcement.getString(BfConsts.KEY_BGP_ANNOUNCEMENT_SRC_IP));
      _dstNode = announcement.getString(BfConsts.KEY_BGP_ANNOUNCEMENT_DST_NODE);
      _dstIp = new Ip(
            announcement.getString(BfConsts.KEY_BGP_ANNOUNCEMENT_DST_IP));
      _srcProtocol = RoutingProtocol.fromProtocolName(announcement
            .getString(BfConsts.KEY_BGP_ANNOUNCEMENT_SRC_PROTOCOL));
      _originType = OriginType.fromString(announcement
            .getString(BfConsts.KEY_BGP_ANNOUNCEMENT_ORIGIN_TYPE));
      _localPreference = announcement
            .getInt(BfConsts.KEY_BGP_ANNOUNCEMENT_LOCAL_PREF);
      _med = announcement.getInt(BfConsts.KEY_BGP_ANNOUNCEMENT_MED);
      _originatorIp = new Ip(
            announcement.getString(BfConsts.KEY_BGP_ANNOUNCEMENT_ORIGINATOR_IP));

      JSONArray jsonAsPath = announcement
            .getJSONArray(BfConsts.KEY_BGP_ANNOUNCEMENT_AS_PATH);
      _asPath = new AsPath(jsonAsPath.length());
      for (int pathIndex = 0; pathIndex < jsonAsPath.length(); pathIndex++) {
         JSONArray jsonAsSet = jsonAsPath.getJSONArray(pathIndex);
         AsSet asSet = new AsSet();
         for (int asIndex = 0; asIndex < jsonAsSet.length(); asIndex++) {
            asSet.add(jsonAsSet.getInt(asIndex));
         }
         _asPath.set(pathIndex, asSet);
      }

      _communities = new CommunitySet();
      if (announcement.has(BfConsts.KEY_BGP_ANNOUNCEMENT_COMMUNITIES)) {
         JSONArray jsonCommunities = announcement
               .getJSONArray(BfConsts.KEY_BGP_ANNOUNCEMENT_COMMUNITIES);
         for (int cIndex = 0; cIndex < jsonCommunities.length(); cIndex++) {
            Object currentCommunity = jsonCommunities.get(cIndex);
            long currentCommunityLong;
            if (currentCommunity instanceof Long
                  || currentCommunity instanceof Integer) {
               currentCommunityLong = (long) currentCommunity;
               _communities.add(currentCommunityLong);
            }
            else if (currentCommunity instanceof String) {
               String currentCommunityStr = (String) currentCommunity;
               currentCommunityLong = CommonUtil
                     .communityStringToLong(currentCommunityStr);
            }
            else {
               throw new BatfishException(
                     "Invalid community in BgpAdvertisement JSONArray: "
                           + currentCommunity.toString());
            }
            _communities.add(currentCommunityLong);
         }
      }
   }

   public BgpAdvertisement(String type, Prefix network, Ip nextHopIp,
         String srcNode, Ip srcIp, String dstNode, Ip dstIp,
         RoutingProtocol srcProtocol, OriginType originType,
         int localPreference, int med, Ip originatorIp, AsPath asPath,
         CommunitySet communities) {
      _type = type;
      _network = network;
      _nextHopIp = nextHopIp;
      _srcNode = srcNode;
      _srcIp = srcIp;
      _dstNode = dstNode;
      _dstIp = dstIp;
      _srcProtocol = srcProtocol;
      _originType = originType;
      _localPreference = localPreference;
      _med = med;
      _originatorIp = originatorIp;
      _asPath = asPath;
      _communities = communities;
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
      if (!_communities.toString().equals(other._communities.toString())) {
         return false;
      }
      if (!_dstIp.equals(other._dstIp)) {
         return false;
      }
      if (!_dstNode.equals(other._dstNode)) {
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
      if (!_type.equals(other._type)) {
         return false;
      }
      return true;
   }

   public AsPath getAsPath() {
      return _asPath;
   }

   public CommunitySet getCommunities() {
      return _communities;
   }

   public Ip getDstIp() {
      return _dstIp;
   }

   public String getDstNode() {
      return _dstNode;
   }

   public int getLocalPreference() {
      return _localPreference;
   }

   public int getMed() {
      return _med;
   }

   public Prefix getNetwork() {
      return _network;
   }

   public Ip getNextHopIp() {
      return _nextHopIp;
   }

   public Ip getOriginatorIp() {
      return _originatorIp;
   }

   public OriginType getOriginType() {
      return _originType;
   }

   public Ip getSrcIp() {
      return _srcIp;
   }

   public String getSrcNode() {
      return _srcNode;
   }

   public RoutingProtocol getSrcProtocol() {
      return _srcProtocol;
   }

   public String getType() {
      return _type;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + _asPath.hashCode();
      result = prime * result + _communities.hashCode();
      result = prime * result + _dstIp.hashCode();
      result = prime * result + _dstNode.hashCode();
      result = prime * result + _localPreference;
      result = prime * result + _med;
      result = prime * result + _network.hashCode();
      result = prime * result + _nextHopIp.hashCode();
      result = prime * result + _originType.hashCode();
      result = prime * result + _originatorIp.hashCode();
      result = prime * result + _srcIp.hashCode();
      result = prime * result + _srcNode.hashCode();
      result = prime * result + _srcProtocol.hashCode();
      result = prime * result + _type.hashCode();
      return result;
   }

   @Override
   public String toString() {
      String originatorIp = _originatorIp.equals(UNSET_ORIGINATOR_IP) ? "N/A"
            : _originatorIp.toString();
      return "BgpAdvert<" + _type + ", " + _network + ", " + _nextHopIp + ", "
            + _srcIp + ", " + _dstIp + ", " + _srcProtocol + ", " + _srcNode
            + ", " + _dstNode + ", " + _localPreference + ", " + _med + ", "
            + originatorIp + ", " + _originType + ">";
   }

}
