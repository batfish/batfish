package org.batfish.grammar.palo_alto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import org.batfish.datamodel.Configuration;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public class PaloAltoGrammarTest {
  private static final String TESTCONFIGS_PREFIX = "org/batfish/grammar/palo_alto/testconfigs/";

  private static String TESTRIGS_PREFIX = "org/batfish/grammar/palo_alto/testrigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private Batfish getBatfishForConfigurationNames(String... configurationNames) throws IOException {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    return BatfishTestUtils.getBatfishForTextConfigs(_folder, names);
  }

  private Configuration parseConfig(String hostname) throws IOException {
    return parseTextConfigs(hostname).get(hostname);
  }

  private Map<String, Configuration> parseTextConfigs(String... configurationNames)
      throws IOException {
    return getBatfishForConfigurationNames(configurationNames).loadConfigurations();
  }

  @Test
  public void testHostname() throws IOException {
    String filename = "basic-parsing";
    String hostname = "my-hostname";
    Configuration c = parseConfig(filename);

    assertThat(parseTextConfigs(filename).keySet(), contains(hostname));
  }
}
