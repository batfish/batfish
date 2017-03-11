package org.batfish.representation.cisco;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import org.batfish.common.BatfishException;
import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.RoutingProtocol;

public class BgpProcess extends ComparableStructure<Integer> {

   private static final int DEFAULT_BGP_DEFAULT_METRIC = 0;

   private static final long serialVersionUID = 1L;

   private Map<String, NamedBgpPeerGroup> _afGroups;

   private Map<Prefix6, BgpAggregateIpv6Network> _aggregateIpv6Networks;

   private Map<Prefix, BgpAggregateIpv4Network> _aggregateNetworks;

   private Set<BgpPeerGroup> _allPeerGroups;

   private boolean _alwaysCompareMed;

   private Ip _clusterId;

   private boolean _defaultIpv4Activate;

   private boolean _defaultIpv6Activate;

   private Map<Prefix, DynamicIpBgpPeerGroup> _dynamicIpPeerGroups;

   private Map<Prefix6, DynamicIpv6BgpPeerGroup> _dynamicIpv6PeerGroups;

   private Map<Prefix, BgpNetwork> _ipNetworks;

   private Map<Ip, IpBgpPeerGroup> _ipPeerGroups;

   private Map<Prefix6, BgpNetwork6> _ipv6Networks;

   private Map<Ip6, Ipv6BgpPeerGroup> _ipv6PeerGroups;

   private MasterBgpPeerGroup _masterBgpPeerGroup;

   private Map<String, NamedBgpPeerGroup> _namedPeerGroups;

   private Map<String, NamedBgpPeerGroup> _peerSessions;

   private Map<RoutingProtocol, BgpRedistributionPolicy> _redistributionPolicies;

   private Ip _routerId;

   public BgpProcess(int procnum) {
      super(procnum);
      _afGroups = new HashMap<>();
      _aggregateNetworks = new HashMap<>();
      _aggregateIpv6Networks = new HashMap<>();
      _allPeerGroups = new HashSet<>();
      _defaultIpv4Activate = true;
      _dynamicIpPeerGroups = new HashMap<>();
      _dynamicIpv6PeerGroups = new HashMap<>();
      _namedPeerGroups = new HashMap<>();
      _ipNetworks = new LinkedHashMap<>();
      _ipPeerGroups = new HashMap<>();
      _ipv6Networks = new LinkedHashMap<>();
      _ipv6PeerGroups = new HashMap<>();
      _peerSessions = new HashMap<>();
      _redistributionPolicies = new EnumMap<>(RoutingProtocol.class);
      _masterBgpPeerGroup = new MasterBgpPeerGroup();
      _masterBgpPeerGroup.setDefaultMetric(DEFAULT_BGP_DEFAULT_METRIC);
   }

   public DynamicIpBgpPeerGroup addDynamicIpPeerGroup(Prefix prefix) {
      DynamicIpBgpPeerGroup pg = new DynamicIpBgpPeerGroup(prefix);
      _dynamicIpPeerGroups.put(prefix, pg);
      _allPeerGroups.add(pg);
      return pg;
   }

   public DynamicIpv6BgpPeerGroup addDynamicIpv6PeerGroup(Prefix6 prefix6) {
      DynamicIpv6BgpPeerGroup pg = new DynamicIpv6BgpPeerGroup(prefix6);
      _dynamicIpv6PeerGroups.put(prefix6, pg);
      _allPeerGroups.add(pg);
      return pg;
   }

   public void addIpPeerGroup(Ip ip) {
      IpBgpPeerGroup pg = new IpBgpPeerGroup(ip);
      if (_defaultIpv4Activate) {
         pg.setActive(true);
      }
      _ipPeerGroups.put(ip, pg);
      _allPeerGroups.add(pg);
   }

   public void addIpPeerGroupMember(Ip address, String namedPeerGroupName) {
      NamedBgpPeerGroup namedPeerGroup = _namedPeerGroups
            .get(namedPeerGroupName);
      if (namedPeerGroup != null) {
         namedPeerGroup.addNeighborIpAddress(address);
         IpBgpPeerGroup ipPeerGroup = _ipPeerGroups.get(address);
         if (ipPeerGroup == null) {
            addIpPeerGroup(address);
            ipPeerGroup = _ipPeerGroups.get(address);
         }
         ipPeerGroup.setGroupName(namedPeerGroupName);
      }
      else {
         throw new BatfishException(
               "Peer group: \"" + namedPeerGroupName + "\" does not exist!");
      }
   }

   public void addIpv6PeerGroup(Ip6 ip6) {
      Ipv6BgpPeerGroup pg = new Ipv6BgpPeerGroup(ip6);
      if (_defaultIpv6Activate) {
         pg.setActive(true);
      }
      _ipv6PeerGroups.put(ip6, pg);
      _allPeerGroups.add(pg);
   }

   public void addIpv6PeerGroupMember(Ip6 address, String namedPeerGroupName) {
      NamedBgpPeerGroup namedPeerGroup = _namedPeerGroups
            .get(namedPeerGroupName);
      if (namedPeerGroup != null) {
         namedPeerGroup.addNeighborIpv6Address(address);
         Ipv6BgpPeerGroup ipv6PeerGroup = _ipv6PeerGroups.get(address);
         if (ipv6PeerGroup == null) {
            addIpv6PeerGroup(address);
            ipv6PeerGroup = _ipv6PeerGroups.get(address);
         }
         ipv6PeerGroup.setGroupName(namedPeerGroupName);
      }
      else {
         throw new BatfishException(
               "Peer group: \"" + namedPeerGroupName + "\" does not exist!");
      }
   }

   public void addNamedPeerGroup(String name, int definitionLine) {
      NamedBgpPeerGroup pg = new NamedBgpPeerGroup(name, definitionLine);
      _namedPeerGroups.put(name, pg);
      _allPeerGroups.add(pg);
   }

   public void addPeerSession(String name, int definitionLine) {
      NamedBgpPeerGroup pg = new NamedBgpPeerGroup(name, definitionLine);
      _peerSessions.put(name, pg);
      _allPeerGroups.add(pg);
   }

   public Map<String, NamedBgpPeerGroup> getAfGroups() {
      return _afGroups;
   }

   public Map<Prefix6, BgpAggregateIpv6Network> getAggregateIpv6Networks() {
      return _aggregateIpv6Networks;
   }

   public Map<Prefix, BgpAggregateIpv4Network> getAggregateNetworks() {
      return _aggregateNetworks;
   }

   public Set<BgpPeerGroup> getAllPeerGroups() {
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

   public Map<Prefix, DynamicIpBgpPeerGroup> getDynamicIpPeerGroups() {
      return _dynamicIpPeerGroups;
   }

   public Map<Prefix6, DynamicIpv6BgpPeerGroup> getDynamicIpv6PeerGroups() {
      return _dynamicIpv6PeerGroups;
   }

   public Map<Prefix, BgpNetwork> getIpNetworks() {
      return _ipNetworks;
   }

   public Map<Ip, IpBgpPeerGroup> getIpPeerGroups() {
      return _ipPeerGroups;
   }

   public Map<Prefix6, BgpNetwork6> getIpv6Networks() {
      return _ipv6Networks;
   }

   public Map<Ip6, Ipv6BgpPeerGroup> getIpv6PeerGroups() {
      return _ipv6PeerGroups;
   }

   public MasterBgpPeerGroup getMasterBgpPeerGroup() {
      return _masterBgpPeerGroup;
   }

   public Map<String, NamedBgpPeerGroup> getNamedPeerGroups() {
      return _namedPeerGroups;
   }

   public Map<String, NamedBgpPeerGroup> getPeerSessions() {
      return _peerSessions;
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
