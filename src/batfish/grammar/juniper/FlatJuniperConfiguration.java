package batfish.grammar.juniper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import batfish.grammar.juniper.firewall.FlatFireWallStanza;
import batfish.grammar.juniper.interfaces.FlatInterfacesStanza;
import batfish.grammar.juniper.policy_options.FlatPolicyOptionsStanza;
import batfish.grammar.juniper.routing_options.FlatRoutingOptionsStanza;
import batfish.representation.juniper.BGPGroup;
import batfish.representation.juniper.BGPNeighbor;
import batfish.representation.juniper.BGPProcess;
import batfish.representation.juniper.ExtendedAccessListTerm;
import batfish.representation.juniper.GenerateRoute;
import batfish.representation.juniper.JuniperVendorConfiguration;
import batfish.representation.juniper.ExtendedAccessList;
import batfish.representation.juniper.Interface;
import batfish.representation.juniper.OSPFProcess;
import batfish.representation.juniper.PolicyStatement;
import batfish.representation.juniper.PolicyStatementClause;
import batfish.representation.juniper.RouteFilter;
import batfish.util.SubRange;

public class FlatJuniperConfiguration {
   private JuniperVendorConfiguration _configuration;
   private int _asNum;
   private String _routerID;
   private HashMap<String, String> _interfaceAddressMap;

   private static final double DEFAULT_REFERENCE_BANDWIDTH = 1E9; // bits per

   // second

   public FlatJuniperConfiguration() {
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
         FlatFireWallStanza fs = (FlatFireWallStanza) js;
         processFirewallStanza(fs);
         break;

      case INTERFACES:
         FlatInterfacesStanza is = (FlatInterfacesStanza) js;
         processInterfacesStanza(is);
         break;

      case NULL:
         break;

      case POLICY_OPTIONS:
         FlatPolicyOptionsStanza pos = (FlatPolicyOptionsStanza) js;
         processPolicyOptionsStanza(pos);
         break;

      case PROTOCOLS:
         FlatProtocolsStanza ps = (FlatProtocolsStanza) js;
         //System.out.println(ps.getType1());
         switch (ps.getType1()) {
         case BGP:
            processBGPStanza(ps);
            break;

         case NULL:
            break;

         case OSPF:
            processOSPFStanza(ps);
            break;

         default:
            System.out.println("bad protocols stanza type");
            break;

         }
         break;

      case ROUTING_OPTIONS:
         FlatRoutingOptionsStanza ros = (FlatRoutingOptionsStanza) js;
         processRoutingOptionsStanza(ros);
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

   private void processInterfacesStanza(FlatInterfacesStanza is) {
      Interface inter = is.getInterface();
      if (inter != null) {
         if (!(_interfaceAddressMap.containsKey(inter.getName()))) {
            // System.out.println("new if: "+inter.getName());
            _configuration.addInterface(inter);
            _interfaceAddressMap.put(inter.getName(), inter.getIP() + "/"
                  + inter.getSubnetMask());
         }
         else {
            // System.out.println("old if: "+inter.getName());
            for (Interface tempI : _configuration.getInterfaces()) {
               if (tempI.getName().equals(inter.getName())) {
                  // System.out.println(is.getType1());
                  switch (is.getType1()) {
                  case DISABLE:
                     tempI.setActive(false);
                     break;

                  case NULL:
                     break;

                  case UNIT:
                     // System.out.println(is.getType2()+" "+is.getType3());
                     switch (is.getType2()) {
                     case BRIDGE:
                        switch (is.getType3()) {
                        case INTERFACE_MODE:
                           tempI.setSwitchportMode(inter.getSwitchportMode());
                           break;

                        case NULL:
                           break;

                        case VLAN_ID:
                           tempI.setAccessVlan(inter.getAccessVlan());
                           break;

                        case VLAN_ID_LIST:
                           tempI.setNativeVlan(0);
                           tempI.addAllowedRanges(inter.getAllowedVlans());
                           break;

                        case ADDRESS:
                           throw new Error("to implement");
                        case NATIVE_VLAN:
                           throw new Error("to implement");
                        case FILTER:

                        default:
                           throw new Error("bad unit type");

                        }

                        break;

                     case INET:
                        switch (is.getType3()) {
                        case ADDRESS:
                           tempI.setIP(inter.getIP());
                           tempI.setSubnetMask(inter.getSubnetMask());
                           // System.out.println(tempI.getIP());
                           // System.out.println(tempI.getSubnetMask());
                           _interfaceAddressMap.remove(inter.getName());
                           _interfaceAddressMap.put(inter.getName(),
                                 inter.getIP() + "/" + inter.getSubnetMask());
                           break;

                        case NULL:
                           break;

                        case INTERFACE_MODE:
                        case VLAN_ID:
                        case VLAN_ID_LIST:
                        case NATIVE_VLAN:
                           throw new Error("to implement");
                        case FILTER:
                        default:
                           throw new Error("bad unit type");

                        }
                        break;

                     case INET6:
                        break;

                     default:
                        System.out.println("bad family type");
                        break;

                     }
                     break;
                  case NATIVE_VLAN_ID:
                     throw new Error("to implement");

                  default:
                     System.out.println("bad interface stanza type");
                     break;

                  }
               }
            }
         }
      }

   }

   private void processOSPFStanza(FlatProtocolsStanza ps) {
      // System.out.println(ps.getOType2()+" "+ps.getOType3());
      List<OSPFProcess> tmpOspf = _configuration.getOSPFProcesses();
      OSPFProcess ospf;

      if (tmpOspf.isEmpty()) {
         ospf = new OSPFProcess(0);
         if (_routerID != null) {
            ospf.setRouterId(_routerID);
         }
         ospf.setReferenceBandwidth(DEFAULT_REFERENCE_BANDWIDTH);
      }
      else {
         ospf = tmpOspf.get(0);
      }

      switch (ps.getOType2()) {
      case AREA:
         switch (ps.getOType3()) {
         case INTERFACE:
            HashMap<Integer, ArrayList<String>> ospfAreaMap = ps
                  .getOSPFAreaMap();
            Set<Integer> arealist = ospfAreaMap.keySet();
            for (Integer a : arealist) {
               ArrayList<String> iflist = ospfAreaMap.get(a);
               for (String ifname : iflist) {
                  String ipsub = _interfaceAddressMap.get(ifname);
                  String[] iptmp = ipsub.split("/");
                  ospf.addNetwork(iptmp[0], iptmp[1], a.intValue());
               }
            }
            break;

         case NULL:
            break;

         default:
            System.out.println("bad area stanza type");
            break;
         }
         break;

      case EXPORT:
         ospf.addExportPolicyStatements(ps.getOSPFExports());
         break;

      case NULL:
         break;

      case REFERENCE_BANDWIDTH:
         ospf.setReferenceBandwidth(ps.getReferenceBandwidth());
         break;

      default:
         System.out.println("bad ospf stanza type");
         break;

      }

      if (tmpOspf.isEmpty()) {
         _configuration.addOSPFProcess(ospf);
      }
   }

   private void processBGPStanza(FlatProtocolsStanza ps) {

      List<BGPProcess> tmpBgp = _configuration.getBGPProcesses();
      BGPProcess bgp;

      if (tmpBgp.isEmpty()) {

         if (_asNum >= 0) {
            bgp = new BGPProcess(_asNum);
         }
         bgp = new BGPProcess(0);
      }
      else {
         bgp = tmpBgp.get(0);
      }
      // System.out.println(ps.getBType2());
      switch (ps.getBType2()) {
      case GROUP:
         if (!(ps.getGroupList().isEmpty())) {
            BGPGroup newG = ps.getGroupList().get(0);
            BGPGroup oldG = bgp.getPeerGroup(newG.getName());
            if (oldG == null) {
               bgp.addPeerGroup(newG);
            }
            else {
               switch (ps.getBType3()) {
               case CLUSTER:
                  /*
                   * Not implemented yet if (_isExternal) { System.out
                   * .println("setting route reflector on external group "); }
                   */

                  oldG.setClusterId(newG.getClusterId());
                  oldG.setRouteReflectorClient();

                  break;

               case FAMILY:
                  break;

               case LOCAL_AS:
                  oldG.setLocalAS(newG.getLocalAS());
                  break;

               case NEIGHBOR:
                  BGPNeighbor newN = newG.getNeighbors().get(0);
                  BGPNeighbor oldN = null;
                  for (BGPNeighbor n : oldG.getNeighbors()) {
                     if (n.getIP().equals(newN.getIP())) {
                        oldN = n;
                     }
                  }
                  if (oldN == null) {
                     oldG.addNeighbor(newN);
                  }
                  else {
                     switch (ps.getBType4()) {
                     case EXPORT:
                        oldN.setOutboundPolicyStatement(newN
                              .getOutboundPolicyStatement());
                        break;

                     case IMPORT:
                        oldN.setInboundPolicyStatement(newN
                              .getInboundPolicyStatement());
                        break;

                     case LOCAL_AS:
                        oldN.setLocalAS(newN.getLocalAS());
                        break;

                     case NULL:
                        break;

                     case PEER_AS:
                        oldN.setRemoteAS(newN.getRemoteAS());
                        break;

                     default:
                        System.out
                              .println("bad neighbor group bgp stanza type");
                        break;
                     }
                  }
                  break;

               case NULL:
                  break;

               case TYPE:
                  break;

               case EXPORT:
                  oldG.setOutboundPolicyStatement(newG
                        .getOutboundPolicyStatement());
                  break;
               case IMPORT:
                  throw new Error("not implemented");
               case PEER_AS:
                  throw new Error("not implemented");
               case LOCAL_ADDRESS:
                  throw new Error("not implemented");

               default:
                  System.out.println("bad group bgp stanza type");
                  break;
               }
            }
         }
         break;

      case NULL:
         break;

      default:
         System.out.println("bad bgp stanza type");
         break;

      }

      bgp.addActivatedNeighbors(bgp.getActivatedNeighbors());
      if (tmpBgp.isEmpty()) {
         _configuration.addBGPProcess(bgp);
      }

   }

   private void processRoutingOptionsStanza(FlatRoutingOptionsStanza ros) {
      // System.out.println(ros.getType1());
      switch (ros.getType1()) {
      case AGGREGATE:
         break;

      case AS:
         _asNum = ros.getASNum();
         List<BGPProcess> tmpBgp = _configuration.getBGPProcesses();
         if (!(tmpBgp.isEmpty())) {
            for (BGPProcess b : tmpBgp) {
               // System.out.println("setting as num for bgp process "
               // + b.getPid());
               b.setAsNum(_asNum);
            }
         }
         break;

      case GENERATE:
         GenerateRoute newG = ros.getGenerateRoutes();
         GenerateRoute oldG = null;
         for (GenerateRoute g : _configuration.getGenerateRoutes()) {
            if ((g.getPrefix().equals(newG.getPrefix()))
                  && (g.getPrefixLength() == newG.getPrefixLength())) {
               oldG = g;
               // System.out.println("found old matching gnerated route");
            }
         }
         if (oldG == null) {
            // System.out.println("add new generated route");
            _configuration.addGenerateRoute(newG);
         }
         else {
            // System.out.println(ros.getType2());
            switch (ros.getType2()) {
            case NULL:
               break;

            case POLICY:
               oldG.setPolicy(newG.getPolicy());
               break;

            default:
               throw new Error("bad generate route stanza type");

            }
         }
         break;

      case NULL:
         break;

      case ROUTER_ID:
         _routerID = ros.getRouterID();
         List<OSPFProcess> tmpOspf = _configuration.getOSPFProcesses();
         if (!(tmpOspf.isEmpty())) {
            for (OSPFProcess o : tmpOspf) {
               // System.out.println("setting router id for ospf process "
               // + o.getPid());
               o.setRouterId(_routerID);
            }
         }
         break;

      case STATIC:
         _configuration.addStaticRoutes(ros.getStaticRoutes());
         break;

      default:
         System.out.println("bad ro stanza type");
         break;

      }
   }

   private void processFirewallStanza(FlatFireWallStanza fs) {
      Map<String, ExtendedAccessList> emap = _configuration
            .getExtendedAccessLists();
      ExtendedAccessList eal = fs.getFilters().get(0);
      ExtendedAccessListTerm newterm = eal.getTerms().get(0);
      ExtendedAccessListTerm oldterm = null;

      //System.out.println("firewall: "+fs.getIsFrom());
      //System.out.println(fs.getFType() + " " + fs.getTType());
      if (!(emap.containsKey(eal.getId()))) {
         _configuration.addExtendedAccessList(eal);
      }
      else {
         ExtendedAccessList oldL = emap.get(eal.getId());
         for (ExtendedAccessListTerm t : oldL.getTerms()) {
            if (t.getTermName().equals(newterm.getTermName())) {
               oldterm = t;
               break;
            }
         }
         if (oldterm == null) {
            oldL.addTerm(newterm);
         }
         else {
            if (fs.getIsFrom()) {
               switch (fs.getFType()) {
               case DESTINATION_ADDRESS:
                  for (String da : newterm.getDestinationAddress()) {
                     oldterm.addDestinationAddress(da);
                  }
                  break;

               case SOURCE_PORT:
                  for (SubRange sr : newterm.getSrcPortRanges()) {
                     oldterm.addSrcPortRange(sr);
                  }
                  break;

               case PROTOCOL:
                  for (Integer p : newterm.getProtocols()) {
                     oldterm.addProtocol(p);
                  }
                  break;

               case SOURCE_ADDRESS:
                  for (String sa : newterm.getSourceAddress()) {
                     oldterm.addSourceAddress(sa);
                  }
                  break;
                  
               case DESTINATION_PORT:
                  for (SubRange dp : newterm.getDstPortRanges()) {
                     oldterm.addDstPortRange(dp);
                  }
                  break;

               default:
                  throw new Error("bad firewall term from stanza type");

               }
            }
            else {
               switch (fs.getTType()) {
               case ACCEPT:
               case DISCARD:
                  oldterm.setLineAction(newterm.getLineAction());
                  break;

               case NEXT_TERM:
               case NULL:
                  break;

               default:
                  throw new Error("bad firewall term then stanza type");

               }
            }
         }

      }

   }

   public void processPolicyOptionsStanza(FlatPolicyOptionsStanza pos) {
      //System.out.println("policy options: "+pos.getType1());
      switch (pos.getType1()) {
      case AS_PATH:
         _configuration.addAsPathAccessLists(pos.getAsPathLists());
         break;

      case COMMUNITY:
         _configuration.addExpandedCommunityLists(pos.getCommunities());
         break;

      case NULL:
         break;

      case POLICY_STATEMENT:
         if (!(pos.getMaps().isEmpty())) {
            PolicyStatement newps = pos.getMaps().get(0);
            PolicyStatement oldps = _configuration.getPolicyStatements().get(
                  newps.getMapName());
            if (oldps == null) {
               //System.out.println("cannot find old ps");
               _configuration.addPolicyStatements(pos.getMaps());
               _configuration.addRouteFilters(pos.getFilters());
            }
            else {
               //System.out.println("found ps: " + oldps.getMapName());
               PolicyStatementClause newpsc = newps.getClauseList().get(0);
               PolicyStatementClause oldpsc = null;
               for (PolicyStatementClause psc : oldps.getClauseList()) {
                  if (psc.getClauseName().equals(newpsc.getClauseName())) {
                     oldpsc = psc;
                     break;
                  }
               }
               if (oldpsc == null) {
                  //System.out.println("cannot find old psc");
                  oldps.addClause(newpsc);
                  _configuration.addRouteFilters(pos.getFilters());
               }
               else {
                  //System.out.println("found psc: " + oldpsc.getClauseName());
                  if (pos.isFrom()) {
                     //System.out.println("is From");
                     //System.out.println(pos.getFType());
                     switch (pos.getFType()) {
                     case IPV6:
                        break;

                     case NEIGHBOR:
                     case PREFIX_LIST:
                     case PROTOCOL:
                        oldpsc.addMatchLines(newpsc.getMatchList());
                        break;

                     case NULL:
                        break;

                     case ROUTE_FILTER:
                        oldpsc.addMatchLines(newpsc.getMatchList());
                        _configuration.addRouteFilters(pos.getFilters());
                        break;
                     case COMMUNITY:
                     case AS_PATH:
                        throw new Error("not implemented");
                     

                     default:
                        System.out.println("bad from stanza type");
                        break;

                     }
                  }
                  else {
                     //System.out.println("is Then");
                     //System.out.println(pos.getTType());
                     switch (pos.getTType()) {
                     case ACCEPT:
                     case REJECT:
                        oldpsc.setAction(newpsc.getAction());
                        break;

                     case LOCAL_PREF:
                     case METRIC:
                     case NEXT_HOP:
                        oldpsc.addSetLines(newpsc.getSetList());
                        break;

                     case NULL:
                        break;
                     case COMMUNITY_ADD:
                     case COMMUNITY_DELETE:
                     case COMMUNITY_SET:
                        throw new Error("not implemented");
                     default:
                        throw new Error("bad then stanza type");

                     }
                  }
               }
            }
            // _configuration.addRouteFilters(pos.getFilters());
         }
         break;

      case PREFIX_LIST:
         if (!(pos.getFilters().isEmpty())) {
            RouteFilter newrf = pos.getFilters().get(0);
            RouteFilter oldrf = _configuration.getRouteFilter().get(
                  newrf.getName());
            if (oldrf == null) {
               //System.out.println("cannot find old filter");
               _configuration.addRouteFilters(pos.getFilters());
            }
            else {
               //System.out.println("found filter: " + oldrf.getName());
               oldrf.addLines(newrf.getLines());
            }
         }
         break;

      default:
         System.out.println("bad policy options stanza type");
         break;

      }

   }

   public JuniperVendorConfiguration getConfiguration() {
      return _configuration;
   }

}
