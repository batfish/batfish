package org.batfish.grammar.fortios;

import static org.batfish.main.BatfishTestUtils.DUMMY_SNAPSHOT_1;
import static org.batfish.main.BatfishTestUtils.configureBatfishTestSettings;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.notNullValue;

import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.config.Settings;
import org.batfish.datamodel.Ip;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.main.Batfish;
import org.batfish.representation.fortios.FortiosConfiguration;
import org.batfish.representation.fortios.Ippool;
import org.batfish.representation.fortios.Policy;
import org.junit.Test;

public class FortiosGrammarNewTokensTest {
  private static final String IPPOOL1 = "ippool1";

  private FortiosConfiguration parseConfig(String src) {
    Settings settings = new Settings();
    configureBatfishTestSettings(settings);
    FortiosCombinedParser parser = new FortiosCombinedParser(src, settings);
    Warnings warnings = new Warnings();
    FortiosControlPlaneExtractor extractor =
        new FortiosControlPlaneExtractor(src, parser, warnings, new SilentSyntaxCollection());
    ParserRuleContext tree =
        Batfish.parse(parser, new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false), settings);
    if (!parser.getErrors().isEmpty()) {
      throw new BatfishException("Parser errors: " + parser.getErrors());
    }
    extractor.processParseTree(DUMMY_SNAPSHOT_1, tree);
    return (FortiosConfiguration) extractor.getVendorConfiguration();
  }

  @Test
  public void testNewTokens() {
    // Test system tokens
    assertThat(parseConfig("config system ntp\nend\n"), notNullValue());
    assertThat(parseConfig("config system fortiguard\nend\n"), notNullValue());

    // Test new firewall tokens
    assertThat(
        parseConfig("config firewall ssl-ssh-profile\nedit \"deep-inspection\"\nnext\nend\n"),
        notNullValue());
    assertThat(
        parseConfig("config firewall profile-protocol-options\nedit \"default\"\nnext\nend\n"),
        notNullValue());
    assertThat(parseConfig("config firewall local-in-policy\nedit 1\nnext\nend\n"), notNullValue());
    assertThat(
        parseConfig("config firewall schedule onetime\nedit \"EXP\"\nnext\nend\n"), notNullValue());
    assertThat(
        parseConfig("config firewall ssh local-key\nedit \"RSA\"\nnext\nend\n"), notNullValue());
  }

  @Test
  public void testMoreNewTokens() {
    // Test recently added tokens
    assertThat(
        parseConfig("config emailfilter profile\nedit \"sniffer-profile\"\nnext\nend\n"),
        notNullValue());
    assertThat(
        parseConfig("config virtual-patch profile\nedit \"default\"\nnext\nend\n"), notNullValue());
    assertThat(
        parseConfig("config video-filter profile\nedit \"default\"\nnext\nend\n"), notNullValue());
    assertThat(parseConfig("config report layout\nedit \"default\"\nnext\nend\n"), notNullValue());
    assertThat(parseConfig("config waf profile\nedit \"default\"\nnext\nend\n"), notNullValue());
    assertThat(parseConfig("config casb saas-application\nend\n"), notNullValue());
    assertThat(parseConfig("config casb user-activity\nend\n"), notNullValue());

    // Test new policy set commands
    String policyConfig =
        """
        config firewall policy
        edit 1
        set ssl-ssh-profile "certificate-no-inspection"
        set av-profile "antivirus-monitor"
        set webfilter-profile "web-monitor"
        set dnsfilter-profile "dns-monitor"
        set ips-sensor "ips-monitor"
        set application-list "app-monitor"
        set profile-protocol-options "default"
        set file-filter-profile "file-monitor"
        set logtraffic all
        set logtraffic-start enable
        set nat enable
        set ippool enable
        set poolname "my-pool"
        set np-acceleration disable
        set auto-asic-offload disable
        next
        end
        """;
    assertThat(parseConfig(policyConfig), notNullValue());
  }

  @Test
  public void testIgnoredBlocks() {
    String config =
        """
        config firewall DoS-policy
        edit 1
        next
        end
        config firewall on-demand-sniffer
        edit "sniffer1"
        next
        end
        config wireless-controller wtp-profile
        edit "profile1"
        next
        end
        config router rip
        end
        config router ospf
        end
        config router isis
        end
        config router multicast
        end
        config router bgp
        config redistribute6 "connected"
        end
        end
        """;
    assertThat(parseConfig(config), notNullValue());
  }

  @Test
  public void testInterfaceSpeeds() {
    String config =
        """
        config system interface
        edit "port1"
        set speed 1000auto
        next
        edit "port2"
        set speed 10000auto
        next
        edit "port3"
        set speed 400Gfull
        next
        edit "port4"
        set speed 400Gauto
        next
        end
        """;
    assertThat(parseConfig(config), notNullValue());
  }

  @Test
  public void testServiceProtocolAll() {
    String config =
        """
        config firewall service custom
        edit "ALL_ICMP"
        set protocol ALL
        next
        end
        """;
    assertThat(parseConfig(config), notNullValue());
  }

  @Test
  public void testIppool() {
    String config =
        """
        config firewall ippool
            edit "ippool1"
                set type overload
                set startip 1.1.1.1
                set endip 1.1.1.10
                set comments "test pool"
                set associated-interface "port1"
            next
        end
        """;
    FortiosConfiguration c = parseConfig(config);
    assertThat(c.getIppools(), hasKey(IPPOOL1));
    Ippool pool = c.getIppools().get(IPPOOL1);
    assertThat(pool.getType(), equalTo(Ippool.Type.OVERLOAD));
    assertThat(pool.getStartip(), equalTo(Ip.parse("1.1.1.1")));
    assertThat(pool.getEndip(), equalTo(Ip.parse("1.1.1.10")));
    assertThat(pool.getComments(), equalTo("test pool"));
    assertThat(pool.getAssociatedInterface(), equalTo("port1"));
  }

  @Test
  public void testPolicyNatIppool() {
    String config =
        """
        config system interface
            edit "port1"
                set type physical
            next
            edit "port2"
                set type physical
            next
        end
        config firewall address
            edit "all"
                set type ipmask
                set subnet 0.0.0.0 0.0.0.0
            next
        end
        config firewall service custom
            edit "ALL"
                set protocol TCP/UDP/SCTP
                set tcp-portrange 1-65535
                set udp-portrange 1-65535
                set sctp-portrange 1-65535
            next
        end
        config firewall ippool
            edit "ippool1"
                set startip 1.1.1.1
                set endip 1.1.1.1
            next
        end
        config firewall policy
            edit 1
                set srcintf "port1"
                set dstintf "port2"
                set srcaddr "all"
                set dstaddr "all"
                set service "ALL"
                set nat enable
                set ippool enable
                set poolname "ippool1"
            next
        end
        """;
    FortiosConfiguration c = parseConfig(config);
    assertThat(c.getPolicies(), hasKey("1"));
    Policy p = c.getPolicies().get("1");
    assertThat(p.getNat(), equalTo(true));
    assertThat(p.getIppool(), equalTo(true));
    assertThat(p.getPoolnames(), contains(IPPOOL1));
  }

  @Test
  public void testMissingIgnoredBlocks() {
    String config =
        """
        config system accprofile
        end
        config system admin
        end
        config antivirus
        end
        config firewall address6
        end
        config system api-user
        end
        config application
        end
        config system automation-action
        end
        config system automation-stitch
        end
        config system automation-trigger
        end
        config firewall service category
        end
        config system console
        end
        config system custom-language
        end
        config system dns
        end
        config dnsfilter
        end
        config dlp
        end
        config endpoint-control
        end
        config system email-server
        end
        config system federated-upgrade
        end
        config file-filter
        end
        config system ftm-push
        end
        config system ha
        end
        config icap
        end
        config system ike
        end
        config firewall internet-service-definition
        end
        config ips
        end
        config system ipam
        end
        config firewall ippool
        end
        config log
        end
        config firewall multicast-address
        end
        config system netflow
        end
        config np-queues
        end
        config system np6
        end
        config system npu
        end
        config system object-tagging
        end
        config system settings
        end
        config system session-ttl
        end
        config system physical-switch
        end
        config ip-protocol
        end
        config ethernet-type
        end
        config firewall proxy-address
        end
        config system replacemsg-image
        end
        config system sdwan
        end
        config system session-helper
        end
        config firewall shaper
        end
        config system snmp
        end
        config split-port-mode
        end
        config system standalone-cluster
        end
        config system sso-admin
        end
        config system storage
        end
        config switch-controller
        end
        config user
        end
        config voip
        end
        config vpn
        end
        config wanopt
        end
        config web-proxy
        end
        config webfilter
        end
        """;
    assertThat(parseConfig(config), notNullValue());
  }
}
