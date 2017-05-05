package org.batfish.datamodel;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.batfish.datamodel.collections.CommunitySet;

public class BgpRoute extends AbstractRoute {

   public static class Builder extends AbstractRouteBuilder<BgpRoute> {

      private AsPath _asPath;

      private SortedSet<Long> _clusterList;

      private CommunitySet _communities;

      private int _localPreference;

      private Ip _originatorIp;

      private OriginType _originType;

      private RoutingProtocol _protocol;

      private boolean _receivedFromRouteReflectorClient;

      private RoutingProtocol _srcProtocol;

      private int _weight;

      public Builder() {
         _asPath = new AsPath();
         _communities = new CommunitySet();
         _clusterList = new TreeSet<>();
      }

      @Override
      public BgpRoute build() {
         return new BgpRoute(_network, _nextHopIp, _admin, _asPath,
               _communities, _localPreference, _metric, _originatorIp,
               _clusterList, _receivedFromRouteReflectorClient, _originType,
               _protocol, _srcProtocol, _weight);
      }

      public AsPath getAsPath() {
         return _asPath;
      }

      public Set<Long> getClusterList() {
         return _clusterList;
      }

      public CommunitySet getCommunities() {
         return _communities;
      }

      public int getLocalPreference() {
         return _localPreference;
      }

      public Ip getOriginatorIp() {
         return _originatorIp;
      }

      public OriginType getOriginType() {
         return _originType;
      }

      public RoutingProtocol getProtocol() {
         return _protocol;
      }

      public int getWeight() {
         return _weight;
      }

      public void setAsPath(AsPath asPath) {
         _asPath = asPath;
      }

      public void setClusterList(SortedSet<Long> clusterList) {
         _clusterList = clusterList;
      }

      public void setCommunities(CommunitySet communities) {
         _communities = communities;
      }

      public void setLocalPreference(int localPreference) {
         _localPreference = localPreference;
      }

      public void setOriginatorIp(Ip originatorIp) {
         _originatorIp = originatorIp;
      }

      public void setOriginType(OriginType originType) {
         _originType = originType;
      }

      public void setProtocol(RoutingProtocol protocol) {
         _protocol = protocol;
      }

      public void setReceivedFromRouteReflectorClient(
            boolean receivedFromRouteReflectorClient) {
         _receivedFromRouteReflectorClient = receivedFromRouteReflectorClient;
      }

      public void setSrcProtocol(RoutingProtocol srcProtocol) {
         _srcProtocol = srcProtocol;
      }

      public void setWeight(int weight) {
         _weight = weight;
      }

   }

   public static final int DEFAULT_LOCAL_PREFERENCE = 100;

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final int _admin;

   private final AsPath _asPath;

   private final SortedSet<Long> _clusterList;

   private final CommunitySet _communities;

   private final int _localPreference;

   private final int _med;

   private final Ip _originatorIp;

   private final OriginType _originType;

   private final RoutingProtocol _protocol;

   private final boolean _receivedFromRouteReflectorClient;

   private final RoutingProtocol _srcProtocol;

   private final int _weight;

   public BgpRoute(Prefix network, Ip nextHopIp, int admin, AsPath asPath,
         CommunitySet communities, int localPreference, int med,
         Ip originatorIp, SortedSet<Long> clusterList,
         boolean receivedFromRouteReflectorClient, OriginType originType,
         RoutingProtocol protocol, RoutingProtocol srcProtocol, int weight) {
      super(network, nextHopIp);
      _admin = admin;
      _asPath = asPath;
      _clusterList = clusterList;
      _communities = communities;
      _localPreference = localPreference;
      _med = med;
      _originatorIp = originatorIp;
      _originType = originType;
      _protocol = protocol;
      _receivedFromRouteReflectorClient = receivedFromRouteReflectorClient;
      _srcProtocol = srcProtocol;
      _weight = weight;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      BgpRoute other = (BgpRoute) obj;
      if (_admin != other._admin) {
         return false;
      }
      if (!_asPath.equals(other._asPath)) {
         return false;
      }
      if (!_clusterList.equals(other._clusterList)) {
         return false;
      }
      if (!_communities.equals(other._communities)) {
         return false;
      }
      if (_localPreference != other._localPreference) {
         return false;
      }
      if (_med != other._med) {
         return false;
      }
      if (!_network.equals(other._network)) {
         return false;
      }
      if (_nextHopIp == null) {
         if (other._nextHopIp != null) {
            return false;
         }
      }
      else if (!_nextHopIp.equals(other._nextHopIp)) {
         return false;
      }
      if (_originType != other._originType) {
         return false;
      }
      if (_originatorIp == null) {
         if (other._originatorIp != null) {
            return false;
         }
      }
      else if (!_originatorIp.equals(other._originatorIp)) {
         return false;
      }
      if (_protocol != other._protocol) {
         return false;
      }
      if (_weight != other._weight) {
         return false;
      }
      return true;
   }

   @Override
   public int getAdministrativeCost() {
      return _admin;
   }

   public AsPath getAsPath() {
      return _asPath;
   }

   public Set<Long> getClusterList() {
      return _clusterList;
   }

   public CommunitySet getCommunities() {
      return _communities;
   }

   public int getLocalPreference() {
      return _localPreference;
   }

   @Override
   public Integer getMetric() {
      return _med;
   }

   @Override
   public String getNextHopInterface() {
      return null;
   }

   public Ip getOriginatorIp() {
      return _originatorIp;
   }

   public OriginType getOriginType() {
      return _originType;
   }

   @Override
   public RoutingProtocol getProtocol() {
      return _protocol;
   }

   public boolean getReceivedFromRouteReflectorClient() {
      return _receivedFromRouteReflectorClient;
   }

   public RoutingProtocol getSrcProtocol() {
      return _srcProtocol;
   }

   @Override
   public int getTag() {
      return -1;
   }

   public int getWeight() {
      return _weight;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + _admin;
      result = prime * result + ((_asPath == null) ? 0 : _asPath.hashCode());
      result = prime * result + _clusterList.hashCode();
      result = prime * result
            + ((_communities == null) ? 0 : _communities.hashCode());
      result = prime * result + _localPreference;
      result = prime * result + _med;
      result = prime * result + _network.hashCode();
      result = prime * result
            + ((_nextHopIp == null) ? 0 : _nextHopIp.hashCode());
      result = prime * result
            + ((_originType == null) ? 0 : _originType.hashCode());
      result = prime * result
            + ((_originatorIp == null) ? 0 : _originatorIp.hashCode());
      result = prime * result
            + ((_protocol == null) ? 0 : _protocol.hashCode());
      result = prime * result + _weight;
      return result;
   }

}
