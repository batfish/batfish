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

  /** Test for https://github.com/batfish/batfish/issue/6149. */
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
    String hostname = "nested-config";
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
     * Flattened config should be two lines: header line and set-host-name line
     * This test is only checking content of the set-host-name line
     */
    String flatText = flattener.getFlattenedConfigurationText().split("\n", -1)[1];

    /* Confirm original line numbers are preserved */
    assertThat(lineMap.getOriginalLine(2, flatText.indexOf("system")), equalTo(2));
    assertThat(lineMap.getOriginalLine(2, flatText.indexOf("host-name")), equalTo(3));
    assertThat(lineMap.getOriginalLine(2, flatText.indexOf("nested-config")), equalTo(3));
  }
}
