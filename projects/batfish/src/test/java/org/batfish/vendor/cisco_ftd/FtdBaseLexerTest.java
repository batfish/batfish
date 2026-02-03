package org.batfish.vendor.cisco_ftd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import org.batfish.vendor.cisco_ftd.representation.FtdConfiguration;
import org.junit.Test;

/** Tests for FTD lexer functionality (tokenization, state tracking). */
public class FtdBaseLexerTest extends FtdGrammarTest {

  @Test
  public void testPrintStateVariables() {
    // Test that the lexer can parse and track state correctly
    String config = "hostname test-ftd\n";
    FtdConfiguration vc = parseVendorConfig(config);

    assertThat(vc, notNullValue());
    assertThat(vc.getWarnings(), notNullValue());
  }

  @Test
  public void testTokenizationOfSimpleConfig() {
    String config = "hostname test-ftd\n";
    FtdConfiguration vc = parseVendorConfig(config);

    assertThat(vc.getHostname(), equalTo("test-ftd"));
  }

  @Test
  public void testTokenizationOfInterfaceConfig() {
    String config =
        join(
            "interface GigabitEthernet0/0",
            "  nameif outside",
            "  ip address 10.1.1.1 255.255.255.0");
    FtdConfiguration vc = parseVendorConfig(config);

    assertThat(vc.getInterfaces(), hasKey("GigabitEthernet0/0"));
  }

  @Test
  public void testTokenizationOfAccessList() {
    String config =
        join(
            "access-list ACL_IN extended permit ip any any",
            "access-list ACL_IN extended deny tcp any any eq 22");
    FtdConfiguration vc = parseVendorConfig(config);

    assertThat(vc.getAccessLists(), hasKey("ACL_IN"));
    assertThat(vc.getAccessLists().get("ACL_IN").getLines(), hasSize(2));
  }

  @Test
  public void testTokenizationOfObjectGroup() {
    String config =
        join(
            "object-group network WEB_SERVERS",
            "  network-object host 10.1.1.1",
            "  network-object host 10.1.1.2");

    FtdConfiguration vc = parseVendorConfig(config);

    assertThat(vc.getNetworkObjectGroups(), hasKey("WEB_SERVERS"));
    assertThat(vc.getNetworkObjectGroups().get("WEB_SERVERS").getMembers(), hasSize(2));
  }

  @Test
  public void testTokenizationOfNatRule() {
    String config = "nat (inside,outside) source dynamic ANY INTERFACE\n";
    FtdConfiguration vc = parseVendorConfig(config);

    assertThat(vc.getNatRules(), hasSize(1));
  }

  @Test
  public void testTokenizationOfOspf() {
    String config =
        join(
            "router ospf 100",
            "  router-id 1.1.1.1",
            "  network 192.168.1.0 255.255.255.0 area 0",
            "  passive-interface inside");

    FtdConfiguration vc = parseVendorConfig(config);

    assertThat(vc.getOspfProcesses(), hasKey("100"));
  }

  @Test
  public void testTokenizationOfBgp() {
    String config =
        join("router bgp 65001", "  router-id 1.1.1.1", "  neighbor 10.1.1.1 remote-as 65002");

    FtdConfiguration vc = parseVendorConfig(config);

    assertThat(vc.getBgpProcess(), notNullValue());
    assertThat(vc.getBgpProcess().getAsn(), equalTo(65001L));
  }

  @Test
  public void testTokenizationOfCryptoConfig() {
    String config =
        join(
            "crypto map CMAP 10 match address VPN_ACL",
            "crypto map CMAP 10 set peer 1.2.3.4",
            "crypto map CMAP 10 set transform-set ESP-AES-SHA");

    FtdConfiguration vc = parseVendorConfig(config);

    assertThat(vc.getCryptoMaps(), hasKey("CMAP"));
  }

  @Test
  public void testLexerHandlesEmptyInput() {
    String config = "";
    FtdConfiguration vc = parseVendorConfig(config);

    assertThat(vc, notNullValue());
  }

  @Test
  public void testLexerHandlesWhitespace() {
    String config = "   \n\n  hostname test  \n";
    FtdConfiguration vc = parseVendorConfig(config);

    assertThat(vc.getHostname(), equalTo("test"));
  }

  @Test
  public void testLexerHandlesComments() {
    String config =
        join(
            "! This is a comment",
            "hostname test",
            "! Another comment",
            "interface GigabitEthernet0/0");

    FtdConfiguration vc = parseVendorConfig(config);

    assertThat(vc.getHostname(), equalTo("test"));
    assertThat(vc.getInterfaces(), hasKey("GigabitEthernet0/0"));
  }

  @Test
  public void testLexerHandlesComplexInterfaceName() {
    String config = "interface Port-channel1.320\n";
    FtdConfiguration vc = parseVendorConfig(config);

    assertThat(vc.getInterfaces(), hasKey("Port-channel1.320"));
  }

  @Test
  public void testLexerHandlesHyphenatedNames() {
    String config =
        join(
            "object-group network WEB-SERVERS",
            "  network-object host 10.1.1.1",
            "interface GigabitEthernet0/0",
            "  nameif WEB-SRV");

    FtdConfiguration vc = parseVendorConfig(config);

    assertThat(vc.getNetworkObjectGroups(), hasKey("WEB-SERVERS"));
    assertThat(vc.getInterfaces(), hasKey("GigabitEthernet0/0"));
  }

  @Test
  public void testLexerHandlesAllKeywordTypes() {
    String config =
        join(
            "hostname test",
            "interface GigabitEthernet0/0",
            "  nameif outside",
            "  security-level 0",
            "  mtu 1500",
            "  shutdown",
            "access-list ACL1 extended permit ip any any",
            "router ospf 100",
            "  network 192.168.1.0 255.255.255.0 area 0");

    FtdConfiguration vc = parseVendorConfig(config);

    assertThat(vc.getHostname(), equalTo("test"));
    assertThat(vc.getInterfaces(), hasKey("GigabitEthernet0/0"));
    assertThat(vc.getAccessLists(), hasKey("ACL1"));
    assertThat(vc.getOspfProcesses(), hasKey("100"));
  }

  @Test
  public void testLexerHandlesLargeConfig() {
    StringBuilder sb = new StringBuilder();
    sb.append("hostname test\n");
    for (int i = 0; i < 100; i++) {
      sb.append("interface GigabitEthernet").append(i).append("/0\n");
      sb.append(" nameif INT").append(i).append("\n");
    }

    FtdConfiguration vc = parseVendorConfig(sb.toString());

    assertThat(vc.getHostname(), equalTo("test"));
    assertThat(vc.getInterfaces().size(), equalTo(100));
  }
}
