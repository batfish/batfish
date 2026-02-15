package org.batfish.vendor.cisco_ftd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.io.CharStreams;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.batfish.datamodel.Configuration;
import org.batfish.vendor.cisco_ftd.representation.FtdConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test parsing of realistic FTD configuration examples.
 *
 * <p>This test reads actual FTD configuration files and attempts to parse them, reporting
 * success/failure and any parsing issues.
 */
public class FtdRealisticConfigTest extends FtdGrammarTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private static class ParseResult {
    String _configName;
    boolean _success;
    String _errorMessage;
    int _warningCount;
    List<String> _warningMessages;

    ParseResult(String configName) {
      this._configName = configName;
      this._warningMessages = new ArrayList<>();
    }
  }

  /** Parse a single FTD configuration file and return detailed results. */
  private ParseResult parseConfigResource(String resourceName) {
    ParseResult result = new ParseResult(resourceName);

    try (InputStream inputStream = getClass().getResourceAsStream(resourceName)) {
      if (inputStream == null) {
        result._success = false;
        result._errorMessage = "Configuration resource not found: " + resourceName;
        return result;
      }

      String configText =
          CharStreams.toString(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

      // Parse the configuration using the parent class method
      FtdConfiguration vc = parseVendorConfig(configText);

      // Try to convert to vendor-independent configuration
      try {
        List<Configuration> configs = vc.toVendorIndependentConfigurations();
        if (configs == null || configs.isEmpty()) {
          result._success = false;
          result._errorMessage =
              "Conversion to vendor-independent config produced no configurations";
          return result;
        }
      } catch (Exception e) {
        result._success = false;
        result._errorMessage = "Conversion to vendor-independent config failed: " + e.getMessage();
        return result;
      }

      // Success - record warnings if any
      result._success = true;
      result._warningCount = vc.getWarnings().getParseWarnings().size();
      vc.getWarnings().getParseWarnings().forEach(w -> result._warningMessages.add(w.toString()));

    } catch (Exception e) {
      result._success = false;
      result._errorMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
      if (e.getCause() != null) {
        result._errorMessage += " (caused by: " + e.getCause().getMessage() + ")";
      }
    }

    return result;
  }

  /** Test parsing of BGP configuration with loopback interface. */
  @Test
  public void testBgpWithLoopback() {
    String config =
        join(
            "! Cisco FTD BGP Configuration with Loopback Interface",
            "! Based on \"Configure eBGP with Loopback Interface on Secure Firewall\"",
            "",
            "hostname ftd-bgp-router",
            "NGFW VERSION 7.4.0",
            "",
            "interface GigabitEthernet0/0",
            " nameif OUTSIDE",
            " security-level 0",
            " ip address 203.0.113.1 255.255.255.0",
            "!",
            "",
            "interface GigabitEthernet0/1",
            " nameif INSIDE",
            " security-level 100",
            " ip address 192.168.1.1 255.255.255.0",
            "!",
            "",
            "interface Loopback0",
            " nameif LOOPBACK0",
            " security-level 0",
            " ip address 10.0.0.1 255.255.255.255",
            "!",
            "",
            "router bgp 65001",
            " bgp log-neighbor-changes",
            " bgp router-id 10.0.0.1",
            " neighbor 203.0.113.2 remote-as 65002",
            " neighbor 203.0.113.2 description \"ISP Peer\"",
            " neighbor 203.0.113.2 timers keepalive 10 holdtime 30",
            " !",
            " address-family ipv4",
            "  neighbor 203.0.113.2 activate",
            "  network 192.168.1.0 mask 255.255.255.0",
            "  network 10.0.0.1 mask 255.255.255.255",
            "  redistribute connected",
            " exit-address-family",
            "!",
            "",
            "route OUTSIDE 0.0.0.0 0.0.0.0 203.0.113.254");

    ParseResult result = testParseString(config, "bgp_with_loopback.txt");
    reportResult("bgp_with_loopback.txt", result);
    assertThat(result._configName + " should parse successfully", result._success, equalTo(true));
  }

  /** Test parsing of prefilter ACL with TRUST action. */
  @Test
  public void testPrefilterTrustAcl() {
    String config =
        join(
            "! Cisco FTD Prefilter ACL with TRUST Action",
            "! Based on \"Configure Control Plane Access Control Policies\"",
            "",
            "hostname ftd-prefilter",
            "NGFW VERSION 7.4.0",
            "",
            "interface GigabitEthernet0/0",
            " nameif OUTSIDE",
            " security-level 0",
            " ip address 203.0.113.1 255.255.255.0",
            "!",
            "",
            "interface GigabitEthernet0/1",
            " nameif INSIDE",
            " security-level 100",
            " ip address 192.168.1.1 255.255.255.0",
            "!",
            "",
            "! Prefilter ACL with TRUST action",
            "access-list PREFILTER-OUTSIDE-IN advanced trust ip any any rule-id 268455934",
            "access-list PREFILTER-OUTSIDE-IN remark ACL-OUTSIDE-IN_#174 | Prefilter-FTD",
            "access-list PREFILTER-OUTSIDE-IN extended permit ip 192.168.1.0 255.255.255.0 any"
                + " rule-id 268455935",
            "access-list PREFILTER-OUTSIDE-IN extended deny ip any any rule-id 268455936 log",
            "",
            "! Apply ACL to interface",
            "access-group PREFILTER-OUTSIDE-IN global",
            "",
            "! NAT configuration",
            "nat (INSIDE,OUTSIDE) source dynamic PAT-INSIDE interface");

    ParseResult result = testParseString(config, "prefilter_trust_acl.txt");
    reportResult("prefilter_trust_acl.txt", result);
    assertThat(result._configName + " should parse successfully", result._success, equalTo(true));
  }

  /** Test parsing of site-to-site VPN configuration. */
  @Test
  public void testSiteToSiteVpn() {
    String config =
        join(
            "! Cisco FTD Site-to-Site IKEv2 VPN Configuration",
            "! Based on \"Configure a VRF Aware Site-to-Site Tunnel with IKEv2\"",
            "",
            "hostname ftd-vpn",
            "NGFW VERSION 7.4.0",
            "",
            "interface GigabitEthernet0/0",
            " nameif OUTSIDE",
            " security-level 0",
            " ip address 203.0.113.1 255.255.255.0",
            "!",
            "",
            "interface GigabitEthernet0/1",
            " nameif INSIDE",
            " security-level 100",
            " ip address 192.168.1.1 255.255.255.0",
            "!",
            "",
            "! IKEv2 Proposal",
            "crypto ikev2 proposal FTD-IKEv2-PROPOSAL",
            " encryption aes-cbc-256",
            " integrity sha256",
            " group 14",
            "!",
            "",
            "! IKEv2 Policy",
            "crypto ikev2 policy FTD-IKEv2-POLICY",
            " proposal FTD-IKEv2-PROPOSAL",
            "!",
            "",
            "! Tunnel Group",
            "tunnel-group REMOTE-SITE type ipsec-l2l",
            "tunnel-group REMOTE-SITE ipsec-attributes",
            " ikev2 remote-authentication pre-shared-key MySecret123",
            " ikev2 local-authentication pre-shared-key MySecret123",
            "!",
            "",
            "! Crypto Map",
            "crypto map CRYPTO-MAP 10 match address VPN-TRAFFIC",
            "crypto map CRYPTO-MAP 10 set peer 198.51.100.1",
            "crypto map CRYPTO-MAP 10 set ikev2 ipsec-proposal FTD-IPSEC-PROPOSAL",
            "crypto map CRYPTO-MAP interface OUTSIDE",
            "",
            "! ACL for VPN traffic",
            "access-list VPN-TRAFFIC extended permit ip 192.168.1.0 255.255.255.0 10.10.10.0"
                + " 255.255.255.0");

    ParseResult result = testParseString(config, "site_to_site_vpn.txt");
    reportResult("site_to_site_vpn.txt", result);
    assertThat(result._configName + " should parse successfully", result._success, equalTo(true));
  }

  /** Test parsing of OSPF interface configuration. */
  @Test
  public void testOspfInterfaces() {
    String config =
        join(
            "! Cisco FTD OSPF Configuration",
            "! Based on various OSPF configuration examples",
            "",
            "hostname ftd-ospf",
            "NGFW VERSION 7.4.0",
            "",
            "interface GigabitEthernet0/0",
            " nameif OUTSIDE",
            " security-level 0",
            " ip address 203.0.113.1 255.255.255.0",
            "!",
            "",
            "interface GigabitEthernet0/1",
            " nameif INSIDE",
            " security-level 100",
            " ip address 192.168.1.1 255.255.255.0",
            "!",
            "",
            "interface GigabitEthernet0/2",
            " nameif DMZ",
            " security-level 50",
            " ip address 172.16.1.1 255.255.255.0",
            "!",
            "",
            "router ospf 1",
            " router-id 1.1.1.1",
            " network 203.0.113.0 255.255.255.0 area 0",
            " network 192.168.1.0 255.255.255.0 area 0",
            " network 172.16.1.0 255.255.255.0 area 1",
            " area 0 filter-list prefix AREA0-OUT out",
            " area 1 filter-list prefix AREA1-IN in",
            " log-adjacency-changes",
            " default-information originate always",
            " redistribute connected metric 1 metric-type 1 route-map CONNECTED-MAP",
            "!",
            "",
            "route OUTSIDE 0.0.0.0 0.0.0.0 203.0.113.254");

    ParseResult result = testParseString(config, "ospf_interfaces.txt");
    reportResult("ospf_interfaces.txt", result);
    assertThat(result._configName + " should parse successfully", result._success, equalTo(true));
  }

  /** Test all configurations and print a summary. */
  @Test
  public void testAllConfigurations() {
    System.out.println("\n=== Cisco FTD Parser Test Results ===\n");

    List<ParseResult> results = new ArrayList<>();

    // Test BGP with loopback
    results.add(testBgpWithLoopbackResult());

    // Test prefilter trust ACL
    results.add(testPrefilterTrustAclResult());

    // Test site-to-site VPN
    results.add(testSiteToSiteVpnResult());

    // Test OSPF interfaces
    results.add(testOspfInterfacesResult());

    // Print summary
    int successCount = 0;
    int failureCount = 0;

    for (ParseResult result : results) {
      System.out.println("Configuration: " + result._configName);
      System.out.println("  Status: " + (result._success ? "SUCCESS" : "FAILURE"));

      if (result._success) {
        successCount++;
        System.out.println("  Warnings: " + result._warningCount);
        if (result._warningCount > 0) {
          System.out.println("  Warning details:");
          for (String warning : result._warningMessages) {
            System.out.println("    - " + warning);
          }
        }
      } else {
        failureCount++;
        System.out.println("  Error: " + result._errorMessage);
      }
      System.out.println();
    }

    System.out.println("Summary:");
    System.out.println("  Total: " + results.size());
    System.out.println("  Passed: " + successCount);
    System.out.println("  Failed: " + failureCount);
    System.out.println();

    // Assert that all tests passed
    assertThat("All configurations should parse successfully", failureCount, equalTo(0));
  }

  private ParseResult testParseString(String configText, String configName) {
    ParseResult result = new ParseResult(configName);

    try {
      // Parse the configuration using the parent class method
      FtdConfiguration vc = parseVendorConfig(configText);

      // Try to convert to vendor-independent configuration
      try {
        List<Configuration> configs = vc.toVendorIndependentConfigurations();
        if (configs == null || configs.isEmpty()) {
          result._success = false;
          result._errorMessage =
              "Conversion to vendor-independent config produced no configurations";
          return result;
        }
      } catch (Exception e) {
        result._success = false;
        result._errorMessage = "Conversion to vendor-independent config failed: " + e.getMessage();
        // Add stack trace for debugging
        result._errorMessage += "\n" + getStackTrace(e);
        return result;
      }

      // Success - record warnings if any
      result._success = true;
      result._warningCount = vc.getWarnings().getParseWarnings().size();
      vc.getWarnings().getParseWarnings().forEach(w -> result._warningMessages.add(w.toString()));

    } catch (Exception e) {
      result._success = false;
      result._errorMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
      // Add stack trace for debugging
      result._errorMessage += "\n" + getStackTrace(e);
      if (e.getCause() != null) {
        result._errorMessage +=
            "\nCaused by: "
                + e.getCause().getClass().getSimpleName()
                + ": "
                + e.getCause().getMessage();
        result._errorMessage += "\n" + getStackTrace(e.getCause());
      }
    }

    return result;
  }

  private ParseResult testBgpWithLoopbackResult() {
    String config =
        join(
            "hostname ftd-bgp-router",
            "NGFW VERSION 7.4.0",
            "interface GigabitEthernet0/0",
            " nameif OUTSIDE",
            " security-level 0",
            " ip address 203.0.113.1 255.255.255.0",
            "interface GigabitEthernet0/1",
            " nameif INSIDE",
            " security-level 100",
            " ip address 192.168.1.1 255.255.255.0",
            "interface Loopback0",
            " nameif LOOPBACK0",
            " security-level 0",
            " ip address 10.0.0.1 255.255.255.255",
            "router bgp 65001",
            " bgp log-neighbor-changes",
            " bgp router-id 10.0.0.1",
            " neighbor 203.0.113.2 remote-as 65002",
            " neighbor 203.0.113.2 description \"ISP Peer\"",
            " neighbor 203.0.113.2 timers keepalive 10 holdtime 30",
            " address-family ipv4",
            "  neighbor 203.0.113.2 activate",
            "  network 192.168.1.0 mask 255.255.255.0",
            "  network 10.0.0.1 mask 255.255.255.255",
            "  redistribute connected",
            " exit-address-family",
            "route OUTSIDE 0.0.0.0 0.0.0.0 203.0.113.254");
    return testParseString(config, "bgp_with_loopback.txt");
  }

  private ParseResult testPrefilterTrustAclResult() {
    String config =
        join(
            "hostname ftd-prefilter",
            "NGFW VERSION 7.4.0",
            "interface GigabitEthernet0/0",
            " nameif OUTSIDE",
            " security-level 0",
            " ip address 203.0.113.1 255.255.255.0",
            "interface GigabitEthernet0/1",
            " nameif INSIDE",
            " security-level 100",
            " ip address 192.168.1.1 255.255.255.0",
            "access-list PREFILTER-OUTSIDE-IN advanced trust ip any any rule-id 268455934",
            "access-list PREFILTER-OUTSIDE-IN remark ACL-OUTSIDE-IN_#174 | Prefilter-FTD",
            "access-list PREFILTER-OUTSIDE-IN extended permit ip 192.168.1.0 255.255.255.0 any"
                + " rule-id 268455935",
            "access-list PREFILTER-OUTSIDE-IN extended deny ip any any rule-id 268455936 log",
            "access-group PREFILTER-OUTSIDE-IN global",
            "nat (INSIDE,OUTSIDE) source dynamic PAT-INSIDE interface");
    return testParseString(config, "prefilter_trust_acl.txt");
  }

  private ParseResult testSiteToSiteVpnResult() {
    String config =
        join(
            "hostname ftd-vpn",
            "NGFW VERSION 7.4.0",
            "interface GigabitEthernet0/0",
            " nameif OUTSIDE",
            " security-level 0",
            " ip address 203.0.113.1 255.255.255.0",
            "interface GigabitEthernet0/1",
            " nameif INSIDE",
            " security-level 100",
            " ip address 192.168.1.1 255.255.255.0",
            "crypto ikev2 proposal FTD-IKEv2-PROPOSAL",
            " encryption aes-cbc-256",
            " integrity sha256",
            " group 14",
            "crypto ikev2 policy FTD-IKEv2-POLICY",
            " proposal FTD-IKEv2-PROPOSAL",
            "tunnel-group REMOTE-SITE type ipsec-l2l",
            "tunnel-group REMOTE-SITE ipsec-attributes",
            " ikev2 remote-authentication pre-shared-key MySecret123",
            " ikev2 local-authentication pre-shared-key MySecret123",
            "crypto map CRYPTO-MAP 10 match address VPN-TRAFFIC",
            "crypto map CRYPTO-MAP 10 set peer 198.51.100.1",
            "crypto map CRYPTO-MAP 10 set ikev2 ipsec-proposal FTD-IPSEC-PROPOSAL",
            "crypto map CRYPTO-MAP interface OUTSIDE",
            "access-list VPN-TRAFFIC extended permit ip 192.168.1.0 255.255.255.0 10.10.10.0"
                + " 255.255.255.0");
    return testParseString(config, "site_to_site_vpn.txt");
  }

  private ParseResult testOspfInterfacesResult() {
    String config =
        join(
            "hostname ftd-ospf",
            "NGFW VERSION 7.4.0",
            "interface GigabitEthernet0/0",
            " nameif OUTSIDE",
            " security-level 0",
            " ip address 203.0.113.1 255.255.255.0",
            "interface GigabitEthernet0/1",
            " nameif INSIDE",
            " security-level 100",
            " ip address 192.168.1.1 255.255.255.0",
            "interface GigabitEthernet0/2",
            " nameif DMZ",
            " security-level 50",
            " ip address 172.16.1.1 255.255.255.0",
            "router ospf 1",
            " router-id 1.1.1.1",
            " network 203.0.113.0 255.255.255.0 area 0",
            " network 192.168.1.0 255.255.255.0 area 0",
            " network 172.16.1.0 255.255.255.0 area 1",
            " area 0 filter-list prefix AREA0-OUT out",
            " area 1 filter-list prefix AREA1-IN in",
            " log-adjacency-changes",
            " default-information originate always",
            " redistribute connected metric 1 metric-type 1 route-map CONNECTED-MAP",
            "route OUTSIDE 0.0.0.0 0.0.0.0 203.0.113.254");
    return testParseString(config, "ospf_interfaces.txt");
  }

  private String getStackTrace(Throwable t) {
    StringBuilder sb = new StringBuilder();
    for (StackTraceElement element : t.getStackTrace()) {
      sb.append("    at ").append(element.toString()).append("\n");
    }
    return sb.toString();
  }

  private void reportResult(String configName, ParseResult result) {
    System.out.println("\n--- Testing: " + configName + " ---");
    if (result._success) {
      System.out.println("Status: PASSED");
      if (result._warningCount > 0) {
        System.out.println("Warnings: " + result._warningCount);
      }
    } else {
      System.out.println("Status: FAILED");
      System.out.println("Error: " + result._errorMessage);
    }
  }
}
