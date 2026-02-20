package org.batfish.vendor.cisco_aci.representation;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 * Analyzer for ACI contract usage patterns.
 *
 * <p>This analyzer identifies various contract-related issues including:
 *
 * <ul>
 *   <li>Unused contracts (defined but not consumed)
 *   <li>Unprovided contracts (defined but not provided)
 *   <li>Orphaned consumers (contracts consumed but not provided)
 *   <li>Orphaned providers (contracts provided but not consumed)
 *   <li>Duplicate contract names across tenants
 *   <li>Redundant contracts (identical filter rules)
 *   <li>Contracts with broken filter references
 * </ul>
 */
public final class AciContractUsageAnalyzer {

  private AciContractUsageAnalyzer() {}

  /**
   * Analyzes contract usage across all tenants in the ACI configuration.
   *
   * @param config The ACI configuration to analyze
   * @return List of findings describing potential issues
   */
  public static @Nonnull List<ContractUsageFinding> analyzeContractUsage(
      @Nonnull AciConfiguration config) {
    List<ContractUsageFinding> findings = new ArrayList<>();

    // Build maps of contract providers and consumers
    Multimap<String, String> contractProviders = HashMultimap.create();
    Multimap<String, String> contractConsumers = HashMultimap.create();
    Map<String, Tenant> tenantMap = new HashMap<>();

    // Collect all contract references from EPGs
    for (Tenant tenant : config.getTenants().values()) {
      tenantMap.put(tenant.getName(), tenant);

      for (Epg epg : tenant.getEpgs().values()) {
        for (String contractRef : epg.getProvidedContracts()) {
          contractProviders.put(contractRef, epg.getName());
        }
        for (String contractRef : epg.getConsumedContracts()) {
          contractConsumers.put(contractRef, epg.getName());
        }
      }
    }

    // Find unused and unprovided contracts
    findings.addAll(findUnusedContracts(config, contractConsumers));
    findings.addAll(findUnprovidedContracts(config, contractProviders));

    // Find orphaned consumers and providers
    findings.addAll(findOrphanedConsumers(config, contractProviders, contractConsumers));
    findings.addAll(findOrphanedProviders(config, contractProviders, contractConsumers));

    // Find duplicate contract names
    findings.addAll(findDuplicateContracts(config));

    // Find redundant contracts
    findings.addAll(findRedundantContracts(config));

    // Find contracts with broken filter references
    findings.addAll(findBrokenFilterReferences(config));

    return findings;
  }

  /**
   * Finds contracts that are defined but not consumed by any EPG.
   *
   * @param config The ACI configuration
   * @param contractConsumers Map of contract names to consuming EPGs
   * @return List of findings for unused contracts
   */
  @VisibleForTesting
  static List<ContractUsageFinding> findUnusedContracts(
      @Nonnull AciConfiguration config, @Nonnull Multimap<String, String> contractConsumers) {
    List<ContractUsageFinding> findings = new ArrayList<>();

    for (Contract contract : config.getContracts().values()) {
      String fqContractName = contract.getName();

      if (!contractConsumers.containsKey(fqContractName)) {
        String tenantName = contract.getTenant() != null ? contract.getTenant() : "unknown";

        findings.add(
            new ContractUsageFinding(
                ContractUsageFinding.Severity.MEDIUM,
                ContractUsageFinding.Category.UNUSED,
                extractContractName(fqContractName),
                tenantName,
                String.format(
                    "Contract '%s' is defined but not consumed by any EPG", fqContractName),
                "Remove the unused contract or associate it with an EPG that should consume it"));
      }
    }

    return findings;
  }

  /**
   * Finds contracts that are defined but not provided by any EPG.
   *
   * @param config The ACI configuration
   * @param contractProviders Map of contract names to providing EPGs
   * @return List of findings for unprovided contracts
   */
  @VisibleForTesting
  static List<ContractUsageFinding> findUnprovidedContracts(
      @Nonnull AciConfiguration config, @Nonnull Multimap<String, String> contractProviders) {
    List<ContractUsageFinding> findings = new ArrayList<>();

    for (Contract contract : config.getContracts().values()) {
      String fqContractName = contract.getName();

      if (!contractProviders.containsKey(fqContractName)) {
        String tenantName = contract.getTenant() != null ? contract.getTenant() : "unknown";

        findings.add(
            new ContractUsageFinding(
                ContractUsageFinding.Severity.MEDIUM,
                ContractUsageFinding.Category.UNPROVIDED,
                extractContractName(fqContractName),
                tenantName,
                String.format(
                    "Contract '%s' is defined but not provided by any EPG", fqContractName),
                "Remove the unused contract or associate it with an EPG that should provide it"));
      }
    }

    return findings;
  }

  /**
   * Finds contracts that are consumed but not provided by any EPG.
   *
   * <p>This represents a configuration issue where EPGs are trying to consume a contract that no
   * EPG provides, which will result in no traffic being allowed.
   *
   * @param config The ACI configuration
   * @param contractProviders Map of contract names to providing EPGs
   * @param contractConsumers Map of contract names to consuming EPGs
   * @return List of findings for orphaned consumers
   */
  @VisibleForTesting
  static List<ContractUsageFinding> findOrphanedConsumers(
      @Nonnull AciConfiguration config,
      @Nonnull Multimap<String, String> contractProviders,
      @Nonnull Multimap<String, String> contractConsumers) {
    List<ContractUsageFinding> findings = new ArrayList<>();

    for (String contractRef : contractConsumers.keySet()) {
      if (!contractProviders.containsKey(contractRef)) {
        Collection<String> consumers = contractConsumers.get(contractRef);
        String tenantName = extractTenantName(contractRef);

        findings.add(
            new ContractUsageFinding(
                ContractUsageFinding.Severity.HIGH,
                ContractUsageFinding.Category.ORPHANED_CONSUMER,
                extractContractName(contractRef),
                tenantName,
                String.format(
                    "Contract '%s' is consumed by EPG(s) %s but not provided by any EPG",
                    contractRef, consumers),
                "Configure an EPG to provide this contract, or remove the contract reference from"
                    + " the consuming EPG(s)"));
      }
    }

    return findings;
  }

  /**
   * Finds contracts that are provided but not consumed by any EPG.
   *
   * <p>This represents a configuration issue where EPGs are providing a contract that no EPG
   * consumes, which may indicate incomplete configuration or stale policy definitions.
   *
   * @param config The ACI configuration
   * @param contractProviders Map of contract names to providing EPGs
   * @param contractConsumers Map of contract names to consuming EPGs
   * @return List of findings for orphaned providers
   */
  @VisibleForTesting
  static List<ContractUsageFinding> findOrphanedProviders(
      @Nonnull AciConfiguration config,
      @Nonnull Multimap<String, String> contractProviders,
      @Nonnull Multimap<String, String> contractConsumers) {
    List<ContractUsageFinding> findings = new ArrayList<>();

    for (String contractRef : contractProviders.keySet()) {
      if (!contractConsumers.containsKey(contractRef)) {
        Collection<String> providers = contractProviders.get(contractRef);
        String tenantName = extractTenantName(contractRef);

        findings.add(
            new ContractUsageFinding(
                ContractUsageFinding.Severity.LOW,
                ContractUsageFinding.Category.ORPHANED_PROVIDER,
                extractContractName(contractRef),
                tenantName,
                String.format(
                    "Contract '%s' is provided by EPG(s) %s but not consumed by any EPG",
                    contractRef, providers),
                "Configure an EPG to consume this contract, or remove the contract reference from"
                    + " the providing EPG(s)"));
      }
    }

    return findings;
  }

  /**
   * Finds duplicate contract names across different tenants.
   *
   * <p>While ACI allows contracts with the same name in different tenants (they are scoped by
   * tenant), having duplicate names can cause confusion. This analysis identifies such cases.
   *
   * @param config The ACI configuration
   * @return List of findings for duplicate contract names
   */
  @VisibleForTesting
  static List<ContractUsageFinding> findDuplicateContracts(@Nonnull AciConfiguration config) {
    List<ContractUsageFinding> findings = new ArrayList<>();

    // Group contracts by name (without tenant prefix)
    Multimap<String, Contract> contractsByName = HashMultimap.create();
    for (Contract contract : config.getContracts().values()) {
      String contractName = extractContractName(contract.getName());
      contractsByName.put(contractName, contract);
    }

    // Find contracts with the same name in different tenants
    for (Map.Entry<String, Collection<Contract>> entry : contractsByName.asMap().entrySet()) {
      Collection<Contract> contracts = entry.getValue();

      if (contracts.size() > 1) {
        // Found duplicate names
        Set<String> tenants =
            contracts.stream()
                .map(c -> c.getTenant() != null ? c.getTenant() : "unknown")
                .collect(Collectors.toSet());

        if (tenants.size() > 1) {
          // Different tenants have contracts with the same name
          String contractName = entry.getKey();
          String tenantsList = String.join(", ", tenants);

          for (Contract contract : contracts) {
            String tenantName = contract.getTenant() != null ? contract.getTenant() : "unknown";

            findings.add(
                new ContractUsageFinding(
                    ContractUsageFinding.Severity.LOW,
                    ContractUsageFinding.Category.DUPLICATE,
                    contractName,
                    tenantName,
                    String.format(
                        "Contract '%s' also exists in tenant(s): %s", contractName, tenantsList),
                    "Consider using unique contract names across tenants to avoid confusion"));
          }
        }
      }
    }

    return findings;
  }

  /**
   * Finds contracts with identical filter rules (redundant contracts).
   *
   * <p>Redundant contracts have the same filter definitions, which may indicate duplicate policy
   * definitions that could be consolidated.
   *
   * @param config The ACI configuration
   * @return List of findings for redundant contracts
   */
  @VisibleForTesting
  static List<ContractUsageFinding> findRedundantContracts(@Nonnull AciConfiguration config) {
    List<ContractUsageFinding> findings = new ArrayList<>();

    // Build a map of filter signatures to contracts
    Map<String, Set<String>> signatureToContracts = new HashMap<>();

    for (Contract contract : config.getContracts().values()) {
      String signature = computeContractFilterSignature(contract);

      if (!signature.isEmpty()) {
        signatureToContracts
            .computeIfAbsent(signature, k -> new HashSet<>())
            .add(contract.getName());
      }
    }

    // Find contracts with identical filter signatures
    for (Map.Entry<String, Set<String>> entry : signatureToContracts.entrySet()) {
      Set<String> contracts = entry.getValue();

      if (contracts.size() > 1) {
        // Found redundant contracts
        String contractsList = String.join(", ", contracts);

        for (String contractName : contracts) {
          String tenantName = extractTenantName(contractName);
          String shortName = extractContractName(contractName);

          findings.add(
              new ContractUsageFinding(
                  ContractUsageFinding.Severity.INFO,
                  ContractUsageFinding.Category.REDUNDANT,
                  shortName,
                  tenantName,
                  String.format(
                      "Contract '%s' has identical filter rules as: %s",
                      contractName, contractsList),
                  "Consider consolidating redundant contracts into a single contract"));
        }
      }
    }

    return findings;
  }

  /**
   * Finds contracts that reference non-existent filters.
   *
   * <p>This identifies broken references where a contract subject references a filter that doesn't
   * exist in the configuration.
   *
   * @param config The ACI configuration
   * @return List of findings for broken filter references
   */
  @VisibleForTesting
  static List<ContractUsageFinding> findBrokenFilterReferences(@Nonnull AciConfiguration config) {
    List<ContractUsageFinding> findings = new ArrayList<>();

    // Collect all existing filter names
    Set<String> existingFilters = new HashSet<>();
    for (Tenant tenant : config.getTenants().values()) {
      existingFilters.addAll(tenant.getFilters().keySet());
    }

    // Check all contract filter references
    for (Contract contract : config.getContracts().values()) {
      String tenantName = contract.getTenant() != null ? contract.getTenant() : "unknown";
      String contractName = extractContractName(contract.getName());

      for (Contract.Subject subject : contract.getSubjects()) {
        for (Contract.FilterRef filterRef : subject.getFilters()) {
          String filterName = filterRef.getName();

          // Check if filter exists (with or without tenant prefix)
          boolean filterExists = existingFilters.contains(filterName);
          if (!filterExists) {
            // Try with tenant prefix
            String fqFilterName = tenantName + ":" + filterName;
            filterExists = existingFilters.contains(fqFilterName);
          }

          if (!filterExists) {
            findings.add(
                new ContractUsageFinding(
                    ContractUsageFinding.Severity.HIGH,
                    ContractUsageFinding.Category.BROKEN_REFERENCE,
                    contractName,
                    tenantName,
                    String.format(
                        "Contract '%s' references non-existent filter '%s'",
                        contract.getName(), filterName),
                    "Create the missing filter or update the contract to reference an existing"
                        + " filter"));
          }
        }
      }
    }

    return findings;
  }

  /**
   * Computes a signature for a contract's filter rules to detect redundancy.
   *
   * @param contract The contract to analyze
   * @return A string signature representing the contract's filter rules
   */
  @VisibleForTesting
  static String computeContractFilterSignature(@Nonnull Contract contract) {
    StringBuilder signature = new StringBuilder();

    for (Contract.Subject subject : contract.getSubjects()) {
      for (Contract.FilterRef filter : subject.getFilters()) {
        signature.append(filter.getName());
        signature.append("|");
        signature.append(filter.getEtherType());
        signature.append("|");
        signature.append(filter.getIpProtocol());
        signature.append("|");
        signature.append(filter.getSourcePorts());
        signature.append("|");
        signature.append(filter.getDestinationPorts());
        signature.append("|");
        signature.append(filter.getSourceAddress());
        signature.append("|");
        signature.append(filter.getDestinationAddress());
        signature.append(";");
      }
    }

    return signature.toString();
  }

  /**
   * Extracts the contract name from a fully-qualified contract name.
   *
   * @param fqContractName The fully-qualified contract name (e.g., "tenant1:contract1")
   * @return The contract name without tenant prefix
   */
  @VisibleForTesting
  static String extractContractName(@Nonnull String fqContractName) {
    int colonIndex = fqContractName.indexOf(':');
    return colonIndex > 0 ? fqContractName.substring(colonIndex + 1) : fqContractName;
  }

  /**
   * Extracts the tenant name from a fully-qualified contract name.
   *
   * @param fqContractName The fully-qualified contract name (e.g., "tenant1:contract1")
   * @return The tenant name
   */
  @VisibleForTesting
  static String extractTenantName(@Nonnull String fqContractName) {
    int colonIndex = fqContractName.indexOf(':');
    return colonIndex > 0 ? fqContractName.substring(0, colonIndex) : "unknown";
  }
}
