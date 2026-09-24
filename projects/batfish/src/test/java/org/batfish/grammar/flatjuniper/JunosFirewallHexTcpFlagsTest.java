package org.batfish.grammar.flatjuniper;

import static org.batfish.datamodel.IpProtocol.TCP;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasIpAccessList;
import static org.batfish.datamodel.matchers.IpAccessListMatchers.accepts;
import static org.batfish.datamodel.matchers.IpAccessListMatchers.rejects;
import static org.batfish.grammar.JunosGrammarTestUtils.parseConfig;
import static org.hamcrest.MatcherAssert.assertThat;

import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosFirewallHexTcpFlagsTest {

  private static final String HOSTNAME = "firewall-hex-tcp-flags";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private static Flow.Builder tcpFlow() {
    return Flow.builder()
        .setIngressNode(HOSTNAME)
        .setIpProtocol(TCP)
        .setSrcIp(Ip.parse("192.0.2.1"))
        .setDstIp(Ip.parse("198.51.100.1"))
        .setSrcPort(12345)
        .setDstPort(80);
  }

  @Test
  public void testConversion() {
    Configuration config = parseConfig(_folder, HOSTNAME);
    Flow syn = tcpFlow().setTcpFlagsSyn(true).build();
    Flow synFin = tcpFlow().setTcpFlagsSyn(true).setTcpFlagsFin(true).build();
    Flow fin = tcpFlow().setTcpFlagsFin(true).build();
    Flow none = tcpFlow().build();
    Flow all =
        tcpFlow()
            .setTcpFlagsAck(true)
            .setTcpFlagsCwr(true)
            .setTcpFlagsEce(true)
            .setTcpFlagsFin(true)
            .setTcpFlagsPsh(true)
            .setTcpFlagsRst(true)
            .setTcpFlagsSyn(true)
            .setTcpFlagsUrg(true)
            .build();
    Flow allExceptUrg = all.toBuilder().setTcpFlagsUrg(false).build();

    assertThat(config, hasIpAccessList("HEX-SYN", accepts(syn, null, config)));
    assertThat(config, hasIpAccessList("HEX-SYN", rejects(none, null, config)));
    assertThat(config, hasIpAccessList("HEX-BOTH", accepts(synFin, null, config)));
    assertThat(config, hasIpAccessList("HEX-BOTH", rejects(syn, null, config)));
    assertThat(config, hasIpAccessList("HEX-NOT-BOTH", rejects(synFin, null, config)));
    assertThat(config, hasIpAccessList("HEX-NOT-BOTH", accepts(syn, null, config)));
    assertThat(config, hasIpAccessList("HEX-NOT-BOTH", accepts(fin, null, config)));
    assertThat(config, hasIpAccessList("HEX-NEVER", rejects(synFin, null, config)));
    assertThat(config, hasIpAccessList("HEX-NEVER", rejects(none, null, config)));
    assertThat(config, hasIpAccessList("HEX-NEVER-REVERSE", rejects(synFin, null, config)));
    assertThat(config, hasIpAccessList("HEX-ALL", accepts(all, null, config)));
    assertThat(config, hasIpAccessList("HEX-ALL", rejects(allExceptUrg, null, config)));
    assertThat(config, hasIpAccessList("HEX-NOT-ALL", rejects(all, null, config)));
    assertThat(config, hasIpAccessList("HEX-NOT-ALL", accepts(allExceptUrg, null, config)));
    assertThat(config, hasIpAccessList("HEX-NOT-ZERO", rejects(none, null, config)));
    assertThat(config, hasIpAccessList("NAMED-ALL", accepts(all, null, config)));
    assertThat(config, hasIpAccessList("NAMED-ALL", rejects(allExceptUrg, null, config)));
  }
}
