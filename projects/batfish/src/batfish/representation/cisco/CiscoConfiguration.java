package batfish.representation.cisco;

import java.util.HashMap;
import java.util.Map;

import batfish.grammar.cisco.CiscoGrammar.Cisco_configurationContext;
import batfish.grammar.cisco.CiscoGrammar.Router_bgp_stanzaContext;
import batfish.grammar.cisco.CiscoGrammar.Router_ospf_stanzaContext;

public class CiscoConfiguration {
   private Map<String, IpAsPathAccessList> _asPathAccessLists;
   private BgpProcess _bgpProcess;
   private Router_bgp_stanzaContext _bgpProcessContext;
   private Cisco_configurationContext _context;
   private Map<String, ExtendedAccessList> _extendedAcls;
   private String _hostname;
   private Map<String, Interface> _interfaces;
   private OspfProcess _ospfProcess;
   private Router_ospf_stanzaContext _ospfProcessContext;
   private Map<String, StandardAccessList> _standardAcls;

   public CiscoConfiguration() {
      _interfaces = new HashMap<String, Interface>();
      _standardAcls = new HashMap<String, StandardAccessList>();
      _extendedAcls = new HashMap<String, ExtendedAccessList>();
   }

   public void addAsPathAccessListLine(String name, IpAsPathAccessListLine line) {
      IpAsPathAccessList list = _asPathAccessLists.get(name);
      if (list == null) {
         list = new IpAsPathAccessList(name);
         _asPathAccessLists.put(name, list);
      }
      list.addLine(line);
   }

   public Map<String, IpAsPathAccessList> getAsPathAccessLists() {
      return _asPathAccessLists;
   }

   public BgpProcess getBgpProcess() {
      return _bgpProcess;
   }

   public Router_bgp_stanzaContext getBgpProcessContext() {
      return _bgpProcessContext;
   }

   public Cisco_configurationContext getContext() {
      return _context;
   }

   public Map<String, ExtendedAccessList> getExtendedAcls() {
      return _extendedAcls;
   }

   public String getHostname() {
      return _hostname;
   }

   public Map<String, Interface> getInterfaces() {
      return _interfaces;
   }

   public OspfProcess getOspfProcess() {
      return _ospfProcess;
   }

   public Router_ospf_stanzaContext getOspfProcessContext() {
      return _ospfProcessContext;
   }

   public Map<String, StandardAccessList> getStandardAcls() {
      return _standardAcls;
   }

   public void setBgpProcess(BgpProcess bgpProcess,
         Router_bgp_stanzaContext context) {
      _bgpProcess = bgpProcess;
      _bgpProcessContext = context;
   }

   public void setContext(Cisco_configurationContext ctx) {
      _context = ctx;
   }

   public void setHostname(String hostname) {
      _hostname = hostname;
   }

   public void setOspfProcess(OspfProcess proc, Router_ospf_stanzaContext ctx) {
      _ospfProcess = proc;
      _ospfProcessContext = ctx;
   }

}
