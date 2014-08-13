package batfish.grammar.juniper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import batfish.grammar.juniper.firewall.FireWallStanza;
import batfish.grammar.juniper.interfaces.InterfacesStanza;
import batfish.grammar.juniper.policy_options.PolicyOptionsStanza;
import batfish.grammar.juniper.routing_options.RoutingOptionsStanza;
import batfish.grammar.juniper.protocols.ProtocolsStanza;
import batfish.grammar.juniper.system.SystemStanza;
import batfish.representation.juniper.BGPGroup;
import batfish.representation.juniper.BGPProcess;
import batfish.representation.juniper.JuniperVendorConfiguration;
import batfish.representation.juniper.ExtendedAccessList;
import batfish.representation.juniper.Interface;
import batfish.representation.juniper.OSPFProcess; 

public class JuniperConfiguration {
   private JuniperVendorConfiguration _configuration;
   private int _asNum;
   private String _routerID;
   private HashMap<String, String> _interfaceAddressMap;

   private static final double DEFAULT_REFERENCE_BANDWIDTH = 1E9; // bits per

   // second

   public JuniperConfiguration() {
      _configuration = new JuniperVendorConfiguration();
      _interfaceAddressMap = new HashMap<String, String>();
      _asNum = -1;
   }

   public String getRouterID() {
      return _routerID;
   }

   public void processStanza(JStanza js) {
      switch (js.getType()) {
      case FIREWALL:
        /* FireWallStanza fs = (FireWallStanza) js;
         for (ExtendedAccessList eal : fs.getFilters()) {
            _configuration.addExtendedAccessList(eal);
         }*/
         break;

      case INTERFACES:
         InterfacesStanza is = (InterfacesStanza) js;
         for (Interface inter : is.get_interfaces()) {
            //_configuration.addInterface(inter);
            _interfaceAddressMap.put(inter.get_name(), inter.get_ip() + "/"
                  + inter.get_subnet());
         }
         break;

      case NULL:
         break;

      case POLICY_OPTIONS:
         PolicyOptionsStanza pos = (PolicyOptionsStanza) js;
         _configuration.addPolicyStatements(pos.get_policyStatements());
         /*configuration.addRouteFilters(pos.get-());*/ // TODO [P0]
         _configuration.addCommunities(pos.get_communities());
         _configuration.addAsPathAccessLists(pos.get_asPathLists());
         break;

      case PROTOCOLS:
         ProtocolsStanza ps = (ProtocolsStanza) js;

         // OSPF Information
         OSPFProcess ospf = new OSPFProcess(0);
         if (_routerID != null) {
            ospf.set_routerId(_routerID);
         }
         
         HashMap<Integer, List<String>> ospfAreaMap = ps.get_ospfAreaMap();
         if (ospfAreaMap != null) {
            Set<Integer> arealist = ospfAreaMap.keySet();
            for (Integer a : arealist) {
               List<String> iflist = ospfAreaMap.get(a);
               for (String ifname : iflist) {
                  
                  String ipsub = _interfaceAddressMap.get(ifname);
                  System.out.println(ifname +" : "+ipsub);
                  if (ipsub != null) {
                     String[] iptmp = ipsub.split("/");
                     ospf.addNetwork(iptmp[0], iptmp[1], a.intValue());
                  }
                  else {
                     System.out.println("Interface not found: " + ifname);
                  }  
                              
                  ospf.addNetworkByInterface(ifname, a.intValue());
               }
            }
            if (ps.get_ospfReferenceBandwidth() < 0) {
               ospf.set_referenceBandwidth(DEFAULT_REFERENCE_BANDWIDTH);
            }
            else {
               ospf.set_referenceBandwidth(ps.get_ospfReferenceBandwidth());
            }
            ospf.addExportPolicyStatements(ps.get_ospfExports());
            _configuration.addOSPFProcess(ospf);
            
         }

         // BGP Information
         BGPProcess bgp = new BGPProcess(_asNum);
         if (_routerID != null) {
            bgp.setRouterID(_routerID);
         }
         if (ps.get_groupList() != null) {
        	 for (BGPGroup g : ps.get_groupList()) {
        		 bgp.addPeerGroup(g);
            }	
            bgp.addActivatedNeighbors(ps.get_activatedNeighbors());
            _configuration.addBGPProcess(bgp);
         }

         break;

      case ROUTING_OPTIONS:
         RoutingOptionsStanza ros = (RoutingOptionsStanza) js;
         _configuration.addStaticRoutes(ros.get_staticRoutes());
         // TODO [Ask Ari] : _configuration.addGenerateRoutes(ros.getGenerateRoutes());
         _asNum = ros.get_asNum();
         List<BGPProcess> tmpBgp = _configuration.getBGPProcesses();
         if (!(tmpBgp.isEmpty())) {
            for (BGPProcess b : tmpBgp) {
               b.setAsNum(_asNum);
            }
         }
         _routerID = ros.get_routerId();
         List<OSPFProcess> ospfProcs = _configuration.getOSPFProcesses();
         if (!(ospfProcs.isEmpty())) {
            for (OSPFProcess o : ospfProcs) {
               o.set_routerId(_routerID);
            }
         }
         // TODO [Ask Ari]: _ribGRoups never gets used?
         break;

      case SYSTEM:
         SystemStanza ss = (SystemStanza) js;
         _configuration.setHostname(ss.get_hostName());
         break;

      default:
         throw new Error("bad jstanza type");
      }
   }

   public JuniperVendorConfiguration getConfiguration() {
      return _configuration;
   }

}
