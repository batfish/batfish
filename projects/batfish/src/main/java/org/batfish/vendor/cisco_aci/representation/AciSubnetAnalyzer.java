package org.batfish.vendor.cisco_aci.representation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Prefix;

/**
 * Analyzer for detecting subnet overlap issues in ACI bridge domains.
 *
 * <p>This analyzer detects:
 *
 * <ul>
 *   <li>Overlapping subnets within the same VRF (CRITICAL - will cause routing issues)
 *   <li>Overlapping subnets across different VRFs (INFO - may be intentional with VRF-lite)
 *   <li>Identical subnets in different bridge domains (HIGH - duplicate configuration)
 *   <li>Bridge domains with no subnets configured (LOW - may be intentional)
 *   <li>Invalid subnet formats or mask lengths (MEDIUM - configuration error)
 * </ul>
 */
public final class AciSubnetAnalyzer {

  private AciSubnetAnalyzer() {}

  /**
   * Analyzes all subnets across all bridge domains in the ACI configuration.
   *
   * @param config The ACI configuration to analyze
   * @return List of subnet findings sorted by severity
   */
  public static @Nonnull List<SubnetFinding> analyzeSubnets(AciConfiguration config) {
    Builder<SubnetFinding> findings = new ImmutableList.Builder<>();

    // Collect all bridge domains with their subnets
    List<BridgeDomainInfo> bdInfoList = new ArrayList<>();
    for (Entry<String, AciConfiguration.Tenant> tenantEntry : config.getTenants().entrySet()) {
      String tenantName = tenantEntry.getKey();
      AciConfiguration.Tenant tenant = tenantEntry.getValue();

      for (Entry<String, AciConfiguration.BridgeDomain> bdEntry :
          tenant.getBridgeDomains().entrySet()) {
        String bdName = bdEntry.getKey();
        AciConfiguration.BridgeDomain bd = bdEntry.getValue();

        bdInfoList.add(new BridgeDomainInfo(tenantName, bdName, bd));
      }
    }

    // Check for bridge domains with no subnets
    findings.addAll(checkBridgeDomainsNoSubnets(bdInfoList));

    // Check for overlapping subnets
    findings.addAll(checkOverlappingSubnets(bdInfoList));

    // Check for duplicate subnets
    findings.addAll(checkDuplicateSubnets(bdInfoList));

    // Validate subnet formats
    findings.addAll(validateSubnetFormats(bdInfoList));

    return findings.build();
  }

  /**
   * Checks for bridge domains with no subnets configured.
   *
   * @param bdInfoList List of bridge domain information
   * @return List of findings for bridge domains without subnets
   */
  private static List<SubnetFinding> checkBridgeDomainsNoSubnets(
      List<BridgeDomainInfo> bdInfoList) {
    Builder<SubnetFinding> findings = new ImmutableList.Builder<>();

    for (BridgeDomainInfo bdInfo : bdInfoList) {
      if (bdInfo._subnets.isEmpty()) {
        findings.add(
            new SubnetFinding(
                SubnetFinding.Severity.LOW,
                SubnetFinding.Category.NO_SUBNET,
                bdInfo._fqBdName,
                bdInfo._vrf != null ? bdInfo._vrf : "None",
                "N/A",
                null,
                null,
                null,
                String.format(
                    "Bridge domain '%s' in tenant '%s' has no subnets configured",
                    bdInfo._bdName, bdInfo._tenant),
                "Verify this is intentional. Bridge domains without subnets cannot provide"
                    + " IP gateway services. Add subnets if this bridge domain should support"
                    + " Layer 3 forwarding."));
      }
    }

    return findings.build();
  }

  /**
   * Checks for overlapping subnets within and across VRFs.
   *
   * @param bdInfoList List of bridge domain information
   * @return List of findings for overlapping subnets
   */
  private static List<SubnetFinding> checkOverlappingSubnets(List<BridgeDomainInfo> bdInfoList) {
    Builder<SubnetFinding> findings = new ImmutableList.Builder<>();

    // Group subnets by VRF
    Map<String, List<SubnetInfo>> subnetsByVrf = new HashMap<>();
    List<SubnetInfo> allSubnets = new ArrayList<>();

    for (BridgeDomainInfo bdInfo : bdInfoList) {
      String vrfKey = bdInfo._vrf != null ? bdInfo._vrf : "NO_VRF";
      for (String subnetStr : bdInfo._subnets) {
        SubnetInfo subnetInfo =
            new SubnetInfo(bdInfo._tenant, bdInfo._bdName, bdInfo._fqBdName, vrfKey, subnetStr);
        subnetsByVrf.computeIfAbsent(vrfKey, k -> new ArrayList<>()).add(subnetInfo);
        allSubnets.add(subnetInfo);
      }
    }

    // Check for overlaps within the same VRF (CRITICAL)
    for (Entry<String, List<SubnetInfo>> vrfEntry : subnetsByVrf.entrySet()) {
      List<SubnetInfo> vrfSubnets = vrfEntry.getValue();
      for (int i = 0; i < vrfSubnets.size(); i++) {
        for (int j = i + 1; j < vrfSubnets.size(); j++) {
          SubnetInfo subnet1 = vrfSubnets.get(i);
          SubnetInfo subnet2 = vrfSubnets.get(j);

          // Skip if comparing the same bridge domain (multiple subnets in same BD is OK)
          if (subnet1._fqBdName.equals(subnet2._fqBdName)) {
            continue;
          }

          // Skip identical subnets (handled by duplicate check)
          if (subnet1._subnet.equals(subnet2._subnet)) {
            continue;
          }

          if (subnetsOverlap(subnet1._subnet, subnet2._subnet)) {
            findings.add(
                new SubnetFinding(
                    SubnetFinding.Severity.CRITICAL,
                    SubnetFinding.Category.OVERLAP_SAME_VRF,
                    subnet1._fqBdName,
                    subnet1._vrf,
                    subnet1._subnet,
                    subnet2._fqBdName,
                    subnet2._vrf,
                    subnet2._subnet,
                    String.format(
                        "Overlapping subnets in VRF '%s': '%s' in BD '%s' overlaps with '%s' in BD"
                            + " '%s'",
                        subnet1._vrf,
                        subnet1._subnet,
                        subnet1._bdName,
                        subnet2._subnet,
                        subnet2._bdName),
                    "Overlapping subnets in the same VRF will cause routing conflicts. Reconfigure"
                        + " the subnets to use non-overlapping ranges, or move one of the bridge"
                        + " domains to a different VRF."));
          }
        }
      }
    }

    // Check for overlaps across different VRFs (INFO - may be intentional)
    for (int i = 0; i < allSubnets.size(); i++) {
      for (int j = i + 1; j < allSubnets.size(); j++) {
        SubnetInfo subnet1 = allSubnets.get(i);
        SubnetInfo subnet2 = allSubnets.get(j);

        // Same VRF already handled above
        if (subnet1._vrf.equals(subnet2._vrf)) {
          continue;
        }

        // Skip identical subnets (handled by duplicate check)
        if (subnet1._subnet.equals(subnet2._subnet)) {
          continue;
        }

        if (subnetsOverlap(subnet1._subnet, subnet2._subnet)) {
          findings.add(
              new SubnetFinding(
                  SubnetFinding.Severity.INFO,
                  SubnetFinding.Category.OVERLAP_DIFF_VRF,
                  subnet1._fqBdName,
                  subnet1._vrf,
                  subnet1._subnet,
                  subnet2._fqBdName,
                  subnet2._vrf,
                  subnet2._subnet,
                  String.format(
                      "Overlapping subnets across VRFs: '%s' in VRF '%s' (BD '%s') overlaps with"
                          + " '%s' in VRF '%s' (BD '%s')",
                      subnet1._subnet,
                      subnet1._vrf,
                      subnet1._bdName,
                      subnet2._subnet,
                      subnet2._vrf,
                      subnet2._bdName),
                  "Overlapping subnets in different VRFs may be intentional (VRF-lite scenario)."
                      + " Verify this is the desired design. If not, reconfigure subnets to avoid"
                      + " overlap."));
        }
      }
    }

    return findings.build();
  }

  /**
   * Checks for duplicate (identical) subnets in different bridge domains.
   *
   * @param bdInfoList List of bridge domain information
   * @return List of findings for duplicate subnets
   */
  private static List<SubnetFinding> checkDuplicateSubnets(List<BridgeDomainInfo> bdInfoList) {
    Builder<SubnetFinding> findings = new ImmutableList.Builder<>();

    // Group by subnet string
    Map<String, List<SubnetInfo>> subnetsByStr = new HashMap<>();

    for (BridgeDomainInfo bdInfo : bdInfoList) {
      String vrfKey = bdInfo._vrf != null ? bdInfo._vrf : "NO_VRF";
      for (String subnetStr : bdInfo._subnets) {
        SubnetInfo subnetInfo =
            new SubnetInfo(bdInfo._tenant, bdInfo._bdName, bdInfo._fqBdName, vrfKey, subnetStr);
        subnetsByStr.computeIfAbsent(subnetStr, k -> new ArrayList<>()).add(subnetInfo);
      }
    }

    // Find duplicates
    for (Entry<String, List<SubnetInfo>> entry : subnetsByStr.entrySet()) {
      List<SubnetInfo> subnetInfos = entry.getValue();
      if (subnetInfos.size() > 1) {
        // Found duplicate subnet in multiple bridge domains
        for (int i = 0; i < subnetInfos.size(); i++) {
          for (int j = i + 1; j < subnetInfos.size(); j++) {
            SubnetInfo subnet1 = subnetInfos.get(i);
            SubnetInfo subnet2 = subnetInfos.get(j);

            // Only report if in different bridge domains
            if (!subnet1._fqBdName.equals(subnet2._fqBdName)) {
              findings.add(
                  new SubnetFinding(
                      SubnetFinding.Severity.HIGH,
                      SubnetFinding.Category.DUPLICATE,
                      subnet1._fqBdName,
                      subnet1._vrf,
                      entry.getKey(),
                      subnet2._fqBdName,
                      subnet2._vrf,
                      entry.getKey(),
                      String.format(
                          "Duplicate subnet '%s' configured in multiple bridge domains: '%s' and"
                              + " '%s'",
                          entry.getKey(), subnet1._bdName, subnet2._bdName),
                      "Duplicate subnets in different bridge domains may indicate a configuration"
                          + " error. Verify if this is intentional. If the same subnet is needed in"
                          + " multiple locations, ensure they are in different VRFs or use"
                          + " different subnet ranges."));
            }
          }
        }
      }
    }

    return findings.build();
  }

  /**
   * Validates subnet formats and mask lengths.
   *
   * @param bdInfoList List of bridge domain information
   * @return List of findings for invalid subnet formats
   */
  private static List<SubnetFinding> validateSubnetFormats(List<BridgeDomainInfo> bdInfoList) {
    Builder<SubnetFinding> findings = new ImmutableList.Builder<>();

    for (BridgeDomainInfo bdInfo : bdInfoList) {
      for (String subnetStr : bdInfo._subnets) {
        try {
          Prefix prefix = Prefix.parse(subnetStr);

          // Check for reasonable mask lengths
          int prefixLength = prefix.getPrefixLength();
          if (prefixLength > 32) {
            findings.add(
                new SubnetFinding(
                    SubnetFinding.Severity.MEDIUM,
                    SubnetFinding.Category.INVALID_FORMAT,
                    bdInfo._fqBdName,
                    bdInfo._vrf != null ? bdInfo._vrf : "None",
                    subnetStr,
                    null,
                    null,
                    null,
                    String.format(
                        "Invalid subnet '%s' in bridge domain '%s': prefix length %d exceeds"
                            + " maximum of 32",
                        subnetStr, bdInfo._bdName, prefixLength),
                    "Correct the subnet prefix length to a valid value (0-32 for IPv4)."));
          } else if (prefixLength > 30) {
            findings.add(
                new SubnetFinding(
                    SubnetFinding.Severity.LOW,
                    SubnetFinding.Category.INVALID_FORMAT,
                    bdInfo._fqBdName,
                    bdInfo._vrf != null ? bdInfo._vrf : "None",
                    subnetStr,
                    null,
                    null,
                    null,
                    String.format(
                        "Unusually small subnet '%s' in bridge domain '%s': /%d provides only %d"
                            + " addresses",
                        subnetStr,
                        bdInfo._bdName,
                        prefixLength,
                        (int) Math.pow(2, 32 - prefixLength)),
                    "Verify this small subnet is intentional. Consider using a larger subnet for"
                        + " better address allocation."));
          }
        } catch (IllegalArgumentException e) {
          findings.add(
              new SubnetFinding(
                  SubnetFinding.Severity.MEDIUM,
                  SubnetFinding.Category.INVALID_FORMAT,
                  bdInfo._fqBdName,
                  bdInfo._vrf != null ? bdInfo._vrf : "None",
                  subnetStr,
                  null,
                  null,
                  null,
                  String.format(
                      "Invalid subnet format '%s' in bridge domain '%s': %s",
                      subnetStr, bdInfo._bdName, e.getMessage()),
                  "Correct the subnet format to use valid CIDR notation (e.g., 10.1.1.0/24)."));
        }
      }
    }

    return findings.build();
  }

  /**
   * Compares two subnets to determine if they overlap.
   *
   * @param subnet1 First subnet in CIDR notation
   * @param subnet2 Second subnet in CIDR notation
   * @return true if the subnets overlap, false otherwise
   */
  public static boolean subnetsOverlap(String subnet1, String subnet2) {
    try {
      Prefix p1 = Prefix.parse(subnet1);
      Prefix p2 = Prefix.parse(subnet2);

      // Two subnets overlap if one contains the other
      return p1.containsPrefix(p2) || p2.containsPrefix(p1);
    } catch (IllegalArgumentException e) {
      // If we can't parse either subnet, they don't overlap
      return false;
    }
  }

  /** Internal class to hold bridge domain information for analysis. */
  private static class BridgeDomainInfo {
    final String _tenant;
    final String _bdName;
    final String _fqBdName;
    final @Nullable String _vrf;
    final List<String> _subnets;

    BridgeDomainInfo(String tenant, String bdName, AciConfiguration.BridgeDomain bd) {
      _tenant = tenant;
      // bdName is fully qualified (tenant:bd), extract short name
      _bdName = bdName.substring(bdName.indexOf(':') + 1);
      _fqBdName = bdName;
      _vrf = bd.getVrf();
      _subnets = bd.getSubnets() != null ? bd.getSubnets() : ImmutableList.of();
    }
  }

  /** Internal class to hold subnet information for comparison. */
  private static class SubnetInfo {
    final String _tenant;
    final String _bdName;
    final String _fqBdName;
    final String _vrf;
    final String _subnet;

    SubnetInfo(String tenant, String bdName, String fqBdName, String vrf, String subnet) {
      _tenant = tenant;
      _bdName = bdName;
      _fqBdName = fqBdName;
      _vrf = vrf;
      _subnet = subnet;
    }
  }
}
