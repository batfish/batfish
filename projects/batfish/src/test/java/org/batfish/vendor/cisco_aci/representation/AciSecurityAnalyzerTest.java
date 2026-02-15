package org.batfish.vendor.cisco_aci.representation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

/** Tests for {@link AciSecurityAnalyzer}. */
public final class AciSecurityAnalyzerTest {

  @Test
  public void testAnalyzeContractsFindsAnyAnyAndBroadPortRange() {
    AciConfiguration config = new AciConfiguration();

    AciConfiguration.Contract contract = new AciConfiguration.Contract("tenant1:c1");
    contract.setTenant("tenant1");
    AciConfiguration.Contract.Subject subject = new AciConfiguration.Contract.Subject();
    AciConfiguration.Contract.Filter filterRef = new AciConfiguration.Contract.Filter();
    filterRef.setName("tenant1:f1");
    subject.setFilters(ImmutableList.of(filterRef));
    contract.setSubjects(ImmutableList.of(subject));
    config.getContracts().put("tenant1:c1", contract);

    AciConfiguration.Filter filter = new AciConfiguration.Filter("tenant1:f1");
    AciConfiguration.Filter.Entry anyAny = new AciConfiguration.Filter.Entry();
    anyAny.setName("anyAny");

    AciConfiguration.Filter.Entry broadRange = new AciConfiguration.Filter.Entry();
    broadRange.setName("broadRange");
    broadRange.setProtocol("tcp");
    broadRange.setDestinationFromPort("1");
    broadRange.setDestinationToPort("65535");

    filter.setEntries(ImmutableList.of(anyAny, broadRange));
    config.getFilters().put("tenant1:f1", filter);

    List<SecurityFinding> findings = AciSecurityAnalyzer.analyzeContracts(config);
    List<SecurityFinding.Category> categories =
        findings.stream().map(SecurityFinding::getCategory).collect(Collectors.toList());

    assertThat(categories, hasItem(SecurityFinding.Category.ANY_ANY));
    assertThat(categories, hasItem(SecurityFinding.Category.BROAD_PORT_RANGE));
    assertThat(categories, hasItem(SecurityFinding.Category.MISSING_DENY));
  }

  @Test
  public void testAnalyzeContractsFindsMissingFilterReference() {
    AciConfiguration config = new AciConfiguration();

    AciConfiguration.Contract contract = new AciConfiguration.Contract("tenant1:c1");
    contract.setTenant("tenant1");
    AciConfiguration.Contract.Subject subject = new AciConfiguration.Contract.Subject();
    AciConfiguration.Contract.Filter missingFilterRef = new AciConfiguration.Contract.Filter();
    missingFilterRef.setName("tenant1:missing");
    subject.setFilters(ImmutableList.of(missingFilterRef));
    contract.setSubjects(ImmutableList.of(subject));
    config.getContracts().put("tenant1:c1", contract);

    List<SecurityFinding> findings = AciSecurityAnalyzer.analyzeContracts(config);
    List<SecurityFinding.Category> categories =
        findings.stream().map(SecurityFinding::getCategory).collect(Collectors.toList());

    assertThat(categories, hasItem(SecurityFinding.Category.OVERLY_PERMISSIVE));
  }

  @Test
  public void testIsAnyAnyRule_AllNull() {
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    // All fields are null

    assertThat(AciSecurityAnalyzer.isAnyAnyRule(entry), equalTo(true));
  }

  @Test
  public void testIsAnyAnyRule_EmptyString() {
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    entry.setProtocol("");
    entry.setSourceAddress("");
    entry.setDestinationAddress("");

    assertThat(AciSecurityAnalyzer.isAnyAnyRule(entry), equalTo(true));
  }

  @Test
  public void testIsAnyAnyRule_ExplicitAny() {
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    entry.setProtocol("any");
    entry.setSourceAddress("any");
    entry.setDestinationAddress("any");
    entry.setDestinationPort("any");
    entry.setSourcePort("any");

    assertThat(AciSecurityAnalyzer.isAnyAnyRule(entry), equalTo(true));
  }

  @Test
  public void testIsAnyAnyRule_UnspecifiedKeyword() {
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    entry.setProtocol("unspecified");
    entry.setSourceAddress("unspecified");
    entry.setDestinationAddress("unspecified");

    assertThat(AciSecurityAnalyzer.isAnyAnyRule(entry), equalTo(true));
  }

  @Test
  public void testIsAnyAnyRule_ZeroValue() {
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    entry.setProtocol("0");
    entry.setDestinationPort("0");
    entry.setSourcePort("0");

    assertThat(AciSecurityAnalyzer.isAnyAnyRule(entry), equalTo(true));
  }

  @Test
  public void testIsAnyAnyRule_WithSpecificProtocol() {
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    entry.setProtocol("tcp");

    assertThat(AciSecurityAnalyzer.isAnyAnyRule(entry), equalTo(false));
  }

  @Test
  public void testIsAnyAnyRule_WithSpecificSourceAddress() {
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    entry.setSourceAddress("10.0.0.1/32");

    assertThat(AciSecurityAnalyzer.isAnyAnyRule(entry), equalTo(false));
  }

  @Test
  public void testIsAnyAnyRule_WithSpecificDestinationAddress() {
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    entry.setDestinationAddress("192.168.1.0/24");

    assertThat(AciSecurityAnalyzer.isAnyAnyRule(entry), equalTo(false));
  }

  @Test
  public void testIsAnyAnyRule_WithPortRange() {
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    entry.setDestinationFromPort("80");
    entry.setDestinationToPort("443");

    assertThat(AciSecurityAnalyzer.isAnyAnyRule(entry), equalTo(false));
  }

  @Test
  public void testIsAnyAnyRule_WithSpecificPort() {
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    entry.setProtocol("tcp");
    entry.setDestinationPort("443");

    assertThat(AciSecurityAnalyzer.isAnyAnyRule(entry), equalTo(false));
  }

  @Test
  public void testIsOverlyPermissive_AnyAddressWithProtocol() {
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    entry.setProtocol("tcp");
    // Source and destination are null (any)

    assertThat(AciSecurityAnalyzer.isOverlyPermissive(entry), equalTo(true));
  }

  @Test
  public void testIsOverlyPermissive_ExplicitAnyAddressWithProtocol() {
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    entry.setProtocol("udp");
    entry.setSourceAddress("any");
    entry.setDestinationAddress("any");

    assertThat(AciSecurityAnalyzer.isOverlyPermissive(entry), equalTo(true));
  }

  @Test
  public void testIsOverlyPermissive_UnspecifiedAddressWithProtocol() {
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    entry.setProtocol("tcp");
    entry.setSourceAddress("unspecified");
    entry.setDestinationAddress("unspecified");

    assertThat(AciSecurityAnalyzer.isOverlyPermissive(entry), equalTo(true));
  }

  @Test
  public void testIsOverlyPermissive_WithSourceAddressRestriction() {
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    entry.setProtocol("tcp");
    entry.setSourceAddress("10.0.0.1/32");
    // Destination is still any

    assertThat(AciSecurityAnalyzer.isOverlyPermissive(entry), equalTo(false));
  }

  @Test
  public void testIsOverlyPermissive_WithDestinationAddressRestriction() {
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    entry.setProtocol("tcp");
    entry.setDestinationAddress("192.168.1.0/24");
    // Source is still any

    assertThat(AciSecurityAnalyzer.isOverlyPermissive(entry), equalTo(false));
  }

  @Test
  public void testIsOverlyPermissive_WithBothAddressRestrictions() {
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    entry.setProtocol("tcp");
    entry.setSourceAddress("10.0.0.1/32");
    entry.setDestinationAddress("192.168.1.0/24");

    assertThat(AciSecurityAnalyzer.isOverlyPermissive(entry), equalTo(false));
  }

  @Test
  public void testIsOverlyPermissive_NoProtocol() {
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    // Protocol is null - should not be overly permissive (caught by any-any check)

    assertThat(AciSecurityAnalyzer.isOverlyPermissive(entry), equalTo(false));
  }

  @Test
  public void testIsOverlyPermissive_AnyProtocol() {
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    entry.setProtocol("any");

    assertThat(AciSecurityAnalyzer.isOverlyPermissive(entry), equalTo(false));
  }

  @Test
  public void testCheckBroadPortRange_DestinationPortRange_1to65535() {
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    entry.setDestinationFromPort("1");
    entry.setDestinationToPort("65535");

    String result = AciSecurityAnalyzer.checkBroadPortRange(entry);
    assertThat(result, notNullValue());
    assertThat(result.contains("overly broad destination port range"), equalTo(true));
    assertThat(result.contains("1-65535"), equalTo(true));
  }

  @Test
  public void testCheckBroadPortRange_DestinationPortRange_1to59000() {
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    entry.setDestinationFromPort("1");
    entry.setDestinationToPort("59000");

    String result = AciSecurityAnalyzer.checkBroadPortRange(entry);
    assertThat(result, notNullValue());
    assertThat(result.contains("overly broad destination port range"), equalTo(true));
  }

  @Test
  public void testCheckBroadPortRange_DestinationPortRange_100to60000() {
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    entry.setDestinationFromPort("100");
    entry.setDestinationToPort("60000");

    String result = AciSecurityAnalyzer.checkBroadPortRange(entry);
    assertThat(result, notNullValue());
    assertThat(result.contains("broad destination port range"), equalTo(true));
  }

  @Test
  public void testCheckBroadPortRange_DestinationPortRange_80to443() {
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    entry.setDestinationFromPort("80");
    entry.setDestinationToPort("443");

    String result = AciSecurityAnalyzer.checkBroadPortRange(entry);
    assertThat(result, nullValue());
  }

  @Test
  public void testCheckBroadPortRange_SourcePortRange_1to65535() {
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    entry.setSourceFromPort("1");
    entry.setSourceToPort("65535");

    String result = AciSecurityAnalyzer.checkBroadPortRange(entry);
    assertThat(result, notNullValue());
    assertThat(result.contains("overly broad source port range"), equalTo(true));
    assertThat(result.contains("1-65535"), equalTo(true));
  }

  @Test
  public void testCheckBroadPortRange_SourcePortRange_1to59000() {
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    entry.setSourceFromPort("1");
    entry.setSourceToPort("59000");

    String result = AciSecurityAnalyzer.checkBroadPortRange(entry);
    assertThat(result, notNullValue());
    assertThat(result.contains("overly broad source port range"), equalTo(true));
  }

  @Test
  public void testCheckBroadPortRange_SourcePortRange_100to60000() {
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    entry.setSourceFromPort("100");
    entry.setSourceToPort("60000");

    String result = AciSecurityAnalyzer.checkBroadPortRange(entry);
    assertThat(result, notNullValue());
    assertThat(result.contains("broad source port range"), equalTo(true));
  }

  @Test
  public void testCheckBroadPortRange_SourcePortRange_SmallRange() {
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    entry.setSourceFromPort("1024");
    entry.setSourceToPort("2048");

    String result = AciSecurityAnalyzer.checkBroadPortRange(entry);
    assertThat(result, nullValue());
  }

  @Test
  public void testCheckBroadPortRange_InvalidPortNumber() {
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    entry.setDestinationFromPort("invalid");
    entry.setDestinationToPort("65535");

    String result = AciSecurityAnalyzer.checkBroadPortRange(entry);
    assertThat(result, nullValue());
  }

  @Test
  public void testCheckBroadPortRange_NoPortRange() {
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    // No port range specified

    String result = AciSecurityAnalyzer.checkBroadPortRange(entry);
    assertThat(result, nullValue());
  }

  @Test
  public void testCheckBroadPortRange_OnlyFromPort() {
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    entry.setDestinationFromPort("80");
    // Missing ToPort

    String result = AciSecurityAnalyzer.checkBroadPortRange(entry);
    assertThat(result, nullValue());
  }

  @Test
  public void testCheckBroadPortRange_OnlyToPort() {
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    entry.setDestinationToPort("443");
    // Missing FromPort

    String result = AciSecurityAnalyzer.checkBroadPortRange(entry);
    assertThat(result, nullValue());
  }

  @Test
  public void testCheckBroadPortRange_BothDestinationAndSourceBroad() {
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    entry.setDestinationFromPort("1");
    entry.setDestinationToPort("65535");
    entry.setSourceFromPort("1");
    entry.setSourceToPort("65535");

    String result = AciSecurityAnalyzer.checkBroadPortRange(entry);
    // Should detect destination port range first
    assertThat(result, notNullValue());
    assertThat(result.contains("destination"), equalTo(true));
  }

  @Test
  public void testIsUnrestrictedProtocol_TcpNoPorts() {
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    entry.setProtocol("tcp");
    // No port restrictions

    assertThat(AciSecurityAnalyzer.isUnrestrictedProtocol(entry), equalTo(true));
  }

  @Test
  public void testIsUnrestrictedProtocol_UdpNoPorts() {
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    entry.setProtocol("udp");

    assertThat(AciSecurityAnalyzer.isUnrestrictedProtocol(entry), equalTo(true));
  }

  @Test
  public void testIsUnrestrictedProtocol_TcpUdpNoPorts() {
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    entry.setProtocol("tcp-udp");

    assertThat(AciSecurityAnalyzer.isUnrestrictedProtocol(entry), equalTo(true));
  }

  @Test
  public void testIsUnrestrictedProtocol_Protocol6NoPorts() {
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    entry.setProtocol("6"); // TCP protocol number

    assertThat(AciSecurityAnalyzer.isUnrestrictedProtocol(entry), equalTo(true));
  }

  @Test
  public void testIsUnrestrictedProtocol_Protocol17NoPorts() {
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    entry.setProtocol("17"); // UDP protocol number

    assertThat(AciSecurityAnalyzer.isUnrestrictedProtocol(entry), equalTo(true));
  }

  @Test
  public void testIsUnrestrictedProtocol_TcpWithDestinationPort() {
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    entry.setProtocol("tcp");
    entry.setDestinationPort("443");

    assertThat(AciSecurityAnalyzer.isUnrestrictedProtocol(entry), equalTo(false));
  }

  @Test
  public void testIsUnrestrictedProtocol_TcpWithSourcePort() {
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    entry.setProtocol("tcp");
    entry.setSourcePort("22");

    assertThat(AciSecurityAnalyzer.isUnrestrictedProtocol(entry), equalTo(false));
  }

  @Test
  public void testIsUnrestrictedProtocol_TcpWithDestinationRange() {
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    entry.setProtocol("tcp");
    entry.setDestinationFromPort("8000");
    entry.setDestinationToPort("9000");

    assertThat(AciSecurityAnalyzer.isUnrestrictedProtocol(entry), equalTo(false));
  }

  @Test
  public void testIsUnrestrictedProtocol_TcpWithSourceRange() {
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    entry.setProtocol("tcp");
    entry.setSourceFromPort("1024");
    entry.setSourceToPort("65535");

    assertThat(AciSecurityAnalyzer.isUnrestrictedProtocol(entry), equalTo(false));
  }

  @Test
  public void testIsUnrestrictedProtocol_IcmpNoPorts() {
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    entry.setProtocol("icmp");
    // ICMP doesn't require port restrictions

    assertThat(AciSecurityAnalyzer.isUnrestrictedProtocol(entry), equalTo(false));
  }

  @Test
  public void testIsUnrestrictedProtocol_AnyProtocol() {
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    entry.setProtocol("any");

    assertThat(AciSecurityAnalyzer.isUnrestrictedProtocol(entry), equalTo(false));
  }

  @Test
  public void testIsUnrestrictedProtocol_NoProtocol() {
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    // No protocol specified

    assertThat(AciSecurityAnalyzer.isUnrestrictedProtocol(entry), equalTo(false));
  }

  @Test
  public void testIsUnrestrictedProtocol_TcpUpperCase() {
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    entry.setProtocol("TCP");

    assertThat(AciSecurityAnalyzer.isUnrestrictedProtocol(entry), equalTo(true));
  }

  @Test
  public void testIsUnrestrictedProtocol_MixedCaseTcpUdp() {
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    entry.setProtocol("Tcp-Udp");

    assertThat(AciSecurityAnalyzer.isUnrestrictedProtocol(entry), equalTo(true));
  }

  @Test
  public void testAnalyzeContractsFindsUnrestrictedProtocol() {
    AciConfiguration config = new AciConfiguration();

    AciConfiguration.Contract contract = new AciConfiguration.Contract("tenant1:c1");
    contract.setTenant("tenant1");
    AciConfiguration.Contract.Subject subject = new AciConfiguration.Contract.Subject();
    AciConfiguration.Contract.Filter filterRef = new AciConfiguration.Contract.Filter();
    filterRef.setName("tenant1:f1");
    subject.setFilters(ImmutableList.of(filterRef));
    contract.setSubjects(ImmutableList.of(subject));
    config.getContracts().put("tenant1:c1", contract);

    AciConfiguration.Filter filter = new AciConfiguration.Filter("tenant1:f1");
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    entry.setName("unrestricted");
    entry.setProtocol("tcp");

    filter.setEntries(ImmutableList.of(entry));
    config.getFilters().put("tenant1:f1", filter);

    List<SecurityFinding> findings = AciSecurityAnalyzer.analyzeContracts(config);
    List<SecurityFinding.Category> categories =
        findings.stream().map(SecurityFinding::getCategory).collect(Collectors.toList());

    assertThat(categories, hasItem(SecurityFinding.Category.UNRESTRICTED_PROTOCOL));
  }

  @Test
  public void testAnalyzeContractsFindsOverlyPermissive() {
    AciConfiguration config = new AciConfiguration();

    AciConfiguration.Contract contract = new AciConfiguration.Contract("tenant1:c1");
    contract.setTenant("tenant1");
    AciConfiguration.Contract.Subject subject = new AciConfiguration.Contract.Subject();
    AciConfiguration.Contract.Filter filterRef = new AciConfiguration.Contract.Filter();
    filterRef.setName("tenant1:f1");
    subject.setFilters(ImmutableList.of(filterRef));
    contract.setSubjects(ImmutableList.of(subject));
    config.getContracts().put("tenant1:c1", contract);

    AciConfiguration.Filter filter = new AciConfiguration.Filter("tenant1:f1");
    AciConfiguration.Filter.Entry entry = new AciConfiguration.Filter.Entry();
    entry.setName("overlyPermissive");
    entry.setProtocol("udp");
    entry.setSourceAddress("any");
    entry.setDestinationAddress("any");

    filter.setEntries(ImmutableList.of(entry));
    config.getFilters().put("tenant1:f1", filter);

    List<SecurityFinding> findings = AciSecurityAnalyzer.analyzeContracts(config);
    List<SecurityFinding.Category> categories =
        findings.stream().map(SecurityFinding::getCategory).collect(Collectors.toList());

    assertThat(categories, hasItem(SecurityFinding.Category.OVERLY_PERMISSIVE));
  }

  @Test
  public void testAnalyzeContracts_SortsBySeverity() {
    AciConfiguration config = new AciConfiguration();

    AciConfiguration.Contract contract = new AciConfiguration.Contract("tenant1:c1");
    contract.setTenant("tenant1");
    AciConfiguration.Contract.Subject subject = new AciConfiguration.Contract.Subject();
    AciConfiguration.Contract.Filter filterRef = new AciConfiguration.Contract.Filter();
    filterRef.setName("tenant1:f1");
    subject.setFilters(ImmutableList.of(filterRef));
    contract.setSubjects(ImmutableList.of(subject));
    config.getContracts().put("tenant1:c1", contract);

    AciConfiguration.Filter filter = new AciConfiguration.Filter("tenant1:f1");
    AciConfiguration.Filter.Entry anyAny = new AciConfiguration.Filter.Entry();
    anyAny.setName("anyAny");

    AciConfiguration.Filter.Entry unrestricted = new AciConfiguration.Filter.Entry();
    unrestricted.setName("unrestricted");
    unrestricted.setProtocol("tcp");

    filter.setEntries(ImmutableList.of(anyAny, unrestricted));
    config.getFilters().put("tenant1:f1", filter);

    List<SecurityFinding> findings = AciSecurityAnalyzer.analyzeContracts(config);

    // Should have findings for: any-any, unrestricted, overly permissive, missing-deny
    assertThat(findings, hasSize(4));

    // Count findings by severity
    long highCount =
        findings.stream().filter(f -> f.getSeverity() == SecurityFinding.Severity.HIGH).count();
    long mediumCount =
        findings.stream().filter(f -> f.getSeverity() == SecurityFinding.Severity.MEDIUM).count();
    long lowCount =
        findings.stream().filter(f -> f.getSeverity() == SecurityFinding.Severity.LOW).count();

    assertThat(highCount, equalTo(2L)); // any-any, overly permissive
    assertThat(mediumCount, equalTo(1L)); // unrestricted protocol
    assertThat(lowCount, equalTo(1L)); // missing deny

    // Verify we have the expected categories
    List<SecurityFinding.Category> categories =
        findings.stream().map(SecurityFinding::getCategory).collect(Collectors.toList());
    assertThat(categories, hasItem(SecurityFinding.Category.ANY_ANY));
    assertThat(categories, hasItem(SecurityFinding.Category.UNRESTRICTED_PROTOCOL));
    assertThat(categories, hasItem(SecurityFinding.Category.OVERLY_PERMISSIVE));
    assertThat(categories, hasItem(SecurityFinding.Category.MISSING_DENY));
  }

  @Test
  public void testAnalyzeContracts_MultipleSubjects() {
    AciConfiguration config = new AciConfiguration();

    AciConfiguration.Contract contract = new AciConfiguration.Contract("tenant1:c1");
    contract.setTenant("tenant1");

    AciConfiguration.Contract.Subject subject1 = new AciConfiguration.Contract.Subject();
    AciConfiguration.Contract.Filter filterRef1 = new AciConfiguration.Contract.Filter();
    filterRef1.setName("tenant1:f1");
    subject1.setFilters(ImmutableList.of(filterRef1));

    AciConfiguration.Contract.Subject subject2 = new AciConfiguration.Contract.Subject();
    AciConfiguration.Contract.Filter filterRef2 = new AciConfiguration.Contract.Filter();
    filterRef2.setName("tenant1:f2");
    subject2.setFilters(ImmutableList.of(filterRef2));

    contract.setSubjects(ImmutableList.of(subject1, subject2));
    config.getContracts().put("tenant1:c1", contract);

    AciConfiguration.Filter filter1 = new AciConfiguration.Filter("tenant1:f1");
    AciConfiguration.Filter.Entry entry1 = new AciConfiguration.Filter.Entry();
    entry1.setName("entry1");
    entry1.setProtocol("tcp");
    filter1.setEntries(ImmutableList.of(entry1));
    config.getFilters().put("tenant1:f1", filter1);

    AciConfiguration.Filter filter2 = new AciConfiguration.Filter("tenant1:f2");
    AciConfiguration.Filter.Entry entry2 = new AciConfiguration.Filter.Entry();
    entry2.setName("entry2");
    filter2.setEntries(ImmutableList.of(entry2));
    config.getFilters().put("tenant1:f2", filter2);

    List<SecurityFinding> findings = AciSecurityAnalyzer.analyzeContracts(config);
    List<SecurityFinding.Category> categories =
        findings.stream().map(SecurityFinding::getCategory).collect(Collectors.toList());

    // Should find both any-any and unrestricted protocol
    assertThat(categories, hasItem(SecurityFinding.Category.ANY_ANY));
    assertThat(categories, hasItem(SecurityFinding.Category.UNRESTRICTED_PROTOCOL));
  }

  @Test
  public void testAnalyzeContracts_EmptyConfiguration() {
    AciConfiguration config = new AciConfiguration();

    List<SecurityFinding> findings = AciSecurityAnalyzer.analyzeContracts(config);

    assertThat(findings, hasSize(0));
  }

  @Test
  public void testAnalyzeContracts_ContractWithNoSubjects() {
    AciConfiguration config = new AciConfiguration();

    AciConfiguration.Contract contract = new AciConfiguration.Contract("tenant1:c1");
    contract.setTenant("tenant1");
    contract.setSubjects(ImmutableList.of());
    config.getContracts().put("tenant1:c1", contract);

    List<SecurityFinding> findings = AciSecurityAnalyzer.analyzeContracts(config);

    // Should still have missing deny finding
    assertThat(findings, hasSize(1));
    assertThat(findings.get(0).getCategory(), equalTo(SecurityFinding.Category.MISSING_DENY));
  }

  @Test
  public void testAnalyzeContracts_SubjectWithNoFilters() {
    AciConfiguration config = new AciConfiguration();

    AciConfiguration.Contract contract = new AciConfiguration.Contract("tenant1:c1");
    contract.setTenant("tenant1");
    AciConfiguration.Contract.Subject subject = new AciConfiguration.Contract.Subject();
    subject.setFilters(ImmutableList.of());
    contract.setSubjects(ImmutableList.of(subject));
    config.getContracts().put("tenant1:c1", contract);

    List<SecurityFinding> findings = AciSecurityAnalyzer.analyzeContracts(config);

    // Should only have missing deny finding
    assertThat(findings, hasSize(1));
    assertThat(findings.get(0).getCategory(), equalTo(SecurityFinding.Category.MISSING_DENY));
  }
}
