package org.batfish.vendor.cisco_aci.representation;

import static org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker.NO_PREFERENCE;
import static org.batfish.datamodel.bgp.NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.ospf.OspfArea;
import org.batfish.datamodel.ospf.OspfInterfaceSettings;
import org.batfish.datamodel.ospf.OspfNetworkType;
import org.batfish.datamodel.ospf.OspfProcess;
import org.batfish.datamodel.ospf.StubType;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.CallExpr;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.LiteralOrigin;
import org.batfish.datamodel.routing_policy.expr.SelfNextHop;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetLocalPreference;
import org.batfish.datamodel.routing_policy.statement.SetNextHop;
import org.batfish.datamodel.routing_policy.statement.SetOrigin;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;

/** Converts ACI routing components (L2Out/L3Out/BGP/OSPF/static routes). */
final class AciRoutingConverter {

  private AciRoutingConverter() {}

  static void convertL3Outs(
      FabricNode node,
      AciConfiguration aciConfig,
      Map<String, Interface> interfaces,
      Vrf vrf,
      Configuration c,
      Warnings warnings) {
    Map<String, L3Out> l3Outs = aciConfig.getL3Outs();
    if (l3Outs == null || l3Outs.isEmpty()) {
      return;
    }

    for (L3Out l3Out : l3Outs.values()) {
      String l3OutName = l3Out.getName();
      String vrfName = l3Out.getVrf();

      Vrf targetVrf = vrf;
      if (vrfName != null) {
        Vrf foundVrf = c.getVrfs().get(vrfName);
        if (foundVrf != null) {
          targetVrf = foundVrf;
        } else {
          warnings.redFlagf("VRF %s not found for L3Out %s, using default VRF", vrfName, l3OutName);
        }
      }

      if (l3Out.getBgpPeers() != null && !l3Out.getBgpPeers().isEmpty()) {
        if (targetVrf.getBgpProcess() == null) {
          BgpProcess.Builder bgpProcessBuilder = convertBgpPeers(l3Out, targetVrf, warnings);
          if (bgpProcessBuilder != null) {
            bgpProcessBuilder.build();
          }
        }
        addBgpPeersToProcess(l3Out, interfaces, targetVrf, c, warnings);
      }

      if (l3Out.getStaticRoutes() != null && !l3Out.getStaticRoutes().isEmpty()) {
        convertStaticRoutes(l3Out, interfaces, targetVrf, warnings);
      }

      if (l3Out.getOspfConfig() != null) {
        convertOspfConfig(l3Out.getOspfConfig(), l3OutName, node, interfaces, targetVrf, warnings);
      }

      if (l3Out.getExternalEpgs() != null && !l3Out.getExternalEpgs().isEmpty()) {
        convertExternalEpgs(l3Out, interfaces, targetVrf, warnings);
      }
    }
  }

  static void convertL2Outs(
      AciConfiguration aciConfig,
      Map<String, Interface> interfaces,
      Vrf defaultVrf,
      Configuration c,
      Warnings warnings) {
    Map<String, L2Out> l2Outs = aciConfig.getL2Outs();
    if (l2Outs == null || l2Outs.isEmpty()) {
      return;
    }

    for (Map.Entry<String, L2Out> entry : l2Outs.entrySet()) {
      L2Out l2Out = entry.getValue();
      String l2OutName = l2Out.getName();
      if (l2OutName == null || l2OutName.isEmpty()) {
        continue;
      }

      int vlanId = parseL2OutVlanId(l2Out, warnings);
      if (vlanId <= 0) {
        continue;
      }

      Vrf targetVrf = defaultVrf;
      String bdName = l2Out.getBridgeDomain();
      if (bdName != null && !bdName.isEmpty()) {
        BridgeDomain bd = aciConfig.getBridgeDomains().get(bdName);
        if (bd != null && bd.getVrf() != null) {
          Vrf foundVrf = c.getVrfs().get(bd.getVrf());
          if (foundVrf != null) {
            targetVrf = foundVrf;
          }
        }
      }

      String interfaceName = "L2Out-" + l2OutName;
      if (interfaces.containsKey(interfaceName)) {
        continue;
      }

      Interface.Builder l2OutInterface =
          Interface.builder()
              .setName(interfaceName)
              .setType(InterfaceType.VLAN)
              .setOwner(c)
              .setVrf(targetVrf)
              .setAdminUp(true)
              .setMtu(AciConstants.DEFAULT_MTU)
              .setVlan(vlanId)
              .setHumanName(String.format("L2Out %s (VLAN %d)", l2OutName, vlanId))
              .setDescription(
                  l2Out.getDescription() != null
                      ? l2Out.getDescription()
                      : String.format("L2Out %s for external L2 connectivity", l2OutName))
              .setDeclaredNames(ImmutableList.of(interfaceName));

      interfaces.put(interfaceName, l2OutInterface.build());
    }
  }

  private static int parseL2OutVlanId(L2Out l2Out, Warnings warnings) {
    String encap = l2Out.getEncapsulation();
    if (encap == null || encap.isEmpty()) {
      return Math.abs(l2Out.getName().hashCode() % 4094) + 1;
    }

    encap = encap.toLowerCase();

    if (encap.startsWith("vlan-")) {
      try {
        int vlan = Integer.parseInt(encap.substring(5));
        if (vlan >= 1 && vlan <= 4095) {
          return vlan;
        }
      } catch (NumberFormatException e) {
        warnings.redFlagf("Invalid VLAN encapsulation '%s' for L2Out %s", encap, l2Out.getName());
      }
    }

    if (encap.startsWith("vxlan-")) {
      try {
        int vni = Integer.parseInt(encap.substring(6));
        if (vni >= 1) {
          return (vni % 4094) + 1;
        }
      } catch (NumberFormatException e) {
        warnings.redFlagf("Invalid VXLAN encapsulation '%s' for L2Out %s", encap, l2Out.getName());
      }
    }

    return 0;
  }

  private static @Nullable BgpProcess.Builder convertBgpPeers(
      L3Out l3Out, Vrf vrf, Warnings warnings) {
    if (l3Out.getBgpPeers() == null || l3Out.getBgpPeers().isEmpty()) {
      return null;
    }

    org.batfish.vendor.cisco_aci.representation.BgpProcess bgpProcessConfig = l3Out.getBgpProcess();
    if (bgpProcessConfig == null) {
      bgpProcessConfig = new org.batfish.vendor.cisco_aci.representation.BgpProcess();
    }

    Ip routerId = null;
    if (bgpProcessConfig.getRouterId() != null) {
      try {
        routerId = Ip.parse(bgpProcessConfig.getRouterId());
      } catch (IllegalArgumentException e) {
        warnings.redFlagf(
            "Invalid router ID %s for BGP process in L3Out %s",
            bgpProcessConfig.getRouterId(), l3Out.getName());
      }
    }
    if (routerId == null) {
      routerId = Ip.AUTO;
    }

    BgpProcess.Builder bgpBuilder =
        BgpProcess.builder()
            .setRouterId(routerId)
            .setEbgpAdminCost(
                bgpProcessConfig.getEbgpAdminCost() != null
                    ? bgpProcessConfig.getEbgpAdminCost()
                    : AciConstants.DEFAULT_EBGP_ADMIN_COST)
            .setIbgpAdminCost(
                bgpProcessConfig.getIbgpAdminCost() != null
                    ? bgpProcessConfig.getIbgpAdminCost()
                    : AciConstants.DEFAULT_IBGP_ADMIN_COST)
            .setLocalAdminCost(
                bgpProcessConfig.getVrfAdminCost() != null
                    ? bgpProcessConfig.getVrfAdminCost()
                    : AciConstants.DEFAULT_LOCAL_BGP_WEIGHT)
            .setLocalOriginationTypeTieBreaker(NO_PREFERENCE)
            .setNetworkNextHopIpTieBreaker(HIGHEST_NEXT_HOP_IP)
            .setRedistributeNextHopIpTieBreaker(HIGHEST_NEXT_HOP_IP);

    bgpBuilder.setVrf(vrf);

    return bgpBuilder;
  }

  private static @Nullable BgpActivePeerConfig convertBgpPeer(
      BgpPeer bgpPeer,
      L3Out l3Out,
      Map<String, Interface> interfaces,
      Vrf vrf,
      @Nullable Long localAs,
      Configuration c,
      Warnings warnings) {
    String peerAddressStr = bgpPeer.getPeerAddress();
    if (peerAddressStr == null) {
      warnings.redFlagf("BGP peer in L3Out %s has no peer address", l3Out.getName());
      return null;
    }

    Ip peerAddress;
    try {
      peerAddress = Ip.parse(peerAddressStr);
    } catch (IllegalArgumentException e) {
      warnings.redFlagf("Invalid BGP peer address %s in L3Out %s", peerAddressStr, l3Out.getName());
      return null;
    }

    BgpActivePeerConfig.Builder peerBuilder =
        BgpActivePeerConfig.builder().setPeerAddress(peerAddress);

    if (bgpPeer.getRemoteAs() != null) {
      try {
        long remoteAs = Long.parseLong(bgpPeer.getRemoteAs());
        peerBuilder.setRemoteAsns(LongSpace.of(remoteAs));
      } catch (NumberFormatException e) {
        warnings.redFlagf(
            "Invalid remote AS %s for BGP peer %s in L3Out %s",
            bgpPeer.getRemoteAs(), peerAddressStr, l3Out.getName());
      }
    }

    if (bgpPeer.getLocalAs() != null) {
      try {
        long peerLocalAs = Long.parseLong(bgpPeer.getLocalAs());
        peerBuilder.setLocalAs(peerLocalAs);
      } catch (NumberFormatException e) {
        warnings.redFlagf(
            "Invalid local AS %s for BGP peer %s in L3Out %s",
            bgpPeer.getLocalAs(), peerAddressStr, l3Out.getName());
      }
    } else if (localAs != null) {
      peerBuilder.setLocalAs(localAs);
    }

    Ip localIp = determineBgpLocalIp(bgpPeer, l3Out, interfaces, warnings);
    if (localIp != null) {
      peerBuilder.setLocalIp(localIp);
    }

    peerBuilder.setDescription(
        bgpPeer.getDescription() != null
            ? bgpPeer.getDescription()
            : String.format("BGP peer from L3Out %s", l3Out.getName()));

    if (bgpPeer.getEbgpMultihop() != null && bgpPeer.getEbgpMultihop()) {
      peerBuilder.setEbgpMultihop(true);
    }

    Ipv4UnicastAddressFamily.Builder afBuilder = Ipv4UnicastAddressFamily.builder();

    String importPolicyName = createBgpImportPolicy(bgpPeer, l3Out, c, warnings);
    if (importPolicyName != null) {
      afBuilder.setImportPolicy(importPolicyName);
    }

    String exportPolicyName = createBgpExportPolicy(bgpPeer, l3Out, c);
    if (exportPolicyName != null) {
      afBuilder.setExportPolicy(exportPolicyName);
    }

    if (bgpPeer.getRouteReflectorClient() != null && bgpPeer.getRouteReflectorClient()) {
      afBuilder.setRouteReflectorClient(true);
    }

    peerBuilder.setIpv4UnicastAddressFamily(afBuilder.build());

    BgpProcess bgpProcess = vrf.getBgpProcess();
    if (bgpProcess != null) {
      peerBuilder.setBgpProcess(bgpProcess);
    }

    return peerBuilder.build();
  }

  private static void addBgpPeersToProcess(
      L3Out l3Out, Map<String, Interface> interfaces, Vrf vrf, Configuration c, Warnings warnings) {
    if (l3Out.getBgpPeers() == null || l3Out.getBgpPeers().isEmpty()) {
      return;
    }

    Long localAs = null;
    if (l3Out.getBgpProcess() != null && l3Out.getBgpProcess().getAs() != null) {
      localAs = l3Out.getBgpProcess().getAs();
    }

    for (BgpPeer bgpPeer : l3Out.getBgpPeers()) {
      convertBgpPeer(bgpPeer, l3Out, interfaces, vrf, localAs, c, warnings);
    }
  }

  private static @Nullable Ip determineBgpLocalIp(
      BgpPeer bgpPeer, L3Out l3Out, Map<String, Interface> interfaces, Warnings warnings) {
    String updateSourceInterface = bgpPeer.getUpdateSourceInterface();
    if (updateSourceInterface != null) {
      Interface iface = interfaces.get(updateSourceInterface);
      if (iface != null && iface.getConcreteAddress() != null) {
        return iface.getConcreteAddress().getIp();
      }
      warnings.redFlagf(
          "Update source interface %s not found or has no IP address for BGP peer %s in L3Out %s",
          updateSourceInterface, bgpPeer.getPeerAddress(), l3Out.getName());
      return null;
    }

    Ip peerAddress;
    try {
      peerAddress = Ip.parse(bgpPeer.getPeerAddress());
    } catch (IllegalArgumentException e) {
      return null;
    }
    for (Interface iface : interfaces.values()) {
      if (iface.getConcreteAddress() != null) {
        Prefix subnet = iface.getConcreteAddress().getPrefix();
        if (subnet.containsIp(peerAddress)) {
          return iface.getConcreteAddress().getIp();
        }
      }
    }

    Interface loopback = interfaces.get("loopback0");
    if (loopback != null && loopback.getConcreteAddress() != null) {
      return loopback.getConcreteAddress().getIp();
    }

    return null;
  }

  private static @Nullable String createBgpImportPolicy(
      BgpPeer bgpPeer, L3Out l3Out, Configuration c, Warnings warnings) {
    String policyName =
        String.format("~BGP_IMPORT~%s~%s", l3Out.getName(), bgpPeer.getPeerAddress());

    RoutingPolicy.Builder policyBuilder = RoutingPolicy.builder().setName(policyName).setOwner(c);

    if (bgpPeer.getLocalPreference() != null) {
      try {
        int localPref = Integer.parseInt(bgpPeer.getLocalPreference());
        policyBuilder.addStatement(new SetLocalPreference(new LiteralLong(localPref)));
      } catch (NumberFormatException e) {
        warnings.redFlagf(
            "Invalid local preference %s for BGP peer %s in L3Out %s",
            bgpPeer.getLocalPreference(), bgpPeer.getPeerAddress(), l3Out.getName());
      }
    }

    policyBuilder.addStatement(new SetOrigin(new LiteralOrigin(OriginType.IGP, null)));

    if (bgpPeer.getImportRouteMap() != null) {
      List<Statement> trueStatements = ImmutableList.of(Statements.ExitAccept.toStaticStatement());
      List<Statement> falseStatements = ImmutableList.of(Statements.ExitReject.toStaticStatement());
      If routeMapIf =
          new If(
              "Apply import route-map " + bgpPeer.getImportRouteMap(),
              new CallExpr(bgpPeer.getImportRouteMap()),
              trueStatements,
              falseStatements);
      policyBuilder.addStatement(routeMapIf);
    } else {
      policyBuilder.addStatement(Statements.ExitAccept.toStaticStatement());
    }

    RoutingPolicy policy = policyBuilder.build();
    c.getRoutingPolicies().put(policyName, policy);
    return policyName;
  }

  private static @Nullable String createBgpExportPolicy(
      BgpPeer bgpPeer, L3Out l3Out, Configuration c) {
    String policyName =
        String.format("~BGP_EXPORT~%s~%s", l3Out.getName(), bgpPeer.getPeerAddress());

    RoutingPolicy.Builder policyBuilder = RoutingPolicy.builder().setName(policyName).setOwner(c);

    if (bgpPeer.getNextHopSelf() != null && bgpPeer.getNextHopSelf()) {
      policyBuilder.addStatement(new SetNextHop(SelfNextHop.getInstance()));
    }

    if (bgpPeer.getExportRouteMap() != null) {
      List<Statement> trueStatements = ImmutableList.of(Statements.ExitAccept.toStaticStatement());
      List<Statement> falseStatements = ImmutableList.of(Statements.ExitReject.toStaticStatement());
      If routeMapIf =
          new If(
              "Apply export route-map " + bgpPeer.getExportRouteMap(),
              new CallExpr(bgpPeer.getExportRouteMap()),
              trueStatements,
              falseStatements);
      policyBuilder.addStatement(routeMapIf);
    } else {
      policyBuilder.addStatement(Statements.ExitAccept.toStaticStatement());
    }

    RoutingPolicy policy = policyBuilder.build();
    c.getRoutingPolicies().put(policyName, policy);
    return policyName;
  }

  private static void convertStaticRoutes(
      L3Out l3Out, Map<String, Interface> interfaces, Vrf vrf, Warnings warnings) {
    if (l3Out.getStaticRoutes() == null || l3Out.getStaticRoutes().isEmpty()) {
      return;
    }

    for (org.batfish.vendor.cisco_aci.representation.StaticRoute aciStaticRoute :
        l3Out.getStaticRoutes()) {
      StaticRoute staticRoute = convertStaticRoute(aciStaticRoute, l3Out, interfaces, warnings);
      if (staticRoute != null) {
        vrf.getStaticRoutes().add(staticRoute);
      }
    }
  }

  private static @Nullable StaticRoute convertStaticRoute(
      org.batfish.vendor.cisco_aci.representation.StaticRoute aciStaticRoute,
      L3Out l3Out,
      Map<String, Interface> interfaces,
      Warnings warnings) {
    String prefixStr = aciStaticRoute.getPrefix();
    if (prefixStr == null) {
      warnings.redFlagf("Static route in L3Out %s has no prefix", l3Out.getName());
      return null;
    }

    Prefix prefix;
    try {
      prefix = Prefix.parse(prefixStr);
    } catch (IllegalArgumentException e) {
      warnings.redFlagf(
          "Invalid prefix %s for static route in L3Out %s", prefixStr, l3Out.getName());
      return null;
    }

    StaticRoute.Builder routeBuilder = StaticRoute.builder().setNetwork(prefix);

    boolean hasNextHopIp = false;
    boolean hasNextHopInterface = false;

    String nextHopStr = aciStaticRoute.getNextHop();
    if (nextHopStr != null) {
      try {
        Ip nextHop = Ip.parse(nextHopStr);
        routeBuilder.setNextHopIp(nextHop);
        hasNextHopIp = true;
      } catch (IllegalArgumentException e) {
        warnings.redFlagf(
            "Invalid next hop %s for static route %s in L3Out %s",
            nextHopStr, prefixStr, l3Out.getName());
      }
    }

    String nextHopInterface = aciStaticRoute.getNextHopInterface();
    if (nextHopInterface != null) {
      if (interfaces.containsKey(nextHopInterface)) {
        routeBuilder.setNextHopInterface(nextHopInterface);
        hasNextHopInterface = true;
      } else {
        warnings.redFlagf(
            "Next hop interface %s not found for static route %s in L3Out %s",
            nextHopInterface, prefixStr, l3Out.getName());
      }
    }

    if (!hasNextHopIp && !hasNextHopInterface) {
      warnings.redFlagf(
          "Static route %s in L3Out %s has no valid next hop (missing both IP and interface)",
          prefixStr, l3Out.getName());
      return null;
    }

    int adminDist = 1;
    if (aciStaticRoute.getAdministrativeDistance() != null) {
      try {
        adminDist = Integer.parseInt(aciStaticRoute.getAdministrativeDistance());
      } catch (NumberFormatException e) {
        warnings.redFlagf(
            "Invalid administrative distance %s for static route %s in L3Out %s, using default (1)",
            aciStaticRoute.getAdministrativeDistance(), prefixStr, l3Out.getName());
      }
    }
    routeBuilder.setAdmin(adminDist);

    if (aciStaticRoute.getTag() != null) {
      try {
        long tag = Long.parseLong(aciStaticRoute.getTag());
        routeBuilder.setTag(tag);
      } catch (NumberFormatException e) {
        warnings.redFlagf(
            "Invalid tag %s for static route %s in L3Out %s",
            aciStaticRoute.getTag(), prefixStr, l3Out.getName());
      }
    }

    if (aciStaticRoute.getTrack() != null) {
      routeBuilder.setTrack(aciStaticRoute.getTrack());
    }

    return routeBuilder.build();
  }

  private static void convertOspfConfig(
      OspfConfig ospfConfig,
      String l3OutName,
      FabricNode node,
      Map<String, Interface> interfaces,
      Vrf vrf,
      Warnings warnings) {

    String processId = ospfConfig.getProcessId();
    if (processId == null || processId.isEmpty()) {
      processId = l3OutName;
    }

    Ip routerId = inferOspfRouterId(node, interfaces, l3OutName, warnings);

    ImmutableSortedMap.Builder<Long, OspfArea> areasBuilder = ImmutableSortedMap.naturalOrder();

    if (ospfConfig.getAreas() != null) {
      for (org.batfish.vendor.cisco_aci.representation.OspfArea aciArea :
          ospfConfig.getAreas().values()) {
        Long areaNum = parseAreaId(aciArea.getAreaId());
        if (areaNum == null) {
          warnings.redFlagf(
              "Invalid OSPF area ID %s in L3Out %s, skipping area", aciArea.getAreaId(), l3OutName);
          continue;
        }

        OspfArea.Builder areaBuilder = OspfArea.builder().setNumber(areaNum);

        String areaType = aciArea.getAreaType();
        if (areaType != null) {
          switch (areaType.toLowerCase()) {
            case "stub":
              areaBuilder.setStubType(StubType.STUB);
              break;
            case "nssa":
              areaBuilder.setStubType(StubType.NSSA);
              break;
            default:
              areaBuilder.setNonStub();
              break;
          }
        }

        areasBuilder.put(areaNum, areaBuilder.build());
      }
    }

    if (areasBuilder.build().isEmpty()) {
      Long defaultArea = parseAreaId(ospfConfig.getAreaId());
      if (defaultArea == null) {
        defaultArea = 0L;
      }
      areasBuilder.put(defaultArea, OspfArea.builder().setNumber(defaultArea).build());
    }

    OspfProcess ospfProcess =
        OspfProcess.builder()
            .setProcessId(processId)
            .setRouterId(routerId)
            .setReferenceBandwidth(100.0)
            .setAreas(areasBuilder.build())
            .build();

    vrf.addOspfProcess(ospfProcess);

    applyOspfInterfaceSettings(ospfConfig, l3OutName, interfaces, processId, warnings);
  }

  private static Ip inferOspfRouterId(
      FabricNode node, Map<String, Interface> interfaces, String l3OutName, Warnings warnings) {

    for (Interface iface : interfaces.values()) {
      if (iface.getAllAddresses().isEmpty()) {
        continue;
      }
      InterfaceAddress addr = iface.getAllAddresses().iterator().next();
      if (addr instanceof ConcreteInterfaceAddress) {
        Ip ip = ((ConcreteInterfaceAddress) addr).getIp();
        if (!ip.equals(Ip.ZERO)) {
          return ip;
        }
      }
    }

    String nodeId = node.getNodeId();
    if (nodeId != null && !nodeId.isEmpty()) {
      try {
        int id = Integer.parseInt(nodeId.replaceAll("[^0-9]", ""));
        return Ip.create(id & 0xFF);
      } catch (NumberFormatException e) {
        // Fall through to default
      }
    }

    warnings.redFlagf("Could not infer OSPF router ID for L3Out %s, using 0.0.0.1", l3OutName);
    return Ip.create(1);
  }

  private static void applyOspfInterfaceSettings(
      OspfConfig ospfConfig,
      String l3OutName,
      Map<String, Interface> interfaces,
      String processId,
      Warnings warnings) {

    if (ospfConfig.getOspfInterfaces() == null) {
      return;
    }

    for (OspfInterface ospfIface : ospfConfig.getOspfInterfaces()) {
      String ifaceName = ospfIface.getName();
      Interface batfishIface = interfaces.get(ifaceName);

      if (batfishIface == null) {
        String l3OutIfaceName = "L3Out-" + l3OutName + "-" + ifaceName;
        batfishIface = interfaces.get(l3OutIfaceName);
      }

      if (batfishIface == null) {
        warnings.redFlagf(
            "OSPF interface %s in L3Out %s not found in converted interfaces",
            ifaceName, l3OutName);
        continue;
      }

      Long areaNum = parseAreaId(ospfConfig.getAreaId());
      if (areaNum == null) {
        areaNum = 0L;
      }

      OspfInterfaceSettings.Builder settingsBuilder =
          OspfInterfaceSettings.builder()
              .setEnabled(true)
              .setProcess(processId)
              .setAreaName(areaNum);

      if (ospfIface.getCost() != null) {
        settingsBuilder.setCost(ospfIface.getCost());
      }

      int helloInterval = ospfIface.getHelloInterval() != null ? ospfIface.getHelloInterval() : 10;
      settingsBuilder.setHelloInterval(helloInterval);

      int deadInterval = ospfIface.getDeadInterval() != null ? ospfIface.getDeadInterval() : 40;
      settingsBuilder.setDeadInterval(deadInterval);

      OspfNetworkType networkType = convertOspfNetworkType(ospfIface.getNetworkType());
      if (networkType != null) {
        settingsBuilder.setNetworkType(networkType);
      } else {
        settingsBuilder.setNetworkType(OspfNetworkType.POINT_TO_POINT);
      }

      boolean passive = ospfIface.getPassive() != null ? ospfIface.getPassive() : false;
      settingsBuilder.setPassive(passive);

      batfishIface.setOspfSettings(settingsBuilder.build());
    }
  }

  @VisibleForTesting
  static @Nullable OspfNetworkType convertOspfNetworkType(@Nullable String networkTypeStr) {
    if (networkTypeStr == null) {
      return null;
    }

    switch (networkTypeStr.toLowerCase()) {
      case "point-to-point":
      case "p2p":
        return OspfNetworkType.POINT_TO_POINT;
      case "broadcast":
      case "bcast":
        return OspfNetworkType.BROADCAST;
      case "non-broadcast":
      case "nbma":
        return OspfNetworkType.NON_BROADCAST_MULTI_ACCESS;
      case "point-to-multipoint":
      case "p2mp":
        return OspfNetworkType.POINT_TO_MULTIPOINT;
      default:
        return null;
    }
  }

  @VisibleForTesting
  static @Nullable Long parseAreaId(@Nullable String areaIdStr) {
    if (areaIdStr == null || areaIdStr.isEmpty()) {
      return null;
    }

    try {
      return Long.parseLong(areaIdStr);
    } catch (NumberFormatException e) {
      // Not a simple number, try IP address format
    }

    String[] parts = areaIdStr.split("\\.");
    if (parts.length != 4) {
      return null;
    }

    try {
      long result = 0;
      for (int i = 0; i < 4; i++) {
        int octet = Integer.parseInt(parts[i]);
        if (octet < 0 || octet > 255) {
          return null;
        }
        result = (result << 8) | octet;
      }
      return result;
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private static void convertExternalEpgs(
      L3Out l3Out, Map<String, Interface> interfaces, Vrf vrf, Warnings warnings) {
    if (l3Out.getExternalEpgs() == null || l3Out.getExternalEpgs().isEmpty()) {
      return;
    }

    for (ExternalEpg extEpg : l3Out.getExternalEpgs()) {
      String epgName = extEpg.getName();
      if (epgName == null) {
        continue;
      }

      if (extEpg.getSubnets() != null && !extEpg.getSubnets().isEmpty()) {
        for (String subnetStr : extEpg.getSubnets()) {
          Prefix subnet = parsePrefix(subnetStr);
          if (subnet != null && extEpg.getNextHop() != null) {
            try {
              Ip nextHop = Ip.parse(extEpg.getNextHop());
              StaticRoute staticRoute =
                  StaticRoute.builder()
                      .setNetwork(subnet)
                      .setNextHopIp(nextHop)
                      .setAdministrativeCost(1)
                      .build();
              vrf.getStaticRoutes().add(staticRoute);
            } catch (IllegalArgumentException e) {
              warnings.redFlagf(
                  "Invalid next hop %s for external EPG %s in L3Out %s",
                  extEpg.getNextHop(), epgName, l3Out.getName());
            }
          }
        }
      }

      if (extEpg.getInterface() != null) {
        Interface iface = interfaces.get(extEpg.getInterface());
        if (iface == null) {
          warnings.redFlagf(
              "Interface %s not found for external EPG %s in L3Out %s",
              extEpg.getInterface(), epgName, l3Out.getName());
        }
      }
    }
  }

  private static @Nullable Prefix parsePrefix(String prefixStr) {
    if (prefixStr == null) {
      return null;
    }
    try {
      return Prefix.parse(prefixStr);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }
}
