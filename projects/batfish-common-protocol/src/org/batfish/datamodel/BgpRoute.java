package org.batfish.datamodel;

import org.batfish.datamodel.collections.CommunitySet;

public class BgpRoute extends AbstractRoute {

   public static class Builder extends AbstractRouteBuilder<BgpRoute> {

      private AsPath _asPath;

      private CommunitySet _communities;

      private int _localPreference;

      private Ip _originatorIp;

      private OriginType _originType;

      private RoutingProtocol _protocol;

      private RoutingProtocol _srcProtocol;

      public Builder() {
         _asPath = new AsPath();
         _communities = new CommunitySet();
      }

      @Override
      public BgpRoute build() {
         return new BgpRoute(_network, _nextHopIp, _admin, _asPath,
               _communities, _localPreference, _metric, _originatorIp,
               _originType, _protocol, _srcProtocol);
      }

      public AsPath getAsPath() {
         return _asPath;
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

      public void setAsPath(AsPath asPath) {
         _asPath = asPath;
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

      public void setSrcProtocol(RoutingProtocol srcProtocol) {
         _srcProtocol = srcProtocol;
      }

   }

   public static final int DEFAULT_LOCAL_PREFERENCE = 100;

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final int _admin;

   private final AsPath _asPath;

   private final CommunitySet _communities;

   private final int _localPreference;

   private final int _med;

   private final Ip _originatorIp;

   private final OriginType _originType;

   private final RoutingProtocol _protocol;

   private final RoutingProtocol _srcProtocol;

   public BgpRoute(Prefix network, Ip nextHopIp, int admin, AsPath asPath,
         CommunitySet communities, int localPreference, int med,
         Ip originatorIp, OriginType originType, RoutingProtocol protocol,
         RoutingProtocol srcProtocol) {
      super(network, nextHopIp);
      _admin = admin;
      _asPath = asPath;
      _communities = communities;
      _localPreference = localPreference;
      _med = med;
      _originatorIp = originatorIp;
      _originType = originType;
      _protocol = protocol;
      _srcProtocol = srcProtocol;
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
      if (!_communities.equals(other._communities)) {
         return false;
      }
      if (_localPreference != other._localPreference) {
         return false;
      }
      if (_med != other._med) {
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
      return true;
   }

   @Override
   public int getAdministrativeCost() {
      return _admin;
   }

   public AsPath getAsPath() {
      return _asPath;
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

   public RoutingProtocol getSrcProtocol() {
      return _srcProtocol;
   }

   @Override
   public int getTag() {
      return -1;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + _admin;
      result = prime * result + ((_asPath == null) ? 0 : _asPath.hashCode());
      result = prime * result
            + ((_communities == null) ? 0 : _communities.hashCode());
      result = prime * result + _localPreference;
      result = prime * result + _med;
      result = prime * result
            + ((_originType == null) ? 0 : _originType.hashCode());
      result = prime * result
            + ((_originatorIp == null) ? 0 : _originatorIp.hashCode());
      result = prime * result
            + ((_protocol == null) ? 0 : _protocol.hashCode());
      return result;
   }

}
