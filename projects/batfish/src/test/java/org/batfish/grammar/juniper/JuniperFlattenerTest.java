package org.batfish.grammar.juniper;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.batfish.common.BatfishLogger;
import org.batfish.common.Warnings;
import org.batfish.config.Settings;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.grammar.VendorConfigurationFormatDetector;
import org.batfish.grammar.flattener.Flattener;
import org.batfish.grammar.flattener.FlattenerLineMap;
import org.batfish.main.Batfish;
import org.junit.Test;

public class JuniperFlattenerTest {
  private static final String TESTCONFIGS_PREFIX = "org/batfish/grammar/juniper/testconfigs/";

  /** Test for https://github.com/batfish/batfish/issues/6149. */
  @Test
  public void testGH6149Flatten() {
    String hostname = "gh-6149-flatten";
    Flattener flattener =
        Batfish.flatten(
            readResource(TESTCONFIGS_PREFIX + hostname, UTF_8),
            new BatfishLogger(BatfishLogger.LEVELSTR_OUTPUT, false),
            new Settings(),
            new Warnings(),
            ConfigurationFormat.JUNIPER,
            VendorConfigurationFormatDetector.BATFISH_FLATTENED_JUNIPER_HEADER);
    assert flattener instanceof JuniperFlattener;
    String flatText = flattener.getFlattenedConfigurationText();
    // Flattening the configs does not lose either of the filter lines in the input.
    assertThat(
        flatText,
        allOf(
            containsString(
                "set groups FOO interfaces <*> unit <*> family inet filter input-list filterA"),
            containsString(
                "set groups FOO interfaces <*> unit <*> family inet filter input-list filterB")));
  }

  @Test
  public void testNestedConfigLineMap() {
    String hostname = "nested-config-with-flat-statements";
    Flattener flattener =
        Batfish.flatten(
            readResource(TESTCONFIGS_PREFIX + hostname, UTF_8),
            new BatfishLogger(BatfishLogger.LEVELSTR_OUTPUT, false),
            new Settings(),
            new Warnings(),
            ConfigurationFormat.JUNIPER,
            VendorConfigurationFormatDetector.BATFISH_FLATTENED_JUNIPER_HEADER);
    assert flattener instanceof JuniperFlattener;
    FlattenerLineMap lineMap = flattener.getOriginalLineMap();

    /*
     * Flattened config should be 3 lines: header, set-host-name, and set-security-policies.
     * This test checks the latter two.
     */

    {
      /* Confirm original line numbers are preserved for hierarchical words */
      String flatText = flattener.getFlattenedConfigurationText().split("\n", -1)[1];
      assertThat(lineMap.getOriginalLine(2, flatText.indexOf("system")), equalTo(2));
      assertThat(lineMap.getOriginalLine(2, flatText.indexOf("host-name")), equalTo(3));
      assertThat(
          lineMap.getOriginalLine(2, flatText.indexOf("nested-config-with-flat-statements")),
          equalTo(3));
    }
    /* Confirm original line number preserved for flat statements */
    {
      String statement =
          "set security policies from-zone A to-zone B policy P match source-address any";
      for (int index = 0; index < statement.length(); ++index) {
        assertThat(lineMap.getOriginalLine(3, index), equalTo(5));
      }
    }
  }
}
