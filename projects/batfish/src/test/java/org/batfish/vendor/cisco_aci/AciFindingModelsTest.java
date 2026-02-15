package org.batfish.vendor.cisco_aci;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.batfish.vendor.cisco_aci.representation.ContractUsageFinding;
import org.batfish.vendor.cisco_aci.representation.ReachabilityFinding;
import org.batfish.vendor.cisco_aci.representation.SubnetFinding;
import org.batfish.vendor.cisco_aci.representation.VrfIsolationFinding;
import org.junit.Test;

/** Tests data-model finding classes for coverage of getters/setters/equals/hashCode/toString. */
public final class AciFindingModelsTest {

  @Test
  public void testContractUsageFindingModel() {
    ContractUsageFinding finding =
        new ContractUsageFinding(
            ContractUsageFinding.Severity.MEDIUM,
            ContractUsageFinding.Category.UNUSED,
            "c1",
            "t1",
            "desc",
            "reco");
    finding.setRelatedContracts("c2,c3");

    assertEquals(ContractUsageFinding.Severity.MEDIUM, finding.getSeverity());
    assertEquals(ContractUsageFinding.Category.UNUSED, finding.getCategory());
    assertEquals("c1", finding.getContractName());
    assertEquals("t1", finding.getTenantName());
    assertEquals("desc", finding.getDescription());
    assertEquals("reco", finding.getRecommendation());
    assertEquals("c2,c3", finding.getRelatedContracts());
    assertThat(finding.toString(), containsString("t1:c1"));

    ContractUsageFinding sameKeyDifferentText =
        new ContractUsageFinding(
            ContractUsageFinding.Severity.MEDIUM,
            ContractUsageFinding.Category.UNUSED,
            "c1",
            "t1",
            "other desc",
            "other reco");
    assertEquals(finding, sameKeyDifferentText);
    assertEquals(finding.hashCode(), sameKeyDifferentText.hashCode());

    ContractUsageFinding different =
        new ContractUsageFinding(
            ContractUsageFinding.Severity.HIGH,
            ContractUsageFinding.Category.UNUSED,
            "c1",
            "t1",
            "desc",
            "reco");
    assertNotEquals(finding, different);
  }

  @Test
  public void testReachabilityFindingModelAndBuilder() {
    ReachabilityFinding finding =
        new ReachabilityFinding.Builder(
                ReachabilityFinding.Severity.HIGH,
                ReachabilityFinding.Category.NO_CONTRACT,
                "blocked")
            .setSourceEpg("t1:e1")
            .setDestEpg("t1:e2")
            .setContract("t1:c1")
            .setRecommendation("add contract")
            .build();

    assertEquals(ReachabilityFinding.Severity.HIGH, finding.getSeverity());
    assertEquals(ReachabilityFinding.Category.NO_CONTRACT, finding.getCategory());
    assertEquals("t1:e1", finding.getSourceEpg());
    assertEquals("t1:e2", finding.getDestEpg());
    assertEquals("t1:c1", finding.getContract());
    assertEquals("blocked", finding.getDescription());
    assertEquals("add contract", finding.getRecommendation());
    assertThat(finding.toString(), containsString("sourceEpg='t1:e1'"));

    ReachabilityFinding same =
        new ReachabilityFinding.Builder(
                ReachabilityFinding.Severity.HIGH,
                ReachabilityFinding.Category.NO_CONTRACT,
                "blocked")
            .setSourceEpg("t1:e1")
            .setDestEpg("t1:e2")
            .setContract("t1:c1")
            .setRecommendation("add contract")
            .build();
    assertEquals(finding, same);
    assertEquals(finding.hashCode(), same.hashCode());

    ReachabilityFinding different =
        new ReachabilityFinding(
            ReachabilityFinding.Severity.INFO, ReachabilityFinding.Category.INFO, "i", null);
    different.setSourceEpg("x");
    assertNotEquals(finding, different);
  }

  @Test
  public void testVrfIsolationFindingModel() {
    VrfIsolationFinding finding =
        new VrfIsolationFinding(
            VrfIsolationFinding.Severity.MEDIUM,
            VrfIsolationFinding.Category.CROSS_VRF_CONTRACT,
            "t1:vrf1",
            "cross vrf",
            "impact",
            "reco");
    finding.setVrfName2("t1:vrf2");
    finding.setTenantName("t1");
    finding.setBridgeDomain("bd1");
    finding.setSubnet1("10.0.0.0/24");
    finding.setSubnet2("10.0.1.0/24");
    finding.setContractName("t1:c1");

    assertEquals("t1:vrf1", finding.getVrfName1());
    assertEquals("t1:vrf2", finding.getVrfName2());
    assertEquals("t1", finding.getTenantName());
    assertEquals("bd1", finding.getBridgeDomain());
    assertEquals("10.0.0.0/24", finding.getSubnet1());
    assertEquals("10.0.1.0/24", finding.getSubnet2());
    assertEquals("t1:c1", finding.getContractName());
    assertEquals("cross vrf", finding.getDescription());
    assertEquals("impact", finding.getImpact());
    assertEquals("reco", finding.getRecommendation());
    assertThat(finding.toString(), containsString("vrf1 & t1:vrf2"));

    VrfIsolationFinding sameKeyDifferentImpact =
        new VrfIsolationFinding(
            VrfIsolationFinding.Severity.MEDIUM,
            VrfIsolationFinding.Category.CROSS_VRF_CONTRACT,
            "t1:vrf1",
            "cross vrf",
            "other impact",
            "other reco");
    sameKeyDifferentImpact.setVrfName2("t1:vrf2");
    assertEquals(finding, sameKeyDifferentImpact);
    assertEquals(finding.hashCode(), sameKeyDifferentImpact.hashCode());

    VrfIsolationFinding different =
        new VrfIsolationFinding(
            VrfIsolationFinding.Severity.HIGH,
            VrfIsolationFinding.Category.CROSS_VRF_CONTRACT,
            "t1:vrf1",
            "cross vrf",
            "impact",
            "reco");
    assertNotEquals(finding, different);
  }

  @Test
  public void testSubnetFindingModel() {
    SubnetFinding finding =
        new SubnetFinding(
            SubnetFinding.Severity.HIGH,
            SubnetFinding.Category.DUPLICATE,
            "t1:bd1",
            "t1:vrf1",
            "10.0.0.0/24",
            "t1:bd2",
            "t1:vrf1",
            "10.0.0.0/24",
            "duplicate",
            "fix");
    SubnetFinding same =
        new SubnetFinding(
            SubnetFinding.Severity.HIGH,
            SubnetFinding.Category.DUPLICATE,
            "t1:bd1",
            "t1:vrf1",
            "10.0.0.0/24",
            "t1:bd2",
            "t1:vrf1",
            "10.0.0.0/24",
            "duplicate",
            "fix");
    SubnetFinding singleBdFinding =
        new SubnetFinding(
            SubnetFinding.Severity.LOW,
            SubnetFinding.Category.NO_SUBNET,
            "t1:bd3",
            "t1:vrf2",
            "N/A",
            null,
            null,
            null,
            "no subnet",
            "add subnet");

    assertEquals(SubnetFinding.Severity.HIGH, finding.getSeverity());
    assertEquals(SubnetFinding.Category.DUPLICATE, finding.getCategory());
    assertEquals("t1:bd1", finding.getBridgeDomain1());
    assertEquals("t1:vrf1", finding.getVrf1());
    assertEquals("10.0.0.0/24", finding.getSubnet1());
    assertEquals("t1:bd2", finding.getBridgeDomain2());
    assertEquals("t1:vrf1", finding.getVrf2());
    assertEquals("10.0.0.0/24", finding.getSubnet2());
    assertEquals("duplicate", finding.getDescription());
    assertEquals("fix", finding.getRecommendation());
    assertThat(finding.toString(), containsString("category=DUPLICATE"));
    assertEquals(finding, same);
    assertEquals(finding.hashCode(), same.hashCode());
    assertNotEquals(finding, singleBdFinding);
    assertThat(singleBdFinding.getBridgeDomain2(), equalTo(null));
  }
}
