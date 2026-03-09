package org.batfish.representation.fortios;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Vrf;
import org.junit.Test;

public class FortiosIpsecConversionsTest {

  @Test
  public void testConvertIpsec_emptyConfigs() {
    Configuration c = new Configuration("test", ConfigurationFormat.FORTIOS);
    Vrf vrf = new Vrf("default");
    c.getVrfs().put("default", vrf);

    Warnings warnings = new Warnings(true, true, true);

    FortiosIpsecConversions.convertIpsec(new HashMap<>(), new HashMap<>(), c, warnings);

    // Should complete without error and without creating IPsec configs
    assertTrue(c.getIpsecPeerConfigs().isEmpty());
    assertTrue(warnings.getRedFlagWarnings().isEmpty());
  }

  @Test
  public void testConvertIpsec_basicPhase1() {
    Configuration c = new Configuration("test", ConfigurationFormat.FORTIOS);
    Vrf vrf = new Vrf("default");
    c.getVrfs().put("default", vrf);

    Interface wanIface =
        Interface.builder()
            .setName("wan1")
            .setType(InterfaceType.PHYSICAL)
            .setAdminUp(true)
            .setAddress(ConcreteInterfaceAddress.create(Ip.parse("203.0.113.1"), 24))
            .build();
    wanIface.setVrfName("default");
    c.getAllInterfaces().put("wan1", wanIface);

    IpsecPhase1 phase1 = new IpsecPhase1("vpn-tunnel-1");
    phase1.setInterface("wan1");
    phase1.setRemoteGateway(Ip.parse("198.51.100.1"));

    Map<String, IpsecPhase1> phase1Configs = ImmutableMap.of("vpn-tunnel-1", phase1);

    Warnings warnings = new Warnings(true, true, true);

    FortiosIpsecConversions.convertIpsec(phase1Configs, new HashMap<>(), c, warnings);

    // Should create IPsec peer config
    assertThat(c.getIpsecPeerConfigs().size(), equalTo(1));
    assertThat(c.getIpsecPeerConfigs(), hasKey("tunnel-vpn-tunnel-1"));
    assertTrue(warnings.getRedFlagWarnings().isEmpty());
  }

  @Test
  public void testConvertIpsec_noRemoteGateway() {
    Configuration c = new Configuration("test", ConfigurationFormat.FORTIOS);
    Vrf vrf = new Vrf("default");
    c.getVrfs().put("default", vrf);

    Interface wanIface =
        Interface.builder()
            .setName("wan1")
            .setType(InterfaceType.PHYSICAL)
            .setAdminUp(true)
            .setAddress(ConcreteInterfaceAddress.create(Ip.parse("203.0.113.1"), 24))
            .build();
    wanIface.setVrfName("default");
    c.getAllInterfaces().put("wan1", wanIface);

    IpsecPhase1 phase1 = new IpsecPhase1("vpn-tunnel-1");
    phase1.setInterface("wan1");
    // No remote gateway

    Map<String, IpsecPhase1> phase1Configs = ImmutableMap.of("vpn-tunnel-1", phase1);

    Warnings warnings = new Warnings(true, true, true);

    FortiosIpsecConversions.convertIpsec(phase1Configs, new HashMap<>(), c, warnings);

    // Should warn and not create config
    assertTrue(c.getIpsecPeerConfigs().isEmpty());
    assertFalse(warnings.getRedFlagWarnings().isEmpty());
  }

  @Test
  public void testConvertIpsec_noInterface() {
    Configuration c = new Configuration("test", ConfigurationFormat.FORTIOS);
    Vrf vrf = new Vrf("default");
    c.getVrfs().put("default", vrf);

    IpsecPhase1 phase1 = new IpsecPhase1("vpn-tunnel-1");
    phase1.setRemoteGateway(Ip.parse("198.51.100.1"));
    // No interface

    Map<String, IpsecPhase1> phase1Configs = ImmutableMap.of("vpn-tunnel-1", phase1);

    Warnings warnings = new Warnings(true, true, true);

    FortiosIpsecConversions.convertIpsec(phase1Configs, new HashMap<>(), c, warnings);

    // Should warn and not create config
    assertTrue(c.getIpsecPeerConfigs().isEmpty());
    assertFalse(warnings.getRedFlagWarnings().isEmpty());
  }

  @Test
  public void testConvertIpsec_interfaceNotFound() {
    Configuration c = new Configuration("test", ConfigurationFormat.FORTIOS);
    Vrf vrf = new Vrf("default");
    c.getVrfs().put("default", vrf);

    IpsecPhase1 phase1 = new IpsecPhase1("vpn-tunnel-1");
    phase1.setInterface("nonexistent");
    phase1.setRemoteGateway(Ip.parse("198.51.100.1"));

    Map<String, IpsecPhase1> phase1Configs = ImmutableMap.of("vpn-tunnel-1", phase1);

    Warnings warnings = new Warnings(true, true, true);

    FortiosIpsecConversions.convertIpsec(phase1Configs, new HashMap<>(), c, warnings);

    // Should warn and not create config
    assertTrue(c.getIpsecPeerConfigs().isEmpty());
    assertFalse(warnings.getRedFlagWarnings().isEmpty());
  }

  @Test
  public void testConvertIpsec_interfaceNoAddress() {
    Configuration c = new Configuration("test", ConfigurationFormat.FORTIOS);
    Vrf vrf = new Vrf("default");
    c.getVrfs().put("default", vrf);

    Interface wanIface =
        Interface.builder()
            .setName("wan1")
            .setType(InterfaceType.PHYSICAL)
            .setAdminUp(true)
            // No address
            .build();
    wanIface.setVrfName("default");
    c.getAllInterfaces().put("wan1", wanIface);

    IpsecPhase1 phase1 = new IpsecPhase1("vpn-tunnel-1");
    phase1.setInterface("wan1");
    phase1.setRemoteGateway(Ip.parse("198.51.100.1"));

    Map<String, IpsecPhase1> phase1Configs = ImmutableMap.of("vpn-tunnel-1", phase1);

    Warnings warnings = new Warnings(true, true, true);

    FortiosIpsecConversions.convertIpsec(phase1Configs, new HashMap<>(), c, warnings);

    // Should warn and not create config
    assertTrue(c.getIpsecPeerConfigs().isEmpty());
    assertFalse(warnings.getRedFlagWarnings().isEmpty());
  }

  @Test
  public void testConvertIpsec_multipleTunnels() {
    Configuration c = new Configuration("test", ConfigurationFormat.FORTIOS);
    Vrf vrf = new Vrf("default");
    c.getVrfs().put("default", vrf);

    Interface wanIface =
        Interface.builder()
            .setName("wan1")
            .setType(InterfaceType.PHYSICAL)
            .setAdminUp(true)
            .setAddress(ConcreteInterfaceAddress.create(Ip.parse("203.0.113.1"), 24))
            .build();
    wanIface.setVrfName("default");
    c.getAllInterfaces().put("wan1", wanIface);

    IpsecPhase1 phase1a = new IpsecPhase1("vpn-tunnel-1");
    phase1a.setInterface("wan1");
    phase1a.setRemoteGateway(Ip.parse("198.51.100.1"));

    IpsecPhase1 phase1b = new IpsecPhase1("vpn-tunnel-2");
    phase1b.setInterface("wan1");
    phase1b.setRemoteGateway(Ip.parse("198.51.100.2"));

    Map<String, IpsecPhase1> phase1Configs =
        ImmutableMap.of("vpn-tunnel-1", phase1a, "vpn-tunnel-2", phase1b);

    Warnings warnings = new Warnings(true, true, true);

    FortiosIpsecConversions.convertIpsec(phase1Configs, new HashMap<>(), c, warnings);

    // Should create two IPsec peer configs
    assertThat(c.getIpsecPeerConfigs().size(), equalTo(2));
    assertThat(c.getIpsecPeerConfigs(), hasKey("tunnel-vpn-tunnel-1"));
    assertThat(c.getIpsecPeerConfigs(), hasKey("tunnel-vpn-tunnel-2"));
    assertTrue(warnings.getRedFlagWarnings().isEmpty());
  }

  @Test
  public void testIpsecPhase1Defaults() {
    IpsecPhase1 phase1 = new IpsecPhase1("test");

    assertThat(phase1.getKeylifeEffective(), equalTo(IpsecPhase1.DEFAULT_KEYLIFE));
  }

  @Test
  public void testIpsecPhase2Phase1Reference() {
    IpsecPhase2 phase2 = new IpsecPhase2("test-p2");

    phase2.setPhase1Name("vpn-tunnel-1");
    assertThat(phase2.getPhase1Name(), equalTo("vpn-tunnel-1"));
  }

  @Test
  public void testIpsecPhase2AddressSelectors() {
    IpsecPhase2 phase2 = new IpsecPhase2("test-p2");

    phase2.setSrcName("local_subnet");
    phase2.setDstName("remote_subnet");

    assertThat(phase2.getSrcName(), equalTo("local_subnet"));
    assertThat(phase2.getDstName(), equalTo("remote_subnet"));
  }
}
