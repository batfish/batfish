package org.batfish.datamodel;

import org.batfish.common.Pair;
import org.batfish.common.util.ComparableStructure;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OspfNeighbor extends ComparableStructure<Pair<Ip, Ip>> {

   public static final class OspfNeighborSummary
         extends ComparableStructure<String> {

      private static final String LOCAL_IP_VAR = "localIp";

      private static final String REMOTE_IP_VAR = "remoteIp";

      /**
       *
       */
      private static final long serialVersionUID = 1L;

      private static final String VRF_VAR = "vrf";

      private final Ip _localIp;

      private final Ip _remoteIp;

      private final String _vrf;

      public OspfNeighborSummary(OspfNeighbor ospfNeighbor) {
         super(ospfNeighbor.getOwner().getName() + ":"
               + ospfNeighbor._key.toString());
         _localIp = ospfNeighbor._key.getFirst();
         _remoteIp = ospfNeighbor._key.getSecond();
         _vrf = ospfNeighbor._vrf;
      }

      @JsonCreator
      public OspfNeighborSummary(@JsonProperty(NAME_VAR) String name,
            @JsonProperty(LOCAL_IP_VAR) Ip localIp,
            @JsonProperty(REMOTE_IP_VAR) Ip remoteIp,
            @JsonProperty(VRF_VAR) String vrf) {
         super(name);
         _localIp = localIp;
         _remoteIp = remoteIp;
         _vrf = vrf;
      }

      @JsonProperty(LOCAL_IP_VAR)
      public Ip getLocalIp() {
         return _localIp;
      }

      @JsonProperty(REMOTE_IP_VAR)
      public Ip getRemoteIp() {
         return _remoteIp;
      }

      @JsonProperty(VRF_VAR)
      public String getVrf() {
         return _vrf;
      }

   }

   /**
   *
   */
   private static final long serialVersionUID = 1L;

   private long _area;

   private Interface _iface;

   private Configuration _owner;

   private transient OspfNeighbor _remoteOspfNeighbor;

   private String _vrf;

   public OspfNeighbor(Pair<Ip, Ip> ipEdge) {
      super(ipEdge);
   }

   public long getArea() {
      return _area;
   }

   public Interface getIface() {
      return _iface;
   }

   @JsonIgnore
   public Ip getLocalIp() {
      return _key.getFirst();
   }

   public Configuration getOwner() {
      return _owner;
   }

   @JsonIgnore
   public Ip getRemoteIp() {
      return _key.getSecond();
   }

   public OspfNeighbor getRemoteOspfNeighbor() {
      return _remoteOspfNeighbor;
   }

   public String getVrf() {
      return _vrf;
   }

   public void setArea(long area) {
      _area = area;
   }

   public void setIface(Interface iface) {
      _iface = iface;
   }

   public void setInterface(Interface iface) {
      _iface = iface;
   }

   public void setOwner(Configuration owner) {
      _owner = owner;
   }

   public void setRemoteOspfNeighbor(OspfNeighbor remoteOspfNeighbor) {
      _remoteOspfNeighbor = remoteOspfNeighbor;
   }

   public void setVrf(String vrf) {
      _vrf = vrf;
   }

}
