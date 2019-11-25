package org.batfish.grammar.cumulus_concatenated;

import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasHostname;
import static org.batfish.main.BatfishTestUtils.configureBatfishTestSettings;
import static org.batfish.representation.cumulus.CumulusConversions.computeBgpGenerationPolicyName;
import static org.batfish.representation.cumulus.CumulusConversions.computeMatchSuppressedSummaryOnlyPolicyName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.common.util.CommonUtil;
import org.batfish.config.Settings;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.GeneratedRoute;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.bgp.BgpConfederation;
import org.batfish.grammar.GrammarSettings;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.representation.cumulus.CumulusNcluConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public class CumulusConcatenatedGrammarTest {
  private static final String INTERFACES_DELIMITER = "# This file describes the network interfaces";
  private static final String PORTS_DELIMITER = "# ports.conf --";
  private static final String FRR_DELIMITER = "frr version 4.0+cl3u8";

  private static final String TESTCONFIGS_PREFIX =
      "org/batfish/grammar/cumulus_concatenated/testconfigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private static CumulusNcluConfiguration parseFromTextWithSettings(String src, Settings settings) {
    CumulusConcatenatedCombinedParser parser = new CumulusConcatenatedCombinedParser(src, settings);
    ParserRuleContext tree =
        Batfish.parse(parser, new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false), settings);
    CumulusConcatenatedControlPlaneExtractor extractor =
        new CumulusConcatenatedControlPlaneExtractor(
            src, new Warnings(), "", settings, null, false);
    extractor.processParseTree(tree);
    return SerializationUtils.clone((CumulusNcluConfiguration) extractor.getVendorConfiguration());
  }

  private static CumulusNcluConfiguration parse(String src) {
    Settings settings = new Settings();
    settings.setDisableUnrecognized(true);
    settings.setThrowOnLexerError(true);
    settings.setThrowOnParserError(true);

    return parseFromTextWithSettings(src, settings);
  }

  private static CumulusNcluConfiguration parseLines(String... lines) {
    return parse(String.join("\n", lines) + "\n");
  }

  private static CumulusNcluConfiguration parseVendorConfig(String filename) {
    Settings settings = new Settings();
    configureBatfishTestSettings(settings);
    return parseVendorConfig(filename, settings);
  }

  private static CumulusNcluConfiguration parseVendorConfig(
      String filename, GrammarSettings settings) {
    String src = CommonUtil.readResource(TESTCONFIGS_PREFIX + filename);
    CumulusConcatenatedCombinedParser parser = new CumulusConcatenatedCombinedParser(src, settings);
    CumulusConcatenatedControlPlaneExtractor extractor =
        new CumulusConcatenatedControlPlaneExtractor(
            src, new Warnings(), filename, parser.getSettings(), null, false);
    ParserRuleContext tree =
        Batfish.parse(parser, new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false), settings);
    extractor.processParseTree(tree);
    CumulusNcluConfiguration config = (CumulusNcluConfiguration) extractor.getVendorConfiguration();
    config.setFilename(TESTCONFIGS_PREFIX + filename);
    return config;
  }

  private Batfish getBatfishForConfigurationNames(String... configurationNames) throws IOException {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    return BatfishTestUtils.getBatfishForTextConfigs(_folder, names);
  }

  private SortedMap<String, Configuration> parseTextConfigs(String... configurationNames)
      throws IOException {
    return getBatfishForConfigurationNames(configurationNames).loadConfigurations();
  }

  private @Nonnull Configuration parseConfig(String hostname) throws IOException {
    Map<String, Configuration> configs = parseTextConfigs(hostname);
    String canonicalHostname = hostname.toLowerCase();
    assertThat(configs, hasEntry(equalTo(canonicalHostname), hasHostname(canonicalHostname)));
    return configs.get(canonicalHostname);
  }

  @Test
  public void testConcatenation() {
    CumulusNcluConfiguration cfg = parseVendorConfig("concatenation");
    assertThat(cfg.getHostname(), equalTo("hostname"));
  }

  @Test
  public void testConcatenationWithLeadingGarbage() {
    CumulusNcluConfiguration cfg = parseVendorConfig("concatenation_with_leading_garbage");
    assertThat(cfg.getHostname(), equalTo("hostname"));
  }

  @Test
  public void testConcatenationWithMissingHostname() {
    CumulusNcluConfiguration cfg = parseVendorConfig("concatenation_with_missing_hostname");
    assertThat(cfg.getHostname(), emptyString());
  }

  @Test
  public void testPortsUnrecognized() {
    Settings settings = new Settings();
    configureBatfishTestSettings(settings);
    settings.setDisableUnrecognized(false);
    settings.setThrowOnLexerError(false);
    settings.setThrowOnParserError(false);
    CumulusNcluConfiguration cfg = parseVendorConfig("ports_unrecognized", settings);
    assertThat(cfg.getHostname(), equalTo("hostname"));
  }

  @Test
  public void testBgpAggregateAddress_e2e() {
    CumulusNcluConfiguration vsConfig = parseVendorConfig("bgp_aggregate_address");
    Configuration viConfig = vsConfig.toVendorIndependentConfigurations().get(0);
    Vrf vrf = viConfig.getDefaultVrf();

    Prefix prefix1 = Prefix.parse("1.1.1.0/24");
    Prefix prefix2 = Prefix.parse("2.2.0.0/16");

    // Test that the expected routes maps were generated. We test their semantics elsewhere.
    assertThat(
        viConfig.getRoutingPolicies(),
        hasKey(
            computeBgpGenerationPolicyName(
                true, Configuration.DEFAULT_VRF_NAME, prefix1.toString())));
    assertThat(
        viConfig.getRoutingPolicies(),
        hasKey(
            computeBgpGenerationPolicyName(
                true, Configuration.DEFAULT_VRF_NAME, prefix2.toString())));

    // Test that expected generated routes exist
    assertThat(
        vrf.getGeneratedRoutes().stream()
            .map(GeneratedRoute::getNetwork)
            .collect(ImmutableList.toImmutableList()),
        containsInAnyOrder(prefix1, prefix2));

    // suppression route map exists. Semantics tested elsewhere
    assertThat(
        viConfig.getRouteFilterLists(),
        hasKey(computeMatchSuppressedSummaryOnlyPolicyName(vrf.getName())));
  }

  @Test
  public void testVrf() {
    CumulusNcluConfiguration c =
        parseLines(
            "hostname",
            INTERFACES_DELIMITER,
            // declare vrf1
            "iface vrf1",
            "  vrf-table auto",
            PORTS_DELIMITER,
            FRR_DELIMITER,
            // add definition
            "vrf vrf1",
            "  vni 1000",
            "exit-vrf");
    assertThat(c.getVrfs().get("vrf1").getVni(), equalTo(1000));
  }

  @Test
  public void testBgpConfederationConversion() throws IOException {
    Configuration c = parseConfig("bgp_confederation");
    BgpProcess bgpProcess = c.getDefaultVrf().getBgpProcess();
    assertThat(
        bgpProcess.getConfederation(), equalTo(new BgpConfederation(12, ImmutableSet.of(65000L))));
    BgpActivePeerConfig neighbor = bgpProcess.getActiveNeighbors().get(Prefix.parse("1.1.1.1/32"));
    assertThat(neighbor.getConfederationAsn(), equalTo(12L));
    assertThat(neighbor.getLocalAs(), equalTo(65000L));
  }

  @Test
  public void testInterfaces() throws IOException {
    Configuration c = parseConfig("interface_test");

    assertThat(c.getAllInterfaces().keySet(), contains("lo", "swp1", "swp2"));

    Interface lo = c.getAllInterfaces().get("lo");
    assertEquals(lo.getAddress(), ConcreteInterfaceAddress.parse("1.1.1.1/32"));

    Interface swp1 = c.getAllInterfaces().get("swp1");
    assertEquals(swp1.getAddress(), ConcreteInterfaceAddress.parse("2.2.2.2/24"));

    Interface swp2 = c.getAllInterfaces().get("swp2");
    assertEquals(swp2.getAddress(), ConcreteInterfaceAddress.parse("3.3.3.3/24"));
    assertEquals(swp2.getSpeed(), Double.valueOf(10000 * 10e6));
  }
}
