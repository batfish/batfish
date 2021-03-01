package org.batfish.grammar.fortios;

import static com.google.common.collect.Iterables.getOnlyElement;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.matchers.ParseWarningMatchers.hasComment;
import static org.batfish.common.matchers.WarningsMatchers.hasParseWarning;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasHostname;
import static org.batfish.datamodel.matchers.MapMatchers.hasKeys;
import static org.batfish.main.BatfishTestUtils.TEST_SNAPSHOT;
import static org.batfish.main.BatfishTestUtils.configureBatfishTestSettings;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import javax.annotation.Nonnull;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.common.bdd.IpAccessListToBdd;
import org.batfish.common.bdd.IpSpaceToBDD;
import org.batfish.common.plugin.IBatfish;
import org.batfish.config.Settings;
import org.batfish.datamodel.BddTestbed;
import org.batfish.datamodel.Configuration;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.representation.fortios.FortiosConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public final class FortiosGrammarTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();
  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testHostnameExtraction() {
    String filename = "fortios_hostname";
    String hostname = "my_fortios-hostname1";
    assertThat(parseVendorConfig(filename).getHostname(), equalTo(hostname));
  }

  @Test
  public void testHostnameConversion() throws IOException {
    String filename = "fortios_hostname";
    String hostname = "my_fortios-hostname1";
    assertThat(parseTextConfigs(filename), hasEntry(equalTo(hostname), hasHostname(hostname)));
  }

  @Test
  public void testInvalidHostnameWithDotExtraction() {
    String filename = "fortios_bad_hostname";
    // invalid hostname from config file is thrown away
    assertThat(parseVendorConfig(filename).getHostname(), nullValue());
  }

  @Test
  public void testInvalidHostnameWithDotConversion() throws IOException {
    String filename = "fortios_bad_hostname";
    Batfish batfish = getBatfishForConfigurationNames(filename);
    Warnings warnings =
        getOnlyElement(
            batfish
                .loadParseVendorConfigurationAnswerElement(batfish.getSnapshot())
                .getWarnings()
                .values());
    assertThat(warnings, hasParseWarning(hasComment("Illegal value for device hostname")));
  }

  @Test
  public void testReplacemsgExtraction() {
    String hostname = "fortios_replacemsg";
    String majorType = "admin";
    String minorTypePre = "pre_admin-disclaimer-text";
    String minorTypePost = "post_admin-disclaimer-text";
    FortiosConfiguration vc = parseVendorConfig(hostname);
    assertThat(
        vc.getReplacemsgs(), hasEntry(equalTo(majorType), hasKeys(minorTypePre, minorTypePost)));
    assertThat(
        vc.getReplacemsgs().get(majorType).get(minorTypePre).getBuffer(),
        equalTo("\"npre\"''\\\\nabc\\\\\\\" \"\nlastline"));
    assertThat(vc.getReplacemsgs().get(majorType).get(minorTypePost).getBuffer(), nullValue());
  }

  @Test
  public void testReplacemsgConversion() throws IOException {
    String filename = "fortios_replacemsg";
    Batfish batfish = getBatfishForConfigurationNames(filename);
    // Should see a single conversion warning for Ethernet1/1's conflicting speeds
    Warnings warnings =
        getOnlyElement(
            batfish
                .loadParseVendorConfigurationAnswerElement(batfish.getSnapshot())
                .getWarnings()
                .values());
    assertThat(warnings, hasParseWarning(hasComment("Illegal value for replacemsg minor type")));
  }

  private static final BddTestbed BDD_TESTBED =
      new BddTestbed(ImmutableMap.of(), ImmutableMap.of());

  @SuppressWarnings("unused")
  private static final IpAccessListToBdd ACL_TO_BDD;

  @SuppressWarnings("unused")
  private static final IpSpaceToBDD DST_IP_BDD;

  @SuppressWarnings("unused")
  private static final IpSpaceToBDD SRC_IP_BDD;

  private static final String TESTCONFIGS_PREFIX = "org/batfish/grammar/fortios/testconfigs/";

  @SuppressWarnings("unused")
  private static final String SNAPSHOTS_PREFIX = "org/batfish/grammar/fortios/snapshots/";

  static {
    DST_IP_BDD = BDD_TESTBED.getDstIpBdd();
    SRC_IP_BDD = BDD_TESTBED.getSrcIpBdd();
    ACL_TO_BDD = BDD_TESTBED.getAclToBdd();
  }

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

  private @Nonnull FortiosConfiguration parseVendorConfig(String hostname) {
    String src = readResource(TESTCONFIGS_PREFIX + hostname, UTF_8);
    Settings settings = new Settings();
    configureBatfishTestSettings(settings);
    FortiosCombinedParser parser = new FortiosCombinedParser(src, settings);
    FortiosControlPlaneExtractor extractor =
        new FortiosControlPlaneExtractor(src, parser, new Warnings());
    ParserRuleContext tree =
        Batfish.parse(parser, new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false), settings);
    extractor.processParseTree(TEST_SNAPSHOT, tree);
    FortiosConfiguration vendorConfiguration =
        (FortiosConfiguration) extractor.getVendorConfiguration();
    vendorConfiguration.setFilename(TESTCONFIGS_PREFIX + hostname);
    // crash if not serializable
    return SerializationUtils.clone(vendorConfiguration);
  }
}
