package org.batfish.grammar;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.config.Settings;
import org.batfish.datamodel.Configuration;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.batfish.representation.juniper.NamedBgpGroup;
import org.batfish.representation.juniper.RoutingInstance;
import org.junit.rules.TemporaryFolder;

/** Utilities for testing Junos configurations. */
public final class JunosGrammarTestUtils {

  private static final String TESTCONFIGS_PREFIX = "org/batfish/grammar/juniper/testconfigs/";

  public static Batfish getBatfish(TemporaryFolder folder, String... configurationNames)
      throws IOException {
    String[] paths =
        Arrays.stream(configurationNames)
            .map(name -> TESTCONFIGS_PREFIX + name)
            .toArray(String[]::new);
    return BatfishTestUtils.getBatfishForTextConfigs(folder, paths);
  }

  public static Configuration parseConfig(TemporaryFolder folder, String hostname) {
    try {
      Configuration configuration = parseTextConfigs(folder, hostname).get(hostname.toLowerCase());
      if (configuration == null) {
        throw new AssertionError("Missing configuration for " + hostname);
      }
      return configuration;
    } catch (IOException e) {
      throw new AssertionError("Failed to parse " + hostname, e);
    }
  }

  public static Map<String, Configuration> parseTextConfigs(
      TemporaryFolder folder, String... configurationNames) throws IOException {
    Batfish batfish = getBatfish(folder, configurationNames);
    return batfish.loadConfigurations(batfish.getSnapshot());
  }

  public static JuniperConfiguration parseJuniperConfig(TemporaryFolder folder, String hostname) {
    return parseJuniperConfig(folder, hostname, false);
  }

  /** Parses a Junos configuration with optional error recovery. */
  public static JuniperConfiguration parseJuniperConfig(
      TemporaryFolder folder, String hostname, boolean allowErrors) {
    try {
      Batfish batfish = getBatfish(folder, hostname);
      Settings settings = batfish.getSettings();
      if (allowErrors) {
        settings.setDisableUnrecognized(false);
        settings.setHaltOnConvertError(false);
        settings.setHaltOnParseError(false);
        settings.setThrowOnLexerError(false);
        settings.setThrowOnParserError(false);
      }
      JuniperConfiguration configuration = getVendorConfiguration(batfish, hostname);
      if (configuration == null) {
        throw new AssertionError("Missing vendor configuration for " + hostname);
      }
      Warnings warnings =
          batfish
              .loadParseVendorConfigurationAnswerElement(batfish.getSnapshot())
              .getWarnings()
              .get("configs/" + hostname);
      if (warnings == null) {
        warnings = new Warnings(Warnings.Settings.fromLogger(batfish.getLogger()));
      }
      configuration.setWarnings(warnings);
      return configuration;
    } catch (IOException e) {
      throw new AssertionError("Failed to parse " + hostname, e);
    }
  }

  public static JuniperConfiguration getVendorConfiguration(Batfish batfish, String hostname) {
    return (JuniperConfiguration)
        batfish.loadVendorConfigurations(batfish.getSnapshot()).get(hostname);
  }

  public static RoutingInstance getDefaultRoutingInstance(Batfish batfish, String hostname) {
    return getVendorConfiguration(batfish, hostname)
        .getMasterLogicalSystem()
        .getDefaultRoutingInstance();
  }

  public static NamedBgpGroup getNamedBgpGroup(Batfish batfish, String hostname, String groupName) {
    return getDefaultRoutingInstance(batfish, hostname).getNamedBgpGroups().get(groupName);
  }

  public static List<ParseWarning> getParseWarnings(Batfish batfish, String hostname) {
    return batfish
        .loadParseVendorConfigurationAnswerElement(batfish.getSnapshot())
        .getWarnings()
        .getOrDefault("configs/" + hostname, new Warnings())
        .getParseWarnings();
  }

  private JunosGrammarTestUtils() {}
}
