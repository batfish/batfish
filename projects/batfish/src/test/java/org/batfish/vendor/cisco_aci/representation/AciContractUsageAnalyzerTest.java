package org.batfish.vendor.cisco_aci.representation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

/** Tests for {@link AciContractUsageAnalyzer}. */
public final class AciContractUsageAnalyzerTest {

  @Test
  public void testAnalyzeContractUsageFindsOrphanedConsumerAndBrokenFilterReference() {
    AciConfiguration config = new AciConfiguration();
    AciConfiguration.Tenant tenant = new AciConfiguration.Tenant("tenant1");

    AciConfiguration.Contract contract = new AciConfiguration.Contract("tenant1:c1");
    contract.setTenant("tenant1");
    AciConfiguration.Contract.Subject subject = new AciConfiguration.Contract.Subject();
    AciConfiguration.Contract.Filter filterRef = new AciConfiguration.Contract.Filter();
    filterRef.setName("missing-filter");
    subject.setFilters(ImmutableList.of(filterRef));
    contract.setSubjects(ImmutableList.of(subject));

    tenant.getContracts().put("tenant1:c1", contract);
    config.getContracts().put("tenant1:c1", contract);

    AciConfiguration.Epg consumer = new AciConfiguration.Epg("epgConsumer");
    consumer.setTenant("tenant1");
    consumer.setConsumedContracts(ImmutableList.of("tenant1:c1"));
    tenant.getEpgs().put("tenant1:epgConsumer", consumer);

    config.getTenants().put("tenant1", tenant);

    List<ContractUsageFinding> findings = AciContractUsageAnalyzer.analyzeContractUsage(config);
    List<ContractUsageFinding.Category> categories =
        findings.stream().map(ContractUsageFinding::getCategory).collect(Collectors.toList());

    assertThat(categories, hasItem(ContractUsageFinding.Category.ORPHANED_CONSUMER));
    assertThat(categories, hasItem(ContractUsageFinding.Category.BROKEN_REFERENCE));
  }

  @Test
  public void testAnalyzeContractUsageFindsRedundantContracts() {
    AciConfiguration config = new AciConfiguration();
    AciConfiguration.Tenant tenant = new AciConfiguration.Tenant("tenant1");

    AciConfiguration.Contract c1 = new AciConfiguration.Contract("tenant1:c1");
    c1.setTenant("tenant1");
    AciConfiguration.Contract.Subject s1 = new AciConfiguration.Contract.Subject();
    AciConfiguration.Contract.Filter rf1 = new AciConfiguration.Contract.Filter();
    rf1.setName("f1");
    rf1.setIpProtocol("tcp");
    s1.setFilters(ImmutableList.of(rf1));
    c1.setSubjects(ImmutableList.of(s1));

    AciConfiguration.Contract c2 = new AciConfiguration.Contract("tenant1:c2");
    c2.setTenant("tenant1");
    AciConfiguration.Contract.Subject s2 = new AciConfiguration.Contract.Subject();
    AciConfiguration.Contract.Filter rf2 = new AciConfiguration.Contract.Filter();
    rf2.setName("f1");
    rf2.setIpProtocol("tcp");
    s2.setFilters(ImmutableList.of(rf2));
    c2.setSubjects(ImmutableList.of(s2));

    tenant.getContracts().put("tenant1:c1", c1);
    tenant.getContracts().put("tenant1:c2", c2);
    config.getContracts().put("tenant1:c1", c1);
    config.getContracts().put("tenant1:c2", c2);
    config.getTenants().put("tenant1", tenant);

    List<ContractUsageFinding> findings = AciContractUsageAnalyzer.analyzeContractUsage(config);
    long redundantCount =
        findings.stream()
            .filter(f -> f.getCategory() == ContractUsageFinding.Category.REDUNDANT)
            .count();

    assertThat(redundantCount >= 2, equalTo(true));
  }
}
