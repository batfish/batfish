package org.batfish.logicblox;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.batfish.collections.RoleSet;
import org.batfish.common.BatfishException;
import org.batfish.main.Warnings;
import org.batfish.representation.AsPathAccessList;
import org.batfish.representation.AsPathAccessListLine;
import org.batfish.representation.BgpNeighbor;
import org.batfish.representation.BgpProcess;
import org.batfish.representation.CommunityList;
import org.batfish.representation.CommunityListLine;
import org.batfish.representation.Configuration;
import org.batfish.representation.GeneratedRoute;
import org.batfish.representation.Interface;
import org.batfish.representation.Ip;
import org.batfish.representation.IpAccessList;
import org.batfish.representation.IpAccessListLine;
import org.batfish.representation.IpProtocol;
import org.batfish.representation.IsisInterfaceMode;
import org.batfish.representation.IsisLevel;
import org.batfish.representation.IsisProcess;
import org.batfish.representation.OriginType;
import org.batfish.representation.OspfArea;
import org.batfish.representation.OspfMetricType;
import org.batfish.representation.OspfProcess;
import org.batfish.representation.PolicyMap;
import org.batfish.representation.PolicyMapAction;
import org.batfish.representation.PolicyMapClause;
import org.batfish.representation.PolicyMapClauseMatchInterfaceLine;
import org.batfish.representation.PolicyMapMatchAsPathAccessListLine;
import org.batfish.representation.PolicyMapMatchColorLine;
import org.batfish.representation.PolicyMapMatchCommunityListLine;
import org.batfish.representation.PolicyMapMatchIpAccessListLine;
import org.batfish.representation.PolicyMapMatchLine;
import org.batfish.representation.PolicyMapMatchNeighborLine;
import org.batfish.representation.PolicyMapMatchPolicyLine;
import org.batfish.representation.PolicyMapMatchProtocolLine;
import org.batfish.representation.PolicyMapMatchRouteFilterListLine;
import org.batfish.representation.PolicyMapMatchTagLine;
import org.batfish.representation.PolicyMapSetAddCommunityLine;
import org.batfish.representation.PolicyMapSetCommunityLine;
import org.batfish.representation.PolicyMapSetDeleteCommunityLine;
import org.batfish.representation.PolicyMapSetLine;
import org.batfish.representation.PolicyMapSetLocalPreferenceLine;
import org.batfish.representation.PolicyMapSetMetricLine;
import org.batfish.representation.PolicyMapSetNextHopLine;
import org.batfish.representation.PolicyMapSetOriginTypeLine;
import org.batfish.representation.PolicyMapSetLevelLine;
import org.batfish.representation.Prefix;
import org.batfish.representation.RouteFilterLine;
import org.batfish.representation.RouteFilterList;
import org.batfish.representation.RoutingProtocol;
import org.batfish.representation.StaticRoute;
import org.batfish.representation.SwitchportEncapsulationType;
import org.batfish.util.SubRange;
import org.batfish.util.Util;
import org.batfish.z3.Synthesizer;

public class ConfigurationFactExtractor {

   private static final int DEFAULT_CISCO_VLAN_OSPF_COST = 1;

   private static final String FLOW_SINK_INTERFACE_PREFIX = "TenGigabitEthernet100/";

   private Set<Long> _allCommunities;

   private Configuration _configuration;

   private Map<String, StringBuilder> _factBins;

   private Warnings _w;

   public ConfigurationFactExtractor(Configuration c, Set<Long> allCommunities,
         Map<String, StringBuilder> factBins, Warnings warnings) {
      _configuration = c;
      _allCommunities = allCommunities;
      _factBins = factBins;
      _w = warnings;
   }

   private void writeAsPaths() {
      StringBuilder wSetAsPathLineDeny = _factBins.get("SetAsPathLineDeny");
      StringBuilder wSetAsPathLineMatchAs = _factBins
            .get("SetAsPathLineMatchAs");
      StringBuilder wSetAsPathLineMatchAsAtBeginning = _factBins
            .get("SetAsPathLineMatchAsAtBeginning");
      StringBuilder wSetAsPathLineMatchAsPair = _factBins
            .get("SetAsPathLineMatchAsPair");
      StringBuilder wSetAsPathLineMatchAsPairAtBeginning = _factBins
            .get("SetAsPathLineMatchAsPairAtBeginning");
      StringBuilder wSetAsPathLineMatchEmpty = _factBins
            .get("SetAsPathLineMatchEmpty");
      StringBuilder wSetAsPathLinePermit = _factBins.get("SetAsPathLinePermit");
      String hostname = _configuration.getHostname();
      for (Entry<String, AsPathAccessList> e : _configuration
            .getAsPathAccessLists().entrySet()) {
         String asPathName = hostname + ":" + e.getKey();
         AsPathAccessList asPath = e.getValue();
         List<AsPathAccessListLine> lines = asPath.getLines();
         for (int i = 0; i < lines.size(); i++) {
            AsPathAccessListLine line = lines.get(i);
            switch (line.getAction()) {
            case ACCEPT:
               wSetAsPathLinePermit.append(asPathName + "|" + i + "\n");
               break;

            case REJECT:
               wSetAsPathLineDeny.append(asPathName + "|" + i + "\n");
               break;

            default:
               throw new BatfishException("Bad action");
            }
            SubRange as1Range = line.getAs1Range();
            SubRange as2Range = line.getAs2Range();
            boolean atBeginning = line.getAtBeginning();
            if (as2Range != null) {
               // we are dealing with a pair to match
               int as1Low = as1Range.getStart();
               int as1High = as1Range.getEnd();
               int as2Low = as2Range.getStart();
               int as2High = as2Range.getEnd();
               if (atBeginning) {
                  wSetAsPathLineMatchAsPairAtBeginning.append(asPathName + "|"
                        + i + "|" + as1Low + "|" + as1High + "|" + as2Low + "|"
                        + as2High + "\n");
               }
               else {
                  wSetAsPathLineMatchAsPair.append(asPathName + "|" + i + "|"
                        + as1Low + "|" + as1High + "|" + as2Low + "|" + as2High
                        + "\n");
               }
            }
            else if (as1Range != null) {
               // we are dealing with a single as to match
               int asLow = as1Range.getStart();
               int asHigh = as1Range.getEnd();
               if (atBeginning) {
                  wSetAsPathLineMatchAsAtBeginning.append(asPathName + "|" + i
                        + "|" + asLow + "|" + asHigh + "\n");
               }
               else {
                  wSetAsPathLineMatchAs.append(asPathName + "|" + i + "|"
                        + asLow + "|" + asHigh + "\n");
               }
            }
            if (line.getMatchEmpty()) {
               // we allow an empty match
               wSetAsPathLineMatchEmpty.append(asPathName + "|" + i + "\n");
            }
         }
      }
   }

   private void writeBgpGeneratedRoutes() {
      String hostname = _configuration.getHostname();
      BgpProcess proc = _configuration.getBgpProcess();
      StringBuilder wSetBgpGeneratedRoute_flat = _factBins
            .get("SetBgpGeneratedRoute_flat");
      StringBuilder wSetBgpGeneratedRoutePolicy_flat = _factBins
            .get("SetBgpGeneratedRoutePolicy_flat");
      StringBuilder wSetNetwork = _factBins.get("SetNetwork");
      if (proc != null) {
         for (GeneratedRoute gr : proc.getGeneratedRoutes()) {
            long network_start = gr.getPrefix().getAddress().asLong();
            int prefix_length = gr.getPrefix().getPrefixLength();
            long network_end = gr.getPrefix().getEndAddress().asLong();
            wSetBgpGeneratedRoute_flat.append(hostname + "|" + network_start
                  + "|" + network_end + "|" + prefix_length + "\n");
            wSetNetwork.append(network_start + "|" + network_start + "|"
                  + network_end + "|" + prefix_length + "\n");
            for (PolicyMap generationPolicy : gr.getGenerationPolicies()) {
               String gpName = hostname + ":" + generationPolicy.getMapName();
               wSetBgpGeneratedRoutePolicy_flat.append(hostname + "|"
                     + network_start + "|" + network_end + "|" + prefix_length
                     + "|" + gpName + "\n");
            }
         }
      }
   }

   private void writeBgpNeighborGeneratedRoutes() {
      String hostname = _configuration.getHostname();
      BgpProcess proc = _configuration.getBgpProcess();
      StringBuilder wSetBgpNeighborGeneratedRoute_flat = _factBins
            .get("SetBgpNeighborGeneratedRoute_flat");
      StringBuilder wSetBgpNeighborGeneratedRoutePolicy_flat = _factBins
            .get("SetBgpNeighborGeneratedRoutePolicy_flat");
      if (proc != null) {
         for (BgpNeighbor neighbor : proc.getNeighbors().values()) {
            Prefix neighborPrefix = neighbor.getPrefix();
            long neighborPrefixStart = neighborPrefix.getAddress().asLong();
            long neighborPrefixEnd = neighborPrefix.getEndAddress().asLong();
            int neighborPrefixLength = neighborPrefix.getPrefixLength();
            for (GeneratedRoute gr : neighbor.getGeneratedRoutes()) {
               long network_start = gr.getPrefix().getAddress().asLong();
               int prefix_length = gr.getPrefix().getPrefixLength();
               long network_end = gr.getPrefix().getEndAddress().asLong();
               wSetBgpNeighborGeneratedRoute_flat.append(hostname + "|"
                     + neighborPrefixStart + "|" + neighborPrefixEnd + "|"
                     + neighborPrefixLength + "|" + network_start + "|"
                     + network_end + "|" + prefix_length + "\n");
               for (PolicyMap generationPolicy : gr.getGenerationPolicies()) {
                  String gpName = hostname + ":"
                        + generationPolicy.getMapName();
                  wSetBgpNeighborGeneratedRoutePolicy_flat.append(hostname
                        + "|" + neighborPrefixStart + "|" + neighborPrefixEnd
                        + "|" + neighborPrefixLength + "|" + network_start
                        + "|" + network_end + "|" + prefix_length + "|"
                        + gpName + "\n");
               }
            }
         }
      }
   }

   private void writeBgpNeighborPolicies() {
      StringBuilder wSetBgpImportPolicy = _factBins
            .get("SetBgpImportPolicy_flat");
      StringBuilder wSetBgpExportPolicy = _factBins
            .get("SetBgpExportPolicy_flat");
      String hostname = _configuration.getHostname();
      BgpProcess proc = _configuration.getBgpProcess();
      if (proc != null) {
         for (BgpNeighbor neighbor : proc.getNeighbors().values()) {
            Prefix neighborPrefix = neighbor.getPrefix();
            long neighborPrefixStart = neighborPrefix.getAddress().asLong();
            long neighborPrefixEnd = neighborPrefix.getEndAddress().asLong();
            int neighborPrefixLength = neighborPrefix.getPrefixLength();
            for (PolicyMap inboundMap : neighbor.getInboundPolicyMaps()) {
               String inboundMapName = hostname + ":" + inboundMap.getMapName();
               wSetBgpImportPolicy.append(hostname + "|" + neighborPrefixStart
                     + "|" + neighborPrefixEnd + "|" + neighborPrefixLength
                     + "|" + inboundMapName + "\n");
            }
            for (PolicyMap outboundMap : neighbor.getOutboundPolicyMaps()) {
               String outboundMapName = hostname + ":"
                     + outboundMap.getMapName();
               wSetBgpExportPolicy.append(hostname + "|" + neighborPrefixStart
                     + "|" + neighborPrefixEnd + "|" + neighborPrefixLength
                     + "|" + outboundMapName + "\n");
            }
         }
      }
   }

   private void writeBgpNeighbors() {
      StringBuilder wSetBgpNeighborIp = _factBins
            .get("SetBgpNeighborNetwork_flat");
      StringBuilder wSetLocalAs = _factBins.get("SetLocalAs_flat");
      StringBuilder wSetRemoteAs = _factBins.get("SetRemoteAs_flat");
      StringBuilder wSetBgpNeighborDefaultMetric = _factBins
            .get("SetBgpNeighborDefaultMetric_flat");
      StringBuilder wSetBgpNeighborSendCommunity = _factBins
            .get("SetBgpNeighborSendCommunity_flat");
      String hostname = _configuration.getHostname();
      BgpProcess proc = _configuration.getBgpProcess();
      if (proc != null) {
         for (BgpNeighbor neighbor : proc.getNeighbors().values()) {
            int remoteAs = neighbor.getRemoteAs();
            int localAs = neighbor.getLocalAs();
            int defaultMetric = neighbor.getDefaultMetric();
            Prefix neighborPrefix = neighbor.getPrefix();
            long neighborPrefixStart = neighborPrefix.getAddress().asLong();
            long neighborPrefixEnd = neighborPrefix.getEndAddress().asLong();
            int neighborPrefixLength = neighborPrefix.getPrefixLength();
            wSetBgpNeighborIp.append(hostname + "|" + neighborPrefixStart + "|"
                  + neighborPrefixEnd + "|" + neighborPrefixLength + "\n");
            wSetLocalAs.append(hostname + "|" + neighborPrefixStart + "|"
                  + neighborPrefixEnd + "|" + neighborPrefixLength + "|"
                  + localAs + "\n");
            wSetRemoteAs.append(hostname + "|" + neighborPrefixStart + "|"
                  + neighborPrefixEnd + "|" + neighborPrefixLength + "|"
                  + remoteAs + "\n");
            wSetBgpNeighborDefaultMetric.append(hostname + "|"
                  + neighborPrefixStart + "|" + neighborPrefixEnd + "|"
                  + neighborPrefixLength + "|" + defaultMetric + "\n");
            if (neighbor.getSendCommunity()) {
               wSetBgpNeighborSendCommunity.append(hostname + "|"
                     + neighborPrefixStart + "|" + neighborPrefixEnd + "|"
                     + neighborPrefixLength + "\n");
            }
         }
      }
   }

   private void writeBgpOriginationPolicies() {
      StringBuilder wSetBgpOriginationPolicy = _factBins
            .get("SetBgpOriginationPolicy_flat");
      String hostname = _configuration.getHostname();
      BgpProcess proc = _configuration.getBgpProcess();
      if (proc != null) {
         for (BgpNeighbor neighbor : proc.getNeighbors().values()) {
            Prefix neighborPrefix = neighbor.getPrefix();
            long neighborPrefixStart = neighborPrefix.getAddress().asLong();
            long neighborPrefixEnd = neighborPrefix.getEndAddress().asLong();
            int neighborPrefixLength = neighborPrefix.getPrefixLength();
            for (PolicyMap originationPolicy : neighbor
                  .getOriginationPolicies()) {
               String policyName = hostname + ":"
                     + originationPolicy.getMapName();
               wSetBgpOriginationPolicy.append(hostname + "|"
                     + neighborPrefixStart + "|" + neighborPrefixEnd + "|"
                     + neighborPrefixLength + "|" + policyName + "\n");
            }
         }
      }
   }

   private void writeCommunityLists() {
      StringBuilder wSetCommunityListLine = _factBins
            .get("SetCommunityListLine");
      StringBuilder wSetCommunityListLinePermit = _factBins
            .get("SetCommunityListLinePermit");
      String hostname = _configuration.getHostname();
      for (CommunityList list : _configuration.getCommunityLists().values()) {
         String listName = hostname + ":" + list.getName();
         List<CommunityListLine> lineList = list.getLines();
         for (int i = 0; i < lineList.size(); i++) {
            CommunityListLine line = lineList.get(i);
            for (Long community : line.getMatchingCommunities(_allCommunities)) {
               wSetCommunityListLine.append(listName + "|" + i + "|"
                     + community + "\n");
            }
            switch (line.getAction()) {
            case ACCEPT:
               wSetCommunityListLinePermit.append(listName + "|" + i + "\n");
               break;

            case REJECT:
               break;

            default:
               throw new BatfishException("bad action");
            }
         }
      }
   }

   public void writeFacts() {
      writeRoles();
      writeVendor();
      writeInterfaces();
      writeIpAccessLists();
      writeSetActiveInt();
      writeSetIpInt();
      writeSwitchportSettings();
      writeOspfInterfaces();
      writeStaticRoutes();
      writeBgpNeighborPolicies();
      writeOspfOutboundPolicyMaps();
      writeOspfGeneratedRoutes();
      writeRouteReflectorClients();
      writeOspfRouterId();
      writeLinkLoadLimits();
      writePolicyMaps();
      writeBgpNeighbors();
      writeRouteFilters();
      writeBgpOriginationPolicies();
      writeCommunityLists();
      writeBgpGeneratedRoutes();
      writeBgpNeighborGeneratedRoutes();
      writeGeneratedRoutes();
      writeVlanInterface();
      writeAsPaths();
      writeIsis();
      writeIsisOutboundPolicyMaps();
      writeIsisGeneratedRoutes();
   }

   private void writeGeneratedRoutes() {
      StringBuilder wSetGeneratedRoute_flat = _factBins
            .get("SetGeneratedRoute_flat");
      StringBuilder wSetGeneratedRouteDiscard_flat = _factBins
            .get("SetGeneratedRouteDiscard_flat");
      StringBuilder wSetGeneratedRouteMetric_flat = _factBins
            .get("SetGeneratedRouteMetric_flat");
      StringBuilder wSetGeneratedRoutePolicy_flat = _factBins
            .get("SetGeneratedRoutePolicy_flat");
      StringBuilder wSetNetwork = _factBins.get("SetNetwork");
      String hostname = _configuration.getHostname();
      for (GeneratedRoute gr : _configuration.getGeneratedRoutes()) {
         long network_start = gr.getPrefix().getAddress().asLong();
         int prefix_length = gr.getPrefix().getPrefixLength();
         long network_end = gr.getPrefix().getEndAddress().asLong();
         wSetGeneratedRoute_flat.append(hostname + "|" + network_start + "|"
               + network_end + "|" + prefix_length + "|"
               + gr.getAdministrativeCost() + "\n");
         wSetNetwork.append(network_start + "|" + network_start + "|"
               + network_end + "|" + prefix_length + "\n");
         Integer metric = gr.getMetric();
         if (gr.getDiscard()) {
            wSetGeneratedRouteDiscard_flat.append(hostname + "|"
                  + network_start + "|" + network_end + "|" + prefix_length
                  + "\n");
         }
         if (metric != null) {
            wSetGeneratedRouteMetric_flat.append(hostname + "|" + network_start
                  + "|" + network_end + "|" + prefix_length + "|" + metric
                  + "\n");
         }
         for (PolicyMap grPolicy : gr.getGenerationPolicies()) {
            String policyName = hostname + ":" + grPolicy.getMapName();
            wSetGeneratedRoutePolicy_flat.append(hostname + "|" + network_start
                  + "|" + network_end + "|" + prefix_length + "|" + policyName
                  + "\n");
         }
      }
   }

   private void writeInterfaces() {
      StringBuilder wSetFakeInterface = _factBins.get("SetFakeInterface");
      StringBuilder wSetFlowSinkInterface = _factBins
            .get("SetFlowSinkInterface");
      StringBuilder wSetOspfInterfaceCost = _factBins
            .get("SetOspfInterfaceCost");
      StringBuilder wSetInterfaceFilterIn = _factBins
            .get("SetInterfaceFilterIn");
      StringBuilder wSetInterfaceFilterOut = _factBins
            .get("SetInterfaceFilterOut");
      StringBuilder wSetInterfaceRoutingPolicy = _factBins
            .get("SetInterfaceRoutingPolicy");
      String hostname = _configuration.getHostname();
      for (Interface i : _configuration.getInterfaces().values()) {
         String interfaceName = i.getName();

         // flow sinks
         if (interfaceName.startsWith(FLOW_SINK_INTERFACE_PREFIX)) {
            wSetFlowSinkInterface.append(hostname + "|" + interfaceName + "\n");
         }

         // fake interfaces
         if (interfaceName.startsWith(FLOW_SINK_INTERFACE_PREFIX)
               || interfaceName.startsWith(Synthesizer.FAKE_INTERFACE_PREFIX)) {
            wSetFakeInterface.append(hostname + "|" + interfaceName + "\n");
         }

         OspfProcess proc = _configuration.getOspfProcess();
         // TODO: support ospf running on vlan interface properly
         if (proc != null) {
            Integer ospfCost = i.getOspfCost();
            if (ospfCost == null) {
               if (interfaceName.startsWith("Vlan")) {
                  // TODO: fix for non-cisco
                  ospfCost = DEFAULT_CISCO_VLAN_OSPF_COST;
               }
               else {
                  ospfCost = Math.max((int) (_configuration.getOspfProcess()
                        .getReferenceBandwidth() / i.getBandwidth()), 1);
               }
            }
            wSetOspfInterfaceCost.append(_configuration.getHostname() + "|"
                  + interfaceName + "|" + ospfCost + "\n");
         }
         IpAccessList incomingFilter = i.getIncomingFilter();
         if (incomingFilter != null) {
            String filterName = hostname + ":" + incomingFilter.getName();
            wSetInterfaceFilterIn.append(hostname + "|" + interfaceName + "|"
                  + filterName + "\n");
         }
         IpAccessList outgoingFilter = i.getOutgoingFilter();
         if (outgoingFilter != null) {
            String filterName = hostname + ":" + outgoingFilter.getName();
            wSetInterfaceFilterOut.append(hostname + "|" + interfaceName + "|"
                  + filterName + "\n");
         }
         PolicyMap routingPolicy = i.getRoutingPolicy();
         if (routingPolicy != null) {
            String policyName = hostname + ":" + routingPolicy.getMapName();
            wSetInterfaceRoutingPolicy.append(hostname + "|" + interfaceName
                  + "|" + policyName + "\n");
         }
      }
   }

   private void writeIpAccessLists() {
      StringBuilder wSetIpAccessListLine_deny = _factBins
            .get("SetIpAccessListLine_deny");
      StringBuilder wSetIpAccessListLine_dstIpRange = _factBins
            .get("SetIpAccessListLine_dstIpRange");
      StringBuilder wSetIpAccessListLine_dstPortRange = _factBins
            .get("SetIpAccessListLine_dstPortRange");
      StringBuilder wSetIpAccessListLine_permit = _factBins
            .get("SetIpAccessListLine_permit");
      StringBuilder wSetIpAccessListLine_protocol = _factBins
            .get("SetIpAccessListLine_protocol");
      StringBuilder wSetIpAccessListLine_srcIpRange = _factBins
            .get("SetIpAccessListLine_srcIpRange");
      StringBuilder wSetIpAccessListLine_srcPortRange = _factBins
            .get("SetIpAccessListLine_srcPortRange");
      for (IpAccessList ipAccessList : _configuration.getIpAccessLists()
            .values()) {
         String name = _configuration.getHostname() + ":"
               + ipAccessList.getName();
         List<IpAccessListLine> lines = ipAccessList.getLines();
         for (int i = 0; i < lines.size(); i++) {
            IpAccessListLine line = lines.get(i);
            String invalidMessage = line.getInvalidMessage();
            if (invalidMessage != null) {
               _w.redFlag("IpAccessList " + name + " line " + i
                     + ": disabled: " + invalidMessage + "\n");
               continue;
            }
            switch (line.getAction()) {
            case ACCEPT:
               wSetIpAccessListLine_permit.append(name + "|" + i + "\n");
               break;

            case REJECT:
               wSetIpAccessListLine_deny.append(name + "|" + i + "\n");
               break;

            default:
               throw new BatfishException("bad action");
            }
            for (Prefix dstIpRange : line.getDestinationIpRanges()) {
               long dstIpStart = dstIpRange.getAddress().asLong();
               long dstIpEnd = dstIpRange.getEndAddress().asLong();
               wSetIpAccessListLine_dstIpRange.append(name + "|" + i + "|"
                     + dstIpStart + "|" + dstIpEnd + "\n");
            }
            for (SubRange dstPortRange : line.getDstPortRanges()) {
               long startPort = dstPortRange.getStart();
               long endPort = dstPortRange.getEnd();
               wSetIpAccessListLine_dstPortRange.append(name + "|" + i + "|"
                     + startPort + "|" + endPort + "\n");
            }
            for (IpProtocol protocol : line.getProtocols()) {
               wSetIpAccessListLine_protocol.append(name + "|" + i + "|"
                     + protocol.number() + "\n");
            }
            for (Prefix srcIpRange : line.getSourceIpRanges()) {
               long srcIpStart = srcIpRange.getAddress().asLong();
               long srcIpEnd = srcIpRange.getEndAddress().asLong();
               wSetIpAccessListLine_srcIpRange.append(name + "|" + i + "|"
                     + srcIpStart + "|" + srcIpEnd + "\n");
            }
            for (SubRange srcPortRange : line.getSrcPortRanges()) {
               long startPort = srcPortRange.getStart();
               long endPort = srcPortRange.getEnd();
               wSetIpAccessListLine_srcPortRange.append(name + "|" + i + "|"
                     + startPort + "|" + endPort + "\n");
            }
         }
      }
   }

   private void writeIsis() {
      StringBuilder wSetIsisL1Node = _factBins.get("SetIsisL1Node");
      StringBuilder wSetIsisL2Node = _factBins.get("SetIsisL2Node");
      StringBuilder wSetIsisArea = _factBins.get("SetIsisArea");
      StringBuilder wSetIsisInterfaceCost = _factBins
            .get("SetIsisInterfaceCost");
      StringBuilder wSetIsisL1PassiveInterface = _factBins
            .get("SetIsisL1PassiveInterface");
      StringBuilder wSetIsisL2PassiveInterface = _factBins
            .get("SetIsisL2PassiveInterface");
      StringBuilder wSetIsisL1ActiveInterface = _factBins
            .get("SetIsisL1ActiveInterface");
      StringBuilder wSetIsisL2ActiveInterface = _factBins
            .get("SetIsisL2ActiveInterface");
      String hostname = _configuration.getHostname();
      IsisProcess proc = _configuration.getIsisProcess();
      if (proc != null) {
         for (Interface iface : _configuration.getInterfaces().values()) {
            IsisInterfaceMode l1Mode = iface.getIsisL1InterfaceMode();
            IsisInterfaceMode l2Mode = iface.getIsisL2InterfaceMode();
            String ifaceName = iface.getName();
            switch (l1Mode) {
            case PASSIVE:
               wSetIsisL1PassiveInterface.append(hostname + "|" + ifaceName
                     + "\n");
            case ACTIVE:
               wSetIsisL1ActiveInterface.append(hostname + "|" + ifaceName
                     + "\n");
               Integer isisCost = iface.getIsisCost();
               if (isisCost == null) {
                  isisCost = IsisProcess.DEFAULT_ISIS_INTERFACE_COST;
               }
               wSetIsisInterfaceCost.append(hostname + "|" + ifaceName + "|"
                     + isisCost + "\n");
               break;
            case UNSET:
               break;
            default:
               throw new BatfishException("Bad IS-IS mode");
            }
            switch (l2Mode) {
            case PASSIVE:
               wSetIsisL2PassiveInterface.append(hostname + "|" + ifaceName
                     + "\n");
            case ACTIVE:
               wSetIsisL2ActiveInterface.append(hostname + "|" + ifaceName
                     + "\n");
               Integer isisCost = iface.getIsisCost();
               if (isisCost == null) {
                  isisCost = IsisProcess.DEFAULT_ISIS_INTERFACE_COST;
               }
               wSetIsisInterfaceCost.append(hostname + "|" + ifaceName + "|"
                     + isisCost + "\n");
               break;
            case UNSET:
               break;
            default:
               throw new BatfishException("Bad IS-IS mode");
            }
         }
         boolean level1 = false;
         boolean level2 = false;
         switch (proc.getLevel()) {
         case LEVEL_1:
            level1 = true;
            break;
         case LEVEL_1_2:
            level1 = true;
            level2 = true;
            break;
         case LEVEL_2:
            level2 = true;
            break;
         default:
            throw new BatfishException("Invalid IS-IS level");
         }
         if (level1) {
            wSetIsisL1Node.append(hostname + "\n");
         }
         if (level2) {
            wSetIsisL2Node.append(hostname + "\n");
         }
         String area = proc.getNetAddress().getAreaIdStr();
         wSetIsisArea.append(hostname + "|" + area + "\n");
      }
   }

   private void writeIsisGeneratedRoutes() {
      StringBuilder wSetIsisGeneratedRoute_flat = _factBins
            .get("SetIsisGeneratedRoute_flat");
      StringBuilder wSetIsisGeneratedRoutePolicy_flat = _factBins
            .get("SetIsisGeneratedRoutePolicy_flat");
      StringBuilder wSetNetwork = _factBins.get("SetNetwork");
      String hostname = _configuration.getHostname();
      IsisProcess proc = _configuration.getIsisProcess();
      if (proc != null) {
         for (GeneratedRoute gr : proc.getGeneratedRoutes()) {
            long network_start = gr.getPrefix().getAddress().asLong();
            int prefix_length = gr.getPrefix().getPrefixLength();
            long network_end = gr.getPrefix().getEndAddress().asLong();
            wSetIsisGeneratedRoute_flat.append(hostname + "|" + network_start
                  + "|" + network_end + "|" + prefix_length + "\n");
            wSetNetwork.append(network_start + "|" + network_start + "|"
                  + network_end + "|" + prefix_length + "\n");
            for (PolicyMap generationPolicy : gr.getGenerationPolicies()) {
               String gpName = hostname + ":" + generationPolicy.getMapName();
               wSetIsisGeneratedRoutePolicy_flat.append(hostname + "|"
                     + network_start + "|" + network_end + "|" + prefix_length
                     + "|" + gpName + "\n");
            }
         }
      }
   }

   private void writeIsisOutboundPolicyMaps() {
      StringBuilder wSetIsisOutboundPolicyMap = _factBins
            .get("SetIsisOutboundPolicyMap");
      StringBuilder wSetPolicyMapIsisExternalRouteType = _factBins
            .get("SetPolicyMapIsisExternalRouteType");
      String hostname = _configuration.getHostname();
      IsisProcess proc = _configuration.getIsisProcess();
      if (proc != null) {
         for (PolicyMap map : proc.getOutboundPolicyMaps()) {
            String mapName = hostname + ":" + map.getMapName();
            wSetIsisOutboundPolicyMap.append(hostname + "|" + mapName + "\n");
            IsisLevel exportLevel = proc.getPolicyExportLevels().get(map);
            if (exportLevel == null) {
               continue;
            }
            Set<String> levels = new HashSet<String>();
            switch (exportLevel) {
            case LEVEL_1:
               levels.add(RoutingProtocol.ISIS_L1.protocolName());
               break;
            case LEVEL_2:
               levels.add(RoutingProtocol.ISIS_L2.protocolName());
               break;
            case LEVEL_1_2:
               levels.add(RoutingProtocol.ISIS_L1.protocolName());
               levels.add(RoutingProtocol.ISIS_L2.protocolName());
               break;
            default:
               throw new BatfishException("invalid IS-IS level");
            }
            for (String level : levels) {
               wSetPolicyMapIsisExternalRouteType.append(mapName + "|" + level
                     + "\n");
            }
         }
      }
   }

   private void writeLinkLoadLimits() {
      StringBuilder wSetLinkLoadLimitIn = _factBins.get("SetLinkLoadLimitIn");
      StringBuilder wSetLinkLoadLimitOut = _factBins.get("SetLinkLoadLimitOut");
      String hostname = _configuration.getHostname();
      for (Interface iface : _configuration.getInterfaces().values()) {
         if (iface.getName().startsWith("Vlan")) { // TODO: deal with vlans
            continue;
         }
         double limit = iface.getBandwidth();
         String interfaceName = iface.getName();
         wSetLinkLoadLimitIn.append(hostname + "|" + interfaceName + "|"
               + limit + "\n");
         wSetLinkLoadLimitOut.append(hostname + "|" + interfaceName + "|"
               + limit + "\n");
      }
   }

   private void writeOspfGeneratedRoutes() {
      StringBuilder wSetOspfGeneratedRoute_flat = _factBins
            .get("SetOspfGeneratedRoute_flat");
      StringBuilder wSetOspfGeneratedRoutePolicy_flat = _factBins
            .get("SetOspfGeneratedRoutePolicy_flat");
      StringBuilder wSetNetwork = _factBins.get("SetNetwork");
      String hostname = _configuration.getHostname();
      OspfProcess proc = _configuration.getOspfProcess();
      if (proc != null) {
         for (GeneratedRoute gr : proc.getGeneratedRoutes()) {
            long network_start = gr.getPrefix().getAddress().asLong();
            int prefix_length = gr.getPrefix().getPrefixLength();
            long network_end = gr.getPrefix().getEndAddress().asLong();
            wSetOspfGeneratedRoute_flat.append(hostname + "|" + network_start
                  + "|" + network_end + "|" + prefix_length + "\n");
            wSetNetwork.append(network_start + "|" + network_start + "|"
                  + network_end + "|" + prefix_length + "\n");
            for (PolicyMap generationPolicy : gr.getGenerationPolicies()) {
               String gpName = hostname + ":" + generationPolicy.getMapName();
               wSetOspfGeneratedRoutePolicy_flat.append(hostname + "|"
                     + network_start + "|" + network_end + "|" + prefix_length
                     + "|" + gpName + "\n");
            }
         }
      }
   }

   private void writeOspfInterfaces() {
      StringBuilder wSetOspfInterface = _factBins.get("SetOspfInterface");
      String hostname = _configuration.getHostname();
      OspfProcess proc = _configuration.getOspfProcess();
      if (proc != null) {
         for (OspfArea area : proc.getAreas().values()) {
            for (Interface i : area.getInterfaces()) {
               String interfaceName = i.getName();
               wSetOspfInterface.append(hostname + "|" + interfaceName + "|"
                     + area.getNumber() + "\n");
            }
         }
      }
   }

   private void writeOspfOutboundPolicyMaps() {
      StringBuilder wSetOspfOutboundPolicyMap = _factBins
            .get("SetOspfOutboundPolicyMap");
      StringBuilder wSetPolicyMapOspfExternalRouteType = _factBins
            .get("SetPolicyMapOspfExternalRouteType");
      String hostname = _configuration.getHostname();
      OspfProcess proc = _configuration.getOspfProcess();
      if (proc != null) {
         for (PolicyMap map : proc.getOutboundPolicyMaps()) {
            String mapName = hostname + ":" + map.getMapName();
            OspfMetricType metricType = proc.getPolicyMetricTypes().get(map);
            String protocol = null;
            switch (metricType) {
            case E1:
               protocol = "ospfE1";
               break;
            case E2:
               protocol = "ospfE2";
               break;
            default:
               throw new BatfishException("invalid ospf metric type");
            }
            wSetOspfOutboundPolicyMap.append(hostname + "|" + mapName + "\n");
            wSetPolicyMapOspfExternalRouteType.append(mapName + "|" + protocol
                  + "\n");
         }
      }
   }

   private void writeOspfRouterId() {
      StringBuilder wSetOspfRouterId = _factBins.get("SetOspfRouterId");
      String hostname = _configuration.getHostname();
      OspfProcess proc = _configuration.getOspfProcess();
      if (proc != null) {
         Ip id = proc.getRouterId();
         if (id != null) {
            wSetOspfRouterId.append(hostname + "|" + id.asLong() + "\n");
         }
      }
   }

   private void writePolicyMaps() {
      StringBuilder wSetPolicyMapClauseAddCommunity = _factBins
            .get("SetPolicyMapClauseAddCommunity");
      StringBuilder wSetPolicyMapClauseDeleteCommunity = _factBins
            .get("SetPolicyMapClauseDeleteCommunity");
      StringBuilder wSetPolicyMapClauseDeny = _factBins
            .get("SetPolicyMapClauseDeny");
      StringBuilder wSetPolicyMapClauseMatchAsPath = _factBins
            .get("SetPolicyMapClauseMatchAsPath");
      StringBuilder wSetPolicyMapClauseMatchAcl = _factBins
            .get("SetPolicyMapClauseMatchAcl");
      StringBuilder wSetPolicyMapClauseMatchCommunityList = _factBins
            .get("SetPolicyMapClauseMatchCommunityList");
      StringBuilder wSetPolicyMapClauseMatchNeighbor = _factBins
            .get("SetPolicyMapClauseMatchNeighbor");
      StringBuilder wSetPolicyMapClauseMatchPolicy = _factBins
            .get("SetPolicyMapClauseMatchPolicy");
      StringBuilder wSetPolicyMapClauseMatchProtocol = _factBins
            .get("SetPolicyMapClauseMatchProtocol");
      StringBuilder wSetPolicyMapClauseMatchRouteFilter = _factBins
            .get("SetPolicyMapClauseMatchRouteFilter");
      StringBuilder wSetPolicyMapClausePermit = _factBins
            .get("SetPolicyMapClausePermit");
      StringBuilder wSetPolicyMapClauseSetCommunity = _factBins
            .get("SetPolicyMapClauseSetCommunity");
      StringBuilder wSetPolicyMapClauseSetCommunityNone = _factBins
            .get("SetPolicyMapClauseSetCommunityNone");
      StringBuilder wSetPolicyMapClauseSetLocalPreference = _factBins
            .get("SetPolicyMapClauseSetLocalPreference");
      StringBuilder wSetPolicyMapClauseSetMetric = _factBins
            .get("SetPolicyMapClauseSetMetric");
      StringBuilder wSetPolicyMapClauseMatchTag = _factBins
            .get("SetPolicyMapClauseMatchTag");
      StringBuilder wSetPolicyMapClauseSetNextHopIp = _factBins
            .get("SetPolicyMapClauseSetNextHopIp");
      StringBuilder wSetPolicyMapClauseSetOriginType = _factBins
            .get("SetPolicyMapClauseSetOriginType");
      StringBuilder wSetPolicyMapClauseSetProtocol = _factBins
            .get("SetPolicyMapClauseSetProtocol");
      StringBuilder wSetPolicyMapClauseMatchColor = _factBins
            .get("SetPolicyMapClauseMatchColor");
      StringBuilder wSetPolicyMapClauseMatchInterface = _factBins
            .get("SetPolicyMapClauseMatchInterface");
      String hostname = _configuration.getHostname();
      for (PolicyMap map : _configuration.getPolicyMaps().values()) {
         String mapName = hostname + ":" + map.getMapName();
         List<PolicyMapClause> clauses = map.getClauses();
         for (int i = 0; i < clauses.size(); i++) {
            PolicyMapClause clause = clauses.get(i);
            PolicyMapAction action = clause.getAction();
            if (action == null) {
               _w.redFlag("missing action for policy map: \"" + mapName
                     + "\", clause: " + i);
            }
            else {
               // match lines
               // TODO: complete
               switch (action) {
               case DENY:
                  wSetPolicyMapClauseDeny.append(mapName + "|" + i + "\n");
                  break;
               case PERMIT:
                  wSetPolicyMapClausePermit.append(mapName + "|" + i + "\n");
                  break;
               default:
                  throw new BatfishException("invalid action");
               }
            }
            for (PolicyMapMatchLine matchLine : clause.getMatchLines()) {
               switch (matchLine.getType()) {

               case AS_PATH_ACCESS_LIST:
                  PolicyMapMatchAsPathAccessListLine matchAsPathLine = (PolicyMapMatchAsPathAccessListLine) matchLine;
                  for (AsPathAccessList asPath : matchAsPathLine.getLists()) {
                     String asPathName = hostname + ":" + asPath.getName();
                     wSetPolicyMapClauseMatchAsPath.append(mapName + "|" + i
                           + "|" + asPathName + "\n");
                  }
                  break;

               case COMMUNITY_LIST:
                  PolicyMapMatchCommunityListLine mclLine = (PolicyMapMatchCommunityListLine) matchLine;
                  for (CommunityList cList : mclLine.getLists()) {
                     String cListName = hostname + ":" + cList.getName();
                     wSetPolicyMapClauseMatchCommunityList.append(mapName + "|"
                           + i + "|" + cListName + "\n");
                  }
                  break;

               case IP_ACCESS_LIST:
                  PolicyMapMatchIpAccessListLine mialLine = (PolicyMapMatchIpAccessListLine) matchLine;
                  for (IpAccessList list : mialLine.getLists()) {
                     String listName = hostname + ":" + list.getName();
                     wSetPolicyMapClauseMatchAcl.append(mapName + "|" + i + "|"
                           + listName + "\n");
                  }
                  break;

               case NEIGHBOR:
                  PolicyMapMatchNeighborLine pmmnl = (PolicyMapMatchNeighborLine) matchLine;
                  long neighborIp = pmmnl.getNeighborIp().asLong();
                  wSetPolicyMapClauseMatchNeighbor.append(mapName + "|" + i
                        + "|" + neighborIp + "\n");
                  break;

               case POLICY:
                  PolicyMapMatchPolicyLine matchPolicyLine = (PolicyMapMatchPolicyLine) matchLine;
                  PolicyMap policy = matchPolicyLine.getPolicy();
                  String policyName = hostname + ":" + policy.getMapName();
                  wSetPolicyMapClauseMatchPolicy.append(mapName + "|" + i + "|"
                        + policyName + "\n");
                  break;

               case PROTOCOL:
                  PolicyMapMatchProtocolLine pmmpl = (PolicyMapMatchProtocolLine) matchLine;
                  RoutingProtocol prot = pmmpl.getProtocol();
                  wSetPolicyMapClauseMatchProtocol.append(mapName + "|" + i
                        + "|" + prot.protocolName() + "\n");
                  break;

               case ROUTE_FILTER_LIST:
                  PolicyMapMatchRouteFilterListLine mrfLine = (PolicyMapMatchRouteFilterListLine) matchLine;
                  for (RouteFilterList rfList : mrfLine.getLists()) {
                     String rflName = hostname + ":" + rfList.getName();
                     wSetPolicyMapClauseMatchRouteFilter.append(mapName + "|"
                           + i + "|" + rflName + "\n");
                  }
                  break;

               case TAG:
                  PolicyMapMatchTagLine pmmtl = (PolicyMapMatchTagLine) matchLine;
                  for (Integer tag : pmmtl.getTags()) {
                     wSetPolicyMapClauseMatchTag.append(mapName + "|" + i + "|"
                           + tag + "\n");
                  }
                  break;

               case COLOR:
                  PolicyMapMatchColorLine pmmcl = (PolicyMapMatchColorLine) matchLine;
                  int color = pmmcl.getColor();
                  wSetPolicyMapClauseMatchColor.append(mapName + "|" + i + "|"
                        + color + "\n");
                  break;

               case INTERFACE:
                  PolicyMapClauseMatchInterfaceLine pmmil = (PolicyMapClauseMatchInterfaceLine) matchLine;
                  String ifaceName = pmmil.getName();
                  wSetPolicyMapClauseMatchInterface.append(mapName + "|" + i
                        + "|" + ifaceName + "\n");
                  break;

               default:
                  throw new BatfishException("invalid match type");
               }
            }

            // set lines
            for (PolicyMapSetLine setLine : clause.getSetLines()) {
               switch (setLine.getType()) {
               case ADDITIVE_COMMUNITY:
                  PolicyMapSetAddCommunityLine sacLine = (PolicyMapSetAddCommunityLine) setLine;
                  for (Long community : sacLine.getCommunities()) {
                     wSetPolicyMapClauseAddCommunity.append(mapName + "|" + i
                           + "|" + community + "\n");
                  }
                  break;

               case AS_PATH_PREPEND:
                  // TODO: implement
                  // throw new BatfishException("not implemented");
                  _w.unimplemented(mapName + ":" + i
                        + ": AS_PATH_PREPEND not implemented");
                  break;

               case COMMUNITY:
                  PolicyMapSetCommunityLine scLine = (PolicyMapSetCommunityLine) setLine;
                  for (Long community : scLine.getCommunities()) {
                     wSetPolicyMapClauseSetCommunity.append(mapName + "|" + i
                           + "|" + community + "\n");
                  }
                  break;

               case COMMUNITY_NONE:
                  wSetPolicyMapClauseSetCommunityNone.append(mapName + "|" + i
                        + "\n");
                  break;

               case DELETE_COMMUNITY:
                  PolicyMapSetDeleteCommunityLine sdcLine = (PolicyMapSetDeleteCommunityLine) setLine;
                  String cListName = hostname + ":"
                        + sdcLine.getList().getName();
                  wSetPolicyMapClauseDeleteCommunity.append(mapName + "|" + i
                        + "|" + cListName + "\n");
                  break;

               case LOCAL_PREFERENCE:
                  PolicyMapSetLocalPreferenceLine pmslp = (PolicyMapSetLocalPreferenceLine) setLine;
                  int localPref = pmslp.getLocalPreference();
                  wSetPolicyMapClauseSetLocalPreference.append(mapName + "|"
                        + i + "|" + localPref + "\n");
                  break;

               case METRIC:
                  PolicyMapSetMetricLine pmsml = (PolicyMapSetMetricLine) setLine;
                  int metric = pmsml.getMetric();
                  wSetPolicyMapClauseSetMetric.append(mapName + "|" + i + "|"
                        + metric + "\n");
                  break;

               case NEXT_HOP:
                  PolicyMapSetNextHopLine pmsnhl = (PolicyMapSetNextHopLine) setLine;
                  for (Ip nextHopIp : pmsnhl.getNextHops()) {
                     wSetPolicyMapClauseSetNextHopIp.append(mapName + "|" + i
                           + "|" + nextHopIp.asLong() + "\n");
                  }
                  break;

               case ORIGIN_TYPE:
                  PolicyMapSetOriginTypeLine pmsotl = (PolicyMapSetOriginTypeLine) setLine;
                  OriginType originType = pmsotl.getOriginType();
                  wSetPolicyMapClauseSetOriginType.append(mapName + "|" + i
                        + "|" + originType.toString() + "\n");
                  break;

               case LEVEL:
                  PolicyMapSetLevelLine pmspl = (PolicyMapSetLevelLine) setLine;
                  IsisLevel level = pmspl.getLevel();
                  boolean level1 = false;
                  boolean level2 = false;
                  switch (level) {
                  case LEVEL_1:
                     level1 = true;
                     break;
                  case LEVEL_1_2:
                     level1 = true;
                     level2 = true;
                     break;
                  case LEVEL_2:
                     level2 = true;
                     break;
                  default:
                     throw new BatfishException("Invalid level");
                  }
                  if (level1) {
                     wSetPolicyMapClauseSetProtocol.append(mapName + "|" + i
                           + "|" + RoutingProtocol.ISIS_L1.protocolName()
                           + "\n");
                  }
                  if (level2) {
                     wSetPolicyMapClauseSetProtocol.append(mapName + "|" + i
                           + "|" + RoutingProtocol.ISIS_L2.protocolName()
                           + "\n");
                  }
                  break;

               default:
                  throw new BatfishException("invalid set type");
               }
            }
         }

      }
   }

   private void writeRoles() {
      StringBuilder wSetNodeRole = _factBins.get("SetNodeRole");
      String hostname = _configuration.getHostname();
      RoleSet roles = _configuration.getRoles();
      if (roles != null) {
         for (String role : _configuration.getRoles()) {
            wSetNodeRole.append(hostname + "|" + role + "\n");
         }
      }
   }

   private void writeRouteFilters() {
      StringBuilder wSetRouteFilterLine = _factBins.get("SetRouteFilterLine");
      StringBuilder wSetRouteFilterPermitLine = _factBins
            .get("SetRouteFilterPermitLine");
      String hostname = _configuration.getHostname();
      for (RouteFilterList filter : _configuration.getRouteFilterLists()
            .values()) {
         String filterName = hostname + ":" + filter.getName();
         List<RouteFilterLine> lines = filter.getLines();
         for (int i = 0; i < lines.size(); i++) {
            RouteFilterLine line = lines.get(i);
            Long network_start = line.getPrefix().getAddress().asLong();
            Long network_end = line.getPrefix().getEndAddress().asLong();
            SubRange prefixRange = line.getLengthRange();
            long min_prefix = prefixRange.getStart();
            long max_prefix = prefixRange.getEnd();
            wSetRouteFilterLine.append(filterName + "|" + i + "|"
                  + network_start + "|" + network_end + "|" + min_prefix + "|"
                  + max_prefix + "\n");
            switch (line.getAction()) {
            case ACCEPT:
               wSetRouteFilterPermitLine.append(filterName + "|" + i + "\n");
               break;

            case REJECT:
               break;

            default:
               throw new BatfishException("bad action");
            }
         }
      }
   }

   private void writeRouteReflectorClients() {
      StringBuilder wSetRouteReflectorClient = _factBins
            .get("SetRouteReflectorClient_flat");
      String hostname = _configuration.getHostname();
      BgpProcess proc = _configuration.getBgpProcess();
      if (proc != null) {
         for (BgpNeighbor neighbor : proc.getNeighbors().values()) {
            Prefix neighborPrefix = neighbor.getPrefix();
            long neighborPrefixStart = neighborPrefix.getAddress().asLong();
            long neighborPrefixEnd = neighborPrefix.getEndAddress().asLong();
            int neighborPrefixLength = neighborPrefix.getPrefixLength();
            Long clusterId = neighbor.getClusterId();
            if (clusterId == null) {
               continue;
            }
            wSetRouteReflectorClient.append(hostname + "|"
                  + neighborPrefixStart + "|" + neighborPrefixEnd + "|"
                  + neighborPrefixLength + "|" + clusterId + "\n");
         }
      }
   }

   private void writeSetActiveInt() {
      StringBuilder wSetActiveInt = _factBins.get("SetActiveInt");
      String hostname = _configuration.getHostname();
      for (Interface i : _configuration.getInterfaces().values()) {
         String interfaceName = i.getName();
         boolean active = i.getActive();
         if (active) {
            wSetActiveInt.append(hostname + "|" + interfaceName + "\n");
         }
      }
   }

   private void writeSetIpInt() {
      String hostname = _configuration.getHostname();
      StringBuilder wSetNetwork = _factBins.get("SetNetwork");
      StringBuilder wSetIpInt = _factBins.get("SetIpInt");
      for (Interface i : _configuration.getInterfaces().values()) {
         String interfaceName = i.getName();
         Prefix prefix = i.getPrefix();
         if (prefix != null) {
            long address = prefix.getAddress().asLong();
            int prefix_length = prefix.getPrefixLength();
            long network_start = prefix.getNetworkAddress().asLong();
            long network_end = prefix.getEndAddress().asLong();
            wSetNetwork.append(network_start + "|" + network_start + "|"
                  + network_end + "|" + prefix_length + "\n");
            wSetIpInt.append(hostname + "|" + interfaceName + "|" + address
                  + "|" + prefix_length + "\n");
         }
      }
   }

   private void writeStaticRoutes() {
      StringBuilder wSetNetwork = _factBins.get("SetNetwork");
      StringBuilder wSetStaticRoute_flat = _factBins.get("SetStaticRoute_flat");
      StringBuilder wSetStaticIntRoute_flat = _factBins
            .get("SetStaticIntRoute_flat");
      String hostName = _configuration.getHostname();
      for (StaticRoute route : _configuration.getStaticRoutes()) {
         Prefix prefix = route.getPrefix();
         Ip nextHopIp = route.getNextHopIp();
         if (nextHopIp == null) {
            nextHopIp = new Ip(0);
         }
         int prefix_length = prefix.getPrefixLength();
         long network_start = prefix.getAddress().asLong();
         long network_end = prefix.getEndAddress().asLong();
         int distance = route.getAdministrativeCost();
         int tag = route.getTag();
         String nextHopInt = route.getNextHopInterface();
         wSetNetwork.append(network_start + "|" + network_start + "|"
               + network_end + "|" + prefix_length + "\n");
         if (nextHopInt != null) { // use next hop interface instead
            if (Util.isNullInterface(nextHopInt)) {
               nextHopInt = Util.NULL_INTERFACE_NAME;
            }
            wSetStaticIntRoute_flat.append(hostName + "|" + network_start + "|"
                  + network_end + "|" + prefix_length + "|"
                  + nextHopIp.asLong() + "|" + nextHopInt + "|" + distance
                  + "|" + tag + "\n");
         }
         else {
            wSetStaticRoute_flat.append(hostName + "|" + network_start + "|"
                  + network_end + "|" + prefix_length + "|"
                  + nextHopIp.asLong() + "|" + distance + "|" + tag + "\n");
         }
      }
   }

   private void writeSwitchportSettings() {
      StringBuilder wSetSwitchportAccess = _factBins.get("SetSwitchportAccess");
      StringBuilder wSetSwitchportTrunkNative = _factBins
            .get("SetSwitchportTrunkNative");
      StringBuilder wSetSwitchportTrunkAllows = _factBins
            .get("SetSwitchportTrunkAllows");
      StringBuilder wSetSwitchportTrunkEncapsulation = _factBins
            .get("SetSwitchportTrunkEncapsulation");
      String hostname = _configuration.getHostname();
      for (Interface i : _configuration.getInterfaces().values()) {
         String interfaceName = i.getName();
         switch (i.getSwitchportMode()) {
         case ACCESS:
            int vlan = i.getAccessVlan();
            wSetSwitchportAccess.append(hostname + "|" + interfaceName + "|"
                  + vlan + "\n");
            break;

         // TODO: create derived switchport facts in logic and here
         case DYNAMIC_AUTO:
         case DYNAMIC_DESIRABLE:
         case NONE:
            break;

         case TRUNK:
            SwitchportEncapsulationType encapsulation = i
                  .getSwitchportTrunkEncapsulation();
            wSetSwitchportTrunkEncapsulation.append(hostname + "|"
                  + interfaceName + "|"
                  + encapsulation.toString().toLowerCase() + "\n");
            int nativeVlan = i.getNativeVlan();
            wSetSwitchportTrunkNative.append(hostname + "|" + interfaceName
                  + "|" + nativeVlan + "\n");
            for (SubRange range : i.getAllowedVlans()) {
               wSetSwitchportTrunkAllows.append(hostname + "|" + interfaceName
                     + "|" + range.getStart() + "|" + range.getEnd() + "\n");
            }
            break;

         default:
            throw new BatfishException("invalid switchport mode");
         }
      }
   }

   private void writeVendor() {
      String hostname = _configuration.getHostname();
      String vendor = _configuration.getVendor().getVendorString();
      StringBuilder wSetNodeVendor = _factBins.get("SetNodeVendor");
      wSetNodeVendor.append(hostname + "|" + vendor + "\n");
   }

   private void writeVlanInterface() {
      StringBuilder wSetVlanInterface = _factBins.get("SetVlanInterface");
      String hostname = _configuration.getHostname();
      for (String ifaceName : _configuration.getInterfaces().keySet()) {
         Integer vlan = Util.getInterfaceVlanNumber(ifaceName);
         if (vlan != null) {
            wSetVlanInterface.append(hostname + "|" + ifaceName + "|" + vlan
                  + "\n");
         }
      }
   }

}
