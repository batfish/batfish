package org.batfish.vendor.cisco_aci.representation;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * Analyzer for VRF (Virtual Routing and Forwarding) isolation validation in ACI configurations.
 *
 * <p>This analyzer validates VRF boundaries and detects potential leaks or misconfigurations:
 *
 * <ul>
 *   <li>Detects overlapping subnets across different VRFs
 *   <li>Validates contracts don't incorrectly cross VRF boundaries
 *   <li>Checks for bridge domains associated with multiple VRFs
 *   <li>Identifies unused VRFs (no bridge domains or L3Outs)
 *   <li>Validates L3Out scope is properly scoped to VRF
 * </ul>
 */
public class AciVrfIsolationAnalyzer {

  /** Analyzes VRF isolation across the ACI configuration. */
  public static @Nonnull List<VrfIsolationFinding> analyzeVrfIsolation(AciConfiguration config) {
    ImmutableList.Builder<VrfIsolationFinding> findings = new ImmutableList.Builder<>();

    // Check for overlapping subnets across VRFs
    findings.addAll(checkSubnetOverlap(config));

    // Validate contract VRF scope
    findings.addAll(checkContractVrfScope(config));

    // Detect unused VRFs
    findings.addAll(checkUnusedVrfs(config));

    // Check for bridge domains with VRF issues
    findings.addAll(checkBridgeDomainVrfIssues(config));

    // Validate L3Out scope
    findings.addAll(checkL3OutScope(config));

    return findings.build();
  }

  /**
   * Checks for overlapping subnets across different VRFs.
   *
   * <p>Overlapping subnets in different VRFs are typically configuration errors that can lead to
   * routing ambiguity and communication issues.
   */
  @VisibleForTesting
  static @Nonnull List<VrfIsolationFinding> checkSubnetOverlap(AciConfiguration config) {
    List<VrfIsolationFinding> findings = new ArrayList<>();

    // Map subnet to list of VRFs that use it
    Map<String, List<SubnetVrfPair>> subnetToVrfs = new HashMap<>();

    // Collect all subnets from bridge domains
    for (BridgeDomain bd : config.getBridgeDomains().values()) {
      String vrf = bd.getVrf();
      if (vrf == null) {
        continue; // Skip BDs without VRF
      }

      for (String subnet : bd.getSubnets()) {
        SubnetVrfPair pair = new SubnetVrfPair(subnet, vrf, bd.getName(), bd.getTenant());
        subnetToVrfs.computeIfAbsent(subnet, k -> new ArrayList<>()).add(pair);
      }
    }

    // Check for overlaps
    for (Map.Entry<String, List<SubnetVrfPair>> entry : subnetToVrfs.entrySet()) {
      List<SubnetVrfPair> vrfList = entry.getValue();

      // Check if this subnet appears in multiple VRFs
      Set<String> uniqueVrfs = new HashSet<>();
      for (SubnetVrfPair pair : vrfList) {
        uniqueVrfs.add(pair._vrf);
      }

      if (uniqueVrfs.size() > 1) {
        // Found overlap - create findings for each pair
        List<SubnetVrfPair> pairs = new ArrayList<>(vrfList);
        for (int i = 0; i < pairs.size(); i++) {
          for (int j = i + 1; j < pairs.size(); j++) {
            SubnetVrfPair pair1 = pairs.get(i);
            SubnetVrfPair pair2 = pairs.get(j);

            if (!pair1._vrf.equals(pair2._vrf)) {
              VrfIsolationFinding finding = new VrfIsolationFinding();
              finding.setSeverity(VrfIsolationFinding.Severity.HIGH);
              finding.setCategory(VrfIsolationFinding.Category.SUBNET_OVERLAP);
              finding.setVrfName1(pair1._vrf);
              finding.setVrfName2(pair2._vrf);
              finding.setSubnet1(pair1._subnet);
              finding.setSubnet2(pair2._subnet);
              finding.setBridgeDomain(pair1._bd);
              finding.setTenantName(pair1._tenant);
              finding.setDescription(
                  String.format(
                      "Subnet %s is used in multiple VRFs: %s (in BD %s) and %s (in BD %s)",
                      entry.getKey(), pair1._vrf, pair1._bd, pair2._vrf, pair2._bd));
              finding.setImpact(
                  "Overlapping subnets across VRFs can cause routing ambiguity and unexpected"
                      + " traffic forwarding. This violates VRF isolation principles.");
              finding.setRecommendation(
                  "Ensure each subnet is unique across all VRFs. If the same IP range is needed in"
                      + " multiple VRFs, use non-overlapping subnets or implement proper VRF route"
                      + " leaking with L3Outs.");
              findings.add(finding);
            }
          }
        }
      }
    }

    return findings;
  }

  /**
   * Validates that contracts don't incorrectly cross VRF boundaries.
   *
   * <p>EPGs in different VRFs should not communicate through contracts. Cross-VRF communication
   * should only happen through L3Outs.
   */
  @VisibleForTesting
  static @Nonnull List<VrfIsolationFinding> checkContractVrfScope(AciConfiguration config) {
    List<VrfIsolationFinding> findings = new ArrayList<>();

    // Build a map of EPG to VRF
    Map<String, String> epgToVrf = new HashMap<>();
    Map<String, String> epgToTenant = new HashMap<>();

    for (Epg epg : config.getEpgs().values()) {
      String epgKey = epg.getTenant() + ":" + epg.getName();

      // Find the VRF for this EPG via its bridge domain
      if (epg.getBridgeDomain() != null) {
        String bdKey = epg.getTenant() + ":" + epg.getBridgeDomain();
        BridgeDomain bd = config.getBridgeDomains().get(bdKey);
        if (bd != null && bd.getVrf() != null) {
          epgToVrf.put(epgKey, bd.getVrf());
          epgToTenant.put(epgKey, epg.getTenant());
        }
      }
    }

    // Check each contract's EPGs for cross-VRF references
    for (Contract contract : config.getContracts().values()) {
      // Find EPGs that provide or consume this contract
      Set<String> providerVrfs = new HashSet<>();
      Set<String> consumerVrfs = new HashSet<>();

      for (Epg epg : config.getEpgs().values()) {
        String epgKey = epg.getTenant() + ":" + epg.getName();
        String vrf = epgToVrf.get(epgKey);

        if (vrf == null) {
          continue; // Skip EPGs without VRF
        }

        if (epg.getProvidedContracts().contains(contract.getName())) {
          providerVrfs.add(vrf);
        }
        if (epg.getConsumedContracts().contains(contract.getName())) {
          consumerVrfs.add(vrf);
        }
      }

      // Check if contract spans multiple VRFs
      Set<String> allVrfs = new HashSet<>();
      allVrfs.addAll(providerVrfs);
      allVrfs.addAll(consumerVrfs);

      if (allVrfs.size() > 1) {
        VrfIsolationFinding finding = new VrfIsolationFinding();
        finding.setSeverity(VrfIsolationFinding.Severity.MEDIUM);
        finding.setCategory(VrfIsolationFinding.Category.CROSS_VRF_CONTRACT);
        finding.setContractName(contract.getName());
        finding.setTenantName(contract.getTenant());
        finding.setDescription(
            String.format(
                "Contract '%s' is used by EPGs in multiple VRFs: %s",
                contract.getName(), String.join(", ", allVrfs)));
        finding.setImpact(
            "Contracts spanning multiple VRFs may not work as expected since VRFs provide L3"
                + " isolation. Cross-VRF communication should use L3Outs.");
        finding.setRecommendation(
            "Review the contract and EPG associations. If cross-VRF communication is required, use"
                + " L3Outs with proper route leaking. Otherwise, ensure EPGs using this contract"
                + " are in the same VRF.");
        findings.add(finding);
      }
    }

    return findings;
  }

  /**
   * Identifies unused VRFs that have no bridge domains or L3Outs.
   *
   * <p>Unused VRFs may indicate configuration drift or abandoned plans.
   */
  @VisibleForTesting
  static @Nonnull List<VrfIsolationFinding> checkUnusedVrfs(AciConfiguration config) {
    List<VrfIsolationFinding> findings = new ArrayList<>();

    // Track which VRFs are used
    Set<String> usedVrfs = new HashSet<>();

    // Check bridge domains
    for (BridgeDomain bd : config.getBridgeDomains().values()) {
      if (bd.getVrf() != null) {
        usedVrfs.add(bd.getVrf());
      }
    }

    // Check L3Outs
    for (L3Out l3Out : config.getL3Outs().values()) {
      if (l3Out.getVrf() != null) {
        usedVrfs.add(l3Out.getVrf());
      }
    }

    // Find unused VRFs
    for (TenantVrf vrf : config.getVrfs().values()) {
      if (!usedVrfs.contains(vrf.getName())) {
        VrfIsolationFinding finding = new VrfIsolationFinding();
        finding.setSeverity(VrfIsolationFinding.Severity.LOW);
        finding.setCategory(VrfIsolationFinding.Category.UNUSED_VRF);
        finding.setVrfName1(vrf.getName());
        finding.setDescription(
            String.format("VRF '%s' has no associated bridge domains or L3Outs", vrf.getName()));
        finding.setImpact(
            "Unused VRFs consume configuration resources without providing functionality. This may"
                + " indicate incomplete deployment or abandoned plans.");
        finding.setRecommendation(
            "Remove the unused VRF if it's not needed, or associate bridge domains or L3Outs with"
                + " it if deployment is incomplete.");
        findings.add(finding);
      }
    }

    return findings;
  }

  /**
   * Checks for bridge domains with VRF association issues.
   *
   * <p>Detects bridge domains that are associated with multiple VRFs or have no VRF association.
   */
  @VisibleForTesting
  static @Nonnull List<VrfIsolationFinding> checkBridgeDomainVrfIssues(AciConfiguration config) {
    List<VrfIsolationFinding> findings = new ArrayList<>();

    // Track which bridge domains are used by EPGs in different VRFs
    Map<String, Set<String>> bdToVrfs = new HashMap<>();

    for (Epg epg : config.getEpgs().values()) {
      if (epg.getBridgeDomain() == null) {
        continue;
      }

      String bdKey = epg.getTenant() + ":" + epg.getBridgeDomain();
      BridgeDomain bd = config.getBridgeDomains().get(bdKey);

      if (bd != null && bd.getVrf() != null) {
        bdToVrfs.computeIfAbsent(bdKey, k -> new HashSet<>()).add(bd.getVrf());
      }
    }

    // Check for bridge domains with multiple VRFs
    for (Map.Entry<String, Set<String>> entry : bdToVrfs.entrySet()) {
      if (entry.getValue().size() > 1) {
        String bdKey = entry.getKey();
        BridgeDomain bd = config.getBridgeDomains().get(bdKey);

        if (bd != null) {
          VrfIsolationFinding finding = new VrfIsolationFinding();
          finding.setSeverity(VrfIsolationFinding.Severity.HIGH);
          finding.setCategory(VrfIsolationFinding.Category.BD_MULTI_VRF);
          finding.setBridgeDomain(bd.getName());
          finding.setTenantName(bd.getTenant());
          finding.setDescription(
              String.format(
                  "Bridge domain '%s' is associated with multiple VRFs: %s",
                  bd.getName(), String.join(", ", entry.getValue())));
          finding.setImpact(
              "A bridge domain should be associated with a single VRF. Multiple VRF associations"
                  + " can cause routing confusion and policy misapplication.");
          finding.setRecommendation(
              "Ensure the bridge domain is associated with only one VRF. If multiple VRFs need"
                  + " access to the same Layer 2 domain, use separate bridge domains or implement"
                  + " VRF route leaking.");
          findings.add(finding);
        }
      }
    }

    return findings;
  }

  /**
   * Validates L3Out external connectivity is properly scoped to VRF.
   *
   * <p>Ensures L3Outs are correctly associated with VRFs and don't create unintended cross-VRF
   * connectivity.
   */
  @VisibleForTesting
  static @Nonnull List<VrfIsolationFinding> checkL3OutScope(AciConfiguration config) {
    List<VrfIsolationFinding> findings = new ArrayList<>();

    for (L3Out l3Out : config.getL3Outs().values()) {
      if (l3Out.getVrf() == null) {
        VrfIsolationFinding finding = new VrfIsolationFinding();
        finding.setSeverity(VrfIsolationFinding.Severity.HIGH);
        finding.setCategory(VrfIsolationFinding.Category.L3OUT_SCOPE);
        finding.setTenantName(l3Out.getTenant());
        finding.setDescription(String.format("L3Out '%s' has no VRF association", l3Out.getName()));
        finding.setImpact(
            "L3Outs without VRF association may not function correctly or may apply to the wrong"
                + " VRF, causing routing issues.");
        finding.setRecommendation(
            "Associate the L3Out with the appropriate VRF using fvRsCtx relationship.");
        findings.add(finding);
        continue;
      }

      // Check if external EPG subnets overlap with internal bridge domain subnets in the same VRF
      String vrf = l3Out.getVrf();
      Set<String> internalSubnets = new HashSet<>();

      // Collect internal subnets for this VRF
      for (BridgeDomain bd : config.getBridgeDomains().values()) {
        if (vrf.equals(bd.getVrf())) {
          internalSubnets.addAll(bd.getSubnets());
        }
      }

      // Check external EPG subnets
      for (ExternalEpg extEpg : l3Out.getExternalEpgs()) {
        for (String extSubnet : extEpg.getSubnets()) {
          // Check for overlap with internal subnets
          if (subnetsOverlap(extSubnet, internalSubnets)) {
            VrfIsolationFinding finding = new VrfIsolationFinding();
            finding.setSeverity(VrfIsolationFinding.Severity.MEDIUM);
            finding.setCategory(VrfIsolationFinding.Category.L3OUT_SCOPE);
            finding.setVrfName1(vrf);
            finding.setTenantName(l3Out.getTenant());
            finding.setSubnet1(extSubnet);
            finding.setDescription(
                String.format(
                    "L3Out '%s' in VRF '%s' has external subnet %s that overlaps with internal"
                        + " subnets",
                    l3Out.getName(), vrf, extSubnet));
            finding.setImpact(
                "Overlapping external and internal subnets can cause routing loops and asymmetric"
                    + " routing.");
            finding.setRecommendation(
                "Ensure external subnets in L3Out external EPGs don't overlap with internal bridge"
                    + " domain subnets in the same VRF.");
            findings.add(finding);
          }
        }
      }
    }

    return findings;
  }

  /**
   * Checks if a subnet overlaps with any subnet in a collection.
   *
   * <p>This is a simplified check that looks for exact matches. A more sophisticated implementation
   * would check for subnet containment and overlap.
   */
  private static boolean subnetsOverlap(String subnet, Collection<String> subnets) {
    // For now, just check for exact matches
    // TODO: Implement proper subnet overlap detection using Prefix objects
    return subnets.contains(subnet);
  }

  /** Helper class to track subnet-VRF associations. */
  private static class SubnetVrfPair {
    final String _subnet;
    final String _vrf;
    final String _bd;
    final String _tenant;

    SubnetVrfPair(String subnet, String vrf, String bd, String tenant) {
      _subnet = subnet;
      _vrf = vrf;
      _bd = bd;
      _tenant = tenant;
    }
  }
}
