package org.batfish.representation;

import java.io.Serializable;

import org.batfish.collections.CommunitySet;
import org.batfish.common.BatfishException;
import org.batfish.common.BfConsts;
import org.batfish.util.Util;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * Instances of this class represent hypothetical BGP advertisements used for
 * testing, or where the config of an advertising border router is unavailable
 *
 * @author arifogel
 *
 */

public class BgpAdvertisement implements Serializable {

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
               currentCommunityLong = Util
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
   public String toString() {
      String originatorIp = _originatorIp.equals(UNSET_ORIGINATOR_IP) ? "N/A"
            : _originatorIp.toString();
      return "BgpAdvert<" + _type + ", " + _network + ", " + _nextHopIp + ", "
            + _srcIp + ", " + _dstIp + ", " + _srcProtocol + ", " + _srcNode
            + ", " + _dstNode + ", " + _localPreference + ", " + _med + ", "
            + originatorIp + ", " + _originType + ">";
   }

}
