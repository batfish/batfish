package org.batfish.vendor.cisco_aci.representation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * Analyzer for validating EPG (Endpoint Group) reachability in Cisco ACI configurations.
 *
 * <p>This analyzer performs various validation checks on ACI configurations to ensure that EPGs can
 * communicate as intended based on contract configurations. It checks for:
 *
 * <ul>
 *   <li>EPGs in the same bridge domain (can communicate without contracts)
 *   <li>EPGs in different bridge domains (require contracts)
 *   <li>Orphaned EPGs (no contracts, no communication path)
 *   <li>Contracts referencing non-existent providers/consumers
 *   <li>EPGs with no path attachments (not connected to fabric)
 *   <li>Contracts with no filter rules
 * </ul>
 */
@SuppressWarnings("PMD.UnusedFormalParameter")
public class AciReachabilityAnalyzer {

  /** Status of reachability between two EPGs. */
  public enum ReachabilityStatus {
    /** EPGs can communicate (same BD or valid contract) */
    REACHABLE,
    /** EPGs cannot communicate (different BD, no contract) */
    UNREACHABLE,
    /** Reachability unknown (insufficient information) */
    UNKNOWN,
    /** EPGs are the same */
    SAME_EPG
  }

  /**
   * Analyzes EPG reachability for the entire ACI configuration.
   *
   * <p>This method performs a comprehensive analysis of all EPGs in the configuration and returns a
   * list of findings including:
   *
   * <ul>
   *   <li>Unreachable EPG pairs (no contract, different bridge domains)
   *   <li>Orphaned EPGs (no provider or consumer relationships)
   *   <li>Invalid contract references (provider/consumer EPG does not exist)
   *   <li>EPGs with no path attachments
   *   <li>Empty contracts (no filter rules)
   *   <li>Same-BD communication pairs (informational)
   * </ul>
   *
   * @param config The ACI configuration to analyze
   * @return List of reachability findings sorted by severity (HIGH first)
   */
  public static @Nonnull List<ReachabilityFinding> analyzeEpgReachability(AciConfiguration config) {
    List<ReachabilityFinding> findings = new ArrayList<>();

    // Collect all EPGs across all tenants
    Map<String, Epg> allEpgs = new HashMap<>();
    for (Tenant tenant : config.getTenants().values()) {
      allEpgs.putAll(tenant.getEpgs());
    }

    // Collect all contracts across all tenants
    Map<String, Contract> allContracts = new HashMap<>();
    for (Tenant tenant : config.getTenants().values()) {
      allContracts.putAll(tenant.getContracts());
    }

    // Check each EPG pair for reachability
    List<String> epgNames = new ArrayList<>(allEpgs.keySet());
    for (int i = 0; i < epgNames.size(); i++) {
      for (int j = i + 1; j < epgNames.size(); j++) {
        String epg1 = epgNames.get(i);
        String epg2 = epgNames.get(j);
        ReachabilityStatus status = canCommunicate(epg1, epg2, config, allEpgs);
        analyzeEpgPair(epg1, epg2, status, config, allEpgs, findings);
      }
    }

    // Find orphaned EPGs
    findings.addAll(findOrphanedEpgs(config, allEpgs));

    // Find invalid contract references
    findings.addAll(findInvalidContractReferences(config, allEpgs, allContracts));

    // Find empty contracts
    findings.addAll(findEmptyContracts(allContracts));

    // Find EPGs with no path attachments (if interface info available)
    findings.addAll(findEpgsWithoutPaths(config, allEpgs));

    // Sort by severity (HIGH first)
    findings.sort(
        (f1, f2) -> {
          int severityCompare = f2.getSeverity().compareTo(f1.getSeverity());
          if (severityCompare != 0) {
            return severityCompare;
          }
          return f1.getCategory().compareTo(f2.getCategory());
        });

    return ImmutableList.copyOf(findings);
  }

  /**
   * Determines if two EPGs can communicate based on their configuration.
   *
   * <p>EPGs can communicate if:
   *
   * <ul>
   *   <li>They are in the same bridge domain (no contract required)
   *   <li>There is a contract where one EPG is a provider and the other is a consumer
   * </ul>
   *
   * @param epg1 Fully-qualified name of the first EPG
   * @param epg2 Fully-qualified name of the second EPG
   * @param config The ACI configuration
   * @param allEpgs Map of all EPGs in the configuration
   * @return Reachability status between the two EPGs
   */
  private static @Nonnull ReachabilityStatus canCommunicate(
      String epg1, String epg2, AciConfiguration config, Map<String, Epg> allEpgs) {

    // Same EPG
    if (epg1.equals(epg2)) {
      return ReachabilityStatus.SAME_EPG;
    }

    Epg epg1Obj = allEpgs.get(epg1);
    Epg epg2Obj = allEpgs.get(epg2);

    // One or both EPGs don't exist
    if (epg1Obj == null || epg2Obj == null) {
      return ReachabilityStatus.UNKNOWN;
    }

    // Check if in same bridge domain
    String bd1 = epg1Obj.getBridgeDomain();
    String bd2 = epg2Obj.getBridgeDomain();

    if (bd1 != null && bd1.equals(bd2)) {
      return ReachabilityStatus.REACHABLE;
    }

    // Check for contract relationship
    // epg1 provides, epg2 consumes
    if (hasContractRelationship(epg1Obj, epg2Obj)) {
      return ReachabilityStatus.REACHABLE;
    }

    // epg2 provides, epg1 consumes
    if (hasContractRelationship(epg2Obj, epg1Obj)) {
      return ReachabilityStatus.REACHABLE;
    }

    // Different bridge domains, no contract
    return ReachabilityStatus.UNREACHABLE;
  }

  /**
   * Checks if there's a contract relationship between two EPGs.
   *
   * @param providerEpg The potential provider EPG
   * @param consumerEpg The potential consumer EPG
   * @return true if provider provides a contract that consumer consumes
   */
  private static boolean hasContractRelationship(Epg providerEpg, Epg consumerEpg) {

    Set<String> provided = new HashSet<>(providerEpg.getProvidedContracts());
    Set<String> consumed = new HashSet<>(consumerEpg.getConsumedContracts());

    return !Sets.intersection(provided, consumed).isEmpty();
  }

  /** Analyzes a pair of EPGs and adds findings as appropriate. */
  private static void analyzeEpgPair(
      String epg1,
      String epg2,
      ReachabilityStatus status,
      AciConfiguration config,
      Map<String, Epg> allEpgs,
      List<ReachabilityFinding> findings) {

    switch (status) {
      case SAME_EPG:
        // Skip, not a pair
        break;

      case REACHABLE:
        // Check if same-BD communication (informational)
        Epg epg1Obj = allEpgs.get(epg1);
        Epg epg2Obj = allEpgs.get(epg2);
        if (epg1Obj != null && epg2Obj != null) {
          String bd1 = epg1Obj.getBridgeDomain();
          String bd2 = epg2Obj.getBridgeDomain();
          if (bd1 != null && bd1.equals(bd2)) {
            findings.add(
                new ReachabilityFinding.Builder(
                        ReachabilityFinding.Severity.INFO,
                        ReachabilityFinding.Category.SAME_BD_COMMUNICATION,
                        String.format(
                            "EPGs '%s' and '%s' are in the same bridge domain '%s' "
                                + "and can communicate without a contract",
                            epg1, epg2, bd1))
                    .setSourceEpg(epg1)
                    .setDestEpg(epg2)
                    .setRecommendation(
                        "Consider adding a contract to explicitly define allowed traffic "
                            + "even though same-BD communication is permitted")
                    .build());
          }
        }
        break;

      case UNREACHABLE:
        // High severity - different BD, no contract
        findings.add(
            new ReachabilityFinding.Builder(
                    ReachabilityFinding.Severity.HIGH,
                    ReachabilityFinding.Category.NO_CONTRACT,
                    String.format(
                        "EPGs '%s' and '%s' are in different bridge domains "
                            + "and have no contract relationship - communication is blocked",
                        epg1, epg2))
                .setSourceEpg(epg1)
                .setDestEpg(epg2)
                .setRecommendation(
                    "Create a contract and configure one EPG as provider and the other as consumer,"
                        + " or move EPGs to the same bridge domain")
                .build());
        break;

      case UNKNOWN:
        // Should not happen with valid EPGs, skip
        break;
    }
  }

  /**
   * Finds orphaned EPGs that have no provider or consumer relationships.
   *
   * <p>An orphaned EPG has no contracts and no communication path to other EPGs (unless in same
   * BD). This may indicate misconfiguration or an EPG that is not yet fully configured.
   *
   * @param config The ACI configuration
   * @param allEpgs Map of all EPGs in the configuration
   * @return List of findings for orphaned EPGs
   */
  private static @Nonnull List<ReachabilityFinding> findOrphanedEpgs(
      AciConfiguration config, Map<String, Epg> allEpgs) {

    List<ReachabilityFinding> findings = new ArrayList<>();

    for (Map.Entry<String, Epg> entry : allEpgs.entrySet()) {
      String epgName = entry.getKey();
      Epg epg = entry.getValue();

      boolean hasProvided = !epg.getProvidedContracts().isEmpty();
      boolean hasConsumed = !epg.getConsumedContracts().isEmpty();

      if (!hasProvided && !hasConsumed) {
        // Check if there are other EPGs in the same BD
        String bd = epg.getBridgeDomain();
        boolean hasSameBdNeighbors = false;

        if (bd != null) {
          for (Epg other : allEpgs.values()) {
            if (other != epg && bd.equals(other.getBridgeDomain())) {
              hasSameBdNeighbors = true;
              break;
            }
          }
        }

        if (!hasSameBdNeighbors) {
          findings.add(
              new ReachabilityFinding.Builder(
                      ReachabilityFinding.Severity.MEDIUM,
                      ReachabilityFinding.Category.ORPHANED,
                      String.format(
                          "EPG '%s' has no provider or consumer contracts "
                              + "and no neighboring EPGs in its bridge domain",
                          epgName))
                  .setSourceEpg(epgName)
                  .setRecommendation(
                      "Configure contracts for this EPG or add it to a bridge domain with other"
                          + " EPGs")
                  .build());
        }
      }
    }

    return findings;
  }

  /**
   * Finds contracts that reference non-existent provider or consumer EPGs.
   *
   * @param config The ACI configuration
   * @param allEpgs Map of all EPGs in the configuration
   * @param allContracts Map of all contracts in the configuration
   * @return List of findings for invalid contract references
   */
  private static @Nonnull List<ReachabilityFinding> findInvalidContractReferences(
      AciConfiguration config, Map<String, Epg> allEpgs, Map<String, Contract> allContracts) {

    List<ReachabilityFinding> findings = new ArrayList<>();

    // For each EPG, check if its referenced contracts exist
    for (Map.Entry<String, Epg> epgEntry : allEpgs.entrySet()) {
      String epgName = epgEntry.getKey();
      Epg epg = epgEntry.getValue();

      // Check provided contracts
      for (String contractName : epg.getProvidedContracts()) {
        if (!allContracts.containsKey(contractName)) {
          findings.add(
              new ReachabilityFinding.Builder(
                      ReachabilityFinding.Severity.HIGH,
                      ReachabilityFinding.Category.INVALID_CONTRACT_REFERENCE,
                      String.format(
                          "EPG '%s' provides non-existent contract '%s'", epgName, contractName))
                  .setSourceEpg(epgName)
                  .setContract(contractName)
                  .setRecommendation(
                      "Create the missing contract or remove the reference from the EPG")
                  .build());
        }
      }

      // Check consumed contracts
      for (String contractName : epg.getConsumedContracts()) {
        if (!allContracts.containsKey(contractName)) {
          findings.add(
              new ReachabilityFinding.Builder(
                      ReachabilityFinding.Severity.HIGH,
                      ReachabilityFinding.Category.INVALID_CONTRACT_REFERENCE,
                      String.format(
                          "EPG '%s' consumes non-existent contract '%s'", epgName, contractName))
                  .setSourceEpg(epgName)
                  .setContract(contractName)
                  .setRecommendation(
                      "Create the missing contract or remove the reference from the EPG")
                  .build());
        }
      }
    }

    return findings;
  }

  /**
   * Finds contracts that have no filter rules defined.
   *
   * <p>An empty contract provides no value as it doesn't define any allowed traffic.
   *
   * @param allContracts Map of all contracts in the configuration
   * @return List of findings for empty contracts
   */
  private static @Nonnull List<ReachabilityFinding> findEmptyContracts(
      Map<String, Contract> allContracts) {

    List<ReachabilityFinding> findings = new ArrayList<>();

    for (Map.Entry<String, Contract> entry : allContracts.entrySet()) {
      String contractName = entry.getKey();
      Contract contract = entry.getValue();

      boolean hasFilters = false;
      if (contract.getSubjects() != null) {
        for (Contract.Subject subject : contract.getSubjects()) {
          if (subject.getFilters() != null && !subject.getFilters().isEmpty()) {
            hasFilters = true;
            break;
          }
        }
      }

      if (!hasFilters) {
        findings.add(
            new ReachabilityFinding.Builder(
                    ReachabilityFinding.Severity.MEDIUM,
                    ReachabilityFinding.Category.EMPTY_CONTRACT,
                    String.format("Contract '%s' has no filter rules defined", contractName))
                .setContract(contractName)
                .setRecommendation(
                    "Add filter rules to the contract's subjects to define allowed traffic")
                .build());
      }
    }

    return findings;
  }

  /**
   * Finds EPGs that are not attached to any path in the fabric.
   *
   * <p>This is a basic check that looks for EPGs without bridge domain associations. A more
   * sophisticated implementation would check for actual static/dynamic path bindings.
   *
   * @param config The ACI configuration
   * @param allEpgs Map of all EPGs in the configuration
   * @return List of findings for EPGs without path attachments
   */
  private static @Nonnull List<ReachabilityFinding> findEpgsWithoutPaths(
      AciConfiguration config, Map<String, Epg> allEpgs) {

    List<ReachabilityFinding> findings = new ArrayList<>();

    for (Map.Entry<String, Epg> entry : allEpgs.entrySet()) {
      String epgName = entry.getKey();
      Epg epg = entry.getValue();

      if (epg.getBridgeDomain() == null) {
        findings.add(
            new ReachabilityFinding.Builder(
                    ReachabilityFinding.Severity.MEDIUM,
                    ReachabilityFinding.Category.MISSING_PATH,
                    String.format(
                        "EPG '%s' is not associated with a bridge domain and has no fabric"
                            + " attachment",
                        epgName))
                .setSourceEpg(epgName)
                .setRecommendation(
                    "Associate the EPG with a bridge domain or configure static/dynamic path"
                        + " bindings")
                .build());
      }
    }

    return findings;
  }

  private AciReachabilityAnalyzer() {
    // Prevent instantiation
  }
}
