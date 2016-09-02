package org.batfish.representation.vyos;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.batfish.datamodel.Ip;
import org.batfish.datamodel.collections.RoleSet;
import org.batfish.representation.VendorConfiguration;

public abstract class VyosConfiguration extends VendorConfiguration {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private BgpProcess _bgpProcess;

   protected final Map<String, EspGroup> _espGroups;

   protected String _hostname;

   protected final Map<String, IkeGroup> _ikeGroups;

   protected final Map<String, Interface> _interfaces;

   protected final Set<String> _ipsecInterfaces;

   protected final Map<Ip, IpsecPeer> _ipsecPeers;

   protected final Map<String, PrefixList> _prefixLists;

   protected final RoleSet _roles;

   protected final Map<String, RouteMap> _routeMaps;

   protected final Set<StaticNextHopRoute> _staticNextHopRoutes;

   public VyosConfiguration() {
      _roles = new RoleSet();
      _espGroups = new TreeMap<>();
      _ikeGroups = new TreeMap<>();
      _interfaces = new TreeMap<>();
      _ipsecInterfaces = new TreeSet<>();
      _ipsecPeers = new TreeMap<>();
      _prefixLists = new TreeMap<>();
      _routeMaps = new TreeMap<>();
      _staticNextHopRoutes = new HashSet<>();
   }

   public BgpProcess getBgpProcess() {
      return _bgpProcess;
   }

   public Map<String, EspGroup> getEspGroups() {
      return _espGroups;
   }

   @Override
   public String getHostname() {
      return _hostname;
   }

   public Map<String, IkeGroup> getIkeGroups() {
      return _ikeGroups;
   }

   public Map<String, Interface> getInterfaces() {
      return _interfaces;
   }

   public Set<String> getIpsecInterfaces() {
      return _ipsecInterfaces;
   }

   public Map<Ip, IpsecPeer> getIpsecPeers() {
      return _ipsecPeers;
   }

   public Map<String, PrefixList> getPrefixLists() {
      return _prefixLists;
   }

   @Override
   public RoleSet getRoles() {
      return _roles;
   }

   public Map<String, RouteMap> getRouteMaps() {
      return _routeMaps;
   }

   public Set<StaticNextHopRoute> getStaticNextHopRoutes() {
      return _staticNextHopRoutes;
   }

   public void setBgpProcess(BgpProcess bgpProcess) {
      _bgpProcess = bgpProcess;
   }

   @Override
   public void setHostname(String hostname) {
      _hostname = hostname;
   }

   @Override
   public void setRoles(RoleSet roles) {
      _roles.addAll(roles);
   }

}
