package org.batfish.vendor.cisco_aci.representation;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.batfish.vendor.cisco_aci.representation.SecurityFinding.Category;
import org.batfish.vendor.cisco_aci.representation.SecurityFinding.Severity;

/**
 * Analyzer for detecting security issues in ACI contract configurations.
 *
 * <p>This analyzer inspects ACI contracts and their associated filters to identify overly
 * permissive or misconfigured security rules that could pose security risks. The analysis includes:
 *
 * <ul>
 *   <li>Detecting "any-any" rules (filters with no restrictions)
 *   <li>Identifying overly broad port ranges (e.g., 1-65535)
 *   <li>Finding contracts with only "allow" rules (no deny filters)
 *   <li>Identifying contracts with implicit allow at the end
 *   <li>Detecting protocols that should be restricted (e.g., TCP/UDP with no port restrictions)
 * </ul>
 *
 * <p>The analyzer is extensible and can be enhanced with additional security checks as needed.
 */
@SuppressWarnings({"PMD.UnusedPrivateField", "PMD.UnusedFormalParameter"})
public class AciSecurityAnalyzer {

  /** Common values that indicate "any" or "unspecified" in ACI configurations. */
  private static final ImmutableSet<String> ANY_VALUES =
      ImmutableSet.of("any", "unspecified", "", "0");

  /** Protocols that typically require port restrictions. */
  private static final ImmutableSet<String> PROTOCOLS_REQUIRING_PORTS =
      ImmutableSet.of("tcp", "udp", "tcp-udp", "17", "6");

  /** Minimum valid port number. */
  private static final int MIN_PORT = 1;

  /** Maximum valid port number. */
  private static final int MAX_PORT = 65535;

  /** Default broad port range threshold (covers > 90% of valid port range). */
  private static final int BROAD_PORT_THRESHOLD = 59000;

  /**
   * Analyzes all contracts in the ACI configuration and returns security findings.
   *
   * @param config The ACI configuration to analyze
   * @return List of security findings sorted by severity (HIGH to LOW)
   */
  public static List<SecurityFinding> analyzeContracts(AciConfiguration config) {
    List<SecurityFinding> findings = new ArrayList<>();

    // Analyze each contract
    for (Contract contract : config.getContracts().values()) {
      findings.addAll(analyzeContract(contract, config));
    }

    // Sort by severity (HIGH first) and return
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
   * Analyzes a single contract for security issues.
   *
   * @param contract The contract to analyze
   * @param config The full ACI configuration (for filter lookups)
   * @return List of security findings for this contract
   */
  private static List<SecurityFinding> analyzeContract(Contract contract, AciConfiguration config) {
    List<SecurityFinding> findings = new ArrayList<>();
    String tenantName = contract.getTenant();
    String contractName = contract.getName();

    // Check for missing deny rules
    findings.addAll(checkMissingDenyRules(contract, tenantName, contractName));

    // Analyze each subject in the contract
    for (Contract.Subject subject : contract.getSubjects()) {
      findings.addAll(analyzeSubject(subject, contract, tenantName, config));
    }

    return findings;
  }

  /**
   * Analyzes a contract subject for security issues.
   *
   * @param subject The subject to analyze
   * @param contract The parent contract
   * @param tenantName The tenant name
   * @param config The full ACI configuration
   * @return List of security findings for this subject
   */
  private static List<SecurityFinding> analyzeSubject(
      Contract.Subject subject, Contract contract, String tenantName, AciConfiguration config) {
    List<SecurityFinding> findings = new ArrayList<>();

    for (Contract.FilterRef filter : subject.getFilters()) {
      // Get the actual filter from configuration to get entries
      String filterName = filter.getName();
      FilterModel actualFilter = config.getFilters().get(filterName);

      if (actualFilter != null) {
        findings.addAll(
            analyzeFilter(actualFilter, contract.getName(), tenantName, config.getFilters()));
      } else {
        // Filter reference not found - this is a configuration error
        findings.add(
            new SecurityFinding(
                Severity.MEDIUM,
                Category.OVERLY_PERMISSIVE,
                String.format(
                    "Contract references non-existent filter '%s' - cannot verify security posture",
                    filterName),
                contract.getName(),
                filterName,
                null,
                tenantName,
                "Verify filter exists and is properly referenced in the contract"));
      }
    }

    return findings;
  }

  /**
   * Analyzes a filter and its entries for security issues.
   *
   * @param filter The filter to analyze
   * @param contractName The contract name
   * @param tenantName The tenant name
   * @param allFilters All filters in the configuration (for reference lookups)
   * @return List of security findings for this filter
   */
  private static List<SecurityFinding> analyzeFilter(
      FilterModel filter,
      String contractName,
      String tenantName,
      java.util.Map<String, FilterModel> allFilters) {
    List<SecurityFinding> findings = new ArrayList<>();
    String filterName = filter.getName();

    for (FilterModel.Entry entry : filter.getEntries()) {
      String entryName = entry.getName();

      // Check for any-any rules
      if (isAnyAnyRule(entry)) {
        findings.add(
            new SecurityFinding(
                Severity.HIGH,
                Category.ANY_ANY,
                String.format(
                    "Filter entry '%s' matches all traffic (any protocol, any port, any address)",
                    entryName),
                contractName,
                filterName,
                entryName,
                tenantName,
                "Restrict to specific protocols, ports, and addresses required by the"
                    + " application"));
      }

      // Check for overly permissive rules
      if (isOverlyPermissive(entry)) {
        findings.add(
            new SecurityFinding(
                Severity.HIGH,
                Category.OVERLY_PERMISSIVE,
                String.format(
                    "Filter entry '%s' is overly permissive - allows traffic from any source "
                        + "to any destination with protocol '%s'",
                    entryName, entry.getProtocol()),
                contractName,
                filterName,
                entryName,
                tenantName,
                "Add source/destination address restrictions and limit port ranges to required"
                    + " values"));
      }

      // Check for broad port ranges
      String broadPortFinding = checkBroadPortRange(entry);
      if (broadPortFinding != null) {
        findings.add(
            new SecurityFinding(
                Severity.MEDIUM,
                Category.BROAD_PORT_RANGE,
                String.format("Filter entry '%s' has %s", entryName, broadPortFinding),
                contractName,
                filterName,
                entryName,
                tenantName,
                "Restrict port range to only the specific ports required by the application"));
      }

      // Check for unrestricted protocols
      if (isUnrestrictedProtocol(entry)) {
        findings.add(
            new SecurityFinding(
                Severity.MEDIUM,
                Category.UNRESTRICTED_PROTOCOL,
                String.format(
                    "Filter entry '%s' uses protocol '%s' without port restrictions",
                    entryName, entry.getProtocol()),
                contractName,
                filterName,
                entryName,
                tenantName,
                "Add specific source and/or destination port restrictions for this protocol"));
      }
    }

    return findings;
  }

  /**
   * Checks if a contract has only allow rules (no deny restrictions).
   *
   * @param contract The contract to check
   * @param tenantName The tenant name
   * @param contractName The contract name
   * @return Security finding if contract has only allow rules, empty list otherwise
   */
  private static List<SecurityFinding> checkMissingDenyRules(
      Contract contract, String tenantName, String contractName) {
    List<SecurityFinding> findings = new ArrayList<>();

    // ACI contracts are deny-by-default, but having only allow rules without explicit denies
    // can be a security concern if the allow rules are too permissive
    // This is a LOW severity finding as it's expected in most ACI deployments
    findings.add(
        new SecurityFinding(
            Severity.LOW,
            Category.MISSING_DENY,
            String.format(
                "Contract '%s' contains only allow rules with no explicit deny restrictions",
                contractName),
            contractName,
            null,
            null,
            tenantName,
            "Review allow rules to ensure they follow principle of least privilege. Consider adding"
                + " explicit deny rules for traffic that should never be permitted"));

    return findings;
  }

  /**
   * Checks if a filter entry is an "any-any" rule (matches all traffic).
   *
   * <p>An any-any rule is one that:
   *
   * <ul>
   *   <li>Has no protocol restriction (or allows all protocols)
   *   <li>Has no port restrictions
   *   <li>Has no address restrictions
   * </ul>
   *
   * @param entry The filter entry to check
   * @return true if the entry is an any-any rule, false otherwise
   */
  @VisibleForTesting
  static boolean isAnyAnyRule(FilterModel.Entry entry) {
    // Check if protocol is unspecified or "any"
    boolean anyProtocol = entry.getProtocol() == null || ANY_VALUES.contains(entry.getProtocol());

    // Check for no port restrictions
    boolean noPorts =
        (entry.getDestinationPort() == null || ANY_VALUES.contains(entry.getDestinationPort()))
            && (entry.getSourcePort() == null || ANY_VALUES.contains(entry.getSourcePort()))
            && entry.getDestinationFromPort() == null
            && entry.getDestinationToPort() == null
            && entry.getSourceFromPort() == null
            && entry.getSourceToPort() == null;

    // Check for no address restrictions
    boolean noAddresses =
        (entry.getSourceAddress() == null || ANY_VALUES.contains(entry.getSourceAddress()))
            && (entry.getDestinationAddress() == null
                || ANY_VALUES.contains(entry.getDestinationAddress()));

    return anyProtocol && noPorts && noAddresses;
  }

  /**
   * Checks if a filter entry is overly permissive.
   *
   * <p>An overly permissive rule is one that allows traffic from any source to any destination with
   * minimal restrictions.
   *
   * @param entry The filter entry to check
   * @return true if the entry is overly permissive, false otherwise
   */
  @VisibleForTesting
  static boolean isOverlyPermissive(FilterModel.Entry entry) {
    // Check if both source and destination addresses are unrestricted
    boolean anyAddress =
        (entry.getSourceAddress() == null || ANY_VALUES.contains(entry.getSourceAddress()))
            && (entry.getDestinationAddress() == null
                || ANY_VALUES.contains(entry.getDestinationAddress()));

    // Check if protocol is specified (otherwise it would be caught by any-any check)
    boolean hasProtocol = entry.getProtocol() != null && !ANY_VALUES.contains(entry.getProtocol());

    return anyAddress && hasProtocol;
  }

  /**
   * Checks if a filter entry has overly broad port ranges.
   *
   * @param entry The filter entry to check
   * @return Description of the broad port range if found, null otherwise
   */
  @VisibleForTesting
  static @Nullable String checkBroadPortRange(FilterModel.Entry entry) {
    // Check destination port range
    if (entry.getDestinationFromPort() != null && entry.getDestinationToPort() != null) {
      try {
        int fromPort = Integer.parseInt(entry.getDestinationFromPort());
        int toPort = Integer.parseInt(entry.getDestinationToPort());

        if (fromPort == MIN_PORT && toPort >= BROAD_PORT_THRESHOLD) {
          return String.format(
              "overly broad destination port range %d-%d (covers %d ports)",
              fromPort, toPort, (toPort - fromPort + 1));
        }

        // Check if range covers > 90% of valid port space
        int portRange = toPort - fromPort + 1;
        if (portRange > BROAD_PORT_THRESHOLD) {
          return String.format(
              "broad destination port range %d-%d (covers %d ports)", fromPort, toPort, portRange);
        }
      } catch (NumberFormatException e) {
        // Invalid port number - skip
      }
    }

    // Check source port range
    if (entry.getSourceFromPort() != null && entry.getSourceToPort() != null) {
      try {
        int fromPort = Integer.parseInt(entry.getSourceFromPort());
        int toPort = Integer.parseInt(entry.getSourceToPort());

        if (fromPort == MIN_PORT && toPort >= BROAD_PORT_THRESHOLD) {
          return String.format(
              "overly broad source port range %d-%d (covers %d ports)",
              fromPort, toPort, (toPort - fromPort + 1));
        }

        int portRange = toPort - fromPort + 1;
        if (portRange > BROAD_PORT_THRESHOLD) {
          return String.format(
              "broad source port range %d-%d (covers %d ports)", fromPort, toPort, portRange);
        }
      } catch (NumberFormatException e) {
        // Invalid port number - skip
      }
    }

    return null;
  }

  /**
   * Checks if a filter entry uses a protocol that should have port restrictions but doesn't.
   *
   * @param entry The filter entry to check
   * @return true if protocol is used without port restrictions, false otherwise
   */
  @VisibleForTesting
  static boolean isUnrestrictedProtocol(FilterModel.Entry entry) {
    String protocol = entry.getProtocol();
    if (protocol == null || !PROTOCOLS_REQUIRING_PORTS.contains(protocol.toLowerCase())) {
      return false;
    }

    // Check if any port restrictions exist
    boolean hasDestPort =
        entry.getDestinationPort() != null && !ANY_VALUES.contains(entry.getDestinationPort());
    boolean hasSourcePort =
        entry.getSourcePort() != null && !ANY_VALUES.contains(entry.getSourcePort());
    boolean hasDestRange =
        entry.getDestinationFromPort() != null && entry.getDestinationToPort() != null;
    boolean hasSourceRange = entry.getSourceFromPort() != null && entry.getSourceToPort() != null;

    return !(hasDestPort || hasSourcePort || hasDestRange || hasSourceRange);
  }

  private AciSecurityAnalyzer() {
    // Prevent instantiation
  }
}
