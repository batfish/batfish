package org.batfish.grammar.flatjuniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;

import java.io.IOException;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class JunosMplsLspTest {

  private static final String TESTCONFIGS_PREFIX = "org/batfish/grammar/juniper/testconfigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Batfish getBatfishForConfigurationNames(String... configurationNames) throws IOException {
    String[] names =
        java.util.Arrays.stream(configurationNames)
            .map(s -> TESTCONFIGS_PREFIX + s)
            .toArray(String[]::new);
    return BatfishTestUtils.getBatfishForTextConfigs(_folder, names);
  }

  @Test
  public void testMplsLspComprehensiveParsing() throws IOException {
    String hostname = "mpls-lsp-comprehensive";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    // Doesn't crash, produces configuration successfully.
    assertThat(batfish.loadConfigurations(batfish.getSnapshot()), hasKey(hostname));
  }
}
