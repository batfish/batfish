package org.batfish.nls;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.batfish.common.BatfishException;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.AsPathAccessList;
import org.batfish.datamodel.AsPathAccessListLine;
import org.batfish.datamodel.BgpNeighbor;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.CommunityList;
import org.batfish.datamodel.CommunityListLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.GeneratedRoute;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IsisInterfaceMode;
import org.batfish.datamodel.IsisLevel;
import org.batfish.datamodel.IsisProcess;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.OspfArea;
import org.batfish.datamodel.OspfMetricType;
import org.batfish.datamodel.OspfProcess;
import org.batfish.datamodel.PolicyMap;
import org.batfish.datamodel.PolicyMapAction;
import org.batfish.datamodel.PolicyMapClause;
import org.batfish.datamodel.PolicyMapClauseMatchInterfaceLine;
import org.batfish.datamodel.PolicyMapMatchAsPathAccessListLine;
import org.batfish.datamodel.PolicyMapMatchColorLine;
import org.batfish.datamodel.PolicyMapMatchCommunityListLine;
import org.batfish.datamodel.PolicyMapMatchIpAccessListLine;
import org.batfish.datamodel.PolicyMapMatchLine;
import org.batfish.datamodel.PolicyMapMatchNeighborLine;
import org.batfish.datamodel.PolicyMapMatchPolicyConjunctionLine;
import org.batfish.datamodel.PolicyMapMatchPolicyLine;
import org.batfish.datamodel.PolicyMapMatchProtocolLine;
import org.batfish.datamodel.PolicyMapMatchRouteFilterListLine;
import org.batfish.datamodel.PolicyMapMatchTagLine;
import org.batfish.datamodel.PolicyMapSetAddCommunityLine;
import org.batfish.datamodel.PolicyMapSetCommunityLine;
import org.batfish.datamodel.PolicyMapSetDeleteCommunityLine;
import org.batfish.datamodel.PolicyMapSetLevelLine;
import org.batfish.datamodel.PolicyMapSetLine;
import org.batfish.datamodel.PolicyMapSetLocalPreferenceLine;
import org.batfish.datamodel.PolicyMapSetMetricLine;
import org.batfish.datamodel.PolicyMapSetNextHopLine;
import org.batfish.datamodel.PolicyMapSetOriginTypeLine;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.State;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.SwitchportEncapsulationType;
import org.batfish.datamodel.TcpFlags;
import org.batfish.datamodel.Zone;
import org.batfish.datamodel.collections.RoleSet;
import org.batfish.main.Warnings;

public class ConfigurationFactExtractor {

   private static final int DEFAULT_CISCO_VLAN_OSPF_COST = 1;

   private final Set<Long> _allCommunities;

   private final Configuration _configuration;

   private final Map<String, StringBuilder> _factBins;

   private final Warnings _w;

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
                  wSetAsPathLineMatchAsPairAtBeginning
                        .append(asPathName + "|" + i + "|" + as1Low + "|"
                              + as1High + "|" + as2Low + "|" + as2High + "\n");
               }
               else {
                  wSetAsPathLineMatchAsPair
                        .append(asPathName + "|" + i + "|" + as1Low + "|"
                              + as1High + "|" + as2Low + "|" + as2High + "\n");
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
      StringBuilder wSetBgpGeneratedRouteAttributePolicy_flat = _factBins
            .get("SetBgpGeneratedRouteAttributePolicy_flat");
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
            for (PolicyMap attributePolicy : gr.getAttributePolicies()
                  .values()) {
               String apName = hostname + ":" + attributePolicy.getName();
               wSetBgpGeneratedRouteAttributePolicy_flat
                     .append(hostname + "|" + network_start + "|" + network_end
                           + "|" + prefix_length + "|" + apName + "\n");
            }
            for (PolicyMap generationPolicy : gr.getGenerationPolicies()) {
               String gpName = hostname + ":" + generationPolicy.getName();
               wSetBgpGeneratedRoutePolicy_flat
                     .append(hostname + "|" + network_start + "|" + network_end
                           + "|" + prefix_length + "|" + gpName + "\n");
            }
         }
      }
   }

   private void writeBgpNeighborGeneratedRoutes() {
      String hostname = _configuration.getHostname();
      BgpProcess proc = _configuration.getBgpProcess();
      StringBuilder wSetBgpNeighborGeneratedRoute_flat = _factBins
            .get("SetBgpNeighborGeneratedRoute_flat");
      StringBuilder wSetBgpNeighborGeneratedRouteAttributePolicy_flat = _factBins
            .get("SetBgpNeighborGeneratedRouteAttributePolicy_flat");
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
               for (PolicyMap attributePolicy : gr.getAttributePolicies()
                     .values()) {
                  String apName = hostname + ":" + attributePolicy.getName();
                  wSetBgpNeighborGeneratedRouteAttributePolicy_flat
                        .append(hostname + "|" + neighborPrefixStart + "|"
                              + neighborPrefixEnd + "|" + neighborPrefixLength
                              + "|" + network_start + "|" + network_end + "|"
                              + prefix_length + "|" + apName + "\n");
               }
               for (PolicyMap generationPolicy : gr.getGenerationPolicies()) {
                  String gpName = hostname + ":" + generationPolicy.getName();
                  wSetBgpNeighborGeneratedRoutePolicy_flat
                        .append(hostname + "|" + neighborPrefixStart + "|"
                              + neighborPrefixEnd + "|" + neighborPrefixLength
                              + "|" + network_start + "|" + network_end + "|"
                              + prefix_length + "|" + gpName + "\n");
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
               String inboundMapName = hostname + ":" + inboundMap.getName();
               wSetBgpImportPolicy.append(hostname + "|" + neighborPrefixStart
                     + "|" + neighborPrefixEnd + "|" + neighborPrefixLength
                     + "|" + inboundMapName + "\n");
            }
            for (PolicyMap outboundMap : neighbor.getOutboundPolicyMaps()) {
               String outboundMapName = hostname + ":" + outboundMap.getName();
               wSetBgpExportPolicy.append(hostname + "|" + neighborPrefixStart
                     + "|" + neighborPrefixEnd + "|" + neighborPrefixLength
                     + "|" + outboundMapName + "\n");
            }
         }
      }
   }

   private void writeBgpNeighbors() {
      StringBuilder wSetBgpNeighborLocalIp_flat = _factBins
            .get("SetBgpNeighborLocalIp_flat");
      StringBuilder wSetBgpNeighborNetwork_flat = _factBins
            .get("SetBgpNeighborNetwork_flat");
      StringBuilder wSetBgpMultihopNeighborNetwork_flat = _factBins
            .get("SetBgpMultihopNeighborNetwork_flat");
      StringBuilder wSetLocalAs_flat = _factBins.get("SetLocalAs_flat");
      StringBuilder wSetRemoteAs_flat = _factBins.get("SetRemoteAs_flat");
      StringBuilder wSetBgpNeighborAdvertiseInactive_flat = _factBins
            .get("SetBgpNeighborAdvertiseInactive_flat");
      StringBuilder wSetBgpNeighborAllowLocalAsIn_flat = _factBins
            .get("SetBgpNeighborAllowLocalAsIn_flat");
      StringBuilder wSetBgpNeighborAllowRemoteAsOut_flat = _factBins
            .get("SetBgpNeighborAllowRemoteAsOut_flat");
      StringBuilder wSetBgpNeighborDefaultMetric_flat = _factBins
            .get("SetBgpNeighborDefaultMetric_flat");
      StringBuilder wSetBgpNeighborSendCommunity_flat = _factBins
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
            wSetBgpNeighborNetwork_flat.append(
                  hostname + "|" + neighborPrefixStart + "|" + neighborPrefixEnd
                        + "|" + neighborPrefixLength + "\n");
            Ip localIp = neighbor.getLocalIp();
            if (localIp != null) {
               long localIpAsLong = localIp.asLong();
               wSetBgpNeighborLocalIp_flat.append(hostname + "|"
                     + neighborPrefixStart + "|" + neighborPrefixEnd + "|"
                     + neighborPrefixLength + "|" + localIpAsLong + "\n");
            }
            if (neighbor.getEbgpMultihop()) {
               wSetBgpMultihopNeighborNetwork_flat.append(hostname + "|"
                     + neighborPrefixStart + "|" + neighborPrefixEnd + "|"
                     + neighborPrefixLength + "\n");
            }
            if (neighbor.getAdvertiseInactive()) {
               wSetBgpNeighborAdvertiseInactive_flat.append(hostname + "|"
                     + neighborPrefixStart + "|" + neighborPrefixEnd + "|"
                     + neighborPrefixLength + "\n");
            }
            if (neighbor.getAllowLocalAsIn()) {
               wSetBgpNeighborAllowLocalAsIn_flat.append(hostname + "|"
                     + neighborPrefixStart + "|" + neighborPrefixEnd + "|"
                     + neighborPrefixLength + "\n");
            }
            if (neighbor.getAllowRemoteAsOut()) {
               wSetBgpNeighborAllowRemoteAsOut_flat.append(hostname + "|"
                     + neighborPrefixStart + "|" + neighborPrefixEnd + "|"
                     + neighborPrefixLength + "\n");
            }
            wSetLocalAs_flat.append(
                  hostname + "|" + neighborPrefixStart + "|" + neighborPrefixEnd
                        + "|" + neighborPrefixLength + "|" + localAs + "\n");
            wSetRemoteAs_flat.append(
                  hostname + "|" + neighborPrefixStart + "|" + neighborPrefixEnd
                        + "|" + neighborPrefixLength + "|" + remoteAs + "\n");
            wSetBgpNeighborDefaultMetric_flat.append(hostname + "|"
                  + neighborPrefixStart + "|" + neighborPrefixEnd + "|"
                  + neighborPrefixLength + "|" + defaultMetric + "\n");
            if (neighbor.getSendCommunity()) {
               wSetBgpNeighborSendCommunity_flat.append(hostname + "|"
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
               String policyName = hostname + ":" + originationPolicy.getName();
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
            for (Long community : line
                  .getMatchingCommunities(_allCommunities)) {
               wSetCommunityListLine
                     .append(listName + "|" + i + "|" + community + "\n");
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
      writeZoneFacts();
   }

   private void writeGeneratedRoutes() {
      StringBuilder wSetGeneratedRoute_flat = _factBins
            .get("SetGeneratedRoute_flat");
      StringBuilder wSetGeneratedRouteDiscard_flat = _factBins
            .get("SetGeneratedRouteDiscard_flat");
      StringBuilder wSetGeneratedRouteMetric_flat = _factBins
            .get("SetGeneratedRouteMetric_flat");
      StringBuilder wSetGeneratedRouteAttributePolicy_flat = _factBins
            .get("SetGeneratedRouteAttributePolicy_flat");
      StringBuilder wSetGeneratedRoutePolicy_flat = _factBins
            .get("SetGeneratedRoutePolicy_flat");
      StringBuilder wSetNetwork = _factBins.get("SetNetwork");
      String hostname = _configuration.getHostname();
      for (GeneratedRoute gr : _configuration.getGeneratedRoutes()) {
         long network_start = gr.getPrefix().getAddress().asLong();
         int prefix_length = gr.getPrefix().getPrefixLength();
         long network_end = gr.getPrefix().getEndAddress().asLong();
         wSetGeneratedRoute_flat
               .append(hostname + "|" + network_start + "|" + network_end + "|"
                     + prefix_length + "|" + gr.getAdministrativeCost() + "\n");
         wSetNetwork.append(network_start + "|" + network_start + "|"
               + network_end + "|" + prefix_length + "\n");
         Integer metric = gr.getMetric();
         if (gr.getDiscard()) {
            wSetGeneratedRouteDiscard_flat.append(hostname + "|" + network_start
                  + "|" + network_end + "|" + prefix_length + "\n");
         }
         if (metric != null) {
            wSetGeneratedRouteMetric_flat
                  .append(hostname + "|" + network_start + "|" + network_end
                        + "|" + prefix_length + "|" + metric + "\n");
         }
         for (PolicyMap attributePolicy : gr.getAttributePolicies().values()) {
            String policyName = hostname + ":" + attributePolicy.getName();
            wSetGeneratedRouteAttributePolicy_flat
                  .append(hostname + "|" + network_start + "|" + network_end
                        + "|" + prefix_length + "|" + policyName + "\n");
         }
         for (PolicyMap grPolicy : gr.getGenerationPolicies()) {
            String policyName = hostname + ":" + grPolicy.getName();
            wSetGeneratedRoutePolicy_flat
                  .append(hostname + "|" + network_start + "|" + network_end
                        + "|" + prefix_length + "|" + policyName + "\n");
         }
      }
   }

   private void writeInterfaces() {
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
                  if (i.getBandwidth() != null) {
                     ospfCost = Math.max(
                           (int) (_configuration.getOspfProcess()
                                 .getReferenceBandwidth() / i.getBandwidth()),
                           1);
                  }
                  else {
                     throw new BatfishException(
                           "Expected non-null interface bandwidth for \""
                                 + hostname + "\":\"" + interfaceName + "\"");
                  }
               }
            }
            wSetOspfInterfaceCost.append(_configuration.getHostname() + "|"
                  + interfaceName + "|" + ospfCost + "\n");
         }
         IpAccessList incomingFilter = i.getIncomingFilter();
         if (incomingFilter != null) {
            String filterName = hostname + ":" + incomingFilter.getName();
            wSetInterfaceFilterIn.append(
                  hostname + "|" + interfaceName + "|" + filterName + "\n");
         }
         IpAccessList outgoingFilter = i.getOutgoingFilter();
         if (outgoingFilter != null) {
            String filterName = hostname + ":" + outgoingFilter.getName();
            wSetInterfaceFilterOut.append(
                  hostname + "|" + interfaceName + "|" + filterName + "\n");
         }
         PolicyMap routingPolicy = i.getRoutingPolicy();
         if (routingPolicy != null) {
            String policyName = hostname + ":" + routingPolicy.getName();
            wSetInterfaceRoutingPolicy.append(
                  hostname + "|" + interfaceName + "|" + policyName + "\n");
         }
      }
   }

   private void writeIpAccessLists() {
      StringBuilder wSetIpAccessListLine_deny = _factBins
            .get("SetIpAccessListLine_deny");
      StringBuilder wSetIpAccessListLine_dstIps = _factBins
            .get("SetIpAccessListLine_dstIps");
      StringBuilder wSetIpAccessListLine_dstPorts = _factBins
            .get("SetIpAccessListLine_dstPorts");
      StringBuilder wSetIpAccessListLine_dscp = _factBins
            .get("SetIpAccessListLine_dscp");
      StringBuilder wSetIpAccessListLine_ecn = _factBins
            .get("SetIpAccessListLine_ecn");
      StringBuilder wSetIpAccessListLine_icmpCodes = _factBins
            .get("SetIpAccessListLine_icmpCodes");
      StringBuilder wSetIpAccessListLine_icmpTypes = _factBins
            .get("SetIpAccessListLine_icmpTypes");
      StringBuilder wSetIpAccessListLine_notDscp = _factBins
            .get("SetIpAccessListLine_notDscp");
      StringBuilder wSetIpAccessListLine_notEcn = _factBins
            .get("SetIpAccessListLine_notEcn");
      StringBuilder wSetIpAccessListLine_notIcmpCodes = _factBins
            .get("SetIpAccessListLine_notIcmpCodes");
      StringBuilder wSetIpAccessListLine_notIcmpTypes = _factBins
            .get("SetIpAccessListLine_notIcmpTypes");
      StringBuilder wSetIpAccessListLine_notDstIps = _factBins
            .get("SetIpAccessListLine_notDstIps");
      StringBuilder wSetIpAccessListLine_notDstPorts = _factBins
            .get("SetIpAccessListLine_notDstPorts");
      StringBuilder wSetIpAccessListLine_notSrcIps = _factBins
            .get("SetIpAccessListLine_notSrcIps");
      StringBuilder wSetIpAccessListLine_notSrcPorts = _factBins
            .get("SetIpAccessListLine_notSrcPorts");
      StringBuilder wSetIpAccessListLine_notProtocol = _factBins
            .get("SetIpAccessListLine_notProtocol");
      StringBuilder wSetIpAccessListLine_permit = _factBins
            .get("SetIpAccessListLine_permit");
      StringBuilder wSetIpAccessListLine_protocol = _factBins
            .get("SetIpAccessListLine_protocol");
      StringBuilder wSetIpAccessListLine_srcIps = _factBins
            .get("SetIpAccessListLine_srcIps");
      StringBuilder wSetIpAccessListLine_srcOrDstIps = _factBins
            .get("SetIpAccessListLine_srcOrDstIps");
      StringBuilder wSetIpAccessListLine_srcOrDstPorts = _factBins
            .get("SetIpAccessListLine_srcOrDstPorts");
      StringBuilder wSetIpAccessListLine_srcPorts = _factBins
            .get("SetIpAccessListLine_srcPorts");
      StringBuilder wSetIpAccessListLine_state = _factBins
            .get("SetIpAccessListLine_state");
      StringBuilder wSetIpAccessListLine_tcpFlags = _factBins
            .get("SetIpAccessListLine_tcpFlags");
      StringBuilder wSetIpAccessListLine_tcpFlagsCWR = _factBins
            .get("SetIpAccessListLine_tcpFlagsCWR");
      StringBuilder wSetIpAccessListLine_tcpFlagsECE = _factBins
            .get("SetIpAccessListLine_tcpFlagsECE");
      StringBuilder wSetIpAccessListLine_tcpFlagsURG = _factBins
            .get("SetIpAccessListLine_tcpFlagsURG");
      StringBuilder wSetIpAccessListLine_tcpFlagsACK = _factBins
            .get("SetIpAccessListLine_tcpFlagsACK");
      StringBuilder wSetIpAccessListLine_tcpFlagsPSH = _factBins
            .get("SetIpAccessListLine_tcpFlagsPSH");
      StringBuilder wSetIpAccessListLine_tcpFlagsRST = _factBins
            .get("SetIpAccessListLine_tcpFlagsRST");
      StringBuilder wSetIpAccessListLine_tcpFlagsSYN = _factBins
            .get("SetIpAccessListLine_tcpFlagsSYN");
      StringBuilder wSetIpAccessListLine_tcpFlagsFIN = _factBins
            .get("SetIpAccessListLine_tcpFlagsFIN");
      for (IpAccessList ipAccessList : _configuration.getIpAccessLists()
            .values()) {
         String name = _configuration.getHostname() + ":"
               + ipAccessList.getName();
         List<IpAccessListLine> lines = ipAccessList.getLines();
         for (int i = 0; i < lines.size(); i++) {
            IpAccessListLine line = lines.get(i);
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
            for (IpWildcard dstIpWildcard : line.getDstIps()) {
               Prefix dstIps = dstIpWildcard.toPrefix();
               long dstIpStart = dstIps.getAddress().asLong();
               long dstIpEnd = dstIps.getEndAddress().asLong();
               wSetIpAccessListLine_dstIps.append(
                     name + "|" + i + "|" + dstIpStart + "|" + dstIpEnd + "\n");
            }
            for (IpWildcard dstIpWildcard : line.getNotDstIps()) {
               Prefix dstIps = dstIpWildcard.toPrefix();
               long dstIpStart = dstIps.getAddress().asLong();
               long dstIpEnd = dstIps.getEndAddress().asLong();
               wSetIpAccessListLine_notDstIps.append(
                     name + "|" + i + "|" + dstIpStart + "|" + dstIpEnd + "\n");
            }
            for (SubRange dstPorts : line.getDstPorts()) {
               long startPort = dstPorts.getStart();
               long endPort = dstPorts.getEnd();
               wSetIpAccessListLine_dstPorts.append(
                     name + "|" + i + "|" + startPort + "|" + endPort + "\n");
            }
            for (SubRange dstPorts : line.getNotDstPorts()) {
               long startPort = dstPorts.getStart();
               long endPort = dstPorts.getEnd();
               wSetIpAccessListLine_notDstPorts.append(
                     name + "|" + i + "|" + startPort + "|" + endPort + "\n");
            }
            for (SubRange icmpCodeRange : line.getIcmpCodes()) {
               long start = icmpCodeRange.getStart();
               long end = icmpCodeRange.getEnd();
               wSetIpAccessListLine_icmpCodes
                     .append(name + "|" + i + "|" + start + "|" + end + "\n");
            }
            for (SubRange icmpCodeRange : line.getNotIcmpCodes()) {
               long start = icmpCodeRange.getStart();
               long end = icmpCodeRange.getEnd();
               wSetIpAccessListLine_notIcmpCodes
                     .append(name + "|" + i + "|" + start + "|" + end + "\n");
            }
            for (SubRange icmpTypeRange : line.getIcmpTypes()) {
               long start = icmpTypeRange.getStart();
               long end = icmpTypeRange.getEnd();
               wSetIpAccessListLine_icmpTypes
                     .append(name + "|" + i + "|" + start + "|" + end + "\n");
            }
            for (SubRange icmpTypeRange : line.getNotIcmpTypes()) {
               long start = icmpTypeRange.getStart();
               long end = icmpTypeRange.getEnd();
               wSetIpAccessListLine_notIcmpTypes
                     .append(name + "|" + i + "|" + start + "|" + end + "\n");
            }
            for (IpProtocol protocol : line.getIpProtocols()) {
               wSetIpAccessListLine_protocol
                     .append(name + "|" + i + "|" + protocol.number() + "\n");
            }
            for (IpProtocol protocol : line.getNotIpProtocols()) {
               wSetIpAccessListLine_notProtocol
                     .append(name + "|" + i + "|" + protocol.number() + "\n");
            }
            for (IpWildcard srcIpWildcard : line.getSrcIps()) {
               Prefix srcIps = srcIpWildcard.toPrefix();
               long srcIpStart = srcIps.getAddress().asLong();
               long srcIpEnd = srcIps.getEndAddress().asLong();
               wSetIpAccessListLine_srcIps.append(
                     name + "|" + i + "|" + srcIpStart + "|" + srcIpEnd + "\n");
            }
            for (IpWildcard srcIpWildcard : line.getNotSrcIps()) {
               Prefix srcIps = srcIpWildcard.toPrefix();
               long srcIpStart = srcIps.getAddress().asLong();
               long srcIpEnd = srcIps.getEndAddress().asLong();
               wSetIpAccessListLine_notSrcIps.append(
                     name + "|" + i + "|" + srcIpStart + "|" + srcIpEnd + "\n");
            }
            for (IpWildcard srcOrDstIpWildcard : line.getSrcOrDstIps()) {
               Prefix srcOrDstIps = srcOrDstIpWildcard.toPrefix();
               long srcOrDstIpStart = srcOrDstIps.getAddress().asLong();
               long srcOrDstIpEnd = srcOrDstIps.getEndAddress().asLong();
               wSetIpAccessListLine_srcOrDstIps.append(name + "|" + i + "|"
                     + srcOrDstIpStart + "|" + srcOrDstIpEnd + "\n");
            }
            for (SubRange srcOrDstPorts : line.getSrcOrDstPorts()) {
               long startPort = srcOrDstPorts.getStart();
               long endPort = srcOrDstPorts.getEnd();
               wSetIpAccessListLine_srcOrDstPorts.append(
                     name + "|" + i + "|" + startPort + "|" + endPort + "\n");
            }
            for (SubRange srcPorts : line.getSrcPorts()) {
               long startPort = srcPorts.getStart();
               long endPort = srcPorts.getEnd();
               wSetIpAccessListLine_srcPorts.append(
                     name + "|" + i + "|" + startPort + "|" + endPort + "\n");
            }
            for (SubRange srcPorts : line.getNotSrcPorts()) {
               long startPort = srcPorts.getStart();
               long endPort = srcPorts.getEnd();
               wSetIpAccessListLine_notSrcPorts.append(
                     name + "|" + i + "|" + startPort + "|" + endPort + "\n");
            }
            for (State state : line.getStates()) {
               long stateNum = state.number();
               wSetIpAccessListLine_state
                     .append(name + "|" + i + "|" + stateNum + "\n");
            }
            for (int alt = 0; alt < line.getTcpFlags().size(); alt++) {
               TcpFlags tcpFlags = line.getTcpFlags().get(alt);
               wSetIpAccessListLine_tcpFlags
                     .append(name + "|" + i + "|" + alt + "\n");
               if (tcpFlags.getUseCwr()) {
                  int bit = tcpFlags.getCwr() ? 1 : 0;
                  wSetIpAccessListLine_tcpFlagsCWR
                        .append(name + "|" + i + "|" + alt + "|" + bit + "\n");
               }
               if (tcpFlags.getUseEce()) {
                  int bit = tcpFlags.getEce() ? 1 : 0;
                  wSetIpAccessListLine_tcpFlagsECE
                        .append(name + "|" + i + "|" + alt + "|" + bit + "\n");
               }
               if (tcpFlags.getUseUrg()) {
                  int bit = tcpFlags.getUrg() ? 1 : 0;
                  wSetIpAccessListLine_tcpFlagsURG
                        .append(name + "|" + i + "|" + alt + "|" + bit + "\n");
               }
               if (tcpFlags.getUseAck()) {
                  int bit = tcpFlags.getAck() ? 1 : 0;
                  wSetIpAccessListLine_tcpFlagsACK
                        .append(name + "|" + i + "|" + alt + "|" + bit + "\n");
               }
               if (tcpFlags.getUsePsh()) {
                  int bit = tcpFlags.getPsh() ? 1 : 0;
                  wSetIpAccessListLine_tcpFlagsPSH
                        .append(name + "|" + i + "|" + alt + "|" + bit + "\n");
               }
               if (tcpFlags.getUseRst()) {
                  int bit = tcpFlags.getRst() ? 1 : 0;
                  wSetIpAccessListLine_tcpFlagsRST
                        .append(name + "|" + i + "|" + alt + "|" + bit + "\n");
               }
               if (tcpFlags.getUseSyn()) {
                  int bit = tcpFlags.getSyn() ? 1 : 0;
                  wSetIpAccessListLine_tcpFlagsSYN
                        .append(name + "|" + i + "|" + alt + "|" + bit + "\n");
               }
               if (tcpFlags.getUseFin()) {
                  int bit = tcpFlags.getFin() ? 1 : 0;
                  wSetIpAccessListLine_tcpFlagsFIN
                        .append(name + "|" + i + "|" + alt + "|" + bit + "\n");
               }
            }
            for (int dscp : line.getDscps()) {
               wSetIpAccessListLine_dscp
                     .append(name + "|" + i + "|" + dscp + "\n");
            }
            for (int dscp : line.getNotDscps()) {
               wSetIpAccessListLine_notDscp
                     .append(name + "|" + i + "|" + dscp + "\n");
            }
            for (int ecn : line.getEcns()) {
               wSetIpAccessListLine_ecn
                     .append(name + "|" + i + "|" + ecn + "\n");
            }
            for (int ecn : line.getNotEcns()) {
               wSetIpAccessListLine_notEcn
                     .append(name + "|" + i + "|" + ecn + "\n");
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
               wSetIsisL1PassiveInterface
                     .append(hostname + "|" + ifaceName + "\n");
            case ACTIVE:
               wSetIsisL1ActiveInterface
                     .append(hostname + "|" + ifaceName + "\n");
               Integer isisCost = iface.getIsisCost();
               if (isisCost == null) {
                  isisCost = IsisProcess.DEFAULT_ISIS_INTERFACE_COST;
               }
               wSetIsisInterfaceCost.append(
                     hostname + "|" + ifaceName + "|" + isisCost + "\n");
               break;
            case UNSET:
               break;
            default:
               throw new BatfishException("Bad IS-IS mode");
            }
            switch (l2Mode) {
            case PASSIVE:
               wSetIsisL2PassiveInterface
                     .append(hostname + "|" + ifaceName + "\n");
            case ACTIVE:
               wSetIsisL2ActiveInterface
                     .append(hostname + "|" + ifaceName + "\n");
               Integer isisCost = iface.getIsisCost();
               if (isisCost == null) {
                  isisCost = IsisProcess.DEFAULT_ISIS_INTERFACE_COST;
               }
               wSetIsisInterfaceCost.append(
                     hostname + "|" + ifaceName + "|" + isisCost + "\n");
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
               String gpName = hostname + ":" + generationPolicy.getName();
               wSetIsisGeneratedRoutePolicy_flat
                     .append(hostname + "|" + network_start + "|" + network_end
                           + "|" + prefix_length + "|" + gpName + "\n");
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
            String mapName = hostname + ":" + map.getName();
            wSetIsisOutboundPolicyMap.append(hostname + "|" + mapName + "\n");
            IsisLevel exportLevel = proc.getPolicyExportLevels()
                  .get(map.getName());
            if (exportLevel == null) {
               continue;
            }
            Set<String> levels = new HashSet<>();
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
               wSetPolicyMapIsisExternalRouteType
                     .append(mapName + "|" + level + "\n");
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
         wSetLinkLoadLimitIn
               .append(hostname + "|" + interfaceName + "|" + limit + "\n");
         wSetLinkLoadLimitOut
               .append(hostname + "|" + interfaceName + "|" + limit + "\n");
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
               String gpName = hostname + ":" + generationPolicy.getName();
               wSetOspfGeneratedRoutePolicy_flat
                     .append(hostname + "|" + network_start + "|" + network_end
                           + "|" + prefix_length + "|" + gpName + "\n");
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
            String mapName = hostname + ":" + map.getName();
            OspfMetricType metricType = proc.getPolicyMetricTypes()
                  .get(map.getName());
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
            wSetPolicyMapOspfExternalRouteType
                  .append(mapName + "|" + protocol + "\n");
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
      StringBuilder wSetPolicyMapClauseMatchPolicyConjunction = _factBins
            .get("SetPolicyMapClauseMatchPolicyConjunction");
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
         String mapName = hostname + ":" + map.getName();
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

               case AS_PATH_ACCESS_LIST: {
                  PolicyMapMatchAsPathAccessListLine matchAsPathLine = (PolicyMapMatchAsPathAccessListLine) matchLine;
                  for (AsPathAccessList asPath : matchAsPathLine.getLists()) {
                     String asPathName = hostname + ":" + asPath.getName();
                     wSetPolicyMapClauseMatchAsPath
                           .append(mapName + "|" + i + "|" + asPathName + "\n");
                  }
                  break;
               }

               case COMMUNITY_LIST: {
                  PolicyMapMatchCommunityListLine mclLine = (PolicyMapMatchCommunityListLine) matchLine;
                  for (CommunityList cList : mclLine.getLists()) {
                     String cListName = hostname + ":" + cList.getName();
                     wSetPolicyMapClauseMatchCommunityList
                           .append(mapName + "|" + i + "|" + cListName + "\n");
                  }
                  break;
               }

               case IP_ACCESS_LIST: {
                  PolicyMapMatchIpAccessListLine mialLine = (PolicyMapMatchIpAccessListLine) matchLine;
                  for (IpAccessList list : mialLine.getLists()) {
                     String listName = hostname + ":" + list.getName();
                     wSetPolicyMapClauseMatchAcl
                           .append(mapName + "|" + i + "|" + listName + "\n");
                  }
                  break;
               }

               case NEIGHBOR: {
                  PolicyMapMatchNeighborLine pmmnl = (PolicyMapMatchNeighborLine) matchLine;
                  long neighborIp = pmmnl.getNeighborIp().asLong();
                  wSetPolicyMapClauseMatchNeighbor
                        .append(mapName + "|" + i + "|" + neighborIp + "\n");
                  break;
               }

               case POLICY: {
                  PolicyMapMatchPolicyLine matchPolicyLine = (PolicyMapMatchPolicyLine) matchLine;
                  PolicyMap policy = matchPolicyLine.getPolicy();
                  String policyName = hostname + ":" + policy.getName();
                  wSetPolicyMapClauseMatchPolicy
                        .append(mapName + "|" + i + "|" + policyName + "\n");
                  break;
               }

               case POLICY_CONJUNCTION: {
                  PolicyMapMatchPolicyConjunctionLine matchPolicyConjunctionLine = (PolicyMapMatchPolicyConjunctionLine) matchLine;
                  Set<PolicyMap> policies = matchPolicyConjunctionLine
                        .getConjuncts();
                  for (PolicyMap policy : policies) {
                     String policyName = hostname + ":" + policy.getName();
                     wSetPolicyMapClauseMatchPolicyConjunction
                           .append(mapName + "|" + i + "|" + policyName + "\n");
                  }
                  break;
               }

               case PROTOCOL: {
                  PolicyMapMatchProtocolLine pmmpl = (PolicyMapMatchProtocolLine) matchLine;
                  RoutingProtocol prot = pmmpl.getProtocol();
                  wSetPolicyMapClauseMatchProtocol.append(
                        mapName + "|" + i + "|" + prot.protocolName() + "\n");
                  break;
               }

               case ROUTE_FILTER_LIST: {
                  PolicyMapMatchRouteFilterListLine mrfLine = (PolicyMapMatchRouteFilterListLine) matchLine;
                  for (RouteFilterList rfList : mrfLine.getLists()) {
                     String rflName = hostname + ":" + rfList.getName();
                     wSetPolicyMapClauseMatchRouteFilter
                           .append(mapName + "|" + i + "|" + rflName + "\n");
                  }
                  break;
               }

               case TAG: {
                  PolicyMapMatchTagLine pmmtl = (PolicyMapMatchTagLine) matchLine;
                  for (Integer tag : pmmtl.getTags()) {
                     wSetPolicyMapClauseMatchTag
                           .append(mapName + "|" + i + "|" + tag + "\n");
                  }
                  break;
               }

               case COLOR: {
                  PolicyMapMatchColorLine pmmcl = (PolicyMapMatchColorLine) matchLine;
                  int color = pmmcl.getColor();
                  wSetPolicyMapClauseMatchColor
                        .append(mapName + "|" + i + "|" + color + "\n");
                  break;
               }

               case INTERFACE: {
                  PolicyMapClauseMatchInterfaceLine pmmil = (PolicyMapClauseMatchInterfaceLine) matchLine;
                  String ifaceName = pmmil.getName();
                  wSetPolicyMapClauseMatchInterface.append(mapName + "|" + i
                        + "|" + hostname + "|" + ifaceName + "\n");
                  break;
               }

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
                     wSetPolicyMapClauseAddCommunity
                           .append(mapName + "|" + i + "|" + community + "\n");
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
                     wSetPolicyMapClauseSetCommunity
                           .append(mapName + "|" + i + "|" + community + "\n");
                  }
                  break;

               case COMMUNITY_NONE:
                  wSetPolicyMapClauseSetCommunityNone
                        .append(mapName + "|" + i + "\n");
                  break;

               case DELETE_COMMUNITY:
                  PolicyMapSetDeleteCommunityLine sdcLine = (PolicyMapSetDeleteCommunityLine) setLine;
                  String cListName = hostname + ":"
                        + sdcLine.getList().getName();
                  wSetPolicyMapClauseDeleteCommunity
                        .append(mapName + "|" + i + "|" + cListName + "\n");
                  break;

               case LOCAL_PREFERENCE:
                  PolicyMapSetLocalPreferenceLine pmslp = (PolicyMapSetLocalPreferenceLine) setLine;
                  int localPref = pmslp.getLocalPreference();
                  wSetPolicyMapClauseSetLocalPreference
                        .append(mapName + "|" + i + "|" + localPref + "\n");
                  break;

               case METRIC:
                  PolicyMapSetMetricLine pmsml = (PolicyMapSetMetricLine) setLine;
                  int metric = pmsml.getMetric();
                  wSetPolicyMapClauseSetMetric
                        .append(mapName + "|" + i + "|" + metric + "\n");
                  break;

               case NEXT_HOP:
                  PolicyMapSetNextHopLine pmsnhl = (PolicyMapSetNextHopLine) setLine;
                  for (Ip nextHopIp : pmsnhl.getNextHops()) {
                     wSetPolicyMapClauseSetNextHopIp.append(
                           mapName + "|" + i + "|" + nextHopIp.asLong() + "\n");
                  }
                  break;

               case ORIGIN_TYPE:
                  PolicyMapSetOriginTypeLine pmsotl = (PolicyMapSetOriginTypeLine) setLine;
                  OriginType originType = pmsotl.getOriginType();
                  wSetPolicyMapClauseSetOriginType.append(
                        mapName + "|" + i + "|" + originType.toString() + "\n");
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
            wSetRouteFilterLine.append(
                  filterName + "|" + i + "|" + network_start + "|" + network_end
                        + "|" + min_prefix + "|" + max_prefix + "\n");
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
            wSetRouteReflectorClient.append(
                  hostname + "|" + neighborPrefixStart + "|" + neighborPrefixEnd
                        + "|" + neighborPrefixLength + "|" + clusterId + "\n");
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
         for (Prefix prefix : i.getAllPrefixes()) {
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
            if (CommonUtil.isNullInterface(nextHopInt)) {
               nextHopInt = CommonUtil.NULL_INTERFACE_NAME;
            }
            wSetStaticIntRoute_flat.append(hostName + "|" + network_start + "|"
                  + network_end + "|" + prefix_length + "|" + nextHopIp.asLong()
                  + "|" + nextHopInt + "|" + distance + "|" + tag + "\n");
         }
         else {
            wSetStaticRoute_flat.append(hostName + "|" + network_start + "|"
                  + network_end + "|" + prefix_length + "|" + nextHopIp.asLong()
                  + "|" + distance + "|" + tag + "\n");
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
            wSetSwitchportAccess
                  .append(hostname + "|" + interfaceName + "|" + vlan + "\n");
            break;

         // TODO: create derived switchport facts in logic and here
         case DOT1Q_TUNNEL:
         case DYNAMIC_AUTO:
         case DYNAMIC_DESIRABLE:
         case FEX_FABRIC:
         case TAP:
         case TOOL:
         case NONE:
            break;

         case TRUNK:
            SwitchportEncapsulationType encapsulation = i
                  .getSwitchportTrunkEncapsulation();
            wSetSwitchportTrunkEncapsulation
                  .append(hostname + "|" + interfaceName + "|"
                        + encapsulation.toString().toLowerCase() + "\n");
            int nativeVlan = i.getNativeVlan();
            wSetSwitchportTrunkNative.append(
                  hostname + "|" + interfaceName + "|" + nativeVlan + "\n");
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
      String vendor = _configuration.getConfigurationFormat().getVendorString();
      StringBuilder wSetNodeVendor = _factBins.get("SetNodeVendor");
      wSetNodeVendor.append(hostname + "|" + vendor + "\n");
   }

   private void writeVlanInterface() {
      StringBuilder wSetVlanInterface = _factBins.get("SetVlanInterface");
      String hostname = _configuration.getHostname();
      for (String ifaceName : _configuration.getInterfaces().keySet()) {
         Integer vlan = CommonUtil.getInterfaceVlanNumber(ifaceName);
         if (vlan != null) {
            wSetVlanInterface
                  .append(hostname + "|" + ifaceName + "|" + vlan + "\n");
         }
      }
   }

   private void writeZoneFacts() {
      StringBuilder wSetCrossZoneFilter = _factBins.get("SetCrossZoneFilter");
      StringBuilder wSetDefaultCrossZoneAccept = _factBins
            .get("SetDefaultCrossZoneAccept");
      StringBuilder wSetDefaultInboundAccept = _factBins
            .get("SetDefaultInboundAccept");
      StringBuilder wSetInboundInterfaceFilter = _factBins
            .get("SetInboundInterfaceFilter");
      StringBuilder wSetInterfaceZone = _factBins.get("SetInterfaceZone");
      StringBuilder wSetZoneFromHostFilter = _factBins
            .get("SetZoneFromHostFilter");
      StringBuilder wSetZoneToHostFilter = _factBins.get("SetZoneToHostFilter");
      String hostname = _configuration.getHostname();
      for (Zone srcZone : _configuration.getZones().values()) {
         String srcZoneName = hostname + ":" + srcZone.getName();
         for (Entry<String, IpAccessList> e : srcZone
               .getInboundInterfaceFilters().entrySet()) {
            String ifaceName = e.getKey();
            IpAccessList inboundFilter = e.getValue();
            String inboundFilterName = hostname + ":" + inboundFilter.getName();
            wSetInboundInterfaceFilter.append(
                  hostname + "|" + ifaceName + "|" + inboundFilterName + "\n");
         }
         for (Entry<String, IpAccessList> e : srcZone.getToZonePolicies()
               .entrySet()) {
            String dstZoneName = hostname + ":" + e.getKey();
            IpAccessList crossZoneFilter = e.getValue();
            String crossZoneFilterName = hostname + ":"
                  + crossZoneFilter.getName();
            wSetCrossZoneFilter.append(hostname + "|" + srcZoneName + "|"
                  + dstZoneName + "|" + crossZoneFilterName + "\n");
         }
         IpAccessList fromHostFilter = srcZone.getFromHostFilter();
         if (fromHostFilter != null) {
            String fromHostFilterName = hostname + ":"
                  + fromHostFilter.getName();
            wSetZoneFromHostFilter.append(hostname + "|" + srcZoneName + "|"
                  + fromHostFilterName + "\n");
         }
         IpAccessList toHostFilter = srcZone.getToHostFilter();
         if (toHostFilter != null) {
            String toHostFilterName = hostname + ":" + toHostFilter.getName();
            wSetZoneToHostFilter.append(
                  hostname + "|" + srcZoneName + "|" + toHostFilterName + "\n");
         }
      }
      if (_configuration.getDefaultCrossZoneAction() == LineAction.ACCEPT) {
         wSetDefaultCrossZoneAccept.append(hostname + "\n");
      }
      if (_configuration.getDefaultInboundAction() == LineAction.ACCEPT) {
         wSetDefaultInboundAccept.append(hostname + "\n");
      }
      for (Interface iface : _configuration.getInterfaces().values()) {
         String ifaceName = iface.getName();
         Zone zone = iface.getZone();
         if (zone != null) {
            String zoneName = hostname + ":" + zone.getName();
            wSetInterfaceZone
                  .append(hostname + "|" + ifaceName + "|" + zoneName + "\n");
         }
      }
   }

}
