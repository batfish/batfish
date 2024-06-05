package org.batfish.grammar.cool_nos;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.matchers.ParseWarningMatchers.hasComment;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.datamodel.matchers.MapMatchers.hasKeys;
import static org.batfish.main.BatfishTestUtils.DUMMY_SNAPSHOT_1;
import static org.batfish.main.BatfishTestUtils.configureBatfishTestSettings;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

import javax.annotation.Nonnull;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.config.Settings;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.main.Batfish;
import org.batfish.vendor.cool_nos.CoolNosConfiguration;
import org.batfish.vendor.cool_nos.NextHopDiscard;
import org.batfish.vendor.cool_nos.NextHopGateway;
import org.batfish.vendor.cool_nos.NextHopInterface;
import org.batfish.vendor.cool_nos.StaticRoute;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Tests of Cool NOS example grammar */
public final class CoolNosGrammarTest {

  @Test
  public void testHostnameExtraction() {
    String hostname = "cool.nos.host-name";
    // vc for vendor configuration
    CoolNosConfiguration vc = parseVendorConfig(hostname);
    assertThat(vc.getHostname(), equalTo(hostname));
  }

  @Test
  public void testHostnameWarnings() {
    String hostname = "cool.nos.host-name";
    CoolNosConfiguration vc = parseVendorConfig(hostname);
    assertThat(
        vc.getWarnings().getParseWarnings(),
        containsInAnyOrder(
            hasComment(
                "Invalid hostname 'underscores_not_allowed' does not match regex:"
                    + " [-A-Za-z0-9]+(\\.[-A-Za-z0-9]+)*"),
            hasComment(
                "Invalid hostname '.cannot-start-with-period' does not match regex:"
                    + " [-A-Za-z0-9]+(\\.[-A-Za-z0-9]+)*"),
            hasComment(
                "Invalid hostname 'cannot-end-with-period.' does not match regex:"
                    + " [-A-Za-z0-9]+(\\.[-A-Za-z0-9]+)*"),
            hasComment(
                "Expected hostname with length in range 1-32, but got"
                    + " 'cannot-exceed-max-length-of-32-characters'")));
  }

  @Test
  public void testStaticRouteExtraction() {
    String hostname = "cool-nos-static-routes";
    CoolNosConfiguration vc = parseVendorConfig(hostname);
    assertThat(
        vc.getStaticRoutes(),
        hasKeys(
            Prefix.ZERO,
            Prefix.strict("192.0.2.0/24"),
            Prefix.strict("192.0.2.1/32"),
            Prefix.strict("192.0.2.2/32")));
    {
      StaticRoute sr = vc.getStaticRoutes().get(Prefix.ZERO);
      assertThat(sr.getNextHop(), instanceOf(NextHopInterface.class));
      assertThat(((NextHopInterface) sr.getNextHop()).getInterface(), equalTo("ethernet 1"));
    }
    {
      StaticRoute sr = vc.getStaticRoutes().get(Prefix.strict("192.0.2.0/24"));
      assertThat(sr.getNextHop(), instanceOf(NextHopDiscard.class));
    }
    {
      StaticRoute sr = vc.getStaticRoutes().get(Prefix.strict("192.0.2.1/32"));
      assertThat(sr.getNextHop(), instanceOf(NextHopGateway.class));
      assertThat(((NextHopGateway) sr.getNextHop()).getIp(), equalTo(Ip.parse("192.168.1.1")));
    }
    {
      StaticRoute sr = vc.getStaticRoutes().get(Prefix.strict("192.0.2.2/32"));
      assertThat(sr.getNextHop(), instanceOf(NextHopInterface.class));
      assertThat(((NextHopInterface) sr.getNextHop()).getInterface(), equalTo("vlan 1000"));
    }
  }

  @Test
  public void testStaticRouteWarnings() {
    String hostname = "cool-nos-static-routes";
    CoolNosConfiguration vc = parseVendorConfig(hostname);
    assertThat(
        vc.getWarnings().getParseWarnings(),
        containsInAnyOrder(
            hasComment("Expected vlan number in range 1-4094, but got '5000'"),
            hasComment("Attempt to redefine existing static route for prefix 0.0.0.0/0"),
            hasComment("Attempt to modify non-existent static route for prefix 192.168.1.1/32"),
            hasComment("Attempt to delete non-existent static route with prefix 192.168.2.2/32")));
  }

  @Test
  public void testLogSyslog() {
    String hostname = "cool.nos.null-rule";
    parseVendorConfig(hostname);
  }

  // TODO: helpful conversion tests without having to alter VendorConfigurationFormatDetector

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private @Nonnull CoolNosConfiguration parseVendorConfig(String hostname) {
    String src = readResource(TESTCONFIGS_PREFIX + hostname, UTF_8);
    Settings settings = new Settings();
    configureBatfishTestSettings(settings);
    CoolNosCombinedParser combinedParser = new CoolNosCombinedParser(src, settings);
    Warnings warnings = new Warnings();
    CoolNosControlPlaneExtractor extractor =
        new CoolNosControlPlaneExtractor(
            src, combinedParser, warnings, new SilentSyntaxCollection());
    ParserRuleContext tree =
        Batfish.parse(
            combinedParser, new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false), settings);
    extractor.processParseTree(DUMMY_SNAPSHOT_1, tree);
    CoolNosConfiguration vendorConfiguration =
        (CoolNosConfiguration) extractor.getVendorConfiguration();
    vendorConfiguration.setFilename(TESTCONFIGS_PREFIX + hostname);

    // crash if not serializable
    vendorConfiguration = SerializationUtils.clone(vendorConfiguration);
    vendorConfiguration.setWarnings(warnings);
    return vendorConfiguration;
  }

  private static final String TESTCONFIGS_PREFIX = "org/batfish/grammar/cool_nos/testconfigs/";
}
