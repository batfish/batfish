package org.batfish.representation.vyos;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.batfish.collections.RoleSet;

public class VyosConfiguration implements Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private BgpProcess _bgpProcess;

   protected String _hostname;

   protected final Map<String, Interface> _interfaces;

   protected final Map<String, PrefixList> _prefixLists;

   protected final RoleSet _roles;

   protected final Map<String, RouteMap> _routeMaps;

   protected final Set<StaticNextHopRoute> _staticNextHopRoutes;

   public VyosConfiguration() {
      _roles = new RoleSet();
      _interfaces = new TreeMap<String, Interface>();
      _prefixLists = new TreeMap<String, PrefixList>();
      _routeMaps = new TreeMap<String, RouteMap>();
      _staticNextHopRoutes = new HashSet<StaticNextHopRoute>();
   }

   public BgpProcess getBgpProcess() {
      return _bgpProcess;
   }

   public String getHostname() {
      return _hostname;
   }

   public Map<String, Interface> getInterfaces() {
      return _interfaces;
   }

   public Map<String, PrefixList> getPrefixLists() {
      return _prefixLists;
   }

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

   public void setHostname(String hostname) {
      _hostname = hostname;
   }

   public void setRoles(RoleSet roles) {
      _roles.addAll(roles);
   }

}
