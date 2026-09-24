package org.batfish.grammar;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
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
