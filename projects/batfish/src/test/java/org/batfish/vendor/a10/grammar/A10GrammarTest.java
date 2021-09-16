package org.batfish.vendor.a10.grammar;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.datamodel.ConfigurationFormat.CHECK_POINT_GATEWAY;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasConfigurationFormat;
import static org.batfish.main.BatfishTestUtils.TEST_SNAPSHOT;
import static org.batfish.main.BatfishTestUtils.configureBatfishTestSettings;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.runtime.SnapshotRuntimeData;
import org.batfish.config.Settings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.vendor.ConversionContext;
import org.batfish.vendor.a10.representation.A10Configuration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

@ParametersAreNonnullByDefault
public class A10GrammarTest {
  private static final String TESTCONFIGS_PREFIX = "org/batfish/vendor/a10/grammar/testconfigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private static @Nonnull A10Configuration parseVendorConfig(String hostname) {
    String src = readResource(TESTCONFIGS_PREFIX + hostname, UTF_8);
    Settings settings = new Settings();
    configureBatfishTestSettings(settings);
    A10CombinedParser parser = new A10CombinedParser(src, settings);
    Warnings parseWarnings = new Warnings();
    A10ControlPlaneExtractor extractor =
        new A10ControlPlaneExtractor(src, parser, parseWarnings, new SilentSyntaxCollection());
    ParserRuleContext tree =
        Batfish.parse(parser, new BatfishLogger(BatfishLogger.LEVELSTR_FATAL, false), settings);
    extractor.processParseTree(TEST_SNAPSHOT, tree);
    A10Configuration vendorConfiguration = (A10Configuration) extractor.getVendorConfiguration();
    vendorConfiguration.setFilename(TESTCONFIGS_PREFIX + hostname);
    // crash if not serializable
    A10Configuration vc = SerializationUtils.clone(vendorConfiguration);
    vc.setAnswerElement(new ConvertConfigurationAnswerElement());
    vc.setRuntimeData(SnapshotRuntimeData.EMPTY_SNAPSHOT_RUNTIME_DATA);
    vc.setWarnings(parseWarnings);
    return vc;
  }

  private @Nonnull Batfish getBatfishForConfigurationNames(String... configurationNames)
      throws IOException {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    ConversionContext conversionContext = new ConversionContext();
    return BatfishTestUtils.getBatfishForTextConfigsAndConversionContext(
        _folder, conversionContext, names);
  }

  private @Nonnull Configuration parseConfig(String hostname) {
    try {
      Map<String, Configuration> configs = parseTextConfigs(hostname);
      String canonicalHostname = hostname.toLowerCase();
      String canonicalChassisHostname = canonicalHostname + "-ch01-01";
      assertThat(configs, anyOf(hasKey(canonicalHostname), hasKey(canonicalChassisHostname)));
      Configuration c =
          configs.getOrDefault(canonicalHostname, configs.get(canonicalChassisHostname));
      assertThat(c, hasConfigurationFormat(CHECK_POINT_GATEWAY));
      return c;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private @Nonnull Map<String, Configuration> parseTextConfigs(String... configurationNames)
      throws IOException {
    IBatfish iBatfish = getBatfishForConfigurationNames(configurationNames);
    return iBatfish.loadConfigurations(iBatfish.getSnapshot());
  }

  @Test
  public void testHostname() {
    String hostname = "hostname";
    A10Configuration c = parseVendorConfig(hostname);
    assertThat(c, notNullValue());
    // Confirm hostname extracted as-typed; will be lower-cased in conversion.
    assertThat(c.getHostname(), equalTo("HOSTNAME"));
  }
}
