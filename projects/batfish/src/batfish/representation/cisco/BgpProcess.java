package batfish.representation.cisco;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import batfish.main.BatfishException;
import batfish.representation.Ip;
import batfish.representation.RoutingProtocol;

public class BgpProcess implements Serializable {

   private static final int DEFAULT_BGP_DEFAULT_METRIC = 0;
   private static final long serialVersionUID = 1L;

   private Map<BgpNetwork, Boolean> _aggregateNetworks;
   private Map<String, BgpPeerGroup> _allPeerGroups;
   private boolean _alwaysCompareMed;
   private Ip _clusterId;
   private Map<String, DynamicBgpPeerGroup> _dynamicPeerGroups;
   private Map<Ip, IpBgpPeerGroup> _ipPeerGroups;
   private MasterBgpPeerGroup _masterBgpPeerGroup;
   private Map<String, NamedBgpPeerGroup> _namedPeerGroups;
   private Set<BgpNetwork> _networks;
   private int _pid;
   private Map<RoutingProtocol, BgpRedistributionPolicy> _redistributionPolicies;
   private Ip _routerId;
   private boolean _defaultIpv4Activate;

   public BgpProcess(int procnum) {
      _pid = procnum;
      _allPeerGroups = new HashMap<String, BgpPeerGroup>();
      _defaultIpv4Activate = true;
      _dynamicPeerGroups = new HashMap<String, DynamicBgpPeerGroup>();
      _namedPeerGroups = new HashMap<String, NamedBgpPeerGroup>();
      _ipPeerGroups = new HashMap<Ip, IpBgpPeerGroup>();
      _networks = new LinkedHashSet<BgpNetwork>();
      _aggregateNetworks = new HashMap<BgpNetwork, Boolean>();
      _redistributionPolicies = new EnumMap<RoutingProtocol, BgpRedistributionPolicy>(
            RoutingProtocol.class);
      _masterBgpPeerGroup = new MasterBgpPeerGroup();
      _masterBgpPeerGroup.setDefaultMetric(DEFAULT_BGP_DEFAULT_METRIC);
   }

   public DynamicBgpPeerGroup addDynamicPeerGroup(Ip ip, int prefixLength,
         String name) {
      DynamicBgpPeerGroup pg = new DynamicBgpPeerGroup(ip, prefixLength, name);
      _dynamicPeerGroups.put(name, pg);
      _allPeerGroups.put(name, pg);
      return pg;
   }

   public void addIpPeerGroup(Ip ip) {
      IpBgpPeerGroup pg = new IpBgpPeerGroup(ip);
      if (_defaultIpv4Activate) {
         pg.setActive(true);
      }
      _ipPeerGroups.put(ip, pg);
      _allPeerGroups.put(ip.toString(), pg);
   }

   public void addNamedPeerGroup(String name) {
      NamedBgpPeerGroup pg = new NamedBgpPeerGroup(name);
      _namedPeerGroups.put(name, pg);
      _allPeerGroups.put(name, pg);
   }

   public void addPeerGroupMember(Ip address, String namedPeerGroupName) {
      NamedBgpPeerGroup namedPeerGroup = _namedPeerGroups
            .get(namedPeerGroupName);
      if (namedPeerGroup != null) {
         namedPeerGroup.addNeighborAddress(address);
         IpBgpPeerGroup ipPeerGroup = _ipPeerGroups.get(address);
         if (ipPeerGroup == null) {
            addIpPeerGroup(address);
            ipPeerGroup = _ipPeerGroups.get(address);
         }
         ipPeerGroup.setGroupName(namedPeerGroupName);
      }
      else {
         throw new BatfishException("Peer group: \"" + namedPeerGroupName
               + "\" does not exist!");
      }
   }

   public Map<BgpNetwork, Boolean> getAggregateNetworks() {
      return _aggregateNetworks;
   }

   public Map<String, BgpPeerGroup> getAllPeerGroups() {
      return _allPeerGroups;
   }

   public boolean getAlwaysCompareMed() {
      return _alwaysCompareMed;
   }

   public Ip getClusterId() {
      return _clusterId;
   }

   public int getDefaultMetric() {
      return _masterBgpPeerGroup.getDefaultMetric();
   }

   public Map<String, DynamicBgpPeerGroup> getDynamicPeerGroups() {
      return _dynamicPeerGroups;
   }

   public Map<Ip, IpBgpPeerGroup> getIpPeerGroups() {
      return _ipPeerGroups;
   }

   public MasterBgpPeerGroup getMasterBgpPeerGroup() {
      return _masterBgpPeerGroup;
   }

   public Map<String, NamedBgpPeerGroup> getNamedPeerGroups() {
      return _namedPeerGroups;
   }

   public Set<BgpNetwork> getNetworks() {
      return _networks;
   }

   public BgpPeerGroup getPeerGroup(String name) {
      return _allPeerGroups.get(name);
   }

   public int getPid() {
      return _pid;
   }

   public Map<RoutingProtocol, BgpRedistributionPolicy> getRedistributionPolicies() {
      return _redistributionPolicies;
   }

   public Ip getRouterId() {
      return _routerId;
   }

   public void setAlwaysCompareMed(boolean b) {
      _alwaysCompareMed = b;
   }

   public void setClusterId(Ip clusterId) {
      _clusterId = clusterId;
   }

   public void setRouterId(Ip routerId) {
      _routerId = routerId;
   }

}
