package org.batfish.vendor.fastpath.grammar;

import static com.google.common.collect.Iterables.getOnlyElement;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.datamodel.ConfigurationFormat.FASTPATH;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasConfigurationFormat;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasHostname;
import static org.batfish.main.BatfishTestUtils.DUMMY_SNAPSHOT_1;
import static org.batfish.main.BatfishTestUtils.configureBatfishTestSettings;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.notNullValue;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.common.plugin.IBatfish;
import org.batfish.config.Settings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.InitInfoAnswerElement;
import org.batfish.datamodel.answers.ParseStatus;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.vendor.fastpath.representation.FastpathConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Tests of the FastPath grammar. */
@ParametersAreNonnullByDefault
public final class FastpathGrammarTest {

  private static final String TESTCONFIGS_PREFIX =
      "org/batfish/vendor/fastpath/grammar/testconfigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private @Nonnull Batfish getBatfishForConfigurationNames(String... configurationNames)
      throws IOException {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    Batfish batfish = BatfishTestUtils.getBatfishForTextConfigs(_folder, names);
    return batfish;
  }

  private @Nonnull Configuration parseConfig(String hostname) throws IOException {
    Map<String, Configuration> configs = parseTextConfigs(hostname);
    String canonicalHostname = hostname.toLowerCase();
    assertThat(configs, hasEntry(equalTo(canonicalHostname), hasHostname(canonicalHostname)));
    return configs.get(canonicalHostname);
  }

  private @Nonnull Map<String, Configuration> parseTextConfigs(String... configurationNames)
      throws IOException {
    IBatfish iBatfish = getBatfishForConfigurationNames(configurationNames);
    return iBatfish.loadConfigurations(iBatfish.getSnapshot());
  }

  private @Nonnull FastpathConfiguration parseVendorConfig(String hostname) {
    String src = readResource(TESTCONFIGS_PREFIX + hostname, UTF_8);
    Settings settings = new Settings();
    configureBatfishTestSettings(settings);
    FastpathCombinedParser parser = new FastpathCombinedParser(src, settings);
    Warnings warnings = new Warnings();
    FastpathControlPlaneExtractor extractor =
        new FastpathControlPlaneExtractor(src, parser, warnings, new SilentSyntaxCollection());
    ParserRuleContext tree =
        Batfish.parse(parser, new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false), settings);
    extractor.processParseTree(DUMMY_SNAPSHOT_1, tree);
    FastpathConfiguration vc = (FastpathConfiguration) extractor.getVendorConfiguration();
    vc.setFilename(TESTCONFIGS_PREFIX + hostname);

    // Crash if the vendor model is not serializable.
    vc = SerializationUtils.clone(vc);
    vc.setWarnings(warnings);
    return vc;
  }

  @Test
  public void testHostnameExtraction() {
    FastpathConfiguration c = parseVendorConfig("fastpath_hostname");
    assertThat(c.getHostname(), equalTo("fastpath_hostname"));
  }

  @Test
  public void testSetPromptExtraction() {
    // Legacy Quanta variant.
    FastpathConfiguration c = parseVendorConfig("fastpath_set_prompt");
    assertThat(c.getHostname(), equalTo("fastpath_set_prompt"));
  }

  @Test
  public void testBannerIsIgnored() {
    // The `!System ...` header banner is a comment block and must not break parsing.
    FastpathConfiguration c = parseVendorConfig("fastpath_banner");
    assertThat(c.getHostname(), equalTo("fastpath_banner"));
  }

  @Test
  public void testToVendorIndependentConfiguration() {
    FastpathConfiguration vc = parseVendorConfig("fastpath_hostname");
    vc.setVendor(FASTPATH);
    List<Configuration> configs = vc.toVendorIndependentConfigurations();
    Configuration c = getOnlyElement(configs);
    assertThat(c, hasHostname("fastpath_hostname"));
    assertThat(c.getDefaultVrf(), notNullValue());
  }

  @Test
  public void testHostnameEndToEnd() throws IOException {
    // Exercises the full pipeline: format detection -> parse -> convert. The config's banner is
    // what routes it to FASTPATH, so this also guards the detector/parser-job wiring.
    Configuration c = parseConfig("fastpath_hostname");
    assertThat(c, hasConfigurationFormat(FASTPATH));
    assertThat(c.getHostname(), equalTo("fastpath_hostname"));
  }

  @Test
  public void testDns() throws IOException {
    // DNS client: `ip name server` -> DNS servers, `ip domain name` -> default domain.
    Configuration c = parseConfig("fastpath_dns");
    assertThat(c.getDnsServers(), equalTo(ImmutableSet.of("1.1.1.1", "1.1.1.2")));
    assertThat(c.getDomainName(), equalTo("example.com"));
  }

  @Test
  public void testNtp() throws IOException {
    // SNTP: `sntp server` -> NTP servers.
    Configuration c = parseConfig("fastpath_ntp");
    assertThat(c.getNtpServers(), equalTo(ImmutableSet.of("2.2.2.1", "2.2.2.2")));
  }

  @Test
  public void testSyslog() throws IOException {
    // Logging: `logging host` -> logging (syslog) servers.
    Configuration c = parseConfig("fastpath_syslog");
    assertThat(c.getLoggingServers(), equalTo(ImmutableSet.of("3.3.3.1", "3.3.3.2")));
  }

  /**
   * Like {@link #getBatfishForConfigurationNames} but recovers from unrecognized/unsupported lines
   * instead of throwing, as production parsing does. This lets a config that mixes supported
   * management commands with not-yet-modeled lines parse to {@link
   * ParseStatus#PARTIALLY_UNRECOGNIZED} rather than {@code FAILED}.
   */
  private @Nonnull Batfish getBatfishAllowUnrecognized(String hostname) throws IOException {
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    batfish.getSettings().setDisableUnrecognized(false);
    batfish.getSettings().setThrowOnLexerError(false);
    batfish.getSettings().setThrowOnParserError(false);
    return batfish;
  }

  @Test
  public void testManagementServicesFromRealisticConfig() throws IOException {
    // A representative management block (placeholder values) surrounded by not-yet-modeled lines
    // (serviceport, VLANs, interfaces, OSPF): the DNS/NTP/Syslog values must still extract.
    Batfish batfish = getBatfishAllowUnrecognized("fastpath_management_services");
    Configuration c =
        batfish.loadConfigurations(batfish.getSnapshot()).get("fastpath_management_services");
    assertThat(c, hasConfigurationFormat(FASTPATH));
    assertThat(c.getDnsServers(), equalTo(ImmutableSet.of("1.2.3.4", "1.2.3.5")));
    assertThat(c.getDomainName(), equalTo("example.com"));
    assertThat(c.getNtpServers(), equalTo(ImmutableSet.of("2.3.4.5", "2.3.4.6")));
    assertThat(c.getLoggingServers(), equalTo(ImmutableSet.of("3.4.5.6", "3.4.5.7")));
  }

  @Test
  public void testUnrecognizedLinesArePartiallyRecognized() throws IOException {
    // Not-yet-modeled lines must not fail the whole parse: the config parses to
    // PARTIALLY_UNRECOGNIZED and the supported lines are still modeled.
    String hostname = "fastpath_management_services";
    Batfish batfish = getBatfishAllowUnrecognized(hostname);
    Configuration c = batfish.loadConfigurations(batfish.getSnapshot()).get(hostname);
    assertThat(c, hasHostname(hostname));
    InitInfoAnswerElement initInfo = batfish.initInfo(batfish.getSnapshot(), false, true);
    assertThat(
        initInfo.getParseStatus().get("configs/" + hostname),
        equalTo(ParseStatus.PARTIALLY_UNRECOGNIZED));
  }
}
