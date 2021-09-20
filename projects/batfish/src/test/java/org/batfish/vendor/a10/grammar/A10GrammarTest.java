package org.batfish.vendor.a10.grammar;

import static com.google.common.collect.Iterables.getOnlyElement;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.matchers.ParseWarningMatchers.hasComment;
import static org.batfish.common.matchers.WarningsMatchers.hasParseWarnings;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.datamodel.ConfigurationFormat.A10_ACOS;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasConfigurationFormat;
import static org.batfish.main.BatfishTestUtils.TEST_SNAPSHOT;
import static org.batfish.main.BatfishTestUtils.configureBatfishTestSettings;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

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
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.vendor.ConversionContext;
import org.batfish.vendor.a10.representation.A10Configuration;
import org.batfish.vendor.a10.representation.Interface;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Tests of VS model extraction and VS model-to-VI model conversion for {@link A10Configuration}s.
 */
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
      assertThat(configs, hasKey(canonicalHostname));
      Configuration c = configs.get(canonicalHostname);
      assertThat(c, hasConfigurationFormat(A10_ACOS));
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
  public void testHostnameExtraction() {
    String hostname = "hostname";
    A10Configuration c = parseVendorConfig(hostname);
    assertThat(c, notNullValue());
    // Confirm hostname extracted as-typed; will be lower-cased in conversion.
    assertThat(c.getHostname(), equalTo("HOSTNAME"));
  }

  @Test
  public void testHostnameConversion() {
    String hostname = "hostname";
    Configuration c = parseConfig(hostname);
    assertThat(c, notNullValue());
    // Should be lower-cased
    assertThat(c.getHostname(), equalTo("hostname"));
  }

  @Test
  public void testInterfacesExtraction() {
    String hostname = "interfaces";
    A10Configuration c = parseVendorConfig(hostname);

    Map<Integer, Interface> eths = c.getInterfacesEthernet();
    Map<Integer, Interface> loops = c.getInterfacesLoopback();
    assertThat(eths.keySet(), containsInAnyOrder(1, 9));
    assertThat(loops.keySet(), containsInAnyOrder(0, 10));

    Interface eth1 = eths.get(1);
    Interface eth9 = eths.get(9);
    assertTrue(eth1.getEnabled());
    assertThat(eth1.getIpAddress(), equalTo(ConcreteInterfaceAddress.parse("10.0.1.1/24")));
    assertThat(eth1.getMtu(), equalTo(1234));
    assertThat(eth1.getName(), equalTo("this is a comp\"licat'ed name"));
    assertThat(eth1.getNumber(), equalTo(1));
    assertThat(eth1.getType(), equalTo(Interface.Type.ETHERNET));

    assertFalse(eth9.getEnabled());
    assertThat(eth9.getIpAddress(), equalTo(ConcreteInterfaceAddress.parse("10.0.2.1/24")));
    assertNull(eth9.getMtu());
    assertThat(eth9.getName(), equalTo("baz"));
    assertThat(eth9.getNumber(), equalTo(9));
    assertThat(eth9.getType(), equalTo(Interface.Type.ETHERNET));

    Interface loop0 = loops.get(0);
    Interface loop10 = loops.get(10);
    assertNull(loop0.getEnabled());
    assertThat(loop0.getIpAddress(), equalTo(ConcreteInterfaceAddress.parse("192.168.0.1/32")));
    assertThat(loop0.getNumber(), equalTo(0));
    assertThat(loop0.getType(), equalTo(Interface.Type.LOOPBACK));

    assertNull(loop10.getIpAddress());
  }

  @Test
  public void testInterfaceWarn() throws IOException {
    String filename = "interface_warn";
    Batfish batfish = getBatfishForConfigurationNames(filename);
    Warnings warnings =
        getOnlyElement(
            batfish
                .loadParseVendorConfigurationAnswerElement(batfish.getSnapshot())
                .getWarnings()
                .values());
    assertThat(
        warnings,
        hasParseWarnings(
            containsInAnyOrder(
                hasComment(
                    "Expected interface name with length in range 1-63, but got"
                        + " '1234567890123456789012345678901234567890123456789012345678901234'"),
                hasComment("Expected interface loopback number in range 0-10, but got '11'"),
                hasComment("Expected interface ethernet number in range 1-40, but got '0'"))));
  }
}
