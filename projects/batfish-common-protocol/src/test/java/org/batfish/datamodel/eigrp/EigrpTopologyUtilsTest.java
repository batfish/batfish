package org.batfish.datamodel.eigrp;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkConfigurations;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Vrf;
import org.junit.Test;

/** Tests for {@link EigrpTopologyUtils} */
public class EigrpTopologyUtilsTest {

  @Test
  public void testInitNeighborConfigs() {
    NetworkFactory nf = new NetworkFactory();
    Configuration configuration =
        nf.configurationBuilder()
            .setHostname("conf")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    Vrf vrf = nf.vrfBuilder().setName("vrf").setOwner(configuration).build();
    EigrpMetric metric =
        EigrpMetric.builder().setBandwidth(1d).setDelay(1d).setMode(EigrpProcessMode.NAMED).build();
    nf.interfaceBuilder()
        .setName("iface1")
        .setOwner(configuration)
        .setVrf(vrf)
        .setAddress(ConcreteInterfaceAddress.create(Ip.parse("1.1.1.1"), 24))
        .setEigrp(
            EigrpInterfaceSettings.builder()
                .setAsn(1L)
                .setEnabled(true)
                .setPassive(true)
                .setMetric(metric)
                .build())
        .build();
    nf.interfaceBuilder()
        .setName("iface2")
        .setOwner(configuration)
        .setVrf(vrf)
        .setAddress(ConcreteInterfaceAddress.create(Ip.parse("2.2.2.2"), 24))
        .setEigrp(
            EigrpInterfaceSettings.builder()
                .setAsn(2L)
                .setEnabled(true)
                .setPassive(true)
                .setMetric(metric)
                .build())
        .build();
    vrf.addEigrpProcess(
        EigrpProcess.builder()
            .setAsNumber(1L)
            .setMode(EigrpProcessMode.NAMED)
            .setRouterId(Ip.parse("1.1.1.1"))
            .setExportPolicy("policy")
            .build());

    EigrpTopologyUtils.initNeighborConfigs(
        NetworkConfigurations.of(ImmutableMap.of(configuration.getHostname(), configuration)));

    // only the neighbor corresponding to matching ASN is initialized
    assertThat(
        configuration.getVrfs().get("vrf").getEigrpProcesses().get(1L).getNeighbors(),
        equalTo(
            ImmutableMap.of(
                "iface1",
                EigrpNeighborConfig.builder()
                    .setAsn(1L)
                    .setInterfaceName("iface1")
                    .setPassive(true)
                    .setHostname("conf")
                    .setVrfName("vrf")
                    .setIp(Ip.parse("1.1.1.1"))
                    .build())));
  }
}
