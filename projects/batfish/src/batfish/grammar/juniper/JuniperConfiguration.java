package batfish.grammar.juniper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import batfish.grammar.juniper.firewall.FireWallStanza;
import batfish.grammar.juniper.interfaces.InterfacesStanza;
import batfish.grammar.juniper.policy_options.PolicyOptionsStanza;
import batfish.grammar.juniper.routing_options.RoutingOptionsStanza;
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
         FireWallStanza fs = (FireWallStanza) js;
         for (ExtendedAccessList eal : fs.getFilters()) {
            _configuration.addExtendedAccessList(eal);
         }
         break;

      case INTERFACES:
         InterfacesStanza is = (InterfacesStanza) js;
         for (Interface inter : is.getInterfaceList()) {
            _configuration.addInterface(inter);
            _interfaceAddressMap.put(inter.getName(), inter.getIP() + "/"
                  + inter.getSubnetMask());
         }
         break;

      case NULL:
         break;

      case POLICY_OPTIONS:
         PolicyOptionsStanza pos = (PolicyOptionsStanza) js;
         _configuration.addPolicyStatements(pos.getMaps());
         _configuration.addRouteFilters(pos.getFilters());
         _configuration.addExpandedCommunityLists(pos.getCommunities());
         _configuration.addAsPathAccessLists(pos.getAsPathLists());
         break;

      case PROTOCOLS:
         ProtocolsStanza ps = (ProtocolsStanza) js;

         // OSPF Information
         OSPFProcess ospf = new OSPFProcess(0);
         if (_routerID != null) {
            ospf.setRouterId(_routerID);
         }
         HashMap<Integer, ArrayList<String>> ospfAreaMap = ps.getOSPFAreaMap();
         if (ospfAreaMap != null) {
            Set<Integer> arealist = ospfAreaMap.keySet();
            for (Integer a : arealist) {
               ArrayList<String> iflist = ospfAreaMap.get(a);
               for (String ifname : iflist) {
                  /*
                  String ipsub = _interfaceAddressMap.get(ifname);
                  System.out.println(ifname +" : "+ipsub);
                  if (ipsub != null) {
                     String[] iptmp = ipsub.split("/");
                     ospf.addNetwork(iptmp[0], iptmp[1], a.intValue());
                  }
                  else {
                     // TODO: check if the config bd02f2.lab:850 is wrong
                     System.out.println("Interface not found: " + ifname);
                  }  
                  */                
                  ospf.addNetworkByInterface(ifname, a.intValue());
               }
            }
            if (ps.getReferenceBandwidth() < 0) {
               ospf.setReferenceBandwidth(DEFAULT_REFERENCE_BANDWIDTH);
            }
            else {
               ospf.setReferenceBandwidth(ps.getReferenceBandwidth());
            }
            ospf.addExportPolicyStatements(ps.getOSPFExports());
            _configuration.addOSPFProcess(ospf);
         }

         // BGP Information
         BGPProcess bgp = new BGPProcess(_asNum);
         if (_routerID != null) {
            bgp.setRouterID(_routerID);
         }
         if (ps.getGroupList() != null) {
            for (BGPGroup g : ps.getGroupList()) {
               bgp.addPeerGroup(g);
            }
            bgp.addActivatedNeighbors(ps.getActivatedNeighbor());
            _configuration.addBGPProcess(bgp);
         }

         break;

      case ROUTING_OPTIONS:
         RoutingOptionsStanza ros = (RoutingOptionsStanza) js;
         _configuration.addStaticRoutes(ros.getStaticRoutes());
         _configuration.addGenerateRoutes(ros.getGenerateRoutes());
         _asNum = ros.getASNum();
         List<BGPProcess> tmpBgp = _configuration.getBGPProcesses();
         if (!(tmpBgp.isEmpty())) {
            for (BGPProcess b : tmpBgp) {
               b.setAsNum(_asNum);
            }
         }
         _routerID = ros.getRouterID();
         List<OSPFProcess> tmpOspf = _configuration.getOSPFProcesses();
         if (!(tmpOspf.isEmpty())) {
            for (OSPFProcess o : tmpOspf) {
               o.setRouterId(_routerID);
            }
         }
         break;

      case SYSTEM:
         SystemStanza ss = (SystemStanza) js;
         _configuration.setHostname(ss.getHostName());
         break;

      default:
         System.out.println("bad jstanza type");
         break;
      }

   }

   public JuniperVendorConfiguration getConfiguration() {
      return _configuration;
   }

}
