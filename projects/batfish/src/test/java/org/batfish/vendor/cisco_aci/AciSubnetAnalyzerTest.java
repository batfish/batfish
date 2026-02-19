package org.batfish.vendor.cisco_aci;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.vendor.cisco_aci.representation.AciConfiguration;
import org.batfish.vendor.cisco_aci.representation.AciSubnetAnalyzer;
import org.batfish.vendor.cisco_aci.representation.BridgeDomain;
import org.batfish.vendor.cisco_aci.representation.SubnetFinding;
import org.batfish.vendor.cisco_aci.representation.SubnetFinding.Category;
import org.batfish.vendor.cisco_aci.representation.SubnetFinding.Severity;
import org.batfish.vendor.cisco_aci.representation.Tenant;
import org.junit.Test;

/** Tests for {@link AciSubnetAnalyzer}. */
public class AciSubnetAnalyzerTest {

  @Test
  public void testNoFindingsWithValidNonOverlappingSubnets() {
    AciConfiguration config = new AciConfiguration();

    // Create tenant with VRF
    Tenant tenant = new Tenant("tenant1");
    BridgeDomain bd1 = new BridgeDomain("bd1");
    bd1.setTenant("tenant1");
    bd1.setVrf("tenant1:vrf1");
    bd1.setSubnets(ImmutableList.of("10.1.1.0/24"));

    BridgeDomain bd2 = new BridgeDomain("bd2");
    bd2.setTenant("tenant1");
    bd2.setVrf("tenant1:vrf1");
    bd2.setSubnets(ImmutableList.of("10.1.2.0/24"));

    BridgeDomain bd3 = new BridgeDomain("bd3");
    bd3.setTenant("tenant1");
    bd3.setVrf("tenant1:vrf1");
    bd3.setSubnets(ImmutableList.of("192.168.1.0/24"));

    tenant.getBridgeDomains().put("tenant1:bd1", bd1);
    tenant.getBridgeDomains().put("tenant1:bd2", bd2);
    tenant.getBridgeDomains().put("tenant1:bd3", bd3);
    config.getTenants().put("tenant1", tenant);
    config.finalizeStructures();

    List<SubnetFinding> findings = AciSubnetAnalyzer.analyzeSubnets(config);

    assertThat(findings, empty());
  }

  @Test
  public void testOverlappingSubnetsSameVrf() {
    AciConfiguration config = new AciConfiguration();

    // Create tenant with VRF
    Tenant tenant = new Tenant("tenant1");

    BridgeDomain bd1 = new BridgeDomain("bd1");
    bd1.setTenant("tenant1");
    bd1.setVrf("tenant1:vrf1");
    bd1.setSubnets(ImmutableList.of("10.1.1.0/24"));

    // bd2 has overlapping subnet (10.1.1.0/25 is within 10.1.1.0/24)
    BridgeDomain bd2 = new BridgeDomain("bd2");
    bd2.setTenant("tenant1");
    bd2.setVrf("tenant1:vrf1");
    bd2.setSubnets(ImmutableList.of("10.1.1.0/25"));

    tenant.getBridgeDomains().put("tenant1:bd1", bd1);
    tenant.getBridgeDomains().put("tenant1:bd2", bd2);
    config.getTenants().put("tenant1", tenant);
    config.finalizeStructures();

    List<SubnetFinding> findings = AciSubnetAnalyzer.analyzeSubnets(config);

    assertThat(findings, hasSize(1));
    SubnetFinding finding = findings.get(0);
    assertThat(finding.getSeverity(), equalTo(Severity.CRITICAL));
    assertThat(finding.getCategory(), equalTo(Category.OVERLAP_SAME_VRF));
    assertThat(finding.getSubnet1(), equalTo("10.1.1.0/24"));
    assertThat(finding.getSubnet2(), equalTo("10.1.1.0/25"));
    assertThat(finding.getBridgeDomain1(), equalTo("tenant1:bd1"));
    assertThat(finding.getBridgeDomain2(), equalTo("tenant1:bd2"));
  }

  @Test
  public void testOverlappingSubnetsDifferentVrfs() {
    AciConfiguration config = new AciConfiguration();

    // Create tenant with two VRFs
    Tenant tenant = new Tenant("tenant1");

    BridgeDomain bd1 = new BridgeDomain("bd1");
    bd1.setTenant("tenant1");
    bd1.setVrf("tenant1:vrf1");
    bd1.setSubnets(ImmutableList.of("10.1.1.0/24"));

    // Same subnet in different VRF
    BridgeDomain bd2 = new BridgeDomain("bd2");
    bd2.setTenant("tenant1");
    bd2.setVrf("tenant1:vrf2");
    bd2.setSubnets(ImmutableList.of("10.1.1.0/24"));

    tenant.getBridgeDomains().put("tenant1:bd1", bd1);
    tenant.getBridgeDomains().put("tenant1:bd2", bd2);
    config.getTenants().put("tenant1", tenant);
    config.finalizeStructures();

    List<SubnetFinding> findings = AciSubnetAnalyzer.analyzeSubnets(config);

    // Should generate DUPLICATE (HIGH) finding
    // Note: OVERLAP_DIFF_VRF is not generated for identical subnets since they're covered by
    // DUPLICATE
    assertThat(findings, hasSize(1));
    SubnetFinding duplicateFinding = findings.get(0);
    assertThat(duplicateFinding.getCategory(), equalTo(Category.DUPLICATE));
    assertThat(duplicateFinding.getSeverity(), equalTo(Severity.HIGH));
    assertThat(duplicateFinding.getSubnet1(), equalTo("10.1.1.0/24"));
    assertThat(duplicateFinding.getSubnet2(), equalTo("10.1.1.0/24"));
    assertThat(duplicateFinding.getVrf1(), equalTo("tenant1:vrf1"));
    assertThat(duplicateFinding.getVrf2(), equalTo("tenant1:vrf2"));
  }

  @Test
  public void testDuplicateSubnetsDifferentBridgeDomains() {
    AciConfiguration config = new AciConfiguration();

    Tenant tenant = new Tenant("tenant1");

    BridgeDomain bd1 = new BridgeDomain("web_bd");
    bd1.setTenant("tenant1");
    bd1.setVrf("tenant1:vrf1");
    bd1.setSubnets(ImmutableList.of("10.1.1.0/24"));

    BridgeDomain bd2 = new BridgeDomain("app_bd");
    bd2.setTenant("tenant1");
    bd2.setVrf("tenant1:vrf1");
    bd2.setSubnets(ImmutableList.of("10.1.1.0/24"));

    tenant.getBridgeDomains().put("tenant1:web_bd", bd1);
    tenant.getBridgeDomains().put("tenant1:app_bd", bd2);
    config.getTenants().put("tenant1", tenant);
    config.finalizeStructures();

    List<SubnetFinding> findings = AciSubnetAnalyzer.analyzeSubnets(config);

    assertThat(findings, hasSize(1));
    SubnetFinding finding = findings.get(0);
    assertThat(finding.getSeverity(), equalTo(Severity.HIGH));
    assertThat(finding.getCategory(), equalTo(Category.DUPLICATE));
    assertThat(finding.getSubnet1(), equalTo("10.1.1.0/24"));
    assertThat(finding.getSubnet2(), equalTo("10.1.1.0/24"));
    // Bridge domain order may vary
    assertThat(
        finding.getBridgeDomain1().equals("tenant1:web_bd")
            || finding.getBridgeDomain1().equals("tenant1:app_bd"),
        equalTo(true));
    // Order of BD names in description may vary
    assertThat(
        finding.getDescription(),
        equalTo(
            "Duplicate subnet '10.1.1.0/24' configured in multiple bridge domains:"
                + " 'app_bd' and 'web_bd'"));
  }

  @Test
  public void testBridgeDomainWithNoSubnets() {
    AciConfiguration config = new AciConfiguration();

    Tenant tenant = new Tenant("tenant1");

    BridgeDomain bd1 = new BridgeDomain("bd1");
    bd1.setTenant("tenant1");
    bd1.setVrf("tenant1:vrf1");
    // No subnets configured

    BridgeDomain bd2 = new BridgeDomain("bd2");
    bd2.setTenant("tenant1");
    bd2.setVrf("tenant1:vrf1");
    bd2.setSubnets(ImmutableList.of("10.1.1.0/24"));

    tenant.getBridgeDomains().put("tenant1:bd1", bd1);
    tenant.getBridgeDomains().put("tenant1:bd2", bd2);
    config.getTenants().put("tenant1", tenant);
    config.finalizeStructures();

    List<SubnetFinding> findings = AciSubnetAnalyzer.analyzeSubnets(config);

    assertThat(findings, hasSize(1));
    SubnetFinding finding = findings.get(0);
    assertThat(finding.getSeverity(), equalTo(Severity.LOW));
    assertThat(finding.getCategory(), equalTo(Category.NO_SUBNET));
    assertThat(finding.getBridgeDomain1(), equalTo("tenant1:bd1"));
    assertThat(finding.getSubnet1(), equalTo("N/A"));
  }

  @Test
  public void testInvalidSubnetFormat() {
    AciConfiguration config = new AciConfiguration();

    Tenant tenant = new Tenant("tenant1");

    BridgeDomain bd1 = new BridgeDomain("bd1");
    bd1.setTenant("tenant1");
    bd1.setVrf("tenant1:vrf1");
    bd1.setSubnets(ImmutableList.of("invalid-subnet"));

    BridgeDomain bd2 = new BridgeDomain("bd2");
    bd2.setTenant("tenant1");
    bd2.setVrf("tenant1:vrf1");
    bd2.setSubnets(ImmutableList.of("10.1.1.0/24"));

    tenant.getBridgeDomains().put("tenant1:bd1", bd1);
    tenant.getBridgeDomains().put("tenant1:bd2", bd2);
    config.getTenants().put("tenant1", tenant);
    config.finalizeStructures();

    List<SubnetFinding> findings = AciSubnetAnalyzer.analyzeSubnets(config);

    assertThat(findings, hasSize(1)); // Only invalid format finding
    SubnetFinding invalidFinding =
        findings.stream().filter(f -> f.getCategory() == Category.INVALID_FORMAT).findFirst().get();
    assertThat(invalidFinding.getSeverity(), equalTo(Severity.MEDIUM));
    assertThat(invalidFinding.getSubnet1(), equalTo("invalid-subnet"));
  }

  @Test
  public void testMultipleSubnetsInSameBridgeDomain() {
    AciConfiguration config = new AciConfiguration();

    Tenant tenant = new Tenant("tenant1");

    BridgeDomain bd1 = new BridgeDomain("bd1");
    bd1.setTenant("tenant1");
    bd1.setVrf("tenant1:vrf1");
    // Multiple non-overlapping subnets in same BD is OK
    bd1.setSubnets(ImmutableList.of("10.1.1.0/24", "10.1.2.0/24", "192.168.1.0/24"));

    tenant.getBridgeDomains().put("tenant1:bd1", bd1);
    config.getTenants().put("tenant1", tenant);
    config.finalizeStructures();

    List<SubnetFinding> findings = AciSubnetAnalyzer.analyzeSubnets(config);

    // No findings expected - multiple subnets in same BD is fine
    assertThat(findings, empty());
  }

  @Test
  public void testComplexOverlapScenario() {
    AciConfiguration config = new AciConfiguration();

    Tenant tenant = new Tenant("tenant1");

    // BD1: 10.1.0.0/16 (large range)
    BridgeDomain bd1 = new BridgeDomain("bd1");
    bd1.setTenant("tenant1");
    bd1.setVrf("tenant1:vrf1");
    bd1.setSubnets(ImmutableList.of("10.1.0.0/16"));

    // BD2: 10.1.1.0/24 (within BD1's range)
    BridgeDomain bd2 = new BridgeDomain("bd2");
    bd2.setTenant("tenant1");
    bd2.setVrf("tenant1:vrf1");
    bd2.setSubnets(ImmutableList.of("10.1.1.0/24"));

    // BD3: 10.2.0.0/16 (non-overlapping)
    BridgeDomain bd3 = new BridgeDomain("bd3");
    bd3.setTenant("tenant1");
    bd3.setVrf("tenant1:vrf1");
    bd3.setSubnets(ImmutableList.of("10.2.0.0/16"));

    tenant.getBridgeDomains().put("tenant1:bd1", bd1);
    tenant.getBridgeDomains().put("tenant1:bd2", bd2);
    tenant.getBridgeDomains().put("tenant1:bd3", bd3);
    config.getTenants().put("tenant1", tenant);
    config.finalizeStructures();

    List<SubnetFinding> findings = AciSubnetAnalyzer.analyzeSubnets(config);

    // Should have one CRITICAL overlap finding (bd1 and bd2)
    assertThat(findings, hasSize(1));
    SubnetFinding finding = findings.get(0);
    assertThat(finding.getSeverity(), equalTo(Severity.CRITICAL));
    assertThat(finding.getCategory(), equalTo(Category.OVERLAP_SAME_VRF));
    assertThat(finding.getBridgeDomain1(), equalTo("tenant1:bd1"));
  }

  @Test
  public void testSubnetOverlapUtilityMethod() {
    // Test the utility method directly

    // Identical subnets
    assertThat(AciSubnetAnalyzer.subnetsOverlap("10.1.1.0/24", "10.1.1.0/24"), equalTo(true));

    // One subnet contains the other
    assertThat(AciSubnetAnalyzer.subnetsOverlap("10.1.0.0/16", "10.1.1.0/24"), equalTo(true));
    assertThat(AciSubnetAnalyzer.subnetsOverlap("10.1.1.0/24", "10.1.0.0/16"), equalTo(true));

    // Partial overlap (not possible with proper CIDR, but test anyway)
    assertThat(AciSubnetAnalyzer.subnetsOverlap("10.1.1.0/24", "10.1.2.0/24"), equalTo(false));

    // Completely different
    assertThat(AciSubnetAnalyzer.subnetsOverlap("10.1.1.0/24", "192.168.1.0/24"), equalTo(false));

    // Adjacent subnets
    assertThat(AciSubnetAnalyzer.subnetsOverlap("10.1.0.0/24", "10.1.1.0/24"), equalTo(false));

    // Invalid formats
    assertThat(AciSubnetAnalyzer.subnetsOverlap("invalid", "10.1.1.0/24"), equalTo(false));
    assertThat(AciSubnetAnalyzer.subnetsOverlap("10.1.1.0/24", "invalid"), equalTo(false));
  }

  @Test
  public void testBridgeDomainWithoutVrf() {
    AciConfiguration config = new AciConfiguration();

    Tenant tenant = new Tenant("tenant1");

    // Bridge domain without VRF
    BridgeDomain bd1 = new BridgeDomain("bd1");
    bd1.setTenant("tenant1");
    bd1.setVrf(null); // No VRF
    bd1.setSubnets(ImmutableList.of("10.1.1.0/24"));

    BridgeDomain bd2 = new BridgeDomain("bd2");
    bd2.setTenant("tenant1");
    bd2.setVrf(null); // No VRF
    bd2.setSubnets(ImmutableList.of("10.1.2.0/24"));

    tenant.getBridgeDomains().put("tenant1:bd1", bd1);
    tenant.getBridgeDomains().put("tenant1:bd2", bd2);
    config.getTenants().put("tenant1", tenant);
    config.finalizeStructures();

    List<SubnetFinding> findings = AciSubnetAnalyzer.analyzeSubnets(config);

    // Should not have overlap findings since subnets don't overlap
    assertThat(findings, empty());
  }

  @Test
  public void testMultipleTenants() {
    AciConfiguration config = new AciConfiguration();

    // Tenant 1
    Tenant tenant1 = new Tenant("tenant1");
    BridgeDomain bd1 = new BridgeDomain("bd1");
    bd1.setTenant("tenant1");
    bd1.setVrf("tenant1:vrf1");
    bd1.setSubnets(ImmutableList.of("10.1.1.0/24"));

    tenant1.getBridgeDomains().put("tenant1:bd1", bd1);

    // Tenant 2
    Tenant tenant2 = new Tenant("tenant2");
    BridgeDomain bd2 = new BridgeDomain("bd2");
    bd2.setTenant("tenant2");
    bd2.setVrf("tenant2:vrf1");
    bd2.setSubnets(ImmutableList.of("10.1.1.0/24"));

    tenant2.getBridgeDomains().put("tenant2:bd2", bd2);

    config.getTenants().put("tenant1", tenant1);
    config.getTenants().put("tenant2", tenant2);
    config.finalizeStructures();

    List<SubnetFinding> findings = AciSubnetAnalyzer.analyzeSubnets(config);

    // Should have one DUPLICATE finding
    // Note: OVERLAP_DIFF_VRF is not generated for identical subnets since they're covered by
    // DUPLICATE
    assertThat(findings, hasSize(1));
    SubnetFinding duplicateFinding = findings.get(0);
    assertThat(duplicateFinding.getCategory(), equalTo(Category.DUPLICATE));
    assertThat(duplicateFinding.getSeverity(), equalTo(Severity.HIGH));
    assertThat(duplicateFinding.getSubnet1(), equalTo("10.1.1.0/24"));
    assertThat(duplicateFinding.getSubnet2(), equalTo("10.1.1.0/24"));
    assertThat(duplicateFinding.getVrf1(), equalTo("tenant1:vrf1"));
    assertThat(duplicateFinding.getVrf2(), equalTo("tenant2:vrf1"));
  }

  @Test
  public void testVerySmallSubnets() {
    AciConfiguration config = new AciConfiguration();

    Tenant tenant = new Tenant("tenant1");

    BridgeDomain bd1 = new BridgeDomain("bd1");
    bd1.setTenant("tenant1");
    bd1.setVrf("tenant1:vrf1");
    bd1.setSubnets(ImmutableList.of("10.1.1.0/31")); // Only 2 addresses

    tenant.getBridgeDomains().put("tenant1:bd1", bd1);
    config.getTenants().put("tenant1", tenant);
    config.finalizeStructures();

    List<SubnetFinding> findings = AciSubnetAnalyzer.analyzeSubnets(config);

    // Should have LOW severity finding for /31
    assertThat(findings, hasSize(1));
    SubnetFinding finding = findings.get(0);
    assertThat(finding.getSeverity(), equalTo(Severity.LOW));
    assertThat(finding.getCategory(), equalTo(Category.INVALID_FORMAT));
    assertThat(finding.getSubnet1(), equalTo("10.1.1.0/31"));
  }
}
