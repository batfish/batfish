package org.batfish.grammar.cisco;

import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Configuration;
import org.batfish.main.BatfishTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/** Tests for {@link CiscoParser} and {@link CiscoControlPlaneExtractor} on IOS-XR files. */
public final class CiscoXrGrammarTest {

  private static final String TESTCONFIGS_PREFIX = "org/batfish/grammar/cisco/testconfigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Rule public ExpectedException _thrown = ExpectedException.none();

  private Map<String, Configuration> parseTextConfigs(String... configurationNames)
      throws IOException {
    String[] names =
        Arrays.stream(configurationNames).map(s -> TESTCONFIGS_PREFIX + s).toArray(String[]::new);
    return BatfishTestUtils.parseTextConfigs(_folder, names);
  }

  private @Nonnull Configuration parseConfig(String hostname) {
    try {
      Map<String, Configuration> configs = parseTextConfigs(hostname);
      assertThat(configs, hasKey(hostname.toLowerCase()));
      return configs.get(hostname.toLowerCase());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Regression test for a parser crash related to peer stack indexing issues.
   *
   * <p>The test config is a minimized version of user configuration submitted through Batfish
   * diagnostics.
   */
  @Test
  public void testXrBgpNeighbors() {
    // Don't crash.
    parseConfig("xr-bgp-neighbors");
  }
}
