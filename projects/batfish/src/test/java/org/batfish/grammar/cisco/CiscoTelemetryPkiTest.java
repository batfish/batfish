package org.batfish.grammar.cisco;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;

import java.io.IOException;
import java.util.List;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.config.Settings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.vendor_family.cisco.CiscoFamily;
import org.batfish.datamodel.vendor_family.cisco.Pki;
import org.batfish.datamodel.vendor_family.cisco.Telemetry;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.representation.cisco.CiscoConfiguration;
import org.junit.Test;

public class CiscoTelemetryPkiTest {

  @Test
  public void testTelemetryAndPkiConversion() throws IOException {
    String config =
        String.join(
                "\n",
                "crypto pki trustpoint SLA-TrustPoint",
                " enrollment url http://example.com/scep",
                " subject-alt-name dns example.com",
                " usage ssl-client",
                " source vrf Mgmt",
                " revocation-check crl",
                "!",
                "telemetry ietf subscription 602",
                " encoding encode-tdl",
                " filter xpath /services;serviceName=foo",
                " stream yang-push",
                " update-policy periodic 360000",
                " source-address 10.35.1.21",
                " source-vrf Mgmt",
                " receiver ip address 10.0.0.1 DNAC_ASSURANCE_RECEIVER port 57000 protocol grpc-tcp"
                    + " receiver-type collector",
                "!")
            + "\n";

    Settings settings = new Settings();
    CiscoCombinedParser ciscoParser = new CiscoCombinedParser(config, settings);
    Warnings warnings = new Warnings(true, true, true);
    CiscoControlPlaneExtractor extractor =
        new CiscoControlPlaneExtractor(
            config,
            ciscoParser,
            ConfigurationFormat.CISCO_IOS,
            warnings,
            new SilentSyntaxCollection());
    ParserRuleContext tree =
        Batfish.parse(
            ciscoParser, new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false), settings);
    extractor.processParseTree(BatfishTestUtils.DUMMY_SNAPSHOT_1, tree);

    CiscoConfiguration vConfig = (CiscoConfiguration) extractor.getVendorConfiguration();
    vConfig.setHostname("telemetry-pki-test");
    List<Configuration> configs = vConfig.toVendorIndependentConfigurations();
    Configuration c = configs.get(0);
    CiscoFamily family = c.getVendorFamily().getCisco();

    Pki pki = family.getPki();
    assertThat(pki.getTrustpoints(), hasKey("SLA-TrustPoint"));
    Pki.Trustpoint tp = pki.getTrustpoints().get("SLA-TrustPoint");
    assertThat(tp.getEnrollment(), equalTo("url http://example.com/scep"));
    assertThat(tp.getRevocationCheck(), equalTo("crl"));
    assertThat(tp.getSubjectAltName(), equalTo("dns example.com"));
    assertThat(tp.getUsage(), equalTo("ssl-client"));
    assertThat(tp.getSourceVrf(), equalTo("Mgmt"));

    Telemetry telemetry = family.getTelemetry();
    assertThat(telemetry.getSubscriptions(), hasKey(602));
    Telemetry.Subscription sub = telemetry.getSubscriptions().get(602);
    assertThat(sub.getEncoding(), equalTo(TelemetrySubscription.EncodingType.ENCODE_TDL));
    assertThat(sub.getFilter(), equalTo("xpath /services;serviceName=foo"));
    assertThat(sub.getFilterType(), equalTo(TelemetrySubscription.FilterType.XPATH));
    assertThat(sub.getFilterValue(), equalTo("/services;serviceName=foo"));
    assertThat(sub.getStream(), equalTo(TelemetrySubscription.StreamType.YANG_PUSH));
    assertThat(sub.getUpdatePolicy(), equalTo("periodic 360000"));
    assertThat(sub.getSourceAddress(), equalTo(Ip.parse("10.35.1.21")));
    assertThat(sub.getSourceVrf(), equalTo("Mgmt"));
    assertThat(sub.getReceivers().size(), equalTo(1));
    Telemetry.Receiver receiver = sub.getReceivers().get(0);
    assertThat(receiver.getName(), equalTo("DNAC_ASSURANCE_RECEIVER"));
    assertThat(receiver.getHost(), equalTo("10.0.0.1"));
    assertThat(receiver.getPort(), equalTo(57000));
    assertThat(receiver.getProtocol(), equalTo(TelemetrySubscription.ProtocolType.GRPC_TCP));
    assertThat(receiver.getReceiverType(), equalTo("collector"));
  }
}
