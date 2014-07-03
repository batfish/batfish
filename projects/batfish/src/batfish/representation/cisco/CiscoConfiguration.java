package batfish.representation.cisco;

import java.util.HashMap;
import java.util.Map;

import batfish.grammar.cisco.CiscoGrammar.Cisco_configurationContext;
import batfish.grammar.cisco.CiscoGrammar.Router_bgp_stanzaContext;
import batfish.grammar.cisco.CiscoGrammar.Router_ospf_stanzaContext;

public class CiscoConfiguration {
   protected final Map<String, IpAsPathAccessList> _asPathAccessLists;
   protected BgpProcess _bgpProcess;
   protected Router_bgp_stanzaContext _bgpProcessContext;
   protected Cisco_configurationContext _context;
   protected final Map<String, ExpandedCommunityList> _expandedCommunityLists;
   protected final Map<String, ExtendedAccessList> _extendedAccessLists;
   protected String _hostname;
   protected final Map<String, Interface> _interfaces;
   protected OspfProcess _ospfProcess;
   protected Router_ospf_stanzaContext _ospfProcessContext;
   protected final Map<String, PrefixList> _prefixLists;
   protected final Map<String, RouteMap> _routeMaps;
   protected final Map<String, StandardAccessList> _standardAccessLists;
   protected final Map<String, StandardCommunityList> _standardCommunityLists;
   protected final Map<String, StaticRoute> _staticRoutes;

   public CiscoConfiguration() {
      _asPathAccessLists = new HashMap<String, IpAsPathAccessList>();
      _expandedCommunityLists = new HashMap<String, ExpandedCommunityList>();
      _extendedAccessLists = new HashMap<String, ExtendedAccessList>();
      _interfaces = new HashMap<String, Interface>();
      _prefixLists = new HashMap<String, PrefixList>();
      _routeMaps = new HashMap<String, RouteMap>();
      _standardAccessLists = new HashMap<String, StandardAccessList>();
      _standardCommunityLists = new HashMap<String, StandardCommunityList>();
      _staticRoutes = new HashMap<String, StaticRoute>();
   }

   public Map<String, IpAsPathAccessList> getAsPathAccessLists() {
      return _asPathAccessLists;
   }

   public final BgpProcess getBgpProcess() {
      return _bgpProcess;
   }

   public final Router_bgp_stanzaContext getBgpProcessContext() {
      return _bgpProcessContext;
   }

   public final Cisco_configurationContext getContext() {
      return _context;
   }

   public final Map<String, ExpandedCommunityList> getExpandedCommunityLists() {
      return _expandedCommunityLists;
   }

   public final Map<String, ExtendedAccessList> getExtendedAcls() {
      return _extendedAccessLists;
   }

   public final String getHostname() {
      return _hostname;
   }

   public final Map<String, Interface> getInterfaces() {
      return _interfaces;
   }

   public final OspfProcess getOspfProcess() {
      return _ospfProcess;
   }

   public final Router_ospf_stanzaContext getOspfProcessContext() {
      return _ospfProcessContext;
   }

   public final Map<String, PrefixList> getPrefixLists() {
      return _prefixLists;
   }

   public final Map<String, RouteMap> getRouteMaps() {
      return _routeMaps;
   }

   public final Map<String, StandardAccessList> getStandardAcls() {
      return _standardAccessLists;
   }

   public final Map<String, StandardCommunityList> getStandardCommunityLists() {
      return _standardCommunityLists;
   }

   public final Map<String, StaticRoute> getStaticRoutes() {
      return _staticRoutes;
   }

   public final void setBgpProcess(BgpProcess bgpProcess,
         Router_bgp_stanzaContext context) {
      _bgpProcess = bgpProcess;
      _bgpProcessContext = context;
   }

   public final void setContext(Cisco_configurationContext ctx) {
      _context = ctx;
   }

   public final void setHostname(String hostname) {
      _hostname = hostname;
   }

   public final void setOspfProcess(OspfProcess proc, Router_ospf_stanzaContext ctx) {
      _ospfProcess = proc;
      _ospfProcessContext = ctx;
   }

}
