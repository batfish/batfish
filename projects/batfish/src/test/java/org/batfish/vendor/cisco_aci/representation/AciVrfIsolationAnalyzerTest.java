package org.batfish.vendor.cisco_aci.representation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

/** Tests for {@link AciVrfIsolationAnalyzer}. */
public final class AciVrfIsolationAnalyzerTest {

  @Test
  public void testAnalyzeVrfIsolationFindsSubnetOverlapAcrossVrfs() {
    AciConfiguration config = new AciConfiguration();

    AciConfiguration.BridgeDomain bd1 = new AciConfiguration.BridgeDomain("bd1");
    bd1.setTenant("tenant1");
    bd1.setVrf("tenant1:vrf1");
    bd1.setSubnets(ImmutableList.of("10.10.10.0/24"));
    config.getBridgeDomains().put("tenant1:bd1", bd1);

    AciConfiguration.BridgeDomain bd2 = new AciConfiguration.BridgeDomain("bd2");
    bd2.setTenant("tenant1");
    bd2.setVrf("tenant1:vrf2");
    bd2.setSubnets(ImmutableList.of("10.10.10.0/24"));
    config.getBridgeDomains().put("tenant1:bd2", bd2);

    List<VrfIsolationFinding> findings = AciVrfIsolationAnalyzer.analyzeVrfIsolation(config);
    List<VrfIsolationFinding.Category> categories =
        findings.stream().map(VrfIsolationFinding::getCategory).collect(Collectors.toList());

    assertThat(categories, hasItem(VrfIsolationFinding.Category.SUBNET_OVERLAP));
  }

  @Test
  public void testAnalyzeVrfIsolationFindsCrossVrfContractAndUnusedVrf() {
    AciConfiguration config = new AciConfiguration();

    AciVrfModel vrf1 = new AciVrfModel("tenant1:vrf1");
    vrf1.setTenant("tenant1");
    AciVrfModel vrf2 = new AciVrfModel("tenant1:vrf2");
    vrf2.setTenant("tenant1");
    AciVrfModel vrf3 = new AciVrfModel("tenant1:vrf3");
    vrf3.setTenant("tenant1");
    config.getVrfs().put(vrf1.getName(), vrf1);
    config.getVrfs().put(vrf2.getName(), vrf2);
    config.getVrfs().put(vrf3.getName(), vrf3);

    AciConfiguration.BridgeDomain bd1 = new AciConfiguration.BridgeDomain("bd1");
    bd1.setTenant("tenant1");
    bd1.setVrf("tenant1:vrf1");
    config.getBridgeDomains().put("tenant1:bd1", bd1);

    AciConfiguration.BridgeDomain bd2 = new AciConfiguration.BridgeDomain("bd2");
    bd2.setTenant("tenant1");
    bd2.setVrf("tenant1:vrf2");
    config.getBridgeDomains().put("tenant1:bd2", bd2);

    AciConfiguration.Epg provider = new AciConfiguration.Epg("provider");
    provider.setTenant("tenant1");
    provider.setBridgeDomain("bd1");
    provider.setProvidedContracts(ImmutableList.of("tenant1:c1"));
    config.getEpgs().put("tenant1:provider", provider);

    AciConfiguration.Epg consumer = new AciConfiguration.Epg("consumer");
    consumer.setTenant("tenant1");
    consumer.setBridgeDomain("bd2");
    consumer.setConsumedContracts(ImmutableList.of("tenant1:c1"));
    config.getEpgs().put("tenant1:consumer", consumer);

    AciConfiguration.Contract contract = new AciConfiguration.Contract("tenant1:c1");
    contract.setTenant("tenant1");
    config.getContracts().put("tenant1:c1", contract);

    List<VrfIsolationFinding> findings = AciVrfIsolationAnalyzer.analyzeVrfIsolation(config);
    List<VrfIsolationFinding.Category> categories =
        findings.stream().map(VrfIsolationFinding::getCategory).collect(Collectors.toList());

    assertThat(categories, hasItem(VrfIsolationFinding.Category.CROSS_VRF_CONTRACT));
    assertThat(categories, hasItem(VrfIsolationFinding.Category.UNUSED_VRF));
  }

  @Test
  public void testAnalyzeVrfIsolationFindsL3OutScopeIssues() {
    AciConfiguration config = new AciConfiguration();

    AciConfiguration.BridgeDomain bd = new AciConfiguration.BridgeDomain("bd1");
    bd.setTenant("tenant1");
    bd.setVrf("tenant1:vrf1");
    bd.setSubnets(ImmutableList.of("10.10.10.0/24"));
    config.getBridgeDomains().put("tenant1:bd1", bd);

    AciConfiguration.L3Out noVrf = new AciConfiguration.L3Out("l3out-no-vrf");
    noVrf.setTenant("tenant1");
    config.getL3Outs().put("tenant1:l3out-no-vrf", noVrf);

    AciConfiguration.L3Out overlap = new AciConfiguration.L3Out("l3out-overlap");
    overlap.setTenant("tenant1");
    overlap.setVrf("tenant1:vrf1");
    AciConfiguration.ExternalEpg extEpg = new AciConfiguration.ExternalEpg("ext");
    extEpg.setSubnets(ImmutableList.of("10.10.10.0/24"));
    overlap.setExternalEpgs(ImmutableList.of(extEpg));
    config.getL3Outs().put("tenant1:l3out-overlap", overlap);

    List<VrfIsolationFinding> findings = AciVrfIsolationAnalyzer.analyzeVrfIsolation(config);
    List<VrfIsolationFinding.Category> categories =
        findings.stream().map(VrfIsolationFinding::getCategory).collect(Collectors.toList());

    assertThat(categories, hasItem(VrfIsolationFinding.Category.L3OUT_SCOPE));
  }

  @Test
  public void testAnalyzeVrfIsolationNoFindingForSingleVrfSubnetReuse() {
    AciConfiguration config = new AciConfiguration();

    AciConfiguration.BridgeDomain bd1 = new AciConfiguration.BridgeDomain("bd1");
    bd1.setTenant("tenant1");
    bd1.setVrf("tenant1:vrf1");
    bd1.setSubnets(ImmutableList.of("10.10.10.0/24"));
    config.getBridgeDomains().put("tenant1:bd1", bd1);

    AciConfiguration.BridgeDomain bd2 = new AciConfiguration.BridgeDomain("bd2");
    bd2.setTenant("tenant1");
    bd2.setVrf("tenant1:vrf1");
    bd2.setSubnets(ImmutableList.of("10.10.10.0/24"));
    config.getBridgeDomains().put("tenant1:bd2", bd2);

    List<VrfIsolationFinding> findings = AciVrfIsolationAnalyzer.analyzeVrfIsolation(config);
    List<VrfIsolationFinding.Category> categories =
        findings.stream().map(VrfIsolationFinding::getCategory).collect(Collectors.toList());

    assertThat(categories, not(hasItem(VrfIsolationFinding.Category.SUBNET_OVERLAP)));
  }

  @Test
  public void testCheckSubnetOverlap_ThreeVrfsSameSubnet() {
    AciConfiguration config = new AciConfiguration();

    AciConfiguration.BridgeDomain bd1 = new AciConfiguration.BridgeDomain("bd1");
    bd1.setTenant("tenant1");
    bd1.setVrf("tenant1:vrf1");
    bd1.setSubnets(ImmutableList.of("10.0.0.0/24"));
    config.getBridgeDomains().put("tenant1:bd1", bd1);

    AciConfiguration.BridgeDomain bd2 = new AciConfiguration.BridgeDomain("bd2");
    bd2.setTenant("tenant1");
    bd2.setVrf("tenant1:vrf2");
    bd2.setSubnets(ImmutableList.of("10.0.0.0/24"));
    config.getBridgeDomains().put("tenant1:bd2", bd2);

    AciConfiguration.BridgeDomain bd3 = new AciConfiguration.BridgeDomain("bd3");
    bd3.setTenant("tenant1");
    bd3.setVrf("tenant1:vrf3");
    bd3.setSubnets(ImmutableList.of("10.0.0.0/24"));
    config.getBridgeDomains().put("tenant1:bd3", bd3);

    List<VrfIsolationFinding> findings = AciVrfIsolationAnalyzer.checkSubnetOverlap(config);

    // Should create 3 findings: (vrf1,vrf2), (vrf1,vrf3), (vrf2,vrf3)
    assertThat(findings, hasSize(3));

    for (VrfIsolationFinding finding : findings) {
      assertEquals(VrfIsolationFinding.Severity.HIGH, finding.getSeverity());
      assertEquals(VrfIsolationFinding.Category.SUBNET_OVERLAP, finding.getCategory());
      assertEquals("10.0.0.0/24", finding.getSubnet1());
      assertThat(
          finding.getDescription(), containsString("Subnet 10.0.0.0/24 is used in multiple VRFs"));
    }
  }

  @Test
  public void testCheckSubnetOverlap_NoOverlap() {
    AciConfiguration config = new AciConfiguration();

    AciConfiguration.BridgeDomain bd1 = new AciConfiguration.BridgeDomain("bd1");
    bd1.setTenant("tenant1");
    bd1.setVrf("tenant1:vrf1");
    bd1.setSubnets(ImmutableList.of("10.0.0.0/24"));
    config.getBridgeDomains().put("tenant1:bd1", bd1);

    AciConfiguration.BridgeDomain bd2 = new AciConfiguration.BridgeDomain("bd2");
    bd2.setTenant("tenant1");
    bd2.setVrf("tenant1:vrf2");
    bd2.setSubnets(ImmutableList.of("10.0.1.0/24"));
    config.getBridgeDomains().put("tenant1:bd2", bd2);

    List<VrfIsolationFinding> findings = AciVrfIsolationAnalyzer.checkSubnetOverlap(config);

    assertThat(findings, hasSize(0));
  }

  @Test
  public void testCheckSubnetOverlap_BdWithoutVrf() {
    AciConfiguration config = new AciConfiguration();

    AciConfiguration.BridgeDomain bd1 = new AciConfiguration.BridgeDomain("bd1");
    bd1.setTenant("tenant1");
    bd1.setVrf("tenant1:vrf1");
    bd1.setSubnets(ImmutableList.of("10.0.0.0/24"));
    config.getBridgeDomains().put("tenant1:bd1", bd1);

    AciConfiguration.BridgeDomain bd2 = new AciConfiguration.BridgeDomain("bd2");
    bd2.setTenant("tenant1");
    bd2.setVrf(null); // No VRF
    bd2.setSubnets(ImmutableList.of("10.0.0.0/24"));
    config.getBridgeDomains().put("tenant1:bd2", bd2);

    List<VrfIsolationFinding> findings = AciVrfIsolationAnalyzer.checkSubnetOverlap(config);

    // Should not find overlap since bd2 has no VRF
    assertThat(findings, hasSize(0));
  }

  @Test
  public void testCheckContractVrfScope_MultipleProvidersDifferentVrfs() {
    AciConfiguration config = new AciConfiguration();

    AciVrfModel vrf1 = new AciVrfModel("tenant1:vrf1");
    vrf1.setTenant("tenant1");
    AciVrfModel vrf2 = new AciVrfModel("tenant1:vrf2");
    vrf2.setTenant("tenant1");
    config.getVrfs().put(vrf1.getName(), vrf1);
    config.getVrfs().put(vrf2.getName(), vrf2);

    AciConfiguration.BridgeDomain bd1 = new AciConfiguration.BridgeDomain("bd1");
    bd1.setTenant("tenant1");
    bd1.setVrf("tenant1:vrf1");
    config.getBridgeDomains().put("tenant1:bd1", bd1);

    AciConfiguration.BridgeDomain bd2 = new AciConfiguration.BridgeDomain("bd2");
    bd2.setTenant("tenant1");
    bd2.setVrf("tenant1:vrf2");
    config.getBridgeDomains().put("tenant1:bd2", bd2);

    AciConfiguration.Epg provider1 = new AciConfiguration.Epg("provider1");
    provider1.setTenant("tenant1");
    provider1.setBridgeDomain("bd1");
    provider1.setProvidedContracts(ImmutableList.of("tenant1:contract1"));
    config.getEpgs().put("tenant1:provider1", provider1);

    AciConfiguration.Epg provider2 = new AciConfiguration.Epg("provider2");
    provider2.setTenant("tenant1");
    provider2.setBridgeDomain("bd2");
    provider2.setProvidedContracts(ImmutableList.of("tenant1:contract1"));
    config.getEpgs().put("tenant1:provider2", provider2);

    AciConfiguration.Contract contract = new AciConfiguration.Contract("tenant1:contract1");
    contract.setTenant("tenant1");
    config.getContracts().put("tenant1:contract1", contract);

    List<VrfIsolationFinding> findings = AciVrfIsolationAnalyzer.checkContractVrfScope(config);

    assertThat(findings, hasSize(1));
    VrfIsolationFinding finding = findings.get(0);
    assertEquals(VrfIsolationFinding.Severity.MEDIUM, finding.getSeverity());
    assertEquals(VrfIsolationFinding.Category.CROSS_VRF_CONTRACT, finding.getCategory());
    assertEquals("tenant1:contract1", finding.getContractName());
    assertThat(finding.getDescription(), containsString("tenant1:vrf1"));
    assertThat(finding.getDescription(), containsString("tenant1:vrf2"));
  }

  @Test
  public void testCheckContractVrfScope_SingleVrfNoFinding() {
    AciConfiguration config = new AciConfiguration();

    AciConfiguration.BridgeDomain bd1 = new AciConfiguration.BridgeDomain("bd1");
    bd1.setTenant("tenant1");
    bd1.setVrf("tenant1:vrf1");
    config.getBridgeDomains().put("tenant1:bd1", bd1);

    AciConfiguration.BridgeDomain bd2 = new AciConfiguration.BridgeDomain("bd2");
    bd2.setTenant("tenant1");
    bd2.setVrf("tenant1:vrf1");
    config.getBridgeDomains().put("tenant1:bd2", bd2);

    AciConfiguration.Epg provider = new AciConfiguration.Epg("provider");
    provider.setTenant("tenant1");
    provider.setBridgeDomain("bd1");
    provider.setProvidedContracts(ImmutableList.of("tenant1:contract1"));
    config.getEpgs().put("tenant1:provider", provider);

    AciConfiguration.Epg consumer = new AciConfiguration.Epg("consumer");
    consumer.setTenant("tenant1");
    consumer.setBridgeDomain("bd2");
    consumer.setConsumedContracts(ImmutableList.of("tenant1:contract1"));
    config.getEpgs().put("tenant1:consumer", consumer);

    AciConfiguration.Contract contract = new AciConfiguration.Contract("tenant1:contract1");
    contract.setTenant("tenant1");
    config.getContracts().put("tenant1:contract1", contract);

    List<VrfIsolationFinding> findings = AciVrfIsolationAnalyzer.checkContractVrfScope(config);

    assertThat(findings, hasSize(0));
  }

  @Test
  public void testCheckContractVrfScope_EpgWithoutBridgeDomain() {
    AciConfiguration config = new AciConfiguration();

    AciConfiguration.Epg epg = new AciConfiguration.Epg("epg1");
    epg.setTenant("tenant1");
    epg.setBridgeDomain(null); // No bridge domain
    epg.setProvidedContracts(ImmutableList.of("tenant1:contract1"));
    config.getEpgs().put("tenant1:epg1", epg);

    AciConfiguration.Contract contract = new AciConfiguration.Contract("tenant1:contract1");
    contract.setTenant("tenant1");
    config.getContracts().put("tenant1:contract1", contract);

    List<VrfIsolationFinding> findings = AciVrfIsolationAnalyzer.checkContractVrfScope(config);

    // Should not crash, should return empty findings
    assertThat(findings, hasSize(0));
  }

  @Test
  public void testCheckUnusedVrfs_AllVrfsUsed() {
    AciConfiguration config = new AciConfiguration();

    AciVrfModel vrf1 = new AciVrfModel("tenant1:vrf1");
    vrf1.setTenant("tenant1");
    AciVrfModel vrf2 = new AciVrfModel("tenant1:vrf2");
    vrf2.setTenant("tenant1");
    config.getVrfs().put(vrf1.getName(), vrf1);
    config.getVrfs().put(vrf2.getName(), vrf2);

    AciConfiguration.BridgeDomain bd1 = new AciConfiguration.BridgeDomain("bd1");
    bd1.setTenant("tenant1");
    bd1.setVrf("tenant1:vrf1");
    config.getBridgeDomains().put("tenant1:bd1", bd1);

    AciConfiguration.L3Out l3Out = new AciConfiguration.L3Out("l3out1");
    l3Out.setTenant("tenant1");
    l3Out.setVrf("tenant1:vrf2");
    config.getL3Outs().put("tenant1:l3out1", l3Out);

    List<VrfIsolationFinding> findings = AciVrfIsolationAnalyzer.checkUnusedVrfs(config);

    assertThat(findings, hasSize(0));
  }

  @Test
  public void testCheckUnusedVrfs_MultipleUnusedVrfs() {
    AciConfiguration config = new AciConfiguration();

    AciVrfModel vrf1 = new AciVrfModel("tenant1:vrf1");
    vrf1.setTenant("tenant1");
    AciVrfModel vrf2 = new AciVrfModel("tenant1:vrf2");
    vrf2.setTenant("tenant1");
    AciVrfModel vrf3 = new AciVrfModel("tenant1:vrf3");
    vrf3.setTenant("tenant1");
    config.getVrfs().put(vrf1.getName(), vrf1);
    config.getVrfs().put(vrf2.getName(), vrf2);
    config.getVrfs().put(vrf3.getName(), vrf3);

    // Only vrf1 is used
    AciConfiguration.BridgeDomain bd1 = new AciConfiguration.BridgeDomain("bd1");
    bd1.setTenant("tenant1");
    bd1.setVrf("tenant1:vrf1");
    config.getBridgeDomains().put("tenant1:bd1", bd1);

    List<VrfIsolationFinding> findings = AciVrfIsolationAnalyzer.checkUnusedVrfs(config);

    assertThat(findings, hasSize(2));
    for (VrfIsolationFinding finding : findings) {
      assertEquals(VrfIsolationFinding.Severity.LOW, finding.getSeverity());
      assertEquals(VrfIsolationFinding.Category.UNUSED_VRF, finding.getCategory());
      assertThat(finding.getVrfName1(), containsString("tenant1:vrf")); // Either vrf2 or vrf3
    }
  }

  @Test
  public void testCheckUnusedVrfs_VrfUsedByL3Out() {
    AciConfiguration config = new AciConfiguration();

    AciVrfModel vrf = new AciVrfModel("tenant1:vrf1");
    vrf.setTenant("tenant1");
    config.getVrfs().put(vrf.getName(), vrf);

    AciConfiguration.L3Out l3Out = new AciConfiguration.L3Out("l3out1");
    l3Out.setTenant("tenant1");
    l3Out.setVrf("tenant1:vrf1");
    config.getL3Outs().put("tenant1:l3out1", l3Out);

    List<VrfIsolationFinding> findings = AciVrfIsolationAnalyzer.checkUnusedVrfs(config);

    assertThat(findings, hasSize(0));
  }

  @Test
  public void testCheckBridgeDomainVrfIssues_BdUsedBySingleVrf() {
    AciConfiguration config = new AciConfiguration();

    AciConfiguration.BridgeDomain bd = new AciConfiguration.BridgeDomain("bd1");
    bd.setTenant("tenant1");
    bd.setVrf("tenant1:vrf1");
    config.getBridgeDomains().put("tenant1:bd1", bd);

    AciConfiguration.Epg epg1 = new AciConfiguration.Epg("epg1");
    epg1.setTenant("tenant1");
    epg1.setBridgeDomain("bd1");
    config.getEpgs().put("tenant1:epg1", epg1);

    List<VrfIsolationFinding> findings = AciVrfIsolationAnalyzer.checkBridgeDomainVrfIssues(config);

    assertThat(findings, hasSize(0));
  }

  @Test
  public void testCheckBridgeDomainVrfIssues_BdUsedByMultipleVrfs() {
    AciConfiguration config = new AciConfiguration();

    AciConfiguration.BridgeDomain bd1 = new AciConfiguration.BridgeDomain("bd1");
    bd1.setTenant("tenant1");
    bd1.setVrf("tenant1:vrf1");
    config.getBridgeDomains().put("tenant1:bd1", bd1);

    AciConfiguration.BridgeDomain bd2 = new AciConfiguration.BridgeDomain("bd2");
    bd2.setTenant("tenant1");
    bd2.setVrf("tenant1:vrf2");
    config.getBridgeDomains().put("tenant1:bd2", bd2);

    // EPGs in different VRFs pointing to same BD name
    // This simulates a configuration error where the BD is shared across VRFs
    AciConfiguration.Epg epg1 = new AciConfiguration.Epg("epg1");
    epg1.setTenant("tenant1");
    epg1.setBridgeDomain("bd1");
    config.getEpgs().put("tenant1:epg1", epg1);

    AciConfiguration.Epg epg2 = new AciConfiguration.Epg("epg2");
    epg2.setTenant("tenant1");
    epg2.setBridgeDomain("bd2");
    config.getEpgs().put("tenant1:epg2", epg2);

    List<VrfIsolationFinding> findings = AciVrfIsolationAnalyzer.checkBridgeDomainVrfIssues(config);

    // Should not find issues since EPGs reference different BD keys
    assertThat(findings, hasSize(0));
  }

  @Test
  public void testCheckBridgeDomainVrfIssues_EpgWithoutBridgeDomain() {
    AciConfiguration config = new AciConfiguration();

    AciConfiguration.Epg epg = new AciConfiguration.Epg("epg1");
    epg.setTenant("tenant1");
    epg.setBridgeDomain(null);
    config.getEpgs().put("tenant1:epg1", epg);

    List<VrfIsolationFinding> findings = AciVrfIsolationAnalyzer.checkBridgeDomainVrfIssues(config);

    assertThat(findings, hasSize(0));
  }

  @Test
  public void testCheckL3OutScope_L3OutWithVrfNoOverlap() {
    AciConfiguration config = new AciConfiguration();

    AciConfiguration.BridgeDomain bd = new AciConfiguration.BridgeDomain("bd1");
    bd.setTenant("tenant1");
    bd.setVrf("tenant1:vrf1");
    bd.setSubnets(ImmutableList.of("10.0.0.0/24"));
    config.getBridgeDomains().put("tenant1:bd1", bd);

    AciConfiguration.L3Out l3Out = new AciConfiguration.L3Out("l3out1");
    l3Out.setTenant("tenant1");
    l3Out.setVrf("tenant1:vrf1");

    AciConfiguration.ExternalEpg extEpg = new AciConfiguration.ExternalEpg("ext");
    extEpg.setSubnets(ImmutableList.of("192.168.0.0/24"));
    l3Out.setExternalEpgs(ImmutableList.of(extEpg));

    config.getL3Outs().put("tenant1:l3out1", l3Out);

    List<VrfIsolationFinding> findings = AciVrfIsolationAnalyzer.checkL3OutScope(config);

    assertThat(findings, hasSize(0));
  }

  @Test
  public void testCheckL3OutScope_L3OutWithoutVrf() {
    AciConfiguration config = new AciConfiguration();

    AciConfiguration.L3Out l3Out = new AciConfiguration.L3Out("l3out1");
    l3Out.setTenant("tenant1");
    l3Out.setVrf(null); // No VRF
    config.getL3Outs().put("tenant1:l3out1", l3Out);

    List<VrfIsolationFinding> findings = AciVrfIsolationAnalyzer.checkL3OutScope(config);

    assertThat(findings, hasSize(1));
    VrfIsolationFinding finding = findings.get(0);
    assertEquals(VrfIsolationFinding.Severity.HIGH, finding.getSeverity());
    assertEquals(VrfIsolationFinding.Category.L3OUT_SCOPE, finding.getCategory());
    assertThat(finding.getDescription(), containsString("has no VRF association"));
  }

  @Test
  public void testCheckL3OutScope_MultipleL3OutsMixedConfiguration() {
    AciConfiguration config = new AciConfiguration();

    AciConfiguration.BridgeDomain bd = new AciConfiguration.BridgeDomain("bd1");
    bd.setTenant("tenant1");
    bd.setVrf("tenant1:vrf1");
    bd.setSubnets(ImmutableList.of("10.0.0.0/24"));
    config.getBridgeDomains().put("tenant1:bd1", bd);

    // L3Out without VRF
    AciConfiguration.L3Out l3Out1 = new AciConfiguration.L3Out("l3out-no-vrf");
    l3Out1.setTenant("tenant1");
    config.getL3Outs().put("tenant1:l3out-no-vrf", l3Out1);

    // L3Out with overlapping subnet
    AciConfiguration.L3Out l3Out2 = new AciConfiguration.L3Out("l3out-overlap");
    l3Out2.setTenant("tenant1");
    l3Out2.setVrf("tenant1:vrf1");

    AciConfiguration.ExternalEpg extEpg = new AciConfiguration.ExternalEpg("ext");
    extEpg.setSubnets(ImmutableList.of("10.0.0.0/24"));
    l3Out2.setExternalEpgs(ImmutableList.of(extEpg));

    config.getL3Outs().put("tenant1:l3out-overlap", l3Out2);

    List<VrfIsolationFinding> findings = AciVrfIsolationAnalyzer.checkL3OutScope(config);

    assertThat(findings, hasSize(2));

    // Check that we have both types of findings
    List<VrfIsolationFinding.Category> categories =
        findings.stream().map(VrfIsolationFinding::getCategory).collect(Collectors.toList());
    assertThat(categories, hasItem(VrfIsolationFinding.Category.L3OUT_SCOPE));

    // Check for no VRF finding
    VrfIsolationFinding noVrfFinding =
        findings.stream()
            .filter(f -> f.getDescription().contains("no VRF association"))
            .findFirst()
            .orElse(null);
    assertThat(noVrfFinding, notNullValue());

    // Check for overlap finding
    VrfIsolationFinding overlapFinding =
        findings.stream()
            .filter(f -> f.getDescription().contains("overlaps with internal"))
            .findFirst()
            .orElse(null);
    assertThat(overlapFinding, notNullValue());
    assertEquals(VrfIsolationFinding.Severity.MEDIUM, overlapFinding.getSeverity());
  }

  @Test
  public void testCheckL3OutScope_ExternalEpgWithNoSubnets() {
    AciConfiguration config = new AciConfiguration();

    AciConfiguration.BridgeDomain bd = new AciConfiguration.BridgeDomain("bd1");
    bd.setTenant("tenant1");
    bd.setVrf("tenant1:vrf1");
    bd.setSubnets(ImmutableList.of("10.0.0.0/24"));
    config.getBridgeDomains().put("tenant1:bd1", bd);

    AciConfiguration.L3Out l3Out = new AciConfiguration.L3Out("l3out1");
    l3Out.setTenant("tenant1");
    l3Out.setVrf("tenant1:vrf1");

    AciConfiguration.ExternalEpg extEpg = new AciConfiguration.ExternalEpg("ext");
    extEpg.setSubnets(ImmutableList.of()); // Empty subnets
    l3Out.setExternalEpgs(ImmutableList.of(extEpg));

    config.getL3Outs().put("tenant1:l3out1", l3Out);

    List<VrfIsolationFinding> findings = AciVrfIsolationAnalyzer.checkL3OutScope(config);

    assertThat(findings, hasSize(0));
  }

  @Test
  public void testAnalyzeVrfIsolation_EmptyConfiguration() {
    AciConfiguration config = new AciConfiguration();

    List<VrfIsolationFinding> findings = AciVrfIsolationAnalyzer.analyzeVrfIsolation(config);

    assertThat(findings, hasSize(0));
  }

  @Test
  public void testAnalyzeVrfIsolation_MultipleFindingTypes() {
    AciConfiguration config = new AciConfiguration();

    // Setup VRFs
    AciVrfModel vrf1 = new AciVrfModel("tenant1:vrf1");
    vrf1.setTenant("tenant1");
    AciVrfModel vrf2 = new AciVrfModel("tenant1:vrf2");
    vrf2.setTenant("tenant1");
    AciVrfModel vrf3 = new AciVrfModel("tenant1:vrf3");
    vrf3.setTenant("tenant1");
    config.getVrfs().put(vrf1.getName(), vrf1);
    config.getVrfs().put(vrf2.getName(), vrf2);
    config.getVrfs().put(vrf3.getName(), vrf3);

    // Setup bridge domains with overlapping subnets
    AciConfiguration.BridgeDomain bd1 = new AciConfiguration.BridgeDomain("bd1");
    bd1.setTenant("tenant1");
    bd1.setVrf("tenant1:vrf1");
    bd1.setSubnets(ImmutableList.of("10.0.0.0/24"));
    config.getBridgeDomains().put("tenant1:bd1", bd1);

    AciConfiguration.BridgeDomain bd2 = new AciConfiguration.BridgeDomain("bd2");
    bd2.setTenant("tenant1");
    bd2.setVrf("tenant1:vrf2");
    bd2.setSubnets(ImmutableList.of("10.0.0.0/24"));
    config.getBridgeDomains().put("tenant1:bd2", bd2);

    // Setup cross-VRF contract
    AciConfiguration.Epg provider = new AciConfiguration.Epg("provider");
    provider.setTenant("tenant1");
    provider.setBridgeDomain("bd1");
    provider.setProvidedContracts(ImmutableList.of("tenant1:c1"));
    config.getEpgs().put("tenant1:provider", provider);

    AciConfiguration.Epg consumer = new AciConfiguration.Epg("consumer");
    consumer.setTenant("tenant1");
    consumer.setBridgeDomain("bd2");
    consumer.setConsumedContracts(ImmutableList.of("tenant1:c1"));
    config.getEpgs().put("tenant1:consumer", consumer);

    AciConfiguration.Contract contract = new AciConfiguration.Contract("tenant1:c1");
    contract.setTenant("tenant1");
    config.getContracts().put("tenant1:c1", contract);

    // Setup L3Out without VRF
    AciConfiguration.L3Out l3Out = new AciConfiguration.L3Out("l3out1");
    l3Out.setTenant("tenant1");
    config.getL3Outs().put("tenant1:l3out1", l3Out);

    List<VrfIsolationFinding> findings = AciVrfIsolationAnalyzer.analyzeVrfIsolation(config);
    List<VrfIsolationFinding.Category> categories =
        findings.stream().map(VrfIsolationFinding::getCategory).collect(Collectors.toList());

    // Should find SUBNET_OVERLAP, CROSS_VRF_CONTRACT, UNUSED_VRF, and L3OUT_SCOPE
    assertThat(categories, hasItem(VrfIsolationFinding.Category.SUBNET_OVERLAP));
    assertThat(categories, hasItem(VrfIsolationFinding.Category.CROSS_VRF_CONTRACT));
    assertThat(categories, hasItem(VrfIsolationFinding.Category.UNUSED_VRF));
    assertThat(categories, hasItem(VrfIsolationFinding.Category.L3OUT_SCOPE));
  }

  @Test
  public void testAnalyzeVrfIsolation_VerifyFindingFields() {
    AciConfiguration config = new AciConfiguration();

    AciConfiguration.BridgeDomain bd1 = new AciConfiguration.BridgeDomain("bd1");
    bd1.setTenant("tenant1");
    bd1.setVrf("tenant1:vrf1");
    bd1.setSubnets(ImmutableList.of("10.0.0.0/24"));
    config.getBridgeDomains().put("tenant1:bd1", bd1);

    AciConfiguration.BridgeDomain bd2 = new AciConfiguration.BridgeDomain("bd2");
    bd2.setTenant("tenant1");
    bd2.setVrf("tenant1:vrf2");
    bd2.setSubnets(ImmutableList.of("10.0.0.0/24"));
    config.getBridgeDomains().put("tenant1:bd2", bd2);

    List<VrfIsolationFinding> findings = AciVrfIsolationAnalyzer.analyzeVrfIsolation(config);

    assertThat(findings, hasSize(1));
    VrfIsolationFinding finding = findings.get(0);

    assertEquals(VrfIsolationFinding.Severity.HIGH, finding.getSeverity());
    assertEquals(VrfIsolationFinding.Category.SUBNET_OVERLAP, finding.getCategory());
    assertEquals("tenant1:vrf1", finding.getVrfName1());
    assertEquals("tenant1:vrf2", finding.getVrfName2());
    assertEquals("10.0.0.0/24", finding.getSubnet1());
    assertEquals("tenant1", finding.getTenantName());
    assertThat(finding.getDescription(), notNullValue());
    assertThat(finding.getImpact(), notNullValue());
    assertThat(finding.getRecommendation(), notNullValue());
  }

  @Test
  public void testCheckSubnetOverlap_MultipleSubnetsPerBd() {
    AciConfiguration config = new AciConfiguration();

    AciConfiguration.BridgeDomain bd1 = new AciConfiguration.BridgeDomain("bd1");
    bd1.setTenant("tenant1");
    bd1.setVrf("tenant1:vrf1");
    bd1.setSubnets(ImmutableList.of("10.0.0.0/24", "10.0.1.0/24", "10.0.2.0/24"));
    config.getBridgeDomains().put("tenant1:bd1", bd1);

    AciConfiguration.BridgeDomain bd2 = new AciConfiguration.BridgeDomain("bd2");
    bd2.setTenant("tenant1");
    bd2.setVrf("tenant1:vrf2");
    bd2.setSubnets(ImmutableList.of("10.0.1.0/24", "10.0.3.0/24"));
    config.getBridgeDomains().put("tenant1:bd2", bd2);

    List<VrfIsolationFinding> findings = AciVrfIsolationAnalyzer.checkSubnetOverlap(config);

    // Only 10.0.1.0/24 overlaps
    assertThat(findings, hasSize(1));
    assertEquals("10.0.1.0/24", findings.get(0).getSubnet1());
  }

  @Test
  public void testCheckContractVrfScope_ContractWithNoEpgs() {
    AciConfiguration config = new AciConfiguration();

    AciConfiguration.Contract contract = new AciConfiguration.Contract("tenant1:contract1");
    contract.setTenant("tenant1");
    config.getContracts().put("tenant1:contract1", contract);

    List<VrfIsolationFinding> findings = AciVrfIsolationAnalyzer.checkContractVrfScope(config);

    // Should not crash, should return empty findings
    assertThat(findings, hasSize(0));
  }

  @Test
  public void testCheckL3OutScope_DifferentVrfsNoOverlapCheck() {
    AciConfiguration config = new AciConfiguration();

    AciConfiguration.BridgeDomain bd = new AciConfiguration.BridgeDomain("bd1");
    bd.setTenant("tenant1");
    bd.setVrf("tenant1:vrf1");
    bd.setSubnets(ImmutableList.of("10.0.0.0/24"));
    config.getBridgeDomains().put("tenant1:bd1", bd);

    // L3Out in different VRF with same subnet - should not trigger overlap
    AciConfiguration.L3Out l3Out = new AciConfiguration.L3Out("l3out1");
    l3Out.setTenant("tenant1");
    l3Out.setVrf("tenant1:vrf2"); // Different VRF

    AciConfiguration.ExternalEpg extEpg = new AciConfiguration.ExternalEpg("ext");
    extEpg.setSubnets(ImmutableList.of("10.0.0.0/24"));
    l3Out.setExternalEpgs(ImmutableList.of(extEpg));

    config.getL3Outs().put("tenant1:l3out1", l3Out);

    List<VrfIsolationFinding> findings = AciVrfIsolationAnalyzer.checkL3OutScope(config);

    // Should not find overlap since BD and L3Out are in different VRFs
    assertThat(findings, hasSize(0));
  }
}
