package org.batfish.vendor.cisco_aci.representation;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.acl.PermittedByAcl;

/** Converts ACI contracts and EPG policy relationships into ACLs. */
final class AciContractConverter {

  private static final String CONTRACT_ACL_PREFIX = "~CONTRACT~";
  private static final String TABOO_ACL_PREFIX = "~TABOO~";

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

  static void convertContracts(AciConfiguration aciConfig, Configuration c, Warnings warnings) {
    for (Map.Entry<String, AciConfiguration.Contract> entry : aciConfig.getContracts().entrySet()) {
      // Use the map key which contains the fully-qualified name (tenant:contract)
      String contractName = entry.getKey();
      AciConfiguration.Contract contract = entry.getValue();
      if (contractName == null || contractName.isEmpty()) {
        continue;
      }
      String aclName = getContractAclName(contractName);
      List<ExprAclLine> aclLines =
          buildAclLinesFromSubjects(contractName, contract.getSubjects(), aciConfig, warnings);
      installAclIfNonEmpty(c, aclName, aclLines);
    }
  }

  static void convertTabooContracts(
      AciConfiguration aciConfig, Configuration c, Warnings warnings) {
    for (Map.Entry<String, AciConfiguration.TabooContract> entry :
        aciConfig.getTabooContracts().entrySet()) {
      String tabooName = entry.getKey();
      AciConfiguration.TabooContract taboo = entry.getValue();
      if (tabooName == null || tabooName.isEmpty()) {
        continue;
      }
      String aclName = getTabooAclName(tabooName);
      List<ExprAclLine> aclLines =
          buildAclLinesFromSubjects(tabooName, taboo.getSubjects(), aciConfig, warnings);
      installAclIfNonEmpty(c, aclName, aclLines);
    }
  }

  private static @Nonnull List<ExprAclLine> buildAclLinesFromSubjects(
      String contractName,
      @Nullable List<AciConfiguration.Contract.Subject> subjects,
      AciConfiguration aciConfig,
      Warnings warnings) {
    ImmutableList.Builder<ExprAclLine> lines = ImmutableList.builder();

    // Extract tenant name from fully-qualified contract name (tenant:contract)
    String tenantName = null;
    int colonIdx = contractName.indexOf(':');
    if (colonIdx > 0) {
      tenantName = contractName.substring(0, colonIdx);
    }

    if (subjects != null) {
      for (AciConfiguration.Contract.Subject subject : subjects) {
        if (subject.getFilters() != null) {
          for (AciConfiguration.Contract.Filter filterRef : subject.getFilters()) {
            String filterName = filterRef.getName();
            if (filterName == null || filterName.isEmpty()) {
              lines.addAll(toAclLines(filterRef, contractName, warnings));
              continue;
            }

            String fqFilterName =
                (tenantName != null) ? (tenantName + ":" + filterName) : filterName;
            AciConfiguration.Filter fullFilter = aciConfig.getFilters().get(fqFilterName);

            if (fullFilter != null
                && fullFilter.getEntries() != null
                && !fullFilter.getEntries().isEmpty()) {
              for (AciConfiguration.Filter.Entry filterEntry : fullFilter.getEntries()) {
                List<ExprAclLine> entryLines =
                    toAclEntryLines(filterEntry, contractName, filterName, warnings);
                lines.addAll(entryLines);
              }
            } else {
              lines.addAll(toAclLines(filterRef, contractName, warnings));
            }
          }
        }
      }
    }

    if (!lines.build().isEmpty()) {
      lines.add(
          new ExprAclLine(
              LineAction.DENY,
              AclLineMatchExprs.TRUE,
              "Default deny for contract " + contractName));
    }
    return lines.build();
  }

  private static void installAclIfNonEmpty(
      Configuration c, String aclName, List<ExprAclLine> aclLines) {
    if (!aclLines.isEmpty()) {
      List<AclLine> aclLinesCasted = new ArrayList<>(aclLines);
      IpAccessList acl =
          IpAccessList.builder().setOwner(c).setName(aclName).setLines(aclLinesCasted).build();
      c.getIpAccessLists().put(aclName, acl);
    }
  }

  /**
   * Converts a contract filter to ACL lines.
   *
   * <p>This method converts ACI filter entries to Batfish ACL lines, supporting:
   *
   * <ul>
   *   <li>IP protocols - TCP, UDP, ICMP, or protocol number
   *   <li>TCP/UDP port ranges - Single ports or ranges (e.g., "80", "8080-8090")
   *   <li>IP address ranges - With wildcards (e.g., "10.0.0.0/24", "10.0.0.0/0.0.0.255")
   *   <li>ICMP types and codes - For ICMP protocol filtering
   *   <li>Non-IP traffic - ARP, MPLS via etherType field
   * </ul>
   *
   * @param filter The contract filter
   * @param contractName The contract name for trace elements
   * @param c The Batfish configuration
   * @param warnings Warnings container
   * @return List of ACL lines
   */
  private static List<ExprAclLine> toAclLines(
      AciConfiguration.Contract.Filter filter, String contractName, Warnings warnings) {

    ImmutableList.Builder<ExprAclLine> lines = ImmutableList.builder();

    // Determine action based on filter's action field (default to permit)
    LineAction action = LineAction.PERMIT;
    if (filter.getAction() != null && "deny".equalsIgnoreCase(filter.getAction())) {
      action = LineAction.DENY;
    }

    // Build match expressions based on filter criteria
    ImmutableList.Builder<AclLineMatchExpr> matchExprs = ImmutableList.builder();

    // Handle non-IP traffic via etherType (e.g., ARP, MPLS)
    if (filter.getEtherType() != null && !filter.getEtherType().isEmpty()) {
      AclLineMatchExpr etherTypeExpr =
          toEtherType(filter.getEtherType(), contractName, filter, warnings);
      if (etherTypeExpr != null) {
        matchExprs.add(etherTypeExpr);
      }
    }

    // Match on IP protocol if specified
    if (filter.getIpProtocol() != null) {
      AclLineMatchExpr protocolExpr = toIpProtocolMatchExpr(filter.getIpProtocol(), warnings);
      if (protocolExpr != null) {
        matchExprs.add(protocolExpr);

        // Handle ICMP-specific fields (type and code)
        if (filter.getIpProtocol().toLowerCase().contains("icmp")) {
          if (filter.getIcmpType() != null) {
            matchExprs.add(toIcmpTypeCode(filter.getIcmpType(), filter.getIcmpCode(), warnings));
          }
        }
      }
    }

    // Match on source IP address if specified
    if (filter.getSourceAddress() != null && !filter.getSourceAddress().isEmpty()) {
      AclLineMatchExpr srcAddrExpr = toIpMatchExpr(filter.getSourceAddress(), true, warnings);
      if (srcAddrExpr != null) {
        matchExprs.add(srcAddrExpr);
      }
    }

    // Match on destination IP address if specified
    if (filter.getDestinationAddress() != null && !filter.getDestinationAddress().isEmpty()) {
      AclLineMatchExpr dstAddrExpr = toIpMatchExpr(filter.getDestinationAddress(), false, warnings);
      if (dstAddrExpr != null) {
        matchExprs.add(dstAddrExpr);
      }
    }

    // Match on destination ports if specified (supports ranges)
    if (filter.getDestinationPorts() != null && !filter.getDestinationPorts().isEmpty()) {
      IntegerSpace portSpace =
          toPortSpace(filter.getDestinationPorts(), contractName, filter, true, warnings);
      if (!portSpace.isEmpty()) {
        matchExprs.add(AclLineMatchExprs.matchDstPort(portSpace));
      }
    }

    // Match on source ports if specified (supports ranges)
    if (filter.getSourcePorts() != null && !filter.getSourcePorts().isEmpty()) {
      IntegerSpace portSpace =
          toPortSpace(filter.getSourcePorts(), contractName, filter, false, warnings);
      if (!portSpace.isEmpty()) {
        matchExprs.add(AclLineMatchExprs.matchSrcPort(portSpace));
      }
    }

    // Handle ARP opcode if specified
    if (isMeaningfulArpOpcode(filter.getArpOpcode())) {
      // ARP is handled at L2, so we emit a warning that this filter may have limited effect
      warnings.redFlagf(
          "ARP opcode specified in contract %s filter %s: %s. ARP filtering has limited effect in"
              + " IP ACLs.",
          contractName, filter.getName(), filter.getArpOpcode());
    }

    AclLineMatchExpr matchExpr =
        matchExprs.build().isEmpty()
            ? AclLineMatchExprs.TRUE
            : AclLineMatchExprs.and(matchExprs.build());

    String filterName = filter.getName() != null ? filter.getName() : "unnamed";
    ExprAclLine line =
        new ExprAclLine(
            action, matchExpr, String.format("Contract %s filter %s", contractName, filterName));

    lines.add(line);
    return lines.build();
  }

  /**
   * Converts a filter entry to ACL lines.
   *
   * <p>This method converts ACI filter entries (vzEntry) to Batfish ACL lines, supporting:
   *
   * <ul>
   *   <li>IP protocols - TCP, UDP, ICMP, or protocol number
   *   <li>TCP/UDP ports - Single ports or ranges (dPort, sPort, dFromPort/dToPort,
   *       sFromPort/sToPort)
   *   <li>IP address ranges - srcAddr, dstAddr
   *   <li>ICMP types and codes - icmpv4T/icmpv4C, icmpv6T/icmpv6C
   *   <li>Non-IP traffic - ARP via arpOpc, etherType
   * </ul>
   *
   * @param entry The filter entry
   * @param contractName The contract name for trace elements
   * @param filterName The filter name for trace elements
   * @param c The Batfish configuration
   * @param warnings Warnings container
   * @return List of ACL lines
   */
  private static List<ExprAclLine> toAclEntryLines(
      AciConfiguration.Filter.Entry entry,
      String contractName,
      String filterName,
      Warnings warnings) {

    ImmutableList.Builder<ExprAclLine> lines = ImmutableList.builder();
    LineAction action = LineAction.PERMIT; // Default action for filter entries
    ImmutableList.Builder<AclLineMatchExpr> matchExprs = ImmutableList.builder();

    // Handle non-IP traffic via etherType (e.g., ARP, MPLS)
    if (entry.getEtherType() != null && !entry.getEtherType().isEmpty()) {
      AclLineMatchExpr etherTypeExpr =
          toEtherType(entry.getEtherType(), contractName, entry.getName(), warnings);
      if (etherTypeExpr != null) {
        matchExprs.add(etherTypeExpr);
      }
    }

    // Match on IP protocol if specified
    if (entry.getProtocol() != null) {
      AclLineMatchExpr protocolExpr = toIpProtocolMatchExpr(entry.getProtocol(), warnings);
      if (protocolExpr != null) {
        matchExprs.add(protocolExpr);

        // Handle ICMP-specific fields (type and code)
        String protocol = entry.getProtocol().toLowerCase();
        if (protocol.contains("icmpv4") || protocol.equals("icmp")) {
          if (entry.getIcmpv4Type() != null) {
            matchExprs.add(toIcmpTypeCode(entry.getIcmpv4Type(), entry.getIcmpv4Code(), warnings));
          }
        } else if (protocol.contains("icmpv6")) {
          if (entry.getIcmpv6Type() != null) {
            matchExprs.add(toIcmpTypeCode(entry.getIcmpv6Type(), entry.getIcmpv6Code(), warnings));
          }
        }
      }
    }

    // Match on source IP address if specified
    if (entry.getSourceAddress() != null && !entry.getSourceAddress().isEmpty()) {
      AclLineMatchExpr srcAddrExpr = toIpMatchExpr(entry.getSourceAddress(), true, warnings);
      if (srcAddrExpr != null) {
        matchExprs.add(srcAddrExpr);
      }
    }

    // Match on destination IP address if specified
    if (entry.getDestinationAddress() != null && !entry.getDestinationAddress().isEmpty()) {
      AclLineMatchExpr dstAddrExpr = toIpMatchExpr(entry.getDestinationAddress(), false, warnings);
      if (dstAddrExpr != null) {
        matchExprs.add(dstAddrExpr);
      }
    }

    // Match on destination ports - handle both single port and port range
    String dstPort = normalizeSinglePort(entry.getDestinationPort());
    String dstFromPort = normalizeRangeEndpoint(entry.getDestinationFromPort());
    String dstToPort = normalizeRangeEndpoint(entry.getDestinationToPort());

    if (dstPort != null || (dstFromPort != null && dstToPort != null)) {
      List<String> dstPorts = new ArrayList<>();
      if (dstPort != null) {
        dstPorts.add(dstPort);
      } else if (dstFromPort != null && dstToPort != null) {
        dstPorts.add(dstFromPort + "-" + dstToPort);
      }
      IntegerSpace portSpace = toPortSpace(dstPorts, contractName, entry.getName(), true, warnings);
      if (!portSpace.isEmpty()) {
        matchExprs.add(AclLineMatchExprs.matchDstPort(portSpace));
      }
    }

    // Match on source ports - handle both single port and port range
    String srcPort = normalizeSinglePort(entry.getSourcePort());
    String srcFromPort = normalizeRangeEndpoint(entry.getSourceFromPort());
    String srcToPort = normalizeRangeEndpoint(entry.getSourceToPort());

    if (srcPort != null || (srcFromPort != null && srcToPort != null)) {
      List<String> srcPorts = new ArrayList<>();
      if (srcPort != null) {
        srcPorts.add(srcPort);
      } else if (srcFromPort != null && srcToPort != null) {
        srcPorts.add(srcFromPort + "-" + srcToPort);
      }
      IntegerSpace portSpace =
          toPortSpace(srcPorts, contractName, entry.getName(), false, warnings);
      if (!portSpace.isEmpty()) {
        matchExprs.add(AclLineMatchExprs.matchSrcPort(portSpace));
      }
    }

    // Handle ARP opcode if specified
    if (isMeaningfulArpOpcode(entry.getArpOpcode())) {
      warnings.redFlagf(
          "ARP opcode specified in contract %s filter %s entry %s: %s. ARP filtering has limited"
              + " effect in IP ACLs.",
          contractName, filterName, entry.getName(), entry.getArpOpcode());
    }

    // Handle stateful flag - warn that this may not be fully supported
    if (Boolean.TRUE.equals(entry.getStateful())) {
      warnings.redFlagf(
          "Stateful filtering specified in contract %s filter %s entry %s. Stateful filtering"
              + " may not be fully supported in ACL conversion.",
          contractName, filterName, entry.getName());
    }

    AclLineMatchExpr matchExpr =
        matchExprs.build().isEmpty()
            ? AclLineMatchExprs.TRUE
            : AclLineMatchExprs.and(matchExprs.build());

    String entryName = entry.getName() != null ? entry.getName() : "unnamed";
    ExprAclLine line =
        new ExprAclLine(
            action,
            matchExpr,
            String.format("Contract %s filter %s entry %s", contractName, filterName, entryName));

    lines.add(line);
    return lines.build();
  }

  /**
   * Converts an ACI protocol string to a Batfish IpProtocol match expression.
   *
   * <p>Supports protocol names (tcp, udp, icmp) and protocol numbers (6, 17, 1, etc.). Any valid IP
   * protocol number from 0-255 is supported.
   *
   * @param protocol The protocol string (e.g., "tcp", "udp", "icmp", "6", "17")
   * @param warnings Warnings container
   * @return The AclLineMatchExpr for matching the protocol, or null if protocol is null
   */
  private static @Nullable AclLineMatchExpr toIpProtocolMatchExpr(
      String protocol, Warnings warnings) {
    if (protocol == null) {
      return null;
    }
    String p = protocol.toLowerCase().trim();

    // Handle common protocol names
    switch (p) {
      case "tcp":
        return AclLineMatchExprs.matchIpProtocol(IpProtocol.TCP);
      case "udp":
        return AclLineMatchExprs.matchIpProtocol(IpProtocol.UDP);
      case "icmp":
        return AclLineMatchExprs.matchIpProtocol(IpProtocol.ICMP);
      case "ip":
      case "ipv4":
        return null; // No protocol filtering needed for "any IP"
      case "igmp":
        return AclLineMatchExprs.matchIpProtocol(IpProtocol.IGMP);
      case "ipinip":
        return AclLineMatchExprs.matchIpProtocol(IpProtocol.IPINIP);
      case "gre":
        return AclLineMatchExprs.matchIpProtocol(IpProtocol.GRE);
      case "ospf":
        return AclLineMatchExprs.matchIpProtocol(IpProtocol.OSPF);
      case "pim":
        return AclLineMatchExprs.matchIpProtocol(IpProtocol.PIM);
      case "sctp":
        return AclLineMatchExprs.matchIpProtocol(IpProtocol.SCTP);
      default:
        // Try to parse as protocol number
        try {
          int protoNum = Integer.parseInt(p);
          if (protoNum >= 0 && protoNum <= 255) {
            return AclLineMatchExprs.matchIpProtocol(protoNum);
          } else {
            warnings.redFlagf("Invalid IP protocol number: %s (must be 0-255)", protocol);
            return null;
          }
        } catch (NumberFormatException e) {
          // Unknown protocol name
          warnings.redFlagf("Unknown IP protocol: %s", protocol);
          return null;
        }
    }
  }

  private static boolean isMeaningfulArpOpcode(@Nullable String arpOpcode) {
    if (arpOpcode == null) {
      return false;
    }
    String normalized = arpOpcode.trim().toLowerCase();
    return !normalized.isEmpty() && !normalized.equals("unspecified") && !normalized.equals("any");
  }

  private static @Nullable String normalizeRangeEndpoint(@Nullable String value) {
    if (value == null) {
      return null;
    }
    String normalized = value.trim().toLowerCase();
    if (normalized.isEmpty()
        || normalized.equals("unspecified")
        || normalized.equals("any")
        || normalized.equals("0")) {
      return null;
    }
    return value.trim();
  }

  private static @Nullable String normalizeSinglePort(@Nullable String value) {
    return normalizeRangeEndpoint(value);
  }

  /**
   * Converts ACI path attachments (fvRsPathAtt) to interface assignments.
   *
   * <p>Path attachments link Endpoint Groups to physical interfaces/ports.
   *
   * @param aciConfig The ACI configuration
   * @param interfaces Map of existing interfaces
   * @param c The Batfish configuration
   * @param warnings Warnings container
   */
  static void convertPathAttachments(
      AciConfiguration aciConfig,
      Map<String, Interface> interfaces,
      Configuration c,
      Warnings warnings) {

    // Process EPGs to find their interface associations
    for (AciConfiguration.Epg epg : aciConfig.getEpgs().values()) {
      String bridgeDomainName = epg.getBridgeDomain();
      String epgDisplayName = epg.getName();
      String tenantName = epg.getTenant();
      String appProfileName = epg.getApplicationProfile();

      // Find the bridge domain to determine VLAN
      Integer vlanId = null;
      if (bridgeDomainName != null) {
        AciConfiguration.BridgeDomain bd = aciConfig.getBridgeDomains().get(bridgeDomainName);
        if (bd != null) {
          // Generate a VLAN ID from the BD name
          vlanId = Math.abs(bd.getName().hashCode() % 4094) + 1;
        }
      }

      // For each interface in the fabric nodes, check if it belongs to this EPG
      for (AciConfiguration.FabricNode node : aciConfig.getFabricNodes().values()) {
        if (node.getInterfaces() == null) {
          continue;
        }
        for (AciConfiguration.FabricNode.Interface iface : node.getInterfaces().values()) {
          if (epg.getName().equals(iface.getEpg())) {
            // This interface belongs to the EPG
            Interface batfishIface = interfaces.get(iface.getName());
            if (batfishIface == null) {
              warnings.redFlagf(
                  "Interface %s not found for EPG %s", iface.getName(), epg.getName());
              continue;
            }

            // Build EPG metadata description
            StringBuilder epgMetadata = new StringBuilder();
            epgMetadata.append("EPG: ").append(epgDisplayName);
            if (tenantName != null) {
              epgMetadata.append(" | Tenant: ").append(tenantName);
            }
            if (appProfileName != null) {
              epgMetadata.append(" | AppProfile: ").append(appProfileName);
            }
            if (bridgeDomainName != null) {
              epgMetadata.append(" | BridgeDomain: ").append(bridgeDomainName);
            }

            // Append EPG info to existing description
            String existingDesc = batfishIface.getDescription();
            if (existingDesc != null && !existingDesc.isEmpty()) {
              batfishIface.setDescription(existingDesc + " | " + epgMetadata.toString());
            } else {
              batfishIface.setDescription(epgMetadata.toString());
            }

            // Set VLAN based on EPG's bridge domain
            if (vlanId != null) {
              IntegerSpace newVlans =
                  IntegerSpace.builder()
                      .including(batfishIface.getAllowedVlans())
                      .including(vlanId)
                      .build();
              batfishIface.setAllowedVlans(newVlans);
              batfishIface.setNativeVlan(vlanId);
              batfishIface.setSwitchportMode(SwitchportMode.TRUNK);
              batfishIface.setSwitchport(true);
            }

            // Also check for explicit VLAN on interface
            if (iface.getVlan() != null) {
              try {
                int explicitVlan = Integer.parseInt(iface.getVlan());
                IntegerSpace newVlans =
                    IntegerSpace.builder()
                        .including(batfishIface.getAllowedVlans())
                        .including(explicitVlan)
                        .build();
                batfishIface.setAllowedVlans(newVlans);
                batfishIface.setNativeVlan(explicitVlan);
                batfishIface.setSwitchportMode(SwitchportMode.TRUNK);
                batfishIface.setSwitchport(true);
              } catch (NumberFormatException e) {
                warnings.redFlagf(
                    "Invalid VLAN for interface %s: %s", iface.getName(), iface.getVlan());
              }
            }

            // Apply EPG contract policy relationships as interface ACLs.
            applyEpgPolicies(epg, batfishIface, c, warnings);
          }
        }
      }
    }
  }

  static void applyEpgPolicies(
      AciConfiguration.Epg epg, Interface iface, Configuration c, Warnings warnings) {
    List<String> incomingAclRefs =
        resolveContractAclRefs(
            epg.getConsumedContracts(),
            epg.getConsumedContractInterfaces(),
            epg.getTenant(),
            c,
            warnings,
            epg.getName(),
            "incoming",
            AclKind.CONTRACT);
    List<String> outgoingAclRefs =
        resolveContractAclRefs(
            epg.getProvidedContracts(),
            epg.getProvidedContractInterfaces(),
            epg.getTenant(),
            c,
            warnings,
            epg.getName(),
            "outgoing",
            AclKind.CONTRACT);
    List<String> tabooAclRefs =
        resolveContractAclRefs(
            epg.getProtectedByTaboos(),
            ImmutableList.of(),
            epg.getTenant(),
            c,
            warnings,
            epg.getName(),
            "taboo",
            AclKind.TABOO);

    if (!incomingAclRefs.isEmpty() || !tabooAclRefs.isEmpty()) {
      IpAccessList incomingFilter =
          buildEpgPolicyAcl(
              c, epg.getName(), "IN", incomingAclRefs, tabooAclRefs, /* defaultPermit= */ true);
      iface.setIncomingFilter(incomingFilter);
    }
    if (!outgoingAclRefs.isEmpty() || !tabooAclRefs.isEmpty()) {
      IpAccessList outgoingFilter =
          buildEpgPolicyAcl(
              c, epg.getName(), "OUT", outgoingAclRefs, tabooAclRefs, /* defaultPermit= */ true);
      iface.setOutgoingFilter(outgoingFilter);
    }
  }

  private static @Nonnull List<String> resolveContractAclRefs(
      @Nullable List<String> contractNames,
      @Nullable List<String> contractInterfaceNames,
      @Nullable String tenantName,
      Configuration c,
      Warnings warnings,
      String epgName,
      String direction,
      AclKind aclKind) {
    Set<String> aclRefs = new HashSet<>();
    if (contractNames != null) {
      for (String name : contractNames) {
        resolveContractAclRef(
            name, tenantName, c, aclRefs, warnings, epgName, direction, "contract", aclKind);
      }
    }
    if (contractInterfaceNames != null) {
      for (String name : contractInterfaceNames) {
        resolveContractAclRef(
            name,
            tenantName,
            c,
            aclRefs,
            warnings,
            epgName,
            direction,
            "contract-interface",
            aclKind);
      }
    }
    return ImmutableList.copyOf(aclRefs);
  }

  private static void resolveContractAclRef(
      @Nullable String rawName,
      @Nullable String tenantName,
      Configuration c,
      Set<String> aclRefs,
      Warnings warnings,
      String epgName,
      String direction,
      String refType,
      AclKind aclKind) {
    if (rawName == null || rawName.isEmpty()) {
      return;
    }
    List<String> candidateContractNames = new ArrayList<>();
    candidateContractNames.add(rawName);
    if (tenantName != null && !rawName.contains(":")) {
      candidateContractNames.add(tenantName + ":" + rawName);
    }
    for (String contractName : candidateContractNames) {
      String aclName =
          aclKind == AclKind.TABOO
              ? getTabooAclName(contractName)
              : getContractAclName(contractName);
      if (c.getIpAccessLists().containsKey(aclName)) {
        aclRefs.add(aclName);
        return;
      }
    }
    warnings.redFlagf(
        "Could not resolve %s reference '%s' for EPG %s (%s direction) to a known contract ACL",
        refType, rawName, epgName, direction);
  }

  private static @Nonnull IpAccessList buildEpgPolicyAcl(
      Configuration c,
      String epgName,
      String direction,
      List<String> permitAclRefs,
      List<String> denyAclRefs,
      boolean defaultPermit) {
    String aclName =
        String.format("~EPG_POLICY~%s~%s", epgName.replaceAll("[^A-Za-z0-9:_-]", "_"), direction);

    ImmutableList.Builder<AclLine> lines = ImmutableList.builder();
    for (String denyAclRef : denyAclRefs) {
      lines.add(
          new ExprAclLine(
              LineAction.DENY,
              new PermittedByAcl(denyAclRef),
              String.format("Denied by taboo policy ACL %s", denyAclRef)));
    }
    for (String permitAclRef : permitAclRefs) {
      lines.add(
          new ExprAclLine(
              LineAction.PERMIT,
              new PermittedByAcl(permitAclRef),
              String.format("Permitted by contract policy ACL %s", permitAclRef)));
    }
    lines.add(
        new ExprAclLine(
            defaultPermit ? LineAction.PERMIT : LineAction.DENY,
            AclLineMatchExprs.TRUE,
            defaultPermit
                ? String.format("Default permit for EPG %s %s policy", epgName, direction)
                : String.format("Default deny for EPG %s %s policy", epgName, direction)));

    IpAccessList acl =
        IpAccessList.builder().setOwner(c).setName(aclName).setLines(lines.build()).build();
    c.getIpAccessLists().put(aclName, acl);
    return acl;
  }

  private enum AclKind {
    CONTRACT,
    TABOO
  }

  /**
   * Gets the generated ACL name for a contract.
   *
   * @param contractName The contract name
   * @return The ACL name
   */
  @VisibleForTesting
  public static @Nonnull String getContractAclName(String contractName) {
    return CONTRACT_ACL_PREFIX + contractName;
  }

  public static @Nonnull String getTabooAclName(String tabooName) {
    return TABOO_ACL_PREFIX + tabooName;
  }

  private static @Nullable AclLineMatchExpr toEtherType(
      String etherType,
      String contractName,
      AciConfiguration.Contract.Filter filter,
      Warnings warnings) {
    if (etherType == null) {
      warnings.redFlagf("Null etherType in contract %s filter %s", contractName, filter.getName());
      return null;
    }
    String et = etherType.toLowerCase().trim();

    // Handle hex format (0x prefix or just hex digits)
    int etherTypeValue;
    if (et.startsWith("0x") || et.startsWith("0X")) {
      if (et.length() <= 2) {
        warnings.redFlagf(
            "Invalid etherType (empty hex value) in contract %s filter %s: %s",
            contractName, filter.getName(), etherType);
        return null;
      }
      try {
        etherTypeValue = Integer.parseInt(et.substring(2), 16);
        if (etherTypeValue < 0 || etherTypeValue > 0xFFFF) {
          warnings.redFlagf(
              "EtherType out of range (0x0000-0xFFFF) in contract %s filter %s: 0x%x",
              contractName, filter.getName(), etherTypeValue);
          return null;
        }
      } catch (NumberFormatException e) {
        warnings.redFlagf(
            "Invalid etherType in contract %s filter %s: %s",
            contractName, filter.getName(), etherType);
        return null;
      }
    } else if (et.matches("[0-9a-f]+")) {
      try {
        etherTypeValue = Integer.parseInt(et, 16);
      } catch (NumberFormatException e) {
        warnings.redFlagf(
            "Invalid etherType in contract %s filter %s: %s",
            contractName, filter.getName(), etherType);
        return null;
      }
    } else {
      // Handle named etherTypes
      switch (et) {
        case "arp":
          etherTypeValue = 0x0806;
          break;
        case "ipv4":
        case "ip":
          etherTypeValue = 0x0800;
          break;
        case "ipv6":
          etherTypeValue = 0x86dd;
          break;
        case "mpls":
        case "mpls_unicast":
          etherTypeValue = 0x8847;
          break;
        case "mpls_multicast":
          etherTypeValue = 0x8848;
          break;
        default:
          warnings.redFlagf(
              "Unknown etherType in contract %s filter %s: %s",
              contractName, filter.getName(), etherType);
          return null;
      }
    }

    // Convert etherType to match expression
    // Since Batfish ACLs work at IP layer, non-IP etherTypes require special handling
    switch (etherTypeValue) {
      case 0x0800: // IPv4
        // No filtering needed - this is the default
        return null;
      case 0x86dd: // IPv6
        // For now, warn that IPv6 filtering is not fully supported in IPv4 ACLs
        warnings.redFlagf(
            "IPv6 etherType specified in contract %s filter %s: IPv6 filtering has limited effect"
                + " in IPv4 ACLs",
            contractName, filter.getName());
        return null;
      case 0x0806: // ARP
        // ARP is L2 - we can't match it in IP ACLs
        warnings.redFlagf(
            "ARP etherType specified in contract %s filter %s: ARP filtering has limited effect in"
                + " IP ACLs",
            contractName, filter.getName());
        return AclLineMatchExprs.FALSE; // Will not match any IP traffic
      case 0x8847: // MPLS unicast
      case 0x8848: // MPLS multicast
        // MPLS is L2.5 - warn about limited effect
        warnings.redFlagf(
            "MPLS etherType specified in contract %s filter %s: MPLS filtering has limited effect"
                + " in IP ACLs",
            contractName, filter.getName());
        return AclLineMatchExprs.FALSE; // Will not match any IP traffic
      default:
        // Other non-IP etherTypes
        warnings.redFlagf(
            "Non-IP etherType (0x%04x) specified in contract %s filter %s: This will not match IP"
                + " traffic",
            etherTypeValue, contractName, filter.getName());
        return AclLineMatchExprs.FALSE;
    }
  }

  /**
   * Converts an ACI etherType string to a match expression.
   *
   * <p>This overload is for filter entries which use entry names instead of filter objects.
   *
   * @param etherType The etherType string (e.g., "arp", "ip", "0x0806")
   * @param contractName The contract name for warnings
   * @param entryName The entry name for warnings
   * @param warnings Warnings container
   * @return A match expression for the etherType, or null if not applicable
   */
  private static @Nullable AclLineMatchExpr toEtherType(
      String etherType, String contractName, String entryName, Warnings warnings) {
    String et = etherType.toLowerCase().trim();

    // Handle hex format (0x prefix or just hex digits)
    int etherTypeValue;
    if (et.startsWith("0x") || et.startsWith("0X")) {
      try {
        etherTypeValue = Integer.parseInt(et.substring(2), 16);
      } catch (NumberFormatException e) {
        warnings.redFlagf(
            "Invalid etherType in contract %s entry %s: %s", contractName, entryName, etherType);
        return null;
      }
    } else if (et.matches("[0-9a-f]+")) {
      try {
        etherTypeValue = Integer.parseInt(et, 16);
      } catch (NumberFormatException e) {
        warnings.redFlagf(
            "Invalid etherType in contract %s entry %s: %s", contractName, entryName, etherType);
        return null;
      }
    } else {
      // Handle named etherTypes
      switch (et) {
        case "arp":
          etherTypeValue = 0x0806;
          break;
        case "ipv4":
        case "ip":
          etherTypeValue = 0x0800;
          return null; // IP is the default, no special matching needed
        case "trill":
          etherTypeValue = 0x22F3;
          break;
        case "macsec":
          etherTypeValue = 0x88E5;
          break;
        case "fcoe":
          etherTypeValue = 0x8906;
          break;
        case "lldp":
          etherTypeValue = 0x88CC;
          break;
        case "mpls":
        case "mpls-unicast":
          etherTypeValue = 0x8847;
          break;
        default:
          warnings.redFlagf(
              "Unknown etherType name in contract %s entry %s: %s",
              contractName, entryName, etherType);
          return null;
      }
    }

    // Match on the etherType
    switch (etherTypeValue) {
      case 0x0800: // IPv4
        return null; // No filtering needed for IPv4 (default)
      case 0x0806: // ARP
        // ARP is L2 only, won't match IP traffic
        warnings.redFlagf(
            "ARP etherType specified in contract %s entry %s: ARP filtering has limited effect in"
                + " IP ACLs",
            contractName, entryName);
        return AclLineMatchExprs.FALSE;
      case 0x86DD: // IPv6
        return AclLineMatchExprs.matchIpProtocol(IpProtocol.IPV6);
      case 0x8847: // MPLS unicast
      case 0x8848: // MPLS multicast
        // MPLS is L2.5 - warn about limited effect
        warnings.redFlagf(
            "MPLS etherType specified in contract %s entry %s: MPLS filtering has limited effect"
                + " in IP ACLs",
            contractName, entryName);
        return AclLineMatchExprs.FALSE; // Will not match any IP traffic
      default:
        // Other non-IP etherTypes
        warnings.redFlagf(
            "Non-IP etherType (0x%04x) specified in contract %s entry %s: This will not match IP"
                + " traffic",
            etherTypeValue, contractName, entryName);
        return AclLineMatchExprs.FALSE;
    }
  }

  /**
   * Parses ICMP type and code into a match expression.
   *
   * <p>Supports:
   *
   * <ul>
   *   <li>Type only: "8" for echo request
   *   <li>Type and code: "8:0" for echo request with code 0
   *   <li>Named types: "echo-request", "echo-reply", "destination-unreachable", etc.
   * </ul>
   *
   * @param icmpType The ICMP type string
   * @param icmpCode The ICMP code string (optional)
   * @param warnings Warnings container
   * @return A match expression for ICMP type/code
   */
  private static @Nonnull AclLineMatchExpr toIcmpTypeCode(
      @Nullable String icmpType, @Nullable String icmpCode, Warnings warnings) {
    int typeValue = 0;
    int codeValue = -1; // -1 means no code specified

    // Parse ICMP type
    if (icmpType != null) {
      String typeStr = icmpType.toLowerCase().trim();
      // Handle named ICMP types
      typeValue = parseIcmpTypeName(typeStr);
      if (typeValue == -1) {
        // Not a named type, try numeric
        try {
          typeValue = Integer.parseInt(typeStr);
          if (typeValue < 0 || typeValue > 255) {
            warnings.redFlagf("Invalid ICMP type: %s (must be 0-255)", icmpType);
            typeValue = 0;
          }
        } catch (NumberFormatException e) {
          warnings.redFlagf("Invalid ICMP type: %s", icmpType);
          typeValue = 0;
        }
      }
    }

    // Parse ICMP code if present
    if (icmpCode != null) {
      String codeStr = icmpCode.toLowerCase().trim();
      try {
        codeValue = Integer.parseInt(codeStr);
        if (codeValue < 0 || codeValue > 255) {
          warnings.redFlagf("Invalid ICMP code: %s (must be 0-255)", icmpCode);
          codeValue = 0;
        }
      } catch (NumberFormatException e) {
        warnings.redFlagf("Invalid ICMP code: %s", icmpCode);
        codeValue = 0;
      }
    }

    // Build match expression
    if (codeValue >= 0) {
      return AclLineMatchExprs.and(
          AclLineMatchExprs.matchIcmpType(typeValue), AclLineMatchExprs.matchIcmpCode(codeValue));
    } else {
      return AclLineMatchExprs.matchIcmpType(typeValue);
    }
  }

  /**
   * Parses a named ICMP type to its numeric value.
   *
   * @param typeName The ICMP type name
   * @return The numeric ICMP type, or -1 if not found
   */
  private static int parseIcmpTypeName(String typeName) {
    switch (typeName) {
      case "echo-reply":
        return 0;
      case "destination-unreachable":
      case "dest-unreachable":
        return 3;
      case "source-quench":
        return 4;
      case "redirect":
        return 5;
      case "alternate-host-address":
        return 6;
      case "echo-request":
      case "echo":
        return 8;
      case "router-advertisement":
      case "router-advert":
        return 9;
      case "router-solicitation":
      case "router-solicit":
        return 10;
      case "time-exceeded":
      case "ttl-exceeded":
        return 11;
      case "parameter-problem":
      case "parameter-prob":
        return 12;
      case "timestamp-request":
      case "timestamp":
        return 13;
      case "timestamp-reply":
        return 14;
      case "information-request":
      case "info-request":
        return 15;
      case "information-reply":
      case "info-reply":
        return 16;
      case "address-mask-request":
      case "mask-request":
        return 17;
      case "address-mask-reply":
      case "mask-reply":
        return 18;
      default:
        return -1;
    }
  }

  /**
   * Converts port specifications to an IntegerSpace.
   *
   * <p>Supports:
   *
   * <ul>
   *   <li>Single ports: "80", "443"
   *   <li>Port ranges: "8080-8090"
   *   <li>Multiple specifications: ["80", "8080-8090", "443"]
   * </ul>
   *
   * @param ports List of port specification strings
   * @param contractName The contract name for warnings
   * @param filter The filter for warnings
   * @param isDestination True for destination ports, false for source ports
   * @param warnings Warnings container
   * @return An IntegerSpace containing the specified ports
   */
  private static @Nonnull IntegerSpace toPortSpace(
      List<String> ports,
      String contractName,
      AciConfiguration.Contract.Filter filter,
      boolean isDestination,
      Warnings warnings) {
    IntegerSpace.Builder portSpace = IntegerSpace.builder();
    String portType = isDestination ? "destination" : "source";

    for (String portStr : ports) {
      String ps = normalizePortSpecToken(portStr);
      if (ps == null) {
        continue;
      }

      // Check for range syntax (e.g., "8080-8090")
      if (ps.contains("-")) {
        String[] parts = ps.split("-", 2);
        if (parts.length == 2) {
          try {
            int start = Integer.parseInt(parts[0].trim());
            int end = Integer.parseInt(parts[1].trim());
            if (start < 0 || start > 65535 || end < 0 || end > 65535) {
              warnings.redFlagf(
                  "Invalid %s port range in contract %s filter %s: %s (ports must be 0-65535)",
                  portType, contractName, filter.getName(), portStr);
            } else if (start > end) {
              warnings.redFlagf(
                  "Invalid %s port range in contract %s filter %s: %s (start > end)",
                  portType, contractName, filter.getName(), portStr);
            } else {
              portSpace.including(new SubRange(start, end));
            }
          } catch (NumberFormatException e) {
            warnings.redFlagf(
                "Invalid %s port range in contract %s filter %s: %s",
                portType, contractName, filter.getName(), portStr);
          }
        } else {
          warnings.redFlagf(
              "Invalid %s port range in contract %s filter %s: %s",
              portType, contractName, filter.getName(), portStr);
        }
      } else {
        // Single port
        try {
          int port = Integer.parseInt(ps);
          if (port < 0 || port > 65535) {
            warnings.redFlagf(
                "Invalid %s port in contract %s filter %s: %s (must be 0-65535)",
                portType, contractName, filter.getName(), portStr);
          } else {
            portSpace.including(port);
          }
        } catch (NumberFormatException e) {
          warnings.redFlagf(
              "Invalid %s port in contract %s filter %s: %s",
              portType, contractName, filter.getName(), portStr);
        }
      }
    }

    return portSpace.build();
  }

  /**
   * Converts a list of port strings to an IntegerSpace.
   *
   * <p>This overload is for filter entries which use entry names instead of filter objects.
   *
   * @param ports List of port strings (single ports or ranges like "80" or "8080-8090")
   * @param contractName The contract name for warnings
   * @param entryName The entry name for warnings
   * @param isDestination True for destination ports, false for source ports
   * @param warnings Warnings container
   * @return An IntegerSpace containing the specified ports
   */
  private static @Nonnull IntegerSpace toPortSpace(
      List<String> ports,
      String contractName,
      String entryName,
      boolean isDestination,
      Warnings warnings) {
    IntegerSpace.Builder portSpace = IntegerSpace.builder();
    String portType = isDestination ? "destination" : "source";

    for (String portStr : ports) {
      String ps = normalizePortSpecToken(portStr);
      if (ps == null) {
        continue;
      }

      // Check for range syntax (e.g., "8080-8090")
      if (ps.contains("-")) {
        String[] parts = ps.split("-", 2);
        if (parts.length == 2) {
          try {
            int start = Integer.parseInt(parts[0].trim());
            int end = Integer.parseInt(parts[1].trim());
            if (start < 0 || start > 65535 || end < 0 || end > 65535) {
              warnings.redFlagf(
                  "Invalid %s port range in contract %s entry %s: %s (ports must be 0-65535)",
                  portType, contractName, entryName, portStr);
            } else if (start > end) {
              warnings.redFlagf(
                  "Invalid %s port range in contract %s entry %s: %s (start > end)",
                  portType, contractName, entryName, portStr);
            } else {
              portSpace.including(new SubRange(start, end));
            }
          } catch (NumberFormatException e) {
            warnings.redFlagf(
                "Invalid %s port range in contract %s entry %s: %s",
                portType, contractName, entryName, portStr);
          }
        } else {
          warnings.redFlagf(
              "Invalid %s port range in contract %s entry %s: %s",
              portType, contractName, entryName, portStr);
        }
      } else {
        // Single port
        try {
          int port = Integer.parseInt(ps);
          if (port < 0 || port > 65535) {
            warnings.redFlagf(
                "Invalid %s port in contract %s entry %s: %s (must be 0-65535)",
                portType, contractName, entryName, portStr);
          } else {
            portSpace.including(port);
          }
        } catch (NumberFormatException e) {
          warnings.redFlagf(
              "Invalid %s port in contract %s entry %s: %s",
              portType, contractName, entryName, portStr);
        }
      }
    }

    return portSpace.build();
  }

  private static boolean isPlaceholderPortToken(String token) {
    String normalized = token.trim().toLowerCase();
    return normalized.equals("unspecified") || normalized.equals("any") || normalized.equals("0");
  }

  private static @Nullable String normalizePortSpecToken(@Nullable String token) {
    if (token == null) {
      return null;
    }
    String trimmed = token.trim();
    if (trimmed.isEmpty()) {
      return null;
    }
    if (!trimmed.contains("-")) {
      return isPlaceholderPortToken(trimmed) ? null : trimmed;
    }
    String[] parts = trimmed.split("-", 2);
    if (parts.length != 2) {
      return trimmed;
    }
    // Treat placeholder endpoints as unset range metadata.
    if (isPlaceholderPortToken(parts[0]) || isPlaceholderPortToken(parts[1])) {
      return null;
    }
    return trimmed;
  }

  /**
   * Converts an IP address specification to a match expression.
   *
   * <p>Supports:
   *
   * <ul>
   *   <li>Single IP: "10.0.0.1"
   *   <li>Prefix: "10.0.0.0/24"
   *   <li>Wildcard: "10.0.0.0/0.0.0.255"
   *   <li>Any: "any", "0.0.0.0/0"
   * </ul>
   *
   * @param addrStr The address specification string
   * @param isSource True for source address, false for destination
   * @param warnings Warnings container
   * @return A match expression for the address, or null if invalid
   */
  private static @Nullable AclLineMatchExpr toIpMatchExpr(
      String addrStr, boolean isSource, Warnings warnings) {
    if (addrStr == null || addrStr.trim().isEmpty()) {
      return null;
    }

    String as = addrStr.trim().toLowerCase();

    // Handle "any" keyword
    if (as.equals("any") || as.equals("0.0.0.0/0") || as.equals("0.0.0.0/0.0.0.0")) {
      return null; // No filtering needed for "any"
    }

    // Try CIDR prefix notation first
    try {
      Prefix prefix = Prefix.parse(as);
      if (isSource) {
        return AclLineMatchExprs.matchSrc(prefix);
      } else {
        return AclLineMatchExprs.matchDst(prefix);
      }
    } catch (IllegalArgumentException e) {
      // Not a CIDR prefix, try wildcard notation
    }

    // Try IP/wildcard notation (e.g., "10.0.0.0/0.0.0.255")
    Pattern wildcardPattern = Pattern.compile("([\\d.]+)/([\\d.]+)");
    Matcher matcher = wildcardPattern.matcher(as);
    if (matcher.matches()) {
      try {
        Ip addr = Ip.parse(matcher.group(1));
        Ip wildcard = Ip.parse(matcher.group(2));
        IpWildcard ipWildcard = IpWildcard.ipWithWildcardMask(addr, wildcard);
        if (isSource) {
          return AclLineMatchExprs.matchSrc(ipWildcard);
        } else {
          return AclLineMatchExprs.matchDst(ipWildcard);
        }
      } catch (IllegalArgumentException e) {
        // Invalid wildcard format
      }
    }

    // Try single IP address
    try {
      Ip ip = Ip.parse(as);
      if (isSource) {
        return AclLineMatchExprs.matchSrc(ip);
      } else {
        return AclLineMatchExprs.matchDst(ip);
      }
    } catch (IllegalArgumentException e) {
      warnings.redFlagf("Invalid IP address specification: %s", addrStr);
      return null;
    }
  }

  private AciContractConverter() {}
}
