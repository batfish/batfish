package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import org.batfish.main.BatfishException;
import org.batfish.representation.Ip;
import org.batfish.representation.Prefix;
import org.batfish.representation.RoutingProtocol;

public class BgpProcess implements Serializable {

   private static final int DEFAULT_BGP_DEFAULT_METRIC = 0;
   private static final long serialVersionUID = 1L;

   private Map<Prefix, BgpAggregateNetwork> _aggregateNetworks;
   private Set<BgpPeerGroup> _allPeerGroups;
   private boolean _alwaysCompareMed;
   private Ip _clusterId;
   private boolean _defaultIpv4Activate;
   private Map<Prefix, DynamicBgpPeerGroup> _dynamicPeerGroups;
   private Map<Ip, IpBgpPeerGroup> _ipPeerGroups;
   private MasterBgpPeerGroup _masterBgpPeerGroup;
   private Map<String, NamedBgpPeerGroup> _namedPeerGroups;
   private Set<Prefix> _networks;
   private Map<String, NamedBgpPeerGroup> _peerSessions;
   private int _pid;
   private Map<RoutingProtocol, BgpRedistributionPolicy> _redistributionPolicies;
   private Ip _routerId;

   public BgpProcess(int procnum) {
      _pid = procnum;
      _allPeerGroups = new HashSet<BgpPeerGroup>();
      _defaultIpv4Activate = true;
      _dynamicPeerGroups = new HashMap<Prefix, DynamicBgpPeerGroup>();
      _namedPeerGroups = new HashMap<String, NamedBgpPeerGroup>();
      _ipPeerGroups = new HashMap<Ip, IpBgpPeerGroup>();
      _networks = new LinkedHashSet<Prefix>();
      _aggregateNetworks = new HashMap<Prefix, BgpAggregateNetwork>();
      _peerSessions = new HashMap<String, NamedBgpPeerGroup>();
      _redistributionPolicies = new EnumMap<RoutingProtocol, BgpRedistributionPolicy>(
            RoutingProtocol.class);
      _masterBgpPeerGroup = new MasterBgpPeerGroup();
      _masterBgpPeerGroup.setDefaultMetric(DEFAULT_BGP_DEFAULT_METRIC);
   }

   public DynamicBgpPeerGroup addDynamicPeerGroup(Prefix prefix) {
      DynamicBgpPeerGroup pg = new DynamicBgpPeerGroup(prefix);
      _dynamicPeerGroups.put(prefix, pg);
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

   public void addNamedPeerGroup(String name) {
      NamedBgpPeerGroup pg = new NamedBgpPeerGroup(name);
      _namedPeerGroups.put(name, pg);
      _allPeerGroups.add(pg);
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

   public void addPeerSession(String name) {
      NamedBgpPeerGroup pg = new NamedBgpPeerGroup(name);
      _peerSessions.put(name, pg);
      _allPeerGroups.add(pg);
   }

   public Map<Prefix, BgpAggregateNetwork> getAggregateNetworks() {
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

   public Map<Prefix, DynamicBgpPeerGroup> getDynamicPeerGroups() {
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

   public Set<Prefix> getNetworks() {
      return _networks;
   }

   public Map<String, NamedBgpPeerGroup> getPeerSessions() {
      return _peerSessions;
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
